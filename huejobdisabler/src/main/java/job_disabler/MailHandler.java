package job_disabler;

import lombok.extern.slf4j.Slf4j;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by bunty.kumar on 9/27/18.
 */
@Slf4j
public class MailHandler {
    private Properties properties = new Properties();
    private String fromUserName;
    private String fromUserEmail;
    private static MailHandler mailHandler;
    JobConfig jobConfig;
    private static int count = 0;


    public MailHandler(String fromUserEmail, String fromUserName, JobConfig jobConfig) {
        this.jobConfig = jobConfig;
        if (fromUserName != null)
            this.fromUserName = fromUserName;
        this.fromUserEmail = fromUserEmail;
        properties.put("mail.smtp.auth", jobConfig.getEmail_Smtp_Auth());
        properties.put("mail.smtp.host", jobConfig.getEmail_Smtp_Host());
        properties.put("mail.smtp.port", jobConfig.getEmail_Smtp_Port());

        log.info("MailHandler new instance created for User : {} ", fromUserName);
    }

    public static MailHandler getInstance(String fromUserEmail, String fromUserName, JobConfig jobConfig) {
        synchronized (MailHandler.class) {
            if (mailHandler == null)
                mailHandler = new MailHandler(fromUserEmail, fromUserName, jobConfig);
        }
        return mailHandler;
    }

    public boolean sendMail(String toAddress, String subject, String message) throws MessagingException {
        Session session = Session.getDefaultInstance(properties);
        try {
            Message messages = new MimeMessage(session);
            try {
                messages.setFrom(new InternetAddress(fromUserEmail, fromUserName));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String recipients[] = toAddress.split(",");
            for (int i = 0; i < recipients.length; i++)
                messages.addRecipients(Message.RecipientType.TO,
                        new InternetAddress[]{new InternetAddress(recipients[i])});
            messages.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            messages.setSubject(subject);
            messages.setContent(message, "text/html");
            Transport.send(messages);
            log.info("Email sent successfully to : {}", toAddress);

        } catch (MessagingException me) {
            log.error("{}", me);
            return false;
        }
        return true;
    }

    public static StringBuilder mailBuilder(Map hashMap) {
        HashSet<String> unique = new HashSet<>();
        Iterator iterator = hashMap.entrySet().iterator();
        Iterator iterator1 = hashMap.entrySet().iterator();
        StringBuilder messageBody = new StringBuilder();

        String s1 = "Dear Admin, \n";
        messageBody.append(s1);
        String s3 = "<!doctype html>" +
                "<html>\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h4>Below users has been disabled.Hence the coordinators for these inactive users has been killed : " + "</h4>\n";
        messageBody.append(s3);

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String username = pair.getValue().toString();
            unique.add(username);
        }
        for (String user : unique) {
            messageBody.append("<li>" + user + "</li>\n");
        }

        String s4 = "<!doctype html>" +
                "<html>\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h4>For reference, Below is the mapping of users to their respective coordinators: " + "</h4>\n";
        messageBody.append(s4);

        while (iterator1.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator1.next();
            String coordinatorId = pair.getKey().toString();
            String userName = pair.getValue().toString();
            messageBody.append("<li>" + userName + " : " + coordinatorId + "</li>");
            count = count + 1;
        }
        //sb.append("The total number of coordinator killed : " + count);
        messageBody.append("<!doctype html>" +
                "<html>\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h4>Total number of schedules(or coordinators) killed : " + count + "</h4>\n");
        messageBody.append("Regards," + " \n");
        messageBody.append("\n");
        messageBody.append("Hue Team");
        return messageBody;
    }

    public static String subjectBuilder() {
        String subjectText = "Hue : Killed schedulers or Coordinators of inactive users [OFS]  :  ";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String currentDate = (formatter.format(date));
        String finalSubject = subjectText + currentDate;
        return finalSubject;
    }
}
