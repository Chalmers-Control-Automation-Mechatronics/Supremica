package org.supremica.external.fbd2smv.fbd2smv;

import java.util.*;

public class Block
{
    private LinkedList inputArguments;
    private LinkedList outputArguments;
    private String name;

    public Block(String name, LinkedList inputArguments, LinkedList outputArguments)
    {
		this.name = name;
		this.inputArguments  = inputArguments;
		this.outputArguments = outputArguments;
    }

    public String getInputArgumentName(int index)
    {
		return ((Argument)inputArguments.get(index)).getName();
    }

    public String getOutputArgumentName(int index)
    {
		return ((Argument)outputArguments.get(index)).getName();
    }

    public String getName()
    {
		return name;
    }
}
