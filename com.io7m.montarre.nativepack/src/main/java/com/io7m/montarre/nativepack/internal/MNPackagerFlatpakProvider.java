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


package com.io7m.montarre.nativepack.internal;

import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeProcessesType;

/**
 * A native packager that produces Flatpak packages.
 */

public final class MNPackagerFlatpakProvider
  implements MNativePackagerServiceProviderType
{
  private static final RDottedName NAME =
    new RDottedName("com.io7m.montarre.flatpak");

  /**
   * A native packager that produces Flatpak packages.
   */

  public MNPackagerFlatpakProvider()
  {

  }

  @Override
  public RDottedName name()
  {
    return NAME;
  }

  @Override
  public String describe()
  {
    return "Produces Flatpak packages.";
  }

  @Override
  public MNativePackagerServiceType create(
    final MNativeProcessesType processes)
  {
    return new MNPackagerFlatpak(this, processes);
  }
}
