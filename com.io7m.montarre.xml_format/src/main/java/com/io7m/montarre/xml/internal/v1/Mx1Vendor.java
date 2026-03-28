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
import com.io7m.lanark.core.RDottedName;
import com.io7m.montarre.api.MVendor;
import com.io7m.montarre.api.MVendorID;
import com.io7m.montarre.api.MVendorName;
import org.xml.sax.Attributes;

/**
 * A parser.
 */

public final class Mx1Vendor
  implements BTElementHandlerType<Object, MVendor>
{
  private MVendor vendor;

  /**
   * A parser.
   *
   * @param context The context
   */

  public Mx1Vendor(
    final BTElementParsingContextType context)
  {

  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.vendor = new MVendor(
      new MVendorID(new RDottedName(attributes.getValue("ID"))),
      new MVendorName(attributes.getValue("Name"))
    );
  }

  @Override
  public MVendor onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.vendor;
  }
}
