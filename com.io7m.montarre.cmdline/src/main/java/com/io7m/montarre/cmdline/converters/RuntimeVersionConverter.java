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


package com.io7m.montarre.cmdline.converters;

import com.io7m.quarrel.core.QValueConverterType;

/**
 * A converter for runtime versions.
 */

public final class RuntimeVersionConverter
  implements QValueConverterType<Runtime.Version>
{
  /**
   * A converter for runtime versions.
   */

  public RuntimeVersionConverter()
  {

  }

  @Override
  public Runtime.Version convertFromString(
    final String text)
  {
    return Runtime.Version.parse(text);
  }

  @Override
  public String convertToString(
    final Runtime.Version value)
  {
    return value.toString();
  }

  @Override
  public Runtime.Version exampleValue()
  {
    return Runtime.version();
  }

  @Override
  public String syntax()
  {
    return "https://openjdk.org/jeps/223";
  }

  @Override
  public Class<Runtime.Version> convertedClass()
  {
    return Runtime.Version.class;
  }
}
