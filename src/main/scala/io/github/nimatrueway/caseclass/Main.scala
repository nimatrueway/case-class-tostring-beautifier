package io.github.nimatrueway.caseclass

import scopt.OParser

import java.io.File
import scala.io.Source
import scala.util.Using

object Main {

  import ArgumentProcessor.Args

  def main(args: Array[String]): Unit = {
    val source = ArgumentProcessor.process(args) match {
      case Args(_, _, true) => Source.fromInputStream(System.in)
      case Args(_, Some(content), _) => Source.fromString(content)
      case Args(Some(file), _, _) => Source.fromFile(file)
      case _ => throw new IllegalArgumentException("Unsupported mixture of cli arguments.")
    }
    Using.resource(source) { source =>
      val result = Parser.parse(source.mkString)
      if (result.errors.nonEmpty) {
        for (error <- result.errors) {
          Console.err.println(error)
        }
        Console.err.println()
      }
      for (result <- result.result) {
        Console.println(Printer.toTree(result))
      }
    }
  }


}

object ArgumentProcessor {

  def process(args: Array[String]): Args = {
    val builder = OParser.builder[Args]
    val argDefinitions = Seq(
      builder.head("Parses an output of Scala 'CaseCase.toString()' and attempts to beautify and indent it like a typical json beautifier."),
      builder.opt[String]("file")
        .abbr("f")
        .text("file name to read its content or use '-' to read from stdin")
        .action { case (path, args) => args.copy(file = Some(new File(path))) }
      ,
      builder.opt[String]("string")
        .abbr("s")
        .text("read content directly from argument")
        .action { case (c, args) => args.copy(content = Some(c)) }
      ,
      builder.opt[Unit]("stdin")
        .abbr("i")
        .optional()
        .text("read content from stdin")
        .action { case (_, args) => args.copy(isInputStream = true) }
    )
    val parser = OParser
      .sequence(
        builder.programName("case-class-string-beautifier"),
        argDefinitions: _*
      )
    OParser.parse(parser, args, Args()) match {
      case Some(args) if onlyOneOf(args.file.isDefined, args.content.isDefined, args.isInputStream) =>
        args
      case _ =>
        Console.println(OParser.usage(parser))
        throw new IllegalArgumentException("Invalid arguments!")
    }
  }

  private def onlyOneOf(input: Boolean*) = input.map(if (_) 1 else 0).sum == 1

  case class Args(
                   file: Option[File] = None,
                   content: Option[String] = None,
                   isInputStream: Boolean = false,
                 )

}