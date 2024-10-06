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


package com.io7m.montarre.xml.internal;

import com.io7m.anethum.api.ParseSeverity;
import com.io7m.anethum.api.ParseStatus;
import com.io7m.anethum.api.ParsingException;
import com.io7m.blackthorne.core.BTException;
import com.io7m.blackthorne.core.BTParseError;
import com.io7m.blackthorne.core.BTPreserveLexical;
import com.io7m.blackthorne.jxe.BlackthorneJXE;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXEXInclude;
import com.io7m.montarre.api.MLongDescription;
import com.io7m.montarre.api.parsers.MLongDescriptionParserType;
import com.io7m.montarre.schema.MSchemas;
import com.io7m.montarre.xml.internal.v1.Mx1LongDescription;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.io7m.montarre.xml.internal.v1.Mx1.element;

/**
 * Long description parser.
 */

public final class MLongDescriptionParser implements
  MLongDescriptionParserType
{
  private final JXEHardenedSAXParsers context;
  private final URI source;
  private final InputStream stream;
  private final Consumer<ParseStatus> statusConsumer;

  /**
   * Long description parser.
   *
   * @param inContext        The SAX parsers
   * @param inSource         The source
   * @param inStatusConsumer The status consume
   * @param inStream         The input stream
   */

  public MLongDescriptionParser(
    final JXEHardenedSAXParsers inContext,
    final URI inSource,
    final InputStream inStream,
    final Consumer<ParseStatus> inStatusConsumer)
  {
    this.context =
      Objects.requireNonNull(inContext, "context");
    this.source =
      Objects.requireNonNull(inSource, "source");
    this.stream =
      Objects.requireNonNull(inStream, "stream");
    this.statusConsumer =
      Objects.requireNonNull(inStatusConsumer, "statusConsumer");
  }

  @Override
  public MLongDescription execute()
    throws ParsingException
  {
    try {
      final MLongDescription mpack =
        BlackthorneJXE.parseAll(
          this.source,
          this.stream,
          Map.ofEntries(
            Map.entry(
              element("LongDescription"),
              Mx1LongDescription::new
            )
          ),
          this.context,
          Optional.empty(),
          JXEXInclude.XINCLUDE_DISABLED,
          BTPreserveLexical.PRESERVE_LEXICAL_INFORMATION,
          MSchemas.schemas()
        );

      return mpack;
    } catch (final BTException e) {
      final var statuses =
        e.errors()
          .stream()
          .map(MLongDescriptionParser::mapParseError)
          .toList();

      for (final var status : statuses) {
        this.statusConsumer.accept(status);
      }

      throw new ParsingException(e.getMessage(), List.copyOf(statuses));
    }
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }

  private static ParseStatus mapParseError(
    final BTParseError error)
  {
    return ParseStatus.builder("parse-error", error.message())
      .withSeverity(mapSeverity(error.severity()))
      .withLexical(error.lexical())
      .build();
  }

  private static ParseSeverity mapSeverity(
    final BTParseError.Severity severity)
  {
    return switch (severity) {
      case ERROR -> ParseSeverity.PARSE_ERROR;
      case WARNING -> ParseSeverity.PARSE_WARNING;
    };
  }
}
