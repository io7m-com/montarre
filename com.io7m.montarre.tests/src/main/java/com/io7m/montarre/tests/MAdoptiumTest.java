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

import com.io7m.montarre.adoptium.MEARuntimeSearch;
import com.io7m.montarre.adoptium.MEAdoptiumConfiguration;
import com.io7m.montarre.adoptium.MEAdoptiumFactory;
import com.io7m.montarre.adoptium.MEAdoptiumType;
import com.io7m.montarre.adoptium.METImageKind;
import com.io7m.montarre.api.MArchitectureName;
import com.io7m.montarre.api.MException;
import com.io7m.montarre.api.MOperatingSystemName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MAdoptiumTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(MAdoptiumTest.class);

  private MEAdoptiumFactory adoptiums;
  private MEAdoptiumType adoptium;

  @BeforeEach
  public void setup()
  {
    this.adoptiums =
      new MEAdoptiumFactory();
    this.adoptium =
      this.adoptiums.createAdoptium(
        MEAdoptiumConfiguration.builder()
          .build()
      );
  }

  @AfterEach
  public void tearDown()
    throws MException
  {
    this.adoptium.close();
  }

  @Test
  public void testReleases_JDK_Linux_X86_64_21()
    throws Exception
  {
    final var r =
      this.adoptium.runtimes(
        MEARuntimeSearch.builder()
          .setArchitecture(MArchitectureName.x86_64())
          .setFeatureVersion(21)
          .setImageKind(METImageKind.JDK)
          .setOperatingSystem(MOperatingSystemName.linux())
          .build()
      );

    LOG.debug("{}", r);
    assertTrue(
      r.size() >= 4,
      "Release count %s must be >= 4".formatted(r.size())
    );
  }

  @Test
  public void testReleases_JRE_Linux_X86_64_21()
    throws Exception
  {
    final var r =
      this.adoptium.runtimes(
        MEARuntimeSearch.builder()
          .setArchitecture(MArchitectureName.x86_64())
          .setFeatureVersion(21)
          .setImageKind(METImageKind.JRE)
          .setOperatingSystem(MOperatingSystemName.linux())
          .build()
      );

    LOG.debug("{}", r);
    assertTrue(
      r.size() >= 4,
      "Release count %s must be >= 4".formatted(r.size())
    );
  }

  @Test
  public void testReleases_JDK_Windows_X86_64_21()
    throws Exception
  {
    final var r =
      this.adoptium.runtimes(
        MEARuntimeSearch.builder()
          .setArchitecture(MArchitectureName.x86_64())
          .setFeatureVersion(21)
          .setImageKind(METImageKind.JDK)
          .setOperatingSystem(MOperatingSystemName.windows())
          .build()
      );

    LOG.debug("{}", r);
    assertTrue(
      r.size() >= 4,
      "Release count %s must be >= 4".formatted(r.size())
    );
  }

  @Test
  public void testReleases_JRE_Windows_X86_64_21()
    throws Exception
  {
    final var r =
      this.adoptium.runtimes(
        MEARuntimeSearch.builder()
          .setArchitecture(MArchitectureName.x86_64())
          .setFeatureVersion(21)
          .setImageKind(METImageKind.JRE)
          .setOperatingSystem(MOperatingSystemName.windows())
          .build()
      );

    LOG.debug("{}", r);
    assertTrue(
      r.size() >= 4,
      "Release count %s must be >= 4".formatted(r.size())
    );
  }
}
