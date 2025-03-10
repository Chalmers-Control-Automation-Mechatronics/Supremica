package org.supremica.external.fbd2smv.isagrafReader;

import java.io.*;
import java.util.*;

public class LSFReader
{
	private HashMap variablesByName = new HashMap();
	private HashMap variablesByIndex = new HashMap();
	private HashMap boxes = new HashMap();
	private LinkedList arcs = new LinkedList();
	private LinkedList fbdElements;
	private String programName;
	private int programIndex;

	public LSFReader(FileReader fr, String programName, int programIndex, LinkedList fbdElements)
		throws IOException
	{
		this.programName = programName;
		this.programIndex = programIndex;
		this.fbdElements = fbdElements;

		BufferedReader br = new BufferedReader(fr);

		parse(br);
	}

	public HashMap getVariablesByName()
	{
		return variablesByName;
	}

	public HashMap getVariablesByIndex()
	{
		return variablesByIndex;
	}

	public HashMap getBoxes()
	{
		return boxes;
	}

	public LinkedList getArcs()
	{
		return arcs;
	}

	public LinkedList getFbdElements()
	{
		return fbdElements;
	}

	void parse(BufferedReader br)
		throws IOException
	{
		String input;

		while ((input = br.readLine()) != null)
		{
			if (input.startsWith("#"))
			{
				continue;
			}

			if (input.startsWith("@VAR:"))
			{
				readVAR(input);
			}
			else if (input.startsWith("@BOX:"))
			{
				readBOX(input);
			}
			else if (input.startsWith("@ARC:"))
			{
				readARC(input, false);
			}
			else if (input.startsWith("@RLD:"))
			{
				continue;

				//readRLD(input);
			}
			else if (input.startsWith("@NOT:"))
			{
				readARC(input, true);
			}
			else if (input.startsWith("@TXT:"))
			{
				continue;

				//readTXT(input);
			}
			else if (input.startsWith("@@NBID"))
			{
				continue;
			}
			else
			{
				System.err.println("Unknown tag: " + input);
			}
		}
	}

	public void readVAR(String line)
	{
		String indexString = null;
		String varName = null;
		Integer I = null;
		int x = -1;
		int y = -1;
		StringTokenizer tokenizer = new StringTokenizer(line, ",");

		for (int i = 0; tokenizer.hasMoreTokens(); i++)
		{
			String currToken = tokenizer.nextToken();

			if (i == 0)
			{
				indexString = currToken.substring(5, currToken.length());
			}
			else if (i == 1)
			{
				if (!(currToken.equals("Y=N") || currToken.equals("Y=D")))
				{
					System.err.println("Unknown token: " + currToken);
				}
			}
			else if (i == 2)
			{
				if (!currToken.startsWith("P="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				x = Integer.valueOf(currToken.substring(3, currToken.length())).intValue();
				currToken = tokenizer.nextToken();
				y = Integer.valueOf(currToken.substring(0, currToken.length() - 1)).intValue();
			}
			else if (i == 3)
			{
				if (!currToken.startsWith("S="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				tokenizer.nextToken();
			}
			else if (i == 4)
			{
				if (!currToken.startsWith("C="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				tokenizer.nextToken();
			}
			else if (i == 5)
			{
				if (!currToken.startsWith("X="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				varName = currToken.substring(2, currToken.length());
			}
		}

		if (varName == null)
		{
			System.err.println("Error: varName is null");
		}

		if (indexString == null)
		{
			System.err.println("Error: indexString is null");
		}

		VAR newVAR = new VAR(indexString, varName);

		if (varName.equals("TRUE"))
		{
			varName = "1";
		}

		if (varName.equals("FALSE"))
		{
			varName = "0";
		}

		char c = varName.charAt(0);
		String elementType = null;

		if (Character.isLetter(c))
		{
			elementType = "variable";
		}
		else
		{
			elementType = "constant";
		}

		if (varName.length() > 1)
		{
			System.out.println(varName.substring(0, 2));

			if ((varName.substring(0, 2)).equals("t#"))
			{
				elementType = "constant";
			}
		}

		variablesByIndex.put(newVAR.getIndex(), newVAR);
		variablesByName.put(newVAR.getName(), newVAR);
		fbdElements.add(new FBDElement(programName, programIndex, (String) newVAR.getName(), elementType, Integer.valueOf(indexString).intValue(), x, y));
	}

	public void readBOX(String line)
	{
		String indexString = null;
		String boxName = null;
		String x = null;
		String y = null;
		Integer I = null;
		StringTokenizer tokenizer = new StringTokenizer(line, ",");

		for (int i = 0; tokenizer.hasMoreTokens(); i++)
		{
			String currToken = tokenizer.nextToken();

			if (i == 0)
			{
				indexString = currToken.substring(5, currToken.length());
			}
			else if (i == 1)
			{
				if (!(currToken.equals("Y=N") || currToken.equals("Y=D")))
				{
					System.err.println("Unknown token: " + currToken);
				}
			}
			else if (i == 2)
			{
				if (!currToken.startsWith("P="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				x = currToken.substring(3, currToken.length());
				currToken = tokenizer.nextToken();
				y = currToken.substring(0, currToken.length() - 1);
			}
			else if (i == 3)
			{
				if (!currToken.startsWith("S="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				tokenizer.nextToken();
			}
			else if (i == 4)
			{
				if (!currToken.startsWith("C="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				tokenizer.nextToken();
			}
			else if (i == 5)
			{
				if (!currToken.startsWith("X="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				boxName = currToken.substring(2, currToken.length());
			}
		}

		if (boxName == null)
		{
			System.err.println("Error: boxName is null");
		}

		if (indexString == null)
		{
			System.err.println("Error: indexString is null");
		}

		BOX newBOX = new BOX(indexString, boxName, x, y);

		boxes.put(newBOX.getIndex(), newBOX);

		String elementType;

		if (boxName.equals("{\\div}"))
		{
			elementType = "corner";
		}
		else
		{
			elementType = "box";
		}

		fbdElements.add(new FBDElement(programName, programIndex, (String) newBOX.getName(), elementType, Integer.valueOf(indexString).intValue(), Integer.valueOf(x).intValue(), Integer.valueOf(y).intValue()));
	}

	public void readARC(String line, boolean invert)
	{
		/* Index for the source object (Variable or Box), ie the object to the left of the ARC */
		int sourceIndex = -1;

		/* A source Variable has exactly one output. For a variable sourceOutputNumber = 0.
		 * A source Box may have one or more outputs. Outputs are numbered from 0 (top) to N-1 (bottom).
		 */
		int sourceOutputNumber = -1;
		/* Index for the target object (Variable or Box), ie the object to the right of the ARC */
		int targetIndex = -1;

		/* A target Variable has exactly one input. For a variable targetInputNumber = 0.
		 * A target Box may have one or more inputs. Inputs are numbered from 0 (top) to N-1 (bottom).
		 */
		int targetInputNumber = -1;
		Integer I = null;
		StringTokenizer tokenizer = new StringTokenizer(line, ",");

		for (int i = 0; tokenizer.hasMoreTokens(); i++)
		{
			String currToken = tokenizer.nextToken();

			if (i == 0) {}
			else if (i == 1)
			{
				if (!(currToken.equals("Y=N") || currToken.equals("Y=D")))
				{
					System.err.println("Unknown token: " + currToken);
				}
			}
			else if (i == 2)
			{
				if (!currToken.startsWith("Z="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				tokenizer.nextToken();
			}
			else if (i == 3)
			{
				if (!currToken.startsWith("F="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				sourceIndex = Integer.valueOf(currToken.substring(3, currToken.length())).intValue();
				currToken = tokenizer.nextToken();
				sourceOutputNumber = Integer.valueOf(currToken.substring(0, currToken.length() - 1)).intValue();
			}
			else if (i == 4)
			{
				if (!currToken.startsWith("T="))
				{
					System.err.println("Unknown token: " + currToken);
				}

				targetIndex = Integer.valueOf(currToken.substring(3, currToken.length())).intValue();
				currToken = tokenizer.nextToken();
				targetInputNumber = Integer.valueOf(currToken.substring(0, currToken.length() - 1)).intValue();
			}
		}

		ARC newARC = new ARC(sourceIndex, sourceOutputNumber, targetIndex, targetInputNumber, invert);

		arcs.add(newARC);
	}
}
