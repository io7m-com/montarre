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

import com.io7m.jaffirm.core.PreconditionViolationException;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.nativepack.MNativeProcesses;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MNativeProcessesTest
{
  @Test
  public void testFail()
  {
    final var p =
      new MNativeProcesses();
    final var ex =
      assertThrows(MException.class, () -> {
      p.executeAndWaitChecked(
        System.getenv(),
        new ConcurrentLinkedQueue<>(),
        new ConcurrentLinkedQueue<>(),
        List.of(
          "absolutely-not-a-real-program"
        )
      );
    });

    assertEquals("error-exec", ex.errorCode());
  }

  @Test
  public void testEmpty()
  {
    final var p =
      new MNativeProcesses();
    final var ex =
      assertThrows(PreconditionViolationException.class, () -> {
        p.executeAndWaitChecked(
          System.getenv(),
          new ConcurrentLinkedQueue<>(),
          new ConcurrentLinkedQueue<>(),
          List.of()
        );
      });
  }
}
