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

/**
 * Application packaging tools (Test suite).
 */

open module com.io7m.montarre.tests
{
  requires org.junit.jupiter.api;
  requires org.junit.jupiter.engine;
  requires org.junit.platform.commons;
  requires org.junit.platform.engine;
  requires org.junit.platform.launcher;

  requires nl.jqno.equalsverifier;
  requires net.jqwik.api;
  requires net.jqwik.engine;

  requires com.io7m.montarre.api;
  requires com.io7m.montarre.cmdline;
  requires com.io7m.montarre.io;
  requires com.io7m.montarre.nativepack;
  requires com.io7m.montarre.xml;
  requires com.io7m.montarre.launchstub;
  requires com.io7m.montarre.adoptium;

  requires com.h2database.mvstore;
  requires com.io7m.anethum.api;
  requires com.io7m.jaffirm.core;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.lanark.core;
  requires com.io7m.quarrel.core;
  requires com.io7m.quarrel.ext.xstructural;
  requires com.io7m.quixote.core;
  requires com.io7m.streamtime.core;
  requires com.io7m.verona.core;
  requires io.helidon.webserver;
  requires java.net.http;
  requires java.xml;
  requires org.apache.commons.compress;
  requires org.apache.commons.lang3;

  exports com.io7m.montarre.tests;
}
