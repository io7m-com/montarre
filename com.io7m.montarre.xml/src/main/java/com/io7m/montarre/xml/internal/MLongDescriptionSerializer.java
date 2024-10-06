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


package com.io7m.montarre.xml.internal;

import com.io7m.anethum.api.SerializationException;
import com.io7m.montarre.api.MLongDescription;
import com.io7m.montarre.api.parsers.MLongDescriptionSerializerType;
import com.io7m.montarre.schema.MSchemas;
import com.io7m.montarre.xml.MReindent;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * A long description serializer.
 */

public final class MLongDescriptionSerializer implements
  MLongDescriptionSerializerType
{
  private static final String NS =
    MSchemas.schema1_0().namespace().toString();

  private final OutputStream stream;
  private final XMLOutputFactory outputs;
  private XMLStreamWriter output;

  /**
   * A package serializer.
   *
   * @param inStream The stream
   */

  public MLongDescriptionSerializer(
    final OutputStream inStream)
  {
    this.stream =
      Objects.requireNonNull(inStream, "stream");
    this.outputs =
      XMLOutputFactory.newDefaultFactory();
  }

  @Override
  public void execute(
    final MLongDescription value)
    throws SerializationException
  {
    try {
      final var byteOutput =
        new ByteArrayOutputStream();
      this.output =
        this.outputs.createXMLStreamWriter(byteOutput, "UTF-8");

      this.output.writeStartDocument("UTF-8", "1.0");
      this.output.setDefaultNamespace(NS);
      this.output.writeStartElement(NS, "LongDescription");
      this.output.writeDefaultNamespace(NS);
      this.output.writeAttribute("Language", value.language());

      for (final var p : value.descriptions()) {
        this.output.writeStartElement(NS, "Paragraph");
        this.output.writeCharacters(p.text().trim());
        this.output.writeEndElement();
      }

      for (final var f : value.features()) {
        this.output.writeStartElement(NS, "Feature");
        this.output.writeCharacters(f.text().trim());
        this.output.writeEndElement();
      }

      this.output.writeEndElement();
      this.output.flush();

      MReindent.indent(byteOutput.toByteArray(), this.stream);
    } catch (final Exception e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  @Override
  public void close()
  {

  }
}
