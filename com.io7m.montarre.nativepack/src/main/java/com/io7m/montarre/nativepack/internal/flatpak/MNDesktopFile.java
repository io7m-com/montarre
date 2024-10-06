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

import com.io7m.montarre.api.MCategoryName;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MResource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Functions to generate desktop files.
 *
 * @see "https://standards.freedesktop.org/desktop-entry-spec/latest/"
 */

public final class MNDesktopFile
{
  private MNDesktopFile()
  {

  }

  /**
   * Generate the lines of a desktop file.
   *
   * @param packageV The package
   *
   * @return The lines of the file
   */

  public static List<String> createDesktopFile(
    final MPackageDeclaration packageV)
  {
    final var anyIcons =
      packageV.manifest()
        .items()
        .stream()
        .filter(i -> i instanceof MResource)
        .map(MResource.class::cast)
        .anyMatch(i -> i.role().isIcon());

    final var lines =
      new ArrayList<String>();
    final var metadata =
      packageV.metadata();

    lines.add("[Desktop Entry]");
    lines.add("Type=Application");
    lines.add("Name=%s".formatted(metadata.names().humanName()));
    lines.add("Exec=/app/bin/%s".formatted(metadata.names().shortName()));

    if (anyIcons) {
      lines.add("Icon=%s".formatted(metadata.names().packageName()));
    }

    lines.add("Categories=%s".formatted(
      metadata.categories()
        .stream()
        .sorted()
        .map(MCategoryName::name)
        .collect(Collectors.joining(";"))
    ));
    return List.copyOf(lines);
  }

  /**
   * Generate the text of a desktop file.
   *
   * @param packageV The package
   *
   * @return The text of the file
   */

  public static String createDesktopFileText(
    final MPackageDeclaration packageV)
  {
    return String.join("\n", createDesktopFile(packageV));
  }
}
