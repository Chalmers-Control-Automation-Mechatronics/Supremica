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
 * The SMVModule class is used to store an SMV module 
 * that represents a RAC (the main RAC to verify or internally used lower level RACs) 
 * including variables, assignments and specification.
 *
 *
 * Created: Mon May 13 14:25:39 2008
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.external.rac.verificationModel.smv;

import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import org.supremica.external.rac.SMVModelBuilder;

public class SMVModule
{
    private String name;
    private boolean isMainModule;
    private String variableDeclaration;
    private String initialValues;
    private String expressions;
    private String moduleHead;
    private Map<String, Variable> inputMap;
    private Map<String, Variable> internalVariableMap;
    private Map<String, Variable> fbOutputVariableMap;
    private Map<String, Variable> outputMap;
    private Map<String, Variable> inOutMap;
    public final static String INOUTVARIABLE_INPART_DENOTER = "_in";
    public final static String INOUTVARIABLE_OUTPART_DENOTER = "_out";

    public SMVModule(String name, boolean isMainModule)
    {
	this.name = name;
	this.isMainModule = isMainModule;
	inputMap = new LinkedHashMap<String, Variable>(8); //initial capacity 8 and default load factor (0.75)
	internalVariableMap = new LinkedHashMap<String, Variable>(8);
	fbOutputVariableMap = new LinkedHashMap<String, Variable>(8);
	outputMap = new LinkedHashMap<String, Variable>(8);
	inOutMap = new LinkedHashMap<String, Variable>(8);
	variableDeclaration = 	" # Variable declaration\n";    
	initialValues = 	"\n # Initial values\n";    
	expressions = ""; 
	moduleHead = "";
    }

    // This method must be included since we want to consider two SMVModules with the same name as equal
    // and no duplicates of such SMVModules should be included in any HashSets or similar.
    public int hashCode()
    {
	return name.hashCode();
    }

    // See comment regarding hashCode above. Although the JavaDoc (java 6) for HashSet claims
    // that the equals() method is used when adding elements to HashSets it seems that only hashCode() is
    // used so this method is actually not needed.
    public boolean equals(Object o)
    {
	if (o instanceof SMVModule && ((SMVModule) o).getName().equals(name))
	{
	    return true;
	}
	else
	{
	    return false;
	}
    }


    public String getName()
    {
	return name;
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
    public void addInOut(Variable inOut)
    {
	inOutMap.put(inOut.name, inOut);
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
	var = inOutMap.get(originalName);
	if (var != null)
	{
	    if (originalName.equals(var.getName()))
	    {
		// first time we read an inOut variable we refer to the input part
		return var.getName() + INOUTVARIABLE_INPART_DENOTER; 
	    }
	    else
	    {
		return var.getName();
	    }
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
	if (var == null)
	{
	    var = inOutMap.get(originalName);
	    if ((var != null) && originalName.equals(var.getName()))
	    {
		// first time we assign an inOut variable we should start referring to the output part
		return var.getName() + INOUTVARIABLE_OUTPART_DENOTER + "_1"; 
	    }
	}
	if (var != null)
	{
	    String currentName = var.getName();
	    // Filter out previous_value_denoter, if any
	    if (currentName.endsWith(SMVModelBuilder.PREVIOUS_VALUE_DENOTER))
	    {
		currentName = currentName.substring(0, currentName.lastIndexOf(SMVModelBuilder.PREVIOUS_VALUE_DENOTER));
	    }
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

    // Adding a previous internal or output means that in the PLC-program, the value from the last scan cycle of the 
    // variable or output is used in the code. Hence an extra variable must be used to represent this.
    // For an inoutvariable, instead we refer to the in part of the variable.
    // Return true if it succeeds (varable is found), false otherwise. 
    public boolean addPreviousInternalOrOutput(String originalName, String previousVariableName)
    {
	Variable var = internalVariableMap.get(originalName);
	if (var == null)
	{
	    var = outputMap.get(originalName);
	}
	if (var != null)
	{
	    var.setName(previousVariableName); // This should be reset again by calling updateInternalOrOutputName() 
	    variableDeclaration += " " + var.getVariableDeclaration() + "; # value from previous scan cycle \n";    
	    initialValues += " init(" + var.getName() + ") := " + var.getInitialValue() + ";\n";    
	    expressions = " " + "Next(" + previousVariableName + ") := " + originalName + ";\n" + expressions;
	    return true;
	}
	else
	{
	    var = inOutMap.get(originalName);
	    if (var !=null) // inOut variable
	    {
		return true;
	    }
	    else
	    {
		return false;
	    }
	}


    }
	

//     // Temporarely assign an expression to an internal variable or output.
//     // Return true if it succeeds (varable is found), false otherwise.
//     public boolean assignExpressionToInternalOrOutput(String originalName, String expression)
//     {
// 	Variable var = internalVariableMap.get(originalName);
// 	if (var != null)
// 	{
// 	    var.setName(expression);
// 	    return true;
// 	}
// 	var = outputMap.get(originalName);
// 	if (var != null)
// 	{
// 	    var.setName(expression);
// 	    return true;
// 	}
// 	return false;
//     }
    
    
    
    // Change name of an internal variable or output.
    // Also add variable declaration to SMV-code
    // Return true if it succeeds (varable is found), false otherwise.
    public boolean updateInternalOrOutputName(String originalName, String newName)
    {
	Variable var = internalVariableMap.get(originalName);
	if (var == null)
	{
	    var = outputMap.get(originalName);
	}
	if (var == null)
	{
	    var = inOutMap.get(originalName);
	}
	if (var != null)
	{
	    var.setName(newName);
	    variableDeclaration += " " + var.getVariableDeclaration() + "; # temp variable\n" ;
	    //initialValues += " init(" + newName + ") := " + var.getInitialValue() + ";\n";
	    // (need not be initialized, since only temporal variable)
	    return true;
	}
	else
	{
	    return false;
	}
    }

    // This method is assumed to be called after the variables have been added to this module
    // but after the names have changed.
    public void declareVariablesAndInitialValues()
    {
	// Value Declaration
	for (Variable input : inputMap.values())
	{
	    variableDeclaration += " input " + input.getVariableDeclaration() + ";\n";    
	}
	for (Variable inOut : inOutMap.values())
	{
	    String originalName = inOut.getName();
	    inOut.setName(originalName + INOUTVARIABLE_INPART_DENOTER);  
	    variableDeclaration += " input " + inOut.getVariableDeclaration() + "; # input part of inOut variable\n";    
	    inOut.setName(originalName + INOUTVARIABLE_OUTPART_DENOTER);  
	    variableDeclaration += " output " + inOut.getVariableDeclaration() + "; # output part of inOut variable\n";    
	    inOut.setName(originalName); // set back the name of the variable
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
	// Initial values are only relevant for old (last scan cycle) internal variables and outputs. 
	// All values of inputs should be tested, and outputs and internal variables are assigned directly.

	//for (Variable input : inputMap.values())
	//{
	//    initialValues += isMainModule ? "" : "# "; // called modules should not have initial values (those are given by the caller)
	//    initialValues += " init(" + input.getName() + ") := " + input.getInitialValue() + ";\n";    
	//}
	// Local variables and outputs should not have initial values (since they are assigned immediately).
	// However, we must add initial values for old stored variables and outputs.
	//	for (Variable variable : internalVariableMap.values())
	//{
	//    initialValues += " init(" + variable.getName() + ") := " + variable.getInitialValue() + ";\n";    
	//}
	//for (Variable output : outputMap.values())
	//{
	//    initialValues += " init(" + output.getName() + ") := " + output.getInitialValue() + ";\n";    
	//}
    }

    public String getModuleCode()
    {
	moduleHead += "module " + name + "(";
	for (String inputName : inputMap.keySet())
	{
	    moduleHead += inputName + ", ";    
	}
	for (String inOutName : inOutMap.keySet())
	{
	    moduleHead += inOutName + INOUTVARIABLE_INPART_DENOTER + ", ";    
	    moduleHead += inOutName + INOUTVARIABLE_OUTPART_DENOTER + ", ";    
	}
	for (String outputName : outputMap.keySet())
	{
	    moduleHead += outputName + ", ";    
	}
	moduleHead = moduleHead.substring(0, moduleHead.length()-2) + ")\n{\n";
	
	for (Variable fbOutputVariable : fbOutputVariableMap.values())
	{
	    variableDeclaration += " " + fbOutputVariable.getVariableDeclaration() + "; # FB output, temp variable\n";
	    // (fbOutputVariable need not be initialized, since only temporal variable)
	}

	expressions = "\n # Model code\n" + expressions + "\n # New outputs and internal variables\n";
	for (Entry<String, Variable> variableEntry : internalVariableMap.entrySet())
	{
	    expressions += " " + variableEntry.getKey() + " := " + variableEntry.getValue().getName() + ";\n";    
	}
	for (Entry<String, Variable> outputEntry : outputMap.entrySet())
	{
	    expressions += " " + outputEntry.getKey() + " := " + outputEntry.getValue().getName() + ";\n";    
	}
	// inOut variables
	for (Entry<String, Variable> inOutMapEntry : inOutMap.entrySet())
	{
	    String originalName = inOutMapEntry.getKey();
	    String outName = originalName + INOUTVARIABLE_OUTPART_DENOTER;
	    expressions += " " + outName + " := " + getCurrentVariableName(originalName) + ";\n";
	}
	return moduleHead + variableDeclaration + initialValues + expressions + "}\n";
    }

}
