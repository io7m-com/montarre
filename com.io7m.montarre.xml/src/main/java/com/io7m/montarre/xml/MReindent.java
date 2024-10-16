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

import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.transform.OutputKeys;
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
   * @throws IOException On errors
   */

  public static void indent(
    final byte[] data,
    final OutputStream output)
    throws IOException
  {
    try {
      final var indentOut =
        new ByteArrayOutputStream();

      final var transformer =
        TransformerFactory.newInstance()
          .newTransformer();

      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(
        new StreamSource(new ByteArrayInputStream(data)),
        new StreamResult(indentOut)
      );

      final var sigs =
        XMLSignatureFactory.getInstance("DOM");

      final var canon =
        sigs.newCanonicalizationMethod(
          CanonicalizationMethod.EXCLUSIVE,
          (C14NMethodParameterSpec) null
        );

      final var dd =
        new OctetStreamData(new ByteArrayInputStream(indentOut.toByteArray()));

      final var result =
        (OctetStreamData) canon.transform(dd, null);

      result.getOctetStream().transferTo(output);
    } catch (final IOException e) {
      throw e;
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }

  /**
   * Indent the given XML.
   *
   * @param data The XML
   *
   * @return The indented text
   *
   * @throws IOException On errors
   */

  public static String indent(
    final byte[] data)
    throws IOException
  {
    try (final var out = new ByteArrayOutputStream()) {
      indent(data, out);
      return out.toString(StandardCharsets.UTF_8);
    }
  }
}
