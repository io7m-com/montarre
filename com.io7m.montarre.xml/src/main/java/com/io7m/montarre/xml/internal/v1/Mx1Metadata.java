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


package com.io7m.montarre.xml.internal.v1;

import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MMetadataFlatpakType;
import com.io7m.montarre.api.MPackageName;
import com.io7m.montarre.api.MShortName;
import com.io7m.montarre.api.MVendorName;
import com.io7m.verona.core.VersionException;
import com.io7m.verona.core.VersionParser;
import org.xml.sax.Attributes;

import java.net.URI;
import java.util.Map;

import static com.io7m.montarre.xml.internal.v1.Mx1.element;

/**
 * A parser.
 */

public final class Mx1Metadata
  implements BTElementHandlerType<Object, MMetadata>
{
  private final MMetadata.Builder builder;

  /**
   * A parser.
   *
   * @param context The context
   */

  public Mx1Metadata(
    final BTElementParsingContextType context)
  {
    this.builder = MMetadata.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        element("Flatpak"),
        Mx1Flatpak::new
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case MMetadataFlatpakType flatpak -> {
        this.builder.setFlatpak(flatpak);
      }
      default -> {
        throw new IllegalStateException("Unexpected value: " + result);
      }
    }
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws VersionException
  {
    this.builder.setSiteURI(
      URI.create(attributes.getValue("SiteURL")));
    this.builder.setShortName(
      new MShortName(attributes.getValue("ShortName")));
    this.builder.setRequiredJDKVersion(
      Long.parseUnsignedLong(attributes.getValue("RequiredJDKVersion")));
    this.builder.setPackageName(
      new MPackageName(new RDottedName(attributes.getValue("Name"))));
    this.builder.setVersion(
      VersionParser.parse(attributes.getValue("Version")));
    this.builder.setMainModule(
      attributes.getValue("MainModule"));
    this.builder.setDescription(
      attributes.getValue("Description"));
    this.builder.setCopyright(
      attributes.getValue("Copyright"));
    this.builder.setLicense(
      attributes.getValue("License"));
    this.builder.setVendorName(
      new MVendorName(attributes.getValue("VendorName")));
  }

  @Override
  public MMetadata onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
