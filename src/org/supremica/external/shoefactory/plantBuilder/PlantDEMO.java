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

public class PlantDEMO
{
	State [] filledOneState = new  State [100];
	State InitialState, filledState;
	protected Project thePlant = null;
	private int[] nrOfStationsPerTable = {0,1,1};

	private State [][] table_SMatr =new State [nrOfStationsPerTable.length][3];
	private State [] [] station_SMatr =new State [2][2];

	private State [] IOSTate =new State [8];

	private int s_SMindex=0, index=0,IOindex=0,IOAlfindex=0,Iindex=0;

	private Automaton station =null;

	private Alphabet  [] currAlphabet =new Alphabet [nrOfStationsPerTable.length] ;
	private Alphabet  [] stationAlphabet = new Alphabet [2];
	private Alphabet [] IOAlphabet =new Alphabet[3];
	private int nrOfSlots=1;
	private int sAindex =0;

	public PlantDEMO()
	{
		thePlant = new Project();

		createTable(0,0);
		createTable(1,1);
		createTable(2,1);
	}

	public void createTable(int table_id,int table_type)
	{
		int X = nrOfSlots + table_type * nrOfSlots;

		Automaton currTable = new Automaton("Table"+table_id);
	  	currTable.setType(AutomatonType.Plant);
	   	currAlphabet [table_id] = currTable.getAlphabet();

		InitialState = currTable.createAndAddUniqueState("q");
		filledState = currTable.createAndAddUniqueState("q");

		InitialState.setInitial(true);
		InitialState.setAccepting(true);
		currTable.setInitialState(InitialState);

		// Create events
		if(table_id==0)
		{
			LabeledEvent putEvent1L = new LabeledEvent("Shoe_1put_T"+table_id+"L");
			LabeledEvent getEvent1L = new LabeledEvent("Shoe_1get_T"+table_id+"L");
			LabeledEvent putEvent2L = new LabeledEvent("Shoe_2put_T"+table_id+"L");
			LabeledEvent getEvent2L = new LabeledEvent("Shoe_2get_T"+table_id+"L");
			//LabeledEvent putEvent3L = new LabeledEvent("Shoe_3put_T"+table_id+"L");
			//LabeledEvent getEvent3L = new LabeledEvent("Shoe_3get_T"+table_id+"L");
			LabeledEvent putEvent1R = new LabeledEvent("Shoe_1put_T"+table_id+"R");
			LabeledEvent getEvent1R = new LabeledEvent("Shoe_1get_T"+table_id+"R");
			LabeledEvent putEvent2R = new LabeledEvent("Shoe_2put_T"+table_id+"R");
			LabeledEvent getEvent2R = new LabeledEvent("Shoe_2get_T"+table_id+"R");
			//LabeledEvent putEvent3R = new LabeledEvent("Shoe_3put_T"+table_id+"R");
			//LabeledEvent getEvent3R = new LabeledEvent("Shoe_3get_T"+table_id+"R");

			currAlphabet[table_id].addEvent(putEvent1L);
			currAlphabet[table_id].addEvent(getEvent1L);
			currAlphabet[table_id].addEvent(putEvent2L);
			currAlphabet[table_id].addEvent(getEvent2L);
			//currAlphabet[table_id].addEvent(putEvent3L);
			//currAlphabet[table_id].addEvent(getEvent3L);
			currAlphabet[table_id].addEvent(putEvent1R);
			currAlphabet[table_id].addEvent(getEvent1R);
			currAlphabet[table_id].addEvent(putEvent2R);
			currAlphabet[table_id].addEvent(getEvent2R);
			//currAlphabet[table_id].addEvent(putEvent3R);
			//currAlphabet[table_id].addEvent(getEvent3R);

			Arc putArc1L  = new Arc(InitialState, filledState , putEvent1L);
		    Arc getArc1L  = new Arc(filledState ,InitialState , getEvent1L);
			Arc putArc2L  = new Arc(InitialState, filledState , putEvent2L);
		    Arc getArc2L  = new Arc(filledState ,InitialState , getEvent2L);
		   // Arc putArc3L  = new Arc(InitialState, filledState , putEvent3L);
	    	//Arc getArc3L  = new Arc(filledState ,InitialState , getEvent3L);
			Arc putArc1R  = new Arc(InitialState, filledState , putEvent1R);
		    Arc getArc1R  = new Arc(filledState ,InitialState , getEvent1R);
			Arc putArc2R  = new Arc(InitialState, filledState , putEvent2R);
		    Arc getArc2R  = new Arc(filledState ,InitialState , getEvent2R);
		   // Arc putArc3R  = new Arc(InitialState, filledState , putEvent3R);
	    	//Arc getArc3R  = new Arc(filledState ,InitialState , getEvent3R);
		}
		else
		{
			LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T"+table_id);
			LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T"+table_id);
			LabeledEvent putEvent2 = new LabeledEvent("Shoe_2put_T"+table_id);
			LabeledEvent getEvent2 = new LabeledEvent("Shoe_2get_T"+table_id);
			//LabeledEvent putEvent3 = new LabeledEvent("Shoe_3put_T"+table_id);
			//LabeledEvent getEvent3 = new LabeledEvent("Shoe_3get_T"+table_id);

			currAlphabet[table_id].addEvent(putEvent1);
			currAlphabet[table_id].addEvent(getEvent1);
			currAlphabet[table_id].addEvent(putEvent2);
			currAlphabet[table_id].addEvent(getEvent2);
			//currAlphabet[table_id].addEvent(putEvent3);
			//currAlphabet[table_id].addEvent(getEvent3);

			Arc putArc1  = new Arc(InitialState, filledState , putEvent1);
		    Arc getArc1  = new Arc(filledState ,InitialState , getEvent1);
			Arc putArc2  = new Arc(InitialState, filledState , putEvent2);
		    Arc getArc2  = new Arc(filledState ,InitialState , getEvent2);
		   // Arc putArc3  = new Arc(InitialState, filledState , putEvent3);
	    	//Arc getArc3  = new Arc(filledState ,InitialState , getEvent3);
		}
		// Create states

		createIO(table_id);

		if(table_type ==1)
		{
			LabeledEvent[] CreatedEvents=createStation(table_id, table_id-1, currAlphabet[table_id]);

			Arc	putArc4  = new Arc(InitialState, filledState , CreatedEvents[0]);
			Arc getArc4  = new Arc(filledState,InitialState , CreatedEvents[1]);
			Arc	putArc5  = new Arc(InitialState, filledState , CreatedEvents[2]);
			Arc getArc5  = new Arc(filledState,InitialState , CreatedEvents[3]);
		//	Arc	putArc6  = new Arc(InitialState, filledState , CreatedEvents[4]);
		//	Arc getArc6  = new Arc(filledState,InitialState , CreatedEvents[5]);

		}
		thePlant.addAutomaton(currTable);
	}

	public LabeledEvent[] createStation(int table_id, int station_id, Alphabet tableAlphabet)
	{
		LabeledEvent[] returnedEvents = new LabeledEvent[6] ;
		station = new Automaton("Station_"+table_id+"_"+station_id);
		station.setType(AutomatonType.Plant);
		stationAlphabet[sAindex] = station.getAlphabet();

		LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T"+table_id+"_S"+station_id);
		LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T"+table_id+"_S"+station_id);
		stationAlphabet[sAindex].addEvent(putEvent1);
		stationAlphabet[sAindex].addEvent(getEvent1);
		tableAlphabet.addEvent(putEvent1);
		tableAlphabet.addEvent(getEvent1);

		LabeledEvent putEvent2 = new LabeledEvent("Shoe_2put_T"+table_id+"_S"+station_id);
		LabeledEvent getEvent2 = new LabeledEvent("Shoe_2get_T"+table_id+"_S"+station_id);
		stationAlphabet[sAindex].addEvent(putEvent2);
		stationAlphabet[sAindex].addEvent(getEvent2);
		tableAlphabet.addEvent(putEvent2);
		tableAlphabet.addEvent(getEvent2);
		//LabeledEvent putEvent3 = new LabeledEvent("Shoe_3put_T"+table_id+"_S"+station_id);
		//LabeledEvent getEvent3 = new LabeledEvent("Shoe_3get_T"+table_id+"_S"+station_id);
		//stationAlphabet[sAindex].addEvent(putEvent3);
		//stationAlphabet[sAindex].addEvent(getEvent3);
		//tableAlphabet.addEvent(putEvent3);
		//tableAlphabet.addEvent(getEvent3);
		returnedEvents[0] = putEvent1;
		returnedEvents[1] = getEvent1;
		returnedEvents[2] = putEvent2;
		returnedEvents[3] = getEvent2;
		//returnedEvents[4] = putEvent3;
		//returnedEvents[5] = getEvent3;

		// Create states
		State initialState = station.createAndAddUniqueState("q");
		initialState.setInitial(true);
		initialState.setAccepting(true);
		station.setInitialState(initialState);
		State workState = station.createAndAddUniqueState("q");
		station_SMatr [s_SMindex][0] =initialState;
		station_SMatr [s_SMindex][1] =workState;
		s_SMindex++;
		// Create arcs
		Arc getArc1 = new Arc(initialState, workState, getEvent1);
		Arc putArc1 = new Arc(workState, initialState, putEvent1);
		Arc getArc2 = new Arc(initialState, workState, getEvent2);
		Arc putArc2 = new Arc(workState, initialState, putEvent2);
		//Arc getArc3 = new Arc(initialState, workState, getEvent3);
		//Arc putArc3 = new Arc(workState, initialState, putEvent3);


		thePlant.addAutomaton(station);
		sAindex++;
		return returnedEvents;
	}





	public void createIO(int table_id)
	{
	  	Automaton IO = new Automaton("IO_" + table_id);
	  	IO.setType(AutomatonType.Plant);
	  	IOAlphabet[Iindex] = IO.getAlphabet();


	  	if(table_id==0)
	  	{
			LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T"+ table_id+"L");
			LabeledEvent putEvent2 = new LabeledEvent("Shoe_2put_T"+ table_id+"L");
			//LabeledEvent putEvent3 = new LabeledEvent("Shoe_3put_T"+ table_id+"L");
			LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T"+ table_id+"L");
			LabeledEvent getEvent2 = new LabeledEvent("Shoe_2get_T"+ table_id+"L");
			//LabeledEvent getEvent3 = new LabeledEvent("Shoe_3get_T"+ table_id+"L");
			IOAlphabet[Iindex].addEvent(putEvent1);
			IOAlphabet[Iindex].addEvent(putEvent2);
			//IOAlphabet[Iindex].addEvent(putEvent3);
			IOAlphabet[Iindex].addEvent(getEvent1);
			IOAlphabet[Iindex].addEvent(getEvent2);
			//IOAlphabet[Iindex].addEvent(getEvent3);
	  	  	State initialState = IO.createAndAddUniqueState("q");
	  	  	IOSTate[IOindex]=initialState;
	  	  	IOindex++;
	  	  	initialState.setInitial(true);
	  	  	initialState.setAccepting(true);
	  	  	IO.setInitialState(initialState);
	  	  	Arc putArc1 = new Arc(initialState, initialState, putEvent1);
	  	  	Arc putArc2 = new Arc(initialState, initialState, putEvent2);
	  	  	//Arc putArc3 = new Arc(initialState, initialState, putEvent3);
	  	  	Arc getArc1 = new Arc(initialState, initialState, getEvent1);
			Arc getArc2 = new Arc(initialState, initialState, getEvent2);
	  	  	//Arc getArc3 = new Arc(initialState, initialState, getEvent3);
	  	  	thePlant.addAutomaton(IO);
	  	}

	  	else if (table_id==1)
	  	{
	  		LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T0"+"R");
	  		LabeledEvent putEvent2 = new LabeledEvent("Shoe_1put_T1");
	  		LabeledEvent putEvent3 = new LabeledEvent("Shoe_1put_T2");
	  		LabeledEvent getEvent0 = new LabeledEvent("Shoe_1get_T0"+"R");
	  		LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T1");
	  		LabeledEvent getEvent2 = new LabeledEvent("Shoe_1get_T2");



			LabeledEvent putEvent4 = new LabeledEvent("Shoe_2put_T0"+"R");
			LabeledEvent putEvent5 = new LabeledEvent("Shoe_2put_T1");
			LabeledEvent putEvent6 = new LabeledEvent("Shoe_2put_T2");
			LabeledEvent getEvent3 = new LabeledEvent("Shoe_2get_T0"+"R");
			LabeledEvent getEvent4 = new LabeledEvent("Shoe_2get_T1");
			LabeledEvent getEvent5 = new LabeledEvent("Shoe_2get_T2");


			/*LabeledEvent putEvent7 = new LabeledEvent("Shoe_3put_T0"+"R");
			LabeledEvent putEvent8 = new LabeledEvent("Shoe_3put_T1");
			LabeledEvent putEvent9 = new LabeledEvent("Shoe_3put_T2");
			LabeledEvent getEvent6 = new LabeledEvent("Shoe_3get_T0"+"R");
			LabeledEvent getEvent7 = new LabeledEvent("Shoe_3get_T1");
			LabeledEvent getEvent8 = new LabeledEvent("Shoe_3get_T2");
*/

	  		IOAlphabet[Iindex].addEvent(putEvent1);
			IOAlphabet[Iindex].addEvent(putEvent2);
			IOAlphabet[Iindex].addEvent(putEvent3);
			IOAlphabet[Iindex].addEvent(putEvent4);
			IOAlphabet[Iindex].addEvent(putEvent5);
			IOAlphabet[Iindex].addEvent(putEvent6);
			//IOAlphabet[Iindex].addEvent(putEvent7);
			//IOAlphabet[Iindex].addEvent(putEvent8);
			//IOAlphabet[Iindex].addEvent(putEvent9);
	  		IOAlphabet[Iindex].addEvent(getEvent0);
	  		IOAlphabet[Iindex].addEvent(getEvent1);
			IOAlphabet[Iindex].addEvent(getEvent2);
			IOAlphabet[Iindex].addEvent(getEvent3);
			IOAlphabet[Iindex].addEvent(getEvent4);
			IOAlphabet[Iindex].addEvent(getEvent5);
			//IOAlphabet[Iindex].addEvent(getEvent6);
			//IOAlphabet[Iindex].addEvent(getEvent7);
			//IOAlphabet[Iindex].addEvent(getEvent8);

	  		State initialState = IO.createAndAddUniqueState("q");

	  		IOSTate[IOindex]=initialState ;
	  		IOindex++;
	  		initialState.setInitial(true);
	  		initialState.setAccepting(true);
	  		IO.setInitialState(initialState);

	  		State workState = IO.createAndAddUniqueState("q");
			IOSTate[IOindex]=workState;
			IOindex++;

		  	Arc putArc1 = new Arc(workState , initialState, putEvent1);
			Arc putArc2 = new Arc(workState , initialState, putEvent2);
			Arc putArc3 = new Arc(workState , initialState, putEvent3);
	  		Arc putArc4 = new Arc(workState , initialState, putEvent4);
	  		Arc putArc5 = new Arc(workState , initialState, putEvent5);
	  		Arc putArc6 = new Arc(workState , initialState, putEvent6);
	  		//Arc putArc7 = new Arc(workState , initialState, putEvent7);
	  		//Arc putArc8 = new Arc(workState , initialState, putEvent8);
	  		//Arc putArc9 = new Arc(workState , initialState, putEvent9);
	  		Arc getArc0 = new Arc(initialState, workState, getEvent0);
	  		Arc getArc1 = new Arc(initialState, workState, getEvent1);
      		Arc getArc2 = new Arc(initialState, workState, getEvent2);
			Arc getArc3 = new Arc(initialState, workState, getEvent3);
			Arc getArc4 = new Arc(initialState, workState, getEvent4);
			Arc getArc5 = new Arc(initialState, workState, getEvent5);
			//Arc getArc6 = new Arc(initialState, workState, getEvent6);
			//Arc getArc7 = new Arc(initialState, workState, getEvent7);
			//Arc getArc8 = new Arc(initialState, workState, getEvent8);

   			thePlant.addAutomaton(IO);
		}
	}

	public Project getPlant()
	{
		return thePlant;
	}
}