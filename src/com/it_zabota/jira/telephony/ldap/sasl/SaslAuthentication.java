package com.it_zabota.jira.telephony.ldap.sasl;

import org.apache.log4j.Logger;

import com.it_zabota.jira.telephony.other.UnsetVariableException;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import java.security.PrivilegedAction;
import java.util.Hashtable;

import javax.security.auth.Subject;

public class SaslAuthentication implements PrivilegedAction<InitialDirContext> {

    private String url;
    private String krb5Conf;
    private Subject subject;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKrb5() {
        return krb5Conf;
    }

    public void setKrb5(String krb5Conf) {
        this.krb5Conf = krb5Conf;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public SaslAuthentication(String url, String krb5Conf, Subject subject) throws UnsetVariableException {
        if (url == null) {
            throw new UnsetVariableException("url must be set");
        }
        if (krb5Conf == null) {
            throw new UnsetVariableException("krb5Conf must be set");
        }
        if (subject == null) {
            throw new UnsetVariableException("subject must be set");
        }
        this.url = url;
        this.krb5Conf = krb5Conf;
        this.subject = subject;
    }

    public InitialDirContext run() {
        Hashtable<String, String> environment = new Hashtable<String, String>();

        environment.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put("java.naming.security.authentication", "GSSAPI");
        environment.put("javax.security.sasl.qop", "auth-conf");
        environment.put("java.naming.provider.url", url);
        environment.put("java.security.krb5.conf", krb5Conf);

        try {
            return new InitialDirContext(environment);
        } catch (NamingException ne) {
            Logger.getLogger(this.getClass()).error("", ne);
            return null;
        }
    }

    public InitialDirContext getInitialDirContext() {
        return Subject.doAs(subject, this);
    }
	
}
