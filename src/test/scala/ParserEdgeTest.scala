
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{Disabled, DisplayName, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{CsvFileSource, ValueSource}

class ParserEdgeTest:

  @Test
  def redundantTail(): Unit =
    val result = Parser.parse("AAA(BBB)tail,tail,()")
    assertEquals(
      "AAA(BBB)",
      result.toString
    )
  
  @Test
  def nonCaseClass(): Unit =
    val result = Parser.parse(",,,XXX,,,")
    assertEquals(
      ",,,XXX,,,",
      result.toString
    )