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


package com.io7m.montarre.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.io7m.montarre.api.MLinkRole.HOME_PAGE;

/**
 * Package metadata.
 */

@Value.Immutable
@ImmutablesStyleType
public non-sealed interface MMetadataType
  extends MPackageElementType
{
  /**
   * @return The package name information
   */

  MNamesType names();

  /**
   * @return The package version
   */

  MVersion version();

  /**
   * @return Information involving the Java runtime
   */

  MJavaInfoType javaInfo();

  /**
   * @return The package description
   */

  MDescription description();

  /**
   * @return The various package links.
   */

  Set<MLink> links();

  /**
   * @return The vendor
   */

  MVendor vendor();

  /**
   * @return The copyright and license information
   */

  MCopyingType copying();

  /**
   * @return A long description of the application
   */

  @Value.Default
  default List<MLongDescriptionType> longDescriptions()
  {
    final var results = new ArrayList<MLongDescriptionType>();

    results.add(
      MLongDescription.builder()
        .setLanguage(this.description().text().language())
        .addDescriptions(new MParagraph(this.description().text().text()))
        .build()
    );

    this.description()
      .text()
      .translations()
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByKey())
      .forEach(e -> {
        results.add(
          MLongDescription.builder()
            .setLanguage(e.getKey())
            .addDescriptions(new MParagraph(e.getValue()))
            .build()
        );
      });

    return List.copyOf(results);
  }

  /**
   * A map of the long descriptions by language.
   *
   * @return The long descriptions by language
   */

  @Value.Derived
  default SortedMap<MLanguageCode, MLongDescriptionType> longDescriptionsByLanguage()
  {
    final var map = new TreeMap<MLanguageCode, MLongDescriptionType>();
    for (final var description : this.longDescriptions()) {
      final var language = description.language();
      if (map.containsKey(language)) {
        throw new IllegalArgumentException(
          "A long description already exists with language '%s'"
            .formatted(language)
        );
      }
      map.put(language, description);
    }
    return Collections.unmodifiableSortedMap(map);
  }

  /**
   * @return The application categories
   */

  Set<MCategoryName> categories();

  /**
   * @return The kind of the application
   */

  MApplicationKind applicationKind();

  /**
   * @return The flatpak-specific metadata
   */

  @Value.Default
  default MMetadataFlatpakType flatpak()
  {
    return MMetadataFlatpak.builder()
      .build();
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    if (this.links().stream().noneMatch(link -> link.role() == HOME_PAGE)) {
      throw new IllegalArgumentException(
        "A link with the %s role is required.".formatted(HOME_PAGE)
      );
    }

    if (this.longDescriptions().isEmpty()) {
      throw new IllegalArgumentException(
        "No long descriptions were provided."
      );
    }
  }
}
