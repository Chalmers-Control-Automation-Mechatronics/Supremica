package org.supremica.external.fbd2smv.isagrafReader;

import java.io.*;
import java.util.*;

public class HIEReader
{

    LinkedList programs = new LinkedList();

    public HIEReader(FileReader fr) throws IOException
    {
	BufferedReader br = new BufferedReader(fr);
	parse(br);
    }


    void parse(BufferedReader br) throws IOException
    {
        String input;
	
        while ((input = br.readLine()) != null)
	    {
		if (input.endsWith("(_FBD)"))
		    {
			programs.add(input.substring(0, input.indexOf("(_FBD)")));
		    }
	    }
    }

    public LinkedList getPrograms()
    {
	return programs;
    }
    
}
