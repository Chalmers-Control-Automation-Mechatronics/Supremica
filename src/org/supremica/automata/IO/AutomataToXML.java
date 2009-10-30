
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

import java.io.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class AutomataToXML
    implements AutomataSerializer
{
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(AutomataToXML.class);
    private Automata automata;
    private boolean includeCost = true;
    private boolean debugMode = false;
    private final static int majorFileVersion = 0;
    private final static int minorFileVersion = 9;
    
    // mappings between state/event and id
    private Map<State, Integer> stateIdMap = new HashMap<State, Integer>();
    private Map<LabeledEvent, Integer> eventIdMap = new HashMap<LabeledEvent, Integer>();
    
    public AutomataToXML(Automata automata)
    {
        this.automata = automata;
    }
    
    public AutomataToXML(Automaton automaton)
    {
        this.automata = new Automata();  
        this.automata.addAutomaton(automaton);
    }
    
    public void serialize(PrintWriter pw)
    {
        pw.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        pw.print("<Automata");
        
        if (automata.getName() != null)
        {
            pw.print(" name=\"" + EncodingHelper.normalize(automata.getName()) + "\"");
        }
        
        // To keep track of indices
        //AutomataIndexMap indexMap = new AutomataIndexMap(automata);
        
        pw.print(" major=\"" + majorFileVersion + "\"");
        pw.print(" minor=\"" + minorFileVersion + "\"");        
        
        if ((automata.getComment() != null) && !automata.getComment().equals(""))
        {
            pw.print(" comment=\"" + EncodingHelper.normalize(automata.getComment()) + "\"");
        }
        
        pw.println(">");
        
        for(Automaton aut : automata)
        {   
            pw.println("<Automaton name=\"" + aut.getName() + "\" type=\"" + aut.getType().toString() + "\">");
            
            // Print all events
            pw.println("\t<Events>");
            
            int eventId = 0;
            
            for (LabeledEvent event : aut.getAlphabet())
            {         
                eventIdMap.put(event, eventId);
                pw.print("\t\t<Event id=\"" + eventId + "\" label=\"" + EncodingHelper.normalize(event.getLabel()) + "\"");
                
                eventId++;
                
                //--
                // pw.print("\t\t<Event id=\"" + EncodingHelper.normalize(event.getId()) + "\" label=\"" + EncodingHelper.normalize(event.getLabel()) + "\"");
                //--
                if (!event.isControllable())
                {
                    pw.print(" controllable=\"false\"");
                }
                
                if (!event.isPrioritized())
                {
                    pw.print(" prioritized=\"false\"");
                }
                
                if (!event.isObservable())
                {
                    pw.print(" observable=\"false\"");
                }
                
                if (event.isOperatorIncrease())
                {
                    pw.print(" operatorIncrease=\"true\"");
                }
                
                if (event.isOperatorReset())
                {
                    pw.print(" operatorReset=\"true\"");
                }
                
                if (event.isImmediate())
                {
                    pw.print(" immediate=\"true\"");
                }
                
                if (debugMode)
                {
                    pw.print(" index=" + event.getIndex());
                }
                
                pw.println("/>");
            }
            
            pw.println("\t</Events>");
            
            // Print all states
            pw.println("\t<States>");
            
            int stateId = 0;    // we need to make up id
            
            for(State state: aut)
            {         
                stateIdMap.put(state, stateId);    // The arc must be able to find it fast
                pw.print("\t\t<State id=\"" + stateId + "\"");    // no longer need to normalize
                
                stateId++;
                
                //--
                // pw.print("\t\t<State id=\"" + EncodingHelper.normalize(state.getId()) + "\"");
                //--
                pw.print(" name=\"" + EncodingHelper.normalize(state.getName()) + "\"");    // always print the name
                
                //--
                // if (!state.getId().equals(state.getName()))
                // {
                //      pw.print(" name=\"" + EncodingHelper.normalize(state.getName()) + "\"");
                // }
                //--
                if (state.isInitial())
                {
                    pw.print(" initial=\"true\"");
                }
                
                if (state.isAccepting())
                {
                    pw.print(" accepting=\"true\"");
                }
                
                if (state.isForbidden())
                {
                    pw.print(" forbidden=\"true\"");
                }
                
                if (includeCost)
                {
                    double value = state.getCost();
                    
                    if (value != State.UNDEF_COST)
                    {
                        pw.print(" cost=\"" + value + "\"");
                    }
                }
                
                if (debugMode)
                {
                    pw.print(" synchIndex=" + state.getIndex());
                }
                
                // printIntArray(pw, ((StateRegular)state).getOutgoingEventsIndicies());
                pw.println("/>");
            }
            
            pw.println("\t</States>");
            
            // Print all transitions
            pw.println("\t<Transitions>");
            
            for (State sourceState : aut) 
            {
                Integer sourceId = stateIdMap.get(sourceState);
                Iterator outgoingArcsIt = sourceState.outgoingArcsIterator();
                
                while (outgoingArcsIt.hasNext())
                {
                    Arc arc = (Arc) outgoingArcsIt.next();
                    State destState = arc.getToState();
                    Integer destId = stateIdMap.get(destState);
                    LabeledEvent event = arc.getEvent();
                    Integer eventID = eventIdMap.get(event);
                    
                    pw.print("\t\t<Transition source=\"" + sourceId);
                    pw.print("\" dest=\"" + destId);
                    pw.println("\" event=\"" + eventID + "\"/>");
                    
                    //--
                    // pw.print("\t\t<Transition source=\"" + EncodingHelper.normalize(sourceState.getId()));
                    // pw.print("\" dest=\"" + EncodingHelper.normalize(destState.getId()));
                    // pw.println("\" event=\"" + EncodingHelper.normalize(arc.getEventId()) + "\"/>");
                    //--
                }
            }
            
            pw.println("\t</Transitions>");
            pw.println("</Automaton>");
        }
        
        pw.println("</Automata>");
        pw.flush();
        pw.close();
    }
    
    public void serialize(String fileName)
    throws IOException
    {
        serialize(new PrintWriter(new FileWriter(fileName)));
    }
    
    public void serialize(File theFile)
    throws IOException
    {
        serialize(theFile.getAbsolutePath());
    }
    
    public String serialize()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        serialize(pw);
        return sw.toString();
    }
    
    void printIntArray(PrintWriter pw, int[] theArray)
    {
        for (int i : theArray)
        {
            if (i == 0)
            {
                pw.print(theArray[i]);
            }
            else
            {
                pw.print(" " + theArray[i]);
            }
        }
    }
    
    public boolean writeCost(boolean b)
    {
        boolean old = includeCost;
        
        includeCost = b;
        
        return old;
    }
}
