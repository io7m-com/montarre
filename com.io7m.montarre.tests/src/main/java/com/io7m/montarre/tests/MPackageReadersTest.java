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


package com.io7m.montarre.tests;

import com.io7m.entomos.core.EoFileReaderType;
import com.io7m.entomos.core.EoFileReadersUnchecked;
import com.io7m.entomos.core.EoFileSection;
import com.io7m.jbssio.api.BSSWriterRandomAccessType;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import com.io7m.montarre.api.MCaptions;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MLanguageCode;
import com.io7m.montarre.api.MManifest;
import com.io7m.montarre.api.MModule;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MResourceRole;
import com.io7m.montarre.io.MPackageReaders;
import com.io7m.montarre.io.MPackageWriters;
import com.io7m.montarre.io.internal.MFileFormats;
import com.io7m.montarre.xml.MPackageDeclarationSerializers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MPackageReadersTest
{
  private MPackageWriters writers;
  private Path directory;
  private MPackageReaders readers;
  private MPackageDeclarationSerializers serializers;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
  {
    this.writers =
      new MPackageWriters();
    this.readers =
      new MPackageReaders();
    this.serializers =
      new MPackageDeclarationSerializers();
    this.directory =
      Objects.requireNonNull(directory, "directory");
  }

  @Test
  public void testMissingPackage()
    throws Exception
  {
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTemp =
      this.directory.resolve("out.mpk.tmp");

    try (final var writer =
           this.writers.create(
             outFile,
             outFileTemp,
             MExamplePackages.EMPTY_PACKAGE)) {

    }

    removeManifestFromArchive(outFile);

    final var ex =
      assertThrows(
        MException.class, () -> {
          this.readers.open(outFile);
        });

    assertEquals(
      "error-section-tag-first",
      ex.errorCode()
    );
  }

  @Test
  public void testCorruptPackage()
    throws Exception
  {
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTmp =
      this.directory.resolve("out.mpk.tmp");

    try (final var writer =
           this.writers.create(
             outFile,
             outFileTmp,
             MExamplePackages.EMPTY_PACKAGE)) {

    }

    replaceManifestInArchive(
      outFile,
      "<x>Not a package!".getBytes(StandardCharsets.UTF_8));

    final var ex =
      assertThrows(
        MException.class, () -> {
          this.readers.open(outFile);
        });

    assertEquals(
      "error-package-declaration-unparseable",
      ex.errorCode()
    );
  }

  @Test
  public void testCorruptZip()
    throws Exception
  {
    final var outFile =
      this.directory.resolve("out.mpk");

    Files.writeString(outFile, "THIS IS NOT A ZIP FILE");

    final var ex =
      assertThrows(
        MException.class, () -> {
          this.readers.open(outFile);
        });

    assertEquals(
      "error-file-tag-incorrect",
      ex.errorCode()
    );
  }

  @Test
  public void testEmptyPackage()
    throws Exception
  {
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTmp =
      this.directory.resolve("out.mpk.tmp");

    try (final var writer =
           this.writers.create(
             outFile,
             outFileTmp,
             MExamplePackages.EMPTY_PACKAGE)) {

    }

    try (final var reader = this.readers.open(outFile)) {
      assertEquals(MExamplePackages.EMPTY_PACKAGE, reader.packageDeclaration());
    }
  }

  @Test
  public void testOneFile()
    throws Exception
  {
    final var empty =
      this.directory.resolve("empty");
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTmp =
      this.directory.resolve("out.mpk.tmp");

    final var p =
      MExamplePackages.EMPTY_PACKAGE.withManifest(
        MManifest.builder()
          .addItems(
            new MResource(
              new MFileName("meta/bom.xml"),
              new MHash(
                new MHashAlgorithm("SHA-256"),
                new MHashValue(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
              ),
              0L,
              MResourceRole.BOM,
              Optional.of(MCaptions.ofTranslations(
                Map.entry(new MLanguageCode("en"), "A bill of materials."),
                Map.entry(new MLanguageCode("fr"), "Une nomenclature.")
              ))
            ))
          .build()
      );

    Files.createFile(empty);

    try (final var writer =
           this.writers.create(outFile, outFileTmp, p)) {
      writer.addFile(new MFileName("meta/bom.xml"), empty);
    }

    try (final var reader = this.readers.open(outFile)) {
      assertEquals(p, reader.packageDeclaration());
      reader.checkHash(new MFileName("meta/bom.xml"));
    }
  }

  @Test
  public void testFileMissing()
    throws Exception
  {
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTemp =
      this.directory.resolve("out.mpk.tmp");
    final var empty =
      this.directory.resolve("empty");

    final var p =
      MExamplePackages.EMPTY_PACKAGE.withManifest(
        MManifest.builder()
          .addItems(
            new MResource(
              new MFileName("meta/bom.xml"),
              new MHash(
                new MHashAlgorithm("SHA-256"),
                new MHashValue(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
              ),
              0L,
              MResourceRole.BOM,
              Optional.of(MCaptions.ofTranslations(
                Map.entry(new MLanguageCode("en"), "A bill of materials."),
                Map.entry(new MLanguageCode("fr"), "Une nomenclature.")
              ))
            ))
          .build()
      );

    Files.createFile(empty);

    try (final var writer =
           this.writers.create(outFile, outFileTemp, p)) {
      writer.addFile(new MFileName("meta/bom.xml"), empty);
    }

    MPackageReadersTest.removeFileFromArchive(outFile, new MFileName("meta/bom.xml"));

    final var ex =
      assertThrows(
        MException.class, () -> {
          try (final var reader = this.readers.open(outFile)) {
            assertEquals(p, reader.packageDeclaration());
          }
        });

    assertEquals("error-file-missing", ex.errorCode());
  }

  @Test
  public void testFileExtra()
    throws Exception
  {
    final var empty =
      this.directory.resolve("empty");
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTmp =
      this.directory.resolve("out.mpk.tmp");

    final var p =
      MExamplePackages.EMPTY_PACKAGE.withManifest(
        MManifest.builder()
          .addItems(
            new MResource(
              new MFileName("meta/bom.xml"),
              new MHash(
                new MHashAlgorithm("SHA-256"),
                new MHashValue(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
              ),
              0L,
              MResourceRole.BOM,
              Optional.of(MCaptions.ofTranslations(
                Map.entry(new MLanguageCode("en"), "A bill of materials."),
                Map.entry(new MLanguageCode("fr"), "Une nomenclature.")
              ))
            ))
          .build()
      );

    Files.createFile(empty);

    try (final var writer =
           this.writers.create(outFile, outFileTmp, p)) {
      writer.addFile(new MFileName("meta/bom.xml"), empty);
    }

    MPackageReadersTest.addFileToArchive(outFile, new byte[3]);
    MHexDump.dumpTo(outFile, System.out);

    final var ex =
      assertThrows(
        MException.class, () -> {
          this.readers.open(outFile);
        });

    assertEquals("error-file-entries-extra", ex.errorCode());
  }

  @Test
  public void testFileDisordered()
    throws Exception
  {
    final var file =
      this.directory.resolve("file");
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTmp =
      this.directory.resolve("out.mpk.tmp");

    final MManifest.Builder manifestBuilder = MManifest.builder();
    for (final var ch : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
      manifestBuilder.addItems(
        new MModule(
          new MFileName("lib/" + ch + ".jar"),
          5L,
          new MHash(
            new MHashAlgorithm("SHA-256"),
            new MHashValue(
              "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824")
          )
        )
      );
    }

    final var p =
      MExamplePackages.EMPTY_PACKAGE.withManifest(manifestBuilder.build());

    Files.writeString(file, "hello");

    try (final var writer = this.writers.create(outFile, outFileTmp, p)) {
      for (final var ch : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
        writer.addFile(new MFileName("lib/" + ch + ".jar"), file);
      }
    }

    shuffleArchive(outFile);
    MHexDump.dumpTo(outFile, System.out);

    final var ex =
      assertThrows(
        MException.class, () -> {
          this.readers.open(outFile);
        });

    assertEquals("error-file-entries-not-sorted", ex.errorCode());
  }

  private static void shuffleArchive(
    final Path filePath)
    throws Exception
  {
    final var tempPath =
      Files.createTempFile("file", ".mpk");

    final var unchecked =
      new EoFileReadersUnchecked();
    final var writers =
      new BSSWriters();

    try (var reader = unchecked.forFile(
      MFileFormats.fileIdentifier(),
      MFileFormats.sectionEndIdentifier(),
      filePath,
      null
    )) {
      try (var outChannel = FileChannel.open(
        tempPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
      )) {
        final var writer =
          writers.createWriterFromChannel(
            tempPath.toUri(),
            outChannel,
            "Root"
          );

        writer.writeU64BE(MFileFormats.fileIdentifier());
        writer.writeU32BE(1L);
        writer.writeU32BE(0L);

        final var sectionsMutable =
          new TreeSet<>(reader.sections());

        copySection(reader, sectionsMutable.pollFirst(), writer);
        sectionsMutable.pollLast();

        final var sectionsShuffle = new ArrayList<>(sectionsMutable);
        Collections.shuffle(sectionsShuffle);

        for (final var section : sectionsShuffle) {
          copySection(reader, section, writer);
        }

        writer.writeU64BE(MFileFormats.sectionEndIdentifier());
        writer.writeU64BE(0L);
      }
    }

    Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
  }

  private static void replaceManifestInArchive(
    final Path filePath,
    final byte[] data)
    throws Exception
  {
    final var tempPath =
      Files.createTempFile("file", ".mpk");

    final var unchecked =
      new EoFileReadersUnchecked();
    final var writers =
      new BSSWriters();

    try (var reader = unchecked.forFile(
      MFileFormats.fileIdentifier(),
      MFileFormats.sectionEndIdentifier(),
      filePath,
      null
    )) {
      try (var outChannel = FileChannel.open(
        tempPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
      )) {
        final var writer =
          writers.createWriterFromChannel(
            tempPath.toUri(),
            outChannel,
            "Root"
          );

        writer.writeU64BE(MFileFormats.fileIdentifier());
        writer.writeU32BE(1L);
        writer.writeU32BE(0L);

        final var sectionsMutable =
          new TreeSet<>(reader.sections());

        sectionsMutable.pollFirst();

        writer.writeU64BE(MFileFormats.sectionManifestIdentifier());
        writer.writeU64BE(data.length);
        writer.writeBytes(data);
        writer.align(16);

        for (final var section : sectionsMutable) {
          copySection(reader, section, writer);
        }
      }
    }

    Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
  }

  private static void removeFileFromArchive(
    final Path filePath,
    final MFileName fileName)
    throws Exception
  {
    final var tempPath =
      Files.createTempFile("file", ".mpk");

    final var unchecked =
      new EoFileReadersUnchecked();
    final var writers =
      new BSSWriters();
    final var readers =
      new BSSReaders();

    try (var reader = unchecked.forFile(
      MFileFormats.fileIdentifier(),
      MFileFormats.sectionEndIdentifier(),
      filePath,
      null
    )) {
      try (var outChannel = FileChannel.open(
        tempPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
      )) {
        final var writer =
          writers.createWriterFromChannel(
            tempPath.toUri(),
            outChannel,
            "Root"
          );

        writer.writeU64BE(MFileFormats.fileIdentifier());
        writer.writeU32BE(1L);
        writer.writeU32BE(0L);

        final var sectionsToCopy =
          new ArrayList<EoFileSection>();

        for (final var section : reader.sections()) {
          if (section.tag() == MFileFormats.sectionFileIdentifier()) {
            try (var chan = reader.dataChannel(section)) {
              final var data =
                readers.createReaderFromChannel(
                  URI.create("urn:x"),
                  chan,
                  "Section"
                );

              final var nameLength =
                data.readU32BE();
              final var nameData =
                new byte[(int) nameLength];
              data.readBytes(nameData);

              final var name =
                new String(nameData, StandardCharsets.UTF_8);

              if (new MFileName(name).equals(fileName)) {
                continue;
              }

              sectionsToCopy.add(section);
            }
          } else {
            sectionsToCopy.add(section);
          }
        }

        for (final var section : sectionsToCopy) {
          copySection(reader, section, writer);
        }
      }
    }

    Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
  }

  private static void addFileToArchive(
    final Path filePath,
    final byte[] data)
    throws Exception
  {
    final var tempPath =
      Files.createTempFile("file", ".mpk");

    final var unchecked =
      new EoFileReadersUnchecked();
    final var writers =
      new BSSWriters();

    try (var reader = unchecked.forFile(
      MFileFormats.fileIdentifier(),
      MFileFormats.sectionEndIdentifier(),
      filePath,
      null
    )) {
      try (var outChannel = FileChannel.open(
        tempPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
      )) {
        final var writer =
          writers.createWriterFromChannel(
            tempPath.toUri(),
            outChannel,
            "Root"
          );

        writer.writeU64BE(MFileFormats.fileIdentifier());
        writer.writeU32BE(1L);
        writer.writeU32BE(0L);

        final var sectionsMutable = new TreeSet<>(reader.sections());
        sectionsMutable.pollLast();

        for (final var section : sectionsMutable) {
          copySection(reader, section, writer);
        }

        writer.writeU64BE(MFileFormats.sectionFileIdentifier());

        final var dataSize = 4L + 3L + data.length;
        writer.writeU64BE(dataSize);
        writer.writeU32BE(3L);
        writer.writeBytes("ZZZ".getBytes(StandardCharsets.UTF_8));
        writer.writeBytes(data);
        writer.align(16);

        writer.writeU64BE(MFileFormats.sectionEndIdentifier());
        writer.writeU64BE(0L);
      }
    }

    Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
  }

  private static void removeManifestFromArchive(
    final Path filePath)
    throws Exception
  {
    final var tempPath =
      Files.createTempFile("file", ".mpk");

    final var unchecked =
      new EoFileReadersUnchecked();
    final var writers =
      new BSSWriters();

    try (var reader = unchecked.forFile(
      MFileFormats.fileIdentifier(),
      MFileFormats.sectionEndIdentifier(),
      filePath,
      null
    )) {
      try (var outChannel = FileChannel.open(
        tempPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
      )) {
        final var writer =
          writers.createWriterFromChannel(
            tempPath.toUri(),
            outChannel,
            "Root"
          );

        writer.writeU64BE(MFileFormats.fileIdentifier());
        writer.writeU32BE(1L);
        writer.writeU32BE(0L);

        final var sectionsToCopy = new ArrayList<>(reader.sections());
        sectionsToCopy.removeFirst();

        for (final var section : sectionsToCopy) {
          copySection(reader, section, writer);
        }
      }
    }

    Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
  }

  private static void copySection(
    final EoFileReaderType reader,
    final EoFileSection section,
    final BSSWriterRandomAccessType writer)
    throws Exception
  {
    writer.writeU64BE(section.tag());
    writer.writeU64BE(section.dataSize());
    try (var data = reader.dataChannel(section)) {
      try (var stream = Channels.newInputStream(data)) {
        writer.writeBytes(stream.readAllBytes());
        writer.align(16);
      }
    }
  }
}
