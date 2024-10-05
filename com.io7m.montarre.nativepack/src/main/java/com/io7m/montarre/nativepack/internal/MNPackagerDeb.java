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


package com.io7m.montarre.nativepack.internal;

import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeProcessesType;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.seltzer.api.SStructuredError;
import com.io7m.seltzer.api.SStructuredErrorType;
import com.io7m.verona.core.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.spi.ToolProvider;

/**
 * A native packager that produces Debian packages.
 */

public final class MNPackagerDeb
  extends MNPackagerAbstract
  implements MNativePackagerServiceType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNPackagerDeb.class);

  private static final Instant SOURCE_EPOCH =
    Instant.parse("2010-01-01T00:00:00+00:00");
  private static final FileTime SOURCE_EPOCH_FILETIME =
    FileTime.from(SOURCE_EPOCH);
  private static final RDottedName NAME =
    new RDottedName("com.io7m.montarre.deb");

  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };
  private final MNativeProcessesType processes;

  /**
   * A native packager that produces Debian packages.
   *
   * @param inProvider  The provider
   * @param inProcesses The processes
   */

  public MNPackagerDeb(
    final MNativePackagerServiceProviderType inProvider,
    final MNativeProcessesType inProcesses)
  {
    super(inProvider);

    this.processes =
      Objects.requireNonNull(inProcesses, "processes");
  }

  @Override
  public Optional<SStructuredErrorType<String>> unsupportedReason()
    throws InterruptedException
  {
    final var toolOpt =
      ToolProvider.findFirst("jpackage");

    if (toolOpt.isEmpty()) {
      return Optional.of(
        new SStructuredError<>(
          "error-jpackage",
          "The jpackage tool does not appear to be present in this JDK.",
          Map.of(),
          Optional.of(
            "Verify that you are using a JDK with the jdk.jpackage module installed and working."),
          Optional.empty()
        )
      );
    }

    try {
      final var r = this.processes.executeAndWait(
        System.getenv(),
        new ConcurrentLinkedQueue<>(),
        new ConcurrentLinkedQueue<>(),
        List.of(
          "dpkg",
          "--version"
        )
      );
      if (r != 0) {
        return Optional.of(
          new SStructuredError<>(
            "error-dpkg",
            "The dpkg tool is either missing or not working.",
            Map.of(),
            Optional.of(
              "Install a working dpkg tool."),
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

    final var tool =
      ToolProvider.findFirst("jpackage")
        .orElseThrow(() -> new IllegalStateException("jpackage tool missing."));

    try {
      final var directory =
        workspace.createWorkDirectory();
      final var appDirectory =
        directory.resolve("app");
      final var outputDirectory =
        directory.resolve("output");
      final var buildDirectory =
        outputDirectory.resolve("build");

      this.setAttribute("Directory", appDirectory);
      Files.createDirectories(appDirectory);
      this.setAttribute("Directory", outputDirectory);
      Files.createDirectories(outputDirectory);
      this.setAttribute("Directory", buildDirectory);
      Files.createDirectories(buildDirectory);

      final var jdkPath =
        workspace.javaRuntime()
          .get();

      LOG.info("Unpacking application to {}.", appDirectory);
      packageV.unpackInto(appDirectory);

      final var iconFile =
        this.unpackIcon(workspace, packageV, directory);
      final var licenseFile =
        this.unpackLicense(packageV, directory);

      final var metadata =
        packageV.packageDeclaration().metadata();

      executeJPackage(
        jdkPath,
        metadata.shortName(),
        metadata,
        appDirectory,
        buildDirectory,
        iconFile,
        licenseFile,
        tool
      );

      return this.findOutput(
        workspace,
        metadata.shortName(),
        metadata.version(),
        buildDirectory
      );
    } catch (final Exception e) {
      throw this.error(e);
    }
  }

  private static void executeJPackage(
    final Path jdkPath,
    final MShortName shortName,
    final MMetadataType metadata,
    final Path appDirectory,
    final Path buildDirectory,
    final Optional<Path> iconFile,
    final Optional<Path> licenseFile,
    final ToolProvider tool)
    throws MException
  {
    final var arguments = new ArrayList<String>();
    arguments.add("--verbose");
    arguments.add("--type");
    arguments.add("deb");
    arguments.add("--runtime-image");
    arguments.add(jdkPath.toString());
    arguments.add("--name");
    arguments.add(shortName.name());
    arguments.add("--module");
    arguments.add(metadata.mainModule());
    arguments.add("--module-path");
    arguments.add(appDirectory.resolve("lib").toString());
    arguments.add("--app-version");
    arguments.add(metadata.version().toString());
    arguments.add("--dest");
    arguments.add(buildDirectory.toString());
    arguments.add("--copyright");
    arguments.add(metadata.copyright());
    arguments.add("--description");
    arguments.add(metadata.description());
    arguments.add("--linux-package-name");
    arguments.add(metadata.shortName().name());
    arguments.add("--about-url");
    arguments.add(metadata.siteURI().toString());

    if (iconFile.isPresent()) {
      arguments.add("--icon");
      arguments.add(iconFile.get().toString());
    }

    if (licenseFile.isPresent()) {
      arguments.add("--license-file");
      arguments.add(licenseFile.get().toString());
    }

    LOG.info("Executing jpackage tool.");
    final var r =
      tool.run(System.out, System.err, arguments.toArray(new String[0]));

    if (r != 0) {
      throw new MException(
        "The jpackage tool returned an error.",
        "error-jpackage",
        Map.of("Exit Code", Integer.toString(r))
      );
    }
  }

  private Path findOutput(
    final MNativeWorkspaceType workspace,
    final MShortName shortName,
    final Version version,
    final Path buildDirectory)
    throws IOException
  {
    try (var stream = Files.list(buildDirectory)) {
      final var fileList =
        stream.filter(n -> n.getFileName().toString().endsWith(".deb"))
          .toList();

      if (fileList.isEmpty()) {
        throw new IllegalStateException(
          "Unable to locate the created .deb file!");
      }

      final var newName =
        "%s-%s-%s-%s.deb".formatted(
          shortName,
          version,
          workspace.architecture(),
          workspace.operatingSystem()
        );

      final var newPath =
        buildDirectory.resolve(newName);

      Files.move(
        fileList.get(0),
        newPath,
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING
      );

      return newPath;
    }
  }
}
