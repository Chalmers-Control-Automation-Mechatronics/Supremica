
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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.MultiArc;
import org.supremica.automata.State;
import org.supremica.gui.ExportFormat;
import org.supremica.properties.Config;

public class AutomatonToDot
    implements AutomataSerializer
{

    // We hope that this is the size of an A4 page (isn't 8.5" times 11" ??)
    private static final int DEFAULT_WIDTH = 7;
    private static final int DEFAULT_HEIGHT = 11;
    private final Automaton aut;

    private boolean leftToRight = Config.DOT_LEFT_TO_RIGHT.getValue();
    private boolean withLabel = Config.DOT_WITH_STATE_LABELS.getValue();
    private boolean withCircles = Config.DOT_WITH_CIRCLES.getValue();
    private boolean useStateColors = Config.DOT_USE_STATE_COLORS.getValue();
    private boolean useArcColors = Config.DOT_USE_ARC_COLORS.getValue();
    private boolean writeEventLabels = Config.DOT_WITH_EVENT_LABELS.getValue();

    public AutomatonToDot(final Automaton aut)
    {
        this.aut = aut;
    }

    public boolean isLeftToRight()
    {
        return leftToRight;
    }

    public void setLeftToRight(final boolean leftToRight)
    {
        this.leftToRight = leftToRight;
    }

    public void setWithLabels(final boolean withLabel)
    {
        this.withLabel = withLabel;
    }

    public void setWithEventLabels(final boolean withLabel)
    {
        this.writeEventLabels = withLabel;
    }

    public void setWithCircles(final boolean withCircles)
    {
        this.withCircles = withCircles;
    }

    public void setUseStateColors(final boolean useStateColors)
    {
        this.useStateColors = useStateColors;
    }

    public void setUseArcColors(final boolean useArcColors)
    {
        this.useArcColors = useArcColors;
    }

    protected String getStateColor(final State s)
    {
        if (!useStateColors)
        {
            return "";
        }

        if (s.isAccepting() &&!s.isForbidden())
        {
            return ", color = green3";
        }

        if (s.isForbidden())
        {
            return ", color = red1";
        }

        return "";
    }

    protected String getArcColor(final boolean is_ctrl, final boolean is_prio, final boolean is_imm, final boolean is_eps, final boolean is_obs, final boolean is_prop)
    {
        if (useArcColors)
        {
            if (is_ctrl)
            {
                return ", color = green3";
            }
            else
            {
                return ", color = red1";
            }
        }

        return "";
    }

    @Override
    public void serialize(final PrintWriter pw)
    throws Exception
    {
        //aut.normalizeStateIdentities();

                /*
                EnumerateStates en = new EnumerateStates(aut, "q");
                en.execute();
                 */

        final Vector<State> initialStates = new Vector<State>();
        final String initPrefix = "__init_";
        String standardShape = null;
        String acceptingShape = null;
        String forbiddenShape = null;

        pw.println("digraph state_automaton {");
        pw.println("\tcenter = true;");

        // fix page size to this:
        pw.println("\tsize = \"" + DEFAULT_WIDTH + "," + DEFAULT_HEIGHT + "\";");

        if (leftToRight)
        {
            pw.println("\trankdir = LR;");
        }

        if (withCircles)
        {
            standardShape = "circle";
            acceptingShape = "doublecircle";
            forbiddenShape = "box";
        }
        else
        {
            standardShape = "plaintext";
            acceptingShape = "ellipse";
            forbiddenShape = "box";
        }
        if (!aut.hasInitialState())
        {
            pw.println("\t noState [shape = plaintext, label = \"No initial state\" ]");
            pw.println("}");
            pw.flush();
            pw.close();

            return;
        }

        for (final Iterator<?> states = aut.stateIterator(); states.hasNext(); )
        {
            final State state = (State) states.next();

            if (state.isInitial())
            {
                initialStates.addElement(state);
                pw.println("\t{node [shape = plaintext, style=invis, label=\"\"] \"" + initPrefix + state.getName() + "\"};");
            }

            if (state.isAccepting() &&!state.isForbidden())
            {
                pw.println("\t{node [shape = " + acceptingShape + "] \"" + state.getName() + "\"};");
            }

            if (state.isForbidden())
            {
                pw.println("\t{node [shape = " + forbiddenShape + "] \"" + state.getName() + "\"};");
            }
            else
            {
                pw.println("\t{node [shape = " + standardShape + "] \"" + state.getName() + "\"};");
            }
        }

        for (int i = 0; i < initialStates.size(); i++)
        {
            final String stateId = initialStates.elementAt(i).getName();

            // pw.println("\t\"" + initPrefix + stateId + "\" [label = \"\"]; ");
            // pw.println("\t\"" + initPrefix + stateId + "\" [height = \"0\"]; ");
            // pw.println("\t\"" + initPrefix + stateId + "\" [width = \"0\"]; ");
            pw.println("\t\"" + initPrefix + stateId + "\" -> \"" + stateId + "\";");
        }

        //Alphabet theAlphabet = aut.getAlphabet();
        for (final Iterator<?> states = aut.stateIterator(); states.hasNext(); )
        {
            final State sourceState = (State) states.next();

            pw.print("\t\"" + sourceState.getName() + "\" [label = \"");

            if (withLabel)
            {
                pw.print(EncodingHelper.normalize(sourceState.getName(), ExportFormat.DOT, false));
            }

            pw.println("\"" + getStateColor(sourceState) + "]; ");


            for (final Iterator<?> arcSets = sourceState.outgoingMultiArcIterator();
            arcSets.hasNext(); )
            {
                boolean is_ctrl = true;
                boolean is_prio = false;
                boolean is_imm = false;
                final boolean is_eps = false;
                boolean is_obs = false;
                boolean is_prop = false;
                final MultiArc currArcSet = (MultiArc) arcSets.next();
                final State fromState = currArcSet.getFromState();
                final State toState = currArcSet.getToState();

                pw.print("\t\"" + fromState.getName() + "\" -> \"" + toState.getName());

                pw.print("\" [ label = \"");

                if (writeEventLabels)
                {
                    for (final Iterator<?> arcIt = currArcSet.iterator(); arcIt.hasNext(); )
                    {
                        final Arc currArc = (Arc) arcIt.next();
                        final LabeledEvent thisEvent = currArc.getEvent();

                        if (!thisEvent.isControllable())
                        {
                            pw.print("!");

                            is_ctrl = false;
                        }

                        if (!thisEvent.isPrioritized())
                        {
                            pw.print("?");

                            is_prio = true;
                        }

                        if (thisEvent.isImmediate())
                        {
                            pw.print("#");

                            is_imm = true;
                        }

                        if (thisEvent.isProposition())
                        {
                            pw.print("@");

                            is_prop = true;
                        }

                        if (!thisEvent.isObservable())
                        {
                            pw.print("$");

                            is_obs = true;
                        }

                        pw.print(EncodingHelper.normalize(thisEvent.getLabel(), ExportFormat.DOT, false));

                        if (arcIt.hasNext())
                        {
                            pw.print("\\n");
                        }
                    }
                }
                pw.println("\" " + getArcColor(is_ctrl, is_prio, is_imm, is_eps, is_obs, is_prop) + "];");

                // Commented out large event label font. Did not like this but maybel we can include it as an option.
                //pw.println("\" " + getArcColor(is_ctrl, is_prio, is_imm, is_eps, is_obs, is_prop) + ", fontname=\"Helvetica\" , fontsize=\"26\"];");
            }
        }

        // An attemp to always start at the initial state.
        // The problem is that a rectangle is drawn around the initial state.
        // Ok, new versions of dot seems to be able to deal with this.
        for (final Iterator<State> stateIt = initialStates.iterator(); stateIt.hasNext(); )
        {
            final State currState = stateIt.next();

            pw.println("\t{ rank = min ;");
            pw.println("\t\t\"" + initPrefix + currState.getName() + "\";");
            pw.println("\t\t\"" + currState.getName() + "\";");
            pw.println("\t}");
        }

        pw.println("}");
        pw.flush();
        pw.close();
    }

    @Override
    public void serialize(final String fileName)
    throws Exception
    {
        serialize(new PrintWriter(new FileWriter(fileName)));
    }
}
