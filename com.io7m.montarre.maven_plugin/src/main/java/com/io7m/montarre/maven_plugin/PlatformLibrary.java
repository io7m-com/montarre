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


package com.io7m.montarre.maven_plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * A platform library filter.
 *
 * @see com.io7m.montarre.api.MPlatformFileFilterType
 */

public final class PlatformLibrary
{
  private String operatingSystem;
  private String architecture;
  private List<String> includes = new ArrayList<>();
  private List<String> excludes = new ArrayList<>();

  /**
   * A platform library filter.
   */

  public PlatformLibrary()
  {

  }

  /**
   * @return The architecture name
   *
   * @see com.io7m.montarre.api.MArchitectureName
   */

  public String getArchitecture()
  {
    return this.architecture;
  }

  /**
   * @return The inclusion patterns
   */

  public List<String> getExcludes()
  {
    return this.excludes;
  }

  /**
   * @return The exclusion patterns
   */

  public List<String> getIncludes()
  {
    return this.includes;
  }

  /**
   * @return The operating system name
   *
   * @see com.io7m.montarre.api.MOperatingSystemName
   */

  public String getOperatingSystem()
  {
    return this.operatingSystem;
  }
}
