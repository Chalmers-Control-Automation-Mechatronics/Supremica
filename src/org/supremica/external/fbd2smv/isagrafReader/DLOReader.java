package org.supremica.external.fbd2smv.isagrafReader;

import java.io.*;
import java.util.*;
import org.supremica.external.fbd2smv.util.*;

public class DLOReader
{
    private LinkedList booleans = new LinkedList();

    public DLOReader(FileReader fr) throws IOException
    {
		parse(fr);
    }


    void parse(FileReader fr) throws IOException
    {
        String input;
	BufferedReader br = new BufferedReader(fr);

        while ((input = br.readLine()) != null )
			{
				if (input.startsWith("#B"))
					{
						booleans.add(input.substring(13, input.indexOf("%", 13)));
					}

			}
    }

    public LinkedList getBooleans()
    {
		return booleans;
    }

}

