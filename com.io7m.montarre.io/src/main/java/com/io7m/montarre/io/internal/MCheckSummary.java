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

import com.io7m.montarre.api.MMetadataType;
import com.io7m.montarre.api.MPackageDeclaration;

/**
 * Checks on summaries.
 */

public final class MCheckSummary
  extends MCheckAbstract
  implements MCheckType
{
  private static final String RULE =
    "https://docs.flathub.org/docs/for-app-authors/metainfo-guidelines/quality-guidelines#summary";

  private static final String RULE_NO_REPEAT_NAME =
    "https://docs.flathub.org/docs/for-app-authors/metainfo-guidelines/quality-guidelines#dont-repeat-the-name";

  private static final String RULE_FORMAT =
    "https://docs.flathub.org/docs/for-app-authors/metainfo-guidelines/quality-guidelines#no-weird-formatting-1";

  /**
   * Checks on summaries.
   */

  public MCheckSummary()
  {

  }

  @Override
  protected void onExecute(
    final MPackageDeclaration declaration)
  {
    final MMetadataType meta = declaration.metadata();
    final var description = meta.description();

    for (final var text : description.text().all().entrySet()) {
      final var textS = text.getValue();
      if (textS.length() > 25) {
        this.attribute("RuleURI", RULE);
        this.attribute("Text", textS);
        this.warning("flatpak.summary.length_warn", "Summary should be no longer than 25 characters.");
      }

      if (textS.length() > 35) {
        this.attribute("RuleURI", RULE);
        this.attribute("Text", textS);
        this.error("flatpak.summary.length_require", "Summary must be shorter than 36 characters.");
      }

      if (textS.contains(meta.names().humanName())) {
        this.attribute("RuleURI", RULE_NO_REPEAT_NAME);
        this.attribute("Text", textS);
        this.warning("flatpak.summary.no_repeat_name", "Do not repeat the human name in the summary.");
      }

      if (textS.endsWith(".")) {
        this.attribute("RuleURI", RULE_FORMAT);
        this.attribute("Text", textS);
        this.warning("flatpak.summary.end_dot", "Summaries should not end with a full stop.");
      }
    }
  }
}
