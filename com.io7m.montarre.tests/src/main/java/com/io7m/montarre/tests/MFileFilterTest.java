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

import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MFileFilter;
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.api.MPlatformFileFilter;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MFileFilterTest
{
  @Test
  public void testPlatformJavaFXExample()
  {
    final var filter =
      MPlatformFileFilter.builder()
        .setArchitecture(new MArchitectureName("x86_64"))
        .setOperatingSystem(new MOperatingSystemName("linux"))
        .addIncludes(Pattern.compile("javafx-.*-linux.jar"))
        .build();

    assertTrue(filter.evaluate("javafx-base-23-linux.jar"));
    assertTrue(filter.evaluate("javafx-graphics-23-linux.jar"));
    assertFalse(filter.evaluate("javafx-base-23-windows.jar"));
  }

  @Property
  public void testPlatformExcludesAll(
    final @ForAll String name)
  {
    Assumptions.assumeTrue(Pattern.compile(".*").matcher(name).matches());

    final var filter =
      MPlatformFileFilter.builder()
        .setArchitecture(new MArchitectureName("x86_64"))
        .setOperatingSystem(new MOperatingSystemName("linux"))
        .addIncludes(Pattern.compile(".*"))
        .addExcludes(Pattern.compile(".*"))
        .build();

    assertFalse(filter.evaluate(name));
  }

  @Property
  public void testPlatformIncludesNothing(
    final @ForAll String name)
  {
    final var filter =
      MPlatformFileFilter.builder()
        .setArchitecture(new MArchitectureName("x86_64"))
        .setOperatingSystem(new MOperatingSystemName("linux"))
        .build();

    assertFalse(filter.evaluate(name));
  }

  @Property
  public void testPlatformIncludesAll(
    final @ForAll String name)
  {
    Assumptions.assumeTrue(Pattern.compile(".*").matcher(name).matches());

    final var filter =
      MPlatformFileFilter.builder()
        .setArchitecture(new MArchitectureName("x86_64"))
        .setOperatingSystem(new MOperatingSystemName("linux"))
        .addIncludes(Pattern.compile(".*"))
        .build();

    assertTrue(filter.evaluate(name));
  }

  @Property
  public void testFileExcludesAll(
    final @ForAll String name)
  {
    Assumptions.assumeTrue(Pattern.compile(".*").matcher(name).matches());

    final var filter =
      MFileFilter.builder()
        .addIncludes(Pattern.compile(".*"))
        .addExcludes(Pattern.compile(".*"))
        .build();

    assertFalse(filter.evaluate(name));
  }

  @Property
  public void testFileIncludesNothing(
    final @ForAll String name)
  {
    final var filter =
      MFileFilter.builder()
        .build();

    assertFalse(filter.evaluate(name));
  }

  @Property
  public void testFileIncludesAll(
    final @ForAll String name)
  {
    Assumptions.assumeTrue(Pattern.compile(".*").matcher(name).matches());

    final var filter =
      MFileFilter.builder()
        .addIncludes(Pattern.compile(".*"))
        .build();

    assertTrue(filter.evaluate(name));
  }
}
