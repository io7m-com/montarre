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
import com.io7m.montarre.api.MCaption;
import com.io7m.montarre.api.MFileName;
import com.io7m.montarre.api.MHash;
import com.io7m.montarre.api.MHashAlgorithm;
import com.io7m.montarre.api.MHashValue;
import com.io7m.montarre.api.MResource;
import com.io7m.montarre.api.MResourceRole;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Optional;

import static com.io7m.montarre.xml.internal.v1.Mx1.element;

/**
 * A parser.
 */

public final class Mx1Resource
  implements BTElementHandlerType<MCaption, MResource>
{
  private Optional<MCaption> captionOpt = Optional.empty();
  private MFileName fileName;
  private MHash hash;
  private MResourceRole role;
  private long size;

  /**
   * A parser.
   *
   * @param context The context
   */

  public Mx1Resource(
    final BTElementParsingContextType context)
  {

  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends MCaption>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        element("Caption"),
        Mx1Caption::new
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final MCaption result)
  {
    this.captionOpt = Optional.of(result);
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.fileName =
      new MFileName(attributes.getValue("File"));
    this.size =
      Long.parseUnsignedLong(attributes.getValue("Size"));
    this.hash =
      new MHash(
        new MHashAlgorithm(
          attributes.getValue("HashAlgorithm")
        ),
        new MHashValue(
          attributes.getValue("HashValue")
        )
      );
    this.role =
      MResourceRole.valueOf(attributes.getValue("Role"));
  }

  @Override
  public MResource onElementFinished(
    final BTElementParsingContextType context)
  {
    return new MResource(
      this.fileName,
      this.hash,
      this.size,
      this.role,
      this.captionOpt
    );
  }
}
