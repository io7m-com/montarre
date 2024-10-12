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

/**
 * The role of a resource file.
 */

public enum MResourceRole
{
  /**
   * A bill-of-materials. Recommended to be in CycloneDX format.
   */

  BOM,

  /**
   * A license file.
   */

  LICENSE,

  /**
   * A Windows application icon (".ico").
   */

  ICO_WINDOWS,

  /**
   * A 16x16 application icon.
   */

  ICON_16,

  /**
   * A 24x24 application icon.
   */

  ICON_24,

  /**
   * A 32x32 application icon.
   */

  ICON_32,

  /**
   * A 48x48 application icon.
   */

  ICON_48,

  /**
   * A 64x64 application icon.
   */

  ICON_64,

  /**
   * A 128x128 application icon.
   */

  ICON_128,

  /**
   * A 256x256 application icon.
   */

  ICON_256,

  /**
   * A screenshot.
   */

  SCREENSHOT;

  /**
   * @return The priority of this role as an icon
   */

  public int iconPriority()
  {
    return switch (this) {
      case BOM, LICENSE -> 0;
      case ICO_WINDOWS -> 1;
      case ICON_16 -> 2;
      case ICON_24 -> 3;
      case ICON_32 -> 4;
      case ICON_48 -> 5;
      case ICON_64 -> 6;
      case ICON_128 -> 7;
      case ICON_256 -> 8;
      case SCREENSHOT -> 0;
    };
  }

  /**
   * @return {@code true} if this role implies an icon
   */

  public boolean isIcon()
  {
    return switch (this) {
      case BOM, LICENSE, SCREENSHOT -> false;
      case ICO_WINDOWS,
           ICON_16,
           ICON_24,
           ICON_32,
           ICON_48,
           ICON_64,
           ICON_128,
           ICON_256 -> true;
    };
  }
}
