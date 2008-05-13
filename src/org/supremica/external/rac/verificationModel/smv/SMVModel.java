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
 * The SMVModel class is used to store an SMV model of a RAC
 * including variables, assignments and specification.
 *
 *
 * Created: Mon Apr 14 14:48:39 2008
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.external.rac.verificationModel.smv;

import org.supremica.external.rac.verificationModel.VerificationModel;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;

public class SMVModel extends VerificationModel
{
    private String variableDeclaration;
    private String initialValues;
    private String expressions;
    private String moduleHead;
    private Map<String, Variable> inputMap;
    private Map<String, Variable> internalVariableMap;
    private Map<String, Variable> fbOutputVariableMap;
    private Map<String, Variable> outputMap;

    public SMVModel()
    {
	super();
	inputMap = new LinkedHashMap<String, Variable>(4); //initial capacity 4 and default load factor (0.75)
	internalVariableMap = new LinkedHashMap<String, Variable>(8);
	fbOutputVariableMap = new LinkedHashMap<String, Variable>(8);
	outputMap = new LinkedHashMap<String, Variable>(4);
	variableDeclaration = 	" # Variable declaration\n";    
	initialValues = 	"\n # Initial values\n";    
	expressions = "\n # Model code\n"; 
	moduleHead = "";
    }
    
    public void clear()
    {
	inputMap.clear();
	internalVariableMap.clear();
	fbOutputVariableMap.clear();
	outputMap.clear();
	variableDeclaration = 	" # Variable declaration\n";    
	initialValues = 	"\n # Initial values\n";    
	expressions = "\n # Model code\n"; 
	moduleHead = "";
    }

    public void addInput(Variable input)
    {
	inputMap.put(input.name, input);
    }
    public void addInternalVariable(Variable variable)
    {
	internalVariableMap.put(variable.name, variable);
    }

    public String addFBOutputVariable()
    {
	String tempVariableName = "temp" + (fbOutputVariableMap.size()+1);
	Variable var = new BooleanVariable(tempVariableName);
	// (actual output may not be boolean, but it does not matter, since this variable is never used)
	fbOutputVariableMap.put(tempVariableName, var);
	return tempVariableName;
    }

    public void addOutput(Variable output)
    {
	outputMap.put(output.name, output);
    }
    public void addExpression(String expression)
    {
	expressions += " " + expression + ";\n";
    }
    
    // Get the name of the latest representation (temporary variables are used inside the SMV model) 
    // of this variable.
    // Return null if variable not found.
    public String getCurrentVariableName(String originalName)
    {
	Variable var = internalVariableMap.get(originalName);
	if (var != null)
	{
	    return var.getName();
	}
	var = outputMap.get(originalName);
	if (var != null)
	{
	    return var.getName();
	}
	var = inputMap.get(originalName);
	if (var != null)
	{
	    return var.getName();
	}
	return null;
    }

    // Return the next updated name (temporary variables are used inside the SMV model)
    // representing a specific internal variable or output.
    // The temporary names of a variable Var will be Var_1, Var_2 etc. 
    // Return null if internal variable or output not found.
    public String getNextInternalOrOutputName(String originalName)
    {
	Variable var = internalVariableMap.get(originalName);
	if (var == null)
	{
	    var = outputMap.get(originalName);
	}
	if (var != null)
	{
	    String currentName = var.getName();
	    int number = 0;
	    int index = currentName.lastIndexOf("_");
	    if (index >= 0 && index < currentName.length()-1)
	    {
		try
		{
		    number = Integer.parseInt(currentName.substring(index+1));
		}
		catch(NumberFormatException e){}
	    }
	    if (number > 0)
	    {
		number++;
		return currentName.substring(0, index+1) + number;
	    }
	    else
	    {
		return currentName + "_1";
	    }
	}
	return null;
    }


    // Change name of an internal variable or output.
    // Also add variable declaration to SMV-code
    // Return true if it succeeds (varable is found), false otherwise.
    public boolean updateInternalOrOutputName(String originalName, String newName)
    {
	Variable var = internalVariableMap.get(originalName);
	if (var != null)
	{
	    var.setName(newName);
	    variableDeclaration += " " + var.getVariableDeclaration() + " # temp variable;\n";
	    //initialValues += " init(" + newName + ") := " + var.getInitialValue() + ";\n";
	    // (need not be initialized, since only temporal variable)
	    return true;
	}
	var = outputMap.get(originalName);
	if (var != null)
	{
	    var.setName(newName);
	    variableDeclaration += " " + var.getVariableDeclaration() + " # temp variable;\n" ;
	    //initialValues += " init(" + newName + ") := " + var.getInitialValue() + ";\n";
	    // (need not be initialized, since only temporal variable)
	    return true;
	}
	return false;
    }


    public void declareVariablesAndInitialValues()
    {
	// Value Declaration
	for (Variable input : inputMap.values())
	{
	    variableDeclaration += " input " + input.getVariableDeclaration() + ";\n";    
	}
	for (Variable variable : internalVariableMap.values())
	{
	    variableDeclaration += " " + variable.getVariableDeclaration() + ";\n";    
	}
	for (Variable output : outputMap.values())
	{
	    variableDeclaration += " output " + output.getVariableDeclaration() + ";\n";    
	}

	// Initial values
	for (Variable input : inputMap.values())
	{
	    initialValues += " init(" + input.getName() + ") := " + input.getInitialValue() + ";\n";    
	}
	for (Variable variable : internalVariableMap.values())
	{
	    initialValues += " init(" + variable.getName() + ") := " + variable.getInitialValue() + ";\n";    
	}
	for (Variable output : outputMap.values())
	{
	    initialValues += " init(" + output.getName() + ") := " + output.getInitialValue() + ";\n";    
	}
    }

    public void createModule(String moduleName)
    {
	moduleHead += "module " + moduleName + "(";
	for (String inputName : inputMap.keySet())
	{
	    moduleHead += inputName + ", ";    
	}
	for (String outputName : outputMap.keySet())
	{
	    moduleHead += outputName + ", ";    
	}
	moduleHead = moduleHead.substring(0, moduleHead.length()-2) + ")\n{\n";
	
	for (Variable fbOutputVariable : fbOutputVariableMap.values())
	{
	    variableDeclaration += " " + fbOutputVariable.getVariableDeclaration() + " # FB output, temp variable;\n";
	    // (fbOutputVariable need not be initialized, since only temporal variable)
	}

	expressions += "\n # New outputs and internal variables\n";
	for (Entry<String, Variable> variableEntry : internalVariableMap.entrySet())
	{
	    expressions += " Next(" +  variableEntry.getKey() + ") := " + variableEntry.getValue().getName() + ";\n";    
	}
	for (Entry<String, Variable> outputEntry : outputMap.entrySet())
	{
	    expressions += " Next(" +  outputEntry.getKey() + ") := " + outputEntry.getValue().getName() + ";\n";    
	}
	System.out.println(moduleHead + variableDeclaration + initialValues + expressions + "}\n");
    }

    public void createFile(String fileName, String path)
    {
    }
    
}
