package ca.usask.chdp

import javax.mail._
import javax.mail.internet.{MimeBodyPart, MimeMultipart, InternetAddress, MimeMessage}
import java.util.Properties
import org.slf4j.LoggerFactory
import java.io.UnsupportedEncodingException

object Email {
  val log = LoggerFactory.getLogger("Email")

  @throws(classOf[MessagingException])
  @throws(classOf[UnsupportedEncodingException])
  def sendEmail(body: String, subject: String, recipient: String, fromName: String, fromAddr: String) = {
    try {
      val mailProps = new Properties()
      mailProps.put("mail.smtp.from", fromAddr)
      mailProps.put("mail.smtp.host", "smtp.usask.ca")
      mailProps.put("mail.smtp.port", "587")
      mailProps.put("mail.smtp.auth", "true")
      mailProps.put("mail.smtp.socketFactory.port", "587")

      // StartTLS is not SSL:
      //
      //    mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
      //    mailProps.put("mail.smtp.socketFactory.fallback", "false")
      mailProps.put("mail.smtp.starttls.enable", "true")

      val session = Session.getInstance(
        mailProps, new Authenticator() {
          override def getPasswordAuthentication = new PasswordAuthentication(
            "tms076", System.getenv("EXP_EMAIL_PWD"))
        })
      session.setDebug(false)
      // set from, to, subject, body text
      // Set the from, to, subject, body text
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(fromAddr, fromName))
      // message.setReplyTo(Array(new InternetAddress("poile@edwards.usask.ca")))
      message.setRecipients(Message.RecipientType.TO, recipient.toLowerCase.trim)
      message.setSubject(subject, "UTF-8")

      val mp = new MimeMultipart()
      val mbp = new MimeBodyPart()
      mbp.setContent(body, "text/html;charset=utf-8")
      mp.addBodyPart(mbp)
      message.setContent(mp)
      message.setSentDate(new java.util.Date())

      // And send it
      Transport.send(message)

    } catch {
      // more idiomatic scala, but it didn't work for some reason:
      //
      //    case mex: MessagingException =>
      //      // How to access nested exceptions
      //      def getNextEx(ex: Any): Any = ex match {
      //        case mex: MessagingException =>
      //          mex.printStackTrace()
      //          getNextEx(mex.getNextException)
      //        case _ => println("done printing exceptions")
      //      }
      //      mex.printStackTrace()
      //      getNextEx(mex.getNextException)
      // Prints all nested (chained) exceptions as well
//      case mex: MessagingException => {
//        log.error("MessagingException: {} ", mex.getMessage, mex.fillInStackTrace())
//        var ex = mex
//        // How to access nested exceptions
//        while (ex.getNextException != null) {
//          log.error("MessagingException: {} ", ex.getNextException.getMessage, ex.getNextException.fillInStackTrace())
//          // Get next exception in chain
//          if (!ex.getNextException.isInstanceOf[MessagingException])
//            scala.util.control.Breaks.break()
//          else
//            ex = ex.getNextException.asInstanceOf[MessagingException]
//        }
//      }
      case mex: MessagingException => {
        println(mex.printStackTrace())
        var ex = mex
        // How to access nested exceptions
        while (ex.getNextException != null) {
          println(ex.getNextException.printStackTrace())
          // Get next exception in chain
          if (!ex.getNextException.isInstanceOf[MessagingException])
            scala.util.control.Breaks.break()
          else
            ex = ex.getNextException.asInstanceOf[MessagingException]
        }
      }
    }
  }

  def ConfirmationMsg(session: String): String = {
    "Thank you for registering for the Comm 105 research session.<p>" +
      "Your session of the online simulation game is scheduled for: " + session +
      """<p></p>
<strong>Instructions:</strong>
<ol>
<li>
About 5 minutes before your scheduled time, go to: <a href="http://chp3.usask.ca/simulation/">http://chp3.usask.ca/simulation/</a>
</li>
<li>
This is a game you will play with others. Please don't be late, or else everyone else will have to wait for you.
</li>
<li>Use an internet-connected computer with an updated version of Gooogle Chrome or Firefox.
</li>
<li>
Remember that you need to complete the pre-experiment surveys <emph>4 hours</emph> before the start of your experiment session. If you do not complete the questions you will not be able to participate in the experiment. The surveys are at: <a href="http://chp3.usask.ca/survey/">http://chp3.usask.ca/survey/</a>
</li>
<li>If you need to change your experiment booking, you may log back into the signup session at:
<a href="http://chp3.usask.ca/signup/">http://chp3.usask.ca/signup/</a> . Note: you cannot change your session within 4 hours of the session time.
</li>
</ul>
Thank you for participating!
<p>
Comm 105 Research Pool Group
<p>
(Reply to this email if you have any questions and the research pool coordinator will forward your message to the researcher.)
      """

  }
}
