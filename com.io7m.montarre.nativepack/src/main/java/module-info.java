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

import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.nativepack.internal.app_image.MNPackagerAppImageProvider;
import com.io7m.montarre.nativepack.internal.deb.MNPackagerDebProvider;
import com.io7m.montarre.nativepack.internal.flatpak.MNPackagerFlatpakProvider;
import com.io7m.montarre.nativepack.internal.msi.MNPackagerMSIProvider;

/**
 * Application packaging tools (Native packaging).
 */

module com.io7m.montarre.nativepack
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.montarre.api;

  requires com.io7m.jaffirm.core;
  requires com.io7m.jdownload.core;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.lanark.core;
  requires com.io7m.seltzer.api;
  requires com.io7m.streamtime.core;
  requires com.io7m.verona.core;
  requires java.net.http;
  requires java.xml;
  requires org.apache.commons.compress;
  requires org.slf4j;
  requires org.apache.commons.io;

  provides MNativePackagerServiceProviderType
    with MNPackagerAppImageProvider,
      MNPackagerDebProvider,
      MNPackagerMSIProvider,
      MNPackagerFlatpakProvider;

  uses MNativePackagerServiceProviderType;

  exports com.io7m.montarre.nativepack;

  exports com.io7m.montarre.nativepack.internal
    to com.io7m.montarre.tests;
  exports com.io7m.montarre.nativepack.internal.flatpak
    to com.io7m.montarre.tests;
  exports com.io7m.montarre.nativepack.internal.app_image
    to com.io7m.montarre.tests;
  exports com.io7m.montarre.nativepack.internal.deb
    to com.io7m.montarre.tests;
  exports com.io7m.montarre.nativepack.internal.msi
    to com.io7m.montarre.tests;
}