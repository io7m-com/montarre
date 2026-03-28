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


package com.io7m.montarre.api.natives;

import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.api.MPlatformDependentModule;
import com.io7m.streamtime.core.STTransferStatistics;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * A workspace for producing a native package.
 */

public interface MNativeWorkspaceType
  extends AutoCloseable
{
  @Override
  void close()
    throws MException;


  /**
   * @return The operating system name
   */

  MOperatingSystemName operatingSystem();

  /**
   * @return The architecture name
   */

  MArchitectureName architecture();

  /**
   * @return The transfer statistic for java runtime downloads
   */

  Flow.Publisher<STTransferStatistics> javaRuntimeDownload();

  /**
   * @return The java runtime
   */

  CompletableFuture<Path> javaRuntime();

  /**
   * @return A fresh working directory
   *
   * @throws MException On errors
   */

  Path createWorkDirectory()
    throws MException;

  /**
   * Check if the operating system and architecture of the given module matches
   * this workspace.
   *
   * @param module The module
   *
   * @return {@code true} if the module matches
   */

  default boolean matchesModule(
    final MPlatformDependentModule module)
  {
    final var osMatches =
      Objects.equals(module.operatingSystem(), this.operatingSystem());
    final var archMatches =
      Objects.equals(module.architecture(), this.architecture());

    return (osMatches && archMatches);
  }
}
