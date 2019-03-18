package ca.usask.chdp.models

//case class Survey1Data( map_listInt: Map[String, List[Int]] = Map.empty[String, List[Int]],
//                        map_listDouble: Map[String, List[Double]] = Map.empty[String, List[Double]],
//                        map_Int: Map[String, Int] = Map.empty[String, Int],
//                        map_Double: Map[String, Double] = Map.empty[String, Double],
//                        map_String: Map[String, String] = Map.empty[String, String],
//                        map_MatrixInt: Map[String, List[List[Int]]] = Map.empty[String, List[List[Int]]],
//                        map_MatrixDouble: Map[String, List[List[Double]]] = Map.empty[String, List[List[Double]]])
//

case class Survey1DataRaw(itemID: List[Int] = Nil,
                          itemLowvalYou: List[Int] = Nil,
                          itemHighvalYou: List[Int] = Nil,
                          itemDescYou: List[Int] = Nil,
                          itemLowvalOther: List[Int] = Nil,
                          itemHighvalOther: List[Int] = Nil,
                          itemDescOther: List[Int] = Nil,
                          choiceYou: List[Int] = Nil,
                          choiceOther: List[Int] = Nil,
                          ticksValYou: List[RowInt] = Nil,
                          ticksValOther: List[RowInt] = Nil, //10
                          timeChoice: List[Double] = Nil,    //11
                          ticksTime: List[RowDouble] = Nil)   //12

case class RowInt(row: List[Int] = Nil)
case class RowDouble(row: List[Double] = Nil)

case class Survey1DataSummary(perc: Int = 0,
                              firstSixAngle: Double = 0.0,
                              sessionStart: Double = 0.0,
                              first_item_timestamp: Double = 0.0,
                              altr_value: Double = 0.0,
                              indiv_value: Double = 0.0,
                              ineqav_value: Double = 0.0,
                              jointgain_value: Double = 0.0,  //20
                              firstSixCat: String = "",
                              secondRes: String = "",
                              transitHolds: Boolean = true)