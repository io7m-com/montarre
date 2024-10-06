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


package com.io7m.montarre.nativepack.internal.flatpak;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MFlatpakRuntime;
import com.io7m.montarre.api.MFlatpakRuntimeRole;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeProcessesType;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.montarre.nativepack.internal.MNPackagerAbstract;
import com.io7m.montarre.nativepack.internal.app_image.MNPackagerAppImage;
import com.io7m.montarre.nativepack.internal.app_image.MNPackagerAppImageProvider;
import com.io7m.seltzer.api.SStructuredError;
import com.io7m.seltzer.api.SStructuredErrorType;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A native packager that produces Flatpak packages.
 */

public final class MNPackagerFlatpak
  extends MNPackagerAbstract
  implements MNativePackagerServiceType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNPackagerFlatpak.class);

  private final MNativeProcessesType processes;

  /**
   * A native packager that produces Debian packages.
   *
   * @param inProvider  The provider
   * @param inProcesses The processes
   */

  public MNPackagerFlatpak(
    final MNativePackagerServiceProviderType inProvider,
    final MNativeProcessesType inProcesses)
  {
    super(inProvider);

    this.processes =
      Objects.requireNonNull(inProcesses, "processes");
  }

  @Override
  public Optional<SStructuredErrorType<String>> unsupportedReason(
    final Optional<MPackageDeclaration> packageVOpt)
    throws InterruptedException
  {
    final var appImages =
      new MNPackagerAppImageProvider();
    final var appImage =
      new MNPackagerAppImage(appImages);

    final var appImageReason = appImage.unsupportedReason(packageVOpt);
    if (appImageReason.isPresent()) {
      return appImageReason;
    }

    try {
      final var r = this.processes.executeAndWait(
        System.getenv(),
        new ConcurrentLinkedQueue<>(),
        new ConcurrentLinkedQueue<>(),
        List.of(
          "flatpak",
          "--version"
        )
      );
      if (r != 0) {
        return Optional.of(
          new SStructuredError<>(
            "error-flatpak",
            "The flatpak tool is either missing or not working.",
            Map.of(),
            Optional.of(
              "Install a working flatpak tool."),
            Optional.empty()
          )
        );
      }
    } catch (final MException e) {
      return Optional.of(e);
    }

    if (packageVOpt.isPresent()) {
      final var packageV =
        packageVOpt.get();

      final var missingSDK =
        packageV.metadata()
          .flatpak()
          .runtimes()
          .stream()
          .noneMatch(r -> r.role() == MFlatpakRuntimeRole.SDK);

      final var missingPlatform =
        packageV.metadata()
          .flatpak()
          .runtimes()
          .stream()
          .noneMatch(r -> r.role() == MFlatpakRuntimeRole.PLATFORM);

      if (missingSDK || missingPlatform) {
        return Optional.of(
          new SStructuredError<>(
            "error-flatpak-runtimes",
            "The flatpak metadata is missing an SDK and/or platform.",
            Map.of(),
            Optional.of(
              "Add a runtime with an SDK role, and a runtime with a PLATFORM role."),
            Optional.empty()
          )
        );
      }
    }

    return Optional.empty();
  }

  @Override
  public Path execute(
    final MNativeWorkspaceType workspace,
    final MPackageReaderType reader)
    throws MException
  {
    Objects.requireNonNull(workspace, "workspace");
    Objects.requireNonNull(reader, "reader");

    try {
      final var packageV =
        reader.packageDeclaration();

      final var sdk =
        packageV.metadata()
          .flatpak()
          .runtimes()
          .stream()
          .filter(r -> r.role() == MFlatpakRuntimeRole.SDK)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Missing SDK!"));

      final var platform =
        packageV.metadata()
          .flatpak()
          .runtimes()
          .stream()
          .filter(r -> r.role() == MFlatpakRuntimeRole.PLATFORM)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Missing platform!"));

      final var directory =
        workspace.createWorkDirectory();

      final var outputFile =
        directory.resolve(
          "%s.flatpak".formatted(packageV.metadata().names().packageName())
        );

      final var build =
        directory.resolve("build");
      final var repos =
        directory.resolve("repos");

      Files.createDirectories(build);
      Files.createDirectories(repos);

      this.flatpakInstallRuntime(workspace, sdk);
      this.flatpakInstallRuntime(workspace, platform);
      this.flatpakBuildInit(workspace, packageV, build, sdk, platform);
      this.flatpakWriteMetadata(
        workspace,
        build,
        packageV,
        sdk,
        platform
      );

      generateAndCopyAppImage(workspace, reader, build);

      this.flatpakWriteDesktopFile(build, packageV);
      this.flatpakWriteAppInfoFile(build, packageV);
      this.flatpakWriteIcons(reader, build, packageV);
      this.flatpakBuildFinish(build);
      this.flatpakBuildExport(build, repos);
      this.flatpakBuildBundle(repos, outputFile, packageV);

      return outputFile;
    } catch (final Exception e) {
      throw this.error(e);
    }
  }

  private void flatpakWriteAppInfoFile(
    final Path build,
    final MPackageDeclaration packageV)
    throws IOException
  {
    LOG.info("Writing appinfo file.");

    /*
     * Some systems expect an ".appdata.xml" file, whilst some expect a
     * ".metainfo.xml" file. Given that we're generating the content, we
     * can install both.
     */

    final var text =
      MNAppInfoFile.createAppInfoFileText(packageV);

    final var appdata =
      build.resolve("files")
        .resolve("share")
        .resolve("metainfo")
        .resolve(packageV.metadata().names().packageName() + ".appdata.xml");

    final var metainfo =
      build.resolve("files")
        .resolve("share")
        .resolve("metainfo")
        .resolve(packageV.metadata().names().packageName() + ".metainfo.xml");

    Files.createDirectories(appdata.getParent());
    Files.writeString(appdata, text, this.writeReplaceOptions());
    Files.createDirectories(metainfo.getParent());
    Files.writeString(metainfo, text, this.writeReplaceOptions());
  }

  private void flatpakWriteDesktopFile(
    final Path build,
    final MPackageDeclaration packageV)
    throws IOException
  {
    LOG.info("Writing desktop file.");

    final var desktopFile =
      build.resolve("files")
        .resolve("share")
        .resolve("applications")
        .resolve(packageV.metadata().names().packageName() + ".desktop");

    Files.createDirectories(desktopFile.getParent());
    Files.writeString(
      desktopFile,
      MNDesktopFile.createDesktopFileText(packageV),
      this.writeReplaceOptions()
    );
  }

  private void flatpakWriteIcons(
    final MPackageReaderType reader,
    final Path build,
    final MPackageDeclaration packageV)
    throws MException, IOException
  {
    LOG.info("Writing icons.");

    final var icons =
      packageV.manifest()
        .items()
        .stream()
        .filter(i -> i instanceof MResource)
        .map(MResource.class::cast)
        .filter(i -> i.role().isIcon())
        .toList();

    final var iconsDirectory =
      build.resolve("files")
        .resolve("share")
        .resolve("icons")
        .resolve("hicolor");

    for (final var icon : icons) {
      switch (icon.role()) {
        case BOM, LICENSE, ICO_WINDOWS -> {
          // Not used
        }
        case ICON_16 -> {
          writeIcon(iconsDirectory, "16x16", packageV, reader, icon);
        }
        case ICON_24 -> {
          writeIcon(iconsDirectory, "24x24", packageV, reader, icon);
        }
        case ICON_32 -> {
          writeIcon(iconsDirectory, "32x32", packageV, reader, icon);
        }
        case ICON_48 -> {
          writeIcon(iconsDirectory, "48x48", packageV, reader, icon);
        }
        case ICON_64 -> {
          writeIcon(iconsDirectory, "64x64", packageV, reader, icon);
        }
        case ICON_128 -> {
          writeIcon(iconsDirectory, "128x128", packageV, reader, icon);
        }
      }
    }
  }

  private static void writeIcon(
    final Path iconsDirectory,
    final String sizeDirectory,
    final MPackageDeclaration packageV,
    final MPackageReaderType reader,
    final MResource icon)
    throws IOException, MException
  {
    LOG.debug("Writing icon {}.", sizeDirectory);

    final var outFile =
      iconsDirectory.resolve(sizeDirectory)
        .resolve("apps")
        .resolve(packageV.metadata().names().packageName() + ".png");

    Files.createDirectories(outFile.getParent());
    try (final var stream = reader.readFile(icon.file())) {
      Files.copy(stream, outFile);
    }
  }

  private void flatpakBuildBundle(
    final Path repos,
    final Path outputFile,
    final MPackageDeclaration packageV)
    throws MException, InterruptedException
  {
    LOG.info("Creating Flatpak bundle");

    final var r = this.processes.executeAndWait(
      System.getenv(),
      new ConcurrentLinkedQueue<>(),
      new ConcurrentLinkedQueue<>(),
      List.of(
        "flatpak",
        "build-bundle",
        "--verbose",
        repos.toString(),
        outputFile.toString(),
        packageV.metadata().names().packageName().toString()
      )
    );

    if (r != 0) {
      throw new MException(
        "The flatpak tool returned a non-zero exit code.",
        "error-flatpak",
        Map.of(
          "Error Code", Integer.toString(r)
        )
      );
    }
  }

  private void flatpakBuildExport(
    final Path build,
    final Path repos)
    throws MException, InterruptedException
  {
    LOG.info("Exporting Flatpak build");

    final var r = this.processes.executeAndWait(
      System.getenv(),
      new ConcurrentLinkedQueue<>(),
      new ConcurrentLinkedQueue<>(),
      List.of(
        "flatpak",
        "build-export",
        "--verbose",
        repos.toString(),
        build.toString()
      )
    );

    if (r != 0) {
      throw new MException(
        "The flatpak tool returned a non-zero exit code.",
        "error-flatpak",
        Map.of(
          "Error Code", Integer.toString(r)
        )
      );
    }
  }

  private void flatpakBuildFinish(
    final Path directory)
    throws MException, InterruptedException
  {
    LOG.info("Finishing Flatpak build.");

    final var r = this.processes.executeAndWait(
      System.getenv(),
      new ConcurrentLinkedQueue<>(),
      new ConcurrentLinkedQueue<>(),
      List.of(
        "flatpak",
        "build-finish",
        "--verbose",
        directory.toString()
      )
    );

    if (r != 0) {
      throw new MException(
        "The flatpak tool returned a non-zero exit code.",
        "error-flatpak",
        Map.of(
          "Error Code", Integer.toString(r)
        )
      );
    }
  }

  private static void generateAndCopyAppImage(
    final MNativeWorkspaceType workspace,
    final MPackageReaderType reader,
    final Path build)
    throws MException, IOException
  {
    LOG.info("Generating app-image.");

    final var appImages =
      new MNPackagerAppImageProvider();
    final var appImage =
      new MNPackagerAppImage(appImages);

    appImage.execute(workspace, reader);

    final var appImageRoot =
      appImage.appImageRoot();

    PathUtils.copyDirectory(
      appImageRoot.resolve("bin"),
      build.resolve("files").resolve("bin")
    );
    PathUtils.copyDirectory(
      appImageRoot.resolve("lib"),
      build.resolve("files").resolve("lib")
    );
  }

  private void flatpakWriteMetadata(
    final MNativeWorkspaceType workspace,
    final Path directory,
    final MPackageDeclaration packageV,
    final MFlatpakRuntime sdk,
    final MFlatpakRuntime platform)
    throws IOException
  {
    LOG.info("Writing Flatpak metadata.");

    Preconditions.checkPreconditionV(
      sdk.role() == MFlatpakRuntimeRole.SDK,
      "Runtime must have an SDK role"
    );
    Preconditions.checkPreconditionV(
      platform.role() == MFlatpakRuntimeRole.PLATFORM,
      "Runtime must have a PLATFORM role"
    );

    final var metadata =
      directory.resolve("metadata");

    try (final var writer =
           Files.newBufferedWriter(metadata, this.writeReplaceOptions())) {
      writer.append("[Application]");
      writer.append('\n');

      writer.append("name=");
      writer.append(packageV.metadata().names().packageName().toString());
      writer.append('\n');

      writer.append("runtime=");
      writer.append(platform.fullName(workspace.architecture()));
      writer.append('\n');

      writer.append("sdk=");
      writer.append(sdk.fullName(workspace.architecture()));
      writer.append('\n');

      writer.append("command=/app/bin/");
      writer.append(packageV.metadata().names().shortName().toString());
      writer.append('\n');
    }
  }

  private void flatpakBuildInit(
    final MNativeWorkspaceType workspace,
    final MPackageDeclaration packageV,
    final Path directory,
    final MFlatpakRuntime sdk,
    final MFlatpakRuntime platform)
    throws MException, InterruptedException
  {
    LOG.info("Initializing Flatpak build.");

    Preconditions.checkPreconditionV(
      sdk.role() == MFlatpakRuntimeRole.SDK,
      "Runtime must have an SDK role"
    );
    Preconditions.checkPreconditionV(
      platform.role() == MFlatpakRuntimeRole.PLATFORM,
      "Runtime must have a PLATFORM role"
    );

    final var r = this.processes.executeAndWait(
      System.getenv(),
      new ConcurrentLinkedQueue<>(),
      new ConcurrentLinkedQueue<>(),
      List.of(
        "flatpak",
        "build-init",
        "--verbose",
        directory.toString(),
        packageV.metadata().names().packageName().name().value(),
        sdk.fullName(workspace.architecture()),
        platform.fullName(workspace.architecture())
      )
    );

    if (r != 0) {
      throw new MException(
        "The flatpak tool returned a non-zero exit code.",
        "error-flatpak",
        Map.of(
          "Error Code", Integer.toString(r)
        )
      );
    }
  }

  private void flatpakInstallRuntime(
    final MNativeWorkspaceType workspace,
    final MFlatpakRuntime runtime)
    throws MException, InterruptedException
  {
    final var name =
      runtime.fullName(workspace.architecture());

    LOG.info("Installing Flatpak runtime {}.", name);

    final var r = this.processes.executeAndWait(
      System.getenv(),
      new ConcurrentLinkedQueue<>(),
      new ConcurrentLinkedQueue<>(),
      List.of(
        "flatpak",
        "install",
        "--or-update",
        "--verbose",
        "--assumeyes",
        name
      )
    );

    if (r != 0) {
      throw new MException(
        "The flatpak tool returned a non-zero exit code.",
        "error-flatpak",
        Map.of(
          "Error Code", Integer.toString(r)
        )
      );
    }
  }
}
