package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
public class Var {

	private String name;

	private Class type;

	private String value;

	private String address;

	private Boolean isNegated = false;

	private Boolean isConstant = false;

	private Boolean isRetain = false;
	
	private String documentation="";

	// TODO add qualifier or operation? isRetain;

	public Var(String name, String expectedValue) {

		this.name = name;
		type = String.class;
		value = expectedValue;

	}

	public Var(String name, int expectedValue) {

		this.name = name;
		type = Integer.class;
		value = String.valueOf(expectedValue);

	}

	public Var(String name, Boolean expectedValue) {

		this.name = name;
		type = Boolean.class;
		value = String.valueOf(expectedValue);

	}
	
	public Var(String name, String expectedValue, Class dataType) {

		this.name = name;
		type = dataType;
		value = expectedValue;

	}


	public String getName() {
		return name;
	}

	public Class getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setNegated() {

		this.isNegated = true;
	}

	public Boolean isNegated() {

		return this.isNegated;
	}

	public void setConstant() {

		this.isConstant = true;
	}

	public Boolean isConstant() {

		return this.isConstant;
	}

	public void setRetain() {

		this.isRetain = true;
	}

	public Boolean isRetain() {

		return this.isRetain;
	}

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	
	// TODO setter is not needed? Or private?
	/*
	 * public void setName(String name) { this.name = name; }
	 * 
	 * 
	 * 
	 * public void setValue(String value) { this.value = value; }
	 */

}
