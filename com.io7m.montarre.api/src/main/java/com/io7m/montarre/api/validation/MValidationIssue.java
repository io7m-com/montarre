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


package com.io7m.montarre.api.validation;

import com.io7m.seltzer.api.SStructuredErrorType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A validation issue.
 *
 * @param kind       The error kind
 * @param errorCode  The error code
 * @param message    The message
 * @param attributes The attributes
 */

public record MValidationIssue(
  MValidationKind kind,
  String errorCode,
  String message,
  Map<String, String> attributes)
  implements SStructuredErrorType<String>
{
  /**
   * A validation issue.
   *
   * @param kind       The error kind
   * @param errorCode  The error code
   * @param message    The message
   * @param attributes The attributes
   */

  public MValidationIssue
  {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(errorCode, "errorCode");
    Objects.requireNonNull(message, "message");
    Objects.requireNonNull(attributes, "attributes");
  }

  @Override
  public Optional<String> remediatingAction()
  {
    return Optional.empty();
  }

  @Override
  public Optional<Throwable> exception()
  {
    return Optional.empty();
  }
}