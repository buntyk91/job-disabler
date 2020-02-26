package job_disabler;

import lombok.Data;

/**
 * Created by bunty.kumar on 9/18/18.
 */
@Data
public class JobConfig {

    private String usersContainer;

    private String oozieURL;

    private String initialContextFactory;

    private String providerURL;

    private String securityPrincipal;

    private String securityCredentials;

    private String searchBase;

    private String searchFilter;

    private String oozieGetURL;

    private String proxyHost;

    private String actionName;

    private String port;

    private String fileName;

    private String email_Smtp_Auth;

    private String email_Smtp_Host;

    private String email_Smtp_Port;

    private String toAddress;

    private String fromUserEmail;

    private String fromUserName;

}
