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


package com.io7m.montarre.api.natives;

import com.io7m.montarre.api.MException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An interface for running external native processes.
 */

public interface MNativeProcessesType
  extends AutoCloseable
{
  /**
   * Execute a process, waiting for it to complete, and return the exit code.
   *
   * @param environment   The environment
   * @param standardOut   The standard out log
   * @param standardError The standard error log
   * @param commandLine   The command line
   *
   * @return The exit code
   *
   * @throws MException           On errors
   * @throws InterruptedException On interruption
   */

  int executeAndWait(
    Map<String, String> environment,
    ConcurrentLinkedQueue<String> standardOut,
    ConcurrentLinkedQueue<String> standardError,
    List<String> commandLine)
    throws MException, InterruptedException;

  @Override
  void close()
    throws MException;
}
