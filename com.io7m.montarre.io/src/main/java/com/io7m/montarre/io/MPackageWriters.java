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
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.io.MPackageWriterFactoryType;
import com.io7m.montarre.api.io.MPackageWriterType;
import com.io7m.montarre.api.parsers.MPackageDeclarationSerializerFactoryType;
import com.io7m.montarre.io.internal.MPackageWriter;
import com.io7m.montarre.xml.MPackageDeclarationSerializers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Package writers.
 */

public final class MPackageWriters implements MPackageWriterFactoryType
{
  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.TRUNCATE_EXISTING,
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
  };

  private final MPackageDeclarationSerializerFactoryType serializers;

  /**
   * Package writers.
   */

  public MPackageWriters()
  {
    this(new MPackageDeclarationSerializers());
  }

  /**
   * Package writers.
   *
   * @param inSerializers A serializer factory
   */

  public MPackageWriters(
    final MPackageDeclarationSerializerFactoryType inSerializers)
  {
    this.serializers =
      Objects.requireNonNull(inSerializers, "serializers");
  }

  @Override
  public MPackageWriterType create(
    final Path file,
    final Path fileTmp,
    final MPackageDeclaration packageV)
    throws MException
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(fileTmp, "fileTmp");
    Objects.requireNonNull(packageV, "packageV");

    try {
      final var stream =
        Files.newOutputStream(fileTmp, OPEN_OPTIONS);
      final var buffered =
        new BufferedOutputStream(stream);

      final var writer =
        new MPackageWriter(
          this.serializers,
          new HashMap<>(),
          stream,
          buffered,
          file,
          fileTmp,
          packageV
        );

      writer.start();
      return writer;
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
