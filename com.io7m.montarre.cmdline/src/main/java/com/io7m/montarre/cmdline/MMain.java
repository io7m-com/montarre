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


package com.io7m.montarre.cmdline;

import com.io7m.montarre.cmdline.internal.MCMavenDownload;
import com.io7m.montarre.cmdline.internal.MCNativeCreate;
import com.io7m.montarre.cmdline.internal.MCNativePackagers;
import com.io7m.montarre.cmdline.internal.MCPackageCheck;
import com.io7m.montarre.cmdline.internal.MCPackageExtractDeclaration;
import com.io7m.montarre.cmdline.internal.MCPackageSchema;
import com.io7m.montarre.cmdline.internal.MCWixXML;
import com.io7m.quarrel.core.QApplication;
import com.io7m.quarrel.core.QApplicationMetadata;
import com.io7m.quarrel.core.QApplicationType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QStringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The command-line program.
 */

public final class MMain implements Runnable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MMain.class);

  private final List<String> args;
  private final QApplicationType application;
  private int exitCode;

  /**
   * The main entry point.
   *
   * @param inArgs Command-line arguments
   */

  public MMain(
    final String[] inArgs)
  {
    this.args =
      Objects.requireNonNull(List.of(inArgs), "Command line arguments");

    final var metadata =
      new QApplicationMetadata(
        "montarre",
        "com.io7m.montarre",
        MVersion.MAIN_VERSION,
        MVersion.MAIN_BUILD,
        "The montarre command-line application.",
        Optional.of(URI.create("https://www.io7m.com/software/montarre/"))
      );

    final var builder = QApplication.builder(metadata);

    {
      final var g =
        builder.createCommandGroup(
          new QCommandMetadata(
            "package",
            new QStringType.QConstant("Package commands."),
            Optional.empty()
          )
        );
      g.addCommand(new MCPackageExtractDeclaration());
      g.addCommand(new MCPackageCheck());
      g.addCommand(new MCPackageSchema());
    }

    {
      final var g =
        builder.createCommandGroup(
          new QCommandMetadata(
            "native",
            new QStringType.QConstant("Native package commands."),
            Optional.empty()
          )
        );
      g.addCommand(new MCNativePackagers());
      g.addCommand(new MCNativeCreate());
    }

    {
      final var g =
        builder.createCommandGroup(
          new QCommandMetadata(
            "maven-central",
            new QStringType.QConstant("Maven Central commands."),
            Optional.empty()
          )
        );
      g.addCommand(new MCMavenDownload());
    }

    {
      final var g =
        builder.createCommandGroup(
          new QCommandMetadata(
            "wix",
            new QStringType.QConstant("WiX commands."),
            Optional.empty()
          )
        );
      g.addCommand(new MCWixXML());
    }

    builder.allowAtSyntax(true);

    this.application = builder.build();
    this.exitCode = 0;
  }

  /**
   * The main entry point.
   *
   * @param args Command line arguments
   */

  public static void main(
    final String[] args)
  {
    System.exit(mainExitless(args));
  }

  /**
   * The main (exitless) entry point.
   *
   * @param args Command line arguments
   *
   * @return The exit code
   */

  public static int mainExitless(
    final String[] args)
  {
    final MMain cm = new MMain(args);
    cm.run();
    return cm.exitCode();
  }

  /**
   * @return The program exit code
   */

  public int exitCode()
  {
    return this.exitCode;
  }

  @Override
  public void run()
  {
    this.exitCode = this.application.run(LOG, this.args).exitCode();
  }

  @Override
  public String toString()
  {
    return String.format(
      "[MMain 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
