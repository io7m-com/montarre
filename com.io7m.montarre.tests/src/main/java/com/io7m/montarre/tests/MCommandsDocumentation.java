/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


import com.io7m.montarre.cmdline.MConverters;
import com.io7m.montarre.cmdline.MMain;
import com.io7m.quarrel.core.QCommandGroupType;
import com.io7m.quarrel.core.QCommandOrGroupType;
import com.io7m.quarrel.core.QCommandParserConfiguration;
import com.io7m.quarrel.core.QCommandParsers;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QValueConverterDirectory;
import com.io7m.quarrel.ext.xstructural.QCommandXS;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

public final class MCommandsDocumentation
{
  private MCommandsDocumentation()
  {

  }

  public static void main(
    final String[] args)
  {
    final var main =
      new MMain(new String[0]);

    final var parsers =
      new QCommandParsers();
    final var xs =
      new QCommandXS("xs", true);

    final var parserConfig =
      new QCommandParserConfiguration(
        MConverters.get(),
        QCommandParsers.emptyResources()
      );

    final var commandsByName =
      main.application().commandTree();

    final var nameStack =
      new LinkedList<String>();

    final var commands =
      collectCommands(nameStack, commandsByName);

    for (final var command : commands) {
      writeMain(command, parsers, parserConfig, commandsByName, xs);
      writeParameters(command, parsers, parserConfig, commandsByName, xs);
    }
  }

  private record CommandWithFullName(
    List<String> name,
    QCommandType command)
  {

  }

  private static List<CommandWithFullName> collectCommands(
    final LinkedList<String> nameStack,
    final SortedMap<String, QCommandOrGroupType> commandsByName)
  {
    final var results = new ArrayList<CommandWithFullName>();
    for (final var q : commandsByName.values()) {
      nameStack.addLast(q.metadata().name());
      try {
        results.addAll(collectCommandsOne(nameStack, q));
      } finally {
        nameStack.removeLast();
      }
    }
    return List.copyOf(results);
  }

  private static List<CommandWithFullName> collectCommandsOne(
    final LinkedList<String> nameStack,
    final QCommandOrGroupType q)
  {
    return switch (q) {
      case final QCommandGroupType g -> {
        yield collectCommands(nameStack, g.commandTree());
      }
      case final QCommandType c -> {
        yield List.of(
          new CommandWithFullName(
            List.copyOf(nameStack),
            c
          )
        );
      }
    };
  }

  private static void writeMain(
    final CommandWithFullName commandFull,
    final QCommandParsers parsers,
    final QCommandParserConfiguration parserConfig,
    final SortedMap<String, QCommandOrGroupType> commandsByName,
    final QCommandXS xs)
  {
    try {
      final var parser =
        parsers.create(parserConfig);

      final var textWriter =
        new StringWriter();
      final var writer =
        new PrintWriter(textWriter);

      final var hyphenated =
        String.join("-", commandFull.name);

      final var arguments = new ArrayList<String>();
      arguments.add("--type");
      arguments.add("main");
      arguments.add("--parameters-include");
      arguments.add("scmd-%s-parameters.xml".formatted(hyphenated));
      arguments.addAll(commandFull.name);

      final var context =
        parser.execute(
          commandsByName,
          writer,
          xs,
          arguments
        );

      context.execute();
      writer.println();
      writer.flush();

      final var path =
        Paths.get("/shared-tmp/scmd-%s.xml".formatted(hyphenated));

      Files.writeString(path, textWriter.toString(), StandardCharsets.UTF_8);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void writeParameters(
    final CommandWithFullName commandFull,
    final QCommandParsers parsers,
    final QCommandParserConfiguration parserConfig,
    final SortedMap<String, QCommandOrGroupType> commandsByName,
    final QCommandXS xs)
  {
    try {
      final var command =
        commandFull.command;
      final var name =
        command.metadata().name();
      final var parser =
        parsers.create(parserConfig);

      final var textWriter =
        new StringWriter();
      final var writer =
        new PrintWriter(textWriter);

      final var hyphenated =
        String.join("-", commandFull.name);

      final var arguments = new ArrayList<String>();
      arguments.add("--type");
      arguments.add("parameters");
      arguments.add("--parameters-include");
      arguments.add("scmd-%s-parameters.xml".formatted(hyphenated));
      arguments.addAll(commandFull.name);

      final var context =
        parser.execute(
          commandsByName,
          writer,
          xs,
          arguments
        );

      context.execute();
      writer.println();
      writer.flush();

      final var path =
        Paths.get("/shared-tmp/scmd-%s-parameters.xml".formatted(hyphenated));

      Files.writeString(path, textWriter.toString(), StandardCharsets.UTF_8);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
