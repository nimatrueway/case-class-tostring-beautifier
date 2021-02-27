package io.github.nimatrueway.caseclass

import scala.collection.immutable.Queue

trait INode {
  var parent: INode
  val value: String
  var children: Seq[INode]
  val isLeaf: Boolean
}

case class Node private(val value: String, var children: Seq[INode] = Queue(), val isLeaf: Boolean = true, var parent: INode) extends INode {
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