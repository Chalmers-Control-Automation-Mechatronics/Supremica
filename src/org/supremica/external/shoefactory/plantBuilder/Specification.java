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

	private boolean [] viatable = new boolean [5];
	/*The above array is there to keep track of the tables without stations. The array is true for
	 *This is because a event can only be created once. So when a table has created the events for the via-tables it resets the viatables it has created so the next
	so the following tables won't try to recreate them*/
	LabeledEvent put1Event,put2Event,put3Event,put4Event,put5Event,put6Event;
	LabeledEvent get1Event,get2Event,get3Event,get4Event,get5Event,get6Event;
	LabeledEvent put7Event,put8Event,put9Event,put10Event,put11Event,put12Event;
	LabeledEvent get7Event,get8Event,get9Event,get10Event,get11Event,get12Event;

	LabeledEvent putR1Event,putR2Event,putR3Event,putR4Event,putR5Event,putR10Event;
	LabeledEvent getL1Event,getL2Event,getL3Event,getL4Event,getL5Event,getL10Event;


	State tab1,tab2,tab3,tab4,tab5,g1,g2,g3,g4,g5,g6,g7 ;
	State h1,h2,h3,h4,h5,h6,h7;
	State i1,i2,i3,i4,i5,i6,i7;
	State s0ret1,s0ret2,s0ret3,s0ret4,s0ret5,s0ret6,s0ret7,s0ret8,s0ret9,s0ret10,s0ret11,s0ret12,s0ret13,s0ret14,s0ret15;
	State tab7,tab8,tab9,tab10,tab11,tab12;

	State E1,E2,E3,E4,E5,E6,E7,E8,E9,E10,E11,E12,E13,E14,E15,E16,E17,E18,E19,E20,E21,E22,E23,E24;
	Arc EA1,EA2,EA3,EA4,EA5,EA6,EA7,EA8,EA9,EA10,EA11,EA12,EA13,EA14,EA15,EA16,EA17,EA18,EA19,EA20,EA21,EA22,EA23,fr6;
	State Er2;
	boolean allowerror = false;   //allow error event or not

	LabeledEvent Error;


	public Specification(int nr, boolean[] sv)
	{
		theSpec = new Project();
		shoeNr = nr;


		Automaton currShoe = new Automaton("shoeSpec"+nr);
	  	currShoe.setType(AutomatonType.Specification);
	   	currAlphabet = currShoe.getAlphabet();

		State sInitial = currShoe.createAndAddUniqueState("q");
		sInitial.setInitial(true);
		sInitial.setAccepting(true);
		currShoe.setInitialState(sInitial);
		State s0 = currShoe.createAndAddUniqueState("q");

		LabeledEvent put0LEvent = new LabeledEvent("Shoe_"+nr+"put_T0L");
		LabeledEvent get0LEvent = new LabeledEvent("Shoe_"+nr+"get_T0L");
		currAlphabet.addEvent(put0LEvent);
		currAlphabet.addEvent(get0LEvent);

		LabeledEvent put0REvent = new LabeledEvent("Shoe_"+nr+"put_T0R");
		LabeledEvent get0REvent = new LabeledEvent("Shoe_"+nr+"get_T0R");
		currAlphabet.addEvent(put0REvent);
		currAlphabet.addEvent(get0REvent);

		Arc putArc = new Arc(sInitial, s0, put0LEvent);
		s0ret1 = currShoe.createAndAddUniqueState("q");
		s0ret2 = currShoe.createAndAddUniqueState("q");

		Arc getArc = new Arc(s0ret1,sInitial, get0LEvent);

	    g1 = currShoe.createAndAddUniqueState("q");


		Arc tmp6g =new Arc(s0,g1,get0REvent);
		Arc tmp6p =new Arc(s0ret2,s0ret1,put0REvent);

		Error =new LabeledEvent("Error"+nr);
		//Error.setControllable(false);

		currAlphabet.addEvent(Error);

		if(sv[0] || sv[1] || sv[2] || sv[3] || sv[4])
		{

			put6Event = createPutEvent(6);
			get6Event = createGetEvent(6);

			h1=currShoe.createAndAddUniqueState("q");

			State tab6 = currShoe.createAndAddUniqueState("q");

			if(allowerror)
				Er2=currShoe.createAndAddUniqueState("q");

			if(sv[0])
			{

				LabeledEvent pt6s0 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S0");
				LabeledEvent gt6s0 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S0");
				currAlphabet.addEvent(pt6s0);
				currAlphabet.addEvent(gt6s0);

				State stat0 = currShoe.createAndAddUniqueState("q");
				Arc putstat0 = new Arc(tab6 ,stat0,gt6s0);
				Arc getstat0 = new Arc( stat0,tab6 ,pt6s0);

				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat0,Er1,Error);
				Arc AEr2=new Arc(Er1,Er2,pt6s0);
				}


			}

			if(sv[1])
			{
				LabeledEvent pt6s1 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S1");
				LabeledEvent gt6s1 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S1");
				currAlphabet.addEvent(pt6s1);
				currAlphabet.addEvent(gt6s1);
				State stat1 = currShoe.createAndAddUniqueState("q");
				Arc putstat1 = new Arc(tab6 ,stat1, gt6s1);
				Arc getstat1 = new Arc( stat1,tab6 , pt6s1);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat1,Er1,Error);
				Arc AEr2=new Arc(Er1,Er2,pt6s1);
				}


			}

			if(sv[2])
			{
				LabeledEvent pt6s2 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S2");
				LabeledEvent gt6s2 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S2");
				currAlphabet.addEvent(pt6s2);
				currAlphabet.addEvent(gt6s2);
				State stat2 = currShoe.createAndAddUniqueState("q");
				Arc putstat2 = new Arc(tab6 ,stat2, gt6s2);
				Arc getstat2 = new Arc( stat2,tab6 , pt6s2);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat2,Er1,Error);
				Arc AEr2=new Arc(Er1,Er2,pt6s2);
				}
		}



			if(sv[3])
			{
				LabeledEvent pt6s3 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S3");
				LabeledEvent gt6s3 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S3");
				currAlphabet.addEvent(pt6s3);
				currAlphabet.addEvent(gt6s3);
				State stat3 = currShoe.createAndAddUniqueState("q");
				Arc putstat3 = new Arc(tab6 ,stat3, gt6s3);
				Arc getstat3 = new Arc( stat3,tab6 , pt6s3);

				if(allowerror)
				{

				State Er1=currShoe.createAndAddUniqueState("q");


				Arc AEr1=new Arc(stat3,Er1,Error);
				Arc AEr2=new Arc(Er1,Er2,pt6s3);
				}
			}

			if(sv[4])
			{
				LabeledEvent pt6s4 =new LabeledEvent("Shoe_"+shoeNr+"put_T6_S4");
				LabeledEvent gt6s4 =new LabeledEvent("Shoe_"+shoeNr+"get_T6_S4");
				currAlphabet.addEvent(pt6s4);
				currAlphabet.addEvent(gt6s4);
				State stat4 = currShoe.createAndAddUniqueState("q");
				Arc putstat4 = new Arc(tab6 ,stat4,gt6s4);
				Arc getstat4 = new Arc( stat4,tab6,pt6s4);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");


				Arc AEr1=new Arc(stat4,Er1,Error);
				Arc AEr2=new Arc(Er1,Er2,pt6s4);
				}
			}

			Arc putArc6 = new Arc( g1,tab6,put6Event);
			Arc getArc6 = new Arc( tab6,h1,get6Event);


		}



		if(sv[5] || sv[6] || sv[7] || sv[8])
		{
			put7Event = createPutEvent(7);
			get7Event = createGetEvent(7);
			viatable[0]=true;

		}
		if(sv[9] || sv[10] || sv[11])
		{
			put8Event = createPutEvent(8);
			get8Event = createGetEvent(8);
			viatable[0]=true;
			viatable[1]=true;


		}
		if(sv[12] || sv[13] || sv[14] || sv[15])
		{

			put9Event = createPutEvent(9);
			get9Event = createGetEvent(9);


			viatable[0]=true;
			viatable[1]=true;
			viatable[2]=true;

		}

		if(sv[16] || sv[17] || sv[18] || sv[19])
		{
			put12Event = createPutEvent(12);
			get12Event = createGetEvent(12);


			viatable[0]=true;
			viatable[1]=true;
			viatable[2]=true;
			viatable[3]=true;
		}

		if(sv[20])
		{
			put10Event = new LabeledEvent("Shoe_"+shoeNr+"put_T10L");
			currAlphabet.addEvent(put10Event);
			get10Event = new LabeledEvent("Shoe_"+shoeNr+"get_T10R");
			currAlphabet.addEvent(get10Event);
			viatable[0]=true;
			viatable[1]=true;
			viatable[2]=true;
			viatable[3]=true;
		}

		if(sv[21] || sv[22] || sv[23])
		{
			put11Event = createPutEvent(11);
			get11Event =createGetEvent(11);

			viatable[0]=true;
			viatable[1]=true;
			viatable[2]=true;
			viatable[3]=true;
			viatable[4]=true;
		}

			if(allowerror ||sv[16]||sv[17]||sv[18]||sv[19])
			{
			put10Event = new LabeledEvent("Shoe_"+shoeNr+"put_T10L");
			currAlphabet.addEvent(put10Event);
			getL10Event= new LabeledEvent("Shoe_"+shoeNr+"get_T10L");
			currAlphabet.addEvent(getL10Event);
			}
/*The statement-body below is for the error-handler, it uses the system with via-tables to be more general
,i.e. if you for instance wants to place the error-handling-station at table7, then only viatable[0]=via table 1 would be st true below.
the if(true) is there so you can discard the error events completly if needed.
*/

	if(allowerror)
		{
			viatable[0]=true; //via table1
			viatable[1]=true; //via table2
			viatable[2]=true; //via table3
			viatable[3]=true; //via table4


			if(viatable[0])
			{
			put1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1L");
			currAlphabet.addEvent(put1Event);
			get1Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T1R");
			currAlphabet.addEvent(get1Event);

			putR1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1R");
			currAlphabet.addEvent(putR1Event);

			getL1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1L");
			currAlphabet.addEvent(getL1Event);

			s0ret3 =currShoe.createAndAddUniqueState("q");



			Arc	ag2p = new Arc( s0ret3,s0ret2 , getL1Event);
			s0ret4 =currShoe.createAndAddUniqueState("q");
			Arc ag3p = new Arc( s0ret4,s0ret3 , putR1Event);

		if(sv[5] || sv[6] || sv[6]|| sv[8] || sv[9] || sv[10] || sv[11])
		{
			tab1 = currShoe.createAndAddUniqueState("q");
		    g2 = currShoe.createAndAddUniqueState("q");
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc putArc1 = new Arc( g1, tab1 , put1Event);
		}
			viatable[0]=false;

	}
			if(viatable[1])
			{
			put2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2L");
			currAlphabet.addEvent(put2Event);
			get2Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T2R");
			currAlphabet.addEvent(get2Event);




			putR2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2R");
			currAlphabet.addEvent(putR2Event);
			getL2Event =new LabeledEvent("Shoe_"+shoeNr+"get_T2L");
			currAlphabet.addEvent(getL2Event);
			s0ret5 =currShoe.createAndAddUniqueState("q");
			s0ret6 =currShoe.createAndAddUniqueState("q");

			Arc ag3p = new Arc( s0ret6,s0ret5 , putR2Event);
			Arc ag2p = new Arc( s0ret5,s0ret4 , getL2Event);


			if(sv[9] || sv[10] || sv[11])
			{

				tab2 = currShoe.createAndAddUniqueState("q");
				g3 = currShoe.createAndAddUniqueState("q");
				Arc ag3 = new Arc( tab2, g3 , get2Event);
				Arc putArc2 = new Arc( g2 ,tab2, put2Event);
			}
			viatable[1]=false;
			}



			if(viatable[2])
			{
			put3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3L");
			currAlphabet.addEvent(put3Event);
			get3Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T3R");
			currAlphabet.addEvent(get3Event);
			tab3 = currShoe.createAndAddUniqueState("q");
			g4 = currShoe.createAndAddUniqueState("q");



			putR3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3R");
			currAlphabet.addEvent(putR3Event);
			getL3Event =new LabeledEvent("Shoe_"+shoeNr+"get_T3L");
			currAlphabet.addEvent(getL3Event);

			s0ret7 =currShoe.createAndAddUniqueState("q");
			s0ret8 =currShoe.createAndAddUniqueState("q");


			Arc ag5p = new Arc( s0ret8,s0ret7 , putR3Event);
			Arc ag4p = new Arc( s0ret7,s0ret6 , getL3Event);



			Arc ag4 = new Arc( tab3, g4 , get3Event);
			Arc putArc3;
			if(sv[9] || sv[10] || sv[11])
				 putArc3 = new Arc( g3 ,tab3, put3Event);

			viatable[2]=false;
		}

			if(viatable[3])
			{
			put4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4L");
			currAlphabet.addEvent(put4Event);

			get4Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T4R");
			currAlphabet.addEvent(get4Event);


			putR4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4R");
			currAlphabet.addEvent(putR4Event);
			getL4Event =new LabeledEvent("Shoe_"+shoeNr+"get_T4L");
			currAlphabet.addEvent(getL4Event);

			if(sv[16]||sv[17]||sv[18]||sv[19]||sv[21]||sv[22]||sv[23])
			{

			tab4 = currShoe.createAndAddUniqueState("q");
			g5 = currShoe.createAndAddUniqueState("q");
			s0ret9 =currShoe.createAndAddUniqueState("q");
			s0ret10 =currShoe.createAndAddUniqueState("q");

			Arc ag7p = new Arc( s0ret10,s0ret9 ,putR4Event);
			Arc ag6p = new Arc( s0ret9,s0ret8 ,getL4Event);
			Arc ag5 = new Arc( tab4, g5 , get4Event);
			Arc putArc4 = new Arc( g4 ,tab4, put4Event);
			}

			viatable[3]=false;
			}









		if(sv[0]||sv[1]||sv[2]||sv[3]||sv[4])
		{
			E1=currShoe.createAndAddUniqueState("q");
			E2=currShoe.createAndAddUniqueState("q");
			E3=currShoe.createAndAddUniqueState("q");
			E4=currShoe.createAndAddUniqueState("q");
			E5=currShoe.createAndAddUniqueState("q");
			E6=currShoe.createAndAddUniqueState("q");
			E7=currShoe.createAndAddUniqueState("q");
			E8=currShoe.createAndAddUniqueState("q");
			E9=currShoe.createAndAddUniqueState("q");
			E10=currShoe.createAndAddUniqueState("q");

			E11=currShoe.createAndAddUniqueState("q");
			E12=currShoe.createAndAddUniqueState("q");
			E13=currShoe.createAndAddUniqueState("q");
			E14=currShoe.createAndAddUniqueState("q");
			E15=currShoe.createAndAddUniqueState("q");
			E16=currShoe.createAndAddUniqueState("q");
			E17=currShoe.createAndAddUniqueState("q");
			E18=currShoe.createAndAddUniqueState("q");
			E19=currShoe.createAndAddUniqueState("q");
			E20=currShoe.createAndAddUniqueState("q");
			E21=currShoe.createAndAddUniqueState("q");
			E22=currShoe.createAndAddUniqueState("q");


			EA1=new Arc( E1,E2,put1Event);
			EA2=new Arc( E2,E3,get1Event);
			EA3=new Arc( E3,E4,put2Event);
			EA4=new Arc( E4,E5,get2Event);
			EA5=new Arc( E5,E6,put3Event);
			EA6=new Arc( E6,E7,get3Event);
			EA7=new Arc( E7,E8,put4Event);
			EA8=new Arc( E8,E9,get4Event);
			EA9=new Arc( E9,E10,put10Event);

			fr6=new Arc( Er2,E1,get6Event);

		}

		else if(sv[5]||sv[6]||sv[7]||sv[8] &&!(sv[0]||sv[1]||sv[2]||sv[3]||sv[4]))
		{

			E3=currShoe.createAndAddUniqueState("q");
			E4=currShoe.createAndAddUniqueState("q");
			E5=currShoe.createAndAddUniqueState("q");
			E6=currShoe.createAndAddUniqueState("q");
			E7=currShoe.createAndAddUniqueState("q");
			E8=currShoe.createAndAddUniqueState("q");
			E9=currShoe.createAndAddUniqueState("q");
			E10=currShoe.createAndAddUniqueState("q");
			E11=currShoe.createAndAddUniqueState("q");
			E12=currShoe.createAndAddUniqueState("q");
			E13=currShoe.createAndAddUniqueState("q");
			E14=currShoe.createAndAddUniqueState("q");
			E15=currShoe.createAndAddUniqueState("q");
			E16=currShoe.createAndAddUniqueState("q");
			E17=currShoe.createAndAddUniqueState("q");
			E18=currShoe.createAndAddUniqueState("q");
			E19=currShoe.createAndAddUniqueState("q");
			E20=currShoe.createAndAddUniqueState("q");
			E21=currShoe.createAndAddUniqueState("q");
			E22=currShoe.createAndAddUniqueState("q");


			EA3=new Arc( E3,E4,put2Event);
			EA4=new Arc( E4,E5,get2Event);
			EA5=new Arc( E5,E6,put3Event);
			EA6=new Arc( E6,E7,get3Event);
			EA7=new Arc( E7,E8,put4Event);
			EA8=new Arc( E8,E9,get4Event);
			EA9=new Arc( E9,E10,put10Event);


		}

		else if(sv[9]||sv[10]||sv[11])
		{

			E5=currShoe.createAndAddUniqueState("q");
			E6=currShoe.createAndAddUniqueState("q");
			E7=currShoe.createAndAddUniqueState("q");
			E8=currShoe.createAndAddUniqueState("q");
			E9=currShoe.createAndAddUniqueState("q");
			E10=currShoe.createAndAddUniqueState("q");
			E11=currShoe.createAndAddUniqueState("q");
			E12=currShoe.createAndAddUniqueState("q");
			E13=currShoe.createAndAddUniqueState("q");
			E14=currShoe.createAndAddUniqueState("q");
			E15=currShoe.createAndAddUniqueState("q");
			E16=currShoe.createAndAddUniqueState("q");
			E17=currShoe.createAndAddUniqueState("q");
			E18=currShoe.createAndAddUniqueState("q");
			E19=currShoe.createAndAddUniqueState("q");
			E20=currShoe.createAndAddUniqueState("q");
			E21=currShoe.createAndAddUniqueState("q");
			E22=currShoe.createAndAddUniqueState("q");


			EA5=new Arc( E5,E6,put3Event);
			EA6=new Arc( E6,E7,get3Event);
			EA7=new Arc( E7,E8,put4Event);
			EA8=new Arc( E8,E9,get4Event);
			EA9=new Arc( E9,E10,put10Event);




		}



	LabeledEvent pt10s20 =new LabeledEvent("Shoe_"+shoeNr+"put_T10_S20");
	LabeledEvent gt10s20 =new LabeledEvent("Shoe_"+shoeNr+"get_T10_S20");
	currAlphabet.addEvent(pt10s20);
	currAlphabet.addEvent(gt10s20);
	EA10=new Arc( E10,E11,gt10s20);
	EA11=new Arc( E11,E12,pt10s20);

	EA12=new Arc( E12,E13,getL10Event);

	EA13=new Arc( E13,E14,putR4Event);
	EA14=new Arc( E14,E15,getL4Event);
	EA15=new Arc( E15,E16,putR3Event);
	EA16=new Arc( E16,E17,getL3Event);

	EA17=new Arc( E17,E18,putR2Event);
	EA18=new Arc( E18,E19,getL2Event);
	EA19=new Arc( E19,E20,putR1Event);
	EA20=new Arc( E20,E21,getL1Event);

	EA21=new Arc( E21,E22,put0REvent);
	EA22=new Arc( E22,sInitial,get0LEvent);







}

 //visit table 11?

	if(	sv[21] || sv[22] || sv[23])
	{

		if((sv[16]||sv[17]||sv[18]||sv[19]||sv[20]))
			s0ret14 =currShoe.createAndAddUniqueState("q");


		if(viatable[0])
		{
		put1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1L");
		currAlphabet.addEvent(put1Event);

		get1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1R");
		currAlphabet.addEvent(get1Event);


		putR1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1R");
		currAlphabet.addEvent(putR1Event);

		getL1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1L");
		currAlphabet.addEvent(getL1Event);

		s0ret3 =currShoe.createAndAddUniqueState("q");
		s0ret4 =currShoe.createAndAddUniqueState("q");
		Arc ag3p = new Arc( s0ret4,s0ret3 , putR1Event);
		Arc ag2p = new Arc( s0ret3,s0ret2 , getL1Event);



	if(sv[5] || sv[6] || sv[6]|| sv[8] || sv[9] || sv[10] || sv[11])
	{

		tab1 = currShoe.createAndAddUniqueState("q");
		g2 = currShoe.createAndAddUniqueState("q");
		Arc ag2 = new Arc( tab1, g2 , get1Event);
		Arc putArc1 = new Arc( g1, tab1 , put1Event);
	}
		viatable[0]=false;
		}

		if(viatable[1])
		{
		put2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2L");
		currAlphabet.addEvent(put2Event);
		get2Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T2R");
		currAlphabet.addEvent(get2Event);


		putR2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2R");
		currAlphabet.addEvent(putR2Event);

		getL2Event =new LabeledEvent("Shoe_"+shoeNr+"get_T2L");
		currAlphabet.addEvent(getL2Event);

		s0ret5 =currShoe.createAndAddUniqueState("q");
		s0ret6 =currShoe.createAndAddUniqueState("q");
		Arc ag3p = new Arc( s0ret6,s0ret5 , putR2Event);
		Arc ag2p = new Arc( s0ret5,s0ret4 , getL2Event);




		if(sv[9] || sv[10] || sv[11])
		{

			tab2 = currShoe.createAndAddUniqueState("q");
			g3 = currShoe.createAndAddUniqueState("q");
			Arc ag3 = new Arc( tab2, g3 , get2Event);
			Arc putArc2 = new Arc( g2 ,tab2, put2Event);

		}

		viatable[1]=false;
		}
		if(viatable[2])
		{

		put3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3L");
		currAlphabet.addEvent(put3Event);
		get3Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T3R");
		currAlphabet.addEvent(get3Event);
		tab3 = currShoe.createAndAddUniqueState("q");
		g4 = currShoe.createAndAddUniqueState("q");


		putR3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3R");
		currAlphabet.addEvent(putR3Event);
		getL3Event =new LabeledEvent("Shoe_"+shoeNr+"get_T3L");
		currAlphabet.addEvent(getL3Event);
		s0ret7 =currShoe.createAndAddUniqueState("q");
		s0ret8 =currShoe.createAndAddUniqueState("q");

		Arc ag5p = new Arc( s0ret8,s0ret7 ,putR3Event);
		Arc ag4p = new Arc( s0ret7,s0ret6 ,getL3Event);



		Arc ag4 = new Arc( tab3, g4 , get3Event);

		Arc putArc3;
		if(sv[9] || sv[10] || sv[11])
			putArc3 = new Arc( g3 ,tab3, put3Event);

		viatable[2]=false;
		}


		if(viatable[3])
		{
		put4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4L");
		currAlphabet.addEvent(put4Event);
		get4Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T4R");
		currAlphabet.addEvent(get4Event);

		tab4 = currShoe.createAndAddUniqueState("q");
		g5 = currShoe.createAndAddUniqueState("q");

		putR4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4R");
		currAlphabet.addEvent(putR4Event);
		getL4Event =new LabeledEvent("Shoe_"+shoeNr+"get_T4L");
		currAlphabet.addEvent(getL4Event);
		s0ret9 =currShoe.createAndAddUniqueState("q");
		s0ret10 =currShoe.createAndAddUniqueState("q");

		Arc ag7p = new Arc( s0ret10,s0ret9 ,putR4Event);
		Arc ag6p = new Arc( s0ret9,s0ret8 ,getL4Event);



		Arc ag5 = new Arc( tab4, g5 , get4Event);
		Arc putArc4 = new Arc( g4 ,tab4, put4Event);

		viatable[3]=false;
		}
		if(viatable[4])
		{
		put5Event = new LabeledEvent("Shoe_"+shoeNr+"put_T5L");
		currAlphabet.addEvent(put5Event);
		get5Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T5R");
		currAlphabet.addEvent(get5Event);

		putR5Event = new LabeledEvent("Shoe_"+shoeNr+"put_T5R");
		currAlphabet.addEvent(putR5Event);
		getL5Event =new LabeledEvent("Shoe_"+shoeNr+"get_T5L");
		currAlphabet.addEvent(getL5Event);

		s0ret11 =currShoe.createAndAddUniqueState("q");
		s0ret12 =currShoe.createAndAddUniqueState("q");

		Arc ag9p = new Arc( s0ret12,s0ret11 ,putR5Event);
		Arc ag8p = new Arc( s0ret11,s0ret10 ,getL5Event);


		tab5 = currShoe.createAndAddUniqueState("q");
		viatable[4]=false;
		}
		tab11 =currShoe.createAndAddUniqueState("q");

	if(sv[21])
		{
		LabeledEvent pt11s21 =new LabeledEvent("Shoe_"+shoeNr+"put_T11_S21");
		LabeledEvent gt11s21 =new LabeledEvent("Shoe_"+shoeNr+"get_T11_S21");
		currAlphabet.addEvent(pt11s21);
		currAlphabet.addEvent(gt11s21);
		State stat21 = currShoe.createAndAddUniqueState("q");
		Arc putstat21 = new Arc(tab11 ,stat21, gt11s21);
		Arc getstat21 = new Arc( stat21,tab11 , pt11s21);


		if(allowerror)
		{
		State Er1=currShoe.createAndAddUniqueState("q");
		State Er3=currShoe.createAndAddUniqueState("q");
		State Er4=currShoe.createAndAddUniqueState("q");
		State Er5=currShoe.createAndAddUniqueState("q");
		State Er6=currShoe.createAndAddUniqueState("q");

		Arc AEr1=new Arc(stat21,Er1,Error);
		Arc AEr2=new Arc(Er1,Er3,pt11s21);
		Arc AEr3=new Arc(Er3,Er4,get11Event);
		Arc AEr4=new Arc(Er4,Er5,putR5Event);
		Arc AEr5=new Arc(Er5,Er6,getL5Event);
		Arc AEr6=new Arc(Er6,E10,put10Event);
		}

		}


	if(sv[22])
		{
		LabeledEvent pt11s22 =new LabeledEvent("Shoe_"+shoeNr+"put_T11_S22");
		LabeledEvent gt11s22 =new LabeledEvent("Shoe_"+shoeNr+"get_T11_S22");
		currAlphabet.addEvent(pt11s22);
		currAlphabet.addEvent(gt11s22);
		State stat22 = currShoe.createAndAddUniqueState("q");
		Arc putstat22 = new Arc(tab11 ,stat22, gt11s22);
		Arc getstat22 = new Arc( stat22,tab11 , pt11s22);


		if(allowerror)
		{

		State Er1=currShoe.createAndAddUniqueState("q");
		State Er3=currShoe.createAndAddUniqueState("q");
		State Er4=currShoe.createAndAddUniqueState("q");
		State Er5=currShoe.createAndAddUniqueState("q");
		State Er6=currShoe.createAndAddUniqueState("q");

		Arc AEr1=new Arc(stat22,Er1,Error);
		Arc AEr2=new Arc(Er1,Er3,pt11s22);
		Arc AEr3=new Arc(Er3,Er4,get11Event);
		Arc AEr4=new Arc(Er4,Er5,putR5Event);
		Arc AEr5=new Arc(Er5,Er6,getL5Event);
		Arc AEr6=new Arc(Er6,E10,put10Event);
		}

		}

	if(sv[23])
		{
		LabeledEvent pt11s23 =new LabeledEvent("Shoe_"+shoeNr+"put_T11_S23");
		LabeledEvent gt11s23 =new LabeledEvent("Shoe_"+shoeNr+"get_T11_S23");
		currAlphabet.addEvent(pt11s23);
		currAlphabet.addEvent(gt11s23);
		State stat23 = currShoe.createAndAddUniqueState("q");
		Arc putstat23 = new Arc(tab11 ,stat23, gt11s23);
		Arc getstat23 = new Arc( stat23,tab11 , pt11s23);



		if(allowerror)
		{
		State Er1=currShoe.createAndAddUniqueState("q");
		State Er3=currShoe.createAndAddUniqueState("q");
		State Er4=currShoe.createAndAddUniqueState("q");
		State Er5=currShoe.createAndAddUniqueState("q");
		State Er6=currShoe.createAndAddUniqueState("q");

		Arc AEr1=new Arc(stat23,Er1,Error);
		Arc AEr2=new Arc(Er1,Er3,pt11s23);
		Arc AEr3=new Arc(Er3,Er4,get11Event);
		Arc AEr4=new Arc(Er4,Er5,putR5Event);
		Arc AEr5=new Arc(Er5,Er6,getL5Event);
		Arc AEr6=new Arc(Er6,E10,put10Event);

		}
		}

		g7 = currShoe.createAndAddUniqueState("q");
		Arc ag7 = new Arc( tab5, g7 , get5Event);
		Arc ag7p;

		if(!(sv[16]||sv[17]||sv[18]||sv[19]||sv[20]))
			ag7p = new Arc( g5,tab5 , put5Event);
		else
		 ag7p = new Arc( s0ret14,tab5 , put5Event);



		Arc putArc11 = new Arc( g7,tab11, put11Event);
		Arc getArc11 = new Arc( tab11,s0ret12,get11Event);



	}

//This option is kept, if you want to use station 20 in the production instead
	/*	if(sv[20])
		{



			if(viatable[0])
			{
			put1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1L");
			currAlphabet.addEvent(put1Event);
			get1Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T1R");
			currAlphabet.addEvent(get1Event);
			tab1 = currShoe.createAndAddUniqueState("q");
		    g2 = currShoe.createAndAddUniqueState("q");


			putR1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1R");
			currAlphabet.addEvent(putR1Event);

			getL1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1L");
			currAlphabet.addEvent(getL1Event);

			s0ret3 =currShoe.createAndAddUniqueState("q");



			Arc	ag2p = new Arc( s0ret3,s0ret2 , getL1Event);
			s0ret4 =currShoe.createAndAddUniqueState("q");
			Arc ag3p = new Arc( s0ret4,s0ret3 , putR1Event);

		if(sv[5] || sv[6] || sv[6]|| sv[8] || sv[9] || sv[10] || sv[11])
		{
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc putArc1 = new Arc( g1, tab1 , put1Event);
		}
			viatable[0]=false;
			}
			if(viatable[1])
			{
			put2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2L");
			currAlphabet.addEvent(put2Event);
			get2Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T2R");
			currAlphabet.addEvent(get2Event);




			putR2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2R");
			currAlphabet.addEvent(putR2Event);
			getL2Event =new LabeledEvent("Shoe_"+shoeNr+"get_T2L");
			currAlphabet.addEvent(getL2Event);
			s0ret5 =currShoe.createAndAddUniqueState("q");
			s0ret6 =currShoe.createAndAddUniqueState("q");

			Arc ag3p = new Arc( s0ret6,s0ret5 , putR2Event);
			Arc ag2p = new Arc( s0ret5,s0ret4 , getL2Event);


			if(sv[9] || sv[10] || sv[11])
			{

				tab2 = currShoe.createAndAddUniqueState("q");
				g3 = currShoe.createAndAddUniqueState("q");
				Arc ag3 = new Arc( tab2, g3 , get2Event);
				Arc putArc2 = new Arc( g2 ,tab2, put2Event);
			}
			viatable[1]=false;
			}

			if(viatable[2])
			{
			put3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3L");
			currAlphabet.addEvent(put3Event);
			get3Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T3R");
			currAlphabet.addEvent(get3Event);
			tab3 = currShoe.createAndAddUniqueState("q");
			g4 = currShoe.createAndAddUniqueState("q");



			putR3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3R");
			currAlphabet.addEvent(putR3Event);
			getL3Event =new LabeledEvent("Shoe_"+shoeNr+"get_T3L");
			currAlphabet.addEvent(getL3Event);

			s0ret7 =currShoe.createAndAddUniqueState("q");
			s0ret8 =currShoe.createAndAddUniqueState("q");


			Arc ag5p = new Arc( s0ret8,s0ret7 , putR3Event);
			Arc ag4p = new Arc( s0ret7,s0ret6 , getL3Event);



			Arc ag4 = new Arc( tab3, g4 , get3Event);
			Arc putArc3;
			if(sv[9] || sv[10] || sv[11])
				 putArc3 = new Arc( g3 ,tab3, put3Event);

			viatable[2]=false;
			}

			if(viatable[3])
			{
			put4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4L");
			currAlphabet.addEvent(put4Event);
			get4Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T4R");
			currAlphabet.addEvent(get4Event);

			tab4 = currShoe.createAndAddUniqueState("q");
			g5 = currShoe.createAndAddUniqueState("q");

			putR4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4R");
			currAlphabet.addEvent(putR4Event);
			getL4Event =new LabeledEvent("Shoe_"+shoeNr+"get_T4L");
			currAlphabet.addEvent(getL4Event);

			s0ret9 =currShoe.createAndAddUniqueState("q");
			s0ret10 =currShoe.createAndAddUniqueState("q");

			Arc ag7p = new Arc( s0ret10,s0ret9 ,putR4Event);
			Arc ag6p = new Arc( s0ret9,s0ret8 ,getL4Event);



			Arc ag5 = new Arc( tab4, g5 , get4Event);
			//Arc ag5p = new Arc( g5,tab4 , put4Event);
			Arc putArc4 = new Arc( g4 ,tab4, put4Event);
			//	Arc getArc4 = new Arc( tab4 ,g4, get4Event);
			viatable[3]=false;
			}




			//State ret10 = currShoe.createAndAddUniqueState("q");

			putR10Event= new LabeledEvent("Shoe_"+shoeNr+"put_T10R");
			currAlphabet.addEvent(putR10Event);

			getL10Event= new LabeledEvent("Shoe_"+shoeNr+"get_T10L");
			currAlphabet.addEvent(getL10Event);


			//Arc getstat20 = new Arc(if10 ,stat20, gt10s20);
			//Arc putstat20 = new Arc( stat20,ret10 ,pt10s20);

			if(!(sv[16]||sv[17]||sv[18]||sv[19]))
			{

				LabeledEvent pt10s20 =new LabeledEvent("Shoe_"+shoeNr+"put_T10_S20");
				LabeledEvent gt10s20 =new LabeledEvent("Shoe_"+shoeNr+"get_T10_S20");
				currAlphabet.addEvent(pt10s20);
				currAlphabet.addEvent(gt10s20);

				State stat20 = currShoe.createAndAddUniqueState("q");

				s0ret13 =currShoe.createAndAddUniqueState("q");

				Arc bla1 = new Arc( s0ret13,stat20, gt10s20);

				Arc bla2 = new Arc( stat20,s0ret13,pt10s20);


				Arc putArc10 = new Arc( g5,s0ret13,put10Event);

				Arc ag6;

				if(!(sv[21]||sv[22]||sv[22]))
					{
					ag6=new Arc( s0ret13,s0ret10,getL10Event);
					}
				else
				 	ag6= new Arc( s0ret13,s0ret14,getL10Event);
			}



			else
			{
			tab10 =currShoe.createAndAddUniqueState("q");
			State if10 = currShoe.createAndAddUniqueState("q");
			Arc putArc10 = new Arc( g5,if10,put10Event);
		//	Arc getArc10 = new Arc( ret10,tab10,get10Event);
			Arc getArc10 = new Arc( if10,tab10,get10Event);
			}
		}*/

 //visit table 12?
	if(sv[16] || sv[17] || sv[18] || sv[19] )
		{


		if(viatable[0])
		{
			put1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1L");
			currAlphabet.addEvent(put1Event);
			get1Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T1R");
			currAlphabet.addEvent(get1Event);
			putR1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1R");
			currAlphabet.addEvent(putR1Event);
			getL1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1L");
			currAlphabet.addEvent(getL1Event);
			s0ret3 =currShoe.createAndAddUniqueState("q");
			Arc ag2p = new Arc( s0ret3,s0ret2 , getL1Event);

			s0ret4 =currShoe.createAndAddUniqueState("q");
			Arc ag3p = new Arc( s0ret4,s0ret3,putR1Event);



		if(sv[5] || sv[6] || sv[6]|| sv[8] || sv[9] || sv[10] || sv[11])
		{

			tab1 = currShoe.createAndAddUniqueState("q");
			g2 = currShoe.createAndAddUniqueState("q");
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc putArc1 = new Arc( g1, tab1 , put1Event);

		}
			viatable[0]=false;
		}
		if(viatable[1])
			{
			put2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2L");
			currAlphabet.addEvent(put2Event);
			get2Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T2R");
			currAlphabet.addEvent(get2Event);

			putR2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2R");
			currAlphabet.addEvent(putR2Event);
			getL2Event =new LabeledEvent("Shoe_"+shoeNr+"get_T2L");
			currAlphabet.addEvent(getL2Event);
			s0ret5 =currShoe.createAndAddUniqueState("q");
			s0ret6 =currShoe.createAndAddUniqueState("q");
			Arc ag3p = new Arc( s0ret6,s0ret5 , putR2Event);
			Arc ag2p = new Arc( s0ret5,s0ret4 , getL2Event);



			if(sv[9] || sv[10] || sv[11])
			{

			tab2 = currShoe.createAndAddUniqueState("q");
			g3 = currShoe.createAndAddUniqueState("q");
			Arc ag3 = new Arc( tab2, g3 , get2Event);
			Arc putArc2 = new Arc( g2 ,tab2, put2Event);
						}
			viatable[1]=false;
			}
			if(viatable[2])
			{

			tab3 = currShoe.createAndAddUniqueState("q");
			put3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3L");
			currAlphabet.addEvent(put3Event);
			get3Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T3R");
			currAlphabet.addEvent(get3Event);
			g4 = currShoe.createAndAddUniqueState("q");

			putR3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3R");
			currAlphabet.addEvent(putR3Event);
			getL3Event =new LabeledEvent("Shoe_"+shoeNr+"get_T3L");
			currAlphabet.addEvent(getL3Event);
			s0ret7 =currShoe.createAndAddUniqueState("q");
			s0ret8 =currShoe.createAndAddUniqueState("q");
			Arc ag5p = new Arc( s0ret8,s0ret7 , putR3Event);
			Arc ag4p = new Arc( s0ret7,s0ret6 , getL3Event);

			Arc ag4 = new Arc( tab3, g4 , get3Event);

			Arc putArc3;
			if(sv[9] || sv[10] || sv[11])
			{

				 putArc3 = new Arc( g3 ,tab3, put3Event);

			}

			viatable[2]=false;
			}
			if(viatable[3])
			{
			put4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4L");
			currAlphabet.addEvent(put4Event);
			get4Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T4R");
			currAlphabet.addEvent(get4Event);
			tab4 = currShoe.createAndAddUniqueState("q");
			g5 = currShoe.createAndAddUniqueState("q");

			putR4Event = new LabeledEvent("Shoe_"+shoeNr+"put_T4R");
			currAlphabet.addEvent(putR4Event);
			getL4Event =new LabeledEvent("Shoe_"+shoeNr+"get_T4L");
			currAlphabet.addEvent(getL4Event);
			s0ret9 =currShoe.createAndAddUniqueState("q");
			s0ret10 =currShoe.createAndAddUniqueState("q");

			Arc ag7p = new Arc( s0ret10,s0ret9 ,putR4Event);
			Arc ag6p = new Arc( s0ret9,s0ret8 ,getL4Event);


			Arc ag5 = new Arc( tab4, g5 , get4Event);

			Arc putArc4 = new Arc( g4 ,tab4, put4Event);


			viatable[3]=false;
			}






			if(true)
			{
			   putR10Event= new LabeledEvent("Shoe_"+shoeNr+"put_T10R");
			   currAlphabet.addEvent(putR10Event);

			   get10Event= new LabeledEvent("Shoe_"+shoeNr+"get_T10R");
			   currAlphabet.addEvent(get10Event);

			   tab10 =currShoe.createAndAddUniqueState("q");
				State not10 =currShoe.createAndAddUniqueState("q");
				Arc putArc10 = new Arc( g5,not10,put10Event);
				Arc getArc10 = new Arc( not10,tab10, get10Event);

			}
			tab12 =currShoe.createAndAddUniqueState("q");

		if(sv[16])
				{
				LabeledEvent pt12s16 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S16");
				LabeledEvent gt12s16 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S16");
				currAlphabet.addEvent(pt12s16);
				currAlphabet.addEvent(gt12s16);
				State stat16 = currShoe.createAndAddUniqueState("q");

				Arc putstat16 = new Arc(tab12 ,stat16, gt12s16);
				Arc getstat16 = new Arc( stat16,tab12 , pt12s16);


			if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");
				State Er4=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat16,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt12s16);
				Arc AEr3=new Arc(Er3,Er4,get12Event);
				Arc AEr4=new Arc(Er4,E10,putR10Event);
				}



				}

		if(sv[17])
				{

				LabeledEvent pt12s17 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S17");
				LabeledEvent gt12s17 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S17");

				currAlphabet.addEvent(pt12s17);
				currAlphabet.addEvent(gt12s17);

				State stat17 = currShoe.createAndAddUniqueState("q");
				Arc putstat17 = new Arc(tab12 ,stat17, gt12s17);
				Arc getstat17 = new Arc( stat17,tab12 ,pt12s17);

			if(allowerror)
				{

				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");
				State Er4=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat17,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt12s17);
				Arc AEr3=new Arc(Er3,Er4,get12Event);
				Arc AEr4=new Arc(Er4,E10,putR10Event);
				}
				}


		if(sv[18])
				{
				LabeledEvent pt12s18 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S18");
				LabeledEvent gt12s18 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S18");
				currAlphabet.addEvent(pt12s18);
				currAlphabet.addEvent(gt12s18);

				State stat18 = currShoe.createAndAddUniqueState("q");
				Arc putstat18 = new Arc(tab12 ,stat18, gt12s18);
				Arc getstat18 = new Arc( stat18,tab12 ,pt12s18);


			if(allowerror)
			{

				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");
				State Er4=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat18,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt12s18);
				Arc AEr3=new Arc(Er3,Er4,get12Event);
				Arc AEr4=new Arc(Er4,E10,putR10Event);
				}
				}

		if(sv[19])
				{
				LabeledEvent pt12s19 =new LabeledEvent("Shoe_"+shoeNr+"put_T12_S19");
				LabeledEvent gt12s19 =new LabeledEvent("Shoe_"+shoeNr+"get_T12_S19");
				currAlphabet.addEvent(pt12s19);
				currAlphabet.addEvent(gt12s19);
				State stat19 = currShoe.createAndAddUniqueState("q");
				Arc putstat19 = new Arc(tab12 ,stat19, gt12s19);
				Arc getstat19 = new Arc( stat19,tab12 ,pt12s19);

				if(allowerror)
				{

				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");
				State Er4=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat19,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt12s19);
				Arc AEr3=new Arc(Er3,Er4,get12Event);
				Arc AEr4=new Arc(Er4,E10,putR10Event);
				}
				}

			g6 = currShoe.createAndAddUniqueState("q");


			s0ret13 =currShoe.createAndAddUniqueState("q");



			Arc ag6p = new Arc( g6,s0ret13, putR10Event);



			/*if(sv[20])
			{
				LabeledEvent pt10s20 =new LabeledEvent("Shoe_"+shoeNr+"put_T10_S20");
				LabeledEvent gt10s20 =new LabeledEvent("Shoe_"+shoeNr+"get_T10_S20");
				currAlphabet.addEvent(pt10s20);
				currAlphabet.addEvent(gt10s20);
				State stat20 = currShoe.createAndAddUniqueState("q");
				//State s0ret15=currShoe.createAndAddUniqueState("q");

				Arc bla1 = new Arc( s0ret13,stat20, gt10s20);
				//Arc bla2 = new Arc( stat20,s0ret15,pt10s20);
				Arc bla2 = new Arc( stat20,s0ret13,pt10s20);
				//Arc ag6 = new Arc( s0ret15,s0ret14,getL10Event);
				Arc ag6;

				if(!(sv[21]||sv[22]||sv[22]))
					{

					ag6=new Arc( s0ret13,s0ret10,getL10Event);
				}
				else
				 	ag6= new Arc( s0ret13,s0ret14,getL10Event);
			}*/

		//	else
		//	{

			//if table 11 shall be visited after 12 or else return to table0
			Arc ag6;
			if(!(sv[21]||sv[22]||sv[22]))
				{

				ag6=new Arc( s0ret13,s0ret10,getL10Event);
				}
			else
				 ag6 = new Arc( s0ret13,s0ret14,getL10Event);

		//	}



			Arc putArc12 = new Arc( tab10,tab12,put12Event);
			Arc getArc12 = new Arc( tab12,g6, get12Event);






	}

		//visit table 9
		if(sv[12] || sv[13] || sv[14] || sv[15])
			{


			if(viatable[0])
			{
				put1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1L");
				currAlphabet.addEvent(put1Event);
				get1Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T1R");
				currAlphabet.addEvent(get1Event);

				putR1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1R");
				currAlphabet.addEvent(putR1Event);
				getL1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1L");
				currAlphabet.addEvent(getL1Event);
				s0ret3 =currShoe.createAndAddUniqueState("q");
				Arc ag2p = new Arc( s0ret3,s0ret2 , getL1Event);

				s0ret4 =currShoe.createAndAddUniqueState("q");

				Arc ag3p = new Arc( s0ret4,s0ret3 , putR1Event);

			if(sv[5] || sv[6] || sv[6]|| sv[8] || sv[9] || sv[10] || sv[11])
			{

				tab1 = currShoe.createAndAddUniqueState("q");
				g2 = currShoe.createAndAddUniqueState("q");
				Arc ag2 = new Arc( tab1, g2 , get1Event);
				Arc putArc1 = new Arc( g1, tab1 , put1Event);
			}
				viatable[0]=false;

			}


			if(viatable[1])
			{
				put2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2L");
				currAlphabet.addEvent(put2Event);
				get2Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T2R");
				currAlphabet.addEvent(get2Event);

				putR2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2R");
				currAlphabet.addEvent(putR2Event);
				getL2Event =new LabeledEvent("Shoe_"+shoeNr+"get_T2L");

				currAlphabet.addEvent(getL2Event);

				s0ret5 =currShoe.createAndAddUniqueState("q");
				s0ret6 =currShoe.createAndAddUniqueState("q");

				Arc ag3p = new Arc( s0ret6,s0ret5 , putR2Event);
				Arc ag2p = new Arc( s0ret5,s0ret4 , getL2Event);

				if(sv[9] || sv[10] || sv[11])
				{
					tab2 = currShoe.createAndAddUniqueState("q");
					g3 = currShoe.createAndAddUniqueState("q");
					Arc ag3 = new Arc( tab2, g3 , get2Event);
					Arc putArc2 = new Arc( g2 ,tab2, put2Event);

				}
				viatable[1]=false;

			}


			if(viatable[2])
			{
				put3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3L");
				currAlphabet.addEvent(put3Event);
				get3Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T3R");
				currAlphabet.addEvent(get3Event);

				tab3 = currShoe.createAndAddUniqueState("q");
				g4 = currShoe.createAndAddUniqueState("q");

				putR3Event = new LabeledEvent("Shoe_"+shoeNr+"put_T3R");
				currAlphabet.addEvent(putR3Event);
				getL3Event =new LabeledEvent("Shoe_"+shoeNr+"get_T3L");
				currAlphabet.addEvent(getL3Event);
				s0ret7 =currShoe.createAndAddUniqueState("q");
				s0ret8 =currShoe.createAndAddUniqueState("q");
				Arc ag5p = new Arc( s0ret8,s0ret7 , putR3Event);
				Arc ag4p = new Arc( s0ret7,s0ret6 , getL3Event);

				Arc ag4 = new Arc( tab3, g4 , get3Event);

				Arc putArc3;
				if(sv[9] || sv[10] || sv[11])
					putArc3 = new Arc( g3 ,tab3, put3Event);

				viatable[2]=false;

			}


			tab9 =currShoe.createAndAddUniqueState("q");

			if(sv[12])
				{
				LabeledEvent pt9s12 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S12");
				LabeledEvent gt9s12 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S12");
				currAlphabet.addEvent(pt9s12);
				currAlphabet.addEvent(gt9s12);
				State stat12 = currShoe.createAndAddUniqueState("q");
				Arc putstat12 = new Arc(tab9 ,stat12, gt9s12);
				Arc getstat12 = new Arc( stat12,tab9 ,pt9s12);

				if(allowerror)
				{

				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat12,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt9s12);
				Arc AEr3=new Arc(Er3,E7,get9Event);
				}

				}


			if(sv[13])
				{
				LabeledEvent pt9s13 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S13");
				LabeledEvent gt9s13 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S13");
				currAlphabet.addEvent(pt9s13);
				currAlphabet.addEvent(gt9s13);
				State stat13 = currShoe.createAndAddUniqueState("q");
				Arc putstat13 = new Arc(tab9 ,stat13, gt9s13);
				Arc getstat13 = new Arc( stat13,tab9 ,pt9s13);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat13,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt9s13);
				Arc AEr3=new Arc(Er3,E7,get9Event);
				}

				}


			if(sv[14])
				{

				LabeledEvent pt9s14 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S14");
				LabeledEvent gt9s14 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S14");
				currAlphabet.addEvent(pt9s14);
				currAlphabet.addEvent(gt9s14);
				State stat14 = currShoe.createAndAddUniqueState("q");

				Arc putstat14 = new Arc(tab9 ,stat14, gt9s14);
				Arc getstat14 = new Arc( stat14,tab9 ,pt9s14);


				if(allowerror)
				{

				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat14,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt9s14);
				Arc AEr3=new Arc(Er3,E7,get9Event);
				}

				}

			if(sv[15])
				{
				LabeledEvent pt9s15 =new LabeledEvent("Shoe_"+shoeNr+"put_T9_S15");
				LabeledEvent gt9s15 =new LabeledEvent("Shoe_"+shoeNr+"get_T9_S15");
				currAlphabet.addEvent(pt9s15);
				currAlphabet.addEvent(gt9s15);
				State stat15 = currShoe.createAndAddUniqueState("q");
				Arc putstat15 = new Arc(tab9 ,stat15, gt9s15);
				Arc getstat15 = new Arc( stat15,tab9 ,pt9s15);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat15,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt9s15);
				Arc AEr3=new Arc(Er3,E7,get9Event);
				}
				}



		Arc putArc9 = new Arc( g4,tab9, put9Event);


		//if table 9 is the last table to be visted
			Arc getArc9;

			if(!(sv[16]||sv[17]||sv[18]||sv[19]||sv[20]||sv[21]||sv[22]||sv[23]))
				 getArc9 = new Arc( tab9,s0ret8,get9Event);

			else
				 getArc9 = new Arc( tab9,g4, get9Event);


			}



//visit table 8
		if(sv[9] || sv[10] || sv[11])
			{

			if(viatable[0])
			{
			put1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1L");
			currAlphabet.addEvent(put1Event);
			get1Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T1R");
			currAlphabet.addEvent(get1Event);
			tab1 = currShoe.createAndAddUniqueState("q");
			g2 = currShoe.createAndAddUniqueState("q");
			putR1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1R");
			currAlphabet.addEvent(putR1Event);
			getL1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1L");
			currAlphabet.addEvent(getL1Event);

			s0ret3 =currShoe.createAndAddUniqueState("q");
			Arc ag2p = new Arc( s0ret3,s0ret2 , getL1Event);

			s0ret4 =currShoe.createAndAddUniqueState("q");
			Arc ag3p = new Arc( s0ret4,s0ret3 , putR1Event);


		if(sv[5] || sv[6] || sv[6]|| sv[8] || sv[9] || sv[10] || sv[11])
		{
			Arc ag2 = new Arc( tab1, g2 , get1Event);
			Arc putArc1 = new Arc( g1 ,tab1, put1Event);
		}
			viatable[0]=false;
			}
			if(viatable[1])
			{
			put2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2L");
			currAlphabet.addEvent(put2Event);
			get2Event =	new LabeledEvent("Shoe_"+shoeNr+"get_T2R");
			currAlphabet.addEvent(get2Event);
			tab2 = currShoe.createAndAddUniqueState("q");
			g3 = currShoe.createAndAddUniqueState("q");

			putR2Event = new LabeledEvent("Shoe_"+shoeNr+"put_T2R");
			currAlphabet.addEvent(putR2Event);
			getL2Event =new LabeledEvent("Shoe_"+shoeNr+"get_T2L");
			currAlphabet.addEvent(getL2Event);
			s0ret5 =currShoe.createAndAddUniqueState("q");
			s0ret6 =currShoe.createAndAddUniqueState("q");
			Arc ag3p = new Arc( s0ret6,s0ret5 , putR2Event);
			Arc ag2p = new Arc( s0ret5,s0ret4 , getL2Event);


			Arc ag3 = new Arc( tab2, g3 , get2Event);
			Arc putArc2 = new Arc( g2 ,tab2, put2Event);
			viatable[1]=false;
			}
			tab8 =currShoe.createAndAddUniqueState("q");

			if(sv[9])
				{
				LabeledEvent pt8s9 =new LabeledEvent("Shoe_"+shoeNr+"put_T8_S9");
				LabeledEvent gt8s9 =new LabeledEvent("Shoe_"+shoeNr+"get_T8_S9");
				currAlphabet.addEvent(pt8s9);
				currAlphabet.addEvent(gt8s9);
				State stat9= currShoe.createAndAddUniqueState("q");
				Arc putstat9 = new Arc(tab8 ,stat9, gt8s9);
				Arc getstat9 = new Arc( stat9,tab8 ,pt8s9);



				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat9,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt8s9);
				Arc AEr3=new Arc(Er3,E5,get8Event);
				}

				}

			if(sv[10])
				{
				LabeledEvent pt8s10 =new LabeledEvent("Shoe_"+shoeNr+"put_T8_S10");
				LabeledEvent gt8s10 =new LabeledEvent("Shoe_"+shoeNr+"get_T8_S10");
				currAlphabet.addEvent(pt8s10);
				currAlphabet.addEvent(gt8s10);
				State stat10 = currShoe.createAndAddUniqueState("q");
				Arc putstat10 = new Arc(tab8 ,stat10, gt8s10);
				Arc getstat10 = new Arc( stat10,tab8 ,pt8s10);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat10,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt8s10);
				Arc AEr3=new Arc(Er3,E5,get8Event);
				}

				}

			if(sv[11])
				{
				LabeledEvent pt8s11 =new LabeledEvent("Shoe_"+shoeNr+"put_T8_S11");
				LabeledEvent gt8s11 =new LabeledEvent("Shoe_"+shoeNr+"get_T8_S11");
				currAlphabet.addEvent(pt8s11);
				currAlphabet.addEvent(gt8s11);
				State stat11 = currShoe.createAndAddUniqueState("q");
				Arc putstat11 = new Arc(tab8 ,stat11,gt8s11);
				Arc getstat11 = new Arc( stat11,tab8 ,pt8s11);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat11,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt8s11);
				Arc AEr3=new Arc(Er3,E5,get8Event);
				}

				}


				Arc putArc8 = new Arc( g3,tab8, put8Event);
				Arc getArc8 = new Arc( tab8,g3, get8Event);
			}

		//visit table7
		if(sv[5] || sv[6] || sv[7] || sv[8])
			{


			if(viatable[0])
			{
				put1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1L");
				currAlphabet.addEvent(put1Event);
				get1Event =createGetEvent(1);
				tab1 = currShoe.createAndAddUniqueState("q");
				g2 = currShoe.createAndAddUniqueState("q");

				putR1Event = new LabeledEvent("Shoe_"+shoeNr+"put_T1R");
				currAlphabet.addEvent(putR1Event);
				getL1Event =new LabeledEvent("Shoe_"+shoeNr+"get_T1L");
				currAlphabet.addEvent(getL1Event);
				s0ret3 =currShoe.createAndAddUniqueState("q");
				Arc ag2p = new Arc( s0ret3,s0ret2 , getL1Event);
				s0ret4 =currShoe.createAndAddUniqueState("q");
				Arc ag3p = new Arc( s0ret4,s0ret3 , putR1Event);

			if(sv[5] || sv[6] || sv[6]|| sv[8] || sv[9] || sv[10] || sv[11])
			{
				Arc ag2 	= new Arc( tab1, g2 , get1Event);
				Arc putArc1 = new Arc( g1 ,tab1, put1Event);

			}
				viatable[0]=false;
			}
				tab7 =currShoe.createAndAddUniqueState("q");

			if(sv[5])
				{
				LabeledEvent pt7s5 =new LabeledEvent("Shoe_"+shoeNr+"put_T7_S5");
				LabeledEvent gt7s5 =new LabeledEvent("Shoe_"+shoeNr+"get_T7_S5");
				currAlphabet.addEvent(pt7s5);
				currAlphabet.addEvent(gt7s5);
				State stat5 = currShoe.createAndAddUniqueState("q");
				Arc putstat5 = new Arc(tab7 ,stat5, gt7s5);
				Arc getstat5 = new Arc( stat5,tab7 ,pt7s5);

				if(allowerror)
				{

				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat5,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt7s5);
				Arc AEr3=new Arc(Er3,E3,get7Event);
				}

				}
			if(sv[6])
				{
				LabeledEvent pt7s6 =new LabeledEvent("Shoe_"+shoeNr+"put_T7_S6");
				LabeledEvent gt7s6 =new LabeledEvent("Shoe_"+shoeNr+"get_T7_S6");
				currAlphabet.addEvent(pt7s6);
				currAlphabet.addEvent(gt7s6);
				State stat6 = currShoe.createAndAddUniqueState("q");
				Arc putstat6 = new Arc(tab7 ,stat6, gt7s6);
				Arc getstat6 = new Arc( stat6,tab7 ,pt7s6);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat6,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt7s6);
				Arc AEr3=new Arc(Er3,E3,get7Event);
				}

				}

			if(sv[7])
				{
				LabeledEvent pt7s7=new LabeledEvent("Shoe_"+shoeNr+"put_T7_S7");
				LabeledEvent gt7s7=new LabeledEvent("Shoe_"+shoeNr+"get_T7_S7");
				currAlphabet.addEvent(pt7s7);
				currAlphabet.addEvent(gt7s7);
				State stat7= currShoe.createAndAddUniqueState("q");
				Arc putstat7 = new Arc(tab7 ,stat7, gt7s7);
				Arc getstat7 = new Arc( stat7, tab7 ,pt7s7);


				if(allowerror)
				{
				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat7,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt7s7);
				Arc AEr3=new Arc(Er3,E3,get7Event);

				}
				}

			if(sv[8])
				{
				LabeledEvent pt7s8 =new LabeledEvent("Shoe_"+shoeNr+"put_T7_S8");
				LabeledEvent gt7s8 =new LabeledEvent("Shoe_"+shoeNr+"get_T7_S8");
				currAlphabet.addEvent(pt7s8);
				currAlphabet.addEvent(gt7s8);
				State stat8 = currShoe.createAndAddUniqueState("q");
				Arc putstat8 = new Arc(tab7 ,stat8, gt7s8);
				Arc getstat8 = new Arc( stat8,tab7 ,pt7s8);

				if(allowerror)
				{

				State Er1=currShoe.createAndAddUniqueState("q");
				State Er3=currShoe.createAndAddUniqueState("q");

				Arc AEr1=new Arc(stat8,Er1,Error);
				Arc AEr2=new Arc(Er1,Er3,pt7s8);
				Arc AEr3=new Arc(Er3,E3,get7Event);

				}
				}


			i1=	currShoe.createAndAddUniqueState("q");
			Arc putArc7 = new Arc( g2,tab7, put7Event);
			Arc getArc7 = new Arc( tab7,i1, get7Event);
			}


			if(sv[0]||sv[1]||sv[2]||sv[3]||sv[4])
			{
				h2=	currShoe.createAndAddUniqueState("q");
				h3= currShoe.createAndAddUniqueState("q");
				h4= currShoe.createAndAddUniqueState("q");
				h5= currShoe.createAndAddUniqueState("q");   //this part handles the paralell structure

				Arc aft6_1 = new Arc( h1,h2, put1Event);
				Arc aft6_2 = new Arc( h2,h3, get1Event);
				Arc aft6_3 = new Arc( h3,h4, put2Event);
				Arc aft6_4 = new Arc( h4,h5, get2Event);
				Arc aft6_5 = new Arc( h5,tab3, put3Event);
			}

			if(sv[5]||sv[6]||sv[7]||sv[8])
			{
				i2=	currShoe.createAndAddUniqueState("q");
				i3= currShoe.createAndAddUniqueState("q");
				Arc aft7_1 = new Arc( i1,i2, put2Event);
				Arc aft7_2 = new Arc( i2,i3, get2Event);
				Arc aft7_3 = new Arc( i3,tab3, put3Event);
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