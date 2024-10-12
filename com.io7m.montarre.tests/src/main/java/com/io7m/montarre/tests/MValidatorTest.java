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
import com.io7m.montarre.api.MDescription;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MFeature;
import com.io7m.montarre.api.MLanguageCode;
import com.io7m.montarre.api.MLongDescription;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MNames;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPackageName;
import com.io7m.montarre.api.MParagraph;
import com.io7m.montarre.api.MTranslatedTexts;
import com.io7m.montarre.api.validation.MValidationIssue;
import com.io7m.montarre.api.validation.MValidatorType;
import com.io7m.montarre.io.MValidators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.io7m.montarre.tests.MExamplePackages.EMPTY_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MValidatorTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MValidatorTest.class);

  private MValidators validators;
  private MValidatorType validator;

  @BeforeEach
  public void setup()
  {
    this.validators =
      new MValidators();
  }

  @AfterEach
  public void tearDown()
    throws MException
  {
    this.validator.close();
  }

  @Test
  public void testNameTooLong0()
  {
    this.validator =
      this.validators.create(withHumanName("LongHumanNameXYZ"));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.name."))
        .toList();

    assertEquals("flatpak.name.length_warn", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @Test
  public void testNameTooLong1()
  {
    this.validator =
      this.validators.create(withHumanName("TooTooTooTooLongHumanNameXYZ"));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.name."))
        .toList();

    assertEquals("flatpak.name.length_warn", e.get(0).errorCode());
    assertEquals("flatpak.name.length_require", e.get(1).errorCode());
    assertEquals(2, e.size());
  }

  @Test
  public void testNameWeird()
  {
    this.validator =
      this.validators.create(withHumanName("Firefox.org"));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.name."))
        .toList();

    assertEquals("flatpak.name.format", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @Test
  public void testSummaryTooLong0()
  {
    this.validator =
      this.validators.create(withSummary(
        Map.entry(new MLanguageCode("en"), "This is a very long summary"),
        Map.entry(new MLanguageCode("fr"), "Il s'agit d'un long résumé")
      ));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.summary."))
        .toList();

    assertEquals("flatpak.summary.length_warn", e.get(0).errorCode());
    assertEquals("flatpak.summary.length_warn", e.get(1).errorCode());
    assertEquals(2, e.size());
  }

  @Test
  public void testSummaryTooLong1()
  {
    this.validator =
      this.validators.create(withSummary(
        Map.entry(
          new MLanguageCode("en"),
          "This is a very very very long summary"),
        Map.entry(
          new MLanguageCode("fr"),
          "C'est un résumé très, très, très long")
      ));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.summary."))
        .toList();

    assertEquals("flatpak.summary.length_warn", e.get(0).errorCode());
    assertEquals("flatpak.summary.length_require", e.get(1).errorCode());
    assertEquals("flatpak.summary.length_warn", e.get(2).errorCode());
    assertEquals("flatpak.summary.length_require", e.get(3).errorCode());
    assertEquals(4, e.size());
  }

  @Test
  public void testSummaryName()
  {
    this.validator =
      this.validators.create(withSummary(
        Map.entry(new MLanguageCode("en"), "This is a Example summary"),
        Map.entry(new MLanguageCode("fr"), "C'est un résumé Example")
      ));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.summary."))
        .toList();

    assertEquals("flatpak.summary.no_repeat_name", e.get(0).errorCode());
    assertEquals("flatpak.summary.no_repeat_name", e.get(1).errorCode());
    assertEquals(2, e.size());
  }

  @Test
  public void testSummaryDot()
  {
    this.validator =
      this.validators.create(withSummary(
        Map.entry(new MLanguageCode("en"), "This is a summary."),
        Map.entry(new MLanguageCode("fr"), "C'est un résumé.")
      ));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.summary."))
        .toList();

    assertEquals("flatpak.summary.end_dot", e.get(0).errorCode());
    assertEquals("flatpak.summary.end_dot", e.get(1).errorCode());
    assertEquals(2, e.size());
  }

  @Test
  public void testDescriptionFeatures()
  {
    this.validator =
      this.validators.create(withLongDescription(
        List.of(),
        List.of(
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A"),
          new MFeature("A")
        )
      ));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.description."))
        .toList();

    assertEquals("flatpak.description.features_too_long", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @Test
  public void testAppIDComponentCount()
  {
    this.validator =
      this.validators.create(withPackageName("a.x"));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.app_id."))
        .toList();

    assertEquals("flatpak.app_id.components", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @Test
  public void testAppIDTooLong()
  {
    final var name =
      "c".repeat(60) + "com." +
      "c".repeat(60) + "com." +
      "c".repeat(60) + "com." +
      "c".repeat(60) + "com." +
      "c".repeat(60) + "com";

    this.validator =
      this.validators.create(withPackageName(name));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.app_id."))
        .toList();

    assertEquals("flatpak.app_id.length", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @Test
  public void testAppIDEndsGenericApp()
  {
    this.validator =
      this.validators.create(withPackageName("a.x.app"));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.app_id."))
        .toList();

    assertEquals("flatpak.app_id.ends_generic", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @Test
  public void testAppIDEndsGenericDesktop()
  {
    this.validator =
      this.validators.create(withPackageName("a.x.desktop"));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.app_id."))
        .toList();

    assertEquals("flatpak.app_id.ends_generic", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @Test
  public void testAppIDComponentFormat0()
  {
    this.validator =
      this.validators.create(withPackageName("x.x-x.x"));
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.app_id."))
        .toList();

    assertEquals("flatpak.app_id.component_format", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  @TestFactory
  public Stream<DynamicTest> testAppIDBanned()
  {
    return Stream.of(
      "com.github",
      "com.gitlab",
      "codeberg.org",
      "framagit.org"
    ).map(prefix -> {
      return DynamicTest.dynamicTest("testAppIDBanned_" + prefix, () -> {
        this.validator =
          this.validators.create(withPackageName(prefix + ".x"));
        final var rawErrors =
          this.validator.execute();
        this.dumpErrors(rawErrors);

        final var e =
          rawErrors.stream()
            .filter(ei -> ei.errorCode().startsWith("flatpak.app_id."))
            .toList();

        assertEquals("flatpak.app_id.banned_prefix", e.get(0).errorCode());
        assertEquals(1, e.size());
      });
    });
  }

  @TestFactory
  public Stream<DynamicTest> testAppIDLongEnough()
  {
    return Stream.of(
      "io.github",
      "io.gitlab",
      "page.codeberg",
      "io.frama"
    ).map(prefix -> {
      return DynamicTest.dynamicTest("testAppIDLongEnough_" + prefix, () -> {
        this.validator =
          this.validators.create(withPackageName(prefix + ".x"));
        final var rawErrors =
          this.validator.execute();
        this.dumpErrors(rawErrors);

        final var e =
          rawErrors.stream()
            .filter(ei -> ei.errorCode().startsWith("flatpak.app_id."))
            .toList();

        assertEquals(
          "flatpak.app_id.prefix_requires_length",
          e.get(0).errorCode());
        assertEquals(1, e.size());
      });
    });
  }

  @Test
  public void testIconsMissing()
  {
    this.validator =
      this.validators.create(EMPTY_PACKAGE);
    final var rawErrors =
      this.validator.execute();
    this.dumpErrors(rawErrors);

    final var e =
      rawErrors.stream()
        .filter(ei -> ei.errorCode().startsWith("flatpak.icons."))
        .toList();

    assertEquals("flatpak.icons.required", e.get(0).errorCode());
    assertEquals(1, e.size());
  }

  private static MPackageDeclaration withPackageName(
    final String name)
  {
    return EMPTY_PACKAGE.withMetadata(
      MMetadata.builder()
        .from(EMPTY_PACKAGE.metadata())
        .setNames(
          MNames.builder()
            .from(EMPTY_PACKAGE.metadata().names())
            .setPackageName(new MPackageName(new RDottedName(name)))
            .build()
        ).build()
    );
  }

  private MPackageDeclaration withHumanName(
    final String name)
  {
    return EMPTY_PACKAGE.withMetadata(
      MMetadata.builder()
        .from(EMPTY_PACKAGE.metadata())
        .setNames(
          MNames.builder()
            .from(EMPTY_PACKAGE.metadata().names())
            .setHumanName(name)
            .build()
        ).build()
    );
  }

  @SafeVarargs
  private MPackageDeclaration withSummary(
    final Map.Entry<MLanguageCode, String>... entries)
  {
    return EMPTY_PACKAGE.withMetadata(
      MMetadata.builder()
        .from(EMPTY_PACKAGE.metadata())
        .setDescription(new MDescription(
          MTranslatedTexts.ofTranslations(entries)
        ))
        .build()
    );
  }

  private MPackageDeclaration withLongDescription(
    final List<MParagraph> descriptions,
    final List<MFeature> features)
  {
    return EMPTY_PACKAGE.withMetadata(
      MMetadata.builder()
        .from(EMPTY_PACKAGE.metadata())
        .addLongDescriptions(
          MLongDescription.builder()
            .setLanguage(new MLanguageCode("fr"))
            .addAllDescriptions(descriptions)
            .addAllFeatures(features)
            .build()
        )
        .build()
    );
  }


  private void dumpErrors(
    final List<MValidationIssue> errors)
  {
    for (final var error : errors) {
      LOG.debug("{}", error);
    }
  }
}
