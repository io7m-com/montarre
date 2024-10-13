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
import com.io7m.montarre.api.MOperatingSystemName;
import com.io7m.montarre.launchstub.MLSMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MLSMainTest
{
  private PrintStream saved;
  private ByteArrayOutputStream byteOut;
  private PrintStream bytePrint;

  @BeforeEach
  public void setup()
  {
    this.saved = System.out;
    this.byteOut = new ByteArrayOutputStream();
    this.bytePrint = new PrintStream(this.byteOut);
    System.setOut(this.bytePrint);
  }

  @AfterEach
  public void tearDown()
  {
    System.setOut(this.saved);
  }

  @Test
  public void testOS()
  {
    MLSMain.main(new String[] {
      "get-os"
    });

    final var text =
      this.byteOut.toString(StandardCharsets.UTF_8)
        .trim();

    System.err.println(text);

    assertEquals(
      MOperatingSystemName.infer(System.getProperty("os.name")).name(),
      text
    );
  }

  @Test
  public void testArch()
  {
    MLSMain.main(new String[] {
      "get-arch"
    });

    final var text =
      this.byteOut.toString(StandardCharsets.UTF_8)
        .trim();

    System.err.println(text);

    assertEquals(
      MArchitectureName.infer(System.getProperty("os.arch")).name(),
      text
    );
  }

  @Test
  public void testModulePath()
  {
    MLSMain.main(new String[] {
      "get-module-path",
      "EXAMPLE_HOME"
    });

    final var text =
      this.byteOut.toString(StandardCharsets.UTF_8)
        .trim();

    System.err.println(text);
  }

  @Test
  public void testJavaVersion()
  {
    MLSMain.main(new String[] {
      "check-java-version",
      "9"
    });

    final var text =
      this.byteOut.toString(StandardCharsets.UTF_8)
        .trim();

    System.err.println(text);
  }

  @Test
  public void testJavaVersionTooOld()
  {
    assertThrows(RuntimeException.class, () -> {
      MLSMain.main(new String[] {
        "check-java-version",
        "4294967295"
      });
    });

    final var text =
      this.byteOut.toString(StandardCharsets.UTF_8)
        .trim();

    System.err.println(text);
  }
}
