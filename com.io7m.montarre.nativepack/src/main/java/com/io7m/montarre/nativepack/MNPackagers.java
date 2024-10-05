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


package com.io7m.montarre.nativepack;

import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.natives.MNativePackagerDirectoryType;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeProcessesType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The native packager directory.
 */

public final class MNPackagers
  implements MNativePackagerDirectoryType
{
  private final SortedMap<RDottedName, MNativePackagerServiceType> packages;

  private MNPackagers(
    final SortedMap<RDottedName, MNativePackagerServiceType> inPackagers)
  {
    this.packages =
      Collections.unmodifiableSortedMap(
        Objects.requireNonNull(inPackagers, "packagers")
      );
  }

  /**
   * Create a directory.
   *
   * @param packagerProviders The providers
   * @param processes         The native processes
   *
   * @return A directory
   */

  public static MNPackagers create(
    final List<MNativePackagerServiceProviderType> packagerProviders,
    final MNativeProcessesType processes)
  {
    final var packagers =
      new TreeMap<RDottedName, MNativePackagerServiceType>();

    for (final var provider : packagerProviders) {
      packagers.put(provider.name(), provider.create(processes));
    }

    return new MNPackagers(packagers);
  }

  /**
   * Create a directory, loading providers from {@link ServiceLoader}.
   *
   * @param processes The native processes
   *
   * @return A directory
   */

  public static MNPackagers createFromServiceLoader(
    final MNativeProcessesType processes)
  {
    return create(
      ServiceLoader.load(MNativePackagerServiceProviderType.class)
        .stream()
        .map(ServiceLoader.Provider::get)
        .toList(),
      processes
    );
  }

  /**
   * Create a directory, loading providers from {@link ServiceLoader}.
   *
   * @return A directory
   */

  public static MNPackagers createFromServiceLoader()
  {
    return createFromServiceLoader(new MNativeProcesses());
  }

  @Override
  public SortedMap<RDottedName, MNativePackagerServiceType> packagers()
  {
    return this.packages;
  }
}
