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
import com.io7m.entomos.core.EoException;
import com.io7m.entomos.core.EoFileReaderType;
import com.io7m.entomos.core.EoFileReadersChecked;
import com.io7m.entomos.core.EoFileReadersUnchecked;
import com.io7m.entomos.core.EoFileSection;
import com.io7m.jbssio.api.BSSReaderProviderType;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MManifestItemType;
import com.io7m.montarre.api.MModule;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPlatformDependentModule;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.parsers.MPackageDeclarationParserFactoryType;
import com.io7m.wendover.core.SubrangeSeekableByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A package reader.
 */

public final class MPackageReader implements MPackageReaderType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MPackageReader.class);

  private static final BSSReaderProviderType READERS =
    new BSSReaders();
  private static final EoFileReadersChecked FILE_READERS_CHECKED =
    new EoFileReadersChecked(READERS);
  private static final EoFileReadersUnchecked FILE_READERS_UNCHECKED =
    new EoFileReadersUnchecked(READERS);

  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };

  private final MPackageDeclarationParserFactoryType parsers;
  private final HashMap<String, Object> attributes;
  private final FileChannel fileChannel;
  private final EoFileReaderType fileReader;
  private final Path file;
  private final ArrayList<MNamedFileEntry> fileEntryList;
  private final HashMap<MFileName, MNamedFileEntry> fileEntryMap;
  private MPackageDeclaration packageV;

  /**
   * A package reader.
   *
   * @param inFile    The file
   * @param inParsers The parsers
   */

  public MPackageReader(
    final Path inFile,
    final MPackageDeclarationParserFactoryType inParsers)
    throws IOException, EoException
  {
    this.file =
      Objects.requireNonNull(inFile, "File");
    this.parsers =
      Objects.requireNonNull(inParsers, "parsers");

    this.attributes = new HashMap<>();
    this.attributes.put("File", inFile);

    this.fileChannel =
      FileChannel.open(inFile, StandardOpenOption.READ);

    this.fileReader =
      FILE_READERS_CHECKED.forChannel(
        inFile.toUri(),
        MFileFormats.fileIdentifier(),
        MFileFormats.sectionEndIdentifier(),
        this.fileChannel,
        MFileFormats.fileFormats()
      );

    this.fileEntryList =
      new ArrayList<>();
    this.fileEntryMap =
      new HashMap<>();
  }

  /**
   * Extract a manifest from the file.
   *
   * @param file The file
   *
   * @return The manifest bytes
   *
   * @throws MException On errors
   */

  public static byte[] extractManifest(
    final Path file)
    throws MException
  {
    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
      try (var reader = FILE_READERS_UNCHECKED.forChannel(
        file.toUri(),
        MFileFormats.fileIdentifier(),
        MFileFormats.sectionEndIdentifier(),
        channel,
        null)) {
        final var sectionOpt =
          reader.sections()
            .stream()
            .filter(s -> s.tag() == MFileFormats.sectionManifestIdentifier())
            .findFirst();

        if (sectionOpt.isEmpty()) {
          throw errorNoManifestSection(file);
        }

        final var section =
          sectionOpt.get();
        final var dataChannel =
          reader.dataChannel(section);
        final var dataStream =
          Channels.newInputStream(dataChannel);

        return dataStream.readAllBytes();
      }
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  private static MException errorNoManifestSection(
    final Path file)
  {
    return new MException(
      "Could not locate a manifest section in the given file.",
      "error-manifest-section-missing",
      Map.of("File", file.toString()),
      Optional.empty()
    );
  }

  private record MNamedFileEntry(
    MFileName name,
    EoFileSection section,
    long fileDataOffset)
  {

  }

  /**
   * Open the package declaration and parse it.
   *
   * @throws MException On errors
   */

  public void start()
    throws MException
  {
    try {
      this.readFileEntries();
      this.readManifest();
      this.checkEntriesNoExtras(this.packageV.manifest().items());
      this.checkEntriesNoMissing(this.packageV.manifest().items());
      this.checkEntriesSorted();
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  private void readManifest()
    throws Exception
  {
    final var section =
      this.fileReader.sections().first();

    try (var channel = this.fileReader.dataChannel(section)) {
      try (var bounded = new SubrangeSeekableByteChannel(
        channel, 0L, section.dataSize())) {
        try (var stream = Channels.newInputStream(bounded)) {
          this.packageV = this.parsers.parse(this.file.toUri(), stream);
        }
      }
    } catch (final ParsingException e) {
      throw this.errorParsing(e);
    }
  }

  private void readFileEntries()
    throws Exception
  {
    for (final var section : this.fileReader.sections()) {
      if (section.tag() == MFileFormats.sectionFileIdentifier()) {
        final var data =
          this.fileReader.dataChannel(section);
        final var sectionReader =
          READERS.createReaderFromChannelBounded(
            this.file.toUri(),
            data,
            "FileSection",
            section.dataSize()
          );

        final var nameLength =
          sectionReader.readU32BE("NameLength");
        final var nameData =
          new byte[(int) nameLength];

        sectionReader.readBytes("NameData", nameData);

        final var name =
          new String(nameData, StandardCharsets.UTF_8);
        final var fileName =
          new MFileName(name);

        this.fileEntryList.add(
          new MNamedFileEntry(
            fileName,
            section,
            sectionReader.offsetCurrentRelative()
          )
        );
      }
    }
  }

  /**
   * There must be no extra entries in the file outside of what the
   * manifest specifies.
   */

  private void checkEntriesNoExtras(
    final List<MManifestItemType> items)
    throws MException
  {
    for (final var entry : this.fileEntryList) {
      this.fileEntryMap.put(entry.name, entry);
    }

    final var entryMap = new HashMap<>(this.fileEntryMap);
    for (final var item : items) {
      final var nameUpper =
        new MFileName(item.file().name().toUpperCase(Locale.ROOT));
      entryMap.remove(nameUpper);
    }

    if (!entryMap.isEmpty()) {
      throw this.errorExtraUnlistedEntries(entryMap.keySet());
    }
  }

  /**
   * Each entry that appears in the manifest must appear in the file.
   */

  private void checkEntriesNoMissing(
    final List<MManifestItemType> items)
    throws MException
  {
    for (final var item : items) {
      final var nameUpper =
        new MFileName(item.file().name().toUpperCase(Locale.ROOT));

      this.attributes.put("Entry Name", nameUpper);
      if (!this.fileEntryMap.containsKey(nameUpper)) {
        throw this.errorMissingPackageEntry();
      }
    }
    this.attributes.remove("Entry Name");
  }

  private MException errorExtraUnlistedEntries(
    final Set<MFileName> names)
  {
    final var iter = names.iterator();
    for (var index = 0; index < names.size(); ++index) {
      this.attributes.put(
        "Extra Entry (%s)".formatted(index),
        iter.next()
      );
    }

    return new MException(
      "The file archive contains entries not listed in the manifest.",
      "error-file-entries-extra",
      this.copyAttributes()
    );
  }

  /**
   * Package entries must be written in alphabetical order.
   */

  private void checkEntriesSorted()
    throws MException
  {
    final var entriesSorted =
      this.fileEntryList.stream()
        .sorted(Comparator.comparing(o -> o.name))
        .toList();

    if (!this.fileEntryList.equals(entriesSorted)) {
      throw this.errorNotSorted();
    }
  }

  private MException errorNotSorted()
  {
    return new MException(
      "The file archive entries are not sorted.",
      "error-file-entries-not-sorted",
      this.copyAttributes()
    );
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
      this.fileChannel.close();
    } catch (final IOException e) {
      throw MException.wrap(e);
    }
  }

  @Override
  public InputStream readFile(
    final MFileName fileName)
    throws MException
  {
    this.attributes.put("File", fileName);

    final var item =
      this.packageV.manifest()
        .itemsMap()
        .get(fileName);

    final var fileEntry =
      this.fileEntryMap.get(fileName);

    if (item == null || fileEntry == null) {
      throw this.errorNoSuchEntry();
    }

    try {
      final var channel = this.fileReader.dataChannel(fileEntry.section);
      channel.position(fileEntry.fileDataOffset);
      return Channels.newInputStream(channel);
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  @Override
  public void checkHash(
    final MFileName fileName)
    throws MException
  {
    Objects.requireNonNull(fileName, "file");

    this.attributes.put("File", fileName);

    final var item =
      this.packageV.manifest()
        .itemsMap()
        .get(fileName);

    final var fileEntry = this.fileEntryMap.get(fileName);
    if (item == null || fileEntry == null) {
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

    try {
      final var channel =
        this.fileReader.dataChannel(fileEntry.section);

      channel.position(fileEntry.fileDataOffset);
      try (final var stream = Channels.newInputStream(channel)) {
        try (final var digestStream = new DigestInputStream(stream, digest)) {
          digestStream.transferTo(OutputStream.nullOutputStream());
        }
      }

      this.checkDigest(digest, item.hash());
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  @Override
  public void unpackInto(
    final Path output,
    final Function<MPlatformDependentModule, PlatformDependentModulePolicy> filterPlatform)
    throws MException
  {
    Objects.requireNonNull(output, "output");
    this.unpackArchive(output, filterPlatform);
  }

  private void unpackArchive(
    final Path outputDirectory,
    final Function<MPlatformDependentModule, PlatformDependentModulePolicy> filterPlatform)
    throws MException
  {
    LOG.debug("Unpacking…");

    this.attributes.clear();

    try {
      final var metaInfDir =
        outputDirectory.resolve("META-INF");
      final var metaDir =
        outputDirectory.resolve("meta");
      final var libDir =
        outputDirectory.resolve("lib");

      Files.createDirectories(metaInfDir);
      Files.createDirectories(metaDir);
      Files.createDirectories(libDir);

      this.unpackDeclaration(metaInfDir);

      for (final var item : this.packageV.manifest().items()) {
        final var entry = this.findEntry(item.file());
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
                Files.createDirectories(osDir);

                this.copyEntry(entry, osDir.resolve(entryPath.getFileName()));
              }
            }
          }
        }
      }
    } catch (final IOException e) {
      throw MException.wrap(e);
    }
  }

  private void copyEntry(
    final MNamedFileEntry entry,
    final Path path)
    throws MException
  {
    try (var channel = this.fileReader.dataChannel(entry.section)) {
      try (var stream = Channels.newInputStream(channel)) {
        stream.skipNBytes(entry.fileDataOffset);
        Files.copy(stream, path);
      }
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
  }

  private MNamedFileEntry findEntry(
    final MFileName newFile)
    throws MException
  {
    final var nameUpper =
      new MFileName(newFile.name().toUpperCase(Locale.ROOT));
    final var e =
      this.fileEntryMap.get(nameUpper);

    if (e == null) {
      throw this.errorNoSuchEntry();
    }
    return e;
  }

  private void unpackDeclaration(
    final Path metaInfDir)
    throws MException
  {
    final var section =
      this.fileReader.sections().first();

    try (var channel = this.fileReader.dataChannel(section)) {
      try (var bounded = new SubrangeSeekableByteChannel(
        channel, 0L, section.dataSize())) {
        try (var stream = Channels.newInputStream(bounded)) {
          final var directory = metaInfDir.resolve("MONTARRE");
          Files.createDirectories(directory);
          Files.copy(stream, directory.resolve("PACKAGE.XML"));
        }
      }
    } catch (final Exception e) {
      throw MException.wrap(e);
    }
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
