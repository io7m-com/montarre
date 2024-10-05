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


package com.io7m.montarre.api;


import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.verona.core.Version;
import org.immutables.value.Value;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Package metadata.
 */

@Value.Immutable
@ImmutablesStyleType
public non-sealed interface MMetadataType
  extends MPackageElementType
{
  /**
   * @return The package version
   */

  Version version();

  /**
   * @return The package name
   */

  MPackageName packageName();

  /**
   * @return The minimum required JDK version
   */

  long requiredJDKVersion();

  /**
   * @return The package description
   */

  String description();

  /**
   * @return The package copyright string
   */

  String copyright();

  /**
   * @return The SPDX license identifier
   */

  String license();

  /**
   * @return The package site
   */

  URI siteURI();

  /**
   * @return The package main module
   */

  String mainModule();

  /**
   * @return The package short name
   */

  MShortName shortName();

  /**
   * @return The vendor name
   */

  MVendorName vendorName();

  /**
   * @return The flatpak-specific metadata
   */

  @Value.Default
  default MMetadataFlatpakType flatpak()
  {
    return MMetadataFlatpak.builder()
      .build();
  }

  /**
   * @return A name-based UUID from the package name
   */

  @Value.Derived
  default UUID id()
  {
    return UUID.nameUUIDFromBytes(
      this.packageName()
        .name()
        .value()
        .getBytes(StandardCharsets.UTF_8)
    );
  }
}
