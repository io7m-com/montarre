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


package com.io7m.montarre.io;

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.io.MPackageReaderFactoryType;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.parsers.MPackageDeclarationParserFactoryType;
import com.io7m.montarre.io.internal.MPackageReader;
import com.io7m.montarre.xml.MPackageDeclarationParsers;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Package readers.
 */

public final class MPackageReaders implements MPackageReaderFactoryType
{
  private final MPackageDeclarationParserFactoryType parsers;

  /**
   * Package readers.
   */

  public MPackageReaders()
  {
    this(new MPackageDeclarationParsers());
  }

  /**
   * Package writers.
   *
   * @param inParsers The package declaration parsers
   */

  public MPackageReaders(
    final MPackageDeclarationParserFactoryType inParsers)
  {
    this.parsers =
      Objects.requireNonNull(inParsers, "parsers");
  }

  @Override
  public MPackageReaderType open(
    final Path file)
    throws MException
  {
    Objects.requireNonNull(file, "file");

    try {
      final var zipFile =
        ZipFile.builder()
          .setPath(file)
          .get();

      final var reader = new MPackageReader(file, zipFile, this.parsers);
      reader.start();
      return reader;
    } catch (final IOException e) {
      throw this.errorIO(file, e);
    }
  }

  private MException errorIO(
    final Path file,
    final IOException e)
  {
    return new MException(
      Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()),
      e,
      "error-io",
      Map.of("File", file.toString()),
      Optional.empty()
    );
  }
}
