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

import static java.lang.Boolean.TRUE;

/**
 * "check"
 */

public final class MCPackageCheck implements QCommandType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MCPackageCheck.class);

  private static final QParameterNamed1<Path> INPUT_FILE =
    new QParameterNamed1<>(
      "--file",
      List.of(),
      new QStringType.QConstant("The input file."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Boolean> CHECK_HASHES =
    new QParameterNamed1<>(
      "--check-hashes",
      List.of(),
      new QStringType.QConstant("Whether to check file hashes."),
      Optional.of(TRUE),
      Boolean.class
    );

  private final QCommandMetadata metadata;

  /**
   * Construct a command.
   */

  public MCPackageCheck()
  {
    this.metadata = new QCommandMetadata(
      "check",
      new QStringType.QConstant("Check a package file."),
      Optional.empty()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(INPUT_FILE, CHECK_HASHES),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType newContext)
  {
    QLogback.configure(newContext);

    final var inputFile =
      newContext.parameterValue(INPUT_FILE);
    final var checkHashes =
      newContext.parameterValue(CHECK_HASHES);

    final var readers =
      new MPackageReaders();

    var failed = false;

    try (final var reader = readers.open(inputFile)) {
      final var manifest = reader.packageDeclaration().manifest();
      for (final var file : manifest.items()) {
        if (checkHashes) {
          try {
            reader.checkHash(file.file());
            LOG.info("{}: OK", file.file());
          } catch (final MException e) {
            failed = true;
            MCSLogging.logStructuredError(LOG, e);
          }
        }
      }
    } catch (final MException e) {
      failed = true;
      MCSLogging.logStructuredError(LOG, e);
    }

    return failed ? QCommandStatus.FAILURE : QCommandStatus.SUCCESS;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
