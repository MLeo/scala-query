package com.novocode.squery.combinator

import com.novocode.squery.session.TypeMapper
import com.novocode.squery.session.TypeMapper._

object Operator {
  final case class Is(left: Node, right: Node) extends OperatorColumn[Boolean] with BinaryNode with BooleanColumnOps with BooleanLikeColumnOps
  final case class In(left: Node, right: Node) extends OperatorColumn[Boolean] with BinaryNode with BooleanColumnOps with BooleanLikeColumnOps
  final case class And(left: Node, right: Node) extends OperatorColumn[Boolean] with BinaryNode with BooleanColumnOps with BooleanLikeColumnOps
  final case class Or(left: Node, right: Node) extends OperatorColumn[Boolean] with BinaryNode with BooleanColumnOps with BooleanLikeColumnOps
  final case class Count(child: Node) extends OperatorColumn[Int] with UnaryNode
  final case class Max(child: Node) extends OperatorColumn[Int] with UnaryNode
  final case class Not(child: Node) extends OperatorColumn[Boolean] with UnaryNode with BooleanColumnOps

  final case class Ordering(left: Node, right: Node, desc: Boolean) extends BinaryNode {
    override def toString = "Ordering " + (if(desc) "desc" else "asc")
    override def nodeChildrenNames = Stream("expr", "by")
  }
}
