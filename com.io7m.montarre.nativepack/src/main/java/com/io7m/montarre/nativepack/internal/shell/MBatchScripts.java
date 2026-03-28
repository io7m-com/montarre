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
 * Functions to generate launcher batch scripts.
 */

public final class MBatchScripts
{
  private MBatchScripts()
  {

  }

  /**
   * Generate the lines of a launcher batch script.
   *
   * @param javaInfo The Java info
   * @param name     The short name
   *
   * @return The lines
   */

  public static List<String> batchScript(
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
    out.add("@echo off");
    out.add("");

    out.add("REM Auto generated: Do not edit.");
    out.add("REM This is a launch script for Windows-like platforms.");
    out.add("");

    out.add("if NOT DEFINED %s (".formatted(homeName));
    out.add("  echo %s is unset".formatted(homeName));
    out.add("  exit /b 1");
    out.add(")");
    out.add("");

    out.add("REM Check that the available Java runtime is suitable.");
    out.add("java -jar \"%%%s%%/bin/launch.jar\" check-java-version 21".formatted(homeName));
    out.add("if %errorlevel% neq 0 exit /b 1");
    out.add("");

    out.add("REM Build a module path.");
    out.add("for /f %%%%i in ('java -jar %%%s%%\\bin\\launch.jar get-module-path %%%s%%') do set %s=%%%%i"
              .formatted(
                homeName,
                homeName,
                pathName
              ));
    out.add("if %errorlevel% neq 0 exit /b 1");
    out.add("");

    out.add("REM Run the application.");
    out.add("java -p %%%s%% -m %s %%*".formatted(pathName, javaInfo.mainModule()));
    out.add("if %errorlevel% neq 0 exit /b 1");
    return List.copyOf(out);
  }

  /**
   * Generate the text of a launcher batch script.
   *
   * @param javaInfo The Java info
   * @param name     The short name
   *
   * @return The text
   */

  public static String batchScriptText(
    final MJavaInfoType javaInfo,
    final MShortName name)
  {
    return String.join("\r\n", batchScript(javaInfo, name));
  }
}
