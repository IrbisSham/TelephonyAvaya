package com.it_zabota.jira.telephony.ldap.simple;

import javax.naming.directory.DirContext;

public interface Authenticator {
	public DirContext getInitialDirContext();

}
