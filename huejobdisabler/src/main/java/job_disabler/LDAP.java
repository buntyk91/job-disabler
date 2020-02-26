package job_disabler;

import lombok.extern.slf4j.Slf4j;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * Created by bunty.kumar on 2/15/18.
 */

/*LDAP is very important class for this project.In this class we are connection to the LDAP which is an
* Active directory which stores all the information about the employees in an organization.So we are making a
* connection using the credentials and fetching the list of all the Terminated users.*/
@Slf4j
public class LDAP {

    JobConfig jobConfig;
    ArrayList<String> terminatedUsersList = new ArrayList<>();

    public LDAP(JobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }


    public LdapContext ldapCredentials() {
        LdapContext ldapContext;
        try {
            Hashtable environmentAttributes = new Hashtable();

            environmentAttributes.put(Context.INITIAL_CONTEXT_FACTORY, jobConfig.getInitialContextFactory());
            environmentAttributes.put(Context.PROVIDER_URL, jobConfig.getProviderURL());
            environmentAttributes.put(Context.SECURITY_AUTHENTICATION, "Simple");
            environmentAttributes.put(Context.SECURITY_PRINCIPAL, jobConfig.getSecurityPrincipal());
            try {
                environmentAttributes.put(Context.SECURITY_CREDENTIALS, jobConfig.getSecurityCredentials().getBytes("UTF8"));
            } catch (java.io.UnsupportedEncodingException e) {
                log.error("{}",e);
                log.info("Password error. Connection is failed");
            }
            ldapContext = new InitialLdapContext(environmentAttributes, null);
            log.info("Connection is Successful");
        } catch (NamingException e) {
            log.error("Ldap Initialization error " + e);
            throw new RuntimeException(e);
        }
        return ldapContext;
    }

   /*This method takes the LDAPContext as the argument and return the list of all the terminated users.
    * Pagination has also been done here as LDAP provides only 1000 results in a single run.Pagination
     * provides the facility to fetch all the terminated users which is more than 1000.*/

    public ArrayList<String> getLDAPTerminatedUsersList(LdapContext ldapContext) throws IOException, NamingException {
        ArrayList<String> terminatedUsers;
        try {
            terminatedUsers = new ArrayList<>();
            SearchControls searchControls = new SearchControls();
            String[] returnedAttribute = {"sAMAccountName"};
            searchControls.setReturningAttributes(returnedAttribute);
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            int pageSize = 10000;
            byte[] cookie;

            Control[] controls = new Control[] {new PagedResultsControl(pageSize, Control.NONCRITICAL)};
            ldapContext.setRequestControls(controls);
            int totalResults = 0;

            do {
                NamingEnumeration results =
                        ldapContext.search(jobConfig.getSearchBase(), jobConfig.getSearchFilter(), searchControls);
                while (results != null && results.hasMoreElements()) {
                    SearchResult searchResult = (SearchResult) results.next();
                    Attributes totalAttributes = searchResult.getAttributes();
                    terminatedUsers.add(totalAttributes.get("sAMAccountName").toString());
                    totalResults = totalResults + 1;
                }
                cookie = parseControls(ldapContext.getResponseControls());
                ldapContext.setRequestControls(new Control[] {new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
            } while ((cookie != null) && (cookie.length != 0));
            ldapContext.close();
            log.info("Total number of terminated users till date : {}", totalResults);

        } catch (NamingException e) {
            log.error("Paged Result failed " + e);
            throw new IOException(e);
        }
        return terminatedUsers;
    }

    static byte[] parseControls(Control[] controls) throws NamingException, IOException {
        byte[] cookie = null;
        if (controls != null) {
            for (int i = 0; i < controls.length; i++) {
                if (controls[i] instanceof PagedResultsResponseControl) {
                    PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                    cookie = prrc.getCookie();
                }
            }
        }
        return (cookie == null) ? new byte[0] : cookie;
    }

    public ArrayList<String> removeSamName(ArrayList<String> arrayList) {
        for (String name : arrayList) {
            String names = name.substring(16);
            terminatedUsersList.add(names);
        }
        return terminatedUsersList;
    }
}





