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
 * A version.
 */

public final class Version
{
  private String number = "";
  private String date = "";

  /**
   * A version.
   */

  public Version()
  {

  }

  /**
   * @return The number
   */

  public String getNumber()
  {
    return this.number;
  }

  /**
   * Set the number
   *
   * @param newID The number
   */

  public void setNumber(
    final String newID)
  {
    this.number = newID;
  }

  /**
   * @return The date
   */

  public String getDate()
  {
    return this.date;
  }

  /**
   * Set the date
   *
   * @param newName The date
   */

  public void setDate(
    final String newName)
  {
    this.date = newName;
  }
}
