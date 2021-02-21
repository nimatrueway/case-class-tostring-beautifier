import scala.util.matching.Regex
import scala.util.control.Breaks._

object Parser {

  private val tokenPattern = "(\\()|(\\))|(,)|([^,()]+\\()|([^,()]+)".r

  def parse(input: String): INode = {
    val root = Node.NonLeaf("")
    var current = root
    var lastToken: Regex.Match = null

    breakable {
      for (token <- tokenPattern.findAllMatchIn(input)) {

        def fail = {
          val i = token.start
          val I = " " * i.toString.length
          val b = input.substring(Math.max(0, i - 10), i)
          val B = " " * b.length
          val a = input.substring(i, Math.min(i + 10, input.length))
          Console.err.println("Parsed so far: ")
          Console.err.println("")
          Console.err.println(Printer.toTree(root))
          Console.err.println("")
          throw new IllegalArgumentException(
            s"""
               |Illegal token starts at index $i : $b$a
               |                              $I   $B^                                   
          """.stripMargin)
        }

        lazy val lastTokenChar = input(lastToken.end - 1)

        if (lastToken == null && Fn.unapply(token.toString()) == None) { // cover any input that is not case-class
          root.newChild(Node.Leaf(input))
          break()
        }

        current = (current, token.toString()) match {
          case (n@Node.NonLeaf(_, _, _), Fn(fnName)) => // cover Fn(
            n.newChild(Node.NonLeaf(fnName))
            
          case (n@Node.NonLeaf(p, _, _), ",") => // cover Fn(X,
            if (p == null)
              fail
            if (lastTokenChar == ',' || // cover Fn(X,,Y)
              lastTokenChar == '(') // cover Fn(,X,Y) 
              n.newChild(Node.Leaf(""))
            n
            
          case (n@Node(_, _, _, p), ")") => // cover Fn(X..)
            if (p == null)
              fail
            if (lastTokenChar == ',') // cover Fn(X,Y,)
              n.newChild(Node.Leaf(""))
            if (p == root && token.end < input.length) { // cover redundant tail after first Fn(...)
              System.err.println(s"Warning: input had some redundant tail from ${token.end}: ${input.substring(token.end)}")
              break()
            }
            p
            
          case (n@Node.NonLeaf(_, _, _), name) => // cover Fn(X
            n.withNewChild(Node.Leaf(name))
            
          case _ =>
            fail
        }

        lastToken = token
      }
    }

    root.children.head
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

  object Fn {
    def unapply(input: String): Option[(String)] =
      if (input.endsWith("(")) Some(input.init) else None
  }

}
