/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui.editor;



import java.util.*;

import com.nwoods.jgo.*;

import java.awt.Rectangle;

import org.supremica.automata.*;

public class Labels extends JGoArea {

    private ArcSet            theArcSet = null;
    private AutomatonDocument doc       = null;

    public Labels(AutomatonDocument doc, ArcSet theArcSet) throws Exception {

        this.doc       = doc;
        this.theArcSet = theArcSet;

        updateArcs();
    }

    public void initialize() {

        setSelectable(true);
        setDraggable(true);
        setResizable(true);
    }

    public void updateArcs() throws Exception {

        Automaton theAutomaton = doc.getAutomaton();
        Alphabet  theAlphabet  = theAutomaton.getAlphabet();

        for (Iterator arcIt = theArcSet.iterator(); arcIt.hasNext(); ) {
            Arc     currArc   = (Arc) arcIt.next();
            String  eventId   = currArc.getEventId();
            Event   currEvent = theAlphabet.getEventWithId(eventId);
            JGoText theText   = new JGoText(currEvent.getLabel());

            addObjectAtTail(theText);
        }
    }

    public void geometryChange(Rectangle rect) {
        super.geometryChange(rect);
        layoutChildren();
    }

    public void layoutChildren() {

        // System.err.println("layoutChildren");
        int     top      = getTop();
        int     left     = getLeft();
        JGoText prevText = null;
        int     i        = 0;

        for (JGoListPosition pos = getFirstObjectPos(); pos != null;
                pos = getNextObjectPos(pos)) {

            // System.out.println("layout: " + i);
            JGoText theText = (JGoText) getObjectAtPos(pos);

            theText.setSpotLocation(Center, left, top + 15 * i);

            i++;

            /*
            if (prevText != null)
            {
                    theText.setSpotLocation(TopLeft, prevText, BottomLeft);
            }
            else
            {
                    theText.setSpotLocation(TopLeft, getSpotLocation(TopLeft));
            }*/
            prevText = theText;
        }
    }
}

