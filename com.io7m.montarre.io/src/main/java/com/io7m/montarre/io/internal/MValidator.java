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

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MPackageDeclaration;
import com.io7m.montarre.api.validation.MValidationIssue;
import com.io7m.montarre.api.validation.MValidatorType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A validator.
 */

public final class MValidator implements MValidatorType
{
  private final MPackageDeclaration declaration;
  private final List<MCheckType> checks;

  /**
   * A validator.
   *
   * @param inDeclaration The package
   */

  public MValidator(
    final MPackageDeclaration inDeclaration)
  {
    this.declaration =
      Objects.requireNonNull(inDeclaration, "declaration");

    this.checks =
      Stream.of(
          new MCheckApplicationID(),
          new MCheckDescription(),
          new MCheckIcons(),
          new MCheckName(),
          new MCheckSummary(),
          new MCheckScreenshots()
        )
        .map(MCheckType.class::cast)
        .sorted(Comparator.comparing(o -> o.getClass().getName()))
        .toList();
  }

  @Override
  public List<MValidationIssue> execute()
  {
    return this.checks.stream()
      .flatMap(c -> c.execute(this.declaration).stream())
      .toList();
  }

  @Override
  public void close()
    throws MException
  {

  }
}
