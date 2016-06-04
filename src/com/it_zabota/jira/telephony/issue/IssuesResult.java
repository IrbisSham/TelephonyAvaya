package com.it_zabota.jira.telephony.issue;

public class IssuesResult {
public String getIssueKey() {
		return issueKey;
	}
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}
	public String getIssueType() {
		return issueType;
	}
	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}
	public String getIssueTheme() {
		return issueTheme;
	}
	public void setIssueTheme(String issueTheme) {
		this.issueTheme = issueTheme;
	}
	public String getIssueDate() {
		return issueDate;
	}
	public void setIssueDate(String issueDate) {
		this.issueDate = issueDate;
	}
	public String getIssueStatus() {
		return issueStatus;
	}
	public void setIssueStatus(String issueStatus) {
		this.issueStatus = issueStatus;
	}
	//	"№", "Тип обращения", "Тема", "Дата", "Статус"
	private String issueKey;
	private String issueType;
	private String issueTheme;
	private String issueDate;
	private String issueStatus;
}
