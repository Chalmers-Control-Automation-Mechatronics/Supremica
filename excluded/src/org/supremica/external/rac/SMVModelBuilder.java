/*
 * This RAC package is external to the Supremica tool and developed by 
 * Oscar Ljungkrantz. Contact:
 *
 * Oscar Ljungkrantz 
 * oscar.ljungkrantz@chalmers.se
 * +46 (0)708-706278
 * SWEDEN
 *
 * for technical discussions about RACs (Reusable Automation Components).
 * For questions about Supremica license or other technical discussions,
 * contact Supremica according to License Agreement below.
 */

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

/**
 * The SMVModelBuilder class is used to build a verification 
 * model in SMV from the chosen RAC.
 *
 *
 * Created: Thu Apr 10 17:10:39 2008
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.external.rac;

import org.supremica.external.genericPLCProgramDescription.xsd.*;
import org.supremica.external.genericPLCProgramDescription.xsd.Project.Types.Pous.*;
import org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.*;
import org.supremica.external.genericPLCProgramDescription.xsd.Project.Types.Pous.Pou.Interface.*;
import org.supremica.external.rac.verificationModel.smv.*;
import org.supremica.external.rac.verificationModel.*;
import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class SMVModelBuilder extends VerificationModelBuilder
{
    private SMVModel smvModel;
    private Functions functions;
    public final static StandardFBs<String, String> standardFBs = new StandardFBs<String, String>();
    public final static String SCAN_CYCLE_DENOTER = "scancycletime_mustbereplaced_withsystemscancycletime";
    public final static String PREVIOUS_VALUE_DENOTER = "_previous";

    public SMVModelBuilder()
    {
	super();
	smvModel = null;
	functions = new Functions();
    }

    public VerificationModel getVerificationModel()
    {
	return smvModel;
    }

    public void createNewVerificationModel(Project plcProject, String racName)
    {
	smvModel = new SMVModel();
	createSMVModule(plcProject, racName, true);

	smvModel.createFile(racName + ".smv", "..\\examples\\rac\\generatedSMVCode");
    }
    
    private void createSMVModule(Project plcProject, String racName, boolean isMainModule)
    {
	SMVModule currentModule = null;
	if (isMainModule)
	{
	    currentModule = new SMVModule("main", isMainModule);
	}
	else
	{
	    currentModule = new SMVModule(racName, isMainModule);
	}

	if (!smvModel.addNewModule(currentModule))
	{
	    // Module already added to SMVModel
	    return;
	}
	
	
	FunctionBlocks currentInternalFBs = new FunctionBlocks();

	Pou pou = getRAC(plcProject, racName);

	// the RAC is declared as a POU in this project
	if (pou != null)
	{
	    // Inputs, outputs and internal variables
	    for (VarList varList : pou.getInterface().getLocalVarsOrTempVarsOrInputVars())
	    {
		if (varList instanceof InputVars)
		{
		    for (org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable 
			     var : varList.getVariable())
		    {
			currentModule.addInput(createVariable(var, racName, false));
		    }
		}
		if (varList instanceof OutputVars)
		{
		    for (org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable 
			     var : varList.getVariable())
		    {
			currentModule.addOutput(createVariable(var, racName, true));
		    }
		    
		}
		if (varList instanceof LocalVars)
		{
		    for (org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable 
			     var : varList.getVariable())
		    {
			Variable variable = createVariable(var, racName, true);
			if (variable != null)
			{
			    currentModule.addInternalVariable(variable);
			}
			// if variable is null it is probably a Function Block, which we handle later.
		    }
		}
		if (varList instanceof TempVars)
		{
		    System.out.println("SMVModelBuilder: Warning! Temporary variables yet not implemented!");
		}
		if (varList instanceof InOutVars)
		{
		    for (org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable 
			     var : varList.getVariable())
		    {
			currentModule.addInOut(createVariable(var, racName, false));
		    }
		}
		if (varList instanceof ExternalVars)
		{
		    System.out.println("SMVModelBuilder: Warning! External variables yet not implemented!");
		}
	    }
	    currentModule.declareVariablesAndInitialValues();
	    
	    // Ladder code
	    if (pou.getBody().getLD() != null)
	    {
		List<Object> ldComponents = pou.getBody().getLD().getCommentOrErrorOrConnector();
		// must iterate with index counter, since list order is modified at runtime:
		for (int ldCompIndex = 0; ldCompIndex < ldComponents.size(); ldCompIndex++)
		{
		    Object ldComp = ldComponents.get(ldCompIndex);
		    // coil
		    if (ldComp instanceof Coil)
		    {
			// address the latest representation of this output or internal variable
			String originalVariable = ((Coil) ldComp).getVariable();
			String currentVariable = currentModule.getCurrentVariableName(originalVariable);
			String nextVariable = currentModule.getNextInternalOrOutputName(originalVariable);
			
			if (currentVariable == null || nextVariable == null)
			{
			    System.out.println("SMVModelBuilder: Error! Coil connected to unknown output or " 
					       + "internal variable " + ((Coil) ldComp).getVariable() + "!");
			    return;
			}

			// If an variable or output is set or reset before being assigned, in the LD,
			// we should use the old variable or output, i.e. var := var_previous | ... , for set-coil etc.
			if (currentVariable.equals(originalVariable) && !((Coil) ldComp).getStorage().value().equals("none"))
			{
			    if (currentModule.addPreviousInternalOrOutput(originalVariable, currentVariable + PREVIOUS_VALUE_DENOTER))
			    {
				currentVariable += PREVIOUS_VALUE_DENOTER;
			    }
			}
			
			
			String s = nextVariable + " := ";
			String ending ="";
			if (!((Coil) ldComp).isNegated() && ((Coil) ldComp).getStorage().value().equals("none"))
			{
			    // normal coil
			}
			else if (((Coil) ldComp).isNegated() && ((Coil) ldComp).getStorage().value().equals("none"))
			{
			    // negated coil
			    s += "~(";
			    ending +=")";
			}
			else if (!((Coil) ldComp).isNegated() && ((Coil) ldComp).getStorage().value().equals("set"))
			{
			    // set coil
			    s += currentVariable + " | ";
			}
			else if (!((Coil) ldComp).isNegated() && ((Coil) ldComp).getStorage().value().equals("reset"))
			{
			    // reset coil
			    s += currentVariable + " & ~(";
			    ending +=")";
			}
			else
			{
			    // unknown coil
			    System.out.println("SMVModelBuilder: Error! Unknown coil type used with variable " 
					       + ((Coil) ldComp).getVariable() + "!");
			}
			
			// Previous connected LD symbols
			List<String> orExpressions = new LinkedList<String>();
			for (Connection connection : ((Coil) ldComp).getConnectionPointIn().getConnection())
			{
			    Object prevComp = getLDComponent(ldComponents, connection.getRefLocalId());
			    String orExpression = "";
			    if (prevComp == null)
			    {
				System.out.println("SMVModelBuilder: Warning! " + "component with refNbr " 
						   + connection.getRefLocalId() + " could not be found!");
			    }
			    else
			    {
				orExpression += expandExpression(ldComponents, prevComp, connection.getFormalParameter()
								 , ldComp, currentModule, currentInternalFBs);
			    }
			    
			    // end of sub-expression
			    if (orExpression.endsWith(" & "))
			    {
				orExpression = orExpression.substring(0, orExpression.length()-3);
			    }
			    orExpressions.add(orExpression);
			}
			
			// identify & remove part of the orStrings that are similar for all parts, if more than one part
			ending = orExpressions.size()>1 ? " )" + removeEqualEndings(orExpressions) + ending : ending;
			// start of OR-expression ?
			s += orExpressions.size()>1 ? "( " : ""; 
			s += orExpressions.get(0);
			for (int i = 1 ; i < orExpressions.size() ; i++)
			{
			    s += " | " + orExpressions.get(i); 
			}
			
			s += ending; 
			
			// end of complete expression
			currentModule.addExpression(s);
			currentModule.updateInternalOrOutputName(originalVariable, nextVariable);
		    }
		}
	    }
	    else
	    {
		System.out.println("SMVModelBuilder: Warning! "  
				   + "So far only LD is supported as RAC implementation language!");
	    }
	    
	    
	    for (String usedFBName : currentInternalFBs.usedFBTypes)
	    {
		// Standard FB
		if (standardFBs.containsKey(usedFBName))
		{
		    System.out.println("SMVModelBuilder: We have a standard function block of type " + usedFBName);
		    
		    File theFile = new File(standardFBs.get(usedFBName));
		    if(theFile!=null)
		    {
			smvModel.importModuleFromFile(theFile, usedFBName);
		    }
		    else
		    {
			System.out.println("SMVModelBuilder: Error! Couldn't find the file " + standardFBs.get(usedFBName) + " for FB " + usedFBName);
		    }
		}
		// User defined FB
		else
		{
		    createSMVModule(plcProject, usedFBName, false);
		}
	    }
	}
	// Otherwize it must be a standard FB
	else
	{
	    System.out.println("SMVModelBuilder: Warning! RAC " + racName 
			       + " is unknown. Perhaps it is a standard FB, but it is yet not implemented as such.");
	    
	}

    }

    // Return & remove part of the orStrings that are similar for all parts
    private String removeEqualEndings(List<String> orExpressions)
    {
	if (orExpressions.size()<=1)
	{
	    return "";
	}
	String compareString = orExpressions.get(0);
	// find size of shortest String
	int minSize = java.lang.Math.min(compareString.length(),orExpressions.get(1).length()); 
	for (int k = 2; k < orExpressions.size() ; k++)
	{
	    minSize = java.lang.Math.min(minSize,orExpressions.get(k).length());
	}

	int i = compareString.length() - minSize;
	boolean equalityFound = false;
	while (!equalityFound && i < compareString.length())
	{
	    equalityFound = true;
	    int k = 1;
	    while (equalityFound && k < orExpressions.size())
	    {
		// Do not remove equal endings due to similar variableName, i.e. MoveFwd and AlarmFwd.
		// Thus, an equalEnding must start with a "&".
		if (!orExpressions.get(k).endsWith(compareString.substring(i)) || !compareString.substring(i).trim().startsWith("&"))
		{
		    equalityFound = false;
		}
		k++;
	    }
	    i++;
	}
	if (!equalityFound)
	{
	    return "";
	}
	for (int k = 0 ; k < orExpressions.size() ; k++)
	{
	    String s = orExpressions.remove(k);
	    orExpressions.add(k, s.substring(0, i-1+s.length()-compareString.length()));
	}
	return compareString.substring(i-1, compareString.length());
    }

    // Expand the LD expression with the LD component (called at the output outputName
    // , if applicable (for FBs)) and continue leftwards
    private String expandExpression(List<Object> ldComponents, Object ldComp, String outputName, 
				    Object currentCoil, SMVModule currentModule, FunctionBlocks currentInternalFBs)
    {
	String s = "";
	List<Connection> connectionList = null; 

	// coil
	if (ldComp instanceof Coil)
	{
	    // If coil is detected inside LD rung, the list of ldComponents has to be rearranged
	    // since unfortunately PLCOpen Editor places those inner coils in wrong order (last)

	    // Not, they do not!!!! (only in specific examples); they place them in the order they were added
	    // when adding them in the editor.
	    ldComponents.remove(ldComp);
	    ldComponents.add(ldComponents.indexOf(currentCoil)+1, ldComp);

	    s += "";
	    connectionList = ((Coil) ldComp).getConnectionPointIn().getConnection();
	}
	
	// contact
	else if (ldComp instanceof Contact)
	{
	    if ( ((Contact) ldComp).isNegated() )
	    {
		s += "~";
	    }
	    // If an variable or output is refered to before being assigned, in the LD,
	    // we should use the old variable or output, i.e. coiloutput := ... var_previous ...
	    String originalName = ((Contact) ldComp).getVariable();
	    String currentName = currentModule.getCurrentVariableName(originalName);
	    if (currentName.equals(originalName))
	    {
		if (currentModule.addPreviousInternalOrOutput(originalName, currentName + PREVIOUS_VALUE_DENOTER))
		{
		    currentName += PREVIOUS_VALUE_DENOTER;
		}
	    }
	    
	    s += currentName + " & ";
	    connectionList = ((Contact) ldComp).getConnectionPointIn().getConnection();
	} 
	
	// functions or FBs
	else if (ldComp instanceof Block)
	{
	    String typeName = ((Block) ldComp).getTypeName();
	    String instanceName = ((Block) ldComp).getInstanceName();
	    
	    // function
	    if (instanceName == null)
	    {
		if (functions.isBooleanFunction(typeName))
		{
		    s += functions.expandBooleanFunction(ldComponents, (Block) ldComp, typeName, currentCoil, currentModule, currentInternalFBs);
		}
		else if (functions.isComparisonFunction(typeName))
		{
		    s += functions.expandComparisonFunction(ldComponents, (Block) ldComp, typeName, currentCoil, currentModule, currentInternalFBs);
		}
		else if (functions.isArithmeticEnableFunction(typeName))
		{
		    // Those arithmetic enable functions have one boolean output to tell whether or not the function was performed
		    // and one (probably nonboolean) output to store the arithmetic result
		    String nonBooleanOutputName = getOutputVariableName(ldComponents, ((Block) ldComp).getLocalId());
		    s += functions.expandArithmeticEnableFunction(ldComponents, (Block) ldComp, 
								  typeName, nonBooleanOutputName, currentCoil, currentModule, currentInternalFBs);
		}
		else if (functions.isArithmeticFunction(typeName))
		{
		    // Those arithmetic functions have only one (probably nonboolean) output to store the arithmetic result
		    s += functions.expandArithmeticFunction(ldComponents, (Block) ldComp, 
								  typeName, currentCoil, currentModule, currentInternalFBs);
		}
		else if (functions.isTypeConvertFunction(typeName))
		{
		    s += functions.expandTypeConvertFunction(ldComponents, (Block) ldComp, 
								  typeName, currentCoil, currentModule, currentInternalFBs);
		}
		else
		{
		    System.out.println("SMVModelBuilder: Warning! Function " + typeName + " could not be found!");
		}		
	    }
	    // FB
	    else
	    {
		s += currentInternalFBs.expandFB(ldComponents, (Block) ldComp, typeName, instanceName, 
					  outputName, currentCoil, currentModule);
	    }
	}

	// inVariable
	else if (ldComp instanceof InVariable)
	{
	    // The invariable points to a local variable or an input
	    if (currentModule.getCurrentVariableName(((InVariable) ldComp).getExpression()) != null)
	    {
		// If an variable or output is refered to before being assigned, in the LD,
		// we should use the old variable or output, i.e. coiloutput := ... var_previous ...
		String originalName = ((InVariable) ldComp).getExpression();
		String currentName = currentModule.getCurrentVariableName(originalName);
		if (currentName.equals(originalName))
		{
		    if (currentModule.addPreviousInternalOrOutput(originalName, currentName + PREVIOUS_VALUE_DENOTER))
		    {
			currentName += PREVIOUS_VALUE_DENOTER;
		    }
		}


		s += currentName;
	    }
	    // The invariable probably contains a value of string or int etc
	    else
	    {
		s += ((InVariable) ldComp).getExpression();
	    }
	}
	
	// Continue leftwards in the LD expression
	if (connectionList != null && connectionList.size() > 0)
	{
	    // Previous connected LD symbols
	    List<String> orExpressions = new LinkedList<String>();
	    for (Connection connection : connectionList)
	    {
		Object prevComp = getLDComponent(ldComponents, connection.getRefLocalId());
		String orExpression = "";
		if (prevComp == null)
		{
		    System.out.println("SMVModelBuilder: Warning! " + "component with refNbr " 
				       + connection.getRefLocalId() + " could not be found!");
		}
		else
		{
		    orExpression += expandExpression(ldComponents, prevComp, connection.getFormalParameter()
						     , currentCoil, currentModule, currentInternalFBs);
		}
		
		// end of sub-expression
		if (orExpression.endsWith(" & "))
		{
		    orExpression = orExpression.substring(0, orExpression.length()-3);
		}
		orExpressions.add(orExpression);
	    }
	    
	    // identify & remove part of the orStrings that are similar for all parts
	    String ending = " )" + removeEqualEndings(orExpressions);

	    // start of OR-expression ?
	    s += orExpressions.size()>1 ? "( " : ""; 
	    s += orExpressions.get(0);
	    for (int i = 1 ; i < orExpressions.size() ; i++)
	    {
		s += " | " + orExpressions.get(i); 
	    }
	    // end of OR-expression ?
	    s += orExpressions.size()>1 ? ending : ""; 
	}
	return s;
    }
    
    // Get the LD component representing a certain localId
    private Object getLDComponent(List<Object> ldComponents, BigInteger id)
    {
	for (Object ldComp : ldComponents)
	{
	    if ( ldComp instanceof Coil && ((Coil) ldComp).getLocalId().equals(id) 
		 || ldComp instanceof Block && ((Block) ldComp).getLocalId().equals(id) 
		 || ldComp instanceof InVariable && ((InVariable) ldComp).getLocalId().equals(id) 
		 || ldComp instanceof Label && ((Label) ldComp).getLocalId().equals(id) 
		 || ldComp instanceof Contact && ((Contact) ldComp).getLocalId().equals(id) 
		 || ldComp instanceof LeftPowerRail && ((LeftPowerRail) ldComp).getLocalId().equals(id) )
	    {
		return ldComp;
	    }
	}
	return null;
    }

    // Get the name of the first output variable that is connected to the block with id identification
    private String getOutputVariableName(List<Object> ldComponents, BigInteger id)
    {
	for (Object ldComp : ldComponents)
	{
	    if (ldComp instanceof OutVariable && 
		((OutVariable) ldComp).getConnectionPointIn().getConnection().get(0).getRefLocalId().equals(id))
	    {
		return ((OutVariable) ldComp).getExpression();
	    }
	}
	System.out.println("SMVModelBuilder: Error! Output variable refering to FB/function with  refNbr " 
			   + id + " could not be found!");
	return "";
    } 
    
    // Create a variable representation of the PLCOpen variable 
    private Variable createVariable(org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable var, String fbName, boolean needsInitialValue)
    {
	Variable variable = null;

	// Create type specific parts of variables
	DataType type = var.getType();
	//   BOOL
	if (type.getBOOL() != null)
	{
	    variable = new BooleanVariable(var.getName());
	}
	//   TIME
	// So far, I assume that the time is written as integers, this will be amended in the future.
	// According to IEC 61131-3 time should be written as for instance t#23ms
	if (type.getTIME() != null)
	{
	    variable = new TimeVariable(var.getName());
	    try
	    {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(fbName + ": Type minimal possible value in ms (as integer) for time variable " + var.getName() + ": ");
		System.out.flush();
		((TimeVariable) variable).setMinValue(in.readLine());
		System.out.print(fbName + ": Type maximal possible value in ms (as integer) for time variable " + var.getName() + ": ");
		System.out.flush();
		((TimeVariable) variable).setMaxValue(in.readLine());
	    }
	    catch(IOException exception)
	    {
		System.err.println("SMVModelBuilder: IOException! Could not read the input from the keybord!");
	    }
	}
	//   STRING
	if (type.getString() != null)
	{
	    variable = new StringVariable(var.getName());
	    try
	    {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(fbName + ": Type allowed values for String variable " + var.getName() 
				 + ", separated by comma (,): ");
		System.out.flush();
		String possibleValues = in.readLine();
		while ( !(var.getInitialValue().getSimpleValue() != null && 
		       possibleValues.indexOf(var.getInitialValue().getSimpleValue().getValue()) >=0) )
		{
		    System.out.print("\nThe allowed values must include the initial value: " 
				     + var.getInitialValue().getSimpleValue().getValue() + ". Try again: ");
		    possibleValues = in.readLine();
		}
		((StringVariable) variable).setPossibleValues(possibleValues);
	    }
	    catch(IOException exception)
	    {
		System.err.println("SMVModelBuilder: IOException! Could not read the input from the keybord!");
	    }
	}
	//   INT
	else if (type.getSINT()!=null || type.getINT()!=null || type.getDINT()!=null || type.getLINT()!=null 
		 || type.getUSINT()!=null || type.getUINT()!=null || type.getUDINT()!=null || type.getULINT()!=null )
	{
	    variable = new IntVariable(var.getName());
	    try
	    {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(fbName + ": Type minimal possible value for int variable " + var.getName() + ": ");
		System.out.flush();
		((IntVariable) variable).setMinValue(in.readLine());
		System.out.print(fbName + ": Type maximal possible value for int variable " + var.getName() + ": ");
		System.out.flush();
		((IntVariable) variable).setMaxValue(in.readLine());
	    }
	    catch(IOException exception)
	    {
		System.err.println("SMVModelBuilder: IOException! Could not read the input from the keybord!");
	    }
	}
	
	// Create general parts of variables
	if (variable != null)
	{
	    if (var.getInitialValue() != null)
	    {
		if (var.getInitialValue().getSimpleValue() != null)
		{
		    variable.setInitialValue(var.getInitialValue().getSimpleValue().getValue());
		}
		else
		{
		    System.out.println("SMVModelBuilder: Error! Can only handle simple values right now!");
		}
	    }
	    else if (needsInitialValue)
	    {
		    System.out.println("SMVModelBuilder: Warning! All variables except inputs and inouts must have initial value!");
	    }
	}
	else if (type.getDerived() != null)
	{
	    // Probably it is a reference to a function block and we should do nothing with this variable
	}
	else
	{
	    System.out.println("SMVModelBuilder: Warning! Unknown variable type of variable " + var.getName() + "!");
	}
	return variable;
    }


    private class FunctionBlocks {
	
	private Set<String> usedFBTypes;
	private Set<String> usedFBInstances;

	private FunctionBlocks()
	{
	    usedFBTypes = new LinkedHashSet<String>(4);
	    usedFBInstances = new HashSet<String>(5);
	}

	private String expandFB(List<Object> ldComponents, Block fb, 
				String typeName, String instanceName, String outputName, Object currentCoil, SMVModule currentModule)
	{
	    if (!usedFBTypes.contains(typeName));
	    {
		usedFBTypes.add(typeName);
	    }
	    if (!usedFBInstances.contains(instanceName))
	    {
		usedFBInstances.add(instanceName);
		
		// FB instance in SMV looks like:
		// instance : TYPE( input1, input2, temp1, temp2 );
		String fbInstance = instanceName + " : " + typeName + "( ";
		for (org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable 
			 inputVariable : fb.getInputVariables().getVariable())
		{
		    // In PLCOpenEditor (although not GX IEC Developer) FB inputs must be single branches,
		    // not parallel (OR-branches), hence only one connection.
		    Connection connection = inputVariable.getConnectionPointIn().getConnection().get(0);
		    Object prevComp = getLDComponent(ldComponents, connection.getRefLocalId());
		    fbInstance += expandExpression(ldComponents, prevComp, connection.getFormalParameter(), currentCoil, currentModule, this);
		    // end of sub-expression
		    if (fbInstance.endsWith(" & "))
		    {
			fbInstance = fbInstance.substring(0, fbInstance.length()-3);
		    }
		    fbInstance += ", ";
		}
		for (org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.OutputVariables.Variable 
			 outputVariable : fb.getOutputVariables().getVariable())
		{
		    fbInstance += currentModule.addFBOutputVariable() + ", ";
		}
		// remove last ", "
		fbInstance = fbInstance.substring(0, fbInstance.length()-2) + " )";
		
		currentModule.addExpression(fbInstance);


		// And the call of the FB might look like:
		// Variable_i := instance.O_j;   , for output O_j,
		// both for this boolean output and all other connected output variables
		for (Object ldComp : ldComponents)
		{
		    // Find output variable that is connected to output of this FB
		    // Eaxh output variable can only be connected to one FB output
		    if ( ldComp instanceof OutVariable && 
			((OutVariable) ldComp).getConnectionPointIn().getConnection().get(0).getRefLocalId().equals(fb.getLocalId()) )
		    {
			String outputVariableName = ((OutVariable) ldComp).getExpression();
			String nextOutputName = currentModule.getNextInternalOrOutputName(outputVariableName);
			currentModule.updateInternalOrOutputName(outputVariableName, nextOutputName);
			// The name of the specific output of the FB to which the output variable is connected
			String fbOutputName = ((OutVariable) ldComp).getConnectionPointIn().getConnection().get(0).getFormalParameter();
			currentModule.addExpression(nextOutputName + " := " + instanceName + "." + fbOutputName);
		    }
		}
		


	    }
	    return instanceName + "." + outputName;
	}
    }


    private class Functions {
	
	private Map<String, String> booleanFunctionToSMV;
	private Map<String, String> comparisonFunctionToSMV;
	private Map<String, String> arithmeticFunctionToSMV;
	private Map<String, String> arithmeticEnableFunctionToSMV;
	private Map<String, String> typeConvertFunctionToSMV;
	
	private Functions()
	{
	    // Boolean functions
	    booleanFunctionToSMV = new HashMap<String, String>(4);
	    booleanFunctionToSMV.put("AND", " & ");
	    booleanFunctionToSMV.put("OR", " | ");
	    booleanFunctionToSMV.put("XOR", " ^ ");
	    booleanFunctionToSMV.put("NOT", "~");

	    // Comparison functions
	    comparisonFunctionToSMV = new HashMap<String, String>(6);
	    comparisonFunctionToSMV.put("GT", " > ");
	    comparisonFunctionToSMV.put("GE", " >= ");
	    comparisonFunctionToSMV.put("EQ", " = ");
	    comparisonFunctionToSMV.put("LT", " < ");
	    comparisonFunctionToSMV.put("LE", " <= ");
	    comparisonFunctionToSMV.put("NE", " ~= ");
	    
	    // Arithmetic functions
	    arithmeticFunctionToSMV = new HashMap<String, String>(5);
	    arithmeticFunctionToSMV.put("ADD", " + ");
	    arithmeticFunctionToSMV.put("MUL", " * ");
	    arithmeticFunctionToSMV.put("SUB", " - ");
	    arithmeticFunctionToSMV.put("DIV", " / ");
	    arithmeticFunctionToSMV.put("MOD", " mod ");

	    // Arithmetic enable (extra boolean input and output) functions
	    arithmeticEnableFunctionToSMV = new HashMap<String, String>(8);
	    arithmeticEnableFunctionToSMV.put("ADD_E", " + ");
	    arithmeticEnableFunctionToSMV.put("MUL_E", " * ");
	    arithmeticEnableFunctionToSMV.put("SUB_E", " - ");
	    arithmeticEnableFunctionToSMV.put("DIV_E", " / ");
	    arithmeticEnableFunctionToSMV.put("MOD_E", " mod ");
	    arithmeticEnableFunctionToSMV.put("INC_E", " + 1");
	    arithmeticEnableFunctionToSMV.put("INCP_M", " + 1");
	    arithmeticEnableFunctionToSMV.put("DEC_E", " - 1");
	    arithmeticEnableFunctionToSMV.put("MOVE_E", ""); // just assign the value
	    arithmeticEnableFunctionToSMV.put("MOV_M", ""); // just assign the value
	    arithmeticEnableFunctionToSMV.put("MOV_E_Int", ""); // just assign the value

	    // Type conversion
	    typeConvertFunctionToSMV = new HashMap<String, String>(2);
	    typeConvertFunctionToSMV.put("BOOL_TO_INT", "");
	    typeConvertFunctionToSMV.put("INT_TO_TIME", "");  // assume time is in ms; must be ammended to handle true time variables
	    typeConvertFunctionToSMV.put("TIME_TO_INT", "");  // int time will be in ms; must be ammended to handle true time variables
	}

	private boolean isBooleanFunction(String functionName)
	{
	    return booleanFunctionToSMV.containsKey(functionName);
	}

	private boolean isComparisonFunction(String functionName)
	{
	    return comparisonFunctionToSMV.containsKey(functionName);
	}

	private boolean isArithmeticFunction(String functionName)
	{
	    return arithmeticFunctionToSMV.containsKey(functionName);
	}

	private boolean isArithmeticEnableFunction(String functionName)
	{
	    return arithmeticEnableFunctionToSMV.containsKey(functionName);
	}

	private boolean isTypeConvertFunction(String functionName)
	{
	    return typeConvertFunctionToSMV.containsKey(functionName);
	}

	private String expandTypeConvertFunction(List<Object> ldComponents, Block function, 
					     String functionName, Object currentCoil, SMVModule currentModule, FunctionBlocks currentInternalFBs)
	{
	    // In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
	    // not parallel (OR-branches), hence only one connection.
	    Connection connectionIn = function.getInputVariables().getVariable().get(0).getConnectionPointIn().getConnection().get(0);
	    Object prevComp = getLDComponent(ldComponents, connectionIn.getRefLocalId());
	    return expandExpression(ldComponents, prevComp, connectionIn.getFormalParameter(), currentCoil, currentModule, currentInternalFBs);
	}	    

	private String expandBooleanFunction(List<Object> ldComponents, Block function, 
					     String functionName, Object currentCoil, SMVModule currentModule, FunctionBlocks currentInternalFBs)
	{
	    List<org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable> 
		variableList = function.getInputVariables().getVariable();
	    String s = "";
	    // NOT has only one input and the operator effects this input
	    if (functionName.equals("NOT"))
	    {
		// In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
		// not parallel (OR-branches), hence only one connection.
		Connection connection = variableList.get(0).getConnectionPointIn().getConnection().get(0);
		Object prevComp = getLDComponent(ldComponents, connection.getRefLocalId());
		s += booleanFunctionToSMV.get(functionName) + "(" + expandExpression(ldComponents, prevComp
										     , connection.getFormalParameter() 
										     , currentCoil, currentModule, currentInternalFBs);
		// end of sub-expression
		if (s.endsWith(" & "))
		{
		    s = s.substring(0, s.length()-3);
		}
		s += ")";
	    }
	    else if (booleanFunctionToSMV.containsKey(functionName))
	    {
		String smvOperator = booleanFunctionToSMV.get(functionName);
		s += "(";
		for (org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable var : variableList)
		{
		    // In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
		    // not parallel (OR-branches), hence only one connection.
		    Connection connection = var.getConnectionPointIn().getConnection().get(0);
		    Object prevComp = getLDComponent(ldComponents, connection.getRefLocalId());
		    s += expandExpression(ldComponents, prevComp, connection.getFormalParameter(), currentCoil, currentModule, currentInternalFBs);
		    // end of sub-expression
		    if (s.endsWith(" & "))
		    {
			s = s.substring(0, s.length()-3);
		    }
		    s += smvOperator;
		}
		s = s.substring(0, s.length() - smvOperator.length()) + ")";
	    }
	    else
	    {
		System.out.println("SMVModelBuilder.Functions: Warning! boolean function " 
				   + functionName + " could not be found!");
	    }
	    return s;
	}

	private String expandComparisonFunction(List<Object> ldComponents, Block function, 
					     String functionName, Object currentCoil, SMVModule currentModule, FunctionBlocks currentInternalFBs)
	{
	    List<org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable> 
		variableList = function.getInputVariables().getVariable();
	    String s = "";
	    if (comparisonFunctionToSMV.containsKey(functionName))
	    {
		String smvOperator = comparisonFunctionToSMV.get(functionName);
		s += "(";
		
		// In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
		// not parallel (OR-branches), hence only one connection.
		Connection connection1 = variableList.get(0).getConnectionPointIn().getConnection().get(0);
		Connection connection2 = variableList.get(1).getConnectionPointIn().getConnection().get(0);
		Object input1 = getLDComponent(ldComponents, connection1.getRefLocalId());
		Object input2 = getLDComponent(ldComponents, connection2.getRefLocalId());

		s += expandExpression(ldComponents, input1, connection1.getFormalParameter(), currentCoil, currentModule, currentInternalFBs);
		// end of sub-expression
		if (s.endsWith(" & "))
		{
		    s = s.substring(0, s.length()-3);
		}
		s += smvOperator + expandExpression(ldComponents, input2, connection2.getFormalParameter(), currentCoil, currentModule, currentInternalFBs);
		if (s.endsWith(" & "))
		{
		    s = s.substring(0, s.length()-3);
		}
		s += ")";
	    }
	    else
	    {
		System.out.println("SMVModelBuilder.Functions: Warning! comparison function " 
				   + functionName + " could not be found!");
	    }
	    return s;
	}

	// Result of this function should be something like this:
	// if (enableCondition)
	//   out_k+1 := i2 arithmeticExpression i3
	// else
	//   out_k+1 := out_k
	//return enableCondition
	private String expandArithmeticEnableFunction(List<Object> ldComponents, Block function, String functionName
						      , String nonBooleanOutputName, Object currentCoil
						      , SMVModule currentModule, FunctionBlocks currentInternalFBs)
	{
	    List<org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable> 
		variableList = function.getInputVariables().getVariable();
	    String enableCondition = "";
	    if (arithmeticEnableFunctionToSMV.containsKey(functionName))
	    {
		String smvOperator = arithmeticEnableFunctionToSMV.get(functionName);
		// In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
		// not parallel (OR-branches), hence only one connection.
		Connection connection1 = variableList.get(0).getConnectionPointIn().getConnection().get(0);
		Object enableInput = getLDComponent(ldComponents, connection1.getRefLocalId());
		enableCondition = expandExpression(ldComponents, enableInput, connection1.getFormalParameter()
						   , currentCoil, currentModule, currentInternalFBs);
		if (enableCondition.endsWith(" & "))
		{
		    enableCondition = enableCondition.substring(0, enableCondition.length()-3);
		}
		String ifExpr = "IF (" + enableCondition + ")\n";
		String currentOutputName = currentModule.getCurrentVariableName(nonBooleanOutputName);
		String nextOutputName = currentModule.getNextInternalOrOutputName(nonBooleanOutputName);
		ifExpr += "   " + nextOutputName + " := ";
	
		if (currentOutputName.equals(nonBooleanOutputName))
		{
		    if (currentModule.addPreviousInternalOrOutput(nonBooleanOutputName, currentOutputName + PREVIOUS_VALUE_DENOTER))
		    {
			currentOutputName += PREVIOUS_VALUE_DENOTER;
		    }
		}

		// Functions with more inputs than the enable input: MOVE_E, ADD_E, MUL_E etc
		if (variableList.size() > 1)
		{
		    Connection connection2 = variableList.get(1).getConnectionPointIn().getConnection().get(0);
		    Object input2 = getLDComponent(ldComponents, connection2.getRefLocalId());
		    ifExpr += expandExpression(ldComponents, input2, connection2.getFormalParameter(), currentCoil, currentModule, currentInternalFBs);
		    // end of sub-expression
		    if (ifExpr.endsWith(" & "))
		    {
			ifExpr = ifExpr.substring(0, ifExpr.length()-3);
		    }
		}
		// For instance DEC_E, INC_E have enable input only
		else
		{
		    ifExpr += currentOutputName;
		}
		ifExpr += smvOperator;
		// Functions with more than two inputs: ADD_E, MUL_E etc
		if (variableList.size() > 2)
		{
		    Connection connection3 = variableList.get(2).getConnectionPointIn().getConnection().get(0);
		    Object input3 = getLDComponent(ldComponents, connection3.getRefLocalId());
		    ifExpr += expandExpression(ldComponents, input3, connection3.getFormalParameter(), currentCoil, currentModule, currentInternalFBs);
		    if (ifExpr.endsWith(" & "))
		    {
			ifExpr = ifExpr.substring(0, ifExpr.length()-3);
		    }
		}
		currentModule.addExpression(ifExpr + ";\n ELSE\n   " 
				       + nextOutputName + " := " + currentOutputName);
		
		currentModule.updateInternalOrOutputName(nonBooleanOutputName, nextOutputName);
	    }
	    else
	    {
		System.out.println("SMVModelBuilder.Functions: Warning! arithmetic function " 
				   + functionName + " could not be found!");
	    }
	    return enableCondition;
	}




	
	// Even though functions inside LD should have boolean inputs and outputs, these nonboolean functions
	// may actually be used if they are later "converted" to boolean expressions using for instance 
	// a comparison function.
	// Result of this function should be like this:
	// i1 arithmeticExpression i2 (aritmheticExpression i3 ...)
	private String expandArithmeticFunction(List<Object> ldComponents, Block function, String functionName
						      , Object currentCoil, SMVModule currentModule, FunctionBlocks currentInternalFBs)
	{
	    List<org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable> 
		variableList = function.getInputVariables().getVariable();
	    String s = "";
	    if (arithmeticFunctionToSMV.containsKey(functionName))
	    {
		String smvOperator = arithmeticFunctionToSMV.get(functionName);
		for (org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable var : variableList)
		{ 
		    // In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
		    // not parallel (OR-branches), hence only one connection.
		    Connection currentConnection = var.getConnectionPointIn().getConnection().get(0);
		    Object currentInput = getLDComponent(ldComponents, currentConnection.getRefLocalId());
		    s += expandExpression(ldComponents, currentInput, currentConnection.getFormalParameter(), currentCoil, currentModule, currentInternalFBs);
		    // end of sub-expression
		    if (s.endsWith(" & "))
		    {
			s = s.substring(0, s.length()-3);
		    }
		    s += smvOperator;
		}
		if (s.endsWith(smvOperator))
		{
		    s = s.substring(0, s.length()-smvOperator.length());
		}
	    }
	    else
	    {
		System.out.println("SMVModelBuilder.Functions: Warning! arithmetic function " 
				   + functionName + " could not be found!");
	    }
	    return s;
	}
	
    }
    
}