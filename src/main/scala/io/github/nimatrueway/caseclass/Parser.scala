package io.github.nimatrueway.caseclass

import scala.util.control.Breaks.{break, breakable}
import scala.util.matching.Regex

object Parser {

  private val tokenPattern = "(\\()|(\\))|(,)|([^,()]+\\()|([^,()]+)".r

  def parse(input: String): ParseResult = {
    val root = Node.NonLeaf("")
    var current = root
    var lastToken: Regex.Match = null
    var errors: List[String] = Nil

    breakable {
      for (token <- tokenPattern.findAllMatchIn(input)) {

        def fail(): Nothing = {
          val i = token.start
          val I = " " * i.toString.length
          val b = input.substring(Math.max(0, i - 10), i)
          val B = " " * b.length
          val a = input.substring(i, Math.min(i + 10, input.length))
          errors = errors :+
            s"""
               |Illegal token starts at index $i : $b$a
               |                              $I   $B^
            """.stripMargin
          break()
        }

        lazy val lastTokenChar = input(lastToken.end - 1)

        if (lastToken == null && Fn.unapply(token.toString()).isEmpty) { // cover any input that is not case-class
          root.newChild(Node.Leaf(input))
          break()
        }

        current = (current, token.toString()) match {
          case (n@Node.NonLeaf(_, _, _), Fn(fnName)) => // cover Fn(
            n.newChild(Node.NonLeaf(fnName))

          case (n@Node.NonLeaf(p, _, _), ",") => // cover Fn(X,
            if (p == null)
              fail()
            if (lastTokenChar == ',' || // cover Fn(X,,Y)
              lastTokenChar == '(') // cover Fn(,X,Y) 
              n.newChild(Node.Leaf(""))
            n

          case (n@Node(_, _, _, p), ")") => // cover Fn(X..)
            if (p == null)
              fail()
            if (lastTokenChar == ',') // cover Fn(X,Y,)
              n.newChild(Node.Leaf(""))
            if (p == root && token.end < input.length) { // cover redundant tail after first Fn(...)
              val sampleText = input.substring(token.end, Math.min(token.end + 30, input.length))
              errors = errors :+ s"Warning: input had some redundant tail from ${token.end}: $sampleText"
              break()
            }
            p

          case (n@Node.NonLeaf(_, _, _), name) => // cover Fn(X
            n.withNewChild(Node.Leaf(name))

          case _ =>
            fail()
        }

        lastToken = token
      }
    }

    ParseResult(root.children.headOption, errors.toSeq)
  }

  implicit class RichNode(node: INode) {
    /**
     * Add new 'child' to me, and return the child
     */
    def newChild(child: INode): INode = {
      child.parent = node
      node.children = node.children :+ child
      child
    }

    /**
     * Add new 'child' to me, and return myself
     */
    def withNewChild(child: INode): INode = {
      child.parent = node
      node.children = node.children :+ child
      node
    }
  }

  case class ParseResult(
                          result: Option[INode],
                          errors: Seq[String]
                        )

  object Fn {
    def unapply(input: String): Option[(String)] =
      if (input.endsWith("(")) Some(input.init) else None
  }

}
