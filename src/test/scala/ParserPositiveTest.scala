
import io.github.nimatrueway.caseclass.Parser
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ParserPositiveTest {

  @ParameterizedTest
  @ValueSource(strings = Array(
    "AAA",
    "AAA()",
    "AAA(BBB)",
    "AAA(BBB,CCC,DDD)",
    "AAA(BBB,,DDD)",
    "AAA(BBB(CCC))",
    "AAA(BBB(CCC,DDD),EEE(FFF,GGG))",
    "AAA(,BBB(),CCC(DDD),EEE(FFF(GGG),HHH))",
    "()",
    "(,)",
    "(,,)",
    "(AAA)",
    "(AAA,BBB)",
    "AAA(,BBB(),CCC(DDD),(),,EEE(FFF(GGG),HHH))",
  ))
  def simpleString(text: String): Unit = {
    val result = Parser.parse(text)
    assertEquals(
      text,
      result.toString
    )
  }

}