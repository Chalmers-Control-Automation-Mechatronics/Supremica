package org.supremica.external.fbd2smv.fbdProject;

import java.util.*;

public class Dictionary
{
    private LinkedList booleans = new LinkedList();
    private LinkedList integers = new LinkedList();

    public void setBooleans(LinkedList booleans)
    {
	this.booleans = booleans;
    }

    public void setIntegers(LinkedList integers)
    {
	this.integers = integers;
    }


    public void addBoolean(String name)
    {
	booleans.add(name);
	
    }

    public void addInteger(String name)
    {
	integers.add(name);
    }

    public LinkedList getBooleans()
    {
	Collections.sort(booleans);
	return booleans;
    }

    public LinkedList getIntegers()
    {
	Collections.sort(integers);
	return integers;
    }
}

