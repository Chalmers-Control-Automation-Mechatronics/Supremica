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
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;

/**
 * Generates output to dot for viewing the moular structure of an automata. I.e. Automaton-Objects
 * become nodes and if a pair of automata share a number of events, there is an edge between the
 * corresponding nodes labeled with the number of common events.
 *
 * @author hugo
 */
public class AutomataToHierarchyToDot
    implements AutomataSerializer
{
    private Automata theAutomata;
    private boolean leftToRight = false;
    private boolean withLabel = true;
    private boolean withCircles = false;
    private boolean useColors = false;
    
    public AutomataToHierarchyToDot(Automata aut)
    {
        this.theAutomata = aut;
    }
    
    public boolean isLeftToRight()
    {
        return leftToRight;
    }
    
    public void setLeftToRight(boolean leftToRight)
    {
        this.leftToRight = leftToRight;
    }
    
    public void setWithLabels(boolean withLabel)
    {
        this.withLabel = withLabel;
    }
    
    public void setWithCircles(boolean withCircles)
    {
        this.withCircles = withCircles;
    }
    
    public void setUseColors(boolean useColors)
    {
        this.useColors = useColors;
    }
    
    private String getColor(Automaton aut)
    {
        if (!useColors)
        {
            return "";
        }
        
        if (aut.isUndefined())
        {
            return ", color = pink";
        }
        
        if (aut.isPlant())
        {
            return ", color = red";
        }
        
        if (aut.isSupervisor())
        {
            return ",color = forestgreen";
        }
        
        if (aut.isSpecification())
        {
            return ", color = green";
        }
        
        // What on G*d's green earth was that?
        return ", color = white";
    }
    
    private String getShape(Automaton aut)
    {
        if (withCircles)
        {
            return "";
        }
        
        if (aut.isPlant())
        {
            return ", shape = box";
        }
        
        if (aut.isSupervisor() || aut.isSpecification())
        {
            return ", shape = ellipse";
        }
        
        if (aut.isUndefined())
        {
            return ", shape = egg";
        }
        
        // What the f**k was that?
        return "";
    }
    
    public void serialize(PrintWriter pw)
    throws Exception
    {
        pw.println("graph structure {");
        
        // pw.println("\tcenter = true;");
        // Left to right or top to bottom?
        if (leftToRight)
        {
            pw.println("\trankdir = LR;");
        }
        
        // Circles?
        if (withCircles)
        {
            pw.println("\tnode [shape = circle];");
        }
        else
        {
            
            //pw.println("\tnode [shape = plaintext];");
            //pw.println("\tnode [shape = ellipse];");
        }
        
        // Filled?
        if (useColors)
        {
            pw.println("\tnode [style = filled];");
        }
        
        // The automata are nodes in the graph
        //for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
        for (int i = 0; i < theAutomata.size(); i++)
        {
            Automaton currAutomaton = theAutomata.getAutomatonAt(i);
            
            pw.print("\t\"" + currAutomaton.getName() + "\" [label = \"");
            
            if (withLabel)
            {
                pw.print(EncodingHelper.normalize(currAutomaton.getName()));
            }
            
            pw.println("\"" + getColor(currAutomaton) + getShape(currAutomaton) + "]; ");
            
            // The arcs in the graph represent common events in the respective alphabets
            Alphabet currAlphabet = currAutomaton.getAlphabet();
            
            //for (Iterator otherIt = theAutomata.iterator(); otherIt.hasNext(); )
            for (int j = i + 1; j < theAutomata.size(); j++)
            {
                Automaton otherAutomaton = theAutomata.getAutomatonAt(j);
                Alphabet otherAlphabet = otherAutomaton.getAlphabet();
                
                Alphabet intersection = AlphabetHelpers.intersect(currAlphabet, otherAlphabet);
                int eventTotal = intersection.nbrOfEvents();
                int uncon = intersection.nbrOfUncontrollableEvents();
                
                if (eventTotal > 0)
                {                    
                    //pw.print("\t\"" + currAutomaton.getName() + "\" -- \"" + otherAutomaton.getName() + "\";");
                    pw.print("\t\"" + currAutomaton.getName() + "\" -- \"" + otherAutomaton.getName() + "\" ");
                    
                    if (eventTotal == 1)
                    {                        
                        // pw.print("[style = dashed, "); // This was incredibly impopular... (I thought it was neat...) /hguo
                        pw.print("[style = solid, ");
                    }
                    
                    if (eventTotal > 1)
                    {
                        pw.print("[style = solid, ");
                    }
                    
                    pw.println("label = \"" + eventTotal + " (" + uncon + ")\"];");
                }
            }
        }
        
        pw.println("}");
        pw.flush();
        pw.close();       
    }
    
    public void serialize(String fileName)
    throws Exception
    {
        serialize(new PrintWriter(new FileWriter(fileName)));
    }
}
