package com.novocode.squery.combinator

import com.novocode.squery.session.{PositionedResult, PositionedParameters, TypeMapper}
import com.novocode.squery.session.TypeMapper._


sealed trait TableBase[T] extends Column[T] {
  def join[U <: TableBase.T_](other: U) = new Join[this.type, U](this, other)
  override def isNamedTable = true
}

object TableBase {
  type T_ = TableBase[_]
}

abstract class Table[T](val tableName: String) extends TableBase[T] with ConvertibleColumn[T] {

  def nodeChildren = Nil
  override def toString = "Table " + tableName

  val O = ColumnOption

  def column[C](n: String, options: ColumnOption[C]*)(implicit tm: TypeMapper[C]) = new NamedColumn[C](Node(this), n, tm, options:_*)

  def * : ConvertibleColumn[T]

  def getResult(rs: PositionedResult) = *.getResult(rs)
  def getResultOption(rs: PositionedResult) = *.getResultOption(rs)
  def setParameter(ps: PositionedParameters, value: Option[T]) = *.setParameter(ps, value)
}

object Table {
  final case class Alias(child: Node) extends UnaryNode {
    override def toString = "Table.Alias"
    override def isNamedTable = true
  }
}

final class Join[+T1 <: TableBase.T_, +T2 <: TableBase.T_](_left: T1, _right: T2) extends TableBase[Nothing] {
  def left = _left.withOp(Join.JoinPart(Node(_left), Node(this)))
  def right = _right.withOp(Join.JoinPart(Node(_right), Node(this)))
  def nodeChildren = Node(_left) :: Node(_right) :: Nil
  override def toString = "Join(" + Node(_left) + "," + Node(_right) + ")"

  //def on(preds: BooleanColumn) = this
}

object Join {
  def unapply[T1 <: TableBase.T_, T2 <: TableBase.T_](j: Join[T1, T2]) = Some((j.left, j.right))

  final case class JoinPart(left: Node, right: Node) extends BinaryNode {
    override def toString = "JoinPart"
    override def nodeChildrenNames = Stream("table", "from")
  }
}

class Union[T <: Column.T_](val all: Boolean, query1: Query[T], query2: Query[T]) extends TableBase[Nothing] {
  val left = Node(query1)
  val right = Node(query2)
  def nodeChildren = left :: right :: Nil
}

object Union {
  //def unapply(u: Union[_]) = Some((u.all, u.left, u.right))

  final case class UnionPart(child: Node) extends UnaryNode {
    override def toString = "UnionPart"
  }
}
