/*
 * ExtCatMouse.java
 *
 * Created on March 3, 2008, 2:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.testcases;

import java.util.StringTokenizer;
import org.omg.CORBA.portable.IDLEntity;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.Project;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.StateSet;
import org.supremica.automata.execution.*;
import uk.ac.ic.doc.scenebeans.Null;
import org.supremica.log.*;


/**
 *
 * @author Sajed
 */
class Level extends Room
{
//    private static Logger logger = LoggerFactory.createLogger(ExtCatMouse.class);
    Automata level;;
    final String LEVEL_NAME = "L";
    int numor = new CatMouse().number_of_rooms;
    int id;
    
    public Level()
    {
        level = new Automata();
    }
    
    public Level(int id, int num, int num_levels) throws Exception
    {
       this();
       
       this.id = id;
     
        Room room;
        for (int i = 0; i < numor; ++i)
        {
            room = new Room(i,num);
            Automaton roomAuto = room.build(room.getAutomaton(), id,num_levels);

            level.addAutomaton(roomAuto);
        }
    }
    
    public Automata buildLevel()
    throws Exception
    {
        Automata sm = new Automata(level);
        Automaton[] r = new Automaton[numor];
        
        for(int i=0;i<numor;i++)
        {
            r[i] = sm.getAutomatonAt(i);
            sm.renameAutomaton(r[i],LEVEL_NAME+id+NAME_SEP+r[i].getName());
        }
        
        return sm;
    }
    
    public Automata getAutomata()
    {
        return level;
    }
}

public class ExtCatMouse{
    
    Project project = new Project("Extended Cat Mouse");
    Automata theAutomata = new Automata();
    int number_of_levels, number_of_cats;
    
    int MODEL_ID = 4;
    
    /** Creates a new instance of ExtCatMouse */
    public ExtCatMouse() {}
    
    public ExtCatMouse(int num, int num_levels)
        throws Exception
    {
        // Add comment
        project.setComment("Consider the cat and mouse problem. Assume this five rooms maze is just the first level of a tower composed by n identical levels. A controllable bidirectional passageway connects room j of level 5*i+j to room j of  5*i+j+1 (for i = 0, 1, 2   ..., and   j = 1, 2, 3, 4, 5). The first level is only connected with the second, the last level is only connected with the last-but-one. There are initially k cats in room 1 of the first level and k mice in room 5 of the last level.");
        
        number_of_levels = num_levels;
        number_of_cats = num;
       
        if(MODEL_ID == 1)
        {
            for (int i = 0; i < num; ++i){
                Automaton catAutomaton = new Cat(i,num_levels).getCat();
                Automaton mouseAutomaton = new Mouse(i,num_levels).getMouse();
                project.addAutomaton(catAutomaton);
                project.addAutomaton(mouseAutomaton);

                theAutomata.addAutomaton(catAutomaton);
                theAutomata.addAutomaton(mouseAutomaton);
            }

            Level[] levels = new Level[num_levels];
            Automata l;

            for (int i = 0; i < num_levels; i++)
            {
                levels[i] = new Level(i,num,num_levels);

                l = levels[i].buildLevel();
                for(int j=0;j<l.nbrOfAutomata();j++)
                {
                    Automaton a = l.getAutomatonAt(j);

                    project.addAutomaton(a);
                    theAutomata.addAutomaton(a);
                }
            }
        }
        
        //THE NEW MODEL
        else if(MODEL_ID == 2)
        {
            CatBuffer[][] cb = new CatBuffer[num_levels][5];
            MouseBuffer[][] mb = new MouseBuffer[num_levels][5];
            RoomSpec[][] spec = new RoomSpec[num_levels][5];
            int init_id;
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
            ExtCatBuffer[][] ecb = new ExtCatBuffer[num_levels][5];
            ExtMouseBuffer[][] emb = new ExtMouseBuffer[num_levels][5];
            int init_id;
            
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
            CatBuffer[][] cb = new CatBuffer[num_levels][5];
            MouseBuffer[][] mb = new MouseBuffer[num_levels][5];
            ExtRoomSpec[][] spec = new ExtRoomSpec[num_levels][5];
            int init_id;
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
    
}
