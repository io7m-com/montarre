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


package com.io7m.montarre.tests;

import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.MApplicationKind;
import com.io7m.montarre.api.MArchiveFormat;
import com.io7m.montarre.api.MCopying;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MJavaInfo;
import com.io7m.montarre.api.MLink;
import com.io7m.montarre.api.MLinkRole;
import com.io7m.montarre.api.MManifest;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MNames;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPackageName;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVendorID;
import com.io7m.montarre.api.MVendorName;
import com.io7m.montarre.api.MVersion;
import com.io7m.montarre.api.http.MHTTPClients;
import com.io7m.montarre.api.natives.MNativeWorkspaceConfiguration;
import com.io7m.montarre.io.MPackageReaders;
import com.io7m.montarre.io.MPackageWriters;
import com.io7m.montarre.nativepack.MNWorkspaces;
import com.io7m.montarre.xml.MPackageDeclarationSerializers;
import com.io7m.quixote.core.QWebServerType;
import com.io7m.quixote.core.QWebServers;
import com.io7m.streamtime.core.STTransferStatistics;
import com.io7m.verona.core.Version;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 10L, unit = TimeUnit.SECONDS)
public final class MNWorkspacesTest implements Flow.Subscriber<STTransferStatistics>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNWorkspacesTest.class);

  private static final MPackageDeclaration EMPTY_PACKAGE =
    MPackageDeclaration.builder()
      .setMetadata(
        MMetadata.builder()
          .setCopying(
            MCopying.builder()
              .setCopyright("Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com")
              .setLicense("ISC")
              .build()
          )
          .setDescription("An example package.")
          .setJavaInfo(
            MJavaInfo.builder()
              .setMainModule("com.io7m.example/com.io7m.example.Main")
              .setRequiredJDKVersion(21)
              .build()
          )
          .setApplicationKind(MApplicationKind.CONSOLE)
          .setNames(
            MNames.builder()
              .setPackageName(new MPackageName(new RDottedName("com.io7m.example")))
              .setShortName(new MShortName("example"))
              .build()
          )
          .addLinks(new MLink(MLinkRole.HOME_PAGE, URI.create("https://www.example.com")))
          .setVendor(new MVendor(
            new MVendorID(new RDottedName("com.io7m")),
            new MVendorName("io7m")
          ))
          .setVersion(
            new MVersion(
              Version.of(1, 0, 0),
              LocalDate.parse("2024-10-06")
            )
          )
          .build())
      .setManifest(
        MManifest.builder()
          .build())
      .build();

  private MNWorkspaces workspaces;
  private MPackageWriters writers;
  private MPackageReaders readers;
  private MPackageDeclarationSerializers serializers;
  private Path packageFile;
  private QWebServers servers;
  private QWebServerType server;
  private MHTTPClients httpClients;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
    throws MException, IOException
  {
    this.servers =
      new QWebServers();
    this.server =
      this.servers.create(10000);

    this.httpClients =
      new MHTTPClients();
    this.writers =
      new MPackageWriters();
    this.readers =
      new MPackageReaders();
    this.serializers =
      new MPackageDeclarationSerializers();
    this.workspaces =
      new MNWorkspaces();

    this.packageFile =
      directory.resolve("out.mpk");
    final var outFileTmp =
      directory.resolve("out.mpk.tmp");

    try (var ignored =
           this.writers.create(this.packageFile, outFileTmp, EMPTY_PACKAGE)) {
      // Nothing
    }
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    this.server.close();
  }

  @Test
  public void testDownloadJDKZip(
    final @TempDir Path directory)
    throws Exception
  {
    this.server.addResponse()
      .forPath("/jdk")
      .withData(resource("jdk.zip"))
      .withStatus(200);

    final var config =
      MNativeWorkspaceConfiguration.builder()
        .setBaseDirectory(directory)
        .setJavaRuntimeDownloadFormat(MArchiveFormat.ZIP)
        .setJavaRuntimeDownloadSHA256("5d28338250e89d0062aba5f4f117eec779f3547ca5deef158950707887caf930")
        .setJavaRuntimeDownloadURI(this.server.uri().resolve("jdk"))
        .build();

    try (var reader = this.readers.open(this.packageFile)) {
      try (var workspace = this.workspaces.open(config, httpClients, reader)) {
        workspace.javaRuntimeDownload().subscribe(this);

        {
          final var path =
            workspace.javaRuntime().get(5L, TimeUnit.SECONDS);
          assertTrue(Files.isDirectory(path));
          assertTrue(Files.isRegularFile(
            path.resolve("jdk")
              .resolve("bin")
              .resolve("java")));
        }

        {
          final var path =
            workspace.javaRuntime().get(5L, TimeUnit.SECONDS);
          assertTrue(Files.isDirectory(path));
          assertTrue(Files.isRegularFile(
            path.resolve("jdk")
              .resolve("bin")
              .resolve("java")));
        }
      }
    }
  }

  @Test
  public void testDownloadJDKTarGz(
    final @TempDir Path directory)
    throws Exception
  {
    this.server.addResponse()
      .forPath("/jdk")
      .withData(resource("jdk.tar.gz"))
      .withStatus(200);

    final var config =
      MNativeWorkspaceConfiguration.builder()
        .setBaseDirectory(directory)
        .setJavaRuntimeDownloadFormat(MArchiveFormat.TAR_GZ)
        .setJavaRuntimeDownloadSHA256("cff89c26e1af9a5aeb0051f0dbcdef0902362940849d14de3f7d6f94fdbdc0e2")
        .setJavaRuntimeDownloadURI(this.server.uri().resolve("jdk"))
        .build();

    try (var reader = this.readers.open(this.packageFile)) {
      try (var workspace = this.workspaces.open(config, httpClients, reader)) {
        workspace.javaRuntimeDownload().subscribe(this);

        {
          final var path =
            workspace.javaRuntime().get(5L, TimeUnit.SECONDS);
          assertTrue(Files.isDirectory(path));
          assertTrue(Files.isRegularFile(
            path.resolve("jdk")
              .resolve("bin")
              .resolve("java")));
        }

        {
          final var path =
            workspace.javaRuntime().get(5L, TimeUnit.SECONDS);
          assertTrue(Files.isDirectory(path));
          assertTrue(Files.isRegularFile(
            path.resolve("jdk")
              .resolve("bin")
              .resolve("java")));
        }
      }
    }
  }

  @RepeatedTest(value = 10, failureThreshold = 1)
  public void testDownloadJDK404(
    final @TempDir Path directory)
    throws Exception
  {
    this.server.addResponse()
      .forPath("/jdk")
      .withData(resource("jdk.zip"))
      .withStatus(404);

    final var config =
      MNativeWorkspaceConfiguration.builder()
        .setBaseDirectory(directory)
        .setJavaRuntimeDownloadFormat(MArchiveFormat.ZIP)
        .setJavaRuntimeDownloadSHA256("5d28338250e89d0062aba5f4f117eec779f3547ca5deef158950707887caf930")
        .setJavaRuntimeDownloadURI(this.server.uri().resolve("jdk"))
        .build();

    try (var reader = this.readers.open(this.packageFile)) {
      try (var workspace = this.workspaces.open(config, httpClients, reader)) {
        workspace.javaRuntimeDownload().subscribe(this);

        final var ex = assertThrows(MException.class, () -> {
          try {
            workspace.javaRuntime().get(5L, TimeUnit.SECONDS);
          } catch (final ExecutionException e) {
            throw e.getCause();
          }
        });
        assertEquals("error-http", ex.errorCode());
      }
    }
  }

  @Test
  public void testDownloadJDKWrongHash(
    final @TempDir Path directory)
    throws Exception
  {
    this.server.addResponse()
      .forPath("/jdk")
      .withData(resource("jdk.notZip"))
      .withStatus(200);

    final var config =
      MNativeWorkspaceConfiguration.builder()
        .setBaseDirectory(directory)
        .setJavaRuntimeDownloadFormat(MArchiveFormat.ZIP)
        .setJavaRuntimeDownloadSHA256("5d28338250e89d0062aba5f4f117eec779f3547ca5deef158950707887caf930")
        .setJavaRuntimeDownloadURI(this.server.uri().resolve("jdk"))
        .build();

    try (var reader = this.readers.open(this.packageFile)) {
      try (var workspace = this.workspaces.open(config, httpClients, reader)) {
        workspace.javaRuntimeDownload().subscribe(this);

        final var ex = assertThrows(MException.class, () -> {
          try {
            workspace.javaRuntime().get(5L, TimeUnit.SECONDS);
          } catch (final ExecutionException e) {
            throw e.getCause();
          }
        });
        assertEquals("error-hash-mismatch", ex.errorCode());
      }
    }
  }

  @Test
  public void testCreateWorkingDirectory(
    final @TempDir Path directory)
    throws Exception
  {
    this.server.addResponse()
      .forPath("/jdk")
      .withData(resource("jdk.notZip"))
      .withStatus(200);

    final var config =
      MNativeWorkspaceConfiguration.builder()
        .setBaseDirectory(directory)
        .setJavaRuntimeDownloadFormat(MArchiveFormat.ZIP)
        .setJavaRuntimeDownloadSHA256("5d28338250e89d0062aba5f4f117eec779f3547ca5deef158950707887caf930")
        .setJavaRuntimeDownloadURI(this.server.uri().resolve("jdk"))
        .build();

    try (var reader = this.readers.open(this.packageFile)) {
      try (var workspace = this.workspaces.open(config, httpClients, reader)) {
        assertTrue(Files.isDirectory(workspace.createWorkDirectory()));
      }
    }
  }

  private static InputStream resource(
    final String name)
  {
    final var path =
      "/com/io7m/montarre/tests/%s".formatted(name);

    return MNWorkspacesTest.class.getResourceAsStream(path);
  }

  @Override
  public void onSubscribe(
    final Flow.Subscription subscription)
  {
    subscription.request(Long.MAX_VALUE);
  }

  @Override
  public void onNext(
    final STTransferStatistics item)
  {
    LOG.debug("{}", item);
  }

  @Override
  public void onError(
    final Throwable throwable)
  {

  }

  @Override
  public void onComplete()
  {

  }
}
