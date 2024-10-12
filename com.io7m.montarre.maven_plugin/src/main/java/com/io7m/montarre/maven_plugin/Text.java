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
 * A text
 *
 * @see com.io7m.montarre.api.MText
 */

public final class Text
{
  private String language = "en";
  private String text = "";

  /**
   * A text
   *
   * @see com.io7m.montarre.api.MText
   */

  public Text()
  {

  }

  /**
   * @return The language
   */

  public String getLanguage()
  {
    return this.language;
  }

  /**
   * Set the language.
   *
   * @param newLanguage The language
   */

  public void setLanguage(
    final String newLanguage)
  {
    this.language = newLanguage;
  }

  /**
   * @return The text
   */

  public String getText()
  {
    return this.text;
  }

  /**
   * Set the text.
   *
   * @param newText The text
   */

  public void setText(
    final String newText)
  {
    this.text = newText;
  }
}
