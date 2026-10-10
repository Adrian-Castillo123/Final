package org.example;
public class PrintedBook extends Item {
    private final double weight;
    public PrintedBook(int id,String t,double p,String a,int s,double w,String d){super(id,t,p,a,d,s);weight=w;}
    public double getWeight(){return weight;}
    public String getType(){return "Printed Book";}
}
