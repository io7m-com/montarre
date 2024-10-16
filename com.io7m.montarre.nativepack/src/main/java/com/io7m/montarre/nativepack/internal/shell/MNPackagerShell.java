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


package com.io7m.montarre.nativepack.internal.shell;

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MJavaInfoType;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.montarre.nativepack.internal.MNArchives;
import com.io7m.montarre.nativepack.internal.MNPackagerAbstract;
import com.io7m.seltzer.api.SStructuredErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;

/**
 * A native packager that produces shell packages.
 */

public final class MNPackagerShell
  extends MNPackagerAbstract
  implements MNativePackagerServiceType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNPackagerShell.class);

  /**
   * A native packager that produces shell packages.
   *
   * @param inProvider The provider
   */

  public MNPackagerShell(
    final MNativePackagerServiceProviderType inProvider)
  {
    super(inProvider);
  }

  @Override
  public Optional<SStructuredErrorType<String>> unsupportedReason(
    final Optional<MPackageDeclaration> packageV)
  {
    return Optional.empty();
  }

  @Override
  public Path execute(
    final MNativeWorkspaceType workspace,
    final MPackageReaderType packageV)
    throws MException
  {
    Objects.requireNonNull(workspace, "workspace");
    Objects.requireNonNull(packageV, "packageV");

    try {
      final var work =
        workspace.createWorkDirectory();
      final var pack =
        work.resolve("pack");
      final var bin =
        pack.resolve("bin");

      final var packV =
        packageV.packageDeclaration();
      final var metadata =
        packV.metadata();
      final var shortName =
        metadata.names()
          .shortName();

      packageV.unpackInto(pack);

      Files.createDirectories(bin);

      writeLauncher(
        bin.resolve("launch.jar")
      );
      writeShellScript(
        bin.resolve(shortName.name()),
        metadata.javaInfo(),
        shortName
      );
      writeBatchScript(
        bin.resolve(shortName.name() + ".bat"),
        metadata.javaInfo(),
        shortName
      );

      final var outName =
        "%s-%s-any.tgz".formatted(
          metadata.names().packageName(),
          metadata.version().version().toString()
        );

      final var outFile =
        work.resolve(outName);

      MNArchives.packTar(
        pack,
        outFile,
        entry -> entry.startsWith("bin/"),
        shortName
      );
      return outFile;
    } catch (final Exception e) {
      throw this.error(e);
    }
  }

  private static void writeShellScript(
    final Path file,
    final MJavaInfoType javaInfo,
    final MShortName shortName)
    throws IOException
  {
    Files.writeString(
      file,
      MShellScripts.shellScriptText(javaInfo, shortName),
      StandardCharsets.UTF_8,
      StandardOpenOption.WRITE,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    );
  }

  private static void writeBatchScript(
    final Path file,
    final MJavaInfoType javaInfo,
    final MShortName shortName)
    throws IOException
  {
    Files.writeString(
      file,
      MBatchScripts.batchScriptText(javaInfo, shortName),
      StandardCharsets.UTF_8,
      StandardOpenOption.WRITE,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    );
  }

  private static void writeLauncher(
    final Path file)
    throws IOException
  {
    try (final var stream = MNPackagerShell.class.getResourceAsStream(
      "/com/io7m/montarre/nativepack/internal/com.io7m.montarre.launchstub.jar")) {
      Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
