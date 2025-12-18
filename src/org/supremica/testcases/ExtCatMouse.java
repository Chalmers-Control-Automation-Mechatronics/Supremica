/*
 * ExtCatMouse.java
 *
 * Created on March 3, 2008, 2:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.testcases;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;

/**
 *
 * @author Sajed
 */
public class ExtCatMouse
{
  final Project project;
  final Automata theAutomata = new Automata();
  final int number_of_floors, number_of_cats;

  // static int MODEL_ID = 4;

	public ExtCatMouse(final String name, final int num, final int num_floors)
	throws Exception
	{
		this(name, num, num_floors, 4, false);
	}

	public ExtCatMouse(final String name, final int num, final int num_floors, final int variantID)
	throws Exception
	{
		this(name, num, num_floors, variantID, false);
	}
  
  public ExtCatMouse(final String name, final int num_cats, final int num_floors, final int MODEL_ID, final boolean use_selfloops)
  throws Exception
  {
    this.project = new Project(name);
    project.setComment("Consider the cat and mouse problem. " +
      "Assume this five room maze is just the first floor of a tower " +
      "composed of n identical levels. A controllable bidirectional passageway " +
      "connects room j of floor 5*i+j to room j of  5*i+j+1 " +
      "(for i = 0, 1, 2, ..., and   j = 1, 2, 3, 4, 5). The first floor is only " +
      "connected with the second, the last floor is only connected with the " +
      "last-but-one. There are initially k cats in room 1 of the first floor " +
      "and k mice in room 5 of the top floor.");
    
    this.number_of_floors = num_floors;
    this.number_of_cats = num_cats;
    
    if(MODEL_ID == 1)
    {
      handleOriginalModel();
    }
    else if(MODEL_ID == 2)
    {
      handleNewModel();
    }
    else if(MODEL_ID == 3)
    {
      handleModel3();
    }
    else if(MODEL_ID == 4)
    {
      handleModel4();
    }
  }

  private void handleOriginalModel()
  throws Exception
  {
    for (int i = 0; i < this.number_of_cats; ++i)
    {
        final Automaton catAutomaton = new Cat(i, this.number_of_floors).getCat();
        final Automaton mouseAutomaton = new Mouse(i, this.number_of_floors).getMouse();
        project.addAutomaton(catAutomaton);
        project.addAutomaton(mouseAutomaton);
        
        theAutomata.addAutomaton(catAutomaton);
        theAutomata.addAutomaton(mouseAutomaton);
    }
    
    final Floor[] floors = new Floor[this.number_of_floors];
    Automata floorAutomata;
    
    for (int i = 0; i < this.number_of_floors; i++)
    {
      floors[i] = new Floor(i, this.number_of_cats, this.number_of_floors);
      floorAutomata = floors[i].buildFloor();
    
      for(int j = 0; j < floorAutomata.nbrOfAutomata(); j++)
      {
          final Automaton a = floorAutomata.getAutomatonAt(j);
          project.addAutomaton(a);
          theAutomata.addAutomaton(a);
      }
    }
  }
    
  private void handleNewModel()
  throws Exception
  {
    final CatBuffer[][] cb = new CatBuffer[this.number_of_floors][5];
    final MouseBuffer[][] mb = new MouseBuffer[this.number_of_floors][5];
    final RoomSpec[][] spec = new RoomSpec[this.number_of_floors][5];
    
    for (int i = 0; i < this.number_of_floors; ++i)
    {
      for (int j = 0; j < 5; ++j)
      {
        cb[i][j] = new CatBuffer(this.number_of_cats, number_of_floors,i+1,j+1);
        mb[i][j] = new MouseBuffer(this.number_of_cats, number_of_floors,i+1,j+1);
        
        project.addAutomaton(cb[i][j].getAutomaton());
        theAutomata.addAutomaton(cb[i][j].getAutomaton());
        
        project.addAutomaton(mb[i][j].getAutomaton());
        theAutomata.addAutomaton(mb[i][j].getAutomaton());
        
        spec[i][j] = new RoomSpec(i+1, j+1, this.number_of_cats, this.number_of_floors,
        cb[i][j].getFEvents(), cb[i][j].getBEvents(),
        mb[i][j].getFEvents(),mb[i][j].getBEvents());
        
        project.addAutomaton(spec[i][j].getAutomaton());
        theAutomata.addAutomaton(spec[i][j].getAutomaton());
      }
    }
  }
    
  private void handleModel3()
  throws Exception
  {
    final ExtCatBuffer[][] ecb = new ExtCatBuffer[this.number_of_floors][5];
    final ExtMouseBuffer[][] emb = new ExtMouseBuffer[this.number_of_floors][5];
    for (int i = 0; i < this.number_of_floors; ++i)
    {
      for (int j = 0; j < 5; ++j)
      {
        ecb[i][j] = new ExtCatBuffer(this.number_of_cats, number_of_floors, i+1, j+1);
        
        project.addAutomaton(ecb[i][j].getAutomaton());
        theAutomata.addAutomaton(ecb[i][j].getAutomaton());
        
        emb[i][j] = new ExtMouseBuffer(this.number_of_cats, number_of_floors, i+1, j+1);
        
        project.addAutomaton(emb[i][j].getAutomaton());
        theAutomata.addAutomaton(emb[i][j].getAutomaton());
      }
    }
  }
    
  private void handleModel4()
  throws Exception
  {
    final CatBuffer[][] cb = new CatBuffer[this.number_of_floors][5];
    final MouseBuffer[][] mb = new MouseBuffer[this.number_of_floors][5];
    final ExtRoomSpec[][] spec = new ExtRoomSpec[this.number_of_floors][5];
    for (int i = 0; i < this.number_of_floors; ++i)
    {
      for (int j = 0; j < 5; ++j)
      {
        cb[i][j] = new CatBuffer(this.number_of_cats, number_of_floors, i+1,j+1);
        mb[i][j] = new MouseBuffer(this.number_of_cats, number_of_floors, i+1,j+1);
        
        spec[i][j] = new ExtRoomSpec(i+1, j+1, this.number_of_cats, this.number_of_floors,
        cb[i][j].getFEvents(), cb[i][j].getBEvents(),
        mb[i][j].getFEvents(), mb[i][j].getBEvents());
        project.addAutomaton(spec[i][j].getAutomaton());
        theAutomata.addAutomaton(spec[i][j].getAutomaton());
      }
    }
  }

  public Automata getAutomata()
  {
    return theAutomata;
  }

  public Project getProject()
  {
    return project;
  }

	// For debugging only
	public static void main(final String[] args)
	{
		try
		{
      final int num_cats = 1;
      final int num_floors = 1;
      final boolean forbiddenSelfloops = false;
      final int variantID = 4;
			final ExtCatMouse cm = new ExtCatMouse("Exte Cat & Mouse", num_cats, num_floors, variantID, forbiddenSelfloops);
			System.out.println(cm.project.toString());
			for(int i = 0; i < cm.project.nbrOfAutomata(); i++)
			{
				final Automaton aut = cm.project.getAutomatonAt(i);
				System.out.println(aut.toDebugString());
			}
		}
		catch(final Exception excp)
		{
			excp.printStackTrace();
		}
	}
}
