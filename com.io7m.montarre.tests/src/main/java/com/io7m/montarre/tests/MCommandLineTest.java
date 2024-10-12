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

import com.io7m.montarre.api.MCaptions;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MLanguageCode;
import com.io7m.montarre.api.MManifest;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MResourceRole;
import com.io7m.montarre.cmdline.MMain;
import com.io7m.montarre.io.MPackageWriters;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MCommandLineTest
{
  private static final MPackageDeclaration PACKAGE_WITH_EMPTY =
    MExamplePackages.EMPTY_PACKAGE.withManifest(
      MManifest.builder()
        .addItems(
          new MResource(
            new MFileName("meta/bom.xml"),
            new MHash(
              new MHashAlgorithm("SHA-256"),
              new MHashValue(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
            MResourceRole.BOM,
            Optional.of(MCaptions.ofTranslations(
              Map.entry(new MLanguageCode("en"), "A bill of materials."),
              Map.entry(new MLanguageCode("fr"), "Une nomenclature.")
            ))
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
      List.of("help", "package", "unpack"),
      List.of("help", "package", "pack"),
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

    try (final var writer =
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

    try (final var writer =
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
  public void testPackageUnpack(
    final @TempDir Path directory,
    final @TempDir Path output)
    throws Exception
  {
    final var fileEmpty = directory.resolve("file.txt");
    Files.createFile(fileEmpty);

    final var file =
      directory.resolve("file.mpk");
    final var fileTmp =
      directory.resolve("file.mpk.tmp");

    try (final var writer =
           WRITERS.create(file, fileTmp, PACKAGE_WITH_EMPTY)) {
      writer.addFile(new MFileName("meta/bom.xml"), fileEmpty);
    }

    final var r = MMain.mainExitless(
      new String[]{
        "package",
        "unpack",
        "--file",
        file.toString(),
        "--output-directory",
        output.toString()
      }
    );
    assertEquals(0, r);
  }

  @Test
  public void testPackageUnpackPack(
    final @TempDir Path directory,
    final @TempDir Path unpackTo)
    throws Exception
  {
    final var inputFile =
      directory.resolve("com.io7m.montarre.distribution-0.0.1-SNAPSHOT.mpk");
    final var outputFile =
      directory.resolve("packed.mpk");

    this.resource("com.io7m.montarre.distribution-0.0.1-SNAPSHOT.mpk", inputFile);

    final var r0 = MMain.mainExitless(
      new String[]{
        "package",
        "unpack",
        "--file",
        inputFile.toString(),
        "--output-directory",
        unpackTo.toString()
      }
    );
    assertEquals(0, r0);

    final var r1 = MMain.mainExitless(
      new String[]{
        "package",
        "pack",
        "--input-directory",
        unpackTo.toString(),
        "--output-file",
        outputFile.toString()
      }
    );
    assertEquals(0, r1);

    assertEquals(
      -1L,
      Files.mismatch(inputFile, outputFile)
    );
  }

  @Test
  public void testPackageValidate(
    final @TempDir Path directory)
    throws Exception
  {
    final var inputFile =
      directory.resolve("com.io7m.montarre.distribution-0.0.1-SNAPSHOT.mpk");

    this.resource("com.io7m.montarre.distribution-0.0.1-SNAPSHOT.mpk", inputFile);

    final var r0 = MMain.mainExitless(
      new String[]{
        "package",
        "validate",
        "--file",
        inputFile.toString(),
        "--warnings-as-errors",
        "true"
      }
    );
    assertEquals(0, r0);

    final var r1 = MMain.mainExitless(
      new String[]{
        "package",
        "validate",
        "--file",
        inputFile.toString(),
        "--warnings-as-errors",
        "false"
      }
    );
    assertEquals(0, r1);
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

  private void resource(
    final String resourceName,
    final Path output)
    throws IOException
  {
    final var file =
      "/com/io7m/montarre/tests/" + resourceName;

    try (final var stream = MCommandLineTest.class.getResourceAsStream(file)) {
      Files.write(output, stream.readAllBytes());
    }
  }
}
