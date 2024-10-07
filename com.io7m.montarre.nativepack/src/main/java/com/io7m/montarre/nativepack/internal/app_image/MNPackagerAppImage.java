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


package com.io7m.montarre.nativepack.internal.app_image;

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.montarre.nativepack.internal.MNPackagerAbstract;
import com.io7m.seltzer.api.SStructuredError;
import com.io7m.seltzer.api.SStructuredErrorType;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.spi.ToolProvider;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A native packager that produces jpackage "app-images".
 */

public final class MNPackagerAppImage
  extends MNPackagerAbstract
  implements MNativePackagerServiceType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNPackagerAppImage.class);

  private static final Instant SOURCE_EPOCH =
    Instant.parse("2010-01-01T00:00:00+00:00");
  private static final FileTime SOURCE_EPOCH_FILETIME =
    FileTime.from(SOURCE_EPOCH);

  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };

  private Path appImageRoot;

  /**
   * A native packager that produces jpackage "app-images".
   *
   * @param inProvider The provider
   */

  public MNPackagerAppImage(
    final MNativePackagerServiceProviderType inProvider)
  {
    super(inProvider);
  }

  private void executeJPackage(
    final MNativeWorkspaceType workspace,
    final Path jdkPath,
    final MMetadataType metadata,
    final Path appDirectory,
    final Path buildDirectory,
    final Optional<Path> iconFile,
    final ToolProvider tool)
    throws MException
  {
    final var arguments = new ArrayList<String>();
    arguments.add("--verbose");
    arguments.add("--type");
    arguments.add("app-image");
    arguments.add("--runtime-image");
    arguments.add(jdkPath.toString());
    arguments.add("--name");
    arguments.add(metadata.names().shortName().name());
    arguments.add("--module");
    arguments.add(metadata.javaInfo().mainModule());
    arguments.add("--module-path");
    arguments.add(appDirectory.resolve("lib").toString());
    arguments.add("--dest");
    arguments.add(buildDirectory.toString());
    arguments.add("--copyright");
    arguments.add(metadata.copying().copyright());
    arguments.add("--description");
    arguments.add(metadata.description());
    arguments.add("--app-content");
    arguments.add(appDirectory.resolve("meta").toString());
    arguments.add("--app-version");
    arguments.add(
      this.translateVersion(workspace, metadata.version().version()).toString()
    );

    if (iconFile.isPresent()) {
      arguments.add("--icon");
      arguments.add(iconFile.get().toString());
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

  @Override
  public Optional<SStructuredErrorType<String>> unsupportedReason(
    final Optional<MPackageDeclaration> packageV)
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
      final var metadata =
        packageV.packageDeclaration().metadata();

      this.executeJPackage(
        workspace,
        jdkPath,
        metadata,
        appDirectory,
        buildDirectory,
        iconFile,
        tool
      );

      this.appImageRoot =
        buildDirectory.resolve(metadata.names().shortName().name());

      return this.packOutput(
        workspace,
        outputDirectory,
        metadata.names().shortName(),
        this.archiveName(workspace, metadata)
      );
    } catch (final Exception e) {
      throw this.error(e);
    }
  }

  /**
   * @return The app image root directory
   */

  public Path appImageRoot()
  {
    return this.appImageRoot;
  }

  private String archiveName(
    final MNativeWorkspaceType workspace,
    final MMetadataType metadata)
  {
    return "%s-%s-%s-%s".formatted(
      metadata.names().packageName(),
      metadata.version().version().toString(),
      workspace.architecture(),
      workspace.operatingSystem()
    );
  }

  private Path packOutput(
    final MNativeWorkspaceType workspace,
    final Path outDirectory,
    final MShortName shortName,
    final String baseName)
    throws IOException, ClosingResourceFailedException
  {
    if (Objects.equals(
      workspace.operatingSystem(),
      MOperatingSystemName.windows())) {
      return this.packZip(
        outDirectory.resolve(baseName + ".zip"),
        shortName
      );
    }

    return this.packTar(
      outDirectory.resolve(baseName + ".tgz"),
      shortName
    );
  }

  private Path packTar(
    final Path outFile,
    final MShortName shortName)
    throws IOException, ClosingResourceFailedException
  {
    LOG.info("Creating tar {}", outFile);
    LOG.debug("Input directory: {}", this.appImageRoot);

    try (final var streams = CloseableCollection.create()) {
      final var fileStream =
        streams.add(Files.walk(this.appImageRoot));
      final var fileList =
        fileStream.sorted()
          .toList();

      final var fileOut =
        streams.add(Files.newOutputStream(outFile, OPEN_OPTIONS));
      final var bufOut =
        streams.add(new BufferedOutputStream(fileOut, 65536));
      final var gzipOut =
        streams.add(new GZIPOutputStream(bufOut));
      final var tarOut =
        streams.add(new TarArchiveOutputStream(gzipOut));

      this.createTarEntries(shortName.name(), tarOut, fileList);
    }

    return outFile;
  }

  private void createTarEntries(
    final String prefix,
    final TarArchiveOutputStream tarOut,
    final List<Path> fileList)
    throws IOException
  {
    for (final var file : fileList) {
      if (Files.isRegularFile(file)) {
        LOG.debug("[tar] {}", file);

        final var entry =
          tarOut.createArchiveEntry(
            file,
            prefix + "/" + this.appImageRoot.relativize(file)
          );
        entry.setCreationTime(SOURCE_EPOCH_FILETIME);
        entry.setLastAccessTime(SOURCE_EPOCH_FILETIME);
        entry.setLastModifiedTime(SOURCE_EPOCH_FILETIME);
        entry.setUserId(0);
        entry.setGroupId(0);

        if (Files.isExecutable(file)) {
          entry.setMode(0755);
        } else {
          entry.setMode(0644);
        }
        tarOut.putArchiveEntry(entry);
        Files.copy(file, tarOut);
        tarOut.closeArchiveEntry();
      }
    }
  }

  private Path packZip(
    final Path outputFile,
    final MShortName shortName)
    throws IOException, ClosingResourceFailedException
  {
    LOG.info("Creating zip {}", outputFile);
    LOG.debug("Input directory: {}", this.appImageRoot);

    try (final var streams = CloseableCollection.create()) {
      final var fileStream =
        streams.add(Files.walk(this.appImageRoot));
      final var fileList =
        fileStream.sorted()
          .toList();
      final var fileOut =
        streams.add(Files.newOutputStream(outputFile, OPEN_OPTIONS));
      final var bufOut =
        streams.add(new BufferedOutputStream(fileOut, 65536));
      final var zipOut =
        streams.add(new ZipOutputStream(bufOut));

      this.createZipEntries(shortName.name(), zipOut, fileList);
    }

    return outputFile;
  }

  private void createZipEntries(
    final String prefix,
    final ZipOutputStream zipOut,
    final List<Path> fileList)
    throws IOException
  {
    for (final var file : fileList) {
      if (Files.isRegularFile(file)) {
        LOG.debug("[zip] {}", file);

        final var entry =
          new ZipEntry(prefix + "/" + this.appImageRoot.relativize(file));

        entry.setCreationTime(SOURCE_EPOCH_FILETIME);
        entry.setLastAccessTime(SOURCE_EPOCH_FILETIME);
        entry.setLastModifiedTime(SOURCE_EPOCH_FILETIME);

        zipOut.putNextEntry(entry);
        Files.copy(file, zipOut);
      }
    }
  }
}
