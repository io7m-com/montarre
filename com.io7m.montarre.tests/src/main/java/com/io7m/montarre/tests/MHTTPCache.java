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

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class MHTTPCache
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MHTTPCache.class);

  private final MVStore store;
  private final MVMap<URI, byte[]> cache;

  public MHTTPCache(
    final Path file)
  {
    this.store =
      new MVStore.Builder()
        .fileName(file.toAbsolutePath().toString())
        .open();

    this.cache =
      this.store.openMap("cache");

    LOG.debug("Cache: {}", file.toAbsolutePath());
  }

  public Optional<byte[]> get(
    final URI uri)
  {
    final var value = this.cache.get(uri);
    if (value == null) {
      LOG.debug("Cache miss: {}", uri);
    } else {
      LOG.debug("Cache hit: {} ({} octets)", uri, value.length);
    }
    return Optional.ofNullable(value);
  }

  public void put(
    final URI uri,
    final byte[] data)
  {
    LOG.debug("Cache put: {} ({} octets)", uri, data.length);
    this.cache.put(
      Objects.requireNonNull(uri, "uri"),
      Objects.requireNonNull(data, "data")
    );
    this.store.commit();
  }
}
