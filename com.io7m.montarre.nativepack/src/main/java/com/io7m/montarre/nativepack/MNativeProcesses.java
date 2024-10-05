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


package com.io7m.montarre.nativepack;

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.natives.MNativeProcessesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The native processes interface.
 */

public final class MNativeProcesses
  implements MNativeProcessesType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MNativeProcesses.class);

  private final ExecutorService executor;
  private final CloseableCollectionType<ClosingResourceFailedException> resources;

  /**
   * The native processes interface.
   */

  public MNativeProcesses()
  {
    this.resources =
      CloseableCollection.create();
    this.executor =
      this.resources.add(Executors.newVirtualThreadPerTaskExecutor());
  }

  @Override
  public int executeAndWait(
    final Map<String, String> environment,
    final ConcurrentLinkedQueue<String> standardOut,
    final ConcurrentLinkedQueue<String> standardError,
    final List<String> commandLine)
    throws MException, InterruptedException
  {
    try {
      final var processBuilder = new ProcessBuilder();
      processBuilder.environment().clear();
      processBuilder.environment().putAll(environment);
      processBuilder.command(commandLine);
      final var process = processBuilder.start();

      this.executor.execute(() -> {
        try (var reader = process.errorReader()) {
          while (true) {
            final var line = reader.readLine();
            if (line == null) {
              break;
            }
            LOG.trace("stderr: {}", line);
          }
        } catch (final Exception e) {
          LOG.trace("", e);
        }
      });

      this.executor.execute(() -> {
        try (var reader = process.inputReader()) {
          while (true) {
            final var line = reader.readLine();
            if (line == null) {
              break;
            }
            LOG.trace("stdout: {}", line);
          }
        } catch (final Exception e) {
          LOG.trace("", e);
        }
      });

      return process.waitFor();
    } catch (final IOException e) {
      throw new MException(
        Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()),
        e,
        "error-exec"
      );
    }
  }

  @Override
  public void close()
    throws MException
  {
    try {
      this.resources.close();
    } catch (ClosingResourceFailedException e) {
      throw new MException(
        "One or more resources could not be closed.",
        e,
        "error-resource-close"
      );
    }
  }
}
