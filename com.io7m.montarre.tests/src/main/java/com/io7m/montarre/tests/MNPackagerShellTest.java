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

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.montarre.api.http.MHTTPClientFactoryType;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeWorkspaceConfiguration;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.montarre.io.MPackageReaders;
import com.io7m.montarre.nativepack.MNWorkspaces;
import com.io7m.montarre.nativepack.MNativeProcesses;
import com.io7m.montarre.nativepack.internal.deb.MNPackagerDebProvider;
import com.io7m.montarre.nativepack.internal.shell.MNPackagerShellProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MNPackagerShellTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNPackagerShellTest.class);

  private Path inputMpk;
  private MNPackagerShellProvider packagers;
  private MNativeProcesses processes;
  private MNativePackagerServiceType packager;
  private MNWorkspaces workspaces;
  private CloseableCollectionType<ClosingResourceFailedException> resources;
  private MPackageReaders readers;
  private MPackageReaderType reader;
  private MNativeWorkspaceType workspace;
  private MHTTPClientFactoryType httpClients;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
    throws Exception
  {
    this.inputMpk =
      directory.resolve("input.mpk");
    this.resource(
      "com.io7m.montarre.distribution-0.0.1-SNAPSHOT.mpk",
      this.inputMpk
    );

    this.resources =
      CloseableCollection.create();

    this.httpClients =
      () -> new MHTTPCachedHTTPClient(
        HttpClient.newBuilder()
          .executor(Executors.newVirtualThreadPerTaskExecutor())
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build()
      );

    this.readers =
      new MPackageReaders();
    this.workspaces =
      new MNWorkspaces();
    this.processes =
      new MNativeProcesses();
    this.packagers =
      new MNPackagerShellProvider();
    this.packager =
      this.packagers.create(this.processes);

    final var configurationBuilder =
      MNativeWorkspaceConfiguration.builder()
        .setBaseDirectory(directory);

    Assumptions.assumeTrue(
      MJavaRuntimes.getRuntimeDefinition().isPresent(),
      "A Java runtime is available on this platform."
    );

    final var runtime =
      MJavaRuntimes.getRuntimeDefinition()
        .orElseThrow();

    configurationBuilder.setJavaRuntimeDownloadFormat(runtime.format());
    configurationBuilder.setJavaRuntimeDownloadSHA256(runtime.sha256());
    configurationBuilder.setJavaRuntimeDownloadURI(runtime.downloadURI());

    final var configuration =
      configurationBuilder.build();

    this.reader =
      this.resources.add(this.readers.open(this.inputMpk));
    this.workspace =
      this.resources.add(
        this.workspaces.open(configuration, this.httpClients, this.reader)
      );
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    this.resources.close();
  }

  @Test
  public void testExecute()
    throws Exception
  {
    Assumptions.assumeTrue(
      this.packager.unsupportedReason(Optional.empty()).isEmpty(),
      "Packager is supported on this platform."
    );

    final var path = this.packager.execute(
      this.workspace,
      this.reader
    );

    LOG.debug("Produced {}", path);
    assertTrue(Files.isRegularFile(path));

    final var name = path.getFileName().toString();
    assertFalse(
      name.contains(this.workspace.architecture().name()),
      "%s must not contain %s".formatted(
        name, this.workspace.architecture().name()
      )
    );
    assertFalse(
      name.contains(this.workspace.operatingSystem().name()),
      "%s must not contain %s".formatted(
        name, this.workspace.operatingSystem().name()
      )
    );
    assertTrue(
      name.contains("0.0.1-SNAPSHOT"),
      "%s must contain %s".formatted(
        name, "0.0.1-SNAPSHOT"
      )
    );
    assertTrue(
      name.contains("com.io7m.montarre"),
      "%s must contain %s".formatted(
        name, "com.io7m.montarre"
      )
    );
    assertTrue(
      name.endsWith("-any.tgz"),
      "%s must end with -any.tgz".formatted(
        name
      )
    );
  }

  private void resource(
    final String resourceName,
    final Path output)
    throws IOException
  {
    final var file =
      "/com/io7m/montarre/tests/" + resourceName;

    try (var stream = MNPackagerShellTest.class.getResourceAsStream(file)) {
      Files.write(output, stream.readAllBytes());
    }
  }
}
