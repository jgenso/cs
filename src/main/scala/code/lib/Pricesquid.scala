package code.lib

import scala.collection.JavaConversions._
import collection.mutable.{HashMap, ArrayBuffer}
import net.liftweb.util.Props
import org.apache.commons.lang3.StringUtils

/**
 * Created by IntelliJ IDEA.
 * User: j2
 * Date: 30-05-12
 * Time: 04:47 PM
 * To change this template use File | Settings | File Templates.
 */

object Pricesquid {

  case class Connection(header: Array[String], keys: List[String], rows: List[ConnectionRow]) {
    def toCsvWriter(): List[Array[String]] = {
      header :: rows.map(_.toArray(keys))
    }
  }

  case class ConnectionRow(shopId: String, itemNo: String, name: String, statTag: String,
                           urls: HashMap[String, String]) {
    override def toString() = {
      "Shop Id: " + shopId + " Item No.: " + itemNo + " Name: " + name + urls.foldLeft(""){case (k,v) => k + " : " + v}
    }

    def toList(keys: List[String]) = List(shopId, itemNo, name, statTag) ::: keys.map(key => urls.getOrElse(key, ""))

    def toArray(keys: List[String]) = toList(keys).toArray
  }

  case class ProductRow(productNumber: String, productName: String, productText1: String, productText2: String,
    productText3: String, link: String) {
    override def toString() = {
      "Product Number: " + productNumber + " Link: " + link
    }
  }

  implicit def toString(c: ConnectionRow) = c.toString()

  implicit def toString(p: ProductRow) = p.toString()

  def convertToConnection(list: List[Array[String]]) = {
    val keys = list.head.drop(4).toList
    Connection(list.head, keys, list.tail.map(l => ConnectionRow(l(0), l(1), l(2), l(3), connectionUrls(keys, l.drop(4).toList))))
  }

  def connectionUrls(keys: List[String], values: List[String]): HashMap[String,String] = {
    HashMap((keys zip values) :_*)
  }

  def convertToProductRows(list: List[Array[String]]) = {
    list.tail.map(l => ProductRow(l(0), l(5), l(6), l(7), l(8), l(14)))
  }

  def regularMatch(connection: Connection, products: List[ProductRow]) = {
    connection.rows.foreach(row => {
      products.filter(p => p.productNumber.trim() == row.itemNo.trim()).headOption match {
        case None =>
          ()
        case Some(p: ProductRow) =>
          val key = "http://" + p.link.split("/")(2) + "_ItemID"
          row.urls.getOrElse(key, "").trim() match {
            case "" =>
              row.urls.update(key, p.link)
            case x =>
              () //println("Ya esta lleno con" + x)
          }
      }
    })
  }

  def levenshteinMatch(connection: Connection,  products: List[ProductRow]) = {
    val threshold = Props.get("threshold", "10").toInt
    connection.rows.foreach(row => {
      val rowWords = row.name.split(" ")
      products.map(product => {
        val productWords = product.productName.split(" ")
        (productWords.foldLeft(0){case (value,name) =>
          value + rowWords.foldLeft(0){case(a,b) =>
            a + StringUtils.getLevenshteinDistance(name,b, threshold)}/rowWords.length} /  productWords.length , product)
      }).filter(p => p._1 > -1 && p._1 < 5).headOption match {
        case None =>
          ()
        case Some(p: (Int, ProductRow)) =>
          val key = "http://" + p._2.link.split("/")(2) + "_ItemID"
          row.urls.getOrElse(key, "").trim() match {
            case "" =>
              row.urls.update(key, p._2.link)
            case x =>
              ()
          }
      }
    })
  }

}