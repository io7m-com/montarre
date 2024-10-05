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
import com.io7m.montarre.api.MFlatpakRuntime;
import com.io7m.montarre.api.MManifestItemType;
import com.io7m.montarre.api.MManifestType;
import com.io7m.montarre.api.MMetadataFlatpakType;
import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MModule;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPlatformDependentModule;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.parsers.MPackageDeclarationSerializerType;
import com.io7m.montarre.schema.MSchemas;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Objects;

/**
 * A package serializer.
 */

public final class MPackageDeclarationSerializer implements
  MPackageDeclarationSerializerType
{
  private static final String NS =
    MSchemas.schema1_0().namespace().toString();

  private final URI target;
  private final OutputStream stream;
  private final XMLOutputFactory outputs;
  private XMLStreamWriter output;

  /**
   * A package serializer.
   *
   * @param inTarget The target
   * @param inStream The stream
   */

  public MPackageDeclarationSerializer(
    final URI inTarget,
    final OutputStream inStream)
  {
    this.target =
      Objects.requireNonNull(inTarget, "target");
    this.stream =
      Objects.requireNonNull(inStream, "stream");
    this.outputs =
      XMLOutputFactory.newDefaultFactory();
  }

  @Override
  public void execute(
    final MPackageDeclaration value)
    throws SerializationException
  {
    try {
      final var byteOutput =
        new ByteArrayOutputStream();
      this.output =
        this.outputs.createXMLStreamWriter(byteOutput, "UTF-8");

      this.output.writeStartDocument("UTF-8", "1.0");
      this.output.setDefaultNamespace(NS);
      this.output.writeStartElement(NS, "Package");
      this.output.writeDefaultNamespace(NS);

      this.writeMetadata(value.metadata());
      this.writeManifest(value.manifest());

      this.output.writeEndElement();
      this.output.flush();

      this.indent(byteOutput.toByteArray());
    } catch (final Exception e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  private void indent(
    final byte[] data)
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

    this.stream.write(
      """
        <?xml version="1.0" encoding="UTF-8"?>
        """.trim().getBytes(StandardCharsets.UTF_8));
    this.stream.write('\n');

    transformer.transform(
      new StreamSource(new ByteArrayInputStream(data)),
      new StreamResult(this.stream)
    );
  }

  private void writeManifest(
    final MManifestType manifest)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Manifest");

    final var sortedItems =
      manifest.items()
        .stream()
        .sorted(Comparator.comparing(MManifestItemType::file))
        .toList();

    for (var item : sortedItems) {
      this.writeManifestItem(item);
    }

    this.output.writeEndElement();
  }

  private void writeManifestItem(
    final MManifestItemType item)
    throws XMLStreamException
  {
    switch (item) {
      case MResource i -> {
        this.writeManifestItemResource(i);
      }
      case MModule i -> {
        this.writeManifestItemModule(i);
      }
      case MPlatformDependentModule i -> {
        this.writeManifestItemPlatformDependentModule(i);
      }
    }
  }

  private void writeManifestItemPlatformDependentModule(
    final MPlatformDependentModule i)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "PlatformDependentModule");
    this.output.writeAttribute("File", i.file().name());
    this.output.writeAttribute("HashAlgorithm", i.hash().algorithm().name());
    this.output.writeAttribute("HashValue", i.hash().value().value());
    this.output.writeAttribute("OperatingSystem", i.operatingSystem().name());
    this.output.writeAttribute("Architecture", i.architecture().name());
    this.output.writeEndElement();
  }

  private void writeManifestItemModule(
    final MModule i)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Module");
    this.output.writeAttribute("File", i.file().name());
    this.output.writeAttribute("HashAlgorithm", i.hash().algorithm().name());
    this.output.writeAttribute("HashValue", i.hash().value().value());
    this.output.writeEndElement();
  }

  private void writeManifestItemResource(
    final MResource i)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Resource");
    this.output.writeAttribute("File", i.file().name());
    this.output.writeAttribute("HashAlgorithm", i.hash().algorithm().name());
    this.output.writeAttribute("HashValue", i.hash().value().value());
    this.output.writeAttribute("Role", i.role().name());
    this.output.writeEndElement();
  }

  private void writeMetadata(
    final MMetadataType metadata)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Metadata");
    this.output.writeAttribute(
      "Version",
      metadata.version().toString()
    );
    this.output.writeAttribute(
      "Name",
      metadata.packageName().name().value()
    );
    this.output.writeAttribute(
      "RequiredJDKVersion",
      Long.toUnsignedString(metadata.requiredJDKVersion())
    );
    this.output.writeAttribute(
      "Description",
      metadata.description()
    );
    this.output.writeAttribute(
      "Copyright",
      metadata.copyright()
    );
    this.output.writeAttribute(
      "SiteURL",
      metadata.siteURI().toString()
    );
    this.output.writeAttribute(
      "MainModule",
      metadata.mainModule()
    );
    this.output.writeAttribute(
      "License",
      metadata.license()
    );
    this.output.writeAttribute(
      "ShortName",
      metadata.shortName().name()
    );
    this.output.writeAttribute(
      "VendorName",
      metadata.vendorName().name()
    );
    this.writeFlatpak(metadata.flatpak());
    this.output.writeEndElement();
  }

  private void writeFlatpak(
    final MMetadataFlatpakType flatpak)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Flatpak");
    for (var runtime : flatpak.runtimes()) {
      this.writeFlatpakRuntime(runtime);
    }
    this.output.writeEndElement();
  }

  private void writeFlatpakRuntime(
    final MFlatpakRuntime runtime)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "FlatpakRuntime");
    this.output.writeAttribute("Name", runtime.name());
    this.output.writeAttribute("Version", runtime.version());
    this.output.writeAttribute("Role", runtime.role().name());
    this.output.writeEndElement();
  }

  @Override
  public void close()
  {

  }
}
