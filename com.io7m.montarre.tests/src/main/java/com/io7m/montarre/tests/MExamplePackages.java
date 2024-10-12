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
import com.io7m.montarre.api.MDescription;
import com.io7m.montarre.api.MJavaInfo;
import com.io7m.montarre.api.MLanguageCode;
import com.io7m.montarre.api.MLink;
import com.io7m.montarre.api.MLinkRole;
import com.io7m.montarre.api.MManifest;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MNames;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPackageName;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.MTranslatedText;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVendorID;
import com.io7m.montarre.api.MVendorName;
import com.io7m.montarre.api.MVersion;
import com.io7m.verona.core.Version;

import java.net.URI;
import java.time.LocalDate;

public final class MExamplePackages
{
  static final MPackageDeclaration EMPTY_PACKAGE =
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
              .setHumanName("Example")
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
          .build())
      .setManifest(
        MManifest.builder()
          .build())
      .build();

  private MExamplePackages()
  {

  }
}
