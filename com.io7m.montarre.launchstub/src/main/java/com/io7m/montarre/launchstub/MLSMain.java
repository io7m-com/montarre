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


package com.io7m.montarre.launchstub;

/**
 * The launcher stub.
 */

public final class MLSMain
{
  private static final String ARCH =
    MArchitectureNames.infer(System.getProperty("os.arch"));

  private static final String OS =
    MOperatingSystemNames.infer(System.getProperty("os.name"));

  private static final boolean IS_WINDOWS =
    MOperatingSystemNames.windows().equals(OS);

  private static final String FILE_SEPARATOR =
    System.getProperty("file.separator");

  private static final String PATH_SEPARATOR =
    System.getProperty("path.separator");

  private MLSMain()
  {

  }

  /**
   * The launcher stub.
   *
   * @param args The command-line arguments
   */

  public static void main(
    final String[] args)
  {
    if (args.length < 1) {
      System.err.println("Usage: get-arch | get-os | check-java-version | get-module-path");
      throw new IllegalArgumentException("Arguments required!");
    }

    switch (args[0].toUpperCase()) {
      case "GET-ARCH": {
        doArch();
        return;
      }
      case "GET-OS": {
        doOS();
        return;
      }
      case "CHECK-JAVA-VERSION": {
        doCheckJava(args[1]);
        return;
      }
      case "GET-MODULE-PATH": {
        doModulePath(args[1]);
        return;
      }
      default: {
        throw new IllegalArgumentException(
          String.format("Unrecognized command '%s'.", args[0])
        );
      }
    }
  }

  private static void doCheckJava(
    final String version)
  {
    final int versionRequired =
      Integer.parseUnsignedInt(version);

    final int versionActual =
      Runtime.version()
        .version()
        .get(0);

    final String versionString =
      Runtime.version()
        .toString();

    if (Integer.compareUnsigned(versionActual, versionRequired) < 0) {
      throw new RuntimeException(
        String.format(
          "At least Java %s is required, but this Java runtime is version %s",
          Integer.toUnsignedString(versionRequired),
          versionString
        )
      );
    }
  }

  private static void doModulePath(
    final String home)
  {
    final StringBuilder path = new StringBuilder();
    path.append(home);
    path.append(FILE_SEPARATOR);
    path.append("lib");
    path.append(PATH_SEPARATOR);

    path.append(home);
    path.append(FILE_SEPARATOR);
    path.append("lib");
    path.append(FILE_SEPARATOR);
    path.append(ARCH);
    path.append(FILE_SEPARATOR);
    path.append(OS);

    System.out.println(path);
  }

  private static void doOS()
  {
    System.out.println(OS);
  }

  private static void doArch()
  {
    System.out.println(ARCH);
  }
}
