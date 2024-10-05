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

import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.seltzer.api.SStructuredErrorType;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A service capable of producing a native package.
 */

public interface MNativePackagerServiceType
{
  /**
   * @return The name of the packager
   */

  RDottedName name();

  /**
   * @return A description of the service
   */

  String describe();

  /**
   * Determine if the service can run on the current platform, and return
   * the reason why it cannot (if it cannot). Some services are unable to
   * run outside specific platforms. For example, producing Windows
   * MSI installers is limited to Windows platforms due to the requirement
   * to run the Windows-only WiX toolkit.
   *
   * @param packageV The package declaration
   * @return The reason that this service is unsupported
   *
   * @throws InterruptedException On interruption
   */

  Optional<SStructuredErrorType<String>> unsupportedReason(
    Optional<MPackageDeclaration> packageV)
    throws InterruptedException;

  /**
   * Execute the packaging service.
   *
   * @param workspace The workspace
   * @param packageV  The package
   *
   * @return The output file
   *
   * @throws MException On errors
   */

  Path execute(
    MNativeWorkspaceType workspace,
    MPackageReaderType packageV)
    throws MException;
}
