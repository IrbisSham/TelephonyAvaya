package com.it_zabota.jira.telephony.ldap.kerberos;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

import com.it_zabota.jira.telephony.ldap.sasl.SaslAuthentication;
import com.it_zabota.jira.telephony.ldap.simple.Authenticator;
import com.it_zabota.jira.telephony.main.JCCM;
import com.it_zabota.jira.telephony.other.UnsetVariableException;
import com.it_zabota.jira.telephony.utils.SysUtils;

public class KerberosAuthenticator implements Authenticator {

	private static InitialDirContext context = null;
    private static LoginContext loginContext = null;

    private String principal = JCCM.LDAP_USERNAME; // "root/devicesoft.org@DEVICESOFT.ORG"
    private String keytab = "krb5.keytab"; // "C:/Users/brett/Documents/krb5.keytab"
    private String url = SysUtils.getUrl("ldap://", JCCM.LDAP_HOST, JCCM.LDAP_PORT); // "ldap://management.devicesoft.org/dc=devicesoft,dc=org"
    private String krb5Conf = "krb5.conf"; // "C:/Users/brett/Documents/krb5.conf"
    private String debug = "true"; // "false"

    private List<SearchResult> results = new ArrayList<SearchResult>();
    
    public List<SearchResult> getResults() {
		return results;
	}

	public void setResults(List<SearchResult> results) {
		this.results = results;
	}

	public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getKeytab() {
        return keytab;
    }

    public void setKeytab(String keytab) {
        this.keytab = keytab;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKrb5Conf() {
        return krb5Conf;
    }

    public void setKrb5Conf(String krb5Conf) {
        this.krb5Conf = krb5Conf;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public InitialDirContext getInitialDirContext() {

        if (context != null) {
            try {
                SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
                NamingEnumeration<SearchResult> resultSearch = context.search(JCCM.LDAP_SEARCHBASE, JCCM.LDAP_SEARCHFILTER, searchControls);
                results.clear();
                while(resultSearch.hasMoreElements()) {                	
                	SearchResult searchResult = (SearchResult) resultSearch.nextElement();
                    results.add(searchResult);   
               }	                
                
            } catch (NameNotFoundException nnfe) {
                return context;
            } catch (NamingException ne) {
                Logger.getLogger(this.getClass()).error("", ne);
            }
            //
            // we have a bad context
            //
        }
        if (loginContext != null) {
            try {
                loginContext.logout();
            } catch (LoginException le) {
                Logger.getLogger(this.getClass()).error("", le);
            }
            //
            // loginContext is invalid
            //
        }
        try {
            Krb5Login krb5Login = new Krb5Login(principal, keytab, debug);

            loginContext = krb5Login.login();
            //
            // we have a login context
            //
            SaslAuthentication saslAuthentication = new SaslAuthentication(url, krb5Conf, loginContext.getSubject());

            context = saslAuthentication.getInitialDirContext();

            if (context == null) {
                Logger.getLogger(this.getClass()).error("context is null");
            } 
//            else {
//                try {
//                    context.addToEnvironment("java.naming.ldap.attributes.binary", "userPKCS12");
//                } catch (NamingException ne) {}
//            }
            return context;

        } catch (UnsetVariableException uve) {
            Logger.getLogger(this.getClass()).error("", uve);
            return null;
        } catch (LoginException le) {
            Logger.getLogger(this.getClass()).error("", le);
            return null;
        }
    }
}
