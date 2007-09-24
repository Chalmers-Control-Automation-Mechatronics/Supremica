package org.supremica.external.processeditor;

import java.io.*;
import java.util.*;

/**
 * Restricts the file format in SOC when certain dialog window is opened.
 */
public class SOCFileFilter extends javax.swing.filechooser.FileFilter {
    public String[] suf;
    public String description = "";
    
    /**
     * Creates a new instance of the class
     */
    public SOCFileFilter(String suffix) {
	StringTokenizer tok = new StringTokenizer(suffix);
	suf = new String[tok.countTokens()];
	for(int i = 0; i < suf.length; i++) {
	    suf[i] = tok.nextToken();
	    //description += "*"+suf[i]+" ";		
	    description = suf[i];
	}
    }
    /**
     * Whether the given file is accepted by the filter.
     *
     * @param f the file to control acceptance
     */
    public boolean accept(File f) {	  	   
	if(f.isDirectory()) {
	    return true;
	}	    
	for(int i = 0; i < suf.length; i++) {	       
	    if(f.getName().toLowerCase().endsWith(suf[i])) {
		return true;
	    }
	}		   
	return false;	
    }
    /**
     * The description of the filter.
     * 
     * @return the description
     */
    public String getDescription() {
	return description;
    }
}
