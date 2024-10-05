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


package com.io7m.montarre.tests;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

final class MHTTPCannedResponse<T> implements HttpResponse<T>
{
  private final T body;
  private final HttpRequest request;

  public MHTTPCannedResponse(
    final T body,
    final HttpRequest request)
  {
    this.body = body;
    this.request = request;
  }

  @Override
  public int statusCode()
  {
    return 200;
  }

  @Override
  public HttpRequest request()
  {
    return this.request;
  }

  @Override
  public Optional<HttpResponse<T>> previousResponse()
  {
    return Optional.empty();
  }

  @Override
  public HttpHeaders headers()
  {
    return HttpHeaders.of(
      Map.of(),
      (s0, s1) -> true
    );
  }

  @Override
  public T body()
  {
    return this.body;
  }

  @Override
  public Optional<SSLSession> sslSession()
  {
    return Optional.empty();
  }

  @Override
  public URI uri()
  {
    return this.request.uri();
  }

  @Override
  public HttpClient.Version version()
  {
    return HttpClient.Version.HTTP_1_1;
  }
}
