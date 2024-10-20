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

import com.io7m.montarre.adoptium.MEARuntimeSearch;
import com.io7m.montarre.adoptium.MEAdoptiumConfiguration;
import com.io7m.montarre.adoptium.MEAdoptiumFactory;
import com.io7m.montarre.adoptium.METImageKind;
import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MArchiveFormat;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.api.http.MHTTPClients;
import com.io7m.montarre.api.natives.MNativePackagerDirectoryType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeWorkspaceConfiguration;
import com.io7m.montarre.io.MPackageReaders;
import com.io7m.montarre.nativepack.MNPackagers;
import com.io7m.montarre.nativepack.MNWorkspaces;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed0N;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType;
import com.io7m.quarrel.ext.logback.QLogback;
import com.io7m.streamtime.core.STTransferStatistics;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * "create"
 */

public final class MCNativeCreate implements QCommandType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MCNativeCreate.class);

  private final QCommandMetadata metadata;

  private static final QParameterNamed0N<String> INCLUDE_PACKAGERS =
    new QParameterNamed0N<>(
      "--include-packagers",
      List.of(),
      new QStringType.QConstant("Only run the named packagers."),
      List.of(),
      String.class
    );

  private static final QParameterNamed1<Path> INPUT_PACKAGE =
    new QParameterNamed1<>(
      "--package",
      List.of(),
      new QStringType.QConstant("The input package."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Path> WORK_DIRECTORY =
    new QParameterNamed1<>(
      "--work-directory",
      List.of(),
      new QStringType.QConstant("The work directory."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Path> OUTPUT_DIRECTORY =
    new QParameterNamed1<>(
      "--output-directory",
      List.of(),
      new QStringType.QConstant("The output directory."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed01<URI> JAVA_DOWNLOAD_URI =
    new QParameterNamed01<>(
      "--java-runtime-download-uri",
      List.of(),
      new QStringType.QConstant("The location for a Java runtime."),
      Optional.empty(),
      URI.class
    );

  private static final QParameterNamed01<String> JAVA_DOWNLOAD_SHA256 =
    new QParameterNamed01<>(
      "--java-runtime-sha256",
      List.of(),
      new QStringType.QConstant("The SHA-256 of the Java runtime."),
      Optional.empty(),
      String.class
    );

  private static final QParameterNamed01<MArchiveFormat> JAVA_DOWNLOAD_FORMAT =
    new QParameterNamed01<>(
      "--java-runtime-format",
      List.of(),
      new QStringType.QConstant("The format of the Java runtime archive."),
      Optional.empty(),
      MArchiveFormat.class
    );

  private static final QParameterNamed01<Runtime.Version> ADOPTIUM_TEMURIN_VERSION =
    new QParameterNamed01<>(
      "--adoptium-temurin-version",
      List.of(),
      new QStringType.QConstant(
        "The version of the Adoptium Temurin runtime to use."),
      Optional.empty(),
      Runtime.Version.class
    );

  /**
   * Construct a command.
   */

  public MCNativeCreate()
  {
    this.metadata = new QCommandMetadata(
      "create",
      new QStringType.QConstant("Create native packages."),
      Optional.empty()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(
        ADOPTIUM_TEMURIN_VERSION,
        INCLUDE_PACKAGERS,
        INPUT_PACKAGE,
        JAVA_DOWNLOAD_FORMAT,
        JAVA_DOWNLOAD_SHA256,
        JAVA_DOWNLOAD_URI,
        OUTPUT_DIRECTORY,
        WORK_DIRECTORY
      ),
      QLogback.parameters().stream()
    ).toList();
  }

  private record RuntimeParameters(
    URI runtimeURI,
    MHash runtimeHash,
    MArchiveFormat format)
  {

  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType newContext)
    throws QException
  {
    QLogback.configure(newContext);

    final RuntimeParameters runtimeParameters;
    try {
      runtimeParameters = this.handleRuntimeParameters(newContext);
    } catch (final MException e) {
      MCSLogging.logStructuredError(LOG, e);
      return QCommandStatus.FAILURE;
    }

    final var httpClients =
      new MHTTPClients();
    final var readers =
      new MPackageReaders();
    final var workspaces =
      new MNWorkspaces();
    final var packagers =
      MNPackagers.createFromServiceLoader();

    final var packageFile =
      newContext.parameterValue(INPUT_PACKAGE);
    final var outputDirectory =
      newContext.parameterValue(OUTPUT_DIRECTORY);

    final var workspaceConfig =
      MNativeWorkspaceConfiguration.builder()
        .setBaseDirectory(
          newContext.parameterValue(WORK_DIRECTORY))
        .setJavaRuntimeDownloadURI(
          runtimeParameters.runtimeURI)
        .setJavaRuntimeDownloadSHA256(
          runtimeParameters.runtimeHash.value().value())
        .setJavaRuntimeDownloadFormat(
          runtimeParameters.format)
        .build();

    LOG.info("Opening package {}.", packageFile);
    try (final var packageReader = readers.open(packageFile)) {
      LOG.info("Creating output directory {}", outputDirectory);
      Files.createDirectories(outputDirectory);

      LOG.info("Creating workspace {}.", workspaceConfig.baseDirectory());
      try (final var workspace = workspaces.open(
        workspaceConfig,
        httpClients,
        packageReader)) {
        LOG.info("Workspace architecture: {}", workspace.architecture());
        LOG.info("Workspace OS: {}", workspace.operatingSystem());

        LOG.info("Downloading Java runtime.");
        workspace.javaRuntimeDownload()
          .subscribe(new MCPerpetualSubscriber<>(this::onJavaDownloadProgress));
        workspace.javaRuntime()
          .get();

        final var packagerList =
          this.getPackagers(newContext, packagers);
        LOG.info("Executing {} packagers.", packagerList.size());

        final var packageDecl = packageReader.packageDeclaration();
        for (final var packager : packagerList) {
          final var unsupportedOpt =
            packager.unsupportedReason(Optional.of(packageDecl));
          if (unsupportedOpt.isPresent()) {
            final var unsupported = unsupportedOpt.get();
            LOG.info(
              "Unsupported: {} {}",
              packager.name(),
              unsupported.message());
            continue;
          }

          LOG.info("Executing packager {}.", packager.name());
          final var output = packager.execute(workspace, packageReader);

          LOG.info("Created {}", output);
          Files.move(
            output,
            outputDirectory.resolve(output.getFileName()),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
          );
        }
      }
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

    return QCommandStatus.SUCCESS;
  }

  private RuntimeParameters handleRuntimeParameters(
    final QCommandContextType newContext)
    throws QException, MException
  {
    final var uriOpt =
      newContext.parameterValue(JAVA_DOWNLOAD_URI);
    final var temurinOpt =
      newContext.parameterValue(ADOPTIUM_TEMURIN_VERSION);

    if (uriOpt.isEmpty() && temurinOpt.isEmpty()) {
      LOG.error(
        "At least one of {} or {} must be specified.",
        JAVA_DOWNLOAD_URI.name(),
        ADOPTIUM_TEMURIN_VERSION.name()
      );
      throw new QException(
        "Missing parameter value.",
        "parameter-missing-value",
        Map.ofEntries(),
        Optional.empty(),
        List.of()
      );
    }

    if (uriOpt.isPresent()) {
      return new RuntimeParameters(
        uriOpt.get(),
        new MHash(
          new MHashAlgorithm("SHA-256"),
          new MHashValue(
            newContext.parameterValueRequireNow(JAVA_DOWNLOAD_SHA256)
          )
        ),
        newContext.parameterValueRequireNow(JAVA_DOWNLOAD_FORMAT)
      );
    }

    return this.handleAdoptium(
      newContext.parameterValueRequireNow(ADOPTIUM_TEMURIN_VERSION)
    );
  }

  private RuntimeParameters handleAdoptium(
    final Runtime.Version requiredVersion)
    throws MException
  {
    final var adoptiums = new MEAdoptiumFactory();
    try (final var adoptium =
           adoptiums.createAdoptium(
             MEAdoptiumConfiguration.builder()
               .build())) {

      final var runtimes =
        adoptium.runtimes(
          MEARuntimeSearch.builder()
            .setImageKind(METImageKind.JRE)
            .setOperatingSystem(os())
            .setArchitecture(arch())
            .setFeatureVersion(requiredVersion.feature())
            .build()
        );

      for (final var runtime : runtimes) {
        if (Objects.equals(runtime.version(), requiredVersion)) {
          return new RuntimeParameters(
            runtime.downloadLink(),
            runtime.hash(),
            runtime.format()
          );
        }
      }

      throw new MException(
        "No Adoptium Temurin runtime is available for the given version.",
        "error-adoptium-temurin-unavailable",
        Map.ofEntries(
          Map.entry("Version", requiredVersion.toString())
        )
      );
    }
  }

  private static MArchitectureName arch()
  {
    return MArchitectureName.infer(System.getProperty("os.arch"));
  }

  private static MOperatingSystemName os()
  {
    return MOperatingSystemName.infer(System.getProperty("os.name"));
  }

  private Collection<MNativePackagerServiceType> getPackagers(
    final QCommandContextType context,
    final MNativePackagerDirectoryType packagers)
  {
    final var includes =
      Set.copyOf(context.parameterValues(INCLUDE_PACKAGERS));
    final var unfiltered =
      packagers.packagers().values();

    if (includes.isEmpty()) {
      return unfiltered;
    }

    return unfiltered.stream()
      .filter(p -> includes.contains(p.name().value()))
      .toList();
  }

  private void onJavaDownloadProgress(
    final STTransferStatistics status)
  {
    final var sizeExpected =
      FileUtils.byteCountToDisplaySize(status.sizeExpected().orElse(0L));
    final var sizeTransferred =
      FileUtils.byteCountToDisplaySize(status.sizeTransferred());
    final var rate =
      FileUtils.byteCountToDisplaySize(status.octetsPerSecond());
    final var remaining =
      Duration.ofSeconds(status.expectedSecondsRemaining().orElse(0L));

    LOG.info(
      "[JDK] {}/{} ({}/s) ~{} remaining",
      sizeTransferred,
      sizeExpected,
      rate,
      remaining
    );
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
