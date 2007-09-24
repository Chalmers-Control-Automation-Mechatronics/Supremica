package org.supremica.external.processeditor.xml;

import java.io.*;

public interface  FileConverter {  
    public void save(File file, Object o, FileConverter fc);
    public Object open(File file, FileConverter fc);
    public void newResource(String s);
    public void newResource(String s, String id);
    public void newRelation(String s);
    public void addRelation();
    public void newOperation(String s);   
    public void addOperation();	
    public void addAttribute(String s);
    public void addPredecessor(String a);
    public void addDescription(String s);
    public Object getResource();    
    public void saveResource(File file);
    public FileConverter newInstance();
}
