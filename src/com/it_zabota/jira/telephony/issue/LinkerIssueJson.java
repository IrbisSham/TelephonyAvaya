package com.it_zabota.jira.telephony.issue;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.it_zabota.jira.telephony.main.JCCM;

public class LinkerIssueJson {
	
	private JSONObject issueLink;

	
	public JSONObject getIssueLink() {
		return issueLink;
	}

	public LinkerIssueJson(String keyInwardIssue, String keyOutwardIssue) throws JSONException {
		JSONObject type = new JSONObject();
		type.put("name", JCCM.JIRA_ISSUE_FIELD_LINK_TYPE_VALUE);
		JSONObject inwardIssue = new JSONObject();
		inwardIssue.put("key", keyInwardIssue);
		JSONObject outwardIssue = new JSONObject();
		outwardIssue.put("key", keyOutwardIssue);
//		JSONObject comment = new JSONObject();
//		comment.put("body", JCCM.JIRA_ISSUE_LINK_COMMENT);
//		JSONObject visibility = new JSONObject();
//		visibility.put("type", JCCM.JIRA_ISSUE_LINK_VISIBILITY_TYPE);
//		visibility.put("value", JCCM.JIRA_ISSUE_LINK_VISIBILITY_VALUE);		
//		comment.put("visibility", visibility);
		
		issueLink = new JSONObject();
		issueLink.put("type", type);
		issueLink.put("inwardIssue", inwardIssue);
		issueLink.put("outwardIssue", outwardIssue);
//		issueLink.put("comment", comment);
		
		
	}

	

	
}
