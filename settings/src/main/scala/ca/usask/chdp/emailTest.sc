import ca.usask.chdp.Email
import java.io.UnsupportedEncodingException
import javax.mail._
import javax.mail.internet.{MimeBodyPart, MimeMultipart, InternetAddress, MimeMessage}
import java.util.Properties
import org.slf4j.LoggerFactory
import org.apache.commons.validator.routines.EmailValidator


Email.sendEmail(Email.ConfirmationMsg("test session"), "This is a test", "poile@edwards.usask.ca", "Comm 105 Research Pool", "devstudy@edwards.usask.ca")

