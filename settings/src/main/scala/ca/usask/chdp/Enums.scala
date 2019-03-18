package ca.usask.chdp

/*object PartNum extends Enumeration {
  val Part1, Part2, Part3 = Value
}

object Round extends Enumeration {
  val Rnd1, Rnd2, Rnd3, Rnd4, Rnd5, Rnd6, Rnd7, Rnd8, Rnd9, Rnd10 = Value
}

object PartMark extends Enumeration {
  val MarkI, MarkII, MarkIII, MarkIV, MarkV, MarkVI, MarkVII, MarkVIII, MarkIX, MarkX,
  MarkXI, MarkXII, MarkXIII, MarkXIV, MarkXV, MarkXVI, MarkXVII, MarkXVIII, MarkXIX, MarkXX = Value
}

object Role extends Enumeration {
  val A, B = Value
}

/**
 * See the application.conf file for an explanation of the equation.
 */
object GameEqn extends Enumeration {
  val A_max, B_max, A_persGoal, B_dep, A_minHelp, B_goals, A_extraHelp, B_min, A_discretionary = Value
}

object UIElemState extends Enumeration {
  val Enabled, Disabled = Value
}

object RoundComplAfterThis extends Enumeration {
  val False, True = Value
  def apply(x: Boolean): Value = x match {
    case false => False
    case true => True
  }
  val x = RoundComplAfterThis.ValueSet
}*/

object Enums {
  object GameEqn extends Enumeration {
    val A_max, B_max, A_persGoal, B_dep, A_minHelp, B_goals, A_extraHelp, B_min, A_discretionary = Value
  }
  def PartNum(x: Int): String = {
    assert(x >= 0 && x <= 2)
    x match {
      case 0 => "Part1"
      case 1 => "Part2"
      case 2 => "Part3"
    }
  }
  def PartNumID(partNum: String): Int = {
    assert(PartNumValues.contains(partNum))
    partNum match {
      case "Part1" => 0
      case "Part2" => 1
      case "Part3" => 2
    }
  }
  def PartNumValues: List[String] = {
    List("Part1", "Part2", "Part3")
  }
  def Round(idx: Int): String = {
    assert(idx >= 0 && idx <= 9)
    idx match {
      case 0 => "Rnd1"
      case 1 => "Rnd2"
      case 2 => "Rnd3"
      case 3 => "Rnd4"
      case 4 => "Rnd5"
      case 5 => "Rnd6"
      case 6 => "Rnd7"
      case 7 => "Rnd8"
      case 8 => "Rnd9"
      case 9 => "Rnd10"
    }
  }
  def RoundID(rnd: String): Int = rnd match {
    case "Rnd1" => 0
    case "Rnd2" => 1
    case "Rnd3" => 2
    case "Rnd4" => 3
    case "Rnd5" => 4
    case "Rnd6" => 5
    case "Rnd7" => 6
    case "Rnd8" => 7
    case "Rnd9" => 8
    case "Rnd10" => 9
    case _ => 999
  }
  def RoundIfAddX(rnd: Int, x: Int): Int = {
    assert(rnd >= 0 && rnd <= 9)
    val newID = rnd + x
    assert(newID >= 0 && newID <= 9)
    newID
  }
  def PartMark(idx: Int): String = {
    assert(idx >= 0 && idx <= 19)
    idx match {
      case 0 => "MarkI"
      case 1 => "MarkII"
      case 2 => "MarkIII"
      case 3 => "MarkIV"
      case 4 => "MarkV"
      case 5 => "MarkVI"
      case 6 => "MarkVII"
      case 7 => "MarkVIII"
      case 8 => "MarkIX"
      case 9 => "MarkX"
      case 10 => "MarkXI"
      case 11 => "MarkXII"
      case 12 => "MarkXIII"
      case 13 => "MarkXIV"
      case 14 => "MarkXV"
      case 15 => "MarkXVI"
      case 16 => "MarkXVII"
      case 17 => "MarkXVIII"
      case 18 => "MarkXIX"
      case 19 => "MarkXX"
    }
  }
  def PartMarkID(mark: String): Int = mark match {
    case "MarkI" => 0
    case "MarkII" => 1
    case "MarkIII" => 2
    case "MarkIV" => 3
    case "MarkV" => 4
    case "MarkVI" => 5
    case "MarkVII" => 6
    case "MarkVIII" => 7
    case "MarkIX" => 8
    case "MarkX" => 9
    case "MarkXI" => 10
    case "MarkXII" => 11
    case "MarkXIII" => 12
    case "MarkXIV" => 13
    case "MarkXV" => 14
    case "MarkXVI" => 15
    case "MarkXVII" => 16
    case "MarkXVIII" => 17
    case "MarkXIX" => 18
    case "MarkXX" => 19
    case _ => 999
  }
  def PartMarkIfAddX(mark: String, x: Int): String = {
    assert(PartMarkID(mark) != 999)
    val markID = PartMarkID(mark)
    val newID = markID + x
    PartMark(newID)
  }
}