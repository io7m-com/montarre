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

import java.util.Set;

/**
 * Reserved names that may not appear in manifests.
 */

public final class MReservedNames
{
  private static final MFileName MONTARRE_PACKAGE =
    new MFileName("META-INF/MONTARRE/PACKAGE.XML");

  private static final Set<MFileName> RESERVED =
    Set.of(MONTARRE_PACKAGE);

  /**
   * Reserved names that may not appear in manifests.
   */

  private MReservedNames()
  {

  }

  /**
   * @return The montarre package file name
   */

  public static MFileName montarrePackage()
  {
    return MONTARRE_PACKAGE;
  }

  /**
   * @param name The name
   *
   * @return {@code true} if the given name is reserved
   */

  public static boolean isReserved(
    final MFileName name)
  {
    return RESERVED.contains(name);
  }
}
