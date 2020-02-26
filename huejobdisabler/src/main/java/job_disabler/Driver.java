package job_disabler;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.yaml.snakeyaml.Yaml;

import javax.naming.ldap.LdapContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bunty.kumar on 2/19/18.
 */

/*This is the driver class from where the application execution will start*/
@Slf4j
public class Driver extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        ToolRunner.run(new Driver(), args);
    }

    @Override
    public int run(String[] args) throws Exception {
        String configFileName = args[0];
        JobConfig jobConfig = loadJobConfig(configFileName, getConf());
        log.info(String.valueOf(jobConfig));

        OozieConnection oozieConnection = new OozieConnection(jobConfig);
        List<OozieData> runningSchedulers = oozieConnection.connectToOozie();

        LDAP ldap = new LDAP(jobConfig);
        LdapContext ctx = ldap.ldapCredentials();
        ArrayList<String> finalLdapUsers = ldap.getLDAPTerminatedUsersList(ctx);
        ArrayList<String> terminatedUsers = ldap.removeSamName(finalLdapUsers);

        ArrayList<String> terminatedUserssample = new ArrayList<>();
        terminatedUserssample.add("siddharth.sawhney");

        CompareLists compareLists = new CompareLists();
        Map<List<String>, String> killMap = compareLists.compareLists(terminatedUserssample, runningSchedulers);

        KillCoordinator killCoordinator = new KillCoordinator(jobConfig);
        killCoordinator.getJobURL(killMap);
        killCoordinator.run(killMap);
        String subject = MailHandler.subjectBuilder();

        MailHandler mailHandler = MailHandler.getInstance(jobConfig.getFromUserEmail(), jobConfig.getFromUserName(), jobConfig);
        mailHandler.sendMail(jobConfig.getToAddress(), subject, MailHandler.mailBuilder(killMap).toString());
        return 0;
    }

    private static JobConfig loadJobConfig(String configFileName, Configuration conf) throws IOException {
        String text = Util.getFromLocalFile(configFileName, conf);
        JobConfig jobConfig = new Yaml().loadAs(text, JobConfig.class);
        return jobConfig;
    }
}
