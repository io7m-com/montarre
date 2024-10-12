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


package com.io7m.montarre.io.internal;

import com.io7m.montarre.api.MPackageDeclaration;

import java.util.regex.Pattern;

/**
 * Checks on names.
 */

public final class MCheckName
  extends MCheckAbstract
  implements MCheckType
{
  private static final String RULE =
    "https://docs.flathub.org/docs/for-app-authors/metainfo-guidelines/quality-guidelines#app-name";

  private static final Pattern VALID_CHARACTERS =
    Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N} ]*");

  /**
   * Checks on names.
   */

  public MCheckName()
  {

  }

  @Override
  protected void onExecute(
    final MPackageDeclaration declaration)
  {
    this.checkNameLength(declaration.metadata().names().humanName());
  }

  private void checkNameLength(
    final String name)
  {
    if (name.length() > 15) {
      this.attribute("RuleURI", RULE);
      this.attribute("Name", name);
      this.warning("flatpak.name.length_warn", "Names should be no longer than 15 characters.");
    }

    if (name.length() >= 20) {
      this.attribute("RuleURI", RULE);
      this.attribute("Name", name);
      this.warning("flatpak.name.length_require", "Names must be shorter than 20 characters.");
    }

    if (!VALID_CHARACTERS.matcher(name).matches()) {
      this.attribute("RuleURI", RULE);
      this.attribute("Name", name);
      this.warning("flatpak.name.format", "Names should avoid nonstandard formatting and punctuation.");
    }
  }
}
