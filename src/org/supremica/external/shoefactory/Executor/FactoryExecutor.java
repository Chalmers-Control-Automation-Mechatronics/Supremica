package org.supremica.external.shoefactory.Executor;

import org.supremica.external.shoefactory.plantBuilder.*;
import org.supremica.external.shoefactory.Animator.*;
import org.supremica.gui.*;
import grafchart.sfc.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.log.*;

public class FactoryExecutor
{
	public static int threadSleepInterval=100;
	boolean[] stationV;
	static Plant shoePlant;
	static int shoeNr=1, firstIndex=0, sNr=0;
	static Gui gui;
	static EditorAPI e;
	static ArrayList shoes = new ArrayList();
	static ArrayList shoeNumbers = new ArrayList();
	GCDocument top;
	GCDocument  jgSupervisor;
//jgsup static för linj
	public FactoryExecutor()
	{

	}

	public void start(boolean [] sv, Gui g)
	{
		stationV = sv;
		gui = g;
		String[] args = {""};

		try
		{
			if(shoeNr==1)
			{
				e = new EditorAPI(args);
				Editor.singleton = e;
				e.removePaletteAction();

				URL url = Supremica.class.getResource("/shoefactory/ShoeFactory.xml");
				top = e.openWorkspace(url.getPath());

				jgSupervisor = e.newWorkspace();
				JgrafSupervisor js = new JgrafSupervisor(jgSupervisor,shoeNr);

				shoePlant = new Plant();
				gui.addProject(shoePlant.getPlant());

				Specification shoeSpec = new Specification(shoeNr,sv);
				gui.addProject(shoeSpec.getSpec());

				SyncBuilder syncPlant = new SyncBuilder(gui, shoePlant.getPlant());
				syncPlant.synthesizePlants("theSupervisor");

				GCDocument newShoe = e.newWorkspace();
				Shoe s = new Shoe(newShoe, stationV, shoeNr);
				shoes.add(newShoe);
				shoeNumbers.add(new Integer(shoeNr));
				
				newShoe.setSpeed(threadSleepInterval);
				top.setSpeed(threadSleepInterval);
				jgSupervisor.setSpeed(threadSleepInterval);
				

				boolean OK = e.compileWorkspace(top);
				if(OK)
				{
					e.startWorkspace(top);
				}

				OK = e.compileWorkspace(jgSupervisor);
				if(OK)
				{
					e.startWorkspace(jgSupervisor);
				}

				OK = e.compileWorkspace(newShoe);
				if(OK)
				{
					//e.startWorkspace(newShoe);
				}
				
				shoeNr++;
			}
			else
			{
				pauseSFC(20000);
				e.stopWorkspace(jgSupervisor);
				
				shoePlant.add_shoe(shoeNr,true);
				Specification shoeSpec = new Specification(shoeNr,sv);
				gui.addProject(shoeSpec.getSpec());

				GCDocument newShoe = e.newWorkspace();
				Shoe s = new Shoe(newShoe, stationV, shoeNr);
				shoes.add(newShoe);
				shoeNumbers.add(new Integer(shoeNr));
				
				shoes.trimToSize();
				shoeNumbers.trimToSize();
				
				remove_Aut(44+shoes.size());

				SyncBuilder syncPlant = new SyncBuilder(gui, shoePlant.getPlant());
				syncPlant.synthesizePlants("theSupervisor");
					
				shoeNr++;
	
				boolean OK = e.compileWorkspace(jgSupervisor);
				if(OK)
				{
					e.startWorkspace(jgSupervisor);
				}
				
				pauseSFC(threadSleepInterval);


				OK = e.compileWorkspace(newShoe);
				if(OK)
				{
					e.startWorkspace(newShoe);
				}


			}
		}
		catch(Exception ex)
		{
			System.out.println("Error"+shoeNr);
		}
	}
	
	public static boolean saveValues(int fI, int s)
	{
		firstIndex = fI;
		sNr = s;
		return true;
	}

	
	public static int getSValue()
	{
		return sNr;
	}
	
	public static int getFiValue()
	{
		return firstIndex;
	}

	public static boolean deleteShoe (int nr)
	{
		Integer i = new Integer(nr);
		GCDocument temp = (GCDocument) shoes.get(shoeNumbers.indexOf(i));
		e.stopWorkspace(temp);
		e.deleteWorkspace(temp);
		shoePlant.remove_shoe(nr);
		remove_Aut(45+shoeNumbers.indexOf(i));
		
		System.out.println("Shoe "+nr+" is manufactured and ready.");
		
		shoes.remove(shoeNumbers.indexOf(i));
		shoeNumbers.remove(shoeNumbers.indexOf(i));
		
		shoes.trimToSize();
		shoeNumbers.trimToSize();
	
		return true;
	}

	public static void remove_Aut(int nr)
	{
		int[] sel ={nr};
		gui.selectAutomata(sel);
		Automata selectedAutomata = gui.getSelectedAutomata();
		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();

			try
			{
				gui.getVisualProjectContainer().getActiveProject().removeAutomaton(currAutomatonName);
			}
			catch (Exception ex)
			{

			}
		}
	}

	//A really ugly way of pausing Jgrafchart by setting the Thread sleep to a very large number
	public void pauseSFC(int time)
	{
		for(int nr=1; nr<=shoes.size(); nr++)
		{
			GCDocument temp = (GCDocument)shoes.get(nr-1);
			temp.setSpeed(time);
		}

		top.setSpeed(time);
		jgSupervisor.setSpeed(time);
	}
	
/*	public static boolean chngspeed(int ms)
		{	
			jgSupervisor.setSpeed(ms);
			return true;
		}			
	
*/	
	
	
	
	
	public static int getShoeIndex(int currIndex)
	{
		Integer i=new Integer(0);

		if(currIndex==0)
		{
			return i.parseInt(shoeNumbers.get(0).toString());
		}	
		else if(shoeNumbers.indexOf(new Integer(currIndex)) < shoeNumbers.size()-1)	
		{
			return i.parseInt( shoeNumbers.get(shoeNumbers.indexOf(new Integer(currIndex))+1).toString() ) ;	
		}
		
		else
			return -1;
	}
}