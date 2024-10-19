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

import com.io7m.montarre.api.MArchitectureName;

import java.util.Objects;

/**
 * Architecture names in the Adoptium API.
 */

public enum METArchitectureName
{
  /**
   * x86_64
   */
  X64("x64"),

  /**
   * x86_32
   */
  X32("x32"),
  /**
   * ppc_64
   */
  PPC64("ppc64"),
  /**
   * ppcle_64
   */
  PPC64LE("ppc64le"),
  /**
   * s390x
   */
  S390X("s390x"),
  /**
   * aarch_64
   */
  AARCH64("aarch64"),
  /**
   * arm_32
   */
  ARM("arm"),
  /**
   * sparc_64
   */
  SPARCV9("sparcv9"),
  /**
   * riscv_64
   */
  RISCV64("riscv64");

  private final String adoptiumName;

  /**
   * @param architecture The input name
   *
   * @return An architecture name
   */

  public static METArchitectureName of(
    final MArchitectureName architecture)
  {
    if (Objects.equals(architecture, MArchitectureName.x86_64())) {
      return X64;
    }
    if (Objects.equals(architecture, MArchitectureName.x86_32())) {
      return X32;
    }
    if (Objects.equals(architecture, MArchitectureName.ppc_64())) {
      return PPC64;
    }
    if (Objects.equals(architecture, MArchitectureName.ppcle_64())) {
      return PPC64LE;
    }
    if (Objects.equals(architecture, MArchitectureName.s390_64())) {
      return S390X;
    }
    if (Objects.equals(architecture, MArchitectureName.aarch_64())) {
      return AARCH64;
    }
    if (Objects.equals(architecture, MArchitectureName.arm_32())) {
      return ARM;
    }
    if (Objects.equals(architecture, MArchitectureName.sparc_64())) {
      return SPARCV9;
    }
    if (Objects.equals(architecture, MArchitectureName.riscv_64())) {
      return RISCV64;
    }

    throw new IllegalArgumentException(
      "Architecture '%s' is not currently supported by Adoptium."
        .formatted(architecture)
    );
  }

  /**
   * @return The Adoptium architecture name
   */

  public String adoptiumName()
  {
    return this.adoptiumName;
  }

  METArchitectureName(
    final String name)
  {
    this.adoptiumName = Objects.requireNonNull(name, "name");
  }
}
