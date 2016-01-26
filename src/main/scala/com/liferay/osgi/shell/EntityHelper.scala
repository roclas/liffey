package com.liferay.osgi.shell

import scala.collection.immutable
import scala.collection.immutable.Iterable
import reflect.runtime.universe._
import reflect.runtime.currentMirror
import scala.util.Try

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
    cc.getClass.getDeclaredFields.flatMap(field =>{
      Try{
          field setAccessible true
          field.getName -> field.get(cc)
      }.toOption
    }).toMap
  }

  def printFilteredTable(table: Seq[AnyRef],filterList:List[String]) =
    println(Tabulator.format(getFilteredTable(table,filterList)))

  def printTable(table: Seq[AnyRef]) = println(Tabulator.format(getWholeTable(table)))

  def printTableHeader(table: Seq[AnyRef]) = println(Tabulator.format(getTableHeaders(table)))

  def getFilteredTable(listOfObjects: Seq[AnyRef],filterList:List[String]): List[Seq[Any]] ={
    //filter ONLY if necessary
    val t=listOfObjects.map(toMap)
    val filteredListOfObjects: Seq[Map[String, Any]] = {
      if(filterList.isEmpty)t
      else t.map{
        _.filter{
          case(k,v)=>if(filterList contains k)true else false
          case _=>false
        }
      }
    }
    //create the table with the seq of already cleaned Maps
    val bodymatrix = filteredListOfObjects.map { _.map { case (k, v) => v } }.map(_.toSeq).toList
    val headers = filteredListOfObjects.head.map { case (k, v) => k }.toSeq
    headers::bodymatrix
  }

  def getWholeTable(listOfObjects: Seq[AnyRef]) = getFilteredTable(listOfObjects,List())

  def getTableHeaders(listOfObjects: Seq[AnyRef]) = {
    listOfObjects.map(toMap).head.map { case (k, v) => k }.toSeq::List()
  }

}

object Tabulator {
  val maxlengthOfARow=20
  val dotdotdot: String = "..."
  val emptyString:String =""
  def format(table: Seq[Seq[Any]]): String = table match {
    case Seq() => emptyString
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
    val cells = (for ((item, size) <- row.zip(colSizes))
      yield if (size == 0) emptyString else ("%"+(if(size>maxlengthOfARow)maxlengthOfARow+dotdotdot.size else size)+"s").
        format(
          if ( Try{item.toString.size > maxlengthOfARow}.toOption.getOrElse(false) )
            item.toString.take(maxlengthOfARow) + dotdotdot
          else item
        )
    )
    cells.mkString("|", "|", "|")
  }

  def rowSeparator(colSizes: Seq[Int]) = colSizes map {len=>
    if(len>maxlengthOfARow) "-" * (maxlengthOfARow+dotdotdot.size)
    else "-" * len
  } mkString("+", "+", "+")
}
