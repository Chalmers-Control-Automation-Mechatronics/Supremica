/**This class is used to represent Direct variables
 * IEC 61131-3
 * Chapter 2.3 Datatypes
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;
public class IECSymbolicVariable implements IECVariable{

	private TypeConstant type;
	private String name;
	/*For function block variables eg. LD A.B*/
    private String typeName;
	private TypeConstant fieldType;
	private String fieldSelector;

	public IECSymbolicVariable(String s, TypeConstant t){
		name = s;
		type = t;
	}

	/**Constructor for record type variables
	 * @param name the variable name (eg. A in LD A.B)
	 * @param varType the variable type
	 * @param fieldSelector the selector name (eg. B in LD A.B)
	 * @param fieldType the selector type
	 */
	public IECSymbolicVariable(String name, TypeConstant varType, String typeName,
                                   String fieldSelector, TypeConstant fieldType) {
		this.name = name;
		this.type = varType;
                this.typeName = typeName;
		this.fieldType = fieldType;
		this.fieldSelector = fieldSelector;
	}

    public TypeConstant getType(){return type;}
    public String getName(){return name;}
    public String getTypeName(){return typeName;}
    public TypeConstant getFieldSelectorType() {return fieldType;}
    public String getFieldSelector() {return fieldSelector;}
}
