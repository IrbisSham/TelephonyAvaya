package com.it_zabota.jira.telephony.ldap.kerberos;

import org.apache.log4j.Logger;

import com.it_zabota.jira.telephony.other.UnsetVariableException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class Krb5Login implements CallbackHandler {

	   private String principal;
	    private String keytab;
	    private String debug;

	    public Krb5Login(String principal, String keytab, String debug) throws UnsetVariableException {
	        if (principal == null) {
	            throw new UnsetVariableException("principal must be set");
	        }
	        if (keytab == null) {
	            throw new UnsetVariableException("keytab must be set");
	        }
	        if (debug == null || !(debug.equals("true") || debug.equals("false"))) {
	            throw new UnsetVariableException("debug must be set to 'true' or 'false'");
	        }
	        this.debug = debug;
	        this.principal = principal;
	        this.keytab = keytab;
	    }

	    public LoginContext login() throws LoginException {
	        Configuration configuration = null;

	        try {
	            configuration = new LdapConfiguration(principal, keytab, debug);
	        } catch (UnsetVariableException uve) {
	            Logger.getLogger(this.getClass()).error("", uve);
	            throw new LoginException(uve.getMessage());
	        }

	        LoginContext loginContext = new LoginContext("unused", null, this, configuration);
	        loginContext.login();

	        return loginContext;
	    }

	    public void handle(Callback[] callbacks) {}

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

}
