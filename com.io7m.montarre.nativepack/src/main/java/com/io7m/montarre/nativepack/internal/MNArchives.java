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

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.montarre.api.MShortName;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Functions to create archives.
 */

public final class MNArchives
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNArchives.class);

  private static final Instant SOURCE_EPOCH =
    Instant.parse("2010-01-01T00:00:00+00:00");
  private static final FileTime SOURCE_EPOCH_FILETIME =
    FileTime.from(SOURCE_EPOCH);

  private static final OpenOption[] OPEN_OPTIONS = {
    StandardOpenOption.WRITE,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  };

  private MNArchives()
  {

  }

  /**
   * Pack the given directory into an archive.
   *
   * @param inputDirectory The input directory
   * @param isExecutable   A function that determines if an entry name is executable
   * @param outputFile     The output file
   * @param shortName      The short name
   *
   * @return The output file
   *
   * @throws IOException                    On errors
   * @throws ClosingResourceFailedException On errors
   */

  public static Path packTar(
    final Path inputDirectory,
    final Path outputFile,
    final Predicate<String> isExecutable,
    final MShortName shortName)
    throws IOException, ClosingResourceFailedException
  {
    Objects.requireNonNull(inputDirectory, "inputDirectory");
    Objects.requireNonNull(outputFile, "outFile");
    Objects.requireNonNull(shortName, "shortName");

    LOG.info("Creating tar {}", outputFile);
    LOG.debug("Input directory: {}", inputDirectory);

    try (final var streams = CloseableCollection.create()) {
      final var fileStream =
        streams.add(Files.walk(inputDirectory));
      final var fileList =
        fileStream.sorted()
          .toList();

      final var fileOut =
        streams.add(Files.newOutputStream(outputFile, OPEN_OPTIONS));
      final var bufOut =
        streams.add(new BufferedOutputStream(fileOut, 65536));
      final var gzipOut =
        streams.add(new GZIPOutputStream(bufOut));
      final var tarOut =
        streams.add(new TarArchiveOutputStream(gzipOut));

      createTarEntries(
        inputDirectory,
        shortName.name(),
        isExecutable,
        tarOut,
        fileList
      );
    }

    return outputFile;
  }

  private static void createTarEntries(
    final Path inputDirectory,
    final String prefix,
    final Predicate<String> isExecutable,
    final TarArchiveOutputStream tarOut,
    final List<Path> fileList)
    throws IOException
  {
    for (final var file : fileList) {
      if (Files.isRegularFile(file)) {
        LOG.debug("[tar] {}", file);

        final var entryWithoutPrefix =
          inputDirectory.relativize(file)
            .toString();
        final var entryName =
          prefix + "/" + entryWithoutPrefix;

        final var entry =
          tarOut.createArchiveEntry(
            file,
            entryName
          );
        entry.setCreationTime(SOURCE_EPOCH_FILETIME);
        entry.setLastAccessTime(SOURCE_EPOCH_FILETIME);
        entry.setLastModifiedTime(SOURCE_EPOCH_FILETIME);
        entry.setUserId(0);
        entry.setGroupId(0);

        if (isExecutable.test(entryWithoutPrefix)) {
          entry.setMode(0755);
        } else {
          entry.setMode(0644);
        }
        tarOut.putArchiveEntry(entry);
        Files.copy(file, tarOut);
        tarOut.closeArchiveEntry();
      }
    }
  }

  /**
   * Pack the given directory into an archive.
   *
   * @param inputDirectory The input directory
   * @param outputFile     The output file
   * @param shortName      The short name
   *
   * @return The output file
   *
   * @throws IOException                    On errors
   * @throws ClosingResourceFailedException On errors
   */

  public static Path packZip(
    final Path inputDirectory,
    final Path outputFile,
    final MShortName shortName)
    throws IOException, ClosingResourceFailedException
  {
    Objects.requireNonNull(inputDirectory, "inputDirectory");
    Objects.requireNonNull(outputFile, "outFile");
    Objects.requireNonNull(shortName, "shortName");

    LOG.info("Creating zip {}", outputFile);
    LOG.debug("Input directory: {}", inputDirectory);

    try (final var streams = CloseableCollection.create()) {
      final var fileStream =
        streams.add(Files.walk(inputDirectory));
      final var fileList =
        fileStream.sorted()
          .toList();
      final var fileOut =
        streams.add(Files.newOutputStream(outputFile, OPEN_OPTIONS));
      final var bufOut =
        streams.add(new BufferedOutputStream(fileOut, 65536));
      final var zipOut =
        streams.add(new ZipOutputStream(bufOut));

      createZipEntries(inputDirectory, shortName.name(), zipOut, fileList);
    }

    return outputFile;
  }

  private static void createZipEntries(
    final Path inputDirectory,
    final String prefix,
    final ZipOutputStream zipOut,
    final List<Path> fileList)
    throws IOException
  {
    for (final var file : fileList) {
      if (Files.isRegularFile(file)) {
        LOG.debug("[zip] {}", file);

        final var entry =
          new ZipEntry(prefix + "/" + inputDirectory.relativize(file));

        entry.setCreationTime(SOURCE_EPOCH_FILETIME);
        entry.setLastAccessTime(SOURCE_EPOCH_FILETIME);
        entry.setLastModifiedTime(SOURCE_EPOCH_FILETIME);

        zipOut.putNextEntry(entry);
        Files.copy(file, zipOut);
      }
    }
  }
}
