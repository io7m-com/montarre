MOperatingSystemNames(
  packageName) ::= <<

/*
 * Copyright © 2024 Mark Raynsford \<code@io7m.com\> https://www.io7m.com
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

package <packageName>;

import java.util.Locale;

final class MOperatingSystemNames
{
  private MOperatingSystemNames()
  {
    
  }

  static final String UNKNOWN =
    "unknown";

  /**
   * @return The unknown operating system
   */

  static String unknown()
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

  static String infer(
    final String name)
  {
    final String upper = name.toUpperCase(Locale.ROOT);
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
  static String zos()
  {
    return "zos";
  }

  /**
   * @return A standard operating system name
   */
  static String windows()
  {
    return "windows";
  }

  /**
   * @return A standard operating system name
   */
  static String solaris()
  {
    return "solaris";
  }

  /**
   * @return A standard operating system name
   */
  static String netbsd()
  {
    return "netbsd";
  }

  /**
   * @return A standard operating system name
   */
  static String openbsd()
  {
    return "openbsd";
  }

  /**
   * @return A standard operating system name
   */
  static String freebsd()
  {
    return "freebsd";
  }

  /**
   * @return A standard operating system name
   */
  static String osx()
  {
    return "osx";
  }

  /**
   * @return A standard operating system name
   */
  static String linux()
  {
    return "linux";
  }

  /**
   * @return A standard operating system name
   */
  static String os400()
  {
    return "os400";
  }

  /**
   * @return A standard operating system name
   */
  static String hpux()
  {
    return "hpux";
  }

  /**
   * @return A standard operating system name
   */
  static String aix()
  {
    return "aix";
  }
}
>>