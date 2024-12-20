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

import com.io7m.montarre.api.MFlatpakPermission;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MFlatpakPermissionTest
{
  @TestFactory
  public Stream<DynamicTest> testInvalid()
    throws IOException
  {
    return linesOf("flatpak-permissions-invalid.txt")
      .map(MFlatpakPermissionTest::invalidOf);
  }

  @TestFactory
  public Stream<DynamicTest> testValid()
    throws IOException
  {
    return linesOf("flatpak-permissions-valid.txt")
      .map(MFlatpakPermissionTest::validOf);
  }

  private static DynamicTest invalidOf(
    final String text)
  {
    return DynamicTest.dynamicTest(
      "testInvalid_%s".formatted(text),
      () -> {
        assertThrows(IllegalArgumentException.class, () -> {
          new MFlatpakPermission(text);
        });
      });
  }

  private static DynamicTest validOf(
    final String text)
  {
    return DynamicTest.dynamicTest(
      "testValid_%s".formatted(text),
      () -> {
        assertEquals(text, new MFlatpakPermission(text).permission());
      });
  }

  private static Stream<String> linesOf(
    final String name)
    throws IOException
  {
    final var file =
      "/com/io7m/montarre/tests/" + name;

    try (var stream = MFlatpakPermissionTest.class.getResourceAsStream(file)) {
      final String text =
        new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      return text.lines();
    }
  }
}
