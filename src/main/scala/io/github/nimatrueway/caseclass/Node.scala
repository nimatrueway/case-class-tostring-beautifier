package io.github.nimatrueway.caseclass

import scala.collection.immutable.Queue

trait INode {
  val value: String
  val isLeaf: Boolean
  var parent: INode
  var children: Seq[INode]
}

case class Node private(value: String, var children: Seq[INode] = Queue(), isLeaf: Boolean = true, var parent: INode) extends INode {
  override def toString: String =
    if (isLeaf) value else s"$value(${children.mkString(",")})"
}

object Node {

  object Leaf {
    def apply(value: String): INode = {
      Node(value = value, children = Queue(), isLeaf = true, parent = null)
    }

    def unapply(arg: INode): Option[(INode, String)] = {
      if (arg.isLeaf) Some(arg.parent, arg.value) else None
    }
  }

  object NonLeaf {
    def apply(value: String): INode = {
      Node(value = value, children = Queue(), isLeaf = false, parent = null)
    }

    def unapply(arg: INode): Option[(INode, String, Seq[INode])] = {
      if (arg.isLeaf) None else Some(arg.parent, arg.value, arg.children)
    }
  }

}