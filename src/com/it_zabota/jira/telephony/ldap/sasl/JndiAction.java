package com.it_zabota.jira.telephony.ldap.sasl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.it_zabota.jira.telephony.ldap.LdapUtils;
import com.it_zabota.jira.telephony.main.JCCM;

public class JndiAction implements java.security.PrivilegedAction  {
	private String[] args;

    public JndiAction(String[] origArgs, LdapUtils ldap) {
		this.args = (String[])origArgs.clone();
		this.ldap = ldap;
    }

    public Object run() {
    	performJndiOperation();
	return null;
    }

    private LdapUtils ldap;
    
    public LdapUtils getLdap() {
		return ldap;
	}

	public void setLdap(LdapUtils ldap) {
		this.ldap = ldap;
	}

	private void performJndiOperation() {
	String dn;

	// Set up environment for creating initial context
	Hashtable env = new Hashtable(11);

	env.put(Context.INITIAL_CONTEXT_FACTORY, 
	    "com.sun.jndi.ldap.LdapCtxFactory");
	String url = "";
	// Must use fully qualified hostname	
    if (JCCM.LDAP_PORT.equals("") || JCCM.LDAP_PORT == null) {
    	url = "ldap://" + JCCM.LDAP_HOST;
    }
    else {
    	url = "ldap://" + JCCM.LDAP_HOST + ":" + JCCM.LDAP_PORT+"/";
    }
    env.put(Context.PROVIDER_URL, url);
	
    
	// Request the use of the "GSSAPI" SASL mechanism
	// Authenticate by using already established Kerberos credentials
	env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");
	// Optional first argument is comma-separated list of auth, auth-int, 
	// auth-conf
	if (args.length > 0) {
	    env.put("javax.security.sasl.qop", args[0]);
	    dn = args[1];
	} else {
	    dn = "";
	}
    
	try {
	    /* Create initial context */
	    DirContext ctx = new InitialDirContext(env);

	    System.out.println(ctx.getAttributes(dn));

	    // do something useful with ctx
	    ldap.setContext(ctx);
	    // Close the context when we're done
	   
	} catch (NamingException e) {
	    e.printStackTrace();
	}
    }
}
