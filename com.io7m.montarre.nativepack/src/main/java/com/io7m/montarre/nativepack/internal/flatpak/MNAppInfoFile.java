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


package com.io7m.montarre.nativepack.internal.flatpak;

import com.io7m.montarre.api.MApplicationKind;
import com.io7m.montarre.api.MLinkRole;
import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.xml.MReindent;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Functions to generate AppInfo files.
 *
 * @see "https://www.freedesktop.org/software/appstream/docs/chap-Metadata.html"
 */

public final class MNAppInfoFile
{
  private MNAppInfoFile()
  {

  }

  /**
   * Generate the text of an app info file.
   *
   * @param packageV The package
   *
   * @return The text of the file
   */

  public static String createAppInfoFileText(
    final MPackageDeclaration packageV)
  {
    try {
      final var metadata =
        packageV.metadata();

      final var byteOutput =
        new ByteArrayOutputStream();
      final var outputs =
        XMLOutputFactory.newDefaultFactory();

      final var output =
        outputs.createXMLStreamWriter(byteOutput, "UTF-8");

      output.writeStartDocument("UTF-8", "1.0");
      output.writeStartElement("component");
      output.writeAttribute("type", typeOfKind(metadata.applicationKind()));

      writeID(output, metadata);
      writeName(output, metadata);
      writeSummary(metadata, output);
      writeLinks(metadata, output);
      writeLicenses(output, metadata);
      writeDescription(output, metadata);
      writeDeveloper(output, metadata);
      writeCategories(output, metadata);
      writeProvides(output, metadata);
      writeReleases(output, metadata);
      writeContentRating(output);

      output.writeEndElement();
      output.flush();

      return MReindent.indent(byteOutput.toByteArray());
    } catch (final Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static void writeLinks(
    final MMetadataType metadata,
    final XMLStreamWriter output)
    throws XMLStreamException
  {
    for (final var link : metadata.links().stream().sorted().toList()) {
      output.writeStartElement("url");
      output.writeAttribute("type", linkType(link.role()));
      output.writeCharacters(link.target().toString());
      output.writeEndElement();
    }
  }

  private static String linkType(
    final MLinkRole role)
  {
    return switch (role) {
      case ISSUES -> "bugtracker";
      case HOME_PAGE -> "homepage";
      case DONATION -> "donation";
      case CONTACT -> "contact";
      case FAQ -> "faq";
      case TRANSLATE -> "translation";
      case CONTRIBUTE -> "contribute";
      case SCM -> "vcs-browser";
    };
  }

  private static void writeContentRating(
    final XMLStreamWriter output)
    throws XMLStreamException
  {
    output.writeStartElement("content_rating");
    output.writeAttribute("type", "oars-1.1");
    output.writeEndElement();
  }

  private static void writeReleases(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("releases");
    {
      output.writeStartElement("release");
      output.writeAttribute("version", metadata.version().version().toString());
      output.writeAttribute("date", "2000-01-01");
      output.writeEndElement();
    }
    output.writeEndElement();
  }

  private static void writeID(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("id");
    output.writeCharacters(metadata.names().packageName().toString());
    output.writeEndElement();
  }

  private static void writeName(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("name");
    output.writeCharacters(metadata.names().humanName());
    output.writeEndElement();
  }

  private static void writeProvides(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("provides");
    {
      output.writeStartElement("binary");
      output.writeCharacters(metadata.names().shortName().name());
      output.writeEndElement();
    }
    output.writeEndElement();
  }

  private static void writeCategories(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("categories");
    {
      for (final var category : metadata.categories().stream().sorted().toList()) {
        output.writeStartElement("category");
        output.writeCharacters(category.name());
        output.writeEndElement();
      }
    }
    output.writeEndElement();
  }

  private static void writeDeveloper(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("developer");
    {
      final var vendor = metadata.vendor();
      output.writeAttribute("id", vendor.id().toString());
      output.writeStartElement("name");
      output.writeCharacters(vendor.name().name());
      output.writeEndElement();
    }
    output.writeEndElement();
  }

  private static void writeDescription(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("description");
    {
      final var longDescriptions =
        metadata.longDescriptionsByLanguage();

      for (final var language : longDescriptions.keySet()) {
        final var description =
          longDescriptions.get(language);

        for (final var paragraph : description.descriptions()) {
          output.writeStartElement("p");
          if (!Objects.equals(language, "en")) {
            output.writeAttribute("xml:lang", language);
          }
          output.writeCharacters(paragraph.text().trim());
          output.writeEndElement();
        }

        if (description.features().isEmpty()) {
          continue;
        }

        output.writeStartElement("ul");

        for (final var feature : description.features()) {
          output.writeStartElement("li");
          if (!Objects.equals(language, "en")) {
            output.writeAttribute("xml:lang", language);
          }
          output.writeCharacters(feature.text().trim());
          output.writeEndElement();
        }

        output.writeEndElement();
      }
    }
    output.writeEndElement();
  }

  private static void writeLicenses(
    final XMLStreamWriter output,
    final MMetadataType metadata)
    throws XMLStreamException
  {
    output.writeStartElement("metadata_license");
    output.writeCharacters("CC0-1.0");
    output.writeEndElement();

    output.writeStartElement("project_license");
    output.writeCharacters(metadata.copying().license());
    output.writeEndElement();
  }

  /*
   * summary-has-dot-suffix
   *
   * "The component summary should not end with a dot (`.`)."
   */

  private static void writeSummary(
    final MMetadataType metadata,
    final XMLStreamWriter output)
    throws XMLStreamException
  {
    final var noDotDescription =
      metadata.description().replaceAll("\\.+$", "");

    output.writeStartElement("summary");
    output.writeCharacters(noDotDescription);
    output.writeEndElement();
  }

  private static String typeOfKind(
    final MApplicationKind kind)
  {
    return switch (kind) {
      case CONSOLE -> "console-application";
      case GRAPHICAL -> "desktop-application";
    };
  }
}
