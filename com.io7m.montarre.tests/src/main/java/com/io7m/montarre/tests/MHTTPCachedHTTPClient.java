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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

final class MHTTPCachedHTTPClient
  extends HttpClient
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MHTTPCachedHTTPClient.class);

  private final HttpClient delegate;

  public MHTTPCachedHTTPClient(
    final HttpClient delegate)
  {
    this.delegate = delegate;
  }

  @Override
  public Optional<Authenticator> authenticator()
  {
    return this.delegate.authenticator();
  }

  @Override
  public boolean awaitTermination(Duration duration)
    throws InterruptedException
  {
    return this.delegate.awaitTermination(duration);
  }

  @Override
  public void close()
  {
    this.delegate.close();
  }

  @Override
  public Optional<Duration> connectTimeout()
  {
    return this.delegate.connectTimeout();
  }

  @Override
  public Optional<CookieHandler> cookieHandler()
  {
    return this.delegate.cookieHandler();
  }

  @Override
  public Optional<Executor> executor()
  {
    return this.delegate.executor();
  }

  @Override
  public Redirect followRedirects()
  {
    return this.delegate.followRedirects();
  }

  @Override
  public boolean isTerminated()
  {
    return this.delegate.isTerminated();
  }

  @Override
  public WebSocket.Builder newWebSocketBuilder()
  {
    return this.delegate.newWebSocketBuilder();
  }

  @Override
  public Optional<ProxySelector> proxy()
  {
    return this.delegate.proxy();
  }

  @Override
  public <T> HttpResponse<T> send(
    final HttpRequest request,
    final HttpResponse.BodyHandler<T> responseBodyHandler)
    throws IOException, InterruptedException
  {
    try {
      return this.sendAsync(request, responseBodyHandler).get();
    } catch (final ExecutionException e) {
      throw new IOException(e);
    }
  }

  @Override
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(
    final HttpRequest request,
    final HttpResponse.BodyHandler<T> responseBodyHandler)
  {
    final var cache =
      MHTTPCacheFixture.get();

    final var existing =
      cache.get(request.uri());

    if (existing.isPresent()) {
      LOG.debug("{}: Returning response from cache", request.uri());
      return this.responseOf(
        request,
        responseBodyHandler,
        200,
        existing.get()
      );
    }

    try {
      LOG.debug("{}: Executing real request", request.uri());

      final var r =
        this.delegate.send(request, HttpResponse.BodyHandlers.ofByteArray());

      if (r.statusCode() >= 400) {
        return this.responseOf(
          request,
          responseBodyHandler,
          r.statusCode(),
          new byte[0]
        );
      }

      cache.put(request.uri(), r.body());
      return this.responseOf(request, responseBodyHandler, 200, r.body());
    } catch (final Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  private <T> CompletableFuture<HttpResponse<T>> responseOf(
    final HttpRequest request,
    final HttpResponse.BodyHandler<T> responseBodyHandler,
    final int status,
    final byte[] data)
  {
    final var responseInfo =
      this.responseInfoOf(status, data);

    final var subscriber =
      responseBodyHandler.apply(responseInfo);
    subscriber.onNext(List.of(ByteBuffer.wrap(data)));
    subscriber.onComplete();

    return subscriber.getBody()
      .thenApply(x -> (HttpResponse<T>) new MHTTPCannedResponse<>(x, request))
      .toCompletableFuture();
  }

  private HttpResponse.ResponseInfo responseInfoOf(
    final int status,
    final byte[] data)
  {
    return new HttpResponse.ResponseInfo()
    {
      @Override
      public int statusCode()
      {
        return status;
      }

      @Override
      public HttpHeaders headers()
      {
        return HttpHeaders.of(
          Map.ofEntries(
            Map.entry(
              "Content-Length",
              List.of(Long.toUnsignedString(data.length)))
          ),
          (s0, s1) -> true
        );
      }

      @Override
      public Version version()
      {
        return Version.HTTP_1_1;
      }
    };
  }

  @Override
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(
    final HttpRequest request,
    final HttpResponse.BodyHandler<T> responseBodyHandler,
    final HttpResponse.PushPromiseHandler<T> pushPromiseHandler)
  {
    return this.delegate.sendAsync(
      request,
      responseBodyHandler,
      pushPromiseHandler
    );
  }

  @Override
  public void shutdown()
  {
    this.delegate.shutdown();
  }

  @Override
  public void shutdownNow()
  {
    this.delegate.shutdownNow();
  }

  @Override
  public SSLContext sslContext()
  {
    return this.delegate.sslContext();
  }

  @Override
  public SSLParameters sslParameters()
  {
    return this.delegate.sslParameters();
  }

  @Override
  public Version version()
  {
    return this.delegate.version();
  }
}
