package code.lib

import scala.util.parsing.combinator._
import scala.util.parsing.combinator.syntactical._

/**
 * Created by IntelliJ IDEA.
 * User: j2
 * Date: 30-05-12
 * Time: 03:01 PM
 * To change this template use File | Settings | File Templates.
 */


object CsvParser extends RegexParsers {

  // Turns off whitespace removal: line separators are an important part of the CSV format...
  override def skipWhitespace = false

  def CRLF = "\r\n" | "\n"
  def EOF = "\\z".r

  // Any number of columns, but no backtracking over accidental double-quotes.
  def stringInQuotes = """(?xs) ".*?" |""".r ^^ {case qstr => if (qstr.length != 0) qstr.substring (1, qstr.length - 1) else ""}
  def line = stringInQuotes ~ ';' ~ stringInQuotes ~ (CRLF | EOF) ^^ {case col1 ~ _ ~ col2 ~ _ => col1 :: col2 :: Nil}

  // Fixed number of columns, but backtracking over accidental double-quotes works.
  def unquote (str: String) = str.substring (1, str.length - (str.charAt (str.length - 1) match {case ';'|'\r'|'\n' => 2; case _ => 1}))
  def col1 = ("(?s)\".*?\";".r ^^ unquote _) | (";" ^^ (_ => ""))
  def col2 = ("(?s)\".*?\"(\\r|\\n)?".r ^^ unquote _) | (("\r" | "\n" | EOF) ^^ (_ => ""))
  def twoColumns = col1 ~ col2 ~ opt ("\n") ^^ {case v1 ~ v2 ~ _ => v1 :: v2 :: Nil}

  def csv: Parser[List[List[String]]] = rep1 (twoColumns)

  def unwrap[T] (result: ParseResult[T]) = result match {
    case Success (data, _) => data
    case f@Failure (message, _) => throw new Exception (f.toString)
    case e@Error (message, _) => throw new Exception (e.toString)
  }


}