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

import com.io7m.anethum.api.ParsingException;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MModule;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPlatformDependentModule;
import com.io7m.montarre.api.MReservedNames;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.parsers.MPackageDeclarationParserFactoryType;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A package reader.
 */

public final class MPackageReader implements MPackageReaderType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MPackageReader.class);

  private static final Instant SOURCE_EPOCH =
    Instant.parse("2024-10-14T00:00:00+00:00");
  private static final FileTime SOURCE_EPOCH_FILETIME =
    FileTime.from(SOURCE_EPOCH);

  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };

  private final ZipFile zipFile;
  private final MPackageDeclarationParserFactoryType parsers;
  private final HashMap<String, Object> attributes;
  private final HashMap<MFileName, ZipArchiveEntry> entries;
  private MPackageDeclaration packageV;

  /**
   * A package reader.
   *
   * @param file      The file
   * @param inZipFile The zip file
   * @param inParsers The parsers
   */

  public MPackageReader(
    final Path file,
    final ZipFile inZipFile,
    final MPackageDeclarationParserFactoryType inParsers)
  {
    this.zipFile =
      Objects.requireNonNull(inZipFile, "zipFile");
    this.parsers =
      Objects.requireNonNull(inParsers, "parsers");

    this.attributes = new HashMap<>();
    this.attributes.put("File", file);
    this.entries = new HashMap<>();
  }

  /**
   * Open the package declaration and parse it.
   *
   * @throws MException On errors
   */

  public void start()
    throws MException
  {
    final var packageEntry =
      this.zipFile.getEntry(MReservedNames.montarrePackage().name());

    if (packageEntry == null) {
      throw this.errorNoPackage();
    }

    try (final var stream = this.zipFile.getInputStream(packageEntry)) {
      this.packageV =
        this.parsers.parse(
          URI.create(MReservedNames.montarrePackage().name()),
          stream
        );
    } catch (final IOException e) {
      throw this.errorIO(e);
    } catch (final ParsingException e) {
      throw this.errorParsing(e);
    }

    for (final var item : this.packageV.manifest().items()) {
      final var itemFile =
        item.file();
      final var entryName =
        itemFile.name().toUpperCase(Locale.ROOT);
      final var entry =
        this.zipFile.getEntry(entryName);

      this.attributes.put("Entry Name", entryName);

      if (entry == null) {
        throw this.errorMissingPackageEntry();
      }

      this.entries.put(itemFile, entry);
    }
  }

  private MException errorMissingPackageEntry()
  {
    return new MException(
      "The package declaration specifies a file that does not exist in the archive.",
      "error-file-missing",
      this.copyAttributes()
    );
  }

  private MException errorParsing(
    final ParsingException e)
  {
    return new MException(
      "The package declaration in the given file was not parseable.",
      e,
      "error-package-declaration-unparseable",
      this.copyAttributes()
    );
  }

  private MException errorNoPackage()
  {
    this.attributes.put("Expected Entry", MReservedNames.montarrePackage());

    return new MException(
      "No package declaration exists in the given file.",
      "error-package-declaration-missing",
      this.copyAttributes()
    );
  }

  private MException errorNoSuchEntry()
  {
    return new MException(
      "No such file.",
      "error-file-nonexistent",
      this.copyAttributes()
    );
  }

  @Override
  public MPackageDeclaration packageDeclaration()
  {
    return this.packageV;
  }

  @Override
  public void close()
    throws MException
  {
    try {
      this.zipFile.close();
    } catch (final IOException e) {
      throw this.errorIO(e);
    }
  }

  @Override
  public InputStream readFile(
    final MFileName file)
    throws MException
  {
    this.attributes.put("File", file);

    final var item =
      this.packageV.manifest()
        .itemsMap()
        .get(file);

    final var zipEntry =
      this.entries.get(file);

    if (item == null || zipEntry == null) {
      throw this.errorNoSuchEntry();
    }

    try {
      return this.zipFile.getInputStream(zipEntry);
    } catch (final IOException e) {
      throw this.errorIO(e);
    }
  }

  @Override
  public void checkHash(
    final MFileName file)
    throws MException
  {
    Objects.requireNonNull(file, "file");

    this.attributes.put("File", file);

    final var item =
      this.packageV.manifest()
        .itemsMap()
        .get(file);

    final var zipEntry =
      this.entries.get(file);

    if (item == null || zipEntry == null) {
      throw this.errorNoSuchEntry();
    }

    final MessageDigest digest;
    try {
      final var algorithmName = item.hash().algorithm().name();
      this.attributes.put("Hash Algorithm", algorithmName);
      digest = MessageDigest.getInstance(algorithmName);
    } catch (final NoSuchAlgorithmException e) {
      throw this.errorHashSupport(e);
    }

    try (final var zipStream = this.zipFile.getInputStream(zipEntry)) {
      try (final var digestStream = new DigestInputStream(zipStream, digest)) {
        digestStream.transferTo(OutputStream.nullOutputStream());
      }
    } catch (final IOException e) {
      throw this.errorIO(e);
    }

    this.checkDigest(digest, item.hash());
  }

  @Override
  public void unpackInto(
    final Path output,
    final Function<MPlatformDependentModule, PlatformDependentModulePolicy> filterPlatform)
    throws MException
  {
    Objects.requireNonNull(output, "output");

    try {
      this.unpackZip(output, filterPlatform);
    } catch (final IOException e) {
      throw this.errorIO(e);
    }
  }

  private void unpackZip(
    final Path outputDirectory,
    final Function<MPlatformDependentModule, PlatformDependentModulePolicy> filterPlatform)
    throws IOException
  {
    LOG.debug("Unpacking…");

    this.attributes.clear();

    final var metaInfDir =
      outputDirectory.resolve("META-INF");
    final var metaDir =
      outputDirectory.resolve("meta");
    final var libDir =
      outputDirectory.resolve("lib");

    Files.createDirectories(metaInfDir);
    setFakeTime(metaInfDir);
    Files.createDirectories(metaDir);
    setFakeTime(metaDir);
    Files.createDirectories(libDir);
    setFakeTime(libDir);

    this.unpackDeclaration(metaInfDir);

    for (final var item : this.packageV.manifest().items()) {
      final var entry =
        this.entries.get(item.file());

      this.attributes.put("File", item.file());

      switch (item) {
        case final MResource ignored -> {
          final var entryPath =
            Paths.get(item.file().name());

          this.copyEntry(entry, metaDir.resolve(entryPath.getFileName()));
        }

        case final MModule ignored -> {
          final var entryPath =
            Paths.get(item.file().name());

          this.copyEntry(entry, libDir.resolve(entryPath.getFileName()));
        }

        case final MPlatformDependentModule platformModule -> {
          switch (filterPlatform.apply(platformModule)) {
            case IGNORE -> {
              // Do nothing.
            }
            case MERGE -> {
              final var entryPath = Paths.get(item.file().name());
              this.copyEntry(entry, libDir.resolve(entryPath.getFileName()));
            }
            case INCLUDE -> {
              final var entryPath =
                Paths.get(item.file().name());
              final var archDir =
                libDir.resolve(platformModule.architecture().name());
              final var osDir =
                archDir.resolve(platformModule.operatingSystem().name());

              Files.createDirectories(archDir);
              setFakeTime(archDir);
              Files.createDirectories(osDir);
              setFakeTime(osDir);

              this.copyEntry(entry, osDir.resolve(entryPath.getFileName()));
            }
          }
        }
      }
    }
  }

  private void unpackDeclaration(
    final Path metaInfDir)
    throws IOException
  {
    final var entry =
      this.zipFile.getEntry(MReservedNames.montarrePackage().name());

    try (final var stream = this.zipFile.getInputStream(entry)) {
      final var directory = metaInfDir.resolve("MONTARRE");
      Files.createDirectories(directory);
      setFakeTime(directory);
      Files.copy(stream, directory.resolve("PACKAGE.XML"));
    }
  }

  private void copyEntry(
    final ZipArchiveEntry entry,
    final Path outFile)
    throws IOException
  {
    try (final var outStream =
           Files.newOutputStream(outFile, OPEN_OPTIONS)) {
      try (final var inStream = this.zipFile.getInputStream(entry)) {
        inStream.transferTo(outStream);
        outStream.flush();
      }
      setFakeTime(outFile);
    }
  }

  private static void setFakeTime(
    final Path outFile)
    throws IOException
  {
    Files.setLastModifiedTime(
      outFile,
      SOURCE_EPOCH_FILETIME
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

  private MException errorHashSupport(
    final NoSuchAlgorithmException e)
  {
    return new MException(
      "Hash algorithm not supported.",
      "error-hash-support",
      this.copyAttributes()
    );
  }

  private Map<String, String> copyAttributes()
  {
    return this.attributes.entrySet()
      .stream()
      .map(e -> Map.entry(e.getKey(), e.getValue().toString()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
