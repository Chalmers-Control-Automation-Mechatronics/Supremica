package org.jgrafchart;

public interface Writable
	extends Referencable
{
	boolean isBoolean();

	boolean isInteger();

	boolean isString();

	void setStoredBoolAction(boolean b);

	void setStoredIntAction(int i);

	void setStoredStringAction(String s);
}
