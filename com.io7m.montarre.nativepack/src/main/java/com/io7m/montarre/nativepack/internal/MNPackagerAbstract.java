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
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MResourceRole;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceProviderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

abstract class MNPackagerAbstract
  implements MNativePackagerServiceType
{
  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };

  private final ConcurrentHashMap<String, Object> attributes;
  private final MNativePackagerServiceProviderType provider;

  protected MNPackagerAbstract(
    final MNativePackagerServiceProviderType inProvider)
  {
    this.provider =
      Objects.requireNonNull(inProvider, "provider");
    this.attributes =
      new ConcurrentHashMap<>();
  }

  private static int compareIconResources(
    final MResource o1,
    final MResource o2)
  {
    return Integer.compare(
      o1.role().iconPriority(),
      o2.role().iconPriority()
    );
  }

  protected final OpenOption[] writeReplaceOptions()
  {
    return OPEN_OPTIONS.clone();
  }

  @Override
  public final String describe()
  {
    return this.provider.describe();
  }

  @Override
  public final RDottedName name()
  {
    return this.provider.name();
  }

  protected final Map<String, String> takeAttributes()
  {
    return this.attributes.entrySet()
      .stream()
      .map(e -> Map.entry(e.getKey(), e.getValue().toString()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  protected final void setAttribute(
    final String name,
    final Object value)
  {
    this.attributes.put(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(value, "value")
    );
  }

  protected final MException error(
    final Throwable e)
  {
    return switch (e) {
      case ExecutionException ee -> {
        yield this.error(ee.getCause());
      }
      case MException ee -> {
        yield ee;
      }
      default -> {
        yield new MException(
          Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()),
          e,
          "error-exception",
          this.takeAttributes(),
          Optional.empty()
        );
      }
    };
  }

  protected final Optional<MResource> findLicense(
    final MPackageReaderType reader)
  {
    return reader.packageDeclaration()
      .manifest()
      .items()
      .stream()
      .filter(p -> p instanceof MResource)
      .map(MResource.class::cast)
      .filter(p -> p.role() == MResourceRole.LICENSE)
      .findFirst();
  }

  protected final Optional<MResource> findBestIcon(
    final MNativeWorkspaceType workspace,
    final MPackageReaderType reader)
  {
    if (isWindows(workspace)) {
      return reader.packageDeclaration()
        .manifest()
        .items()
        .stream()
        .filter(p -> p instanceof MResource)
        .map(MResource.class::cast)
        .filter(p -> p.role() == MResourceRole.ICO_WINDOWS)
        .findFirst();
    }

    return reader.packageDeclaration()
      .manifest()
      .items()
      .stream()
      .filter(p -> p instanceof MResource)
      .map(MResource.class::cast)
      .filter(p -> p.role().isIcon())
      .max(MNPackagerAbstract::compareIconResources);
  }

  private static boolean isWindows(
    final MNativeWorkspaceType workspace)
  {
    return Objects.equals(
      workspace.operatingSystem(),
      MOperatingSystemName.windows()
    );
  }

  protected final Optional<Path> unpackIcon(
    final MNativeWorkspaceType workspace,
    final MPackageReaderType packageV,
    final Path directory)
    throws IOException, MException
  {
    final var iconOpt =
      this.findBestIcon(workspace, packageV);

    if (iconOpt.isPresent()) {
      final var icon = iconOpt.get();
      if (Objects.requireNonNull(icon.role()) == MResourceRole.ICO_WINDOWS) {
        final var file = directory.resolve("icon.ico");
        try (var out = Files.newOutputStream(file, OPEN_OPTIONS)) {
          try (var in = packageV.readFile(icon.file())) {
            in.transferTo(out);
            out.flush();
          }
        }
        return Optional.of(file);
      } else {
        final var file = directory.resolve("icon.png");
        try (var out = Files.newOutputStream(file, OPEN_OPTIONS)) {
          try (var in = packageV.readFile(icon.file())) {
            in.transferTo(out);
            out.flush();
          }
        }
        return Optional.of(file);
      }
    }
    return Optional.empty();
  }

  protected final Optional<Path> unpackLicense(
    final MPackageReaderType packageV,
    final Path directory)
    throws IOException, MException
  {
    final var licenseOpt = this.findLicense(packageV);
    if (licenseOpt.isPresent()) {
      final var icon = licenseOpt.get();
      final var file = directory.resolve("LICENSE.TXT");
      try (var out = Files.newOutputStream(file, OPEN_OPTIONS)) {
        try (var in = packageV.readFile(icon.file())) {
          in.transferTo(out);
          out.flush();
        }
      }
      return Optional.of(file);
    }
    return Optional.empty();
  }
}
