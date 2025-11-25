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
import com.io7m.montarre.api.MManifestType;
import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.MPackageElementType;

import java.util.Map;

/**
 * A parser.
 */

public final class Mx1Package
  implements BTElementHandlerType<MPackageElementType, MPackageDeclaration>
{
  private final MPackageDeclaration.Builder builder;

  /**
   * A parser.
   *
   * @param context The context
   */

  public Mx1Package(
    final BTElementParsingContextType context)
  {
    this.builder = MPackageDeclaration.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends MPackageElementType>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        Mx1.element("Metadata"),
        Mx1Metadata::new
      ),
      Map.entry(
        Mx1.element("Manifest"),
        Mx1Manifest::new
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final MPackageElementType result)
  {
    switch (result) {
      case MManifestType intro -> {
        this.builder.setManifest(intro);
      }
      case MMetadataType intro -> {
        this.builder.setMetadata(intro);
      }
    }
  }

  @Override
  public MPackageDeclaration onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
