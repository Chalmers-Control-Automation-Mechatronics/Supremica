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
package org.supremica.external.shoefactory.plantBuilder;

import org.supremica.automata.*;

public class Specification
{
	protected Project theSpec = null;
	private Automaton station =null;
	private Alphabet currAlphabet;
	private int shoeNr;
	private boolean[] createdEvents = new boolean[13];
	private boolean [] midtab = new boolean [5];
	LabeledEvent put1Event,put2Event,put3Event,put4Event,put5Event;
	LabeledEvent get1Event,get2Event,get3Event,get4Event,get5Event;
	LabeledEvent put7Event,put8Event,put9Event,put10Event,put11Event,put12Event;
	LabeledEvent get7Event,get8Event,get9Event,get10Event,get11Event,get12Event;

	State tab1,tab2,tab3,tab4,tab5,g1,g2,g3,g4,g5,g6,g7;
	State tab7,tab8,tab9,tab10,tab11,tab12;

	public Specification(int nr, boolean[] sv)
	{
		theSpec = new Project();
		shoeNr = nr;

		Automaton currShoe = new Automaton("Shoe"+nr+" spec");
	  	currShoe.setType(AutomatonType.Specification);
	   	currAlphabet = currShoe.getAlphabet();

		State sInitial = currShoe.createAndAddUniqueState("q");
		sInitial.setInitial(true);
		sInitial.setAccepting(true);
		currShoe.setInitialState(sInitial);
		State s0 = currShoe.createAndAddUniqueState("q");

		LabeledEvent put0Event = new LabeledEvent("Shoe_"+nr+"put_T0");
		LabeledEvent get0Event = new LabeledEvent("Shoe_"+nr+"get_T0");
		currAlphabet.addEvent(put0Event);
		currAlphabet.addEvent(get0Event);

		Arc putArc = new Arc(sInitial, s0, put0Event);
		Arc getArc = new Arc(s0,sInitial, get0Event);
	    g1 = currShoe.createAndAddUniqueState("q");
		Arc tmp6g =new Arc(s0,g1,get0Event);
		Arc tmp6p =new Arc(g1,s0,put0Event);
		
		if(sv[0] || sv[1] || sv[2] || sv[3] || sv[4])
		{
			System.out.println("ok0");
			LabeledEvent put6Event = createPutEvent(6);
			LabeledEvent get6Event = createGetEvent(6);
			System.out.println("ok01");
			
			System.out.println("ok02");
			State tab6 = currShoe.createAndAddUniqueState("q");
			System.out.println("ok02");
			if(sv[0])
			{
				System.out.println("ok1in");
				LabeledEvent pt6s0 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S0"); 
				LabeledEvent gt6s0 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S0"); 
				currAlphabet.addEvent(pt6s0);
				currAlphabet.addEvent(gt6s0);
				System.out.println("ok1");
				State stat0 = currShoe.createAndAddUniqueState("q");
				Arc putstat0 = new Arc(tab6 ,stat0,pt6s0);
				Arc getstat0 = new Arc( stat0,tab6 ,gt6s0);	 
			
			}
			
			if(sv[1])
			{
				LabeledEvent pt6s1 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S1"); 
				LabeledEvent gt6s1 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S1"); 
				currAlphabet.addEvent(pt6s1);
				currAlphabet.addEvent(gt6s1);
				State stat1 = currShoe.createAndAddUniqueState("q");
				Arc putstat1 = new Arc(tab6 ,stat1, pt6s1);
				Arc getstat1 = new Arc( stat1,tab6 , gt6s1);	 
			}
			
			if(sv[2])
			{
				LabeledEvent pt6s2 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S2"); 
				LabeledEvent gt6s2 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S2"); 
				currAlphabet.addEvent(pt6s2);
				currAlphabet.addEvent(gt6s2);
				State stat2 = currShoe.createAndAddUniqueState("q");
				Arc putstat2 = new Arc(tab6 ,stat2, pt6s2);
				Arc getstat2 = new Arc( stat2,tab6 , gt6s2);	 
			}
			
			if(sv[3])
			{
				LabeledEvent pt6s3 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S3"); 
				LabeledEvent gt6s3 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S3"); 
				currAlphabet.addEvent(pt6s3);
				currAlphabet.addEvent(gt6s3);
				State stat3 = currShoe.createAndAddUniqueState("q");
				Arc putstat3 = new Arc(tab6 ,stat3, pt6s3);
				Arc getstat3 = new Arc( stat3,tab6 , gt6s3);	 
			}
			
			if(sv[4])
			{
				LabeledEvent pt6s4 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S4"); 
				LabeledEvent gt6s4 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S4"); 
				currAlphabet.addEvent(pt6s4);
				currAlphabet.addEvent(gt6s4);
				State stat4 = currShoe.createAndAddUniqueState("q");
				Arc putstat4 = new Arc(tab6 ,stat4, pt6s4);
				Arc getstat4 = new Arc( stat4,tab6 , gt6s4);	 
			}
			System.out.println("ok1");
			Arc putArc6 = new Arc( g1, tab6 , put6Event);
			Arc getArc6 = new Arc( tab6 ,g1, get6Event);
		
			System.out.println("ok1");
		}



		if(sv[5] || sv[6] || sv[7] || sv[8])
		{
			put7Event = createPutEvent(7);
			get7Event = createGetEvent(7);
			midtab[0]=true;

		}
		if(sv[9] || sv[10] || sv[11])
		{
			put8Event = createPutEvent(8);
			get8Event = createGetEvent(8);
			midtab[0]=true;
			midtab[1]=true;


		}
		if(sv[12] || sv[13] || sv[14] || sv[15])
		{

			put9Event = createPutEvent(9);
			get9Event = createGetEvent(9);
			midtab[0]=true;
			midtab[1]=true;
			midtab[2]=true;
		}
		if(sv[16] || sv[17] || sv[18] || sv[19])
		{
			put12Event = createPutEvent(12);
			get12Event = createGetEvent(12);
			midtab[0]=true;
			midtab[1]=true;
			midtab[2]=true;
			midtab[3]=true;
		}
		if(sv[20])
		{
			put10Event = createPutEvent(10);
			get10Event = createGetEvent(10);
			midtab[0]=true;
			midtab[1]=true;
			midtab[2]=true;
			midtab[3]=true;
		}
		if(sv[21] || sv[22] || sv[23])
		{
			put11Event = createPutEvent(11);
			get11Event =createGetEvent(11);
				
			midtab[0]=true;
			midtab[1]=true;
			midtab[2]=true;
			midtab[3]=true;
			midtab[4]=true;
		}



	if(	sv[21] || sv[22] || sv[23])
	{
		if(midtab[0])
		{
		put1Event = createPutEvent(1);
		get1Event =createGetEvent(1);
		tab1 = currShoe.createAndAddUniqueState("q");
		g2 = currShoe.createAndAddUniqueState("q");
		Arc ag2 = new Arc( tab1, g2 , get1Event);
		Arc ag2p = new Arc( g2,tab1 , put1Event);
		Arc putArc1 = new Arc( g1, tab1 , put1Event);
		Arc getArc1 = new Arc( tab1 ,g1, get1Event);
		midtab[0]=false;
		}

		if(midtab[1])
		{
		put2Event = createPutEvent(2);
		get2Event =createGetEvent(2);
		tab2 = currShoe.createAndAddUniqueState("q");
		g3 = currShoe.createAndAddUniqueState("q");
		Arc ag3 = new Arc( tab2, g3 , get2Event);
		Arc ag3p = new Arc( g3,tab2 , put2Event);
		Arc putArc2 = new Arc( g2 ,tab2, put2Event);
		Arc getArc2 = new Arc( tab2 ,g2, get2Event);
		midtab[1]=false;
		}
		if(midtab[2])
		{
		put3Event = createPutEvent(3);
		get3Event =createGetEvent(3);
		tab3 = currShoe.createAndAddUniqueState("q");
		g4 = currShoe.createAndAddUniqueState("q");
		Arc ag4 = new Arc( tab3, g4 , get3Event);
		Arc ag4p = new Arc( g4,tab3 , put3Event);
		Arc putArc3 = new Arc( g3 ,tab3, put3Event);
		Arc getArc3 = new Arc( tab3 ,g3, get3Event);
		midtab[2]=false;
		}

		System.out.println("Början");
		if(midtab[3])
		{
		put4Event = createPutEvent(4);
		get4Event =createGetEvent(4);
		tab4 = currShoe.createAndAddUniqueState("q");
		g5 = currShoe.createAndAddUniqueState("q");
		Arc ag5 = new Arc( tab4, g5 , get4Event);
		Arc ag5p = new Arc( g5,tab4 , put4Event);
		Arc putArc4 = new Arc( g4 ,tab4, put4Event);
		Arc getArc4 = new Arc( tab4 ,g4, get4Event);
		midtab[3]=false;
		}
		if(midtab[4])
		{
		put5Event = createPutEvent(5);
		get5Event =createGetEvent(5);
		tab5 = currShoe.createAndAddUniqueState("q");
		Arc putArc5 = new Arc( g5 ,tab5, put5Event);
		Arc getArc5 = new Arc( tab5 ,g5, get5Event);
		midtab[4]=false;
		}
		tab11 =currShoe.createAndAddUniqueState("q");
		
	if(sv[21])
		{
		LabeledEvent pt11s21 =new LabeledEvent("Shoe_"+shoeNr+"put_T11_S21"); 
		LabeledEvent gt11s21 =new LabeledEvent("Shoe_"+shoeNr+"get_T11_S21"); 
		currAlphabet.addEvent(pt11s21);
		currAlphabet.addEvent(gt11s21);
		State stat21 = currShoe.createAndAddUniqueState("q");
		Arc putstat21 = new Arc(tab11 ,stat21, pt11s21);
		Arc getstat21 = new Arc( stat21,tab11 , gt11s21);	 
		}	
	if(sv[22])
		{
		LabeledEvent pt11s22 =new LabeledEvent("Shoe_"+shoeNr+"put_T11_S22"); 
		LabeledEvent gt11s22 =new LabeledEvent("Shoe_"+shoeNr+"get_T11_S22"); 
		currAlphabet.addEvent(pt11s22);
		currAlphabet.addEvent(gt11s22);
		State stat22 = currShoe.createAndAddUniqueState("q");
		Arc putstat22 = new Arc(tab11 ,stat22, pt11s22);
		Arc getstat22 = new Arc( stat22,tab11 , gt11s22);	 
		}	
	
	if(sv[23])
		{
		LabeledEvent pt11s23 =new LabeledEvent("Shoe_"+shoeNr+"put_T11_S23"); 
		LabeledEvent gt11s23 =new LabeledEvent("Shoe_"+shoeNr+"get_T11_S23"); 
		currAlphabet.addEvent(pt11s23);
		currAlphabet.addEvent(gt11s23);
		State stat23 = currShoe.createAndAddUniqueState("q");
		Arc putstat23 = new Arc(tab11 ,stat23, pt11s23);
		Arc getstat23 = new Arc( stat23,tab11 , gt11s23);	 
		}	
		
		g7 = currShoe.createAndAddUniqueState("q");
		Arc ag7 = new Arc( tab5, g7 , get5Event);
		Arc ag7p = new Arc( g7,tab5 , put5Event);
		if(g7 ==null ||tab11==null)
			System.out.println("11null");
		Arc putArc11 = new Arc( g7,tab11, put11Event);
		Arc getArc11 = new Arc( tab11,g7, get11Event);
		
			System.out.println("ok2");
	}

	//System.out.println("Mitten");

		if(sv[20])
		{
			if(midtab[0])
			{
			put1Event = createPutEvent(1);
			get1Event =createGetEvent(1);
			tab1 = currShoe.createAndAddUniqueState("q");
		    g2 = currShoe.createAndAddUniqueState("q");
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc ag2p = new Arc( g2,tab1 , put1Event);
			Arc putArc1 = new Arc( g1, tab1 , put1Event);
			Arc getArc1 = new Arc( tab1 ,g1, get1Event);
			midtab[0]=false;
			}
			if(midtab[1])
			{
			put2Event = createPutEvent(2);
			get2Event =createGetEvent(2);
			tab2 = currShoe.createAndAddUniqueState("q");
			g3 = currShoe.createAndAddUniqueState("q");
			Arc ag3 = new Arc( tab2, g3 , get2Event);
			Arc ag3p = new Arc( g3,tab2 , put2Event);
			Arc putArc2 = new Arc( g2 ,tab2, put2Event);
			Arc getArc2 = new Arc( tab2 ,g2, get2Event);
			midtab[1]=false;
			}
			if(midtab[2])
			{
			put3Event = createPutEvent(3);
			get3Event =createGetEvent(3);
			tab3 = currShoe.createAndAddUniqueState("q");
			g4 = currShoe.createAndAddUniqueState("q");
			Arc ag4 = new Arc( tab3, g4 , get3Event);
			Arc ag4p = new Arc( g4,tab3 , put3Event);
			Arc putArc3 = new Arc( g3 ,tab3, put3Event);
			Arc getArc3 = new Arc( tab3 ,g3, get3Event);
			midtab[2]=false;
			}
			if(midtab[3])
			{
			put4Event = createPutEvent(4);
			get4Event =createGetEvent(4);
			tab4 = currShoe.createAndAddUniqueState("q");
			g5 = currShoe.createAndAddUniqueState("q");
			Arc ag5 = new Arc( tab4, g5 , get4Event);
			Arc ag5p = new Arc( g5,tab4 , put4Event);
			
			Arc putArc4 = new Arc( g4 ,tab4, put4Event);
			Arc getArc4 = new Arc( tab4 ,g4, get4Event);
			midtab[3]=false;
			}
			tab10 =currShoe.createAndAddUniqueState("q");
			
			LabeledEvent pt10s20 =new LabeledEvent("Shoe_"+shoeNr+"put_T10_S20"); 
			LabeledEvent gt10s20 =new LabeledEvent("Shoe_"+shoeNr+"get_T10_S20"); 
			currAlphabet.addEvent(pt10s20);
			currAlphabet.addEvent(gt10s20);
			State stat20 = currShoe.createAndAddUniqueState("q");
			Arc putstat20 = new Arc(tab10 ,stat20, pt10s20);
			Arc getstat20 = new Arc( stat20,tab10 , gt10s20);	 
				
			
			if(g5 ==null ||tab10==null)
			System.out.println("10null");
			Arc putArc10 = new Arc( g5,tab10, put10Event);
			Arc getArc10 = new Arc( tab10,g5, get10Event);
			}


	if(sv[16] || sv[17] || sv[18] || sv[19] )
		{
			if(midtab[0])
		{
			put1Event = createPutEvent(1);
			get1Event =createGetEvent(1);
			tab1 = currShoe.createAndAddUniqueState("q");
			g2 = currShoe.createAndAddUniqueState("q");
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc ag2p = new Arc( g2,tab1 , put1Event);
			Arc putArc1 = new Arc( g1, tab1 , put1Event);
			Arc getArc1 = new Arc( tab1 ,g1, get1Event);
			midtab[0]=false;
			}
			if(midtab[1])
			{
			put2Event = createPutEvent(2);
			get2Event =createGetEvent(2);
			tab2 = currShoe.createAndAddUniqueState("q");
			g3 = currShoe.createAndAddUniqueState("q");
			Arc ag3 = new Arc( tab2, g3 , get2Event);
			Arc ag3p = new Arc( g3,tab2 , put2Event);
			Arc putArc2 = new Arc( g2 ,tab2, put2Event);
			Arc getArc2 = new Arc( tab2 ,g2, get2Event);
			midtab[1]=false;
			}
			if(midtab[2])
			{
			put3Event = createPutEvent(3);
			get3Event =createGetEvent(3);
			tab3 = currShoe.createAndAddUniqueState("q");
			g4 = currShoe.createAndAddUniqueState("q");
			Arc ag4 = new Arc( tab3, g4 , get3Event);
			Arc ag4p = new Arc( g4,tab3 , put3Event);
			Arc putArc3 = new Arc( g3 ,tab3, put3Event);
			Arc getArc3 = new Arc( tab3 ,g3, get3Event);
			midtab[2]=false;
			}
			if(midtab[3])
			{
			put4Event = createPutEvent(4);
			get4Event =createGetEvent(4);
			tab4 = currShoe.createAndAddUniqueState("q");
			g5 = currShoe.createAndAddUniqueState("q");
			Arc ag5 = new Arc( tab4, g5 , get4Event);
			Arc ag5p = new Arc( g5,tab4 , put4Event);
			Arc putArc4 = new Arc( g4 ,tab4, put4Event);
			Arc getArc4 = new Arc( tab4 ,g4, get4Event);

			midtab[3]=false;
			}
			if(!sv[20])
			{
				tab10 =currShoe.createAndAddUniqueState("q");
				Arc putArc10 = new Arc( tab4,tab10, put10Event);
				Arc getArc10 = new Arc( tab10,tab4, get10Event);
			}
			tab12 =currShoe.createAndAddUniqueState("q");
			
		if(sv[16])
				{
				LabeledEvent pt12s16 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S16"); 
				LabeledEvent gt12s16 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S16"); 
				currAlphabet.addEvent(pt12s16);
				currAlphabet.addEvent(gt12s16);
				State stat16 = currShoe.createAndAddUniqueState("q");
				Arc putstat16 = new Arc(tab12 ,stat16, pt12s16);
				Arc getstat16 = new Arc( stat16,tab12 , gt12s16);	 
				}	
				
		if(sv[17])
				{
				LabeledEvent pt12s17 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S17"); 
				LabeledEvent gt12s17 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S17"); 
				currAlphabet.addEvent(pt12s17);
				currAlphabet.addEvent(gt12s17);
				State stat17 = currShoe.createAndAddUniqueState("q");
				Arc putstat17 = new Arc(tab12 ,stat17, pt12s17);
				Arc getstat17 = new Arc( stat17,tab12 , gt12s17);	 
				}			
			
		if(sv[18])
				{
				LabeledEvent pt12s18 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S18"); 
				LabeledEvent gt12s18 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S18"); 
				currAlphabet.addEvent(pt12s18);
				currAlphabet.addEvent(gt12s18);
				State stat18 = currShoe.createAndAddUniqueState("q");
				Arc putstat18 = new Arc(tab12 ,stat18, pt12s18);
				Arc getstat18 = new Arc( stat18,tab12 , gt12s18);	 
				}	
		if(sv[19])
				{
				LabeledEvent pt12s19 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S19"); 
				LabeledEvent gt12s19 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S19"); 
				currAlphabet.addEvent(pt12s19);
				currAlphabet.addEvent(gt12s19);
				State stat19 = currShoe.createAndAddUniqueState("q");
				Arc putstat19 = new Arc(tab12 ,stat19, pt12s19);
				Arc getstat19 = new Arc( stat19,tab12 , gt12s19);	 
				}	
		
			g6 = currShoe.createAndAddUniqueState("q");
			if(g6 ==null ||tab10==null)
			System.out.println("12null");
			Arc ag6 = new Arc( tab10, g6 , get10Event);
			Arc ag6p = new Arc( g6,tab10 , put10Event);
			
			if(g6 ==null ||tab12==null)
			System.out.println("12null");
			Arc putArc12 = new Arc( g6,tab12, put12Event);
			Arc getArc12 = new Arc( tab12,g6, get12Event);
				System.out.println("ok3");
			}

			
		if(sv[12] || sv[13] || sv[14] || sv[15])
			{
			if(midtab[0])
			{
			put1Event = createPutEvent(1);
			get1Event =createGetEvent(1);
			tab1 = currShoe.createAndAddUniqueState("q");
			g2 = currShoe.createAndAddUniqueState("q");
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc ag2p = new Arc( g2,tab1 , put1Event);
			Arc putArc1 = new Arc( g1 ,tab1, put1Event);
			Arc getArc1 = new Arc( tab1 ,g1, get1Event);
			midtab[0]=false;
			}
			if(midtab[1])
			{
			put2Event = createPutEvent(2);
			get2Event =createGetEvent(2);
			tab2 = currShoe.createAndAddUniqueState("q");
			g3 = currShoe.createAndAddUniqueState("q");
			Arc ag3 = new Arc( tab2, g3 , get2Event);
			Arc ag3p = new Arc( g3,tab2 , put2Event);
			Arc putArc2 = new Arc( g2 ,tab2, put2Event);
			Arc getArc2 = new Arc( tab2 ,g2, get2Event);
			midtab[1]=false;
			}
			if(midtab[2])
			{
			put3Event = createPutEvent(3);
			get3Event =createGetEvent(3);
			tab3 = currShoe.createAndAddUniqueState("q");
			g4 = currShoe.createAndAddUniqueState("q");
			Arc ag4 = new Arc( tab3, g4 , get3Event);
			Arc ag4p = new Arc( g4,tab3 , put3Event);
			Arc putArc3 = new Arc( g3 ,tab3, put3Event);
			Arc getArc3 = new Arc( tab3 ,tab2, get3Event);
			midtab[2]=false;
			}
			tab9 =currShoe.createAndAddUniqueState("q");
			
			if(sv[12])
				{
				LabeledEvent pt9s12 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S12"); 
				LabeledEvent gt9s12 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S12"); 
				currAlphabet.addEvent(pt9s12);
				currAlphabet.addEvent(gt9s12);
				State stat12 = currShoe.createAndAddUniqueState("q");
				Arc putstat12 = new Arc(tab9 ,stat12, pt9s12);
				Arc getstat12 = new Arc( stat12,tab9 , gt9s12);	 
				}
			if(sv[13])
				{
				LabeledEvent pt9s13 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S13"); 
				LabeledEvent gt9s13 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S13"); 
				currAlphabet.addEvent(pt9s13);
				currAlphabet.addEvent(gt9s13);
				State stat13 = currShoe.createAndAddUniqueState("q");
				Arc putstat13 = new Arc(tab9 ,stat13, pt9s13);
				Arc getstat13 = new Arc( stat13,tab9 , gt9s13);	 
				}
			
			if(sv[14])
				{
				LabeledEvent pt9s14 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S14"); 
				LabeledEvent gt9s14 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S14"); 
				currAlphabet.addEvent(pt9s14);
				currAlphabet.addEvent(gt9s14);
				State stat14 = currShoe.createAndAddUniqueState("q");
				Arc putstat14 = new Arc(tab9 ,stat14, pt9s14);
				Arc getstat14 = new Arc( stat14,tab9 , gt9s14);	 
				}
				
			if(sv[15])
				{
				LabeledEvent pt9s15 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S15"); 
				LabeledEvent gt9s15 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S15"); 
				currAlphabet.addEvent(pt9s15);
				currAlphabet.addEvent(gt9s15);
				State stat15 = currShoe.createAndAddUniqueState("q");
				Arc putstat15 = new Arc(tab9 ,stat15, pt9s15);
				Arc getstat15 = new Arc( stat15,tab9 , gt9s15);	 
				}	
			
			if(g4 ==null ||tab9==null)
			System.out.println("9null");
			Arc putArc9 = new Arc( g4,tab9, put9Event);
			Arc getArc9 = new Arc( tab9,g4, get9Event);
				System.out.println("ok4");
			}

			//System.out.println("Senare");


		if(sv[9] || sv[10] || sv[11])
			{
			if(midtab[0])
			{
			put1Event = createPutEvent(1);
			get1Event =createGetEvent(1);
			tab1 = currShoe.createAndAddUniqueState("q");
			g2 = currShoe.createAndAddUniqueState("q");
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc ag2p = new Arc( g2,tab1 , put1Event);
			Arc putArc1 = new Arc( g1 ,tab1, put1Event);
			Arc getArc1 = new Arc( tab1 ,g1, get1Event);
			midtab[0]=false;
			}
			if(midtab[1])
			{
			put2Event = createPutEvent(2);
			get2Event =createGetEvent(2);
			tab2 = currShoe.createAndAddUniqueState("q");
			g3 = currShoe.createAndAddUniqueState("q");
			Arc ag3 = new Arc( tab2, g3 , get2Event);
			Arc ag3p = new Arc( g3,tab2 , put2Event);
			Arc putArc2 = new Arc( g2 ,tab2, put2Event);
			Arc getArc2 = new Arc( tab2 ,g2, get2Event);
			midtab[1]=false;
			}
			tab8 =currShoe.createAndAddUniqueState("q");
			
			if(sv[9])
				{
				LabeledEvent pt8s9 =new LabeledEvent("Shoe_"+shoeNr+"put_T8_S9"); 
				LabeledEvent gt8s9 =new LabeledEvent("Shoe_"+shoeNr+"get_T8_S9"); 
				currAlphabet.addEvent(pt8s9);
				currAlphabet.addEvent(gt8s9);
				State stat9= currShoe.createAndAddUniqueState("q");
				Arc putstat9 = new Arc(tab8 ,stat9, pt8s9);
				Arc getstat9 = new Arc( stat9,tab8 , gt8s9);	 
				}
			
			if(sv[10])
				{
				LabeledEvent pt8s10 =new LabeledEvent("Shoe_"+shoeNr+"put_T8_S10"); 
				LabeledEvent gt8s10 =new LabeledEvent("Shoe_"+shoeNr+"get_T8_S10"); 
				currAlphabet.addEvent(pt8s10);
				currAlphabet.addEvent(gt8s10);
				State stat10 = currShoe.createAndAddUniqueState("q");
				Arc putstat10 = new Arc(tab8 ,stat10, pt8s10);
				Arc getstat10 = new Arc( stat10,tab8 , gt8s10);	 
				}
			
			if(sv[11])
				{
				LabeledEvent pt8s11 =new LabeledEvent("Shoe_"+shoeNr+"put_T8_S11"); 
				LabeledEvent gt8s11 =new LabeledEvent("Shoe_"+shoeNr+"get_T8_S11"); 
				currAlphabet.addEvent(pt8s11);
				currAlphabet.addEvent(gt8s11);
				State stat11 = currShoe.createAndAddUniqueState("q");
				Arc putstat11 = new Arc(tab8 ,stat11, pt8s11);
				Arc getstat11 = new Arc( stat11,tab8 , gt8s11);	 
				}
			
				if(g3 ==null ||tab8==null)
					System.out.println("8null");
				Arc putArc8 = new Arc( g3,tab8, put8Event);
				Arc getArc8 = new Arc( tab8,g3, get8Event);
				}

		if(sv[5] || sv[6] || sv[7] || sv[8])
			{
			if(midtab[0])
			{
				put1Event = createPutEvent(1);
				get1Event =createGetEvent(1);
				tab1 = currShoe.createAndAddUniqueState("q");
				g2 = currShoe.createAndAddUniqueState("q");
				Arc ag2 	= new Arc( tab1, g2 , get1Event);
				Arc ag2p = new Arc( g2,tab1 , put1Event);
				Arc putArc1 = new Arc( g1 ,tab1, put1Event);
				Arc getArc1 = new Arc( tab1 ,g1, get1Event);
				midtab[0]=false;
			}
				tab7 =currShoe.createAndAddUniqueState("q");
				if(sv[5])
				{
				LabeledEvent pt7s5 =new LabeledEvent("Shoe_"+shoeNr+"put_T7_S5"); 
				LabeledEvent gt7s5 =new LabeledEvent("Shoe_"+shoeNr+"get_T7_S5"); 
				currAlphabet.addEvent(pt7s5);
				currAlphabet.addEvent(gt7s5);
				State stat5 = currShoe.createAndAddUniqueState("q");
				Arc putstat5 = new Arc(tab7 ,stat5, pt7s5);
				Arc getstat5 = new Arc( stat5,tab7 , gt7s5);	 
				}
			if(sv[6])
				{
				LabeledEvent pt7s6 =new LabeledEvent("Shoe_"+shoeNr+"put_T7_S6"); 
				LabeledEvent gt7s6 =new LabeledEvent("Shoe_"+shoeNr+"get_T7_S6"); 
				currAlphabet.addEvent(pt7s6);
				currAlphabet.addEvent(gt7s6);
				State stat6 = currShoe.createAndAddUniqueState("q");
				Arc putstat6 = new Arc(tab7 ,stat6, pt7s6);
				Arc getstat6 = new Arc( stat6,tab7 , gt7s6);	 
				}
			
			if(sv[7])
				{
				LabeledEvent pt7s7=new LabeledEvent("Shoe_"+shoeNr+"put_T7_S7"); 
				LabeledEvent gt7s7=new LabeledEvent("Shoe_"+shoeNr+"get_T7_S7"); 
				currAlphabet.addEvent(pt7s7);
				currAlphabet.addEvent(gt7s7);
				State stat7= currShoe.createAndAddUniqueState("q");
				Arc putstat7 = new Arc(tab7 ,stat7, pt7s7);
				Arc getstat7 = new Arc( stat7, tab7 , gt7s7);	 
				}
			
			if(sv[8])
				{
				LabeledEvent pt7s8 =new LabeledEvent("Shoe_"+shoeNr+"put_T7_S8"); 
				LabeledEvent gt7s8 =new LabeledEvent("Shoe_"+shoeNr+"get_T7_S8"); 
				currAlphabet.addEvent(pt7s8);
				currAlphabet.addEvent(gt7s8);
				State stat8 = currShoe.createAndAddUniqueState("q");
				Arc putstat8 = new Arc(tab7 ,stat8, pt7s8);
				Arc getstat8 = new Arc( stat8,tab7 , gt7s8);	 
				}
			
			
				
			Arc putArc7 = new Arc( g2,tab7, put7Event);
			Arc getArc7 = new Arc( tab7,g2, get7Event);
			}


			// Create arcs


			theSpec.addAutomaton(currShoe);
		}


		public LabeledEvent createPutEvent(int nr)
		{
			LabeledEvent putEvent = new LabeledEvent("Shoe_"+shoeNr+"put_T"+nr);
			currAlphabet.addEvent(putEvent);
			return putEvent;
		}

		public LabeledEvent createGetEvent(int nr)
		{
			LabeledEvent getEvent = new LabeledEvent("Shoe_"+shoeNr+"get_T"+nr);
			currAlphabet.addEvent(getEvent);
			return getEvent;
		}

		public Project getSpec()
		{
			return theSpec;
		}
}