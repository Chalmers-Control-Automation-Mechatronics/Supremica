package org.supremica.external.fbd2smv.util;

import java.io.*;
import java.util.*;

public class FileFinder
{
    public LinkedList getFiles(String path, String fileExtention)
    {
	File f = new File(path);
	LinkedList fileList = new LinkedList();

	String[] files = f.list();
	for (int i = 0; i < files.length; i++) 
	    {
		File f2 = new File(path, files[i]);
		if (f2.isFile())
		    {
			String name = f2.getName();
			String ext = name.substring(name.length()-3, name.length());
					
			if (ext.equals(fileExtention))
			    {
				fileList.add(name);
			    }
		    }
	    }

	Collections.sort(fileList);

	return fileList;
    }

}


