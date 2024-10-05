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

import com.io7m.jdownload.core.JDownloadErrorChecksumMismatch;
import com.io7m.jdownload.core.JDownloadErrorHTTP;
import com.io7m.jdownload.core.JDownloadErrorIO;
import com.io7m.jdownload.core.JDownloadErrorType;
import com.io7m.jdownload.core.JDownloadRequests;
import com.io7m.jdownload.core.JDownloadSucceeded;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.api.http.MHTTPClientFactoryType;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativeWorkspaceConfiguration;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.streamtime.core.STTransferStatistics;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * A workspace.
 */

public final class MNWorkspace implements MNativeWorkspaceType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNWorkspace.class);

  private static final Instant SOURCE_EPOCH =
    Instant.parse("2010-01-01T00:00:00+00:00");
  private static final FileTime SOURCE_EPOCH_FILETIME =
    FileTime.from(SOURCE_EPOCH);
  private static final HexFormat HEX =
    HexFormat.of();

  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };

  private final MNativeWorkspaceConfiguration configuration;
  private final MPackageReaderType packageReader;
  private final CloseableCollectionType<ClosingResourceFailedException> resources;
  private final SubmissionPublisher<STTransferStatistics> javaRuntimeDownload;
  private final ExecutorService executor;
  private final ReentrantLock javaRuntimeDownloadLock;
  private final Path jdkDir;
  private final HttpClient httpClient;
  private final Path jdkArchiveTemp;
  private final Path jdkArchive;
  private final Path jdkOK;
  private final MOperatingSystemName operatingSystem;
  private final MArchitectureName architecture;

  private MNWorkspace(
    final MNativeWorkspaceConfiguration inConfiguration,
    final MPackageReaderType inPackageReader,
    final MHTTPClientFactoryType inHttpClients,
    final MOperatingSystemName inOperatingSystem,
    final MArchitectureName inArchitecture)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.packageReader =
      Objects.requireNonNull(inPackageReader, "packageReader");
    this.operatingSystem =
      Objects.requireNonNull(inOperatingSystem, "operatingSystem");
    this.architecture =
      Objects.requireNonNull(inArchitecture, "architecture");

    this.jdkOK =
      this.configuration.baseDirectory()
        .resolve("jdk-ok");
    this.jdkDir =
      this.configuration.baseDirectory()
        .resolve("jdk");
    this.jdkArchive =
      this.configuration.baseDirectory()
        .resolve("jdk-archive");
    this.jdkArchiveTemp =
      this.configuration.baseDirectory()
        .resolve("jdk-archive.tmp");

    this.resources =
      CloseableCollection.create();
    this.executor =
      this.resources.add(Executors.newVirtualThreadPerTaskExecutor());

    this.httpClient =
      inHttpClients.createHttpClient();

    // Prefer shutdownNow() to close() in order to interrupt operations
    this.resources.add(this.httpClient::shutdownNow);

    this.javaRuntimeDownload =
      this.resources.add(new SubmissionPublisher<>(
        Runnable::run,
        1
      ));
    this.javaRuntimeDownloadLock =
      new ReentrantLock();
  }

  /**
   * Open a workspace.
   *
   * @param configuration The configuration
   * @param httpClients   The HTTP clients
   * @param packageReader The current package
   *
   * @return A workspace
   */

  public static MNativeWorkspaceType open(
    final MNativeWorkspaceConfiguration configuration,
    final MHTTPClientFactoryType httpClients,
    final MPackageReaderType packageReader)
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(httpClients, "httpClients");
    Objects.requireNonNull(packageReader, "packageReader");

    return new MNWorkspace(
      configuration,
      packageReader,
      httpClients,
      MOperatingSystemName.infer(System.getProperty("os.name")),
      MArchitectureName.infer(System.getProperty("os.arch"))
    );
  }

  @Override
  public Flow.Publisher<STTransferStatistics> javaRuntimeDownload()
  {
    return this.javaRuntimeDownload;
  }

  @Override
  public CompletableFuture<Path> javaRuntime()
  {
    final var future = new CompletableFuture<Path>();
    this.executor.execute(() -> {
      try {
        future.complete(this.opJavaRuntimeDownload());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  @Override
  public Path createWorkDirectory()
    throws MException
  {
    final var newDirectory =
      this.configuration.baseDirectory()
        .resolve("work")
        .resolve(UUID.randomUUID().toString());

    try {
      Files.createDirectories(newDirectory);
    } catch (final IOException e) {
      throw new MException(
        "I/O error.",
        e,
        "error-io",
        Map.ofEntries(Map.entry("Directory", newDirectory.toString()))
      );
    }
    return newDirectory;
  }

  private Path opJavaRuntimeDownload()
    throws InterruptedException, MException
  {
    this.javaRuntimeDownloadLock.lock();

    try {
      if (Files.isRegularFile(this.jdkOK)) {
        return this.jdkDir;
      }

      LOG.info(
        "Downloading JDK from {}",
        this.configuration.javaRuntimeDownloadURI()
      );

      final var request =
        JDownloadRequests.builder(
            this.httpClient,
            this.configuration.javaRuntimeDownloadURI(),
            this.jdkArchive,
            this.jdkArchiveTemp
          )
          .setChecksumStatically(
            "SHA-256",
            HEX.parseHex(this.configuration.javaRuntimeDownloadSHA256())
          )
          .setTransferStatisticsReceiver(this.javaRuntimeDownload::submit)
          .build();

      final var result = request.execute();
      return switch (result) {
        case JDownloadErrorType error -> {
          throw this.downloadError(error);
        }
        case JDownloadSucceeded ignored -> {
          yield this.unpackJDK();
        }
      };
    } finally {
      this.javaRuntimeDownloadLock.unlock();
    }
  }

  private Path unpackJDK()
    throws MException
  {
    try {
      switch (this.configuration.javaRuntimeDownloadFormat()) {
        case ZIP -> {
          this.unpackZip(this.jdkArchive, this.jdkDir);
          Files.writeString(this.jdkOK, "OK", OPEN_OPTIONS);
        }
        case TAR_GZ -> {
          this.unpackTarGZ(this.jdkArchive, this.jdkDir, true);
          Files.writeString(this.jdkOK, "OK", OPEN_OPTIONS);
        }
      }
      return this.jdkDir;
    } catch (IOException e) {
      throw new MException(
        "I/O error.",
        e,
        "error-io",
        Map.ofEntries(
          Map.entry("Archive", this.jdkArchive.toString()),
          Map.entry("Output Directory", this.jdkDir.toString())
        )
      );
    }
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

  @Override
  public void close()
    throws MException
  {
    try {
      this.resources.close();
      this.executor.awaitTermination(30L, TimeUnit.SECONDS);
    } catch (final Exception e) {
      throw new MException(
        Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()),
        e,
        "error-resource-close"
      );
    }
  }

  @Override
  public MOperatingSystemName operatingSystem()
  {
    return this.operatingSystem;
  }

  @Override
  public MArchitectureName architecture()
  {
    return this.architecture;
  }

  private void unpackZip(
    final Path source,
    final Path outputDirectory)
    throws IOException
  {
    LOG.info("Unpacking zip %s…".formatted(source));

    final var data =
      Files.readAllBytes(source);
    final var stream =
      new ByteArrayInputStream(data);
    final var zipStream =
      new ZipInputStream(stream);

    while (true) {
      final var entry = zipStream.getNextEntry();
      if (entry == null) {
        break;
      }
      LOG.info("Unpack: %s".formatted(entry.getName()));
      final var outFile = outputDirectory.resolve(entry.getName());
      if (entry.isDirectory()) {
        Files.createDirectories(outFile);
        setFakeTime(outFile);
        continue;
      }

      Files.createDirectories(outFile.getParent());
      setFakeTime(outFile.getParent());

      try (var outStream =
             Files.newOutputStream(outFile, OPEN_OPTIONS)) {
        zipStream.transferTo(outStream);
        outStream.flush();
        setFakeTime(outFile);
      }
    }
  }

  private static void setFakeTime(
    final Path outFile)
    throws IOException
  {
    Files.setLastModifiedTime(
      outFile,
      SOURCE_EPOCH_FILETIME
    );
  }

  private void unpackTarGZ(
    final Path source,
    final Path outputDirectory,
    final boolean stripRoot)
    throws IOException
  {
    LOG.info("Unpacking tar.gz %s…".formatted(source));

    final var data =
      Files.readAllBytes(source);
    final var stream =
      new ByteArrayInputStream(data);
    final var gzip =
      new GZIPInputStream(stream);
    final var tar =
      new TarArchiveInputStream(gzip);

    while (true) {
      final var entry = tar.getNextEntry();
      if (entry == null) {
        break;
      }

      LOG.info("Unpack: %s".formatted(entry.getName()));

      final String usedName;
      if (stripRoot) {
        usedName =
          Stream.of(entry.getName().split("/"))
            .skip(1L)
            .collect(Collectors.joining("/"));
      } else {
        usedName = entry.getName();
      }

      if (Objects.equals(usedName, "")) {
        Files.createDirectories(outputDirectory);
        setFakeTime(outputDirectory);
        continue;
      }

      final var outFile = outputDirectory.resolve(usedName);
      if (entry.isDirectory()) {
        Files.createDirectories(outFile);
        setPermissions(outFile, entry.getMode());
        setFakeTime(outFile);
        continue;
      }

      Files.createDirectories(outFile.getParent());
      try (var outStream =
             Files.newOutputStream(outFile, OPEN_OPTIONS)) {
        tar.transferTo(outStream);
        outStream.flush();
        setPermissions(outFile, entry.getMode());
        setFakeTime(outFile);
      }
    }
  }

  private static void setPermissions(
    final Path outFile,
    final int mode)
    throws IOException
  {
    try {
      Files.setPosixFilePermissions(outFile, modeToPermissions(mode));
    } catch (final UnsupportedOperationException e) {
      // Nothing we can do about this. Non-POSIX filesystem.
    }
  }

  private static Set<PosixFilePermission> modeToPermissions(
    final int mode)
  {
    final var set = new HashSet<PosixFilePermission>();
    set.add(PosixFilePermission.OWNER_WRITE);
    set.add(PosixFilePermission.OWNER_READ);
    set.add(PosixFilePermission.GROUP_READ);

    if (bitIsSet(mode, 0b001_000_000)) {
      set.add(PosixFilePermission.OWNER_EXECUTE);
    }

    return Set.copyOf(set);
  }

  private static boolean bitIsSet(
    final int mode,
    final int x)
  {
    return (mode & x) == x;
  }
}
