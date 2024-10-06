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


package com.io7m.montarre.nativepack.internal.msi;

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeProcessesType;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.montarre.nativepack.MWiXWriters;
import com.io7m.montarre.nativepack.internal.MNPackagerAbstract;
import com.io7m.montarre.nativepack.internal.app_image.MNPackagerAppImage;
import com.io7m.montarre.nativepack.internal.app_image.MNPackagerAppImageProvider;
import com.io7m.seltzer.api.SStructuredError;
import com.io7m.seltzer.api.SStructuredErrorType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A native packager that produces MSI packages.
 */

public final class MNPackagerMSI
  extends MNPackagerAbstract
  implements MNativePackagerServiceType
{
  private final MNativeProcessesType processes;

  /**
   * A native packager that produces MSI packages.
   *
   * @param inProvider  The provider
   * @param inProcesses The processes
   */

  public MNPackagerMSI(
    final MNativePackagerServiceProviderType inProvider,
    final MNativeProcessesType inProcesses)
  {
    super(inProvider);

    this.processes =
      Objects.requireNonNull(inProcesses, "processes");
  }

  @Override
  public Optional<SStructuredErrorType<String>> unsupportedReason(
    final Optional<MPackageDeclaration> packageV)
    throws InterruptedException
  {
    final var appImages =
      new MNPackagerAppImageProvider();
    final var appImage =
      new MNPackagerAppImage(appImages);

    final var appImageReason = appImage.unsupportedReason(packageV);
    if (appImageReason.isPresent()) {
      return appImageReason;
    }

    try {
      final var r = this.processes.executeAndWait(
        System.getenv(),
        new ConcurrentLinkedQueue<>(),
        new ConcurrentLinkedQueue<>(),
        List.of(
          "wix",
          "--version"
        )
      );
      if (r != 0) {
        return Optional.of(
          new SStructuredError<>(
            "error-wix",
            "The wix.exe tool is either missing or not working.",
            Map.of(),
            Optional.of(
              "Install a working wix.exe tool."),
            Optional.empty()
          )
        );
      }
    } catch (final MException e) {
      return Optional.of(e);
    }

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

    final var appImages =
      new MNPackagerAppImageProvider();
    final var appImage =
      new MNPackagerAppImage(appImages);

    appImage.execute(workspace, packageV);

    final var appImageRoot =
      appImage.appImageRoot();

    final var wixWriters =
      new MWiXWriters();
    final var wixDirectory =
      workspace.createWorkDirectory();

    final var wixXML =
      wixDirectory.resolve("wix.xml");

    try (var output =
           Files.newOutputStream(wixXML, this.writeReplaceOptions())) {
      try (var writer = wixWriters.create(
        packageV.packageDeclaration(),
        appImageRoot,
        output
      )) {
        writer.execute();
      }
    } catch (final IOException e) {
      throw this.error(e);
    }

    final var outputName =
      msiName(workspace, packageV.packageDeclaration().metadata());
    final var msiOut =
      wixDirectory.resolve(outputName);

    try {
      final var r = this.processes.executeAndWait(
        System.getenv(),
        new ConcurrentLinkedQueue<>(),
        new ConcurrentLinkedQueue<>(),
        List.of(
          "wix.exe",
          "build",
          wixXML.toString(),
          "-out",
          msiOut.toString()
        )
      );
      if (r != 0) {
        throw new MException(
          "The wix tool returned a non-zero error code.",
          "error-wix",
          Map.ofEntries(
            Map.entry("Error Code", Integer.toString(r))
          )
        );
      }
    } catch (final InterruptedException e) {
      throw this.error(e);
    }

    return msiOut;
  }

  private static String msiName(
    final MNativeWorkspaceType workspace,
    final MMetadataType metadata)
  {
    return "%s-%s-%s-%s.msi".formatted(
      metadata.names().shortName().name(),
      metadata.version().version().toString(),
      workspace.architecture(),
      workspace.operatingSystem()
    );
  }
}
