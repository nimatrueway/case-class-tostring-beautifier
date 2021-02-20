import java.nio.file.{Files, Paths}
import java.util.{Scanner, StringTokenizer}
import java.util.regex.{MatchResult, Pattern}
import scala.jdk.CollectionConverters._
import scala.collection.immutable.Queue
import scala.util.Try
import scala.util.matching.Regex

object Main:

  def main(args: Array[String]): Unit = {
    val content = if (args.length > 0) args.mkString(" ") else throw IllegalArgumentException("First argument must be the case-class toString!")
    val output = Parser.parse(content)
    println(Printer.toTree(output))
  }
