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
class Level extends Room
{
//    private static Logger logger = LoggerFactory.createLogger(ExtCatMouse.class);
    private final Automata level;
    final String LEVEL_NAME = "L";

    public Level()
    {
        level = new Automata();
    }

    public Level(final int id, final int num, final int num_levels) throws Exception
    {
       this();
       this.id = id;

       for (int i = 0; i < CatMouse.NUMBER_OF_ROOMS; ++i)
       {
           final Room room = new Room(i,num);
           final Automaton roomAuto = room.build(room.getAutomaton(), id,num_levels);
           level.addAutomaton(roomAuto);
       }
    }

    public Automata buildLevel()
		throws Exception
    {
        final Automata sm = new Automata(level);
        final Automaton[] r = new Automaton[CatMouse.NUMBER_OF_ROOMS];

        for(int i = 0; i < CatMouse.NUMBER_OF_ROOMS; i++)
        {
            r[i] = sm.getAutomatonAt(i);
            sm.renameAutomaton(r[i], LEVEL_NAME + id + NAME_SEP + r[i].getName());
        }

        return sm;
    }

    public Automata getAutomata()
    {
        return level;
    }
}

public class ExtCatMouse
{
    final Project project; // = new Project("Extended Cat Mouse");
    final Automata theAutomata = new Automata();
    final int number_of_levels, number_of_cats;

    static int MODEL_ID = 4;

	public ExtCatMouse(final String name, final int num, final int num_levels)
		throws Exception
	{
		this(name, num, num_levels, false);
	}

    public ExtCatMouse(final String name, final int num, final int num_levels, final boolean use_selfloops)
        throws Exception
    {
		this.project = new Project(name);
        // Add comment
        project.setComment("Consider the cat and mouse problem. Assume this five rooms maze is just the first level of a tower composed by n identical levels. A controllable bidirectional passageway connects room j of level 5*i+j to room j of  5*i+j+1 (for i = 0, 1, 2   ..., and   j = 1, 2, 3, 4, 5). The first level is only connected with the second, the last level is only connected with the last-but-one. There are initially k cats in room 1 of the first level and k mice in room 5 of the last level.");

        number_of_levels = num_levels;
        number_of_cats = num;

        if(MODEL_ID == 1)
        {
            for (int i = 0; i < num; ++i)
			{
                final Automaton catAutomaton = new Cat(i,num_levels).getCat();
                final Automaton mouseAutomaton = new Mouse(i,num_levels).getMouse();
                project.addAutomaton(catAutomaton);
                project.addAutomaton(mouseAutomaton);

                theAutomata.addAutomaton(catAutomaton);
                theAutomata.addAutomaton(mouseAutomaton);
            }

            final Level[] levels = new Level[num_levels];
            Automata l;

            for (int i = 0; i < num_levels; i++)
            {
                levels[i] = new Level(i,num,num_levels);

                l = levels[i].buildLevel();
                for(int j = 0; j < l.nbrOfAutomata(); j++)
                {
                    final Automaton a = l.getAutomatonAt(j);
                    project.addAutomaton(a);
                    theAutomata.addAutomaton(a);
                }
            }
        }

        //THE NEW MODEL
        else if(MODEL_ID == 2)
        {
            final CatBuffer[][] cb = new CatBuffer[num_levels][5];
            final MouseBuffer[][] mb = new MouseBuffer[num_levels][5];
            final RoomSpec[][] spec = new RoomSpec[num_levels][5];
            for (int i = 0; i < num_levels; ++i)
            {
                for (int j = 0; j < 5; ++j)
                {
                    cb[i][j] = new CatBuffer(num,number_of_levels,i+1,j+1);
                    mb[i][j] = new MouseBuffer(num,number_of_levels,i+1,j+1);

                    project.addAutomaton(cb[i][j].getAutomaton());
                    theAutomata.addAutomaton(cb[i][j].getAutomaton());

                    project.addAutomaton(mb[i][j].getAutomaton());
                    theAutomata.addAutomaton(mb[i][j].getAutomaton());

                    spec[i][j] = new RoomSpec(i+1,j+1,number_of_cats, num_levels, cb[i][j].getFEvents(),cb[i][j].getBEvents(),mb[i][j].getFEvents(),mb[i][j].getBEvents());                    project.addAutomaton(spec[i][j].getAutomaton());
                    theAutomata.addAutomaton(spec[i][j].getAutomaton());
                }
            }
        }
        else if(MODEL_ID == 3)
        {
            final ExtCatBuffer[][] ecb = new ExtCatBuffer[num_levels][5];
            final ExtMouseBuffer[][] emb = new ExtMouseBuffer[num_levels][5];
            for (int i = 0; i < num_levels; ++i)
            {
                for (int j = 0; j < 5; ++j)
                {
                    ecb[i][j] = new ExtCatBuffer(num,number_of_levels,i+1,j+1);

                    project.addAutomaton(ecb[i][j].getAutomaton());
                    theAutomata.addAutomaton(ecb[i][j].getAutomaton());

                    emb[i][j] = new ExtMouseBuffer(num,number_of_levels,i+1,j+1);

                    project.addAutomaton(emb[i][j].getAutomaton());
                    theAutomata.addAutomaton(emb[i][j].getAutomaton());
                }
            }
        }
        else if(MODEL_ID == 4)
        {
            final CatBuffer[][] cb = new CatBuffer[num_levels][5];
            final MouseBuffer[][] mb = new MouseBuffer[num_levels][5];
            final ExtRoomSpec[][] spec = new ExtRoomSpec[num_levels][5];
            for (int i = 0; i < num_levels; ++i)
            {
                for (int j = 0; j < 5; ++j)
                {
                    cb[i][j] = new CatBuffer(num,number_of_levels,i+1,j+1);
                    mb[i][j] = new MouseBuffer(num,number_of_levels,i+1,j+1);

                    spec[i][j] = new ExtRoomSpec(i+1,j+1,number_of_cats, num_levels, cb[i][j].getFEvents(),cb[i][j].getBEvents(),mb[i][j].getFEvents(),mb[i][j].getBEvents());
                    project.addAutomaton(spec[i][j].getAutomaton());
                    theAutomata.addAutomaton(spec[i][j].getAutomaton());
                }
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
			final ExtCatMouse cm = new ExtCatMouse("Exte Cat & Mouse", 1, 1, true);
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
