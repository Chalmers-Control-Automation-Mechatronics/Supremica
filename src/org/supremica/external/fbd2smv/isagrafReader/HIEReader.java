package org.supremica.external.fbd2smv.isagrafReader;

import java.io.*;
import java.util.*;
import org.supremica.external.fbd2smv.util.*;

public class HIEReader
{

    HashMap programs = new HashMap();

    public HIEReader(FileReader fr) throws IOException
    {
	BufferedReader br = new BufferedReader(fr);
	parse(br);
    }


    void parse(BufferedReader br) throws IOException
    {
        String input;
	int index = 0;
	
        while ((input = br.readLine()) != null)
	    {
		if (input.endsWith("(_FBD)"))
		    {
			programs.put(input.substring(0, input.indexOf("(_FBD)")), new Integer(index));
			index++;
		    }
	    }
    }
    
}
