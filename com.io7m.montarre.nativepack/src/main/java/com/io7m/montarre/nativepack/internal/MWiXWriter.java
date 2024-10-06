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


package com.io7m.montarre.nativepack.internal;

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MResourceRole;
import com.io7m.montarre.api.wix.MWiXWriterType;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A WiX writer.
 */

public final class MWiXWriter implements MWiXWriterType
{
  private final MPackageDeclaration pack;
  private final Path directory;
  private final OutputStream output;

  private static final String NS =
    "http://wixtoolset.org/schemas/v4/wxs";

  private final ByteArrayOutputStream bytesOut;

  /**
   * A WiX writer.
   *
   * @param inPack      The package
   * @param inDirectory The input directory
   * @param inOutput    The output
   */

  public MWiXWriter(
    final MPackageDeclaration inPack,
    final Path inDirectory,
    final OutputStream inOutput)
  {
    this.pack =
      Objects.requireNonNull(inPack, "reader");
    this.directory =
      Objects.requireNonNull(inDirectory, "inDirectory");
    this.output =
      Objects.requireNonNull(inOutput, "output");
    this.bytesOut =
      new ByteArrayOutputStream();
  }

  @Override
  public void execute()
    throws MException
  {
    try {
      final var xmlOutputs =
        XMLOutputFactory.newFactory();
      final var xmlOutput =
        xmlOutputs.createXMLStreamWriter(this.bytesOut, "UTF-8");

      this.writeDocument(xmlOutput);
      this.indent(this.bytesOut.toByteArray());
    } catch (final Exception e) {
      throw new MException(e.getMessage(), e, "error-xml");
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

    this.output.write(
      """
        <?xml version="1.0" encoding="UTF-8"?>
        """.trim().getBytes(StandardCharsets.UTF_8));
    this.output.write('\n');

    transformer.transform(
      new StreamSource(new ByteArrayInputStream(data)),
      new StreamResult(this.output)
    );
  }

  private void writeDocument(
    final XMLStreamWriter xmlOutput)
    throws XMLStreamException, IOException
  {
    xmlOutput.writeStartDocument("UTF-8", "1.0");
    xmlOutput.setDefaultNamespace(NS);
    xmlOutput.writeStartElement(NS, "Wix");
    xmlOutput.writeDefaultNamespace(NS);
    this.writePackage(xmlOutput);
    this.writeFilesFragment(xmlOutput);
    xmlOutput.writeEndElement();
    xmlOutput.flush();
  }

  private void writeFilesFragment(
    final XMLStreamWriter xmlOutput)
    throws XMLStreamException, IOException
  {
    xmlOutput.writeStartElement(NS, "Fragment");

    xmlOutput.writeStartElement(NS, "ComponentGroup");
    xmlOutput.writeAttribute("Id", "Files");

    try (final var fileStream = Files.walk(this.directory)) {
      final var fileList =
        fileStream.sorted()
          .toList();

      for (final var file : fileList) {
        if (!Files.isRegularFile(file)) {
          continue;
        }

        xmlOutput.writeStartElement(NS, "Component");
        xmlOutput.writeAttribute("Directory", "INSTALLLOCATION");

        final var subdirectory = this.directory.relativize(file);
        if (subdirectory.getNameCount() > 1) {
          xmlOutput.writeAttribute(
            "Subdirectory",
            subdirectory.getParent().toString()
          );
        }

        xmlOutput.writeStartElement(NS, "File");
        xmlOutput.writeAttribute("Source", file.toString());
        xmlOutput.writeAttribute("KeyPath", "yes");
        xmlOutput.writeEndElement();

        xmlOutput.writeEndElement();
      }
    }

    xmlOutput.writeEndElement();
    xmlOutput.writeEndElement();
  }

  private void writePackage(
    final XMLStreamWriter xmlOutput)
    throws XMLStreamException
  {
    final var metadata =
      this.pack.metadata();
    final var manifest =
      this.pack.manifest();

    final var vendorName =
      metadata.vendor().name().name();
    final var packName =
      metadata.names().packageName().name().value();
    final var packVersion =
      metadata.version().version();
    final var packID =
      metadata.names().id().toString();

    final var iconOpt =
      manifest.items()
        .stream()
        .filter(i -> i instanceof MResource)
        .map(MResource.class::cast)
        .filter(i -> i.role() == MResourceRole.ICO_WINDOWS)
        .findFirst();

    /*
     * Wix says: Specify a four-part version or semantic version, such as
     * '#.#.#.#' or '#.#.#-label.#'.
     */

    final var transformedVersion =
      "%s.0".formatted(packVersion);

    xmlOutput.writeStartElement(NS, "Package");
    xmlOutput.writeAttribute("Language", "1033");
    xmlOutput.writeAttribute("Manufacturer", "io7m");
    xmlOutput.writeAttribute("Name", packName);
    xmlOutput.writeAttribute("Version", transformedVersion);
    xmlOutput.writeAttribute("UpgradeCode", packID);

    /*
     * Disallow downgrades.
     */

    writePackageNoDowngrade(xmlOutput);

    /*
     * Provide an icon.
     */

    if (iconOpt.isPresent()) {
      final var icon = iconOpt.get();
      this.writePackageIcon(xmlOutput, icon);
    }

    /*
     * Embed all the data directly into the MSI file.
     */

    {
      xmlOutput.writeStartElement(NS, "MediaTemplate");
      xmlOutput.writeAttribute("EmbedCab", "yes");
      xmlOutput.writeEndElement();
    }

    {
      xmlOutput.writeStartElement(NS, "StandardDirectory");
      xmlOutput.writeAttribute("Id", "ProgramFilesFolder");

      {
        xmlOutput.writeStartElement(NS, "Directory");
        xmlOutput.writeAttribute("Id", "CompanyFolder");
        xmlOutput.writeAttribute("Name", vendorName);

        {
          xmlOutput.writeStartElement(NS, "Directory");
          xmlOutput.writeAttribute("Id", "INSTALLLOCATION");
          xmlOutput.writeAttribute("Name", packName);
          xmlOutput.writeEndElement();
        }

        xmlOutput.writeEndElement();
      }

      xmlOutput.writeEndElement();
    }

    {
      xmlOutput.writeStartElement(NS, "Feature");
      xmlOutput.writeAttribute("Id", "Application");
      xmlOutput.writeAttribute("Title", "Application");
      xmlOutput.writeAttribute("Level", "1");
      xmlOutput.writeAttribute("ConfigurableDirectory", "INSTALLLOCATION");

      {
        xmlOutput.writeStartElement(NS, "ComponentGroupRef");
        xmlOutput.writeAttribute("Id", "Files");
        xmlOutput.writeEndElement();
      }

      xmlOutput.writeEndElement();
    }

    xmlOutput.writeEndElement();
  }

  private void writePackageIcon(
    final XMLStreamWriter xmlOutput,
    final MResource icon)
    throws XMLStreamException
  {
    {
      final var iconFile =
        this.directory.resolve(icon.file().name());

      xmlOutput.writeStartElement(NS, "Icon");
      xmlOutput.writeAttribute("Id", "Icon.ico");
      xmlOutput.writeAttribute("SourceFile", iconFile.toString());
      xmlOutput.writeEndElement();
    }

    {
      xmlOutput.writeStartElement(NS, "Property");
      xmlOutput.writeAttribute("Id", "ARPPRODUCTICON");
      xmlOutput.writeAttribute("Value", "Icon.ico");
      xmlOutput.writeEndElement();
    }
  }

  private static void writePackageNoDowngrade(
    final XMLStreamWriter xmlOutput)
    throws XMLStreamException
  {
    xmlOutput.writeStartElement(NS, "MajorUpgrade");
    xmlOutput.writeAttribute(
      "DowngradeErrorMessage",
      "A newer version of [ProductName] is already installed.");
    xmlOutput.writeEndElement();
  }

  @Override
  public void close()
  {

  }
}
