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

import com.io7m.anethum.api.ParsingException;
import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.MApplicationKind;
import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MCaptions;
import com.io7m.montarre.api.MCategoryName;
import com.io7m.montarre.api.MCopying;
import com.io7m.montarre.api.MDescription;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MJavaInfo;
import com.io7m.montarre.api.MLanguageCode;
import com.io7m.montarre.api.MLink;
import com.io7m.montarre.api.MLinkRole;
import com.io7m.montarre.api.MManifest;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MModule;
import com.io7m.montarre.api.MNames;
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPackageName;
import com.io7m.montarre.api.MPlatformDependentModule;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MResourceRole;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.MTranslatedText;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVendorID;
import com.io7m.montarre.api.MVendorName;
import com.io7m.montarre.api.MVersion;
import com.io7m.montarre.xml.MPackageDeclarationParsers;
import com.io7m.montarre.xml.MPackageDeclarationSerializers;
import com.io7m.verona.core.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MPackageParsersTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MPackageParsersTest.class);

  private MPackageDeclarationParsers parsers;
  private MPackageDeclarationSerializers serializers;

  @BeforeEach
  public void setup()
  {
    this.parsers =
      new MPackageDeclarationParsers();
    this.serializers =
      new MPackageDeclarationSerializers();
  }

  @Test
  public void testErrorDuplicateFile()
    throws Exception
  {
    this.error("error-duplicate-file.xml");
  }

  @Test
  public void testErrorDuplicateFileCase()
    throws Exception
  {
    this.error("error-duplicate-file-case.xml");
  }

  private void error(
    final String name)
    throws Exception
  {
    final var file =
      "/com/io7m/montarre/tests/%s".formatted(name);

    try (final var stream =
           MPackageParsersTest.class.getResourceAsStream(file)) {
      assertThrows(ParsingException.class, () -> {
        this.parsers.parse(URI.create("urn:fail"), stream);
      });
    }
  }

  @Test
  public void testSerialize()
    throws Exception
  {
    final var streamOut = new ByteArrayOutputStream();

    final var pack0 =
      MPackageDeclaration.builder()
      .setMetadata(
        MMetadata.builder()
          .setCopying(
            MCopying.builder()
              .setCopyright("Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com")
              .setLicense("ISC")
              .build()
          )
          .setDescription(
            new MDescription(
              MTranslatedText.builder()
                .setLanguage(new MLanguageCode("en"))
                .setText("An example package.")
                .build()
            )
          )
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
          .setApplicationKind(MApplicationKind.CONSOLE)
          .addLinks(new MLink(MLinkRole.HOME_PAGE, URI.create("https://www.example.com")))
          .setVendor(new MVendor(
            new MVendorID(new RDottedName("com.io7m")),
            new MVendorName("io7m")
          ))
          .setVersion(
            new MVersion(
              Version.of(1, 0, 0),
              LocalDate.parse("2024-10-06")
            )
          )
          .addCategories(new MCategoryName("Office"))
          .addCategories(new MCategoryName("Networking"))
          .build())
      .setManifest(
        MManifest.builder()
          .addItems(new MModule(
            new MFileName("lib/file.jar"),
            hashOf("lib/file.jar"))
          )
          .addItems(new MPlatformDependentModule(
            new MFileName("lib/x64/windows/file.jar"),
            hashOf("lib/x64/windows/file.jar"),
            new MOperatingSystemName("windows"),
            new MArchitectureName("x64"))
          )
          .addItems(
            new MResource(
              new MFileName("meta/bom.xml"),
              hashOf("meta/bom.xml"),
              MResourceRole.BOM,
              Optional.of(MCaptions.ofTranslations(
                Map.entry(new MLanguageCode("en"), "A bill of materials."),
                Map.entry(new MLanguageCode("fr"), "Une nomenclature.")
              ))
            )
          )
          .build())
      .build();

    this.serializers.serialize(URI.create("urn:out"), streamOut, pack0);

    LOG.debug("{}", streamOut.toString(StandardCharsets.UTF_8));

    final var pack1 =
      this.parsers.parse(
        URI.create("urn:in"),
        new ByteArrayInputStream(streamOut.toByteArray())
      );

    assertEquals(pack0, pack1);
  }

  private static MHash hashOf(
    final String text)
    throws NoSuchAlgorithmException
  {
    final var digest =
      MessageDigest.getInstance("SHA-256");

    digest.update(text.getBytes(StandardCharsets.UTF_8));

    return new MHash(
      new MHashAlgorithm("SHA-256"),
      new MHashValue(HexFormat.of().formatHex(digest.digest()))
    );
  }
}
