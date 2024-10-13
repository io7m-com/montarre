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


package com.io7m.montarre.nativepack.internal;

import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.validation.MValidationIssue;
import com.io7m.montarre.api.validation.MValidationKind;
import com.io7m.montarre.api.wix.MWiXValidatorType;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A WiX validator.
 */

public final class MWiXValidator
  implements MWiXValidatorType, ErrorHandler
{
  private final Path file;
  private final ArrayList<MValidationIssue> errors;

  /**
   * A WiX validator.
   *
   * @param inFile The file
   */

  public MWiXValidator(
    final Path inFile)
  {
    this.file =
      Objects.requireNonNull(inFile, "file");
    this.errors =
      new ArrayList<>();
  }

  @Override
  public List<MValidationIssue> execute()
    throws MException
  {
    this.errors.clear();

    try {
      final var factory =
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      final var schemaFile =
        new StreamSource(schema());
      final var schema =
        factory.newSchema(schemaFile);
      final var validator =
        schema.newValidator();

      try (final var input = Files.newInputStream(this.file)) {
        validator.setErrorHandler(this);
        validator.validate(new StreamSource(input));
      }

      return List.copyOf(this.errors);
    } catch (final SAXException e) {
      throw new MException(e.getMessage(), e, "error-sax");
    } catch (final IOException e) {
      throw new MException(e.getMessage(), e, "error-io");
    }
  }

  private static InputStream schema()
  {
    return MWiXValidator.class.getResourceAsStream(
      "/com/io7m/montarre/nativepack/internal/wix.xsd"
    );
  }

  @Override
  public void close()
    throws MException
  {

  }

  @Override
  public void warning(
    final SAXParseException exception)
    throws SAXException
  {
    this.errors.add(
      new MValidationIssue(
        MValidationKind.WARNING,
        "error-validation-warning",
        exception.getMessage(),
        Map.ofEntries(
          Map.entry("Line", Integer.toString(exception.getLineNumber())),
          Map.entry("Column", Integer.toString(exception.getColumnNumber()))
        )
      )
    );
  }

  @Override
  public void error(
    final SAXParseException exception)
    throws SAXException
  {
    this.errors.add(
      new MValidationIssue(
        MValidationKind.ERROR,
        "error-validation-error",
        exception.getMessage(),
        Map.ofEntries(
          Map.entry("Line", Integer.toString(exception.getLineNumber())),
          Map.entry("Column", Integer.toString(exception.getColumnNumber()))
        )
      )
    );
  }

  @Override
  public void fatalError(
    final SAXParseException exception)
    throws SAXException
  {
    this.errors.add(
      new MValidationIssue(
        MValidationKind.ERROR,
        "error-validation-error",
        exception.getMessage(),
        Map.ofEntries(
          Map.entry("Line", Integer.toString(exception.getLineNumber())),
          Map.entry("Column", Integer.toString(exception.getColumnNumber()))
        )
      )
    );

    throw exception;
  }
}
