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
import com.io7m.montarre.api.MCategoryName;
import com.io7m.montarre.api.MCopyingType;
import com.io7m.montarre.api.MDescription;
import com.io7m.montarre.api.MFlatpakRuntime;
import com.io7m.montarre.api.MJavaInfoType;
import com.io7m.montarre.api.MLink;
import com.io7m.montarre.api.MLongDescriptionType;
import com.io7m.montarre.api.MManifestItemType;
import com.io7m.montarre.api.MManifestType;
import com.io7m.montarre.api.MMetadataFlatpakType;
import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MModule;
import com.io7m.montarre.api.MNamesType;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPlatformDependentModule;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MTranslatedText;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVersion;
import com.io7m.montarre.api.parsers.MPackageDeclarationSerializerType;
import com.io7m.montarre.schema.MSchemas;
import com.io7m.montarre.xml.MReindent;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A package serializer.
 */

public final class MPackageDeclarationSerializer implements
  MPackageDeclarationSerializerType
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

  public MPackageDeclarationSerializer(
    final OutputStream inStream)
  {
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

      MReindent.indent(byteOutput.toByteArray(), this.stream);
    } catch (final Exception e) {
      throw new SerializationException(e.getMessage(), e);
    }
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

    for (final var item : sortedItems) {
      this.writeManifestItem(item);
    }

    this.output.writeEndElement();
  }

  private void writeManifestItem(
    final MManifestItemType item)
    throws XMLStreamException
  {
    switch (item) {
      case final MResource i -> {
        this.writeManifestItemResource(i);
      }
      case final MModule i -> {
        this.writeManifestItemModule(i);
      }
      case final MPlatformDependentModule i -> {
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

    if (i.caption().isPresent()) {
      final var caption = i.caption().get();
      this.output.writeStartElement(NS, "Caption");
      this.writeText(caption.text());
      this.output.writeEndElement();
    }

    this.output.writeEndElement();
  }

  private void writeMetadata(
    final MMetadataType metadata)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Metadata");

    this.output.writeAttribute(
      "ApplicationKind",
      metadata.applicationKind().name()
    );

    this.writeCategories(metadata.categories());
    this.writeCopying(metadata.copying());
    this.writeDescription(metadata.description());
    this.writeFlatpak(metadata.flatpak());
    this.writeJavaInfo(metadata.javaInfo());
    this.writeLinks(metadata.links());
    this.writeLongDescriptions(metadata.longDescriptions());
    this.writeNames(metadata.names());
    this.writeVendor(metadata.vendor());
    this.writeVersion(metadata.version());
    this.output.writeEndElement();
  }

  private void writeDescription(
    final MDescription description)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Description");
    this.writeText(description.text());
    this.output.writeEndElement();
  }

  private void writeText(
    final MTranslatedText text)
    throws XMLStreamException
  {
    final var sortedEntries =
      text.all()
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .toList();

    for (final var entry : sortedEntries) {
      this.output.writeStartElement(NS, "Text");
      this.output.writeAttribute("Language", entry.getKey().name());
      this.output.writeCharacters(entry.getValue());
      this.output.writeEndElement();
    }
  }

  private void writeNames(
    final MNamesType names)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Names");
    this.output.writeAttribute(
      "Name",
      names.packageName().name().value()
    );
    this.output.writeAttribute(
      "ShortName",
      names.shortName().name()
    );
    this.output.writeAttribute(
      "HumanName",
      names.humanName()
    );
    this.output.writeEndElement();
  }

  private void writeCopying(
    final MCopyingType copying)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Copying");
    this.output.writeAttribute(
      "Copyright",
      copying.copyright()
    );
    this.output.writeAttribute(
      "License",
      copying.license()
    );
    this.output.writeEndElement();
  }

  private void writeJavaInfo(
    final MJavaInfoType info)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "JavaInfo");
    this.output.writeAttribute(
      "RequiredJDKVersion",
      Long.toUnsignedString(info.requiredJDKVersion())
    );
    this.output.writeAttribute(
      "MainModule",
      info.mainModule()
    );
    this.output.writeEndElement();
  }

  private void writeVersion(
    final MVersion version)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Version");
    this.output.writeAttribute("Number", version.version().toString());
    this.output.writeAttribute("Date", version.date().toString());
    this.output.writeEndElement();
  }

  private void writeVendor(
    final MVendor vendor)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Vendor");
    this.output.writeAttribute("ID", vendor.id().toString());
    this.output.writeAttribute("Name", vendor.name().toString());
    this.output.writeEndElement();
  }

  private void writeLongDescriptions(
    final List<MLongDescriptionType> descriptions)
    throws XMLStreamException
  {
    for (final var description : descriptions) {
      this.writeLongDescription(description);
    }
  }

  private void writeLongDescription(
    final MLongDescriptionType value)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "LongDescription");
    this.output.writeAttribute("Language", value.language().name());

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
  }

  private void writeLinks(
    final Set<MLink> links)
    throws XMLStreamException
  {
    for (final var link : links.stream().sorted().toList()) {
      this.output.writeStartElement(NS, "Link");
      this.output.writeAttribute("Role", link.role().name());
      this.output.writeAttribute("Target", link.target().toString());
      this.output.writeEndElement();
    }
  }

  private void writeCategories(
    final Set<MCategoryName> categories)
    throws XMLStreamException
  {
    for (final var name : categories.stream().sorted().toList()) {
      this.output.writeStartElement(NS, "Category");
      this.output.writeAttribute("Name", name.name());
      this.output.writeEndElement();
    }
  }

  private void writeFlatpak(
    final MMetadataFlatpakType flatpak)
    throws XMLStreamException
  {
    this.output.writeStartElement(NS, "Flatpak");
    for (final var runtime : flatpak.runtimes()) {
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
