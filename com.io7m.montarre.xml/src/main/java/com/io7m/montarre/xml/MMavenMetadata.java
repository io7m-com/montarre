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


package com.io7m.montarre.xml;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Functions to extract version information from Maven Metadata.
 *
 * @see "https://maven.apache.org/repositories/metadata.html"
 */

public final class MMavenMetadata
{
  private static final DocumentBuilderFactory DOCUMENT_BUILDERS =
    DocumentBuilderFactory.newDefaultInstance();
  private static final XPathFactory XPATH =
    XPathFactory.newInstance();

  private MMavenMetadata()
  {

  }

  /**
   * Information about a snapshot version.
   *
   * @param baseVersion The base version
   * @param timestamp   The timestamp
   * @param buildNumber The build number
   */

  public record SnapshotVersion(
    String baseVersion,
    String timestamp,
    String buildNumber)
  {
    /**
     * Information about a snapshot version.
     */

    public SnapshotVersion
    {
      Objects.requireNonNull(baseVersion, "baseVersion");
      Objects.requireNonNull(timestamp, "timestamp");
      Objects.requireNonNull(buildNumber, "buildNumber");
    }
  }

  /**
   * Extract a snapshot version from the given metadata stream.
   *
   * @param stream The stream
   *
   * @return The snapshot version
   *
   * @throws IOException On errors
   */

  public static SnapshotVersion extractSnapshotVersion(
    final InputStream stream)
    throws IOException
  {
    try {
      final var builder =
        DOCUMENT_BUILDERS.newDocumentBuilder();
      final var document =
        builder.parse(stream);

      final var xPath =
        XPATH.newXPath();

      final var timestamp =
        (String) xPath.evaluate(
          "/metadata/versioning/snapshot/timestamp",
          document,
          XPathConstants.STRING
        );

      final var buildNumber =
        (String) xPath.evaluate(
          "/metadata/versioning/snapshot/buildNumber",
          document,
          XPathConstants.STRING
        );

      final var version =
        (String) xPath.evaluate(
          "/metadata/version",
          document,
          XPathConstants.STRING
        );

      return new SnapshotVersion(version, timestamp, buildNumber);
    } catch (final ParserConfigurationException
                   | XPathExpressionException
                   | SAXException e) {
      throw new IOException(e);
    }
  }
}
