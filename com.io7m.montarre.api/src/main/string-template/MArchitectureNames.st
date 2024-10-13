MArchitectureNames(
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

/**
 * Architecture names.
 */

final class MArchitectureNames
{
  private MArchitectureNames()
  {
    
  }

  static final String UNKNOWN =
    "unknown";
  
  /**
   * @return The unknown architecture
   */

  static String unknown()
  {
    return UNKNOWN;
  }

  /**
   * Infer an architecture from the given string.
   *
   * @param name The name
   *
   * @return The architecture or {@link #unknown()}
   */

  static String infer(
    final String name)
  {
    final String upper = name.toUpperCase(Locale.ROOT);
    switch (upper) {
        case "X8632":
        case "X86":
        case "I386":
        case "I486":
        case "I586":
        case "I686":
        case "IA32":
        case "X86_32":
        case "X32": {
        return x86_32();
      }
      case "X8664":
      case "AMD64":
      case "IA32E":
      case "EM64T":
      case "X64":
      case "X86_64": {
        return x86_64();
      }
      case "IA64N": {
        return itanium_32();
      }
      case "IA64":
      case "IA64W":
      case "ITANIUM64": {
        return itanium_64();
      }
      case "SPARC":
      case "SPARC32": {
        return sparc_32();
      }
      case "SPARCV9":
      case "SPARC64": {
        return sparc_64();
      }
      case "ARM":
      case "ARM32": {
        return arm_32();
      }
      case "AARCH64": {
        return aarch_64();
      }
      case "MIPS":
      case "MIPS32": {
        return mips_32();
      }
      case "MIPS64": {
        return mips_64();
      }
      case "MIPSEL":
      case "MIPS32EL": {
        return mipsel_32();
      }
      case "MIPS64EL": {
        return mipsel_64();
      }
      case "PPC":
      case "PPC32": {
        return ppc_32();
      }
      case "PPCLE":
      case "PPC32LE": {
        return ppcle_32();
      }
      case "PPC64": {
        return ppc_64();
      }
      case "PPC64LE": {
        return ppcle_64();
      }
      case "S390": {
        return s390_32();
      }
      case "S390X": {
        return s390_64();
      }
      case "RISCV":
      case "RISCV32": {
        return riscv_32();
      }
      case "RISCV64": {
        return riscv_64();
      }
      case "E2K": {
        return e2k();
      }
      case "LOONGARCH64": {
        return loongarch_64();
      }
      default: {
        return unknown();
      }
    }
  }

  /**
   * @return A standard architecture name
   */
  static String loongarch_64()
  {
    return "loongarch_64";
  }

  /**
   * @return A standard architecture name
   */
  static String e2k()
  {
    return "e2k";
  }

  /**
   * @return A standard architecture name
   */
  static String riscv_64()
  {
    return "riscv_64";
  }

  /**
   * @return A standard architecture name
   */
  static String riscv_32()
  {
    return "riscv_32";
  }

  /**
   * @return A standard architecture name
   */
  static String s390_64()
  {
    return "s390_64";
  }

  /**
   * @return A standard architecture name
   */
  static String s390_32()
  {
    return "s390_32";
  }

  /**
   * @return A standard architecture name
   */
  static String ppcle_64()
  {
    return "ppcle_64";
  }

  /**
   * @return A standard architecture name
   */
  static String ppc_64()
  {
    return "ppc_64";
  }

  /**
   * @return A standard architecture name
   */
  static String ppcle_32()
  {
    return "ppcle_32";
  }

  /**
   * @return A standard architecture name
   */
  static String ppc_32()
  {
    return "ppc_32";
  }

  /**
   * @return A standard architecture name
   */
  static String mipsel_64()
  {
    return "mipsel_64";
  }

  /**
   * @return A standard architecture name
   */
  static String mipsel_32()
  {
    return "mipsel_32";
  }

  /**
   * @return A standard architecture name
   */
  static String mips_64()
  {
    return "mips_64";
  }

  /**
   * @return A standard architecture name
   */
  static String mips_32()
  {
    return "mips_32";
  }

  /**
   * @return A standard architecture name
   */
  static String aarch_64()
  {
    return "aarch64";
  }

  /**
   * @return A standard architecture name
   */
  static String arm_32()
  {
    return "arm_32";
  }

  /**
   * @return A standard architecture name
   */
  static String sparc_64()
  {
    return "sparc_64";
  }

  /**
   * @return A standard architecture name
   */
  static String sparc_32()
  {
    return "sparc_32";
  }

  /**
   * @return A standard architecture name
   */
  static String itanium_64()
  {
    return "itanium_64";
  }

  /**
   * @return A standard architecture name
   */
  static String itanium_32()
  {
    return "itanium_32";
  }

  /**
   * @return A standard architecture name
   */
  static String x86_64()
  {
    return "x86_64";
  }

  /**
   * @return A standard architecture name
   */
  static String x86_32()
  {
    return "x86_32";
  }
}

>>