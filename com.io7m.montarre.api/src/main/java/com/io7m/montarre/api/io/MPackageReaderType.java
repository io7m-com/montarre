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
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPlatformDependentModule;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * A package being read.
 */

public interface MPackageReaderType
  extends AutoCloseable
{
  /**
   * The policy for platform-dependent modules.
   */

  enum PlatformDependentModulePolicy
  {

    /**
     * Ignore the module; do not unpack it at all.
     */

    IGNORE,

    /**
     * Include the module and merge it into the same directory as the
     * platform-independent modules.
     */

    MERGE,

    /**
     * Include the module and leave it in a platform-dependent module
     * directory.
     */

    INCLUDE
  }

  /**
   * @return The package declaration
   */

  MPackageDeclaration packageDeclaration();

  @Override
  void close()
    throws MException;

  /**
   * @param file The file
   *
   * @return A stream for the given file
   *
   * @throws MException On errors
   */

  InputStream readFile(
    MFileName file)
    throws MException;

  /**
   * Check the digest of the given file, raising an exception if it does not
   * match what is in the package declaration.
   *
   * @param file The file
   *
   * @throws MException On errors
   */

  void checkHash(
    MFileName file)
    throws MException;

  /**
   * Unpack all files into the given output directory.
   *
   * @param output The output directory
   *
   * @throws MException On errors
   */

  default void unpackInto(
    final Path output)
    throws MException
  {
    this.unpackInto(
      output,
      m -> PlatformDependentModulePolicy.INCLUDE
    );
  }

  /**
   * Unpack all files into the given output directory.
   *
   * @param filterPlatform The filter for platform-dependent modules
   * @param output         The output directory
   *
   * @throws MException On errors
   */

  void unpackInto(
    Path output,
    Function<MPlatformDependentModule, PlatformDependentModulePolicy> filterPlatform)
    throws MException;
}
