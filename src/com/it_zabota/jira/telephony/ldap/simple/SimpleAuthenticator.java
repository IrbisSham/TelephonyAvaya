package com.it_zabota.jira.telephony.ldap.simple;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

public class SimpleAuthenticator implements Authenticator {

	  private static InitialDirContext context = null;

	    private String url; // "java.naming.provider.url"
	    private String binddn; // "java.naming.security.principal"
	    private String password; // "java.naming.security.credentials"

	    public InitialDirContext getInitialDirContext() {

	        if (context != null) {
	            try {
	                SearchControls searchControls = new SearchControls();
	                searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);

	                context.search("cn=this is used to test the connection", "objectClass=*", searchControls);
	            } catch (NameNotFoundException nnfe) {
	                return context;
	            } catch (NamingException ne) {
	            }
	            //
	            // we have a bad context
	            //
	        }
	        Hashtable<String, String> env = new Hashtable<String, String>();

	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, url);
	        env.put(Context.SECURITY_PRINCIPAL, binddn);
	        env.put(Context.SECURITY_CREDENTIALS, password);

	        try {
	            context = new InitialDirContext(env);
//	            context.addToEnvironment("java.naming.ldap.attributes.binary", "userPKCS12");
	        } catch (NamingException ne) {
	            context = null;
	        }
	        return context;
	    }

	    public String getUrl() {
	        return url;
	    }

	    public void setUrl(String url) {
	        this.url = url;
	    }

	    public String getBinddn() {
	        return binddn;
	    }

	    public void setBinddn(String binddn) {
	        this.binddn = binddn;
	    }

	    public String getPassword() {
	        return password;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	    }
}
