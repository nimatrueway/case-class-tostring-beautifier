import scala.util.matching.Regex

object Parser {

  private val tokenPattern = "(\\()|(\\))|(,)|([^,()]+\\()|([^,()]+)".r

  def parse(input: String): INode = {
    val root = Node.NonLeaf("")
    var current = root
    var lastToken: Regex.Match = null

    for (token <- tokenPattern.findAllMatchIn(input)) {
      def fail = {
        val i = token.start
        Printer.toTree(root)
        val before = input.substring(Math.max(0, i - 10), Math.min(i, input.length))
        val after = input.substring(Math.min(i, input.length), Math.min(i+10, input.length))
        throw new IllegalArgumentException(s"""
          |Illegal token starts at index $i :  $before$after
          |                                  ${" " * before.length}^                                    
        """.stripMargin)
      }

      lazy val lastTokenChar = input(lastToken.end - 1)

      current =
        (current, token.toString()) match {
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
            p
          case (n@Node.NonLeaf(_, _, _), name) => // cover Fn(X
            n.withNewChild(Node.Leaf(name))
          case _ =>
            fail
        }

      lastToken = token
    }

    val Seq(result) = root.children
    result.parent = null
    result
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
