package org.supremica.external.fbd2smv.fbd2smv;

import java.io.*;
import java.util.*;
import org.supremica.external.fbd2smv.fbd2smv.*;
import org.supremica.external.fbd2smv.fbdProject.*;
import org.supremica.external.fbd2smv.isagrafReader.*;

public class fbd2smv
{
    private String fbdProjectPath;
    private String smvBlocksPath;
    private String smvOutputPath;

    private fbdProject fbdProj;
    private HashMap smvBlocks;


    public fbd2smv(String fbdProjectPath, String smvBlocksPath, String smvOutputPath) throws IOException
    {
	this.fbdProjectPath = fbdProjectPath;
	this.smvBlocksPath  = smvBlocksPath;
	this.smvOutputPath  = smvOutputPath;

	isagrafReader igReader = new isagrafReader(fbdProjectPath);
	fbdProj                = igReader.getFbdProject();


	/* <test> */
  	  LinkedList indices = fbdProj.getOutputVariableIndicesByVariableName("E_Go");
	  System.out.println("indices.size()=" + indices.size());
	
	  for(int i=0; i < indices.size(); i++)
	      {
		  String[] str = (String[])indices.get(i);
		  System.out.println(str[0] + " " + str[1]);
	      }
	
	/* </test> */


		
	LinkedList programs    = fbdProj.getPrograms();
	LinkedList varBooleans = fbdProj.dictionaryGetBooleans();
	LinkedList varIntegers = fbdProj.dictionaryGetIntegers();

	FileWriter  fw  = new FileWriter(smvOutputPath + "controller.smv");
	PrintWriter pw  = new PrintWriter(fw);

	BLKReader blkReader = new BLKReader(smvBlocksPath);
	smvBlocks = blkReader.getBlocks();

	printControllerModule(pw, programs);
	blkReader.printBlocks(pw);
	printMainModule(pw, programs, varBooleans, varIntegers);

	pw.flush();
	pw.close();
    }


    public static void main(String args[]) throws IOException
    {
	fbd2smv theFbd2smv = new fbd2smv(args[0], args[1], args[2]);
    }


    private void translateBoxNames(LinkedList boxes)
    {
	String boxName;

	for (int i=0; i<boxes.size(); i++)
	    {
		boxName = ((BOX)boxes.get(i)).getName();
		if (boxName.equals("=1"))
		    { 
			((BOX)boxes.get(i)).setName("xor");
		    }
		else if (boxName.equals("&"))
		    { 
			((BOX)boxes.get(i)).setName("and");
		    }
		else if (boxName.equals(">=1"))
		    { 
			((BOX)boxes.get(i)).setName("or");
		    }
		else if (boxName.equals(">"))
		    { 
			((BOX)boxes.get(i)).setName("gt");
		    }
		else if (boxName.equals("<"))
		    { 
			((BOX)boxes.get(i)).setName("lt");
		    }
		else if (boxName.equals("+"))
		    { 
			((BOX)boxes.get(i)).setName("add");
		    }
		else if (boxName.equals("-"))
		    { 
			((BOX)boxes.get(i)).setName("substract");
		    }
		else if (boxName.equals("="))
		    { 
			((BOX)boxes.get(i)).setName("equal");
		    }
		else if (boxName.equals("*"))
		    { 
			((BOX)boxes.get(i)).setName("mult");
		    }
		else if (boxName.equals("/"))
		    { 
			((BOX)boxes.get(i)).setName("div");
		    }
	    }
    }


	
    void printControllerModule(PrintWriter pw, LinkedList programs) 
    {
	String str = "";
	String blockName = null;
	String blockName2;

	LinkedList boxes;
	Program program;
	String programName = null;
	String previousProgramName = null;
	String previousBlockName;
	String index = null;
	String previousIndex;

	pw.println("MODULE controller()");
	pw.println("{");
		
	StringBuffer buff = new StringBuffer("\tstate : {idle, read_input, ");
		
	for (int i=0; i<programs.size(); i++)
	    {
		program = (Program)programs.get(i); 
		programName = program.getName();
		boxes = program.getBoxesWithoutCornersList();
		translateBoxNames(boxes);

		for (int j=0; j<boxes.size(); j++)
		    {
			blockName = ((BOX)boxes.get(j)).getName();
			buff.append("compute_" + programName + "_" + blockName + "_" + ((BOX)boxes.get(j)).index + ", ");
		    }

	    }

	buff.append(" write_output};");
	pw.println(buff.toString());


	pw.println("");
	pw.println("");
	pw.println("\tinit(state) := idle;");
	pw.println("\tnext(state) :=");
	pw.println("\t\tcase {");
	pw.println("\t\t\tstate = idle: read_input; ");

	for (int i=0; i<programs.size(); i++)
	    {
		previousProgramName = programName;
		program = (Program)programs.get(i); 
		programName = program.getName();
		boxes = program.getBoxesWithoutCornersList();

		if (boxes.size() > 0) 
		    {
			previousBlockName = blockName;
			previousIndex = index;
			blockName = ((BOX)boxes.get(0)).getName();
			index = ((BOX)boxes.get(0)).index;

			if (i==0)
			    {
				pw.println("\t\t\tstate = read_input: compute_" + programName + "_" + blockName + "_" + ((BOX)boxes.get(0)).index + ";"); 
			    }
			else
			    {
				pw.println("\t\t\tstate = compute_" +  previousProgramName + "_" + previousBlockName +"_" + previousIndex + ": compute_" + programName + "_" + blockName + "_" + ((BOX)boxes.get(0)).index + ";"); 
			    }
		    }
				
		for (int j=1; j<boxes.size(); j++)
		    {
			previousBlockName = blockName;
			previousIndex = index;
			blockName = ((BOX)boxes.get(j)).getName();
			index = ((BOX)boxes.get(j)).index;

						

			pw.println("\t\t\tstate = " + "compute_" + programName + "_" + previousBlockName + "_" + previousIndex  + ": compute_" + programName + "_" + blockName + "_" + index + ";");
		    }

	    }

	pw.println("\t\t\tstate = compute_" + programName + "_" + blockName + "_" + index +  ": read_input;");
		
	pw.println("\t\t\t1: state;");
        pw.println("\t\t};");
	pw.println("");
	pw.println("}");
	pw.println("");
	
    }

    void printMainModule(PrintWriter pw, LinkedList programs, LinkedList varBooleans, LinkedList varIntegers)
    {
	String str = "";
	String blockName;


	//HashMap variables = program.getVariables();
	//LinkedList arcs   = program.getArcs();
	//HashMap boxes         = program.getBoxes()
	LinkedList boxesList;
	Program program;
	HashMap variables;

	pw.println("MODULE main()");
	pw.println("{");

	if (varBooleans.size() > 0 )
	    {
		str = "\t";
		for (int i = 0; i<varBooleans.size(); i++) 
		    {
			str = str + varBooleans.get(i);
			if (i < varBooleans.size() - 1)
			    {
				str = str + ", ";
			    }
		    }
		str = str + ": boolean;";
		pw.println(str);
	    }


	System.out.println("varIntegers.size()=" + varIntegers.size());
	if (varIntegers.size() > 0 )
	    {
		str = "\t";
		for (int i = 0; i<varIntegers.size(); i++) 
		    {
			str = str + varIntegers.get(i);
			if (i < varIntegers.size() - 1)
			    {
				str = str + ", ";
			    }
		    }
		str = str + ": -32..32;";
		pw.println(str);
	    }



	for (int i = 0; i<varBooleans.size(); i++) 
	    {
		String varName = (String)varBooleans.get(i);
			    
		if (!fbdProj.isOutputVariable(varName))

		    {
			pw.println("");
			pw.println("\tnext(" + varName + ") := case");
			pw.println("\t{");
			pw.println("\t\tctrl.state = read_input : {0, 1};");
			pw.println("\t\t1                       : " +  varName + ";");
			pw.println("\t};");
			//}
			//}
		    }
	    }


	for (int i = 0; i<varIntegers.size(); i++) 
	    {
		String varName = (String)varIntegers.get(i);
			    
		if (!fbdProj.isOutputVariable(varName))

		    {
			pw.println("");
			pw.println("\tnext(" + varName + ") := case");
			pw.println("\t{");
			pw.println("\t\tctrl.state = read_input : {-32..32};");
			pw.println("\t\t1                       : " +  varName + ";");
			pw.println("\t};");
		    }
	    }


	pw.println("");
	pw.println("\tctrl: controller;");
	pw.println("");

	// Utskrift av block
        String boxDeclaration = null;
        for(int i=0; i<programs.size(); i++)
            {
                program = (Program)programs.get(i);

                boxesList  = program.getBoxesList();
                for (int j = 0; j<boxesList.size(); j++) {
                    if (!(((BOX)boxesList.get(j)).getName()).equals("{\\div}"))
                        {
                            boxDeclaration = box2Smv((BOX)boxesList.get(j), program);
                            pw.println("\t" + boxDeclaration);
                        }
                }
            }

 


	// Utskrift av output-variabler
	for(int i=0; i<programs.size(); i++)
	    {
		program   = (Program)programs.get(i);
		variables = program.getVariablesByIndex();
		LinkedList outputVariables = new LinkedList();
		LinkedList arcs      = program.getArcs();		
		HashMap    boxes     = program.getBoxes();
		String     outputVariableDeclaration = "";

		for (Iterator varIt = variables.keySet().iterator(); varIt.hasNext();)
		    {
			VAR currVAR = (VAR)variables.get(varIt.next());

			if (fbdProj.isOutputVariable(currVAR.getIndex()) && !outputVariables.contains(currVAR))
			    {
				outputVariables.add(currVAR);
				LinkedList inputElementIndices  = inputElementIndices(currVAR.getIndex(), arcs);

				System.out.println("currVAR.index='" + currVAR.getIndex() + "'");

				System.out.println("OUTPUT VARIABLE: " + currVAR.getName());

				outputVariableDeclaration = "\t" + currVAR.getName() + ":= ";

				String S = null;
 
				System.out.println("i=" + i);

				String  sourceIndex = S.valueOf(((Tuple4)inputElementIndices.get(i)).x);
				int     sourceOutputNumber = ((Tuple4)inputElementIndices.get(i)).y;
				boolean invert = ((Tuple4)inputElementIndices.get(i)).invert;

				FBDElement theFBDElement = getElementByIndex(program, sourceIndex);

				if (theFBDElement.getType().equals("variable") ) 
				    {
					System.out.println("*** 1 ***");
					if (invert)
					    {
						outputVariableDeclaration =  outputVariableDeclaration + "!" + ((VAR)theFBDElement.getElement()).getName() + ", ";
					    }
					else
					    {
						 outputVariableDeclaration =  outputVariableDeclaration + ((VAR)theFBDElement.getElement()).getName() + ", ";
					    }
				    } 
				else if (theFBDElement.getType().equals("box") ) 
				    {
					System.out.println("*** 2 ***");

					/*
					 * Ta reda på vilken av input-boxens utsignaler
					 * som ska kopplas till den aktuella boxens insignal
					 * 
					 */

					BOX box2 = ((BOX)theFBDElement.getElement());
					if ((box2.getName().equals("{\\div}")))
					    {
						System.out.println("*** 3 ***");

						LinkedList divInputIndices = new LinkedList();
						boolean moreDivs = true;
						FBDElement theFBDElement2 = null;
						String sourceIndex2 = null;
						while (moreDivs)
						    {
							moreDivs = false;
							divInputIndices = inputElementIndices(box2.getIndex(), arcs);
							    
							sourceIndex2 = S.valueOf(((Tuple4)divInputIndices.get(0)).x);

							theFBDElement2 = getElementByIndex(program, sourceIndex2);
							if (theFBDElement2.getType().equals("box"))
							    {
								box2 = (BOX)boxes.get(sourceIndex2);
								moreDivs = box2.getName().equals("{\\div}");
							    }
						    }

						theFBDElement = theFBDElement2;
						sourceIndex = sourceIndex2;
					    }
					    


					if (invert)
					    {
						 outputVariableDeclaration =  outputVariableDeclaration + "!";
					    }

					if (theFBDElement.getType().equals("variable") ) 
					    {
						boxDeclaration =  outputVariableDeclaration + ((VAR)theFBDElement.getElement()).getName() + ", ";
					    } 
				
					else
					    {

						String formalArgName = ((Block)smvBlocks.get(((BOX)theFBDElement.getElement()).getName())).getOutputArgumentName(sourceOutputNumber);
						 outputVariableDeclaration =  outputVariableDeclaration + program.getName() + "_" + ((Block)smvBlocks.get(((BOX)theFBDElement.getElement()).getName())).getName() + "_" + sourceIndex + "." + formalArgName + "; ";
			
					    }

					pw.println(outputVariableDeclaration);
				    }


				

			    }
		    }

	    }
	
	System.out.println(boxDeclaration);
	pw.println("}");
    }

    

    public String box2Smv(BOX box, Program program) 
    {
	String     blockName;
	String     boxDeclaration = null;
	FBDElement theFBDElement  = null;
		
	//	HashMap variables    = program.getVariables();
	HashMap boxes        = program.getBoxes();
	LinkedList arcs      = program.getArcs();

	LinkedList inputElementIndices  = inputElementIndices(box.getIndex(), arcs);
	LinkedList outputElementIndices = outputElementIndices(box.getIndex(), arcs);
						
	blockName = box.getName();

	boxDeclaration = program.getName() + "_" + blockName + "_" + box.index + ": ";
	boxDeclaration = boxDeclaration + blockName + "(";

	
	/*
	 * Input elements
	 */
	for (int i=0; i<inputElementIndices.size(); i++)
	    {
		String S = null;
 
		String  sourceIndex = S.valueOf(((Tuple4)inputElementIndices.get(i)).x);
		int     sourceOutputNumber = ((Tuple4)inputElementIndices.get(i)).y;
		boolean invert = ((Tuple4)inputElementIndices.get(i)).invert;

		theFBDElement = getElementByIndex(program, sourceIndex);

		if (theFBDElement.getType().equals("variable") ) 
		    {
			if (invert)
			    {
				boxDeclaration = boxDeclaration + "!" + ((VAR)theFBDElement.getElement()).getName() + ", ";
			    }
			else
			    {
				boxDeclaration = boxDeclaration + ((VAR)theFBDElement.getElement()).getName() + ", ";
			    }
		    } 
		else if (theFBDElement.getType().equals("box") ) 
		    {
			/*
			 * Ta reda på vilken av input-boxens utsignaler
			 * som ska kopplas till den aktuella boxens insignal
			 * 
			 */

			BOX box2 = ((BOX)theFBDElement.getElement());
			if ((box2.getName().equals("{\\div}")))
			    {
				LinkedList divInputIndices = new LinkedList();
				boolean    moreDivs = true;
				FBDElement theFBDElement2 = null;
				String     sourceIndex2 = null;

				while (moreDivs)
				    {
					moreDivs = false;
					divInputIndices = inputElementIndices(box2.getIndex(), arcs);
							    
					sourceIndex2 = S.valueOf(((Tuple4)divInputIndices.get(0)).x);

					theFBDElement2 = getElementByIndex(program, sourceIndex2);
					if (theFBDElement2.getType().equals("box"))
					    {
						box2 = (BOX)boxes.get(sourceIndex2);
						moreDivs = box2.getName().equals("{\\div}");
					    }
				    }

				theFBDElement = theFBDElement2;
				sourceIndex = sourceIndex2;
			    }
					    


			if (invert)
			    {
				boxDeclaration = boxDeclaration + "!";
			    }


			if (theFBDElement.getType().equals("variable") ) 
			    {
				boxDeclaration = boxDeclaration + ((VAR)theFBDElement.getElement()).getName() + ", ";
			    } 
			else
			    {

				System.out.println("%%% BOX.getname()='" + ((BOX)theFBDElement.getElement()).getName() + "'");

				String formalArgName = ((Block)smvBlocks.get(((BOX)theFBDElement.getElement()).getName())).getOutputArgumentName(sourceOutputNumber);
				boxDeclaration = boxDeclaration + program.getName() + "_" + ((Block)smvBlocks.get(((BOX)theFBDElement.getElement()).getName())).getName() + "_" + sourceIndex + "." + formalArgName + ", ";
			    }
		    }
	    }




	/*
	 * Output elements
	 */

	/*
	  for (int i=0; i<outputElementIndices.size(); i++)
	  {
	  String S = null;
		
	  String targetIndex = S.valueOf(((Tuple4)outputElementIndices.get(i)).x);
	  int targetInputNumber = ((Tuple4)outputElementIndices.get(i)).y;

	  theFBDElement = getElementByIndex(program, targetIndex);

	  if (theFBDElement.getType().equals("variable") ) 
	  {
	  boxDeclaration = boxDeclaration + ((VAR)theFBDElement.getElement()).getName() + ", ";
	  } 

	  else if (theFBDElement.getType().equals("box") ) 
	  {

	  System.out.println("BLOCKNAME (output): " + ((BOX)theFBDElement.getElement()).getName()  + "_" + ((BOX)theFBDElement.getElement()).getIndex());
					    



	  String formalArgName = ((Block)smvBlocks.get(((BOX)theFBDElement.getElement()).getName())).getInputArgumentName(targetInputNumber);

	  boxDeclaration = boxDeclaration + program.getName() + "_" + ((Block)smvBlocks.get(((BOX)theFBDElement.getElement()).getName())).getName() + "_" + targetIndex + "." + formalArgName + ", ";
	  }

	  }

	*/

	/* compute box */
	boxDeclaration = boxDeclaration + "ctrl.state = compute_" + program.getName() + "_" + blockName + "_" + box.index + ");";

	return boxDeclaration;
    }



    /*  Inargument: 
     *   boxIndex: index för den box som undersöks
     *   theArcs : lista med alla Arcs
     *
     *  Returnerar:
     *   en lista med index för de element som den undersökta boxen
     *   har som inargument
     */
    public LinkedList inputElementIndices(String boxIndex, List theArcs)
    {
	LinkedList theInputElementIndices = new LinkedList();
	String S = null;

	for (Iterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
	    {
		ARC currARC = (ARC)arcIt.next();
		if (boxIndex.equals(S.valueOf(currARC.getTargetIndex()))) 
		    {
			theInputElementIndices.add(new Tuple4(currARC.getSourceIndex(), currARC.getSourceOutputNumber(), currARC.getTargetInputNumber(), currARC.getInvert()));
		    }
            }

	Collections.sort(theInputElementIndices);

	return theInputElementIndices;
    }




    /*  Inargument: 
     *   boxIndex: index för den box som undersöks
     *   theArcs : lista med alla Arcs
     *
     *  Returnerar:
     *   en lista med index för de element som den undersökta boxen
     *   har som utargument
     */
    public LinkedList outputElementIndices(String boxIndex, List theArcs)
    {
	LinkedList theOutputElementIndices = new LinkedList();
	String S = null;

	for (Iterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
	    {
		ARC currARC = (ARC)arcIt.next();
		if (boxIndex.equals(S.valueOf(currARC.getSourceIndex()))) 
		    {
			theOutputElementIndices.add(new Tuple4(currARC.getTargetIndex(), currARC.getTargetInputNumber(), currARC.getSourceOutputNumber(), currARC.getInvert()));
		    }
            }

	Collections.sort(theOutputElementIndices);

	return theOutputElementIndices;
    }


    class FBDElement
    {
	Object element;
	String type;

	public FBDElement(Object e, String t)
	{
	    this.element = e;
	    this.type = t;
	}

	public Object getElement()
	{
	    return element;
	}

	public String getType()
	{
	    return type;
	}
    }
    


    public FBDElement getElementByIndex(Program program, String index)
    {
	HashMap variables        = program.getVariablesByIndex();
	HashMap boxes            = program.getBoxes();
	FBDElement theFBDElement = null;

	if (variables.containsKey(index))
	    {
		theFBDElement = new FBDElement(variables.get(index), "variable");
		return theFBDElement;
	    }
	/*
	  if (theRelays.containsKey(index))
	  {
	  return theRelays.get(index);
	  }
	*/
	if (boxes.containsKey(index))
	    {
		theFBDElement = new FBDElement(boxes.get(index), "box");
		return theFBDElement;
	    }

	return null;
    }




    class Tuple4 implements java.lang.Comparable
    {
	public int x;
	public int y;
	public int z;
	public boolean invert;
    
	public Tuple4(int x, int y, int z, boolean invert)
	{
	    this.x = x;
	    this.y = y;
	    this.z = z;
	    this.invert = invert;

	}

	public int compareTo(Object o)
	{
	    Integer I;

	    int obj_z = ((Tuple4)o).z;

	    if (this.z < obj_z)
		{
		    return -1;
		} else 
		    { 
			if (this.z == obj_z)
			    {
				return 0;
			    } 
			else
			    {
				return 1;
			    }
		    }
	}


    }




}
