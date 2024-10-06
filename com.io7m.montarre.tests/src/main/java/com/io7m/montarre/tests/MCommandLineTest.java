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
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MManifest;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPackageName;
import com.io7m.montarre.api.MResourceRole;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVendorID;
import com.io7m.montarre.api.MVendorName;
import com.io7m.montarre.api.MVersion;
import com.io7m.montarre.cmdline.MMain;
import com.io7m.montarre.io.MPackageWriters;
import com.io7m.verona.core.Version;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MCommandLineTest
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
          )          .setApplicationKind(MApplicationKind.CONSOLE)
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

  private static final MPackageDeclaration PACKAGE_WITH_EMPTY =
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

  private static final MPackageWriters WRITERS =
    new MPackageWriters();

  @TestFactory
  public Stream<DynamicTest> testHelp()
  {
    return Stream.of(
      List.of("help"),
      List.of("help", "help"),
      List.of("help", "package"),
      List.of("help", "package", "extract-declaration"),
      List.of("help", "package", "check"),
      List.of("help", "native"),
      List.of("help", "native", "packagers"),
      List.of("help", "native", "create"),
      List.of("help", "maven-central", "download")
    ).map(MCommandLineTest::helpOf);
  }

  private static DynamicTest helpOf(
    final List<String> arguments)
  {
    return DynamicTest.dynamicTest("testHelp_" + arguments, () -> {
      final var r = MMain.mainExitless(arguments.toArray(new String[0]));
      assertEquals(0, r);
    });
  }

  @Test
  public void testPackageCheck(
    final @TempDir Path directory)
    throws Exception
  {
    final var fileEmpty = directory.resolve("file.txt");
    Files.createFile(fileEmpty);

    final var file =
      directory.resolve("file.mpk");
    final var fileTmp =
      directory.resolve("file.mpk.tmp");

    try (var writer =
           WRITERS.create(file, fileTmp, PACKAGE_WITH_EMPTY)) {
      writer.addFile(new MFileName("meta/bom.xml"), fileEmpty);
    }

    final var r = MMain.mainExitless(
      new String[]{
        "package",
        "check",
        "--file",
        file.toString()
      }
    );
    assertEquals(0, r);
  }

  @Test
  public void testPackageExtract(
    final @TempDir Path directory)
    throws Exception
  {
    final var fileEmpty = directory.resolve("file.txt");
    Files.createFile(fileEmpty);

    final var file =
      directory.resolve("file.mpk");
    final var fileTmp =
      directory.resolve("file.mpk.tmp");

    try (var writer =
           WRITERS.create(file, fileTmp, PACKAGE_WITH_EMPTY)) {
      writer.addFile(new MFileName("meta/bom.xml"), fileEmpty);
    }

    final var r = MMain.mainExitless(
      new String[]{
        "package",
        "extract-declaration",
        "--file",
        file.toString()
      }
    );
    assertEquals(0, r);
  }

  @Test
  public void testPackageSchema()
    throws Exception
  {
    var r = MMain.mainExitless(
      new String[]{
        "package",
        "schema"
      }
    );
    assertEquals(0, r);

    r = MMain.mainExitless(
      new String[]{
        "package",
        "schema",
        "--version",
        "999"
      }
    );
    assertEquals(1, r);
  }

  @Test
  public void testNativePackagers()
    throws Exception
  {
    final var r = MMain.mainExitless(
      new String[]{
        "native",
        "packagers"
      }
    );
    assertEquals(0, r);
  }

  @Test
  public void testMavenCentralDownload(
    final @TempDir Path directory)
  {
    final var outFile = directory.resolve("output.jar");
    final var r = MMain.mainExitless(
      new String[]{
        "maven-central",
        "download",
        "--group",
        "com.io7m.junreachable",
        "--artifact",
        "com.io7m.junreachable.core",
        "--version",
        "4.0.0",
        "--output-file",
        outFile.toString()
      }
    );
    assertEquals(0, r);
    assertTrue(Files.isRegularFile(outFile));
  }

  @Test
  public void testMavenCentralDownloadFails(
    final @TempDir Path directory)
  {
    final var outFile = directory.resolve("output.jar");
    final var r = MMain.mainExitless(
      new String[]{
        "maven-central",
        "download",
        "--group",
        "com.io7m.junreachable",
        "--artifact",
        "com.io7m.junreachable.core",
        "--version",
        "0.0.0",
        "--output-file",
        outFile.toString()
      }
    );
    assertEquals(1, r);
    assertFalse(Files.isRegularFile(outFile));
  }
}
