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

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MReservedNames;
import com.io7m.montarre.io.MPackageWriters;
import com.io7m.montarre.xml.MPackageDeclarationParsers;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * "pack"
 */

public final class MCPackagePack implements QCommandType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MCPackagePack.class);

  private static final QParameterNamed1<Path> INPUT_DIRECTORY =
    new QParameterNamed1<>(
      "--input-directory",
      List.of(),
      new QStringType.QConstant("The input directory."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Path> OUTPUT_FILE =
    new QParameterNamed1<>(
      "--output-file",
      List.of(),
      new QStringType.QConstant("The output file."),
      Optional.empty(),
      Path.class
    );

  private final QCommandMetadata metadata;

  /**
   * Construct a command.
   */

  public MCPackagePack()
  {
    this.metadata = new QCommandMetadata(
      "pack",
      new QStringType.QConstant("Pack a package file."),
      Optional.empty()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(INPUT_DIRECTORY, OUTPUT_FILE),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType newContext)
  {
    QLogback.configure(newContext);

    try {
      final var inputDirectory =
        newContext.parameterValue(INPUT_DIRECTORY);
      final var outputFile =
        newContext.parameterValue(OUTPUT_FILE);
      final var outputFileTmp =
        Paths.get(outputFile + ".tmp");

      final var writers =
        new MPackageWriters();
      final var parsers =
        new MPackageDeclarationParsers();

      final var packageV =
        parsers.parseFile(
          inputDirectory.resolve(MReservedNames.montarrePackage().name())
        );

      try (final var writer =
             writers.create(outputFile, outputFileTmp, packageV)) {
        writer.packFrom(inputDirectory);
      }

      return QCommandStatus.SUCCESS;
    } catch (final MException e) {
      MCSLogging.logStructuredError(LOG, e);
      return QCommandStatus.FAILURE;
    } catch (final Exception e) {
      if (e.getCause() instanceof final MException ee) {
        MCSLogging.logStructuredError(LOG, ee);
        return QCommandStatus.FAILURE;
      }
      LOG.error("", e);
      return QCommandStatus.FAILURE;
    }
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
