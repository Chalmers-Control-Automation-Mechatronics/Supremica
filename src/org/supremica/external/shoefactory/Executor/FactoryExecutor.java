package org.supremica.external.shoefactory.Executor;

import org.supremica.external.shoefactory.plantBuilder.*;
import org.supremica.external.shoefactory.Animator.*;
import org.supremica.gui.*;
import grafchart.sfc.*;

import java.net.URL;
import java.util.*;

public class FactoryExecutor
{
	boolean[] stationV;
	static Plant shoePlant;
	static int shoeNr=1;
	static Gui gui;
	static EditorAPI e;
	static ArrayList shoes = new ArrayList ();

	public FactoryExecutor(boolean [] sv, Gui g)
	{
		stationV = sv;
		gui = g;
		String[] args = {""};

		try
		{
			if(shoeNr==1)
			{
				e= new EditorAPI(args);
				Editor.singleton = e;
				Editor.removePaletteAction();

				URL url = Supremica.class.getResource("/shoefactory/ShoeFactory.xml");
				//GCDocument top = e.openWorkspace(url.getPath());

				GCDocument jgSupervisor = e.newWorkspace();
				JgrafSupervisor js = new JgrafSupervisor(jgSupervisor,shoeNr);

				shoePlant = new Plant();
				gui.addProject(shoePlant.getPlant());
				
				Specification shoeSpec = new Specification(shoeNr,sv);
				gui.addProject(shoeSpec.getSpec());

				int[] syncAutomata = {45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0};
				SyncBuilder syncPlant = new SyncBuilder(gui, shoePlant.getPlant(), syncAutomata);
				syncPlant.synthesizePlants("theSupervisor");
				

				GCDocument newShoe = e.newWorkspace();
				Shoe s = new Shoe(newShoe, stationV, shoeNr);
				shoes.add(shoeNr-1,s);
/*
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
*/
				shoeNr++;
			}
			else
			{
				shoePlant.add_shoe(shoeNr);
				Specification shoeSpec = new Specification(shoeNr,sv);
				gui.addProject(shoeSpec.getSpec());

				GCDocument newShoe = e.newWorkspace();
				Shoe s = new Shoe(newShoe, stationV, shoeNr);
				shoes.add(shoeNr-1,newShoe);
				shoeNr++;

				boolean OK = e.compileWorkspace(newShoe);
				if(OK)
				{
					//e.startWorkspace(newShoe);
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println("Error"+shoeNr);
		}
	}

	public static boolean deleteShoe (int nr)
	{
		GCDocument temp = (GCDocument) shoes.get(nr-1);
		e.stopWorkspace(temp);
		//e.deleteWorkspace(temp);
		return true;
	}

	public static int fiveRot (int n)
	{
		if(n>8 || n<3)
			return n;
		else
			return n+6;
	}

	public static int fiveRotdown (int n)
	{
		if(n<9)
			return n;
		else
			return n-6;
	}

}
