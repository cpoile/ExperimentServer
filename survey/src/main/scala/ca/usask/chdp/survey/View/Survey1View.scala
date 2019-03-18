package ca.usask.chdp.survey.View

import ca.usask.chdp.survey.SurveyViewManager
import com.vaadin.ui._
import jsExtensions.SvoSlider
import org.slf4j.LoggerFactory
import com.vaadin.server.Page
import org.json.JSONArray
import ca.usask.chdp.models._

class Survey1View(viewMgr: SurveyViewManager, curUser: String) extends CustomComponent {
  val log = LoggerFactory.getLogger(classOf[Survey1View])

  val layout = new CustomLayout("survey1Layout")
  setCompositionRoot(layout)

  val svoSlider = new SvoSlider
  layout.addComponent(svoSlider, "svoSlider")

  val messageBox = new Label("Hi")
  layout.addComponent(messageBox)

  override def attach() {
    super.attach()
    svoSlider.start()

    Notification.show("Please note: this survey will not work with Internet Explorer 8 or below.", Notification.Type.ERROR_MESSAGE)
  }

  var dataRaw = Survey1DataRaw()
  var dataSummary = Survey1DataSummary()

  // Add a server-side function to notify us when they're done with the survey.
  Page.getCurrent.getJavaScript.addFunction("chdp_Survey1View_weAreFinished",
    new JavaScriptFunction() {
      def call(arguments: JSONArray) {
        log.debug("chdp_survey1_finished called with params: {}", arguments)
        val isFinished = arguments.getBoolean(0)
        if (isFinished)
          finishedSurvey()
      }
    })

  /**
   * They did not answer carefully. restart.
   */
  Page.getCurrent.getJavaScript.addFunction("chdp_Survey1View_restart",
    new JavaScriptFunction() {
      def call(arguments: JSONArray) {
        log.debug("chdp.survey1.restart called")
        viewMgr.setPageTo(Survey1)
      }
    })

  /**
   * order of data is from the client in sSlider_connector.
   */
  Page.getCurrent.getJavaScript.addFunction("chdp_Survey1View_returnData",
    new JavaScriptFunction() {
      // first is the name of the array, second is the array
      def call(a: JSONArray) {
        //       log.debug("Received data from javascript svolSlider_Connector -- {}", a)
        dataRaw = Survey1DataRaw(jsonArray2ListInt(a.getJSONArray(0)), jsonArray2ListInt(a.getJSONArray(1)),
          jsonArray2ListInt(a.getJSONArray(2)), jsonArray2ListInt(a.getJSONArray(3)), jsonArray2ListInt(a.getJSONArray(4)),
          jsonArray2ListInt(a.getJSONArray(5)), jsonArray2ListInt(a.getJSONArray(6)), jsonArray2ListInt(a.getJSONArray(7)),
          jsonArray2ListInt(a.getJSONArray(8)), jsonArray2MatrixInt(a.getJSONArray(9)), jsonArray2MatrixInt(a.getJSONArray(10)),
          jsonArray2ListDouble(a.getJSONArray(11)), jsonArray2MatrixDouble(a.getJSONArray(12)))
        dataSummary = Survey1DataSummary(a.getInt(13), a.getDouble(14), a.getDouble(15), a.getDouble(16),
          a.getDouble(17), a.getDouble(18), a.getDouble(19), a.getDouble(20), a.getString(21), a.getString(22),
          a.getBoolean(23))
        //log.debug("{}\n\nSummary: {}",dataRaw, dataSummary)
      }
    })

  def jsonArray2ListInt(jArr: JSONArray): List[Int] = {
    (for (i <- 0 until jArr.length) yield jArr.getInt(i)).toList
  }
  def jsonArray2MatrixInt(jArr: JSONArray): List[RowInt] = {
    (for (i <- 0 until jArr.length) yield {
      traversableInt2RowInt(for (j <- 0 until jArr.getJSONArray(i).length) yield {
        jArr.getJSONArray(i).getInt(j)
      })
    }).toList
  }
  def jsonArray2ListDouble(jArr: JSONArray): List[Double] = {
    (for (i <- 0 until jArr.length) yield jArr.getDouble(i)).toList
  }
  def jsonArray2MatrixDouble(jArr: JSONArray): List[RowDouble] = {
    (for (i <- 0 until jArr.length) yield {
      traversableDouble2RowDouble(for (j <- 0 until jArr.getJSONArray(i).length) yield {
        jArr.getJSONArray(i).getDouble(j)
      })
    }).toList
  }
  def finishedSurvey() {
    //log.debug("finishedSurvey called. curUser: {}", curUser)
    val part = ParticipantDAO.findByEmail(curUser)
    //log.debug("part bean: {}", part)
    part foreach { p =>
      //log.debug("updating user: ", p)
      val newSurveyCompl = p.surveyCompl + ("survey1" -> true)
      val newUserBean = p.copy(surveyCompl = newSurveyCompl, survey1DataRaw = dataRaw,
        survey1DataSummary = dataSummary)
      ParticipantDAO.insertUpdate(newUserBean)
      viewMgr.setPageTo(SurveyMainPage(newUserBean.email))
    }
  }
}
