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

import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.MApplicationKind;
import com.io7m.montarre.api.MCopying;
import com.io7m.montarre.api.MJavaInfo;
import com.io7m.montarre.api.MLink;
import com.io7m.montarre.api.MLinkRole;
import com.io7m.montarre.api.MNames;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MManifest;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPackageName;
import com.io7m.montarre.api.MReservedNames;
import com.io7m.montarre.api.MResourceRole;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVendorID;
import com.io7m.montarre.api.MVendorName;
import com.io7m.montarre.api.MVersion;
import com.io7m.montarre.io.MPackageReaders;
import com.io7m.montarre.io.MPackageWriters;
import com.io7m.montarre.xml.MPackageDeclarationSerializers;
import com.io7m.verona.core.Version;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MPackageReadersTest
{
  private static final MPackageDeclaration EMPTY_PACKAGE =
    MPackageDeclaration.builder()
      .setMetadata(
        MMetadata.builder()
          .setCopying(
            MCopying.builder()
              .setCopyright("Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com")
              .setLicense("ISC")
              .build()
          )
          .setDescription("An example package.")
          .setApplicationKind(MApplicationKind.CONSOLE)
          .setJavaInfo(
            MJavaInfo.builder()
              .setMainModule("com.io7m.example/com.io7m.example.Main")
              .setRequiredJDKVersion(21)
              .build()
          )
          .setNames(
            MNames.builder()
              .setPackageName(new MPackageName(new RDottedName("com.io7m.example")))
              .setShortName(new MShortName("example"))
              .build()
          )
          .setVendor(new MVendor(
            new MVendorID(new RDottedName("com.io7m")),
            new MVendorName("io7m")
          ))
          .addLinks(new MLink(MLinkRole.HOME_PAGE, URI.create("https://www.example.com")))
          .setVersion(
            new MVersion(
              Version.of(1, 0, 0),
              LocalDate.parse("2024-10-06")
            )
          )
          .build())
      .setManifest(
        MManifest.builder()
          .build())
      .build();

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

    try (var out = new ZipArchiveOutputStream(outFile)) {
      out.finish();
    }

    var ex =
      assertThrows(MException.class, () -> {
        this.readers.open(outFile);
      });

    assertEquals(
      "error-package-declaration-missing",
      ex.errorCode()
    );
  }

  @Test
  public void testCorruptPackage()
    throws Exception
  {
    final var outFile =
      this.directory.resolve("out.mpk");

    try (var out = new ZipArchiveOutputStream(outFile)) {
      final var entry =
        new ZipArchiveEntry(MReservedNames.montarrePackage().name());
      out.putArchiveEntry(entry);
      out.write("<x>Not a package!".getBytes(StandardCharsets.UTF_8));
      out.closeArchiveEntry();
      out.finish();
    }

    var ex =
      assertThrows(MException.class, () -> {
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

    var ex =
      assertThrows(MException.class, () -> {
        this.readers.open(outFile);
      });

    assertEquals(
      "error-io",
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

    try (var writer =
           this.writers.create(outFile, outFileTmp, EMPTY_PACKAGE)) {

    }

    try (var reader = this.readers.open(outFile)) {
      assertEquals(EMPTY_PACKAGE, reader.packageDeclaration());
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
      EMPTY_PACKAGE.withManifest(
        MManifest.builder()
          .addItems(
            new MResource(
              new MFileName("meta/bom.xml"),
              new MHash(
                new MHashAlgorithm("SHA-256"),
                new MHashValue(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
              MResourceRole.BOM
            ))
          .build()
      );

    Files.createFile(empty);

    try (var writer =
           this.writers.create(outFile, outFileTmp, p)) {
      writer.addFile(new MFileName("meta/bom.xml"), empty);
    }

    try (var reader = this.readers.open(outFile)) {
      assertEquals(p, reader.packageDeclaration());
    }
  }

  @Test
  public void testFileMissing()
    throws Exception
  {
    final var outFile =
      this.directory.resolve("out.mpk");

    final var p =
      EMPTY_PACKAGE.withManifest(
        MManifest.builder()
          .addItems(
            new MResource(
              new MFileName("meta/bom.xml"),
              new MHash(
                new MHashAlgorithm("SHA-256"),
                new MHashValue(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
              MResourceRole.BOM
            ))
          .build()
      );

    try (var zipFile = new ZipArchiveOutputStream(outFile)) {
      zipFile.putArchiveEntry(
        new ZipArchiveEntry(MReservedNames.montarrePackage().name()));
      this.serializers.serialize(URI.create("out"), zipFile, p);
      zipFile.closeArchiveEntry();
    }

    final var ex =
      assertThrows(MException.class, () -> {
        try (var reader = this.readers.open(outFile)) {
          assertEquals(p, reader.packageDeclaration());
        }
      });

    assertEquals("error-file-missing", ex.errorCode());
  }
}
