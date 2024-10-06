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
import com.io7m.montarre.api.MApplicationKind;
import com.io7m.montarre.api.MCategoryName;
import com.io7m.montarre.api.MCopying;
import com.io7m.montarre.api.MJavaInfo;
import com.io7m.montarre.api.MLink;
import com.io7m.montarre.api.MLongDescription;
import com.io7m.montarre.api.MMetadata;
import com.io7m.montarre.api.MMetadataFlatpakType;
import com.io7m.montarre.api.MNames;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVersion;
import com.io7m.verona.core.VersionException;
import org.xml.sax.Attributes;

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
      Map.entry(element("Flatpak"), Mx1Flatpak::new),
      Map.entry(element("Category"), Mx1Category::new),
      Map.entry(element("Link"), Mx1Link::new),
      Map.entry(element("LongDescription"), Mx1LongDescription::new),
      Map.entry(element("Vendor"), Mx1Vendor::new),
      Map.entry(element("Version"), Mx1Version::new),
      Map.entry(element("JavaInfo"), Mx1JavaInfo::new),
      Map.entry(element("Copying"), Mx1Copying::new),
      Map.entry(element("Names"), Mx1Names::new)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final MVersion version -> {
        this.builder.setVersion(version);
      }
      case final MVendor vendor -> {
        this.builder.setVendor(vendor);
      }
      case final MMetadataFlatpakType flatpak -> {
        this.builder.setFlatpak(flatpak);
      }
      case final MCategoryName category -> {
        this.builder.addCategories(category);
      }
      case final MLink link -> {
        this.builder.addLinks(link);
      }
      case final MJavaInfo info -> {
        this.builder.setJavaInfo(info);
      }
      case final MNames info -> {
        this.builder.setNames(info);
      }
      case final MCopying copying -> {
        this.builder.setCopying(copying);
      }
      case final MLongDescription longDescription -> {
        this.builder.addLongDescriptions(longDescription);
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
    this.builder.setDescription(
      attributes.getValue("Description"));
    this.builder.setApplicationKind(
      MApplicationKind.valueOf(attributes.getValue("ApplicationKind")));
  }

  @Override
  public MMetadata onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
