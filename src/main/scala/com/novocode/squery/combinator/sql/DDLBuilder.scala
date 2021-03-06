package com.novocode.squery.combinator.sql

import scala.collection.mutable.HashMap
import java.io.PrintWriter
import com.novocode.squery.combinator._

class DDLBuilder(table: Table[_]) {

  def buildCreateTable = {
    val b = new StringBuilder append "CREATE TABLE " append table.tableName append " ("
    var first = true
    def f(c: Any): Unit = c match {
      case p:Projection[_] =>
        for(i <- 0 until p.productArity)
          f(p.productElement(i))
      case t:Table[_] => f(t.*)
      case n:NamedColumn[_] =>
        if(first) first = false
        else b append ","
        b append n.name append ' '
        addSqlType(n, b)
      case _ => throw new SQueryException("Cannot use column "+c+" in CREATE TABLE statement")
    }
    f(table)
    b append ")" toString
  }

  private[this] def addSqlType(c: NamedColumn[_], sb: StringBuilder) {
    sb append new StringBuilder append DDLBuilder.typeNames.getOrElse(c.typeMapper.sqlType,
      throw new SQueryException("No SQL type name found for type mapper "+c.typeMapper))
    var notNull = false
    var autoIncrement = false
    var defaultLiteral:String = null
    for(o <- c.options) o match {
      case ColumnOption.NotNull => notNull = true
      case ColumnOption.AutoInc => autoIncrement = true
      case ColumnOption.Default(v) => defaultLiteral = c.asInstanceOf[NamedColumn[Any]].typeMapper.valueToSQLLiteral(v)
    }
    if(defaultLiteral ne null) sb append " DEFAULT " append defaultLiteral
    if(notNull) sb append " NOT NULL"
    if(autoIncrement) sb append " AUTO_INCREMENT"
  }
}

object DDLBuilder {
  private[DDLBuilder] lazy val typeNames = Map() ++
    (for(f <- classOf[java.sql.Types].getFields)
      yield f.get(null).asInstanceOf[Int] -> f.getName)
}
