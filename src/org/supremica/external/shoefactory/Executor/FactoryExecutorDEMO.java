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

public class FactoryExecutorDEMO
{
	public static int threadSleepInterval=200;
	boolean type;
	static PlantDEMO shoePlant;
	static int shoeNr=1, firstIndex=0, sNr=0;
	static Gui gui;
	static EditorAPI e;
	static ArrayList shoes = new ArrayList();
	static ArrayList shoeNumbers = new ArrayList();
	GCDocument top, jgSupervisor;

	public FactoryExecutorDEMO()
	{

	}

	public void start(boolean [] sv, Gui g)
	{
		gui = g;
		String[] args = {""};
		
		if(sv[21])
			type=true;
		else 
			type=false;
			

		try
		{
			if(shoeNr==1)
			{
				e = new EditorAPI(args);
				Editor.singleton = e;
				e.removePaletteAction();

				URL url = Supremica.class.getResource("/shoefactory/ShoeFactoryDEMO.xml");
				//top = e.openWorkspace(url.getPath());

				jgSupervisor = e.newWorkspace();
				JgrafSupervisorDEMO js = new JgrafSupervisorDEMO(jgSupervisor,shoeNr);

				shoePlant = new PlantDEMO();
				gui.addProject(shoePlant.getPlant());

				SpecificationDEMO shoeSpec = new SpecificationDEMO(shoeNr,type);
				gui.addProject(shoeSpec.getSpec());

				SyncBuilder syncPlant = new SyncBuilder(gui, shoePlant.getPlant());
				syncPlant.synthesizePlants("theSupervisor");
		
				//-----DEMO - reduce available slots--------
				for(int i=0;i<9;i++)
				{
					boolean b = js.moveInitial("Table0", "Shoe_1put_T0L");
				}
				for(int i=0;i<22;i++)
				{
					boolean b = js.moveInitial("Table1", "Shoe_1put_T1");
					b = js.moveInitial("Table2", "Shoe_1put_T2");
				}
				//------------------------------------------
		
				GCDocument newShoe = e.newWorkspace();
				ShoeDEMO s = new ShoeDEMO(newShoe, type, shoeNr);
				shoes.add(newShoe);
				shoeNumbers.add(new Integer(shoeNr));
				
				newShoe.setSpeed(threadSleepInterval/2);
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
				pauseSFC(10000);
				e.stopWorkspace(jgSupervisor);
				
				shoePlant.add_shoe(shoeNr,true);
				SpecificationDEMO shoeSpec = new SpecificationDEMO(shoeNr,type);
				gui.addProject(shoeSpec.getSpec());

				GCDocument newShoe = e.newWorkspace();
				ShoeDEMO s = new ShoeDEMO(newShoe, type, shoeNr);
				shoes.add(newShoe);
				shoeNumbers.add(new Integer(shoeNr));
				
				shoes.trimToSize();
				shoeNumbers.trimToSize();
				
				remove_Aut(6+shoes.size());

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
	
	public static boolean saveValues(int fi, int s)
	{
		firstIndex = fi;
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
		//e.deleteWorkspace(temp);
		shoePlant.remove_shoe(nr);
		remove_Aut(7+shoeNumbers.indexOf(i));

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

	//A ugly way of pausing Jgrafchart by setting the Thread sleep to a very large number
	public void pauseSFC(int time)
	{
		for(int nr=1; nr<=shoes.size(); nr++)
		{
			GCDocument temp = (GCDocument)shoes.get(nr-1);
			temp.setSpeed(time/2);
		}

		top.setSpeed(time);
		jgSupervisor.setSpeed(time);
	}
	
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
