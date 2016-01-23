package com.liferay.osgi.shell

import scala.collection.immutable
import scala.collection.immutable.Iterable
import reflect.runtime.universe._
import reflect.runtime.currentMirror

/**
 * Created by carlos on 21/01/16.
 */
class EntityHelper {



  /**
   * Converts any object to a Map
   * This method should be overriden by every object/class that extends this class
   *
   * @param cc
   * @return
   */
  def toMap(cc: AnyRef): Map[String, Any] ={
    //TODO:find a better way to convert an object to a map
    val r = currentMirror.reflect(cc)
    r.symbol.typeSignature.members.filter(_.toString.contains(" get")).map{case s : TermSymbol => (r.symbol.fullName.toString,"\n")}.toMap
  }

  def printTable(table: Seq[AnyRef]) = println(Tabulator.format(getWholeTable(table)))

  def getWholeTable(listOfObjects: Seq[AnyRef]): List[Seq[Any]] ={
    val bodymatrix = listOfObjects.map(toMap).map { _.map { case (k, v) => v } }.map(_.to[Seq]).toList
    val headers = listOfObjects.map(toMap).map { _.map { case (k, v) => k } }.map(_.to[Seq]).head
    headers::bodymatrix
  }
}

object Tabulator {
  def format(table: Seq[Seq[Any]]): String = table match {
    case Seq() => ""
    case _ =>
      val sizes = for (row <- table) yield (for (cell <- row) yield if (cell == null) 0 else cell.toString.length)
      val colSizes = for (col <- sizes.transpose) yield col.max
      val rows = for (row <- table) yield formatRow(row, colSizes)
      formatRows(rowSeparator(colSizes), rows)
  }

  def formatRows(rowSeparator: String, rows: Seq[String]): String = (
    rowSeparator ::
    rows.head ::
    rowSeparator ::
    rows.tail.toList :::
    rowSeparator ::
    List()).mkString("\n")

  def formatRow(row: Seq[Any], colSizes: Seq[Int]) = {
    val cells = (for ((item, size) <- row.zip(colSizes)) yield if (size == 0) "" else ("%" + size + "s").format(item))
    cells.mkString("|", "|", "|")
  }

  def rowSeparator(colSizes: Seq[Int]) = colSizes map { "-" * _ } mkString("+", "+", "+")
}
