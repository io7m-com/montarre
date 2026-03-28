/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.entomos.core.EoFileDescription;
import com.io7m.entomos.core.EoFileSectionDescription;
import com.io7m.entomos.core.EoFileVersionsDescription;
import com.io7m.entomos.core.EoSectionCardinality;
import com.io7m.entomos.core.EoSectionOrdering;
import com.io7m.entomos.core.EoSectionsUnknown;

/**
 * File format definitions.
 */

public final class MFileFormats
{
  private static final long FILE_IDENTIFIER =
    0x894D_5450_0D0A_1A0AL;

  private static final long SECTION_END_IDENTIFIER =
    0x4D5450_5F_454E4421L;
  private static final long SECTION_MANIFEST_IDENTIFIER =
    0x4D5450_5F_4D414E49L;
  private static final long SECTION_FILE_IDENTIFIER =
    0x4D5450_5F_46494C45L;

  private static final EoFileDescription FORMAT_1_0 =
    createDescription1p0();
  private static final EoFileVersionsDescription FORMATS =
    createDescriptions();

  private MFileFormats()
  {

  }

  /**
   * @return The file identifier
   */

  public static long fileIdentifier()
  {
    return FILE_IDENTIFIER;
  }

  /**
   * @return The end section identifier
   */

  public static long sectionEndIdentifier()
  {
    return SECTION_END_IDENTIFIER;
  }

  /**
   * @return The file section identifier
   */

  public static long sectionFileIdentifier()
  {
    return SECTION_FILE_IDENTIFIER;
  }

  private static EoFileVersionsDescription createDescriptions()
  {
    return EoFileVersionsDescription.builder()
      .addDescriptions(FORMAT_1_0)
      .build();
  }

  private static EoFileDescription createDescription1p0()
  {
    final var sectionIdentifier =
      EoFileSectionDescription.builder()
        .setCardinality(EoSectionCardinality.ONE)
        .setOrdering(EoSectionOrdering.MUST_BE_FIRST)
        .setTag(SECTION_MANIFEST_IDENTIFIER)
        .build();

    final var sectionFile =
      EoFileSectionDescription.builder()
        .setCardinality(EoSectionCardinality.ZERO_TO_N)
        .setOrdering(EoSectionOrdering.ANY_ORDER)
        .setTag(SECTION_FILE_IDENTIFIER)
        .build();

    return EoFileDescription.builder()
      .setVersionMajor(1)
      .setVersionMinor(0)
      .setFileTag(FILE_IDENTIFIER)
      .addSections(
        sectionIdentifier,
        sectionFile
      )
      .setSectionsUnknown(EoSectionsUnknown.UNKNOWN_SECTIONS_PERMITTED)
      .setEndTag(SECTION_END_IDENTIFIER)
      .build();
  }

  /**
   * @return The file formats
   */

  public static EoFileVersionsDescription fileFormats()
  {
    return FORMATS;
  }

  /**
   * @return The package manifest section identifier
   */

  public static long sectionManifestIdentifier()
  {
    return SECTION_MANIFEST_IDENTIFIER;
  }
}
