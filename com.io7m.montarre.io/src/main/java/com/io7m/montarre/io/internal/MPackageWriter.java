/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
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
import org.apache.commons.io.output.CloseShieldOutputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A package writer.
 */

public final class MPackageWriter implements MPackageWriterType
{
  private static final Instant SOURCE_EPOCH =
    Instant.parse("2010-01-01T00:00:00+00:00");
  private static final FileTime SOURCE_EPOCH_FILE_TIME =
    FileTime.from(SOURCE_EPOCH);

  private final MPackageDeclarationSerializerFactoryType serializers;
  private final HashMap<String, Object> attributes;
  private final Path archiveFile;
  private final Path archiveFileTmp;
  private final MPackageDeclaration packageV;
  private final CloseableCollectionType<ClosingResourceFailedException> resources;
  private final ZipOutputStream zip;
  private final HashMap<MFileName, MHash> writtenHashes;
  private final AtomicBoolean closed;

  /**
   * A package writer.
   *
   * @param inSerializers The serializer factory
   * @param inStream      The output stream
   * @param inAttributes  The error attributes
   * @param inBuffered    The buffered output
   * @param inFile        The file
   * @param inFileTmp     The temporary file
   * @param inPackageV    The package declaration
   */

  public MPackageWriter(
    final MPackageDeclarationSerializerFactoryType inSerializers,
    final HashMap<String, Object> inAttributes,
    final OutputStream inStream,
    final BufferedOutputStream inBuffered,
    final Path inFile,
    final Path inFileTmp,
    final MPackageDeclaration inPackageV)
  {
    this.serializers =
      Objects.requireNonNull(inSerializers, "inSerializers");
    this.attributes =
      Objects.requireNonNull(inAttributes, "attributes");
    this.resources =
      CloseableCollection.create();

    this.resources.add(inStream);
    this.resources.add(inBuffered);
    this.zip = this.resources.add(new ZipOutputStream(inBuffered, UTF_8));

    this.archiveFile =
      Objects.requireNonNull(inFile, "file");
    this.archiveFileTmp =
      Objects.requireNonNull(inFileTmp, "fileTmp");
    this.packageV =
      Objects.requireNonNull(inPackageV, "packageV");

    this.closed =
      new AtomicBoolean(false);

    this.writtenHashes =
      new HashMap<>();
  }

  /**
   * Start the file.
   *
   * @throws MException On errors
   */

  public void start()
    throws MException
  {
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

    final var entry =
      new ZipEntry(
        MReservedNames.montarrePackage().name().toUpperCase(Locale.ROOT)
      );
    entry.setCreationTime(SOURCE_EPOCH_FILE_TIME);
    entry.setLastAccessTime(SOURCE_EPOCH_FILE_TIME);
    entry.setLastModifiedTime(SOURCE_EPOCH_FILE_TIME);
    entry.setSize(packageData.length);

    try {
      this.zip.putNextEntry(entry);
      this.zip.write(packageData);
      this.zip.closeEntry();
    } catch (final IOException e) {
      throw this.errorIO(e);
    }
  }

  @Override
  public void addFile(
    final MFileName name,
    final Path file)
    throws MException
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(file, "file");

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

    try {
      final var entry = new ZipEntry(name.name().toUpperCase(Locale.ROOT));
      entry.setCreationTime(SOURCE_EPOCH_FILE_TIME);
      entry.setLastAccessTime(SOURCE_EPOCH_FILE_TIME);
      entry.setLastModifiedTime(SOURCE_EPOCH_FILE_TIME);
      entry.setSize(Files.size(file));
      this.zip.putNextEntry(entry);

      final var closeShield =
        CloseShieldOutputStream.wrap(this.zip);
      try (final var digestStream =
             new DigestOutputStream(closeShield, digest)) {
        Files.copy(file, digestStream);

        try {
          this.zip.closeEntry();
        } catch (final IOException e) {
          throw this.errorIO(e);
        }

        this.checkDigest(digest, declared.hash());
        this.writtenHashes.put(name, declared.hash());
      }
    } catch (final IOException e) {
      throw this.errorIO(e);
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

  private MException errorIO(
    final IOException e)
  {
    return new MException(
      Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()),
      e,
      "error-io",
      this.copyAttributes(),
      Optional.empty()
    );
  }

  @Override
  public void close()
    throws MException
  {
    if (this.closed.compareAndSet(false, true)) {
      try {
        this.validate();
        this.resources.close();
      } catch (final ClosingResourceFailedException e) {
        throw new MException(e.getMessage(), e, "error-close");
      }

      try {
        Files.move(
          this.archiveFileTmp,
          this.archiveFile,
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE
        );
      } catch (final IOException e) {
        throw this.errorIO(e);
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
    } catch (final IOException e) {
      throw this.errorIO(e);
    }
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
