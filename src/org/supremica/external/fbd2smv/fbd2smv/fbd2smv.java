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
		
	LinkedList programs    = fbdProj.getPrograms();
	LinkedList varBooleans = fbdProj.dictionaryGetBooleans();
	LinkedList varIntegers = fbdProj.dictionaryGetIntegers();

	FileWriter  fw  = new FileWriter(smvOutputPath + "controller.smv");
	PrintWriter pw  = new PrintWriter(fw);

	BLKReader blkReader = new BLKReader(smvBlocksPath);
	smvBlocks = blkReader.getBlocks();

	LinkedList fbdElements = fbdProj.getFBDElements();
	translateBoxNames2(fbdElements);


	printControllerModule(pw, fbdElements);
	blkReader.printBlocks(pw);
	printMainModule(pw, programs, varBooleans, varIntegers, fbdElements);

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

    private void translateBoxNames_H(HashMap boxes)
    {
	String boxName;

	for (Iterator boxIt = boxes.keySet().iterator(); boxIt.hasNext();)
	    {
		String boxKey = (String)boxIt.next();
		BOX currentBox = (BOX)boxes.get(boxKey);
		boxName = currentBox.getName();

		if (boxName.equals("=1"))
		    { 
			((BOX)boxes.get(boxKey)).setName("xor");
		    }
		else if (boxName.equals("&"))
		    { 
			((BOX)boxes.get(boxKey)).setName("and");
		    }
		else if (boxName.equals(">=1"))
		    { 
			((BOX)boxes.get(boxKey)).setName("or");
		    }
		else if (boxName.equals(">"))
		    { 
			((BOX)boxes.get(boxKey)).setName("gt");
		    }
		else if (boxName.equals("<"))
		    { 
			((BOX)boxes.get(boxKey)).setName("lt");
		    }
		else if (boxName.equals("+"))
		    { 
			((BOX)boxes.get(boxKey)).setName("add");
		    }
		else if (boxName.equals("-"))
		    { 
			((BOX)boxes.get(boxKey)).setName("substract");
		    }
		else if (boxName.equals("="))
		    { 
			((BOX)boxes.get(boxKey)).setName("equal");
		    }
		else if (boxName.equals("*"))
		    { 
			((BOX)boxes.get(boxKey)).setName("mult");
		    }
		else if (boxName.equals("/"))
		    { 
			((BOX)boxes.get(boxKey)).setName("div");
		    }
	    }
    }



    private void translateBoxNames2(LinkedList fbdElements)
    {
	String boxName;
	
	for (int i=0; i<fbdElements.size(); i++)
	    {
		FBDElement fbdElement = (FBDElement)fbdElements.get(i);

		if(fbdElement.getElementType().equals("box"))
		    {
		    
			boxName = fbdElement.getElementName();

			if (boxName.equals("=1"))
			    { 
				fbdElement.setElementName("xor");
			    }
			else if (boxName.equals("&"))
			    { 
				fbdElement.setElementName("and");
			    }
			else if (boxName.equals(">=1"))
			    { 
				fbdElement.setElementName("or");
			    }
			else if (boxName.equals(">"))
			    { 
				fbdElement.setElementName("gt");
			    }
			else if (boxName.equals("<"))
			    { 
				fbdElement.setElementName("lt");
			    }
			else if (boxName.equals("+"))
			    { 
				fbdElement.setElementName("add");
			    }
			else if (boxName.equals("-"))
			    { 
				fbdElement.setElementName("substract");
			    }
			else if (boxName.equals("="))
			    { 
				fbdElement.setElementName("equal");
			    }
			else if (boxName.equals("*"))
			    { 
				fbdElement.setElementName("mult");
			    }
			else if (boxName.equals("/"))
			    { 
				fbdElement.setElementName("div");
			    }
		    }
	    }
    }

	
    	
    void printControllerModule(PrintWriter pw, LinkedList fbdElements) 
    {
	String str = "";
	String blockName = null;
	String blockName2;

	LinkedList boxes;
	Program program;
	//	String programName = null;
	//	String previousProgramName = null;
	String previousBlockName;
	String index = null;
	String previousIndex;
	String S = null;

	pw.println("MODULE controller()");
	pw.println("{");


	/*  Declaration of states
	 *
	 */
	StringBuffer buff = new StringBuffer("\tstate : {idle, read_input, ");

	for (int i=0; i<fbdElements.size(); i++)
	{
	    FBDElement fbdElement = (FBDElement)fbdElements.get(i);
	    String programName = fbdElement.getProgramName();
	    String elementType = fbdElement.getElementType();
	    String elementName = fbdElement.getElementName();
	    String elementIndex = S.valueOf(fbdElement.getElementIndex());

	    if (elementType.equals("variable") || elementType.equals("box"))
		{
		    buff.append("compute_" + programName + "_" + elementName + "_" + elementIndex + ", ");
		}
	}

	buff.append(" write_output};");
	pw.println(buff.toString());

	
	/*  Assignment of state values
	 *
	 */
	pw.println("");
	pw.println("");
	pw.println("\tinit(state) := idle;");
	pw.println("\tnext(state) :=");
	pw.println("\t\tcase {");
	pw.println("\t\t\tstate = idle: read_input; ");


	String previousProgramName  = null;
	String previousElementName  = null;
	String previousElementIndex = null;

	for (int i=0; i<fbdElements.size(); i++)
	{
	    FBDElement fbdElement = (FBDElement)fbdElements.get(i);
	    String programName = fbdElement.getProgramName();
	    String elementType = fbdElement.getElementType();
	    String elementName = fbdElement.getElementName();
	    String elementIndex = S.valueOf(fbdElement.getElementIndex());

	    if (elementType.equals("variable") || elementType.equals("box"))
		{
		    if (i==0)
			{
			    pw.println("\t\t\tstate = read_input: compute_" + programName + "_" + elementName + "_" + elementIndex + ";"); 
			}
		    
		    else
			{
			    pw.println("\t\t\tstate = compute_" +  previousProgramName + "_" + previousElementName +"_" + previousElementIndex + ": compute_" + programName + "_" + elementName + "_" + elementIndex + ";"); 
			}
		    previousProgramName  = programName;
		    previousElementName  = elementName;
		    previousElementIndex = elementIndex;
		}
	}


	
	pw.println("\t\t\tstate = compute_" + previousProgramName + "_" + previousElementName + "_" + previousElementIndex +  ": read_input;");
		
	pw.println("\t\t\t1: state;");
        pw.println("\t\t};");
	pw.println("");
	pw.println("}");
	pw.println("");
	
    }




    void printMainModule(PrintWriter pw, LinkedList programs, LinkedList varBooleans, LinkedList varIntegers, LinkedList fbdElements)
    {
	StringBuffer buff = new StringBuffer("");

	pw.println("MODULE main()");
	pw.println("{");

	
       /* Declaration of boolean variables
	*
	*/
	if (varBooleans.size() > 0 )
	    {
		buff.append("\t");
		for (int i = 0; i<varBooleans.size(); i++) 
		    {
			buff.append(varBooleans.get(i));
			if (i < varBooleans.size() - 1)
			    {
				buff.append(", ");
			    }
		    }
		buff.append(": boolean;");
		pw.println(buff.toString());
	    }


       /* Declaration of integer variables
	*
	*/
	if (varIntegers.size() > 0 )
	    {
		buff = new StringBuffer("");
		for (int i = 0; i<varIntegers.size(); i++) 
		    {
			buff.append(varIntegers.get(i));
			if (i < varIntegers.size() - 1)
			    {
				buff.append(", ");
			    }
		    }
		buff.append(": -32..32;");
		pw.println(buff.toString());
	    }




       /* 
	*  Grouping of variables
	*  There may be many occurences of a variable in a project, 
	*  spread over various programs.
	*  For each occurence of the variable there will be
	*  a distinct fbdElement. These fbdElements must be grouped
	*  to print the assignments of values to the variable 
	*
	*/
	HashMap variables = new HashMap();
	
	for (int i=0; i<fbdElements.size(); i++)
	{
	    FBDElement fbdElement = (FBDElement)fbdElements.get(i);
	    String variableName = fbdElement.getElementName();
	    
	    if (fbdElement.getElementType().equals("variable"))
		{
		    /* All occurences of a variable are grouped in a 
		     * LinkedList called "variable" 
		     * If this is the first occurence of the variable then
		     * the LinkedList is created and pit in the "variables" Hashmap. 
		     * Otherwise the corresponding
		     * LinkedList is retrieved and the current occurence of
		     * the variable is added to the LinkedList.
		     */
		    if (variables.containsKey(variableName))
			{
			    LinkedList variable = (LinkedList)variables.get(variableName);
			    variable.add(fbdElement);
			} 
		    else
			{
			    LinkedList variable  = new LinkedList();
			    variable.add(fbdElement);
			    variables.put(variableName, variable);
			}
		}
	}




	/*
	 * Printing of variable assigments
	 */ 
	
	for (Iterator fbdIt = variables.keySet().iterator(); fbdIt.hasNext();)
	    {
		String variableName = (String)fbdIt.next();
		LinkedList variable = (LinkedList)variables.get(variableName);

		pw.println("");
		pw.println("\tnext(" + variableName + ") := case");
		pw.println("\t{");
		pw.println("\t\tctrl.state = read_input : {0, 1};");

		for (int i=0; i<variable.size(); i++)
		    {
			FBDElement   variableOccurence = (FBDElement)variable.get(i);
			Program      program           = (Program)programs.get(variableOccurence.getProgramIndex());
			LinkedList   arcs              = program.getArcs();		
			HashMap      boxes             = program.getBoxes();
			String       S                 = null;
			buff = new StringBuffer("");

			translateBoxNames_H(boxes);


			/* 
			 *  Retrieve a list of inputs to the variable 
			 *  A variable can have at most one input, therefore
			 *  the list of inputs will be empty or have exactly 
			 *  one element in it.
			 */
			LinkedList inputs              = inputElementIndices(S.valueOf(variableOccurence.getElementIndex()), arcs);
			
			if (inputs.size()>0)
			    {
				String    sourceIndex        = S.valueOf(((Tuple4)inputs.get(0)).x);
				int       sourceOutputNumber = ((Tuple4)inputs.get(0)).y;
				boolean   invert             = ((Tuple4)inputs.get(0)).invert;
				FBDObject theFBDObject       = getElementByIndex(program, sourceIndex);

				/* The input to the variable is another variable */
				if (theFBDObject.getType().equals("variable") ) 
				    {
					buff.append(((VAR)theFBDObject.getElement()).getName() + ";");
				    }

				/* The input to the variable is a box */
				else if (theFBDObject.getType().equals("box") ) 
				    {
					/*
					 * The box may have multiple output signals.
					 * Determine which of the output signals is
					 * connected to the variable's input
					 * 
					 */
					BOX box = ((BOX)theFBDObject.getElement());

					/*
					 *  If the current box is a corner (corners are named {\div})
					 *  we need to move back until we are dealing with
					 *  a conventional box
					 *
					 */
					if ((box.getName().equals("{\\div}")))
					    {
						LinkedList cornerInputs  = new LinkedList();
						boolean    moreCorners   = true;
						FBDObject  theFBDObject2 = null;
						String     sourceIndex2  = null;
						BOX        box2          = null;

						while (moreCorners)
						    {
							moreCorners  = false;

							cornerInputs = inputElementIndices(box.getIndex(), arcs);
							sourceIndex2  = S.valueOf(((Tuple4)cornerInputs.get(0)).x);
							theFBDObject2 = getElementByIndex(program, sourceIndex2);

							if (theFBDObject2.getType().equals("box"))
							    {
								box = (BOX)boxes.get(sourceIndex2);
								moreCorners = box.getName().equals("{\\div}");
							    }
						    }

						theFBDObject = theFBDObject2;
						sourceIndex = sourceIndex2;
					    }

					if (invert)
					    {
						 buff.append("!");
					    }

					if (theFBDObject.getType().equals("variable") ) 
					    {
						buff.append(((VAR)theFBDObject.getElement()).getName() + ";");
					    } 
				
					else
					    {

						String formalArgName = ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getOutputArgumentName(sourceOutputNumber);
						buff.append(program.getName() + "_" + ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getName() + "_" + sourceIndex + "." + formalArgName + ";");
			
					    }

				    }

				pw.println("\t\tctrl.state = compute_" + program.getName() + "_" + variableName + "_" + variableOccurence.getElementIndex() + ": " + buff.toString());
			    }

			
			 }
		

		pw.println("\t\t1                       : " +  variableName + ";");
		pw.println("\t};");
		pw.println("");
	    }

	pw.println("\tctrl: controller;");
	pw.println("");





	// Utskrift av block
        String boxDeclaration = null;
        for(int i=0; i<programs.size(); i++)
            {
                Program    program    = (Program)programs.get(i);
                LinkedList boxesList  = program.getBoxesList();

                for (int j = 0; j<boxesList.size(); j++) {
                    if (!(((BOX)boxesList.get(j)).getName()).equals("{\\div}"))
                        {
                            boxDeclaration = box2Smv((BOX)boxesList.get(j), program);
                            pw.println("\t" + boxDeclaration);
                        }
                }
            }

	pw.println("");
	pw.println("}");

	pw.flush();
 


	// Utskrift av output-variabler
	/*
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

				System.out.println("currVAR.index='" + currVAR.getIndex() + "' currVAR.name=" + currVAR.getName());

				System.out.println("OUTPUT VARIABLE: " + currVAR.getName());

				outputVariableDeclaration = "\t" + currVAR.getName() + ":= ";

				String S = null;
 
				System.out.println("i=" + i);

				String  sourceIndex = S.valueOf(((Tuple4)inputElementIndices.get(i)).x);
				int     sourceOutputNumber = ((Tuple4)inputElementIndices.get(i)).y;
				boolean invert = ((Tuple4)inputElementIndices.get(i)).invert;

				FBDObject theFBDObject = getElementByIndex(program, sourceIndex);

				if (theFBDObject.getType().equals("variable") ) 
				    {
					System.out.println("*** 1 ***");
					if (invert)
					    {
						outputVariableDeclaration =  outputVariableDeclaration + "!" + ((VAR)theFBDObject.getElement()).getName() + ", ";
					    }
					else
					    {
						 outputVariableDeclaration =  outputVariableDeclaration + ((VAR)theFBDObject.getElement()).getName() + ", ";
					    }
				    } 
				else if (theFBDObject.getType().equals("box") ) 
				    {
					System.out.println("*** 2 ***");

	*/

					/*
					 * Ta reda på vilken av input-boxens utsignaler
					 * som ska kopplas till den aktuella boxens insignal
					 * 
					 */

	/*

					BOX box2 = ((BOX)theFBDObject.getElement());
					if ((box2.getName().equals("{\\div}")))
					    {
						System.out.println("*** 3 ***");

						LinkedList divInputIndices = new LinkedList();
						boolean moreDivs = true;
						FBDObject theFBDObject2 = null;
						String sourceIndex2 = null;
						while (moreDivs)
						    {
							moreDivs = false;
							divInputIndices = inputElementIndices(box2.getIndex(), arcs);
							    
							sourceIndex2 = S.valueOf(((Tuple4)divInputIndices.get(0)).x);

							theFBDObject2 = getElementByIndex(program, sourceIndex2);
							if (theFBDObject2.getType().equals("box"))
							    {
								box2 = (BOX)boxes.get(sourceIndex2);
								moreDivs = box2.getName().equals("{\\div}");
							    }
						    }

						theFBDObject = theFBDObject2;
						sourceIndex = sourceIndex2;
					    }
					    


					if (invert)
					    {
						 outputVariableDeclaration =  outputVariableDeclaration + "!";
					    }

					if (theFBDObject.getType().equals("variable") ) 
					    {
						boxDeclaration =  outputVariableDeclaration + ((VAR)theFBDObject.getElement()).getName() + ", ";
					    } 
				
					else
					    {

						String formalArgName = ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getOutputArgumentName(sourceOutputNumber);
						 outputVariableDeclaration =  outputVariableDeclaration + program.getName() + "_" + ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getName() + "_" + sourceIndex + "." + formalArgName + "; ";
			
					    }

					pw.println(outputVariableDeclaration);
				    }


				

			    }
		    }

	    }
	
	System.out.println(boxDeclaration);
	pw.println("}");
	*/
    }

    

    public String box2Smv(BOX box, Program program) 
    {
	String     blockName;
	String     boxDeclaration = null;
	FBDObject theFBDObject  = null;
		
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

		theFBDObject = getElementByIndex(program, sourceIndex);

		if (theFBDObject.getType().equals("variable") ) 
		    {
			if (invert)
			    {
				boxDeclaration = boxDeclaration + "!" + ((VAR)theFBDObject.getElement()).getName() + ", ";
			    }
			else
			    {
				boxDeclaration = boxDeclaration + ((VAR)theFBDObject.getElement()).getName() + ", ";
			    }
		    } 
		else if (theFBDObject.getType().equals("box") ) 
		    {
			/*
			 * Ta reda på vilken av input-boxens utsignaler
			 * som ska kopplas till den aktuella boxens insignal
			 * 
			 */

			BOX box2 = ((BOX)theFBDObject.getElement());
			if ((box2.getName().equals("{\\div}")))
			    {
				LinkedList divInputIndices = new LinkedList();
				boolean    moreDivs = true;
				FBDObject theFBDObject2 = null;
				String     sourceIndex2 = null;

				while (moreDivs)
				    {
					moreDivs = false;
					divInputIndices = inputElementIndices(box2.getIndex(), arcs);
							    
					sourceIndex2 = S.valueOf(((Tuple4)divInputIndices.get(0)).x);

					theFBDObject2 = getElementByIndex(program, sourceIndex2);
					if (theFBDObject2.getType().equals("box"))
					    {
						box2 = (BOX)boxes.get(sourceIndex2);
						moreDivs = box2.getName().equals("{\\div}");
					    }
				    }

				theFBDObject = theFBDObject2;
				sourceIndex = sourceIndex2;
			    }
					    


			if (invert)
			    {
				boxDeclaration = boxDeclaration + "!";
			    }


			if (theFBDObject.getType().equals("variable") ) 
			    {
				boxDeclaration = boxDeclaration + ((VAR)theFBDObject.getElement()).getName() + ", ";
			    } 
			else
			    {

				System.out.println("%%% BOX.getname()='" + ((BOX)theFBDObject.getElement()).getName() + "'");

				String formalArgName = ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getOutputArgumentName(sourceOutputNumber);
				boxDeclaration = boxDeclaration + program.getName() + "_" + ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getName() + "_" + sourceIndex + "." + formalArgName + ", ";
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

	  theFBDObject = getElementByIndex(program, targetIndex);

	  if (theFBDObject.getType().equals("variable") ) 
	  {
	  boxDeclaration = boxDeclaration + ((VAR)theFBDObject.getElement()).getName() + ", ";
	  } 

	  else if (theFBDObject.getType().equals("box") ) 
	  {

	  System.out.println("BLOCKNAME (output): " + ((BOX)theFBDObject.getElement()).getName()  + "_" + ((BOX)theFBDObject.getElement()).getIndex());
					    



	  String formalArgName = ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getInputArgumentName(targetInputNumber);

	  boxDeclaration = boxDeclaration + program.getName() + "_" + ((Block)smvBlocks.get(((BOX)theFBDObject.getElement()).getName())).getName() + "_" + targetIndex + "." + formalArgName + ", ";
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


    class FBDObject
    {
	Object element;
	String type;

	public FBDObject(Object e, String t)
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
    


    public FBDObject getElementByIndex(Program program, String index)
    {
	HashMap variables        = program.getVariablesByIndex();
	HashMap boxes            = program.getBoxes();
	FBDObject theFBDObject = null;

	if (variables.containsKey(index))
	    {
		theFBDObject = new FBDObject(variables.get(index), "variable");
		return theFBDObject;
	    }
	/*
	  if (theRelays.containsKey(index))
	  {
	  return theRelays.get(index);
	  }
	*/
	if (boxes.containsKey(index))
	    {
		theFBDObject = new FBDObject(boxes.get(index), "box");
		return theFBDObject;
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
