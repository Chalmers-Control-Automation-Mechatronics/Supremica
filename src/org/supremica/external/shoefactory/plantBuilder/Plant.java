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

import java.util.*;
import org.supremica.automata.*;

public class Plant
{
	State[] filledOneState = new State[100];
	protected Project thePlant = null;
	private int[] nrOfStationsPerTable = {0,0,0,0,0,0,5,4,3,4,1,3,4};

	private State[][] table_SMatr = new State[13][25];
	private State[][] station_SMatr = new State[24][2];

	private State[] IOSTate = new State [25];

	private int s_SMindex=0,index=0,IOindex=0,IOAlfindex=0,Iindex=0;

	private Automaton station =null;

	private Alphabet[] currAlphabet = new Alphabet[13] ;
	private Alphabet[] stationAlphabet = new Alphabet[24];
	private Alphabet[] IOAlphabet = new Alphabet[25];
	private int nrOfSlots=12;
	private int sAindex=0,sind=0;
	private ArrayList shoesInSystem = new ArrayList(1);

	public Plant()
	{
		thePlant = new Project();
		shoesInSystem.add(new Integer(1));
		
		for(int i=0; i<6; i++)
		{
			createTable(i,0);
		}

		for(int i=6; i<13; i++)
		{
			createTable(i,1);
		}
	}

	public void createTable(int table_id,int table_type)
	{
		if(table_id==10)
			sind=20;
	
		if(table_id==12)
			sind=16;			
			
		int X = nrOfSlots + table_type * nrOfSlots;

		Automaton currTable = new Automaton("Table"+table_id);
	  	currTable.setType(AutomatonType.Plant);
	   	currAlphabet [table_id] = currTable.getAlphabet();

		for(int i=0;i<X+1;i++)
	    {
		    filledOneState [i] = currTable.createAndAddUniqueState("q");
			table_SMatr[table_id][i]=  filledOneState [i];
		}

		filledOneState[0].setInitial(true);
		filledOneState[0].setAccepting(true);
		currTable.setInitialState(filledOneState[0]);
		
		if(table_type ==0 || (table_id ==10 && table_type ==1))
		{
			LabeledEvent putEventL = new LabeledEvent("Shoe_1put_T"+table_id+"L");
			LabeledEvent getEventL = new LabeledEvent("Shoe_1get_T"+table_id+"L");
			currAlphabet[table_id].addEvent(putEventL);
			currAlphabet[table_id].addEvent(getEventL);		
			LabeledEvent putEventR = new LabeledEvent("Shoe_1put_T"+table_id+"R");
			LabeledEvent getEventR = new LabeledEvent("Shoe_1get_T"+table_id+"R");
			currAlphabet[table_id].addEvent(putEventR);
			currAlphabet[table_id].addEvent(getEventR);
			Arc putArcL;
			Arc getArcL;
			Arc getArcR;
			Arc putArcR;
			for(int i=0;i<X;i++)
			{
				putArcL = new Arc(filledOneState[i], filledOneState[i+1], putEventL);
		    	getArcL = new Arc(filledOneState[i+1],filledOneState[i], getEventL);
		    	putArcR = new Arc(filledOneState[i], filledOneState[i+1], putEventR);
		    	getArcR = new Arc(filledOneState[i+1],filledOneState[i], getEventR);
			}			
		}	
	
		else
		{
			LabeledEvent putEvent = new LabeledEvent("Shoe_1put_T"+table_id);
			LabeledEvent getEvent = new LabeledEvent("Shoe_1get_T"+table_id);
			currAlphabet[table_id].addEvent(putEvent);
			currAlphabet[table_id].addEvent(getEvent);
			Arc  putArc ;
			Arc  getArc ;
			for(int i=0;i<X;i++)
			{
				putArc = new Arc(filledOneState[i], filledOneState[i+1], putEvent);
		    	getArc = new Arc(filledOneState[i+1],filledOneState[i], getEvent);
			}		
		}
	
		LabeledEvent CreatedEvents [] =new LabeledEvent [2];
		createIO(table_id);

		if(table_type ==1)
		{
			for(int j=0; j<nrOfStationsPerTable[table_id]; j++)
			{
				CreatedEvents=createStation(table_id, sind, currAlphabet[table_id]);
				for(int i=0;i<X;i++)
				{
					Arc putArc = new Arc(filledOneState [i], filledOneState [i+1], CreatedEvents[0]);
					Arc getArc = new Arc(filledOneState [i+1],filledOneState [i], CreatedEvents[1]);
				}
				sind++;
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

		State initialState = station.createAndAddUniqueState("q");
		initialState.setInitial(true);
		initialState.setAccepting(true);
		station.setInitialState(initialState);
		State workState = station.createAndAddUniqueState("q");

		station_SMatr [s_SMindex][0] =initialState;
		station_SMatr [s_SMindex][1] =workState;
		s_SMindex++;

		Arc getArc = new Arc(initialState, workState, getEvent);
		Arc putArc = new Arc(workState, initialState, putEvent);

		thePlant.addAutomaton(station);
		sAindex++;
		return returnedEvents;
	}

	public void add_shoe(int snmbr, boolean newShoe)
	{
		sAindex =0;
		index=0;
		sind=0;
		IOAlfindex=0;
		int X = nrOfSlots ;
		
		if(newShoe)
			shoesInSystem.add(new Integer(snmbr));

		//Add events to middle tables
		for(int i=0;i<6;i++)
		{
			LabeledEvent putEventL = new LabeledEvent("Shoe_"+snmbr+"put_T"+i+"L");
			LabeledEvent getEventL = new LabeledEvent("Shoe_"+snmbr+"get_T"+i+"L");
			currAlphabet[i].addEvent(putEventL);
			currAlphabet[i].addEvent(getEventL);
			LabeledEvent putEventR = new LabeledEvent("Shoe_"+snmbr+"put_T"+i+"R");
			LabeledEvent getEventR = new LabeledEvent("Shoe_"+snmbr+"get_T"+i+"R");
			currAlphabet[i].addEvent(putEventR);
			currAlphabet[i].addEvent(getEventR);
			for(int j=0;j<12;j++)
			{
				Arc	putArcL  = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEventL);
	    		Arc	getArcL  = new Arc(table_SMatr[i][j+1],table_SMatr[i][j], getEventL);
	    		Arc	putArcR  = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEventR);
	    		Arc	getArcR  = new Arc(table_SMatr[i][j+1],table_SMatr[i][j], getEventR);
			}
		}
	
		
		//Add events to tables with stations
		for(int i=6;i<13;i++)
		{
			if(i==10)
			{
				LabeledEvent putEvent1 = new LabeledEvent("Shoe_"+snmbr+"put_T"+i+"L");
				LabeledEvent getEvent1 = new LabeledEvent("Shoe_"+snmbr+"get_T"+i+"R");
				LabeledEvent putEvent2 = new LabeledEvent("Shoe_"+snmbr+"put_T"+i+"R");
				LabeledEvent getEvent2 = new LabeledEvent("Shoe_"+snmbr+"get_T"+i+"L");
				currAlphabet[i].addEvent(putEvent1);
				currAlphabet[i].addEvent(getEvent1);
				currAlphabet[i].addEvent(putEvent2);
				currAlphabet[i].addEvent(getEvent2);
				for(int j=0;j<24;j++)
				{
					Arc	putArc1 = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEvent1);
	    			Arc	getArc1 = new Arc(table_SMatr[i][j+1],table_SMatr[i][j], getEvent1);
	    			Arc	putArc2 = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEvent2);
	    			Arc	getArc2 = new Arc(table_SMatr[i][j+1],table_SMatr[i][j], getEvent2);
	    		}	
			}
			else
			{
				LabeledEvent putEvent = new LabeledEvent("Shoe_"+snmbr+"put_T"+i);
				LabeledEvent getEvent = new LabeledEvent("Shoe_"+snmbr+"get_T"+i);
				currAlphabet[i].addEvent(putEvent);
				currAlphabet[i].addEvent(getEvent);
				for(int j=0;j<24;j++)
				{
					Arc	putArc = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEvent);
		    		Arc	getArc = new Arc(table_SMatr[i][j+1],table_SMatr[i][j], getEvent);
		    	
				}
			}
		}

		//Add events to stations
		for(int i=6;i<13;i++)
		{	
			if(i==10)
				sind=20;
	
			if(i==12)
				sind=16;	
			
			for(int k=0;k < nrOfStationsPerTable[i];k++)
			{
				LabeledEvent putEvent = new LabeledEvent("Shoe_"+snmbr+"put_T"+i+"_S"+sind);
				LabeledEvent getEvent = new LabeledEvent("Shoe_"+snmbr+"get_T"+i+"_S"+sind);
				stationAlphabet[index].addEvent(putEvent);
				stationAlphabet[index].addEvent(getEvent);
				currAlphabet[i].addEvent(putEvent);
		     	currAlphabet[i].addEvent(getEvent);
				
				for(int j=0;j<24;j++)
				{
					Arc	putArc  = new Arc(table_SMatr[i][j], table_SMatr[i][j+1], putEvent);
	    			Arc	getArc  = new Arc(table_SMatr[i][j+1],table_SMatr[i][j], getEvent);
				}

				Arc	putArc  = new Arc( station_SMatr[index][0],  station_SMatr[index][1], getEvent);
				Arc	getArc  = new Arc( station_SMatr[index][1], station_SMatr[index][0], putEvent);
				index++;
				sind++;
			}
		}

		index=0;
		LabeledEvent putEvent0 = new LabeledEvent("Shoe_"+snmbr+"put_T0L");

		IOAlphabet[IOAlfindex].addEvent(putEvent0);
		IOAlfindex++;

		Arc putArc0 = new Arc(IOSTate[index], IOSTate[index], putEvent0);
		index++;

		//Add events to agvs
		for(int i=1;i<5;i++)
		{
			LabeledEvent putEvent1 = new LabeledEvent("Shoe_"+snmbr+"put_T"+i+"L");			
			LabeledEvent putEvent2 = new LabeledEvent("Shoe_"+snmbr+"put_T"+(i+5));	
			LabeledEvent putEvent3 = new LabeledEvent("Shoe_"+snmbr+"put_T"+(i-1)+"R");
			LabeledEvent getEvent1 = new LabeledEvent("Shoe_"+snmbr+"get_T"+(i-1)+"R");				
			LabeledEvent getEvent2 = new LabeledEvent("Shoe_"+snmbr+"get_T"+(i+5));	
			LabeledEvent getEvent3 = new LabeledEvent("Shoe_"+snmbr+"get_T"+(i)+"L");
			
			IOAlphabet[IOAlfindex].addEvent(putEvent1);
			IOAlphabet[IOAlfindex].addEvent(putEvent2);
			IOAlphabet[IOAlfindex].addEvent(putEvent3);
			
			IOAlphabet[IOAlfindex].addEvent(getEvent1);
			IOAlphabet[IOAlfindex].addEvent(getEvent2);
			IOAlphabet[IOAlfindex].addEvent(getEvent3);
			IOAlfindex++;

			Arc putArc1 = new Arc(IOSTate[index+1], IOSTate[index], putEvent1);
			Arc putArc2 = new Arc(IOSTate[index+1], IOSTate[index], putEvent2);
			Arc putArc3 = new Arc(IOSTate[index+1], IOSTate[index], putEvent3);
			Arc getArc1 = new Arc(IOSTate[index], IOSTate[index+1], getEvent1);
			Arc getArc2 = new Arc(IOSTate[index], IOSTate[index+1], getEvent2);
			Arc getArc3 = new Arc(IOSTate[index], IOSTate[index+1], getEvent3);
			Arc putArc4 = new Arc(IOSTate[index+2], IOSTate[index+1], putEvent1);
			Arc putArc5 = new Arc(IOSTate[index+2], IOSTate[index+1], putEvent2);
			Arc putArc6 = new Arc(IOSTate[index+2], IOSTate[index+1], putEvent3);
			Arc getArc4 = new Arc(IOSTate[index+1], IOSTate[index+2], getEvent1);
			Arc getArc5 = new Arc(IOSTate[index+1], IOSTate[index+2], getEvent2);
			Arc getArc6 = new Arc(IOSTate[index+1], IOSTate[index+2], getEvent3);
			Arc putArc7 = new Arc(IOSTate[index+3], IOSTate[index+2], putEvent1);
			Arc putArc8 = new Arc(IOSTate[index+3], IOSTate[index+2], putEvent2);
			Arc putArc9 = new Arc(IOSTate[index+3], IOSTate[index+2], putEvent3);
			Arc getArc7 = new Arc(IOSTate[index+2], IOSTate[index+3], getEvent1);
			Arc getArc8 = new Arc(IOSTate[index+2], IOSTate[index+3], getEvent2);
			Arc getArc9 = new Arc(IOSTate[index+2], IOSTate[index+3], getEvent3);
			index+=4;
		}
		
		LabeledEvent putEvent7 = new LabeledEvent("Shoe_"+snmbr+"put_T5L");
		LabeledEvent putEvent8 = new LabeledEvent("Shoe_"+snmbr+"put_T10L");
		LabeledEvent putEvent9 = new LabeledEvent("Shoe_"+snmbr+"put_T4R");
		LabeledEvent getEvent7 = new LabeledEvent("Shoe_"+snmbr+"get_T4R");	
		LabeledEvent getEvent8 = new LabeledEvent("Shoe_"+snmbr+"get_T10L");
		LabeledEvent getEvent9 = new LabeledEvent("Shoe_"+snmbr+"get_T5L");
		
		IOAlphabet[IOAlfindex].addEvent(putEvent7);
		IOAlphabet[IOAlfindex].addEvent(putEvent8);	
		IOAlphabet[IOAlfindex].addEvent(putEvent9);
		
		IOAlphabet[IOAlfindex].addEvent(getEvent7);
		IOAlphabet[IOAlfindex].addEvent(getEvent8);
		IOAlphabet[IOAlfindex].addEvent(getEvent9);
		IOAlfindex++;

		Arc putArc7 = new Arc(IOSTate[index+1], IOSTate[index], putEvent7);
		Arc putArc8 = new Arc(IOSTate[index+1], IOSTate[index], putEvent8);
		Arc putArc9 = new Arc(IOSTate[index+1], IOSTate[index], putEvent9);
		Arc getArc7 = new Arc(IOSTate[index], IOSTate[index+1], getEvent7);
		Arc getArc8 = new Arc(IOSTate[index], IOSTate[index+1], getEvent8);
		Arc getArc9 = new Arc(IOSTate[index], IOSTate[index+1], getEvent9);
		Arc putArc10 = new Arc(IOSTate[index+2], IOSTate[index+1], putEvent7);
		Arc putArc11 = new Arc(IOSTate[index+2], IOSTate[index+1], putEvent8);
		Arc putArc12 = new Arc(IOSTate[index+2], IOSTate[index+1], putEvent9);
		Arc getArc10 = new Arc(IOSTate[index+1], IOSTate[index+2], getEvent7);
		Arc getArc11 = new Arc(IOSTate[index+1], IOSTate[index+2], getEvent8);
		Arc getArc12 = new Arc(IOSTate[index+1], IOSTate[index+2], getEvent9);
		Arc putArc13 = new Arc(IOSTate[index+3], IOSTate[index+2], putEvent7);
		Arc putArc14 = new Arc(IOSTate[index+3], IOSTate[index+2], putEvent8);
		Arc putArc15 = new Arc(IOSTate[index+3], IOSTate[index+2], putEvent9);
		Arc getArc13 = new Arc(IOSTate[index+2], IOSTate[index+3], getEvent7);
		Arc getArc14 = new Arc(IOSTate[index+2], IOSTate[index+3], getEvent8);
		Arc getArc15 = new Arc(IOSTate[index+2], IOSTate[index+3], getEvent9);
		index+=4;
			
		LabeledEvent putEvent10 = new LabeledEvent("Shoe_"+snmbr+"put_T11");
		LabeledEvent getEvent10 = new LabeledEvent("Shoe_"+snmbr+"get_T5R");
		LabeledEvent getEvent11 = new LabeledEvent("Shoe_"+snmbr+"get_T11");
		LabeledEvent putEvent11 = new LabeledEvent("Shoe_"+snmbr+"put_T5R");

		IOAlphabet[IOAlfindex].addEvent(putEvent10);
		IOAlphabet[IOAlfindex].addEvent(putEvent11);		
		
		IOAlphabet[IOAlfindex].addEvent(getEvent10);
		IOAlphabet[IOAlfindex].addEvent(getEvent11);
		IOAlfindex++;

		Arc putArc1 = new Arc(IOSTate[index+1], IOSTate[index], putEvent10);
		Arc getArc1 = new Arc(IOSTate[index], IOSTate[index+1], getEvent10);
		Arc putArc2 = new Arc(IOSTate[index+1], IOSTate[index], putEvent11);
		Arc getArc2 = new Arc(IOSTate[index], IOSTate[index+1], getEvent11);
		index+=2;

		LabeledEvent putEvent12 = new LabeledEvent("Shoe_"+snmbr+"put_T12");
		LabeledEvent getEvent12 = new LabeledEvent("Shoe_"+snmbr+"get_T10R");
		LabeledEvent getEvent13 = new LabeledEvent("Shoe_"+snmbr+"get_T12");
		LabeledEvent putEvent13 = new LabeledEvent("Shoe_"+snmbr+"put_T10R");

		IOAlphabet[IOAlfindex].addEvent(putEvent12);
		IOAlphabet[IOAlfindex].addEvent(putEvent13);

		IOAlphabet[IOAlfindex].addEvent(getEvent12);
		IOAlphabet[IOAlfindex].addEvent(getEvent13);
		
		Arc putArc3 = new Arc(IOSTate[index+1], IOSTate[index], putEvent12);
		Arc getArc3 = new Arc(IOSTate[index], IOSTate[index+1], getEvent12);
		Arc putArc4 = new Arc(IOSTate[index+1], IOSTate[index], putEvent13);
		Arc getArc4 = new Arc(IOSTate[index], IOSTate[index+1], getEvent13);
	}
	
	public void remove_shoe(int snmbr)
	{
		shoesInSystem.remove(shoesInSystem.indexOf(new Integer(snmbr)));
		
	 	for(int j=0;j<currAlphabet.length;j++)
		 	{
		 		currAlphabet[j].clear();		
		 	}
		
		for(int i=0;i<24;i++)
		{
		for(int j=0;j<2;j++)
		{
		if(station_SMatr[i][j]!=null)
			station_SMatr[i][j].removeArcs();
		
		}
		}
		
		for(int i=0;i<13;i++)
		{
		for(int j=0;j<25;j++)
		{
		if(table_SMatr[i][j]!=null)
			table_SMatr[i][j].removeArcs();
		}
		}
		
		for(int i=0;i<25;i++)
		{
		if(IOSTate[i]!=null)
			IOSTate[i].removeArcs();	
		}
	
	 	for(int j=0;j<stationAlphabet.length;j++)
	 	{
	 		stationAlphabet[j].clear();
	 	}

	 
	 	for(int j=0;j<IOAlphabet.length;j++)
	 	{
	 		if(IOAlphabet[j]!=null)
	 			IOAlphabet[j].clear();
	 	}
	 	for(int i=0; i<shoesInSystem.size(); i++)
	 	{
	 		add_shoe(((Integer)(shoesInSystem.get(i))).intValue(),false);
	 		shoesInSystem.trimToSize();
	 	}
	}

	public void createIO(int table_id)
	{
		int IOnr = table_id;
		if(table_id==11 || table_id==12)
			IOnr=table_id-5;
			
		Automaton IO = new Automaton("IO_" + IOnr);
	  	IO.setType(AutomatonType.Plant);
	  	IOAlphabet[Iindex] = IO.getAlphabet();

	  	if(table_id==0)
	  	{
			LabeledEvent putEvent = new LabeledEvent("Shoe_1put_T0L");
			IOAlphabet[Iindex].addEvent(putEvent);
	  	  	State initialState = IO.createAndAddUniqueState("q");
	  	  	IOSTate[IOindex]=initialState;
	  	  	IOindex++;
	  	  	initialState.setInitial(true);
	  	  	initialState.setAccepting(true);
	  	  	IO.setInitialState(initialState);
	  	  	Arc putArc = new Arc(initialState, initialState, putEvent);
	  	  	thePlant.addAutomaton(IO);
	  	  	Iindex++;
	  	}

	  	else if (table_id==1 || table_id==2|| table_id==3 || table_id==4)
	  	{
	  		LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T"+table_id+"L");
	  		LabeledEvent putEvent2 = new LabeledEvent("Shoe_1put_T"+(table_id+5));
	  		LabeledEvent putEvent3 = new LabeledEvent("Shoe_1put_T"+(table_id-1)+"R");
	  		LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T"+(table_id-1)+"R");
	  		LabeledEvent getEvent2 = new LabeledEvent("Shoe_1get_T"+(table_id+5));
			LabeledEvent getEvent3 = new LabeledEvent("Shoe_1get_T"+(table_id)+"L");
			
	  		IOAlphabet[Iindex].addEvent(putEvent1);
			IOAlphabet[Iindex].addEvent(putEvent2);
			IOAlphabet[Iindex].addEvent(putEvent3);
	  		
	  		IOAlphabet[Iindex].addEvent(getEvent1);
	  		IOAlphabet[Iindex].addEvent(getEvent2);
	  		IOAlphabet[Iindex].addEvent(getEvent3);

	  		State initialState = IO.createAndAddUniqueState("q");
	  		
	  		IOSTate[IOindex] = initialState ;
	  		IOindex++;
	  		
	  		initialState.setInitial(true);
	  		initialState.setAccepting(true);
	  		IO.setInitialState(initialState);
	  		
	  		State workState1 = IO.createAndAddUniqueState("q");
	  		State workState2 = IO.createAndAddUniqueState("q");
	  		State workState3 = IO.createAndAddUniqueState("q");

			IOSTate[IOindex] = workState1;
			IOindex++;
			IOSTate[IOindex] = workState2;
			IOindex++;
			IOSTate[IOindex] = workState3;
			IOindex++;

		  	Arc putArc1 = new Arc(workState1, initialState, putEvent1);
			Arc putArc2 = new Arc(workState1, initialState, putEvent2);
			Arc putArc3 = new Arc(workState1, initialState, putEvent3);
	  		Arc getArc1 = new Arc(initialState, workState1, getEvent1);
      		Arc getArc2 = new Arc(initialState, workState1, getEvent2);
      		Arc getArc3 = new Arc(initialState, workState1, getEvent3);
      		Arc putArc4 = new Arc(workState2, workState1, putEvent1);
			Arc putArc5 = new Arc(workState2, workState1, putEvent2);
			Arc putArc6 = new Arc(workState2, workState1, putEvent3);
	  		Arc getArc4 = new Arc(workState1, workState2, getEvent1);
      		Arc getArc5 = new Arc(workState1, workState2, getEvent2);
      		Arc getArc6 = new Arc(workState1, workState2, getEvent3);
      		Arc putArc7 = new Arc(workState3, workState2, putEvent1);
			Arc putArc8 = new Arc(workState3, workState2, putEvent2);
			Arc putArc9 = new Arc(workState3, workState2, putEvent3);
	  		Arc getArc7 = new Arc(workState2, workState3, getEvent1);
      		Arc getArc8 = new Arc(workState2, workState3, getEvent2);
      		Arc getArc9 = new Arc(workState2, workState3, getEvent3);

   			thePlant.addAutomaton(IO);
   			Iindex++;
		}

		else if(table_id==5)
		{
			LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T5L");
	  		LabeledEvent putEvent2 = new LabeledEvent("Shoe_1put_T10L");
	  		LabeledEvent putEvent3 = new LabeledEvent("Shoe_1put_T4R");
	  		LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T4R");
	  		LabeledEvent getEvent2 = new LabeledEvent("Shoe_1get_T10L");
			LabeledEvent getEvent3 = new LabeledEvent("Shoe_1get_T5L");
			
	  		IOAlphabet[Iindex].addEvent(putEvent1);
			IOAlphabet[Iindex].addEvent(putEvent2);
			IOAlphabet[Iindex].addEvent(putEvent3);
	  		
	  		IOAlphabet[Iindex].addEvent(getEvent1);
	  		IOAlphabet[Iindex].addEvent(getEvent2);
	  		IOAlphabet[Iindex].addEvent(getEvent3);

	  		State initialState = IO.createAndAddUniqueState("q");
	  		
	  		IOSTate[IOindex] = initialState ;
	  		IOindex++;
	  		
	  		initialState.setInitial(true);
	  		initialState.setAccepting(true);
	  		IO.setInitialState(initialState);
	  		
	  		State workState1 = IO.createAndAddUniqueState("q");
	  		State workState2 = IO.createAndAddUniqueState("q");
	  		State workState3 = IO.createAndAddUniqueState("q");

			IOSTate[IOindex] = workState1;
			IOindex++;
			IOSTate[IOindex] = workState2;
			IOindex++;
			IOSTate[IOindex] = workState3;
			IOindex++;

		  	Arc putArc1 = new Arc(workState1, initialState, putEvent1);
			Arc putArc2 = new Arc(workState1, initialState, putEvent2);
			Arc putArc3 = new Arc(workState1, initialState, putEvent3);
	  		Arc getArc1 = new Arc(initialState, workState1, getEvent1);
      		Arc getArc2 = new Arc(initialState, workState1, getEvent2);
      		Arc getArc3 = new Arc(initialState, workState1, getEvent3);
      		Arc putArc4 = new Arc(workState2, workState1, putEvent1);
			Arc putArc5 = new Arc(workState2, workState1, putEvent2);
			Arc putArc6 = new Arc(workState2, workState1, putEvent3);
	  		Arc getArc4 = new Arc(workState1, workState2, getEvent1);
      		Arc getArc5 = new Arc(workState1, workState2, getEvent2);
      		Arc getArc6 = new Arc(workState1, workState2, getEvent3);
      		Arc putArc7 = new Arc(workState3, workState2, putEvent1);
			Arc putArc8 = new Arc(workState3, workState2, putEvent2);
			Arc putArc9 = new Arc(workState3, workState2, putEvent3);
	  		Arc getArc7 = new Arc(workState2, workState3, getEvent1);
      		Arc getArc8 = new Arc(workState2, workState3, getEvent2);
      		Arc getArc9 = new Arc(workState2, workState3, getEvent3);

   			thePlant.addAutomaton(IO);
   			Iindex++;
		}
			
		else if (table_id==11)
		{
			LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T11");
	    	LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T5R");
	    	LabeledEvent getEvent2 = new LabeledEvent("Shoe_1get_T11");
	    	LabeledEvent putEvent2 = new LabeledEvent("Shoe_1put_T5R");

			IOAlphabet[Iindex].addEvent(putEvent1);
			IOAlphabet[Iindex].addEvent(putEvent2);

			IOAlphabet[Iindex].addEvent(getEvent1);
			IOAlphabet[Iindex].addEvent(getEvent2);

			State initialState = IO.createAndAddUniqueState("q");

			IOSTate[IOindex] = initialState;
			IOindex++;
			
			initialState.setInitial(true);
			initialState.setAccepting(true);
			IO.setInitialState(initialState);
			
			State workState = IO.createAndAddUniqueState("q");

			IOSTate[IOindex] = workState;
			IOindex++;
			
			Arc putArc1 = new Arc(workState, initialState, putEvent1);
			Arc getArc1 = new Arc(initialState, workState, getEvent1);
			Arc putArc2 = new Arc(workState, initialState, putEvent2);
			Arc getArc2 = new Arc(initialState, workState, getEvent2);

			thePlant.addAutomaton(IO);
			Iindex++;
		}

    	else if (table_id==12)
    	{
			LabeledEvent putEvent1 = new LabeledEvent("Shoe_1put_T12");
			LabeledEvent getEvent1 = new LabeledEvent("Shoe_1get_T10R");
			LabeledEvent getEvent2 = new LabeledEvent("Shoe_1get_T12");
			LabeledEvent putEvent2 = new LabeledEvent("Shoe_1put_T10R");
							
			IOAlphabet[Iindex].addEvent(putEvent1);
			IOAlphabet[Iindex].addEvent(putEvent2);

			IOAlphabet[Iindex].addEvent(getEvent1);
			IOAlphabet[Iindex].addEvent(getEvent2);

			State initialState = IO.createAndAddUniqueState("q");
			
			IOSTate[IOindex] = initialState ;
			IOindex++;
			
			initialState.setInitial(true);
			initialState.setAccepting(true);
			IO.setInitialState(initialState);
			
			State workState = IO.createAndAddUniqueState("q");
			
			IOSTate[IOindex]=workState;
			IOindex++;
			
			Arc putArc1 = new Arc(workState, initialState, putEvent1);
			Arc getArc1 = new Arc(initialState, workState, getEvent1);
			Arc putArc2 = new Arc(workState, initialState, putEvent2);
			Arc getArc2 = new Arc(initialState, workState, getEvent2);
			
			thePlant.addAutomaton(IO);
			Iindex++;
		}	
	}

	public Project getPlant()
	{
		return thePlant;
	}
}