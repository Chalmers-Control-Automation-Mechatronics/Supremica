package org.supremica.external.fbd2smv.fbd2smv;

import org.supremica.external.fbd2smv.util.*;
import java.io.*;
import java.util.*;

public class BLKReader
{
    private HashMap blocks    = new HashMap();
    private LinkedList blkFiles  = new LinkedList();
    private LinkedList arguments = new LinkedList();

    private String path;

    public BLKReader(String path) throws IOException
    {
	this.path = path;

	FileFinder fileFinder = new FileFinder();
	blkFiles = fileFinder.getFiles(path, "blk");

	for (int i=0; i<blkFiles.size(); i++)
	    {
		Block block = parse(path + blkFiles.get(i)); 
		blocks.put(block.getName(), block);
	    }

    }


    Block parse(String file) throws IOException
    {

        String input;
	FileReader fr = new FileReader(file);
	BufferedReader br = new BufferedReader(fr);
	Block block;
	LinkedList inputArguments = new LinkedList();
	LinkedList outputArguments = new LinkedList();
	String blockName = null;
	String isagrafBlockName;

	System.out.println("   File:" + file); 

        while ((input = br.readLine()) != null)
	    {
		if ((input.trim()).startsWith("input")  )
		    {
			StringTokenizer tokenizer = new StringTokenizer((input.substring(input.indexOf("input")+6, input.indexOf(":"))), ",");
			String type = (input.substring(input.indexOf(":")+1, input.indexOf(";"))).trim();
			for (int i=0; tokenizer.hasMoreTokens(); i++)
			    {
				String currToken = (tokenizer.nextToken()).trim();

				System.out.println("      Input: " + currToken);

				inputArguments.add(new Argument(currToken, type));
			    }
		    } 
		else if ((input.trim()).startsWith("output"))
		    {
			StringTokenizer tokenizer = new StringTokenizer((input.substring(input.indexOf("output")+7, input.indexOf(":"))), ",");
			String type = (input.substring(input.indexOf(":")+1, input.indexOf(";"))).trim();
						
			for (int i=0; tokenizer.hasMoreTokens(); i++)
			    {
				String currToken = (tokenizer.nextToken()).trim();

				System.out.println("      Output: " + currToken);

				outputArguments.add(new Argument(currToken, type));
			    }
		    } 
		else if ((input.trim()).startsWith("module"))
		    {
			blockName = input.substring(7, input.indexOf("("));
		    } 
	    }
	fr.close();

	System.out.println("return new Block: " + blockName);
	return new Block(blockName, inputArguments, outputArguments);
    }

    
    public void printBlocks(PrintWriter pw) throws IOException
    {
	String input;
	FileReader fr;
	Block block;

	for (Iterator blockIt = blocks.values().iterator(); blockIt.hasNext(); )
	    {
		block = (Block)blockIt.next();

		fr = new FileReader(path + block.getName() + ".blk");
		BufferedReader br = new BufferedReader(fr);
	
		while ((input = br.readLine()) != null)
		    {
			pw.println(input);
		    }
		pw.println("");
		fr.close();
	    }

    } 


    public HashMap getBlocks()
    {
	return blocks;
    }

}

