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


package com.io7m.montarre.cmdline.internal;

import com.io7m.montarre.schema.MSchemas;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType;
import com.io7m.quarrel.ext.logback.QLogback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * "schema"
 */

public final class MCPackageSchema implements QCommandType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MCPackageSchema.class);

  private static final QParameterNamed1<Integer> VERSION =
    new QParameterNamed1<>(
      "--version",
      List.of(),
      new QStringType.QConstant("The schema version."),
      Optional.of(1),
      Integer.class
    );

  private final QCommandMetadata metadata;

  /**
   * Construct a command.
   */

  public MCPackageSchema()
  {
    this.metadata = new QCommandMetadata(
      "schema",
      new QStringType.QConstant("Show the package schema."),
      Optional.empty()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(VERSION),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType newContext)
    throws IOException
  {
    QLogback.configure(newContext);

    final var version =
      newContext.parameterValue(VERSION);

    if (version == 1) {
      try (var stream = MSchemas.schema1_0().location().openStream()) {
        stream.transferTo(System.out);
        System.out.println();
        return QCommandStatus.SUCCESS;
      }
    }

    LOG.error("Unknown schema version.");
    return QCommandStatus.FAILURE;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
