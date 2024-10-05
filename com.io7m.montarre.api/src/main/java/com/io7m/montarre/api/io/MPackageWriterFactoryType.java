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


package com.io7m.montarre.api.io;

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MPackageDeclaration;

import java.nio.file.Path;

/**
 * A provider of writers for packages.
 */

public interface MPackageWriterFactoryType
{
  /**
   * Start writing a new package. When the package writer is closed, validation
   * will be performed and, on success, the output file will be atomically
   * renamed from the temporary file to the output file, replacing any
   * existing file.
   *
   * @param file     The output file
   * @param fileTmp  The temporary output file
   * @param packageV The package declaration
   *
   * @return A new package writer
   *
   * @throws MException On errors
   */

  MPackageWriterType create(
    Path file,
    Path fileTmp,
    MPackageDeclaration packageV)
    throws MException;
}
