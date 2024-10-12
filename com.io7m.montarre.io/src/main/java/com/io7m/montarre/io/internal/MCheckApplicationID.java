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
import com.io7m.montarre.api.MPackageName;

import java.util.regex.Pattern;

/**
 * Checks on package names (application IDs).
 */

public final class MCheckApplicationID
  extends MCheckAbstract
  implements MCheckType
{
  private static final String APP_ID_RULE =
    "https://docs.flathub.org/docs/for-app-authors/requirements#application-id";

  private static final Pattern COMPONENT =
    Pattern.compile("_?[A-Za-z0-9_]+");

  /**
   * Checks on package names (application IDs).
   */

  public MCheckApplicationID()
  {

  }

  @Override
  protected void onExecute(
    final MPackageDeclaration declaration)
  {
    final var id =
      declaration.metadata().names().packageName();

    this.checkNameComponentCount(id);
    this.checkNameComponentFormat(id);

    final var idText = id.toString();
    this.checkNameLength(idText);
    this.checkNameGenericEndApp(idText);
    this.checkNameGenericEndDesktop(idText);

    this.checkBanned("com.github", "io.github", id);
    this.checkBanned("com.gitlab", "io.gitlab", id);
    this.checkBanned("codeberg.org", "page.codeberg", id);
    this.checkBanned("framagit.org", "io.frama", id);

    this.checkLongEnough("io.github", id);
    this.checkLongEnough("io.gitlab", id);
    this.checkLongEnough("page.codeberg", id);
    this.checkLongEnough("io.frama", id);
  }

  private void checkLongEnough(
    final String prefix,
    final MPackageName id)
  {
    if (id.name().value().startsWith(prefix)) {
      if (id.name().segments().size() < 4) {
        this.attribute("RuleURI", APP_ID_RULE);
        this.attribute("ID", id);
        this.error(
          "flatpak.app_id.prefix_requires_length",
          "This package namespace requires the use of at least 4 components."
        );
      }
    }
  }

  private void checkBanned(
    final String bannedPrefix,
    final String suggestedPrefix,
    final MPackageName id)
  {
    if (id.name().value().startsWith(bannedPrefix)) {
      this.attribute("RuleURI", APP_ID_RULE);
      this.attribute("ID", id);
      this.attribute("Required Prefix", suggestedPrefix);
      this.error(
        "flatpak.app_id.banned_prefix",
        "Banned package namespace."
      );
    }
  }

  private void checkNameComponentFormat(
    final MPackageName id)
  {
    final var c = id.name().segments();
    for (int index = 0; index < c.size(); ++index) {
      final var segment = c.get(index);
      if (index + 1 != c.size()) {
        if (!COMPONENT.matcher(segment).matches()) {
          this.attribute("RuleURI", APP_ID_RULE);
          this.attribute("ID", id);
          this.error(
            "flatpak.app_id.component_format",
            "Package name components must match '%s'"
              .formatted(COMPONENT.pattern())
          );
        }
      }
    }
  }

  private void checkNameGenericEndDesktop(
    final String idText)
  {
    if (idText.endsWith(".desktop")) {
      this.attribute("RuleURI", APP_ID_RULE);
      this.attribute("ID", idText);
      this.error(
        "flatpak.app_id.ends_generic",
        "Package name must not end in generic terms like .desktop or .app."
      );
    }
  }

  private void checkNameGenericEndApp(
    final String idText)
  {
    if (idText.endsWith(".app")) {
      this.attribute("RuleURI", APP_ID_RULE);
      this.attribute("ID", idText);
      this.error(
        "flatpak.app_id.ends_generic",
        "Package name must not end in generic terms like .desktop or .app."
      );
    }
  }

  private void checkNameLength(
    final String idText)
  {
    if (idText.length() > 255) {
      this.attribute("RuleURI", APP_ID_RULE);
      this.attribute("ID", idText);
      this.error(
        "flatpak.app_id.length",
        "Package name must not exceed 255 characters."
      );
    }
  }

  private void checkNameComponentCount(
    final MPackageName id)
  {
    if (id.name().segments().size() < 3) {
      this.attribute("RuleURI", APP_ID_RULE);
      this.attribute("ID", id);
      this.error(
        "flatpak.app_id.components",
        "Package name must have at least 3 components."
      );
    }
  }
}
