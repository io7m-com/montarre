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
import com.io7m.montarre.api.validation.MValidationIssue;
import com.io7m.montarre.api.validation.MValidationKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An abstract check.
 */

public abstract class MCheckAbstract implements MCheckType
{
  private final HashMap<String, Object> attributes;
  private final ArrayList<MValidationIssue> errors;

  protected MCheckAbstract()
  {
    this.attributes = new HashMap<>();
    this.errors = new ArrayList<>();
  }

  @Override
  public final List<MValidationIssue> execute(
    final MPackageDeclaration declaration)
  {
    Objects.requireNonNull(declaration, "declaration");
    this.onExecute(declaration);
    return List.copyOf(this.errors);
  }

  protected abstract void onExecute(
    MPackageDeclaration declaration);

  protected final void error(
    final String errorCode,
    final String message)
  {
    this.errors.add(new MValidationIssue(
      MValidationKind.ERROR,
      errorCode,
      message,
      this.takeAttributes()
    ));
  }

  protected final void warning(
    final String errorCode,
    final String message)
  {
    this.errors.add(new MValidationIssue(
      MValidationKind.WARNING,
      errorCode,
      message,
      this.takeAttributes()
    ));
  }

  protected final void clearAttributes()
  {
    this.attributes.clear();
  }

  private Map<String, String> takeAttributes()
  {
    final var r =
      this.attributes.entrySet()
        .stream()
        .map(x -> Map.entry(x.getKey(), x.getValue().toString()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    this.clearAttributes();
    return r;
  }

  protected final void attribute(
    final String name,
    final Object value)
  {
    this.attributes.put(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(value, "value")
    );
  }
}
