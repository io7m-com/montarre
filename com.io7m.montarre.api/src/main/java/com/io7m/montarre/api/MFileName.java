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

import java.util.Objects;

/**
 * A file name. File names are not case-sensitive.
 *
 * @param name The name
 */

public record MFileName(
  String name)
  implements Comparable<MFileName>
{
  /**
   * A file name. File names are not case-sensitive.
   *
   * @param name The name
   */

  public MFileName
  {
    Objects.requireNonNull(name, "name");
  }

  @Override
  public boolean equals(
    final Object other)
  {
    if (this == other) {
      return true;
    }
    if (other instanceof MFileName otherF) {
      return this.name.equalsIgnoreCase(otherF.name);
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(this.name.toUpperCase());
  }

  @Override
  public String toString()
  {
    return this.name;
  }

  @Override
  public int compareTo(
    final MFileName other)
  {
    return this.name.compareToIgnoreCase(other.name);
  }
}
