/***************** IntArrayVector.java ************************/
// Typesafe vector of int[] for holding the result of
// SearchStates (among other things, perhaps)
// Reimplements the interface for all those methods that take or return objects (give me templates!!)

package org.supremica.util;

import java.util.*;

public class IntArrayVector
	extends Vector
{
	public IntArrayVector() 
	{
		super();
	}
	
	public IntArrayVector(int initialCapacity)
	{
		super(initialCapacity);
	}
	public IntArrayVector(int initialCapacity, int capacityIncrement)
	{
		super(initialCapacity, capacityIncrement);
	}

	public void add(int index, int[] element)
	{
		super.add(index, element);
	}
	public boolean add(int[] elem)
	{
		return super.add(elem);
	}
	public void addElement(int[] elem)
	{
		super.addElement(elem);
	}
	public boolean contains(int[] elem) 
	{
		return super.contains(elem);
	}
	public void copyInto(int[][] anArray)
	{
		super.copyInto(anArray);
	}
	public int indexOf(int[] elem)
	{
		return super.indexOf(elem);
	}
	public int indexOf(int[] elem, int index)
	{
		return super.indexOf(elem, index);
	}
	public void insertElementAt(int[] elem, int index)
	{
		super.insertElementAt(elem, index);
	}
	public int lastIndexOf(int[] elem) 
	{
		return super.lastIndexOf(elem);
	}
	// cannot overload on return type (ok) but also no hiding takes place(?)
	public int[] getElementAt(int index)
	{
		return (int[])super.elementAt(index);
	}
	public int[] getFirstElement() 
	{
		return (int[])super.firstElement();
	}
	public int[] getElement(int index)
	{
		return (int[])super.get(index);
	}
	public int[] at(int index) // my own special
	{
		return (int[])super.get(index);
	}
	public int[] getLastElement()
	{
		return (int[])super.lastElement();
	}
}