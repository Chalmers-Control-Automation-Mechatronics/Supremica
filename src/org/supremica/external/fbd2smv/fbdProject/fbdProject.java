package org.supremica.external.fbd2smv.fbdProject;

import java.util.*;
import org.supremica.external.fbd2smv.isagrafReader.*;

public class fbdProject
{
	private Dictionary dictionary = new Dictionary();
	private LinkedList programs = new LinkedList();
	private LinkedList programList = new LinkedList();
	private LinkedList fbdElements = new LinkedList();

	//private HashMap    variables   = new HashMap();
	//private HashMap    boxes       = new HashMap();
	//private LinkedList    arcs     = new HashMap();    
	public fbdProject() {}

	public void dictionarySetBooleans(LinkedList booleans)
	{
		dictionary.setBooleans(booleans);
	}

	public void dictionarySetIntegers(LinkedList integers)
	{
		dictionary.setIntegers(integers);
	}

	public LinkedList dictionaryGetBooleans()
	{
		return dictionary.getBooleans();
	}

	public LinkedList dictionaryGetIntegers()
	{
		return dictionary.getIntegers();
	}

	public void addProgram(Program program)
	{
		programs.add(program);
	}

	public void setProgramList(LinkedList programList)
	{
		this.programList = programList;
	}

	public LinkedList getProgramList()
	{
		return programList;
	}

	public LinkedList getPrograms()
	{
		return programs;
	}

	public LinkedList getFBDElements()
	{
		return fbdElements;
	}

	/*
	public boolean isOutputVariable(String name)
	{
		Program program;
		LinkedList arcs;
		HashMap variables;

		for (int i=0; i<programs.size(); i++)
			{
				program = (Program)programs.get(i);
				arcs = program.getArcs();
				variables = program.getVariablesByName();
				VAR variable = (VAR)variables.get(name);

				if (variable != null) {
					String index = variable.getIndex();

					for (int j=0; j<arcs.size(); j++ )
						{String S = null;
						ARC currARC = (ARC)arcs.get(j);
						if (index.equals(S.valueOf(currARC.getTargetIndex())))
							{
							return true;
							}
						}
				}

			}

		return false;
	}
	*/
	public boolean isOutputVariable(String index)
	{
		Program program;
		LinkedList arcs;
		HashMap variables;

		for (int i = 0; i < programs.size(); i++)
		{
			program = (Program) programs.get(i);
			arcs = program.getArcs();

			String S = null;

			for (int j = 0; j < arcs.size(); j++)
			{
				ARC currARC = (ARC) arcs.get(j);

				if (index.equals(String.valueOf(currARC.getTargetIndex())))
				{
					return true;
				}
			}
		}

		return false;
	}

	public LinkedList getOutputVariableIndicesByVariableName(String name)
	{
		LinkedList indices = new LinkedList();
		Program program;

		for (int i = 0; i < programs.size(); i++)
		{
			program = (Program) programs.get(i);

			HashMap variables = program.getVariablesByIndex();

			for (Iterator varIt = variables.keySet().iterator();
					varIt.hasNext(); )
			{
				String currIndex = (String) varIt.next();

				if (isOutputVariable(currIndex))
				{
					VAR currVar = (VAR) variables.get(currIndex);

					if (name.equals(currVar.getName()))
					{
						String[] str = new String[2];

						str[0] = program.getName();
						str[1] = currVar.getIndex();

						indices.add(str);
					}
				}
			}
		}

		return indices;
	}

	/*
	public LinkedList getOutputVariables()
	{
		LinkedList outputVariables = new LinkedList();
		Program program;

		for (int i=0; i<programs.size(); i++)
		{
			program = (Program)programs.get(i);
			HashMap variables = program.getVariablesByIndex();

			for (Iterator varIt = variables.keySet().iterator(); varIt.hasNext(); )
			{
				String currIndex = (String)varIt.next();

				if (isOutputVariable(currIndex))
				{
					VAR currVar = (VAR)variables.get(currIndex);
					outputVariables.add(currVar);
				}

			}
		}
	}
	*/

	/*
	public boolean isOutputVariable(int ix)
	{
		Program program;
		LinkedList arcs;
		HashMap variables;
		String S = null;
		String index;

		index = S.valueOf(ix);

		for (int i=0; i<programs.size(); i++)
			{
				program = (Program)programs.get(i);
				arcs = program.getArcs();

				for (int j=0; j<arcs.size(); j++ )
				{
					ARC currARC = (ARC)arcs.get(j);
					if (index.equals(S.valueOf(currARC.getTargetIndex())))
						{
							return true;
						}
					}
			}

		return false;

	}
	*/
}
