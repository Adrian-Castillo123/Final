package org.example;
public class Ebook extends Item {
    private final String fileFormat; private final double fileSize;
    public Ebook(int id,String t,double p,String a,String f,double z,String d){super(id,t,p,a,d,100);fileFormat=f;fileSize=z;}
    public String getFileFormat(){return fileFormat;} public double getFileSize(){return fileSize;} public String getType(){return "E-book";}
}
