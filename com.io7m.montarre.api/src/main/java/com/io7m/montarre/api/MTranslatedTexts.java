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


package com.io7m.montarre.api;

import java.util.Map;

/**
 * Functions over translated texts.
 */

public final class MTranslatedTexts
{
  private MTranslatedTexts()
  {

  }

  /**
   * Construct a translated text.
   * @param entries The translations
   * @return A translated text
   */

  @SafeVarargs
  public static MTranslatedText ofTranslations(
    final Map.Entry<MLanguageCode, String>... entries)
  {
    if (entries.length == 0) {
      return MTranslatedText.builder()
        .setLanguage(new MLanguageCode("en"))
        .setText("")
        .build();
    }

    final var builder = MTranslatedText.builder();
    for (int index = 0; index < entries.length; ++index) {
      if (index == 0) {
        builder.setLanguage(entries[index].getKey());
        builder.setText(entries[index].getValue());
      } else {
        builder.putTranslations(
          entries[index].getKey(),
          entries[index].getValue()
        );
      }
    }

    return builder.build();
  }
}
