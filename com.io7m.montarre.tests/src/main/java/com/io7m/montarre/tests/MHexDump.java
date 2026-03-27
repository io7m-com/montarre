/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.lang3.CharUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MHexDump
{
  private MHexDump()
  {

  }

  public static void dumpTo(
    final Path file,
    final OutputStream out)
    throws IOException
  {
    final var writer =
      new BufferedWriter(
        new OutputStreamWriter(
          CloseShieldOutputStream.wrap(out),
          StandardCharsets.UTF_8
        )
      );

    try (var stream = Files.newInputStream(file)) {
      for (final var line : dump(stream)) {
        writer.append(line);
        writer.newLine();
      }
      writer.flush();
    }
  }

  public static List<String> dump(
    final Path file)
    throws IOException
  {
    try (var stream = Files.newInputStream(file)) {
      return dump(stream);
    }
  }

  public static List<String> dump(
    final InputStream stream)
    throws IOException
  {
    final var output = new ArrayList<String>();

    var offset = 0L;
    while (true) {
      final var b = stream.readNBytes(16);
      if (b.length == 0) {
        break;
      }
      output.add(format(offset, b));
      offset += b.length;
    }
    return List.copyOf(output);
  }

  private static String format(
    final long offset,
    final byte[] data)
  {
    final var hexBuffer =
      new StringBuilder();
    final var charBuffer =
      new StringBuilder();

    for (int index = 0; index < 16; ++index) {
      if (index < data.length) {
        hexBuffer.append("%02x".formatted(data[index]));
      } else {
        hexBuffer.append("  ");
      }
      hexBuffer.append(' ');
    }

    for (int index = 0; index < 16; ++index) {
      if (index < data.length) {
        final var ch = data[index];
        if (CharUtils.isAsciiPrintable((char) ch)) {
          charBuffer.append("%c".formatted(ch));
        } else {
          charBuffer.append('.');
        }
      } else {
        charBuffer.append(".");
      }
    }

    final var offsetText = Long.toUnsignedString(offset, 16);
    final var lineBuffer = new StringBuilder();
    lineBuffer.append("| ");
    lineBuffer.append("0x%-8s".formatted(offsetText));
    lineBuffer.append("| ");
    lineBuffer.append(hexBuffer);
    lineBuffer.append("| ");
    lineBuffer.append(charBuffer);
    lineBuffer.append(" |");
    return lineBuffer.toString();
  }
}
