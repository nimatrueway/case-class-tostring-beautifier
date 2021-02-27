package io.github.nimatrueway.caseclass

object Printer {

  def toTree(node: INode, indent: String = ""): String = {
    val args =
      if (node.isLeaf)
        ""
      else if (node.children.isEmpty)
        "()"
      else if (node.children.length == 1 && node.children.head.isLeaf)
        s"(${node.children.head.value})"
      else
        s"""(
           |${node.children.map(n => Printer.toTree(n, indent + "  ")).mkString(",\n")}
           |$indent)""".stripMargin

    indent + node.value + args
  }

}