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
 * A hardware architecture name.
 *
 * @param name The name
 */

public record MArchitectureName(
  String name)
  implements Comparable<MArchitectureName>
{
  private static final Pattern VALID =
    Pattern.compile("[a-z][a-z0-9_-]{0,32}");

  /**
   * @return The pattern that defines validity
   */

  public static Pattern valid()
  {
    return VALID;
  }

  private static final MArchitectureName UNKNOWN =
    new MArchitectureName("unknown");

  /**
   * A hardware architecture name.
   *
   * @param name The name
   */

  public MArchitectureName
  {
    Objects.requireNonNull(name, "name");

    if (!VALID.matcher(name).matches()) {
      throw new IllegalArgumentException(
        "Architecture names must match the pattern '%s'".formatted(VALID)
      );
    }
  }

  /**
   * @return The unknown architecture
   */

  public static MArchitectureName unknown()
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

  public static MArchitectureName infer(
    final String name)
  {
    final var upper = name.toUpperCase(Locale.ROOT);
    return switch (upper) {
      case "X8632",
           "X86",
           "I386",
           "I486",
           "I586",
           "I686",
           "IA32",
           "X86_32",
           "X32" -> {
        yield x86_32();
      }
      case "X8664",
           "AMD64",
           "IA32E",
           "EM64T",
           "X64",
           "X86_64" -> {
        yield x86_64();
      }
      case "IA64N" -> {
        yield itanium_32();
      }
      case "IA64", "IA64W", "ITANIUM64" -> {
        yield itanium_64();
      }
      case "SPARC", "SPARC32" -> {
        yield sparc_32();
      }
      case "SPARCV9", "SPARC64" -> {
        yield sparc_64();
      }
      case "ARM", "ARM32" -> {
        yield arm_32();
      }
      case "AARCH64" -> {
        yield aarch_64();
      }
      case "MIPS", "MIPS32" -> {
        yield mips_32();
      }
      case "MIPS64" -> {
        yield mips_64();
      }
      case "MIPSEL", "MIPS32EL" -> {
        yield mipsel_32();
      }
      case "MIPS64EL" -> {
        yield mipsel_64();
      }
      case "PPC", "PPC32" -> {
        yield ppc_32();
      }
      case "PPCLE", "PPC32LE" -> {
        yield ppcle_32();
      }
      case "PPC64" -> {
        yield ppc_64();
      }
      case "PPC64LE" -> {
        yield ppcle_64();
      }
      case "S390" -> {
        yield s390_32();
      }
      case "S390X" -> {
        yield s390_64();
      }
      case "RISCV", "RISCV32" -> {
        yield riscv_32();
      }
      case "RISCV64" -> {
        yield riscv_64();
      }
      case "E2K" -> {
        yield e2k();
      }
      case "LOONGARCH64" -> {
        yield loongarch_64();
      }
      default -> unknown();
    };
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName loongarch_64()
  {
    return new MArchitectureName("loongarch_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName e2k()
  {
    return new MArchitectureName("e2k");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName riscv_64()
  {
    return new MArchitectureName("riscv_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName riscv_32()
  {
    return new MArchitectureName("riscv_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName s390_64()
  {
    return new MArchitectureName("s390_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName s390_32()
  {
    return new MArchitectureName("s390_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppcle_64()
  {
    return new MArchitectureName("ppcle_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppc_64()
  {
    return new MArchitectureName("ppc_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppcle_32()
  {
    return new MArchitectureName("ppcle_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppc_32()
  {
    return new MArchitectureName("ppc_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mipsel_64()
  {
    return new MArchitectureName("mipsel_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mipsel_32()
  {
    return new MArchitectureName("mipsel_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mips_64()
  {
    return new MArchitectureName("mips_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mips_32()
  {
    return new MArchitectureName("mips_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName aarch_64()
  {
    return new MArchitectureName("aarch64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName arm_32()
  {
    return new MArchitectureName("arm_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName sparc_64()
  {
    return new MArchitectureName("sparc_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName sparc_32()
  {
    return new MArchitectureName("sparc_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName itanium_64()
  {
    return new MArchitectureName("itanium_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName itanium_32()
  {
    return new MArchitectureName("itanium_32");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName x86_64()
  {
    return new MArchitectureName("x86_64");
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName x86_32()
  {
    return new MArchitectureName("x86_32");
  }

  @Override
  public String toString()
  {
    return this.name;
  }

  @Override
  public int compareTo(
    final MArchitectureName o)
  {
    return this.name.compareTo(o.name);
  }
}
