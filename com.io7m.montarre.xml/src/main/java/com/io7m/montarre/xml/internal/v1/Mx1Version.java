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

import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.montarre.api.MVersion;
import com.io7m.verona.core.VersionException;
import com.io7m.verona.core.VersionParser;
import org.xml.sax.Attributes;

import java.time.LocalDate;

/**
 * A parser.
 */

public final class Mx1Version
  implements BTElementHandlerType<Object, MVersion>
{
  private MVersion version;

  /**
   * A parser.
   *
   * @param context The context
   */

  public Mx1Version(
    final BTElementParsingContextType context)
  {

  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws VersionException
  {
    this.version = new MVersion(
      VersionParser.parse(attributes.getValue("Number")),
      LocalDate.parse(attributes.getValue("Date"))
    );
  }

  @Override
  public MVersion onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.version;
  }
}
