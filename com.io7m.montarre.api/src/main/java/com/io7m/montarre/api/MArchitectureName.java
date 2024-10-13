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
    new MArchitectureName(MArchitectureNames.UNKNOWN);

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
    return new MArchitectureName(MArchitectureNames.infer(name));
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName loongarch_64()
  {
    return new MArchitectureName(MArchitectureNames.loongarch_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName e2k()
  {
    return new MArchitectureName(MArchitectureNames.e2k());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName riscv_64()
  {
    return new MArchitectureName(MArchitectureNames.riscv_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName riscv_32()
  {
    return new MArchitectureName(MArchitectureNames.riscv_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName s390_64()
  {
    return new MArchitectureName(MArchitectureNames.s390_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName s390_32()
  {
    return new MArchitectureName(MArchitectureNames.s390_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppcle_64()
  {
    return new MArchitectureName(MArchitectureNames.ppcle_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppc_64()
  {
    return new MArchitectureName(MArchitectureNames.ppc_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppcle_32()
  {
    return new MArchitectureName(MArchitectureNames.ppcle_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName ppc_32()
  {
    return new MArchitectureName(MArchitectureNames.ppc_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mipsel_64()
  {
    return new MArchitectureName(MArchitectureNames.mipsel_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mipsel_32()
  {
    return new MArchitectureName(MArchitectureNames.mipsel_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mips_64()
  {
    return new MArchitectureName(MArchitectureNames.mips_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName mips_32()
  {
    return new MArchitectureName(MArchitectureNames.mips_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName aarch_64()
  {
    return new MArchitectureName(MArchitectureNames.aarch_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName arm_32()
  {
    return new MArchitectureName(MArchitectureNames.arm_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName sparc_64()
  {
    return new MArchitectureName(MArchitectureNames.sparc_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName sparc_32()
  {
    return new MArchitectureName(MArchitectureNames.sparc_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName itanium_64()
  {
    return new MArchitectureName(MArchitectureNames.itanium_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName itanium_32()
  {
    return new MArchitectureName(MArchitectureNames.itanium_32());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName x86_64()
  {
    return new MArchitectureName(MArchitectureNames.x86_64());
  }

  /**
   * @return A standard architecture name
   */
  public static MArchitectureName x86_32()
  {
    return new MArchitectureName(MArchitectureNames.x86_32());
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
