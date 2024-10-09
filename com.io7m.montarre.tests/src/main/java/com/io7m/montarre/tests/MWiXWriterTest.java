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

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.montarre.api.http.MHTTPClientFactoryType;
import com.io7m.montarre.api.io.MPackageReaderType;
import com.io7m.montarre.api.natives.MNativePackagerServiceType;
import com.io7m.montarre.api.natives.MNativeWorkspaceConfiguration;
import com.io7m.montarre.api.natives.MNativeWorkspaceType;
import com.io7m.montarre.io.MPackageReaders;
import com.io7m.montarre.nativepack.MNWorkspaces;
import com.io7m.montarre.nativepack.MNativeProcesses;
import com.io7m.montarre.nativepack.MWiXWriters;
import com.io7m.montarre.nativepack.internal.deb.MNPackagerDebProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MWiXWriterTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MWiXWriterTest.class);

  private Path inputMpk;
  private CloseableCollectionType<ClosingResourceFailedException> resources;
  private MPackageReaders readers;
  private MPackageReaderType reader;
  private MWiXWriters wix;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
    throws Exception
  {
    this.inputMpk =
      directory.resolve("input.mpk");
    this.resource(
      "com.io7m.montarre.distribution-0.0.1-SNAPSHOT.mpk",
      this.inputMpk
    );

    this.resources =
      CloseableCollection.create();
    this.readers =
      new MPackageReaders();
    this.wix =
      new MWiXWriters();

    this.reader =
      this.resources.add(this.readers.open(this.inputMpk));
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    this.resources.close();
  }

  @Test
  public void testExecute(
    final @TempDir Path directory,
    final @TempDir Path schemaDir)
    throws Exception
  {
   this.reader.unpackInto(directory);

   final var out =
     new ByteArrayOutputStream();

    try (final var w = this.wix.create(
      this.reader.packageDeclaration(),
      directory,
      out
    )) {
      w.execute();
    }

    this.validate(schemaDir, out.toByteArray());
  }

  private void validate(
    final @TempDir Path schemaDir,
    final byte[] byteArray)
    throws Exception
  {
    LOG.debug("Validating: {}", new String(byteArray, StandardCharsets.UTF_8));

    final var schemaPath =
      schemaDir.resolve("wix.xsd");

    this.resource("wix.xsd", schemaPath);

    final var factory =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final var schemaFile =
      new StreamSource(Files.newInputStream(schemaPath));
    final var schema =
      factory.newSchema(schemaFile);
    final var validator =
      schema.newValidator();

    validator.validate(
      new StreamSource(new ByteArrayInputStream(byteArray))
    );
  }

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
}
