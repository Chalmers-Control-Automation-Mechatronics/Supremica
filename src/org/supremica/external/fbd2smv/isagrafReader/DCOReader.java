package org.supremica.external.fbd2smv.isagrafReader;

import java.io.*;
import java.util.*;

public class DCOReader
{
    private LinkedList integers = new LinkedList();

    public DCOReader(FileReader fr) throws IOException
    {
	parse(fr);
    }


    void parse(FileReader fr) throws IOException
    {
        String input;
	BufferedReader br = new BufferedReader(fr);

        while ((input = br.readLine()) != null)
        {
	    System.out.println("\t\t\t DCOReader parse");
	    if (input.startsWith("#A"))
		{
		    if ((input.substring(input.indexOf("%%")+2, input.indexOf("%%")+3)).compareTo("I") == 0)
			{
			    integers.add(input.substring(13, input.indexOf("%", 13)));
			} 
		    else  
			{
			    System.out.println("Undefined data type");
			} 
		} 
	}
			    

    }


    public LinkedList getIntegers()
    {
	return integers;
    }

}

