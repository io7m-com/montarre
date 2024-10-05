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
 * A flatpak runtime.
 *
 * @param name    The runtime name (such as "org.freedesktop.Platform").
 * @param version The version (such as "24.08")
 * @param role    The runtime role
 */

public record MFlatpakRuntime(
  String name,
  String version,
  MFlatpakRuntimeRole role)
{
  /**
   * A flatpak runtime.
   *
   * @param name    The runtime name (such as "org.freedesktop.Platform").
   * @param version The version (such as "24.08")
   * @param role    The runtime role
   */

  public MFlatpakRuntime
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(role, "role");
  }

  /**
   * Obtain the full name, including the architecture.
   *
   * @param architecture The architecture
   *
   * @return The full name
   */

  public String fullName(
    final MArchitectureName architecture)
  {
    return "%s/%s/%s".formatted(
      this.name,
      architecture.name(),
      this.version
    );
  }
}
