package com.it_zabota.jira.telephony.ldap.sasl;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

import com.it_zabota.jira.telephony.ldap.LdapUtils;
import com.it_zabota.jira.telephony.ldap.test.SampleCallbackHandler;
import com.it_zabota.jira.telephony.main.JCCM;

public class GssLogging {
	private static Logger logger = Logger.getLogger(JCCM.class);
	public static void main(String[] args, LdapUtils ldap) {
		 LoginContext lc = null;
		 try {
	          lc = new LoginContext("GssLogging", 
	                      new SampleCallbackHandler());
	      } catch (LoginException le) {
	    	  logger.error("Cannot create LoginContext. "
	              + le.getMessage());
	      } catch (SecurityException se) {
	    	  logger.error("Cannot create LoginContext. "
	              + se.getMessage());
	      } 

	      try {
	    
	          // attempt authentication
	          lc.login();
//		      logger.info("Authentication succeeded!");
		      try {
		    	  Subject.doAs(lc.getSubject(), new JndiAction(args, ldap));
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("JndiAction failed: " + e.getMessage());
			}
			  
	      } catch (LoginException le) {
	    
	    	  logger.error("Authentication failed: " + le.getMessage());
	    
	      }
	    
		// 2. Perform JNDI work as logged in subject
	    
	    }		
}
