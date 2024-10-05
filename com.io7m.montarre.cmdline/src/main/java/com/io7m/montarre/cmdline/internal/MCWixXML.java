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
import com.io7m.montarre.io.MPackageReaders;
import com.io7m.montarre.nativepack.MWiXWriters;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * "xml"
 */

public final class MCWixXML implements QCommandType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MCNativeCreate.class);

  private final QCommandMetadata metadata;

  private static final QParameterNamed1<Path> INPUT_PACKAGE =
    new QParameterNamed1<>(
      "--package",
      List.of(),
      new QStringType.QConstant("The input package."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Path> INPUT_DIRECTORY =
    new QParameterNamed1<>(
      "--input-directory",
      List.of(),
      new QStringType.QConstant("The input directory."),
      Optional.empty(),
      Path.class
    );

  /**
   * Construct a command.
   */

  public MCWixXML()
  {
    this.metadata = new QCommandMetadata(
      "xml",
      new QStringType.QConstant("Generate WiX XML."),
      Optional.empty()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(
        INPUT_PACKAGE,
        INPUT_DIRECTORY
      ),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType newContext)
  {
    QLogback.configure(newContext);

    final var readers =
      new MPackageReaders();
    final var writers =
      new MWiXWriters();

    final var packageFile =
      newContext.parameterValue(INPUT_PACKAGE);

    LOG.info("Opening package {}.", packageFile);
    try (var packageReader = readers.open(packageFile)) {
      final var packageDecl =
        packageReader.packageDeclaration();

      try (var writer = writers.create(
        packageDecl,
        newContext.parameterValue(INPUT_DIRECTORY),
        System.out)) {
        writer.execute();
      }
    } catch (final MException e) {
      MCSLogging.logStructuredError(LOG, e);
      return QCommandStatus.FAILURE;
    } catch (final Exception e) {
      if (e.getCause() instanceof MException ee) {
        MCSLogging.logStructuredError(LOG, ee);
        return QCommandStatus.FAILURE;
      }
      LOG.error("", e);
      return QCommandStatus.FAILURE;
    }

    return QCommandStatus.SUCCESS;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
