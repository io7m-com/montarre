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

import com.io7m.montarre.xml.MMavenMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MMavenMetadataTest
{
  private Path file;

  private void resource(
    final String resourceName,
    final Path output)
    throws IOException
  {
    final var file =
      "/com/io7m/montarre/tests/" + resourceName;

    try (final var stream = MWiXWriterTest.class.getResourceAsStream(file)) {
      Files.write(output, stream.readAllBytes());
    }
  }

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
    throws Exception
  {
    this.file = directory.resolve("metadata.xml");
    this.resource("maven-metadata-0.xml", this.file);
  }

  @Test
  public void testVersion()
    throws IOException
  {
    try (final var stream = Files.newInputStream(this.file)) {
      final var version =
        MMavenMetadata.extractSnapshotVersion(stream);

      assertEquals("0.0.5-SNAPSHOT", version.baseVersion());
      assertEquals("3", version.buildNumber());
      assertEquals("20241203.164014", version.timestamp());
    }
  }
}
