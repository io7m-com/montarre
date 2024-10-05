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

import com.io7m.montarre.nativepack.MNPackagers;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType;
import com.io7m.quarrel.ext.logback.QLogback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * "packagers"
 */

public final class MCNativePackagers implements QCommandType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MCPackageCheck.class);

  private final QCommandMetadata metadata;

  /**
   * Construct a command.
   */

  public MCNativePackagers()
  {
    this.metadata = new QCommandMetadata(
      "packagers",
      new QStringType.QConstant("List native packagers."),
      Optional.empty()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType newContext)
    throws InterruptedException
  {
    QLogback.configure(newContext);

    final var directory =
      MNPackagers.createFromServiceLoader();

    final var out = newContext.output();
    for (var entry : directory.packagers().entrySet()) {
      final var name =
        entry.getKey();
      final var packager =
        entry.getValue();

      out.println("Name: " + name);
      out.println("Description: " + packager.describe());

      final var unsupported =
        packager.unsupportedReason(Optional.empty());

      if (unsupported.isPresent()) {
        final var reason = unsupported.get();
        out.println("Supported: no");
        out.println("Reason: " + reason.message());
        for (var e : reason.attributes().entrySet()) {
          out.printf("  %s: %s%n", e.getKey(), e.getValue());
        }
        reason.remediatingAction().ifPresent(s -> {
          out.println("Suggested action: " + s);
        });
        reason.exception().ifPresent(throwable -> {
          out.println("Exception: ");
          throwable.printStackTrace(out);
        });
      } else {
        out.println("Supported: yes");
      }
      out.println("--");
      out.println();
    }

    return QCommandStatus.SUCCESS;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
