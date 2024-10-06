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

/**
 * A vendor.
 */

public final class Vendor
{
  private String id = "";
  private String name = "";

  /**
   * A vendor.
   */

  public Vendor()
  {

  }

  /**
   * @return The ID
   */

  public String getId()
  {
    return this.id;
  }

  /**
   * Set the ID
   *
   * @param newID The ID
   */

  public void setId(
    final String newID)
  {
    this.id = newID;
  }

  /**
   * @return The name
   */

  public String getName()
  {
    return this.name;
  }

  /**
   * Set the name
   *
   * @param newName The name
   */

  public void setName(
    final String newName)
  {
    this.name = newName;
  }
}
