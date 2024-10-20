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


package com.io7m.montarre.adoptium;

import com.io7m.montarre.api.MOperatingSystemName;

import java.util.Objects;

/**
 * Operating system names in the Adoptium API.
 */

public enum METOperatingSystemName
{
  /**
   * linux
   */
  LINUX("linux"),

  /**
   * windows
   */
  WINDOWS("windows"),

  /**
   * mac
   */
  MAC("mac"),

  /**
   * solaris
   */
  SOLARIS("solaris"),

  /**
   * aix
   */
  AIX("aix");

  private final String adoptiumName;

  /**
   * @param os The input name
   *
   * @return An OS name
   */

  public static METOperatingSystemName of(
    final MOperatingSystemName os)
  {
    if (Objects.equals(os, MOperatingSystemName.linux())) {
      return LINUX;
    }
    if (Objects.equals(os, MOperatingSystemName.windows())) {
      return WINDOWS;
    }
    if (Objects.equals(os, MOperatingSystemName.osx())) {
      return MAC;
    }
    if (Objects.equals(os, MOperatingSystemName.solaris())) {
      return SOLARIS;
    }
    if (Objects.equals(os, MOperatingSystemName.aix())) {
      return AIX;
    }

    throw new IllegalArgumentException(
      "Operating system '%s' is not currently supported by Adoptium."
        .formatted(os)
    );
  }

  /**
   * @return The Adoptium OS name
   */

  public String adoptiumName()
  {
    return this.adoptiumName;
  }

  METOperatingSystemName(
    final String name)
  {
    this.adoptiumName = Objects.requireNonNull(name, "name");
  }
}
