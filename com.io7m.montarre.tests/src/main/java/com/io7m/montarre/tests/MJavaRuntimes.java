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


package com.io7m.montarre.tests;

import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MArchiveFormat;
import com.io7m.montarre.api.MOperatingSystemName;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class MJavaRuntimes
{
  private static final MJavaRuntime WINDOWS_X86_64 =
    new MJavaRuntime(
      MArchiveFormat.ZIP,
      URI.create(
        "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_x64_windows_hotspot_21.0.4_7.zip"),
      "b58f6117d26a138da4cb962b974efc4be4b88b65093366146965d16ad3c45e75"
    );

  private static final MJavaRuntime LINUX_X86_64 =
    new MJavaRuntime(
      MArchiveFormat.TAR_GZ,
      URI.create(
        "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz"),
      "d3affbb011ca6c722948f6345d15eba09bded33f9947d4d67e09723e2518c12a"
    );

  private static final List<MJavaRuntime> RUNTIMES =
    List.of(
      WINDOWS_X86_64,
      LINUX_X86_64
    );

  private MJavaRuntimes()
  {

  }

  static Optional<MJavaRuntime> getRuntimeDefinition()
  {
    final var osName =
      MOperatingSystemName.infer(System.getProperty("os.name"));
    final var arch =
      MArchitectureName.infer(System.getProperty("os.arch"));

    if (Objects.equals(osName, MOperatingSystemName.linux())) {
      if (Objects.equals(arch, MArchitectureName.x86_64())) {
        return Optional.of(LINUX_X86_64);
      }
    }

    if (Objects.equals(osName, MOperatingSystemName.windows())) {
      if (Objects.equals(arch, MArchitectureName.x86_64())) {
        return Optional.of(WINDOWS_X86_64);
      }
    }

    return Optional.empty();
  }

  record MJavaRuntime(
    MArchiveFormat format,
    URI downloadURI,
    String sha256)
  {

  }
}
