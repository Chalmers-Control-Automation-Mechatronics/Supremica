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
package org.supremica.external.shoeFactory.plantBuilder;

import org.supremica.automata.*;

public class Plant
{
	State [] filledOneState = new  State [100];
	protected Project thePlant = null;
	private int[] nrOfStationsPerTable = {0,0,0,0,0,0,5,4,3,4,4,1,3};

	private State [][] table_SMatr =new State [13][25];
	private State [] [] station_SMatr =new State [24][2];

	private State [] IOSTate =new State [15];

	private int s_SMindex=0, index=0,IOindex=0,IOAlfindex=0,Iindex=0;

	private Automaton station =null;

	private Alphabet  [] currAlphabet =new Alphabet [13] ;
	private Alphabet  [] stationAlphabet = new Alphabet [24];
	private Alphabet [] IOAlphabet =new Alphabet[25];
	private int nrOfSlots=12;
	private int sAindex =0;

	public Plant()
	{
		thePlant = new Project();

		for(int i=0; i<6; i++)
		{
			createTable(i,0);
		}

		for(int i=6; i<13; i++)
		{
			createTable(i,1);
		}

		add_shoe(2);
		add_shoe(3);
		add_shoe(4);
		add_shoe(5);
		add_shoe(6);
	}

	public void createTable(int table_id,int table_type)
	{
		int X = nrOfSlots + table_type * nrOfSlots;

		Automaton currTable = new Automaton("Table"+table_id);
	  	currTable.setType(AutomatonType.Plant);
	   	currAlphabet [table_id] = currTable.getAlphabet();

		// Create events
		LabeledEvent putEvent = new LabeledEvent("Shoe_1put_T"+table_id);
		LabeledEvent getEvent = new LabeledEvent("Shoe_1get_T"+table_id);
		currAlphabet[table_id].addEvent(putEvent);
		currAlphabet[table_id].addEvent(getEvent);

		// Create states
	    for(int i=0;i<X+1;i++)
	    {
		    filledOneState [i] = currTable.createAndAddUniqueState("q");
			table_SMatr[table_id][i]=  filledOneState [i];
		}

		filledOneState[0].setInitial(true);
		filledOneState[0].setAccepting(true);
		currTable.setInitialState(filledOneState[0]);

		// Create arcs
		Arc  putArc ;
		Arc  getArc ;
		for(int i=0;i<X;i++)
		{
			putArc  = new Arc(filledOneState[i], filledOneState[i+1], putEvent);
	    	getArc  = new Arc(filledOneState[i+1],filledOneState[i] , getEvent);
		}

		//addEvent("work",dummyState,thirdState,currTable);
		LabeledEvent CreatedEvents [] =new LabeledEvent [2];

		createIO(table_id);

		if(table_type ==1)
		{
			for(int j=0; j<nrOfStationsPerTable[table_id]; j++)
			{
				CreatedEvents=createStation(table_id, j, currAlphabet[table_id]);
				for(int i=0;i<X;i++)
				{
					putArc  = new Arc(filledOneState [i], filledOneState [i+1] , CreatedEvents[0]);
					getArc  = new Arc(filledOneState [i+1],filledOneState [i] , CreatedEvents[1]);
				}
			}
		}
		thePlant.addAutomaton(currTable);
	}

	public LabeledEvent[] createStation(int table_id, int station_id, Alphabet tableAlphabet)
	{
		LabeledEvent[] returnedEvents = new LabeledEvent[2] ;
		station = new Automaton("Station_"+table_id+"_"+station_id);
		station.setType(AutomatonType.Plant);
		stationAlphabet[sAindex] = station.getAlphabet();

		LabeledEvent putEvent = new LabeledEvent("Shoe_1put_T"+table_id+"_S"+station_id);
		LabeledEvent getEvent = new LabeledEvent("Shoe_1get_T"+table_id+"_S"+station_id);
		stationAlphabet[sAindex].addEvent(putEvent);
		stationAlphabet[sAindex].addEvent(getEvent);
		tableAlphabet.addEvent(putEvent);
		tableAlphabet.addEvent(getEvent);
		returnedEvents[0] = putEvent;
		returnedEvents[1] = getEvent;

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
		Arc getArc = new Arc(initialState, workState, getEvent);
		Arc putArc = new Arc(workState, initialState, putEvent);

		thePlant.addAutomaton(station);
		sAindex++;
		return returnedEvents;
	}


	public void add_shoe(int snmbr)
	{
		sAindex =0;
		index=0;
		IOAlfindex=0;
		int X = nrOfSlots ;

		for(int i=0;i<13;i++)
		{
			LabeledEvent putEvent = new LabeledEvent("Shoe_"+snmbr+"put_T"+i);
			LabeledEvent getEvent = new LabeledEvent("Shoe_"+snmbr+"get_T"+i);
			currAlphabet[i].addEvent(putEvent);
			currAlphabet[i].addEvent(getEvent);
			for(int j=0;j<12;j++)
			{
				Arc	putArc  = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEvent);
	    		Arc	getArc  = new Arc(table_SMatr[i][j+1],table_SMatr[i][j] , getEvent);
			}
		}

		for(int  i=6;i<13;i++)
		{
			for(int k=0;k < nrOfStationsPerTable[i];k++)
			{
				LabeledEvent putEvent = new LabeledEvent("Shoe_"+snmbr+"put_T"+i+"_S"+k);
				LabeledEvent getEvent = new LabeledEvent("Shoe_"+snmbr+"get_T"+i+"_S"+k);
				stationAlphabet[index].addEvent(putEvent);
				stationAlphabet[index].addEvent(getEvent);
				currAlphabet[i].addEvent(putEvent);
		     	currAlphabet[i].addEvent(getEvent);

				for( int j=0;j<24;j++)
				{
					Arc	putArc  = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEvent);
	    			Arc	getArc  = new Arc(table_SMatr[i][j+1],table_SMatr[i][j] , getEvent);
				}

				Arc	putArc  = new Arc( station_SMatr[index][0],  station_SMatr[index][1], getEvent);
				Arc	getArc  = new Arc( station_SMatr[index][1], station_SMatr[index][0] , putEvent);
				index++;
			}
		}

		index=0;
		IOAlfindex=0;
		LabeledEvent putEvent0 = new LabeledEvent("Shoe_"+snmbr+"put_T"+ 0);

		IOAlphabet[IOAlfindex].addEvent(putEvent0);
		IOAlfindex++;

		Arc putArc0 = new Arc(IOSTate[index] , IOSTate[index], putEvent0);

		index++;

					for(int i=1;i<6;i++){

				 		LabeledEvent putEvent1 = new LabeledEvent("Shoe_"+snmbr+"put_T"+i);
				  		LabeledEvent putEvent2 = new LabeledEvent("Shoe_"+snmbr+"put_T"+(i+5));
				  		LabeledEvent getEvent1 = new LabeledEvent("Shoe_"+snmbr+ "get_T"+(i-1));
				  		LabeledEvent getEvent2 = new LabeledEvent("Shoe_"+snmbr+ "get_T"+(i+5));

						IOAlphabet[IOAlfindex].addEvent(putEvent1);

						IOAlphabet[IOAlfindex].addEvent(putEvent2);

						IOAlphabet[IOAlfindex].addEvent(getEvent1);

						IOAlphabet[IOAlfindex].addEvent(getEvent2);

						IOAlfindex++;

						Arc putArc1 = new Arc(IOSTate[index+1] , IOSTate[index], putEvent1);
						Arc putArc2 = new Arc(IOSTate[index+1] ,IOSTate[index], putEvent2);
						Arc getArc1 = new Arc(IOSTate[index],IOSTate[index+1], getEvent1);
      					Arc getArc2 = new Arc(IOSTate[index],IOSTate[index+1], getEvent2);
						index+=2;
						}

						LabeledEvent putEvent = new LabeledEvent("Shoe_"+snmbr+"put_T"+11);
	    				LabeledEvent getEvent = new LabeledEvent("Shoe_"+snmbr+"get_T5");

						IOAlphabet[IOAlfindex].addEvent(putEvent);

						IOAlphabet[IOAlfindex].addEvent(getEvent);
						IOAlfindex++;

						Arc putArc = new Arc(IOSTate[12] , IOSTate[11], putEvent);
						Arc getArc = new Arc(IOSTate[11], IOSTate[12], getEvent);

						LabeledEvent putEvent1 = new LabeledEvent("Shoe_"+snmbr+"put_T"+12);
						LabeledEvent getEvent1 = new LabeledEvent("Shoe_"+snmbr+"get_T10");

						IOAlphabet[IOAlfindex].addEvent(putEvent1);

						IOAlphabet[IOAlfindex].addEvent(getEvent1);

						Arc putArc1 = new Arc(IOSTate[14] , IOSTate[13], putEvent1);
						Arc getArc1 = new Arc(IOSTate[13], IOSTate[14], getEvent1);


}




	public void createIO(int table_id)
	{
	  	Automaton IO = new Automaton("IO_" + table_id);
	  	IO.setType(AutomatonType.Plant);
	  	IOAlphabet[Iindex] = IO.getAlphabet();


	  	if(table_id==0)
	  	{
			LabeledEvent putEvent = new LabeledEvent("Shoe_1put_T"+ table_id);
			IOAlphabet[Iindex].addEvent(putEvent);
	  	  	State initialState = IO.createAndAddUniqueState("q");
	  	  	IOSTate[IOindex]=initialState;
	  	  	IOindex++;
	  	  	initialState.setInitial(true);
	  	  	initialState.setAccepting(true);
	  	  	IO.setInitialState(initialState);
	  	  	Arc putArc = new Arc(initialState , initialState, putEvent);
	  	  	thePlant.addAutomaton(IO);
	  	}

	  	else if (table_id==1 || table_id==2|| table_id==3 || table_id==4 || table_id==5)
	  	{
	  		LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T"+table_id);
	  		LabeledEvent putEvent2 = new LabeledEvent("Shoe_1put_T"+(table_id+5));
	  		LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T"+(table_id-1));
	  		LabeledEvent getEvent2 = new LabeledEvent("Shoe_1get_T"+(table_id+5));

	  		IOAlphabet[Iindex].addEvent(putEvent1);
			IOAlphabet[Iindex].addEvent(putEvent2);

	  		IOAlphabet[Iindex].addEvent(getEvent1);

	  		IOAlphabet[Iindex].addEvent(getEvent2);

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
	  		Arc getArc1 = new Arc(initialState, workState, getEvent1);
      		Arc getArc2 = new Arc(initialState, workState, getEvent2);

   			thePlant.addAutomaton(IO);
		}

		else if (table_id==11)
		{
			LabeledEvent putEvent = new LabeledEvent("Shoe_1put_T"+table_id);
	    	LabeledEvent getEvent = new LabeledEvent("Shoe_1get_T5");

			IOAlphabet[Iindex].addEvent(putEvent);

			IOAlphabet[Iindex].addEvent(getEvent);

			State initialState = IO.createAndAddUniqueState("q");

			IOSTate[IOindex]=initialState;
			IOindex++;
			initialState.setInitial(true);
			initialState.setAccepting(true);
			IO.setInitialState(initialState);
			State workState = IO.createAndAddUniqueState("q");

			IOSTate[IOindex]=workState;
			IOindex++;
			Arc putArc = new Arc(workState , initialState, putEvent);
			Arc getArc = new Arc(initialState, workState, getEvent);

			thePlant.addAutomaton(IO);
		}

    	else if (table_id==12)
    	{
			LabeledEvent putEvent = new LabeledEvent("Shoe_1put_T"+table_id);
			LabeledEvent getEvent = new LabeledEvent("Shoe_1get_T10");

			IOAlphabet[Iindex].addEvent(putEvent);

			IOAlphabet[Iindex].addEvent(getEvent);

			State initialState = IO.createAndAddUniqueState("q");
			IOSTate[IOindex]=initialState ;
			IOindex++;
			initialState.setInitial(true);
			initialState.setAccepting(true);
			IO.setInitialState(initialState);
			State workState = IO.createAndAddUniqueState("q");
			IOSTate[IOindex]=workState;

			Arc putArc = new Arc(workState , initialState, putEvent);
			Arc getArc = new Arc(initialState, workState, getEvent);
			thePlant.addAutomaton(IO);
		}
			Iindex++;

	}

	public Project getPlant()
	{
		return thePlant;
	}
}