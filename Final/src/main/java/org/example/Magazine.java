package org.example;
public class Magazine extends Item {
    private final String issueNo,month,publisher; private final int year;
    public Magazine(int id,String t,double p,String a,String issue,String month,int year,String pub,String d){super(id,t,p,a,d,20);issueNo=issue;this.month=month;this.year=year;publisher=pub;}
    public String getIssueNo(){return issueNo;} public String getMonth(){return month;} public int getYear(){return year;} public String getPublisher(){return publisher;} public String getType(){return "Magazine";}
}
