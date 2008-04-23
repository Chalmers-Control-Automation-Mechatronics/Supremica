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

public class SMVModelBuilder extends VerificationModelBuilder
{
    private SMVModel smvModel;
    private Functions functions;


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

    public void createNewVerificationModel(Project plcProject, String rac)
    {
	smvModel = new SMVModel();
	Pou pou = getRAC(plcProject, rac);

	// Inputs, outputs and internal variables
	for (VarList varList : pou.getInterface().getLocalVarsOrTempVarsOrInputVars())
	{
	    if (varList instanceof InputVars)
	    {
		for (org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable 
			 var : varList.getVariable())
		{
		    smvModel.addInput(createVariable(var));
		}
	    }
	    if (varList instanceof OutputVars)
	    {
		for (org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable 
			 var : varList.getVariable())
		{
		    smvModel.addOutput(createVariable(var));
		}

	    }
	    if (varList instanceof LocalVars)
	    {
		for (org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable 
			 var : varList.getVariable())
		{
		    smvModel.addInternalVariable(createVariable(var));
		}
	    }
	    if (varList instanceof TempVars)
	    {
		System.out.println("SMVModelBuilder: Warning! Temporary variables yet not implemented!");
	    }
	    if (varList instanceof InOutVars)
	    {
		System.out.println("SMVModelBuilder: Warning! InOut variables yet not implemented!");
	    }
	    if (varList instanceof ExternalVars)
	    {
		System.out.println("SMVModelBuilder: Warning! External variables yet not implemented!");
	    }
	}
	smvModel.declareVariablesAndInitialValues();

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
		    String currentVariable = smvModel.getCurrentVariableName(originalVariable);
		    String nextVariable = smvModel.getNextInternalOrOutputName(originalVariable);
		    if (currentVariable == null || nextVariable == null)
		    {
			System.out.println("SMVModelBuilder: Error! Coil connected to unknown output or " 
					   + "internal variable " + ((Coil) ldComp).getVariable() + "!");
			return;
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
			    orExpression += expandExpression(ldComponents, prevComp, ldComp);
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
		    smvModel.addExpression(s);

		    smvModel.updateInternalOrOutputName(originalVariable, nextVariable);
		}
	    }
	}
	else
	{
		System.out.println("SMVModelBuilder: Warning! " 
				   + "So far only LD is supported as RAC implementation language!");
	}
	
	smvModel.createFile("fil", "mjölk");

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
		if (!orExpressions.get(k).endsWith(compareString.substring(i)))
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

    // Expand the LD expression with the LD component and continue leftwards
    private String expandExpression(List<Object> ldComponents, Object ldComp, Object currentCoil)
    {
	String s = "";
	List<Connection> connectionList = null; 

	// coil
	if (ldComp instanceof Coil)
	{
	    // If coil is detected inside LD rung, the list of ldComponents has to be rearranged
	    // since unfortunately PLCOpen Editor places those inner coils in wrong order (last)
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
	    s += smvModel.getCurrentVariableName(((Contact) ldComp).getVariable()) + " & ";
	    connectionList = ((Contact) ldComp).getConnectionPointIn().getConnection();
	} 

	// functions
	else if (ldComp instanceof Block)
	{
	    String functionName = ((Block) ldComp).getTypeName();
	    if (functions.isBooleanFunction(functionName))
	    {
		s += functions.expandBooleanFunction(ldComponents, (Block) ldComp, functionName, currentCoil);
	    }
	    else if (functions.isComparisonFunction(functionName))
	    {
		s += functions.expandComparisonFunction(ldComponents, (Block) ldComp, functionName, currentCoil);
	    }
	    else if (functions.isArithmeticEnableFunction(functionName))
	    {
		String nonBooleanOutputName = getOutputVariableName(ldComponents, ((Block) ldComp).getLocalId());
		s += functions.expandArithmeticEnableFunction(ldComponents, (Block) ldComp, 
							      functionName, nonBooleanOutputName, currentCoil);
	    }
	    else
	    {
		System.out.println("SMVModelBuilder: Warning! Function " + functionName + " could not be found!");
	    }		
	}

	// inVariable
	else if (ldComp instanceof InVariable)
	{
	    s += ((InVariable) ldComp).getExpression();
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
		    orExpression += expandExpression(ldComponents, prevComp, currentCoil);
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
    private Variable createVariable(org.supremica.external.genericPLCProgramDescription.xsd.VarListPlain.Variable var)
    {
	Variable variable = null;

	// Create type specific parts of variables
	DataType type = var.getType();
	//   BOOL
	if (type.getBOOL() != null)
	{
	    variable = new BooleanVariable(var.getName());
	}
	//   STRING
	if (type.getString() != null)
	{
	    variable = new StringVariable(var.getName());
	    try
	    {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Type allowed values for String variable " + var.getName() 
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
		System.out.print("Type minimal possible value for int variable " + var.getName() + ": ");
		System.out.flush();
		((IntVariable) variable).setMinValue(in.readLine());
		System.out.print("Type maximal possible value for int variable " + var.getName() + ": ");
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
	    else 
	    {
		    System.out.println("SMVModelBuilder: Warning! Each variable must have initial value!");
	    }
	}
	else
	{
	    System.out.println("SMVModelBuilder: Warning! Unknown variable type of variable " + var.getName() + "!");
	}
	return variable;
    }



    private class Functions {
	
	private Map<String, String> booleanFunctionToSMV;
	private Map<String, String> comparisonFunctionToSMV;
	private Map<String, String> arithmeticEnableFunctionToSMV;
	
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
	    
	    // Arithmetic enable (extra boolean input and output) functions
	    arithmeticEnableFunctionToSMV = new HashMap<String, String>(8);
	    arithmeticEnableFunctionToSMV.put("ADD_E", " + ");
	    arithmeticEnableFunctionToSMV.put("MUL_E", " * ");
	    arithmeticEnableFunctionToSMV.put("SUB_E", " - ");
	    arithmeticEnableFunctionToSMV.put("DIV_E", " / ");
	    arithmeticEnableFunctionToSMV.put("MOD_E", " mod ");
	    arithmeticEnableFunctionToSMV.put("INC_E", " + 1");
	    arithmeticEnableFunctionToSMV.put("DEC_E", " - 1");
	    arithmeticEnableFunctionToSMV.put("MOVE_E", ""); // just assign the value
	}

	private boolean isBooleanFunction(String functionName)
	{
	    return booleanFunctionToSMV.containsKey(functionName);
	}

	private boolean isComparisonFunction(String functionName)
	{
	    return comparisonFunctionToSMV.containsKey(functionName);
	}

	private boolean isArithmeticEnableFunction(String functionName)
	{
	    return arithmeticEnableFunctionToSMV.containsKey(functionName);
	}

	private String expandBooleanFunction(List<Object> ldComponents, Block function, 
					     String functionName, Object currentCoil)
	{
	    List<org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable> 
		variableList = function.getInputVariables().getVariable();
	    String s = "";
	    // NOT has only one input and the operator effects this input
	    if (functionName.equals("NOT"))
	    {
		// In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
		// not parallel (OR-branches), hence only one connection.
		Object prevComp = getLDComponent(ldComponents, variableList.get(0).getConnectionPointIn()
						 .getConnection().get(0).getRefLocalId());
		s += booleanFunctionToSMV.get(functionName) + "(" + expandExpression(ldComponents, prevComp, currentCoil);
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
		    Object prevComp = getLDComponent(ldComponents, 
						     var.getConnectionPointIn().getConnection().get(0).getRefLocalId());
		    s += expandExpression(ldComponents, prevComp, currentCoil);
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
					     String functionName, Object currentCoil)
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
		Object input1 = getLDComponent(ldComponents, variableList.get(0).getConnectionPointIn()
					       .getConnection().get(0).getRefLocalId());
		Object input2 = getLDComponent(ldComponents, variableList.get(1).getConnectionPointIn()
					       .getConnection().get(0).getRefLocalId());

		s += expandExpression(ldComponents, input1, currentCoil);
		// end of sub-expression
		if (s.endsWith(" & "))
		{
		    s = s.substring(0, s.length()-3);
		}
		s += smvOperator + expandExpression(ldComponents, input2, currentCoil);
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
	private String expandArithmeticEnableFunction(List<Object> ldComponents, Block function, 
						      String functionName, String nonBooleanOutputName, Object currentCoil)
	{
	    List<org.supremica.external.genericPLCProgramDescription.xsd.Body.SFC.Block.InputVariables.Variable> 
		variableList = function.getInputVariables().getVariable();
	    String enableCondition = "";
	    if (arithmeticEnableFunctionToSMV.containsKey(functionName))
	    {
		String smvOperator = arithmeticEnableFunctionToSMV.get(functionName);
		// In PLCOpenEditor (perhaps also in IEC 61131-3?) function inputs must be single branches,
		// not parallel (OR-branches), hence only one connection.
		Object enableInput = getLDComponent(ldComponents, variableList.get(0).getConnectionPointIn()
					       .getConnection().get(0).getRefLocalId());
		enableCondition = expandExpression(ldComponents, enableInput, currentCoil);
		if (enableCondition.endsWith(" & "))
		{
		    enableCondition = enableCondition.substring(0, enableCondition.length()-3);
		}
		String ifExpr = "IF (" + enableCondition + ")\n";
		String currentOutputName = smvModel.getCurrentVariableName(nonBooleanOutputName);
		String nextOutputName = smvModel.getNextInternalOrOutputName(nonBooleanOutputName);
		ifExpr += "   " + nextOutputName + " := ";
	
		// Functions with more inputs than the enable input: MOVE_E, ADD_E, MUL_E etc
		if (variableList.size() > 1)
		{
		    Object input2 = getLDComponent(ldComponents, variableList.get(1).getConnectionPointIn()
						   .getConnection().get(0).getRefLocalId());
		    ifExpr += expandExpression(ldComponents, input2, currentCoil);
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
		    Object input3 = getLDComponent(ldComponents, variableList.get(2).getConnectionPointIn()
						   .getConnection().get(0).getRefLocalId());
		    ifExpr += expandExpression(ldComponents, input3, currentCoil);
		    if (ifExpr.endsWith(" & "))
		    {
			ifExpr = ifExpr.substring(0, ifExpr.length()-3);
		    }
		}
		smvModel.addExpression(ifExpr + ";\n ELSE\n   " 
				       + nextOutputName + " := " + currentOutputName);
		
		smvModel.updateInternalOrOutputName(nonBooleanOutputName, nextOutputName);
	    }
	    else
	    {
		System.out.println("SMVModelBuilder.Functions: Warning! arithmetic function " 
				   + functionName + " could not be found!");
	    }
	    return enableCondition;
	}

	


    }




}
