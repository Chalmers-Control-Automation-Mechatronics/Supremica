package org.supremica.softplc.CompILer.CodeGen.Datatypes;

/**This class is used to represent Direct variables
 * @see "Chapter 2.3 Datatypes in Programming industrial control
 *       systems using IEC 1131-3 by R. W. Lewis. ISBN: 0 85296 827 2"
 * @author Anders Röding
 */
public class IECSymbolicVariable
	implements IECVariable
{
	private TypeConstant type;
	private String name;
	/* For function block variables eg. LD A.B */
	private String typeName;
	private TypeConstant fieldType;
	private String fieldSelector;
	private String fieldSelectorTypeName;
    

	/**
	 * constructs a new IECSymbolic variable
	 */
	public IECSymbolicVariable(String s, TypeConstant t)
	{
		name = s;
		type = t;
	}

	/**Constructor for record type variables
	 * @param name the variable name (eg. A in LD A.B)
	 * @param varType the variable type
	 * @param fieldSelector the selector name (eg. B in LD A.B)
	 * @param fieldType the selector type
	 */
	public IECSymbolicVariable(String name, TypeConstant varType, String typeName, String fieldSelector, TypeConstant fieldType, 
				   String fieldSelectorTypeName)
	{
		this.name = name;
		this.type = varType;
		this.typeName = typeName;
		this.fieldType = fieldType;
		this.fieldSelector = fieldSelector;
		this.fieldSelectorTypeName = fieldSelectorTypeName;		
	}

	/**
	 * @return variable type
	 */
	public TypeConstant getType()
	{
		return type;
	}

	/**
	 * @return variable name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * especially useful when dealing with derived data types.
	 * @return type name
	 */
	public String getTypeName()
	{
		return typeName;
	}

	/**
	 * @return field selector type
	 */
	public TypeConstant getFieldSelectorType()
	{
		return fieldType;
	}

	/**
	 * @return field selector name
	 */
	public String getFieldSelector()
	{
		return fieldSelector;
	}

	/**
	 * @return field selector type name
	 */
	public String getFieldSelectorTypeName()
	{
		return fieldSelectorTypeName;
	}
}
