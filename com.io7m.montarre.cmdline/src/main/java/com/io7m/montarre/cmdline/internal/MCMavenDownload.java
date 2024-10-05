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

import com.io7m.jdownload.core.JDownloadErrorChecksumMismatch;
import com.io7m.jdownload.core.JDownloadErrorHTTP;
import com.io7m.jdownload.core.JDownloadErrorIO;
import com.io7m.jdownload.core.JDownloadErrorType;
import com.io7m.jdownload.core.JDownloadRequests;
import com.io7m.jdownload.core.JDownloadSucceeded;
import com.io7m.montarre.api.MException;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType;
import com.io7m.quarrel.ext.logback.QLogback;
import com.io7m.streamtime.core.STTransferStatistics;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * "download"
 */

public final class MCMavenDownload implements QCommandType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MCPackageCheck.class);

  private static final QParameterNamed1<URI> BASE_URI =
    new QParameterNamed1<>(
      "--base-uri",
      List.of(),
      new QStringType.QConstant("The base repository URI."),
      Optional.of(
        URI.create("https://repo1.maven.org/maven2")
      ),
      URI.class
    );

  private static final QParameterNamed1<Path> OUTPUT_FILE =
    new QParameterNamed1<>(
      "--output-file",
      List.of(),
      new QStringType.QConstant("The output file."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<String> GROUP_NAME =
    new QParameterNamed1<>(
      "--group",
      List.of(),
      new QStringType.QConstant("The group name."),
      Optional.empty(),
      String.class
    );

  private static final QParameterNamed1<String> ARTIFACT_NAME =
    new QParameterNamed1<>(
      "--artifact",
      List.of(),
      new QStringType.QConstant("The artifact name."),
      Optional.empty(),
      String.class
    );

  private static final QParameterNamed01<String> CLASSIFIER =
    new QParameterNamed01<>(
      "--classifier",
      List.of(),
      new QStringType.QConstant("The classifier name."),
      Optional.empty(),
      String.class
    );

  private static final QParameterNamed1<String> VERSION =
    new QParameterNamed1<>(
      "--version",
      List.of(),
      new QStringType.QConstant("The version."),
      Optional.empty(),
      String.class
    );

  private static final QParameterNamed1<String> TYPE =
    new QParameterNamed1<>(
      "--type",
      List.of(),
      new QStringType.QConstant("The artifact type."),
      Optional.of("jar"),
      String.class
    );

  private final QCommandMetadata metadata;

  /**
   * Construct a command.
   */

  public MCMavenDownload()
  {
    this.metadata = new QCommandMetadata(
      "download",
      new QStringType.QConstant("Download a package from Maven Central"),
      Optional.empty()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(
        ARTIFACT_NAME,
        BASE_URI,
        CLASSIFIER,
        GROUP_NAME,
        OUTPUT_FILE,
        TYPE,
        VERSION
      ),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType newContext)
    throws InterruptedException
  {
    QLogback.configure(newContext);

    final var baseURI =
      newContext.parameterValue(BASE_URI);
    final var group =
      newContext.parameterValue(GROUP_NAME);
    final var artifact =
      newContext.parameterValue(ARTIFACT_NAME);
    final var classifier =
      newContext.parameterValue(CLASSIFIER);
    final var type =
      newContext.parameterValue(TYPE);
    final var version =
      newContext.parameterValue(VERSION);
    final var outputFile =
      newContext.parameterValue(OUTPUT_FILE);
    final var checksumFile =
      Paths.get(outputFile + ".sha1");

    final var groupParts =
      group.replace('.', '/');

    final var builder = new StringBuilder();
    builder.append(baseURI);
    if (!baseURI.toString().endsWith("/")) {
      builder.append('/');
    }
    builder.append(groupParts);
    builder.append('/');
    builder.append(artifact);
    builder.append('/');
    builder.append(version);
    builder.append('/');
    builder.append(artifact);
    builder.append('-');
    builder.append(version);

    if (classifier.isPresent()) {
      builder.append('-');
      builder.append(classifier.get());
    }

    builder.append('.');
    builder.append(type);

    final var targetURI = URI.create(builder.toString());
    builder.append(".sha1");
    final var checksumURI = URI.create(builder.toString());

    LOG.info("Downloading: {}", targetURI);
    LOG.info("Checksum: {}", checksumURI);

    try (var httpClient = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build()) {

      final var request =
        JDownloadRequests.builder(httpClient, targetURI, outputFile)
          .setChecksumFromURL(
            checksumURI,
            "SHA-1",
            checksumFile,
            this::onDownloadProgress
          )
          .setTransferStatisticsReceiver(this::onDownloadProgress)
          .build();

      switch (request.execute()) {
        case JDownloadErrorType error -> {
          throw this.downloadError(error);
        }
        case JDownloadSucceeded ignored -> {
          // Nothing
        }
      }
    } catch (final MException e) {
      MCSLogging.logStructuredError(LOG, e);
      return QCommandStatus.FAILURE;
    }

    return QCommandStatus.SUCCESS;
  }

  private MException downloadError(
    final JDownloadErrorType error)
  {
    return switch (error) {
      case JDownloadErrorChecksumMismatch mismatch -> {
        yield new MException(
          "Hash mismatch.",
          "error-hash-mismatch",
          Map.ofEntries(
            Map.entry("Hash (Expected)", mismatch.hashExpected()),
            Map.entry("Hash (Received)", mismatch.hashReceived()),
            Map.entry("Hash Algorithm", mismatch.algorithm()),
            Map.entry("URI", mismatch.uri().toString()),
            Map.entry("Output File", mismatch.outputFile().toString())
          )
        );
      }
      case JDownloadErrorHTTP http -> {
        yield new MException(
          "HTTP error.",
          "error-http",
          Map.ofEntries(
            Map.entry("HTTP Status", Integer.toUnsignedString(http.status())),
            Map.entry("URI", http.uri().toString()),
            Map.entry("Output File", http.outputFile().toString())
          )
        );
      }
      case JDownloadErrorIO io -> {
        yield new MException(
          "HTTP I/O error.",
          io.exception(),
          "error-http-io",
          Map.ofEntries(
            Map.entry("URI", io.uri().toString()),
            Map.entry("Output File", io.outputFile().toString())
          )
        );
      }
    };
  }

  private void onDownloadProgress(
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
      "Download: {}/{} ({}/s) ~{} remaining",
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
