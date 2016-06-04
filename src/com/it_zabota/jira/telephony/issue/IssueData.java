/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.it_zabota.jira.telephony.issue;

/**
 * Класс для хранения данных о запросе.
 * @author Vers_us
 */
public class IssueData {

    private String phoneNumber;
    private int sla;
    private String company = "";
    private String user = "";
    private String comment;

    public IssueData() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getSla() {
        return sla;
    }

    public void setSla(int sla) {
        this.sla = sla;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
