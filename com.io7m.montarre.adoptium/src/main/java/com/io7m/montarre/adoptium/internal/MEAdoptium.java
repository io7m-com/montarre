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


package com.io7m.montarre.adoptium.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.io7m.montarre.adoptium.MEARuntime;
import com.io7m.montarre.adoptium.MEARuntimeSearch;
import com.io7m.montarre.adoptium.MEAdoptiumConfiguration;
import com.io7m.montarre.adoptium.MEAdoptiumType;
import com.io7m.montarre.adoptium.METArchitectureName;
import com.io7m.montarre.adoptium.METOperatingSystemName;
import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MArchiveFormat;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MOperatingSystemName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The Adoptium client.
 */

public final class MEAdoptium
  implements MEAdoptiumType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MEAdoptium.class);

  private final MEAdoptiumConfiguration configuration;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  /**
   * The Adoptium client.
   *
   * @param inConfiguration The configuration
   * @param inHttpClient    The HTTP client
   */

  public MEAdoptium(
    final MEAdoptiumConfiguration inConfiguration,
    final HttpClient inHttpClient)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.httpClient =
      Objects.requireNonNull(inHttpClient, "httpClient");
    this.objectMapper =
      new ObjectMapper();
  }

  @Override
  public List<MEARuntime> runtimes(
    final MEARuntimeSearch search)
    throws MException
  {
    Objects.requireNonNull(search, "search");

    final var baseText =
      this.configuration.baseURI().toString();

    final var target = new StringBuilder(baseText);
    if (!baseText.endsWith("/")) {
      target.append("/");
    }

    target.append("assets/feature_releases/");
    target.append(search.featureVersion());
    target.append("/");
    target.append("ga");
    target.append("?");

    target.append("architecture=");
    target.append(
      METArchitectureName.of(search.architecture()).adoptiumName());

    target.append("&");
    target.append("os=");
    target.append(
      METOperatingSystemName.of(search.operatingSystem()).adoptiumName());

    target.append("&");
    target.append("image_type=");
    target.append(search.imageKind().adoptiumName());

    LOG.debug("GET {}", target);

    final var request =
      HttpRequest.newBuilder(URI.create(target.toString()))
        .build();

    try {
      final var response =
        this.httpClient.send(
          request,
          HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() >= 400) {
        throw new MException(
          "Server response indicated an error.",
          "error-http-response",
          Map.ofEntries(
            Map.entry("StatusCode", Integer.toString(response.statusCode()))
          )
        );
      }

      final var releases =
        this.objectMapper.readValue(
          response.body(),
          new TypeReference<List<MERelease>>()
          {
          }
        );

      return releases.stream()
        .flatMap(this::toRuntimes)
        .toList();
    } catch (final Exception e) {
      throw new MException(
        Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()),
        e,
        "error-io"
      );
    }
  }

  private Stream<MEARuntime> toRuntimes(
    final MERelease r)
  {
    return r.binaries()
      .stream()
      .flatMap(b -> this.toRuntime(r, b));
  }

  private Stream<MEARuntime> toRuntime(
    final MERelease r,
    final MEBinary b)
  {
    try {
      final var hash = new MHash(
        new MHashAlgorithm("SHA-256"),
        new MHashValue(b.packageV().checksum())
      );

      final var architecture =
        architectureOf(b.architecture());
      final var downloadLink =
        URI.create(b.packageV().link());
      final var operatingSystem =
        operatingSystemOf(b.os());
      final var format =
        formatOf(b.packageV().link());
      final var version =
        versionOf(r.versionData());
      final var size =
        b.packageV().size();

      return Stream.of(
        MEARuntime.builder()
          .setArchitecture(architecture)
          .setDownloadLink(downloadLink)
          .setOperatingSystem(operatingSystem)
          .setHash(hash)
          .setFormat(format)
          .setVersion(version)
          .setSize(size)
          .build()
      );
    } catch (final Exception e) {
      LOG.debug("Unable to parse runtime: ", e);
      return Stream.of();
    }
  }

  private static Runtime.Version versionOf(
    final MEVersion version)
  {
    return Runtime.Version.parse(version.openjdkVersion());
  }

  private static MArchiveFormat formatOf(
    final String link)
  {
    if (link.endsWith(".tar.gz")) {
      return MArchiveFormat.TAR_GZ;
    }
    if (link.endsWith(".tgz")) {
      return MArchiveFormat.TAR_GZ;
    }
    return MArchiveFormat.ZIP;
  }

  private static MOperatingSystemName operatingSystemOf(
    final String os)
  {
    return switch (METOperatingSystemName.valueOf(os.toUpperCase(Locale.ROOT))) {
      case LINUX -> MOperatingSystemName.linux();
      case WINDOWS -> MOperatingSystemName.windows();
      case MAC -> MOperatingSystemName.osx();
      case SOLARIS -> MOperatingSystemName.solaris();
      case AIX -> MOperatingSystemName.aix();
    };
  }

  private static MArchitectureName architectureOf(
    final String architecture)
  {
    return switch (METArchitectureName.valueOf(architecture.toUpperCase(Locale.ROOT))) {
      case X64 -> MArchitectureName.x86_64();
      case X32 -> MArchitectureName.x86_32();
      case PPC64 -> MArchitectureName.ppc_64();
      case PPC64LE -> MArchitectureName.ppcle_32();
      case S390X -> MArchitectureName.s390_64();
      case AARCH64 -> MArchitectureName.aarch_64();
      case ARM -> MArchitectureName.arm_32();
      case SPARCV9 -> MArchitectureName.sparc_64();
      case RISCV64 -> MArchitectureName.riscv_64();
    };
  }

  @Override
  public void close()
    throws MException
  {

  }
}
