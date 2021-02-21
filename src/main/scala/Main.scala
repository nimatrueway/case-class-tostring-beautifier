import java.nio.file.{Files, Paths}
import java.util.{Scanner, StringTokenizer}
import java.util.regex.{MatchResult, Pattern}
import scala.jdk.CollectionConverters._
import scala.collection.immutable.Queue
import scala.io.{Codec, Source, StdIn}
import scala.util.Try
import scala.util.matching.Regex

object Main:

  /**
   * Usages:
   *
   * 1. case-class-string-beautifier "file.txt"
   * 2. cat "file.txt" > case-class-string-beautifier -
   * 3. case-class-string-beautifier -s $(cat "file.txt")
   */
  def main(args: Array[String]): Unit = {
    val content = args.toList match {
      case "-" :: Nil => Source.fromInputStream(System.in)
      case "-s" :: tail => tail
      case file :: _ => Source.fromFile(file)
      case _ => throw IllegalArgumentException("First argument must be either a 'file' or '-' (stdin) or '-s' with string content followed by it.")
    }
    val output = Parser.parse(content.mkString)
    println(Printer.toTree(output))
  }
