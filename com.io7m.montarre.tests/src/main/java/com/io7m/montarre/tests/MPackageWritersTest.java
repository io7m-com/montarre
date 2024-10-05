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
import com.io7m.montarre.api.MVendorName;
import com.io7m.montarre.io.MPackageWriters;
import com.io7m.verona.core.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MPackageWritersTest
{
  private static final MPackageDeclaration EMPTY_PACKAGE =
    MPackageDeclaration.builder()
      .setMetadata(
        MMetadata.builder()
          .setCopyright(
            "Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com")
          .setDescription("An example package.")
          .setLicense("ISC")
          .setMainModule("com.io7m.example/com.io7m.example.Main")
          .setPackageName(new MPackageName(new RDottedName("com.io7m.example")))
          .setRequiredJDKVersion(21L)
          .setShortName(new MShortName("example"))
          .setSiteURI(URI.create("https://www.example.com"))
          .setVendorName(new MVendorName("io7m"))
          .setVersion(Version.of(1, 0, 0))
          .build())
      .setManifest(
        MManifest.builder()
          .build())
      .build();

  private MPackageWriters writers;
  private Path directory;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
  {
    this.writers =
      new MPackageWriters();
    this.directory =
      Objects.requireNonNull(directory, "directory");
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

    assertTrue(Files.isRegularFile(outFile));
    assertFalse(Files.exists(outFileTmp));
  }

  @Test
  public void testForgotFile()
    throws Exception
  {
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

    final var ex = assertThrows(MException.class, () -> {
      try (var writer =
             this.writers.create(outFile, outFileTmp, p)) {

      }
    });

    assertEquals("error-file-missed", ex.errorCode());
  }

  @Test
  public void testFileHashIncorrect()
    throws Exception
  {
    final var data =
      this.directory.resolve("data.txt");
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

    Files.writeString(data, "X!");

    final var ex = assertThrows(MException.class, () -> {
      try (var writer =
             this.writers.create(outFile, outFileTmp, p)) {
        writer.addFile(new MFileName("meta/bom.xml"), data);
      }
    });

    assertEquals("error-hash-mismatch", ex.errorCode());
  }

  @Test
  public void testWriteTwice()
    throws Exception
  {
    final var data =
      this.directory.resolve("data.txt");
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

    Files.createFile(data);

    try (var writer =
           this.writers.create(outFile, outFileTmp, p)) {
      writer.addFile(new MFileName("meta/bom.xml"), data);

      final var ex = assertThrows(MException.class, () -> {
        writer.addFile(new MFileName("meta/bom.xml"), data);
      });

      assertEquals("error-file-already-written", ex.errorCode());
    }
  }

  @Test
  public void testWriteNotDeclared()
    throws Exception
  {
    final var data =
      this.directory.resolve("data.txt");
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

    Files.createFile(data);

    try (var writer =
           this.writers.create(outFile, outFileTmp, p)) {
      writer.addFile(new MFileName("meta/bom.xml"), data);

      final var ex = assertThrows(MException.class, () -> {
        writer.addFile(new MFileName("meta/bom2.xml"), data);
      });
      assertEquals("error-file-undeclared", ex.errorCode());
    }
  }

  @Test
  public void testWriteNotSupportedHash()
    throws Exception
  {
    final var data =
      this.directory.resolve("data.txt");
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
                new MHashAlgorithm("SMASH-1"),
                new MHashValue(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
              MResourceRole.BOM
            ))
          .build()
      );

    Files.createFile(data);

    try (var writer =
           this.writers.create(outFile, outFileTmp, p)) {
      final var ex = assertThrows(MException.class, () -> {
        writer.addFile(new MFileName("meta/bom.xml"), data);
      });
      assertEquals("error-hash-support", ex.errorCode());
    } catch (final MException e) {
      // Ignored
    }
  }

  @Test
  public void testWriteReserved()
    throws Exception
  {
    final var data =
      this.directory.resolve("data.txt");
    final var outFile =
      this.directory.resolve("out.mpk");
    final var outFileTmp =
      this.directory.resolve("out.mpk.tmp");

    final var p =
      EMPTY_PACKAGE.withManifest(
        MManifest.builder()
          .addItems(
            new MResource(
              MReservedNames.montarrePackage(),
              new MHash(
                new MHashAlgorithm("SHA-256"),
                new MHashValue(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
              MResourceRole.BOM
            ))
          .build()
      );

    Files.createFile(data);

    try (var writer =
           this.writers.create(outFile, outFileTmp, p)) {
      final var ex = assertThrows(MException.class, () -> {
        writer.addFile(MReservedNames.montarrePackage(), data);
      });
      assertEquals("error-file-name-reserved", ex.errorCode());
    } catch (final MException e) {
      // Ignored
    }
  }
}
