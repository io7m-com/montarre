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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>A file filter.</p>
 * <p>
 * If a file is matched by at least one inclusion filter, and not matched by
 * any exclusion filter, then the file will be treated as included.
 * </p>
 */

@ImmutablesStyleType
@Value.Immutable
public interface MFileFilterType
{
  /**
   * @return The filename inclusion patterns
   */

  List<Pattern> includes();

  /**
   * @return The filename exclusion patterns
   */


  List<Pattern> excludes();

  /**
   * Evaluate the filter against the given file name.
   *
   * @param file The file name
   *
   * @return {@code true} iff the filter matched
   */

  default boolean evaluate(
    final String file)
  {
    var included = false;

    for (var include : this.includes()) {
      if (include.matcher(file).matches()) {
        included = true;
        break;
      }
    }

    for (var exclude : this.excludes()) {
      if (exclude.matcher(file).matches()) {
        included = false;
        break;
      }
    }

    return included;
  }
}
