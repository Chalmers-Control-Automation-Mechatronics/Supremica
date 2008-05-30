package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;
import java.util.*;

/**
 * PCG mode automata-ordering heuristics.
 * this is the OLD ordering technique: PCG graph search
 */
public class AOH_PCG
	extends AutomataOrderingHeuristic
{
	private int[] order;

	public int[] ordering()
	{
		return order;
	}

	public void init(Automata a)
		throws BDDException
	{
		Vector<Automaton> automata = a.getAutomata();
		PCG pcg = new PCG(automata);
		int i = 0;

		// get weights
		for (Enumeration<Automaton> e = automata.elements(); e.hasMoreElements(); i++)
		{
			Automaton a1 = e.nextElement();

			for (int j = 0; j < i; j++)
			{
				Automaton a2 = automata.elementAt(j);
				int cc = a1.getCommunicationComplexity(a2);

				if (cc != 0)
				{
					pcg.connect(a1, a2, cc);
				}
			}
		}

		order = pcg.getShortestPath();
                
                
                
/*		int size = automata.size();
                //for dining philosopher
                int temp = size/2;
                int temp2 = 0;
                for (i = 0; i < size; i++)
                {
                    if(i%2 == 0)
                    {
                        order[i] = temp2;
                        temp2 = order[i]+1;
                    }
                    else
                    {
                        order[i] = temp;
                        temp = order[i]+1;
                    }
		}
  */              ////////////
	}
}
