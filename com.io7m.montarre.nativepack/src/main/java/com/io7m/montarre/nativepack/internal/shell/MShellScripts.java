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


package com.io7m.montarre.nativepack.internal.shell;

import com.io7m.montarre.api.MJavaInfoType;
import com.io7m.montarre.api.MShortName;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Functions to generate launcher shell scripts.
 */

public final class MShellScripts
{
  private MShellScripts()
  {

  }

  /**
   * Generate the lines of a launcher shell script.
   *
   * @param javaInfo The Java info
   * @param name     The short name
   *
   * @return The lines
   */

  public static List<String> shellScript(
    final MJavaInfoType javaInfo,
    final MShortName name)
  {
    Objects.requireNonNull(javaInfo, "javaInfo");
    Objects.requireNonNull(name, "name");

    final var upperName =
      name.name()
        .toUpperCase(Locale.ROOT)
        .replace('-', '_');

    final var homeName =
      upperName + "_HOME";
    final var pathName =
      upperName + "_MODULE_PATH";

    final var out = new ArrayList<String>();
    out.add("#!/bin/sh");
    out.add("# Auto generated: Do not edit.");
    out.add("# This is a launch script for UNIX-like platforms.");
    out.add("");

    out.add("if [ -z \"${%s}\" ]".formatted(homeName));
    out.add("then");
    out.add("  echo \"%s is unset\" 1>&2".formatted(homeName));
    out.add("  exit 1");
    out.add("fi");
    out.add("");

    out.add("#");
    out.add("# Check that the available Java runtime is suitable.");
    out.add("#");
    out.add("");
    out.add("/usr/bin/env java -jar \"${%s}/bin/launch.jar\" \\"
              .formatted(homeName));
    out.add("  check-java-version %s || exit 1"
              .formatted(Long.toUnsignedString(javaInfo.requiredJDKVersion())));
    out.add("");

    out.add("#");
    out.add("# Build a module path. This is guaranteed to be:");
    out.add("#   ${%s}/lib".formatted(homeName));
    out.add("#   ${%s}/lib/${ARCH}/${OS}".formatted(homeName));
    out.add("#");
    out.add("");
    out.add("%s=$(/usr/bin/env java -jar \"${%s}/bin/launch.jar\" \\"
              .formatted(pathName, homeName));
    out.add("  get-module-path \"${%s}\") || exit 1"
              .formatted(homeName));
    out.add("");

    out.add("#");
    out.add("# Run the application.");
    out.add("#");
    out.add("");
    out.add("exec /usr/bin/env java \\");
    out.add("  -p \"${%s}\" \\".formatted(pathName));
    out.add("  -m %s \\".formatted(javaInfo.mainModule()));
    out.add("  \"$@\"");
    out.add("");

    return List.copyOf(out);
  }

  /**
   * Generate the text of a launcher shell script.
   *
   * @param javaInfo The Java info
   * @param name     The short name
   *
   * @return The text
   */

  public static String shellScriptText(
    final MJavaInfoType javaInfo,
    final MShortName name)
  {
    return String.join("\n", shellScript(javaInfo, name));
  }
}
