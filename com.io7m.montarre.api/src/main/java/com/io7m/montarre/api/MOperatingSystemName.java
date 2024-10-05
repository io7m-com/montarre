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

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An operating system name.
 *
 * @param name The name
 */

public record MOperatingSystemName(
  String name)
  implements Comparable<MOperatingSystemName>
{
  private static final Pattern VALID =
    Pattern.compile("[a-z][a-z0-9_-]{0,32}");

  private static final MOperatingSystemName UNKNOWN =
    new MOperatingSystemName("unknown");

  /**
   * @return The pattern that defines validity
   */

  public static Pattern valid()
  {
    return VALID;
  }

  /**
   * An operating system name.
   *
   * @param name The name
   */

  public MOperatingSystemName
  {
    Objects.requireNonNull(name, "name");

    if (!VALID.matcher(name).matches()) {
      throw new IllegalArgumentException(
        "Operating system names must match the pattern '%s'".formatted(VALID)
      );
    }
  }

  /**
   * @return The unknown operating system
   */

  public static MOperatingSystemName unknown()
  {
    return UNKNOWN;
  }

  /**
   * Infer an operating system from the given string.
   *
   * @param name The name
   *
   * @return The operating system or {@link #unknown()}
   */

  public static MOperatingSystemName infer(
    final String name)
  {
    final var upper = name.toUpperCase(Locale.ROOT);
    if (upper.startsWith("AIX")) {
      return aix();
    }
    if (upper.startsWith("HPUX")) {
      return hpux();
    }
    if (upper.startsWith("OS400")) {
      return os400();
    }
    if (upper.startsWith("LINUX")) {
      return linux();
    }
    if (upper.startsWith("MAC") || upper.startsWith("OSX")) {
      return osx();
    }
    if (upper.startsWith("FREEBSD")) {
      return freebsd();
    }
    if (upper.startsWith("OPENBSD")) {
      return openbsd();
    }
    if (upper.startsWith("NETBSD")) {
      return netbsd();
    }
    if (upper.startsWith("SOLARIS") || upper.startsWith("SUNOS")) {
      return solaris();
    }
    if (upper.startsWith("WINDOWS")) {
      return windows();
    }
    if (upper.startsWith("ZOS")) {
      return zos();
    }
    return UNKNOWN;
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName zos()
  {
    return new MOperatingSystemName("zos");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName windows()
  {
    return new MOperatingSystemName("windows");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName solaris()
  {
    return new MOperatingSystemName("solaris");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName netbsd()
  {
    return new MOperatingSystemName("netbsd");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName openbsd()
  {
    return new MOperatingSystemName("openbsd");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName freebsd()
  {
    return new MOperatingSystemName("freebsd");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName osx()
  {
    return new MOperatingSystemName("osx");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName linux()
  {
    return new MOperatingSystemName("linux");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName os400()
  {
    return new MOperatingSystemName("os400");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName hpux()
  {
    return new MOperatingSystemName("hpux");
  }

  /**
   * @return A standard operating system name
   */
  public static MOperatingSystemName aix()
  {
    return new MOperatingSystemName("aix");
  }

  @Override
  public String toString()
  {
    return this.name;
  }

  @Override
  public int compareTo(
    final MOperatingSystemName other)
  {
    return this.name.compareTo(other.name);
  }
}
