
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
package org.supremica.external.shoefactory.plantBuilder;

import org.supremica.automata.*;

public class SpecificationDEMO
{
	protected Project theSpec = null;
	private Alphabet currAlphabet;
	private int shoeNr, typ;

	public SpecificationDEMO(int nr, boolean type)
	{
		theSpec = new Project();
		shoeNr = nr;

		Automaton currShoe = new Automaton("shoeSpec" + nr);

		currShoe.setType(AutomatonType.SPECIFICATION);

		currAlphabet = currShoe.getAlphabet();

		State sInitial = currShoe.createAndAddUniqueState("q");

		sInitial.setInitial(true);
		sInitial.setAccepting(true);
		currShoe.setInitialState(sInitial);

		State s0 = currShoe.createAndAddUniqueState("q");
		LabeledEvent putT0L = new LabeledEvent("Shoe_" + nr + "put_T0L");
		LabeledEvent getT0R = new LabeledEvent("Shoe_" + nr + "get_T0R");

		currAlphabet.addEvent(putT0L);
		currAlphabet.addEvent(getT0R);

		LabeledEvent putT0R = new LabeledEvent("Shoe_" + nr + "put_T0R");
		LabeledEvent getT0L = new LabeledEvent("Shoe_" + nr + "get_T0L");

		currAlphabet.addEvent(putT0R);
		currAlphabet.addEvent(getT0L);

		LabeledEvent putT1 = new LabeledEvent("Shoe_" + nr + "put_T1");
		LabeledEvent getT1 = new LabeledEvent("Shoe_" + nr + "get_T1");

		currAlphabet.addEvent(putT1);
		currAlphabet.addEvent(getT1);

		LabeledEvent putT2 = new LabeledEvent("Shoe_" + nr + "put_T2");
		LabeledEvent getT2 = new LabeledEvent("Shoe_" + nr + "get_T2");

		currAlphabet.addEvent(putT2);
		currAlphabet.addEvent(getT2);

		LabeledEvent putT1S0 = new LabeledEvent("Shoe_" + nr + "put_T1_S0");
		LabeledEvent getT1S0 = new LabeledEvent("Shoe_" + nr + "get_T1_S0");

		currAlphabet.addEvent(putT1S0);
		currAlphabet.addEvent(getT1S0);

		LabeledEvent putT2S1 = new LabeledEvent("Shoe_" + nr + "put_T2_S1");
		LabeledEvent getT2S1 = new LabeledEvent("Shoe_" + nr + "get_T2_S1");

		currAlphabet.addEvent(putT2S1);
		currAlphabet.addEvent(getT2S1);

		Arc putArc = new Arc(sInitial, s0, putT0L);
		State s1 = currShoe.createAndAddUniqueState("q");
		State s2 = currShoe.createAndAddUniqueState("q");
		State s3 = currShoe.createAndAddUniqueState("q");
		State s4 = currShoe.createAndAddUniqueState("q");
		State s5 = currShoe.createAndAddUniqueState("q");
		State s6 = currShoe.createAndAddUniqueState("q");
		State s7 = currShoe.createAndAddUniqueState("q");
		State s8 = currShoe.createAndAddUniqueState("q");
		State s9 = currShoe.createAndAddUniqueState("q");
		State s10 = currShoe.createAndAddUniqueState("q");
		Arc putArc0 = new Arc(s0, s1, getT0R);

		if (type)
		{
			Arc putArc1 = new Arc(s1, s2, putT1);
			Arc putArc2 = new Arc(s2, s3, getT1S0);
			Arc putArc3 = new Arc(s3, s4, putT1S0);
			Arc putArc4 = new Arc(s4, s5, getT1);
			Arc putArc5 = new Arc(s5, s6, putT2);
			Arc putArc6 = new Arc(s6, s7, getT2S1);
			Arc putArc7 = new Arc(s7, s8, putT2S1);
			Arc putArc8 = new Arc(s8, s9, getT2);
			Arc putArc9 = new Arc(s9, s10, putT0R);
			Arc putArc10 = new Arc(s10, sInitial, getT0L);
		}
		else
		{
			Arc putArc1 = new Arc(s1, s2, putT2);
			Arc putArc2 = new Arc(s2, s3, getT2S1);
			Arc putArc3 = new Arc(s3, s4, putT2S1);
			Arc putArc4 = new Arc(s4, s5, getT2);
			Arc putArc5 = new Arc(s5, s6, putT1);
			Arc putArc6 = new Arc(s6, s7, getT1S0);
			Arc putArc7 = new Arc(s7, s8, putT1S0);
			Arc putArc8 = new Arc(s8, s9, getT1);
			Arc putArc9 = new Arc(s9, s10, putT0R);
			Arc putArc10 = new Arc(s10, sInitial, getT0L);
		}

		theSpec.addAutomaton(currShoe);
	}

	public Project getSpec()
	{
		return theSpec;
	}
}
