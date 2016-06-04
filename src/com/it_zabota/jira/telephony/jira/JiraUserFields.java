package com.it_zabota.jira.telephony.jira;

public class JiraUserFields {
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String name;
	public String displayName;
	public String emailAddress;
	private String[] fldAr = new String [] {"name", "displayName", "emailAddress"};
	
	public String getFld(String fld) throws IllegalArgumentException, ReflectiveOperationException, Exception, Throwable {
		String fldVal = "";
		for (String fldIt : fldAr) {
			if (fldIt.equalsIgnoreCase(fld)) {
				fldVal = (String) this.getClass().getField(fldIt).get(fldIt);
				break;
			}
		}
		return fldVal;
	}
}
