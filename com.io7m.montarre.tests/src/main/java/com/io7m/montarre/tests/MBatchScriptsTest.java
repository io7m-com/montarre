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

import com.io7m.montarre.api.MJavaInfo;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.nativepack.internal.shell.MBatchScripts;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MBatchScriptsTest
{
  @Test
  public void testScript()
    throws IOException
  {
    final var info =
      MJavaInfo.builder()
        .setRequiredJDKVersion(21)
        .setMainModule("com.io7m.montarre.cmdline/com.io7m.montarre.cmdline.MMain")
        .build();

    final var receivedLines =
      MBatchScripts.batchScript(info, new MShortName("montarre"));
    final var expectedLines =
      linesOf("expected.bat").toList();

    assertEquals(
      expectedLines.size(),
      receivedLines.size()
    );

    for (int index = 0; index < receivedLines.size(); ++index) {
      assertEquals(
        expectedLines.get(index),
        receivedLines.get(index)
      );
    }
  }

  private static Stream<String> linesOf(
    final String name)
    throws IOException
  {
    final var file =
      "/com/io7m/montarre/tests/" + name;

    try (var stream = MArchitectureNameTest.class.getResourceAsStream(file)) {
      final String text =
        new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      return text.lines();
    }
  }
}
