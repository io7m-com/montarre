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


package com.io7m.montarre.xml;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Functions to indent XML.
 */

public final class MReindent
{
  private MReindent()
  {

  }

  /**
   * Indent the given XML to the given output.
   *
   * @param data   The XML
   * @param output The output stream
   *
   * @throws TransformerException On errors
   * @throws IOException          On errors
   */

  public static void indent(
    final byte[] data,
    final OutputStream output)
    throws TransformerException, IOException
  {
    final var transformer =
      TransformerFactory.newInstance()
        .newTransformer();

    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(
      "{http://xml.apache.org/xslt}indent-amount",
      "2");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    final var indentedOutput =
      new ByteArrayOutputStream();

    indentedOutput.write(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        .trim().getBytes(StandardCharsets.UTF_8));
    indentedOutput.write('\n');

    transformer.transform(
      new StreamSource(new ByteArrayInputStream(data)),
      new StreamResult(indentedOutput)
    );

    output.write(
      indentedOutput.toString(StandardCharsets.UTF_8)
        .replace("\r\n", "\n")
        .getBytes(StandardCharsets.UTF_8)
    );
  }

  /**
   * Indent the given XML.
   *
   * @return The indented text
   * @param data   The XML
   *
   * @throws TransformerException On errors
   * @throws IOException          On errors
   */

  public static String indent(
    final byte[] data)
    throws TransformerException, IOException
  {
    try (final var out = new ByteArrayOutputStream()) {
      indent(data, out);
      return out.toString(StandardCharsets.UTF_8);
    }
  }
}
