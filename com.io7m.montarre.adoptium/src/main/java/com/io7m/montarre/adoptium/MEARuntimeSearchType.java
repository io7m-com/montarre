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


package com.io7m.montarre.adoptium;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MOperatingSystemName;
import org.immutables.value.Value;

/**
 * Runtime search parameters.
 */

@ImmutablesStyleType
@Value.Immutable
public interface MEARuntimeSearchType
{
  /**
   * @return The Java feature version (such as "21")
   */

  int featureVersion();

  /**
   * @return The architecture name
   */

  MArchitectureName architecture();

  /**
   * @return The operating system name
   */

  MOperatingSystemName operatingSystem();

  /**
   * @return The image kind
   */

  @Value.Default
  default METImageKind imageKind()
  {
    return METImageKind.JRE;
  }
}