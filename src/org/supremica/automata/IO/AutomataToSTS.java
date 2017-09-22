
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.IO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesizerOptions;


public class AutomataToSTS
    implements AutomataSerializer
{
    private final Automata automata;
    @SuppressWarnings("unused")
	private final boolean includeCost = true;

    public AutomataToSTS(final Automata automata)
    {
        this.automata = automata;

    }

    public AutomataToSTS(final Automaton automaton)
    {
        this.automata = new Automata();
        this.automata.addAutomaton(automaton);
    }

	@Override
  public void serialize(final PrintWriter pw)
    {
        pw.println("root = plant");
        final String[][] root = new String[automata.size()+1][];
        root[0] = new String[automata.size()+1];
        root[0][0] = "plant = AND { ";
        String initState = "";
        final String memories = "";
        final ArrayList<Set<?>> markedStates = new ArrayList<Set<?>>();
        HashSet<String> tempMarkedStates = new HashSet<String>();
        for(int i = 0; i<automata.size(); i++)
        {
            final Automaton aut = automata.getAutomatonAt(i);
            final String aut_name = aut.getName().replace(" ", "_").replace(",", "");
            initState += (i+"#"+aut.getInitialState().getName()+" ");
            root[i+1] = new String[3+aut.nbrOfTransitions()];

            root[0][0] += aut_name;
            if(i < (automata.size()-1))
                root[0][0] += ", ";
            else
                root[0][0] += "}";

            tempMarkedStates = new HashSet<String>();
            root[0][i+1] = aut_name+" = OR { ";
            for(int j = 0; j<aut.nbrOfStates();j++)
            {
                final State state = aut.getStateWithIndex(j);
                final String state_name = (i+"#"+state.getName()).replace(" ", "_");
                if(state.isAccepting())
                {
                    tempMarkedStates.add(state_name);
                }
                root[0][i+1] += state_name;
                if(j < (aut.nbrOfStates()-1))
                    root[0][i+1] += ", ";
            }
            root[0][i+1] += " }";

            markedStates.add(tempMarkedStates);

            root[i+1][0] = aut_name;
            root[i+1][1] = "{";
            root[i+1][2] = "{";
            for(final EventProxy e:aut.getEvents())
            {
                if(e.getKind() == EventKind.CONTROLLABLE)
                {
                    root[i+1][1] += e.getName().replace(" ", "_")+", ";
                }
                else if(e.getKind() == EventKind.UNCONTROLLABLE)
                {
                    root[i+1][2] += e.getName().replace(" ", "_")+", ";
                }
            }
            if(root[i+1][1].endsWith(", "))
               root[i+1][1] = root[i+1][1].substring(0, (root[i+1][1].length()-2));
            if(root[i+1][2].endsWith(", "))
                root[i+1][2] = root[i+1][2].substring(0, root[i+1][2].length()-2);
            root[i+1][1] += "}";
            root[i+1][2] += "}";
            int k = 0;
            for(final TransitionProxy trans: aut.getTransitions())
            {
                root[i+1][3+k] = "["+(i+"#"+trans.getSource().getName()).replace(" ", "_")+" "+trans.getEvent().getName().replace(" ", "_")+" "+(i+"#"+trans.getTarget().getName()).replace(" ", "_")+"]";
                k++;
            }
        }

        for(int i=0;i<root.length;i++)
        {
            if(i == 0)
                pw.println("{");
            int j = 0;
            for(final String s:root[i])
            {
                if(i != 0 && j == 3)
                    pw.println("{");
                pw.println(s);
                j++;
            }
            pw.println("}");
            pw.println("");
        }

        // initial state
        pw.print("{");
        pw.print(initState.trim());
        pw.println("}");

        // marked states
        pw.print("{");
        final Set<Set<Object>> monMarkedStates = cartesianProduct(markedStates);
        int j = 0;
		for(final Set<?> hs: monMarkedStates)
		{
			final Iterator<?> it = hs.iterator();
            String mss = "";
			while(it.hasNext())
				mss += (it.next()+" ");
            mss = mss.trim();

            pw.print(" {");
            pw.print(mss);
            pw.print("} ");
            if(j < (monMarkedStates.size()-1))
                pw.print(", ");
            j++;
		}
        pw.println("}");

        // memories
        pw.print("{");
        pw.print(memories);
        pw.println("}");

        pw.flush();
        pw.close();
    }

    public void createSpec(final PrintWriter pw)
    {
        final SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
        synthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE);
        synthesizerOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHIC);
        synthesizerOptions.setPurge(false);
        synthesizerOptions.setMaximallyPermissive(true);
        synthesizerOptions.setMaximallyPermissiveIncremental(true);

/*        AutomataSynthesizer synthesizer = new AutomataSynthesizer(automata, SynchronizationOptions.getDefaultSynthesisOptions(), synthesizerOptions);

        Automaton supervisor=null;
        try
        {
            supervisor = synthesizer.execute().getFirstAutomaton();

        }
        catch (Exception ex)
        {
            logger.error("Exception while creating spec: ", ex);
            logger.debug(ex.getStackTrace());
        }
*/
        //Type 2 specification
        pw.print("{");
        for(int i = 0; i<automata.size(); i++)
        {
            final Automaton aut = automata.getAutomatonAt(i);
            for(final State state:aut)
            {
                if(state.isForbidden())
                {
                    final StringTokenizer st = new StringTokenizer((i+"#"+state.getName()).replace(" ", "_"),".");
                    String temp = "";
                    pw.print("{");
                    while(st.hasMoreTokens())
                    {
                        temp += (st.nextToken()+", ");
                    }
                    temp = temp.substring(0, temp.length()-2);
                    pw.print(temp);
                    pw.print("} ");
                }
            }
        }
        pw.println("}");
        //Type 2 specification
        pw.print("{");
        pw.print("}");

        pw.flush();
        pw.close();
    }

    public void createSpec(final String fileName)
    throws IOException
    {
        createSpec(new PrintWriter(new FileWriter(fileName)));
    }

    public void createSpec(final File theFile)
    throws IOException
    {
        createSpec(theFile.getAbsolutePath());
    }

    @Override
    public void serialize(final String fileName)
    throws IOException
    {
        serialize(new PrintWriter(new FileWriter(fileName)));
    }

    public void serialize(final File theFile)
    throws IOException
    {
        serialize(theFile.getAbsolutePath());
    }

    public String serialize()
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        serialize(pw);
        return sw.toString();
    }

	public static Set<Set<Object>> cartesianProduct(final ArrayList<Set<?>> sets)
	{
		if (sets.size() < 2)
			throw new IllegalArgumentException(
					"Can't have a product of fewer than two sets (got " + sets.size() + ")");

		return _cartesianProduct(0, sets);
	}

	private static Set<Set<Object>> _cartesianProduct(final int index, final ArrayList<Set<?>> sets)
	{
		final Set<Set<Object>> ret = new HashSet<Set<Object>>();
		if (index == sets.size()) {
			ret.add(new HashSet<Object>());
		} else {
			for (final Object obj : sets.get(index)) {

				for (final Set<Object> set : _cartesianProduct(index+1, sets)) {
					set.add(obj);
					ret.add(set);
				}
			}
		}
		return ret;
	}

}
