package code.snippet

import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.http.SHtml._
import net.liftweb.http.{S, FileParamHolder, RequestVar}
import code.lib.CsvParser._
import au.com.bytecode.opencsv.{CSVWriter, CSVReader}
import scala.collection.JavaConversions._
import code.lib.Pricesquid._
import java.io.{FileWriter, BufferedWriter, InputStreamReader, BufferedReader}
import net.liftweb.util.Props

/**
 * Created by IntelliJ IDEA.
 * User: j2
 * Date: 30-05-12
 * Time: 02:43 PM
 * To change this template use File | Settings | File Templates.
 */

class Connect {

  object scanProductsFile extends RequestVar[Box[FileParamHolder]](Empty)
  object connectionsFile extends RequestVar[Box[FileParamHolder]](Empty)
  object scanProductItems extends RequestVar[Box[ProductRow]](Empty)
  object connectionItems extends RequestVar[Box[ConnectionRow]](Empty)

  def render() = {
    "name=products-file" #> fileUpload(f => scanProductsFile.set(Full(f))) &
    "name=connections-file" #> fileUpload(f => connectionsFile.set(Full(f))) &
    "type=submit" #> submit("Connect", () => process())
  }

  def process() = {
    validateFiles() match {
      case Nil =>
        val csvConnection = parse(connectionsFile.is.get)
        val csvProducts = parse(scanProductsFile.is.get)
        val connection = convertToConnection(csvConnection)
        val productRows = convertToProductRows(csvProducts)
        regularMatch(connection, productRows)
        levenshteinMatch(connection, productRows)
        write(connection)
      case errors =>
        errors.foreach(S.error(_))
    }
  }

  def validateFiles() = {
    validateScanProductsFile() ::: validateConnectionsFile()
  }

  def validateScanProductsFile() = {
    scanProductsFile.is.map(f => isValidCsv(f)).getOrElse(List("Empty.file"))
  }

  def validateConnectionsFile() = {
    connectionsFile.is.map(f => isValidCsv(f)).getOrElse(List("Empty.file"))
  }

  def isValidCsv(fileParam: FileParamHolder) = {
    tryo{
      val csvReader = new CSVReader(new BufferedReader(new InputStreamReader(fileParam.fileStream)), ';')
      csvReader.readAll()
    }.dmap(List("Invalid.file"))(s => Nil: List[String])
  }

  def parse(fileParam: FileParamHolder) = {
    val csvReader = new CSVReader(new BufferedReader(new InputStreamReader(fileParam.fileStream)), ';')
    csvReader.readAll().toList
  }

  def write(connection: Connection) = {
    val fileWriter = new FileWriter(Props.get("output.dir", "/home/j2/") + "connection-" +
      java.util.Calendar.getInstance().getTimeInMillis)
    val csvWriter = new CSVWriter(fileWriter, ';')
    csvWriter.writeAll(connection.toCsvWriter())
  }
}