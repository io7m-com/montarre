/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.montarre.io.internal;

import com.io7m.anethum.api.SerializationException;
import com.io7m.jbssio.api.BSSWriterProviderType;
import com.io7m.jbssio.api.BSSWriterRandomAccessType;
import com.io7m.jbssio.vanilla.BSSWriters;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MReservedNames;
import com.io7m.montarre.api.io.MPackageWriterType;
import com.io7m.montarre.api.parsers.MPackageDeclarationSerializerFactoryType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * The default package writer implementation.
 */

public final class MPackageWriter implements MPackageWriterType
{
  private static final BSSWriterProviderType WRITERS =
    new BSSWriters();

  private final MPackageDeclarationSerializerFactoryType serializers;
  private final Path archiveFile;
  private final Path archiveFileTmp;
  private final MPackageDeclaration packageV;
  private final CloseableCollectionType<ClosingResourceFailedException> resources;
  private final HashMap<MFileName, MHash> writtenHashes;
  private final AtomicBoolean closed;
  private final FileChannel channel;
  private final HashMap<String, Object> attributes;
  private BSSWriterRandomAccessType writer;

  /**
   * Construct a package writer.
   *
   * @param inSerializers    The serializers
   * @param inArchiveFile    The archive file
   * @param inArchiveFileTmp The temporary archive file
   * @param inPackageV       The package declaration
   *
   * @throws IOException On errors
   */

  public MPackageWriter(
    final MPackageDeclarationSerializerFactoryType inSerializers,
    final Path inArchiveFile,
    final Path inArchiveFileTmp,
    final MPackageDeclaration inPackageV)
    throws IOException
  {
    this.serializers =
      Objects.requireNonNull(inSerializers, "Serializers");
    this.archiveFile =
      Objects.requireNonNull(inArchiveFile, "ArchiveFile");
    this.archiveFileTmp =
      Objects.requireNonNull(inArchiveFileTmp, "ArchiveFileTmp");
    this.packageV =
      Objects.requireNonNull(inPackageV, "Package");

    this.resources =
      CloseableCollection.create();

    this.channel =
      this.resources.add(
        FileChannel.open(
          this.archiveFileTmp,
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING
        )
      );

    this.attributes =
      new HashMap<>();
    this.writtenHashes =
      new HashMap<>();
    this.closed =
      new AtomicBoolean(false);
  }

  @Override
  public void addFile(
    final MFileName name,
    final Path file)
    throws MException
  {
    try {
      this.checkNotClosed();
      this.attributes.put("File", name);

      if (MReservedNames.isReserved(name)) {
        throw this.errorReserved();
      }

      final var declared =
        this.packageV.manifest()
          .itemsMap()
          .get(name);

      if (declared == null) {
        throw this.errorNotDeclared();
      }

      if (this.writtenHashes.containsKey(name)) {
        throw this.errorFileAlreadyWritten();
      }

      this.attributes.put("Hash Algorithm", declared.hash().algorithm().name());

      final MessageDigest digest;
      try {
        digest = MessageDigest.getInstance(declared.hash().algorithm().name());
      } catch (final NoSuchAlgorithmException e) {
        throw this.errorHashSupport(e);
      }

      this.writer.writeU64BE(MFileFormats.sectionFileIdentifier());

      final var fileSize =
        Files.size(file);
      final var nameUTF8 =
        name.name()
          .toUpperCase(Locale.ROOT)
          .getBytes(StandardCharsets.UTF_8);

      var dataSize = 4L;
      dataSize += nameUTF8.length;
      dataSize += fileSize;

      this.writer.writeU64BE(dataSize);
      this.writer.writeU32BE(nameUTF8.length);
      this.writer.writeBytes(nameUTF8);

      final var buffer = new byte[4096];
      try (var stream = Files.newInputStream(file)) {
        while (true) {
          final var r = stream.read(buffer);
          if (r == -1) {
            break;
          }
          digest.update(buffer, 0, r);
          this.writer.writeBytes(buffer, 0, r);
        }
      }
      this.writer.align(16);

      this.checkDigest(digest, declared.hash());
      this.writtenHashes.put(name, declared.hash());
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  @Override
  public void close()
    throws MException
  {
    if (this.closed.compareAndSet(false, true)) {
      try {
        this.validate();

        this.writer.writeU64BE(MFileFormats.sectionEndIdentifier());
        this.writer.writeU64BE(0L);
        this.resources.close();

        Files.move(
          this.archiveFileTmp,
          this.archiveFile,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING
        );
      } catch (final Exception e) {
        throw MException.wrap(e);
      }
    }
  }

  @Override
  public void packFrom(
    final Path inputDirectory)
    throws MException
  {
    Objects.requireNonNull(inputDirectory, "inputDirectory");

    try (final var stream = Files.walk(inputDirectory)) {
      final var files =
        stream.filter(Files::isRegularFile)
          .sorted()
          .toList();

      for (final var file : files) {
        final var entryName =
          new MFileName(
            inputDirectory.relativize(file)
              .toString()
              .replace('\\', '/')
          );

        if (MReservedNames.isReserved(entryName)) {
          continue;
        }

        this.addFile(entryName, file);
      }
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  /**
   * Start writing data.
   *
   * @throws MException On errors
   */

  public void start()
    throws MException
  {
    try {
      this.writer =
        WRITERS.createWriterFromChannel(
          this.archiveFileTmp.toUri(),
          this.channel,
          "Root"
        );

      this.writer.seekTo(0L);
      this.writer.writeU64BE(MFileFormats.fileIdentifier());
      this.writer.writeU32BE(1);
      this.writer.writeU32BE(0);

      final byte[] packageData;
      try (final var byteOut = new ByteArrayOutputStream()) {
        this.serializers.serialize(
          URI.create("urn:unavailable"),
          byteOut,
          this.packageV
        );
        packageData = byteOut.toByteArray();
      } catch (final SerializationException | IOException e) {
        throw new MException(e.getMessage(), e, "error-serialization");
      }

      this.writer.writeU64BE(MFileFormats.sectionManifestIdentifier());
      this.writer.writeU64BE(Integer.toUnsignedLong(packageData.length));
      this.writer.writeBytes(packageData);
      this.writer.align(16);
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  private void checkNotClosed()
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Writer is closed.");
    }
  }

  private MException errorHashSupport(
    final NoSuchAlgorithmException e)
  {
    return new MException(
      "Hash algorithm not supported.",
      "error-hash-support",
      this.copyAttributes()
    );
  }

  private void checkDigest(
    final MessageDigest digest,
    final MHash hash)
    throws MException
  {
    final var hex =
      HexFormat.of();
    final var received =
      hex.formatHex(digest.digest());
    final var expected =
      hash.value().value();

    if (!Objects.equals(expected, received)) {
      this.attributes.put("Hash (Expected)", expected);
      this.attributes.put("Hash (Received)", received);

      throw new MException(
        "Hash value does not match.",
        "error-hash-mismatch",
        this.copyAttributes(),
        Optional.empty()
      );
    }
  }

  private MException errorReserved()
  {
    return new MException(
      "The file name is reserved and cannot be used for archive content.",
      "error-file-name-reserved",
      this.copyAttributes()
    );
  }

  private MException errorFileAlreadyWritten()
  {
    return new MException(
      "A file with the given name has already been written.",
      "error-file-already-written",
      this.copyAttributes(),
      Optional.of("Only write files once!")
    );
  }

  private MException errorNotDeclared()
  {
    return new MException(
      "No file with the given name is declared in the package manifest.",
      "error-file-undeclared",
      this.copyAttributes(),
      Optional.of(
        "Correct the manifest if this file is supposed to be included.")
    );
  }

  private Map<String, String> copyAttributes()
  {
    return this.attributes.entrySet()
      .stream()
      .map(e -> Map.entry(e.getKey(), e.getValue().toString()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private void validate()
    throws MException
  {
    for (final var file : this.packageV.manifest().items()) {
      if (!this.writtenHashes.containsKey(file.file())) {
        this.attributes.put("File", file.file());
        throw new MException(
          "At least one file in the manifest was not provided!",
          "error-file-missed",
          this.copyAttributes()
        );
      }
    }
  }
}
