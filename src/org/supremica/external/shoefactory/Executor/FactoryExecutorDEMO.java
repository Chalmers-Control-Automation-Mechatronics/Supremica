package org.supremica.external.shoefactory.Executor;

import org.supremica.external.shoefactory.plantBuilder.*;
import org.supremica.external.shoefactory.Animator.*;
import org.supremica.gui.*;
import grafchart.sfc.*;
import java.net.URL;
import java.util.*;

public class FactoryExecutorDEMO
{
	boolean[] stationV;
	static PlantDEMO shoePlant;
	static int shoeNr=1;
	static Gui gui;
	static EditorAPI e;
	static ArrayList shoes = new ArrayList ();
	boolean OK;

	public FactoryExecutorDEMO(boolean [] sv, Gui g)
	{
		stationV = sv;
		gui = g;
		String[] args = {""};

		try
		{
				e= new EditorAPI(args);
				Editor.singleton = e;
				Editor.removePaletteAction();

				URL url = Supremica.class.getResource("/shoefactory/ShoeFactory.xml");
				//GCDocument top = e.openWorkspace(url.getPath());

				shoePlant = new PlantDEMO();
				gui.addProject(shoePlant.getPlant());

				SpecificationDEMO shoeSpec1 = new SpecificationDEMO(1,true);
				gui.addProject(shoeSpec1.getSpec());
				SpecificationDEMO shoeSpec2 = new SpecificationDEMO(2,true);
				gui.addProject(shoeSpec2.getSpec());

				GCDocument jgSupervisor = e.newWorkspace();
				JgrafSupervisorDEMO js = new JgrafSupervisorDEMO(jgSupervisor);
				int[] synthAutomata = {0,1,2,3,4,5,6,7,8};
				SyncBuilder synthPlant = new SyncBuilder(gui, shoePlant.getPlant(), synthAutomata);
				synthPlant.synthesizePlants("theSupervisor");
				synthPlant.synchronizePlants("onlySynchronized");

				GCDocument newShoe1 = e.newWorkspace();
				ShoeDEMO s1 = new ShoeDEMO(newShoe1, stationV, 1);
				shoes.add(0,newShoe1);

				GCDocument newShoe2 = e.newWorkspace();
				ShoeDEMO s2 = new ShoeDEMO(newShoe2, stationV, 2);
				shoes.add(1,newShoe2);
/*openWorkspace fungerar ej
				OK = e.compileWorkspace(top);

				if(OK)
				{
					e.startWorkspace(top);
				}

				OK = e.compileWorkspace(jgSupervisor);
				if(OK)
				{
					e.startWorkspace(jgSupervisor);
				}

				OK = e.compileWorkspace((GCDocument) shoes.get(0));
				if(OK)
				{
					//e.startWorkspace((GCDocument) shoes.get(0));
				}

				OK = e.compileWorkspace((GCDocument) shoes.get(1));
				if(OK)
				{
					//e.startWorkspace((GCDocument) shoes.get(1));
				}
	*/			shoeNr++;
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
}
