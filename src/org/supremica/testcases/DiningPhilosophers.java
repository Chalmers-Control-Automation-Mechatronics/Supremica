
/** DiningPhilosophers.java ***************** */
package org.supremica.testcases;

import java.util.Iterator;
import org.omg.CORBA.portable.IDLEntity;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.Project;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.execution.*;
import org.supremica.log.*;
import uk.ac.ic.doc.scenebeans.Null;

// Builds a Philo automaton
class EatingPhilosopher
{
    private static Logger logger = LoggerFactory.createLogger(EatingPhilosopher.class);
    public final String PHILO_NAME = "Philo";
    
    static int nbrOfStates = 7;
    static State[] states = new State[nbrOfStates];
    // indices into states[]
    final static int INIT = 0;
    final static int L_UP = 1;
    final static int R_UP = 2;
    final static int READY = 3;
    final static int L_DN = 4;
    final static int R_DN = 5;
    final static int EAT = 6;
    
    static int number_of_events = 6;
    static LabeledEvent[] events = new LabeledEvent[number_of_events];

    // indicies into events[]
    final static int L_TAKE = 0;
    final static int R_TAKE = 1;
    final static int L_PUT = 2;
    final static int R_PUT = 3;
    final static int START_EATING = 4;
    final static int INTERM_EVENT = 5;
    final static String LABEL_SEP = "_";
    
    // note, must be the same in both Philosopher and Fork
    final static String NAME_SEP = ":";
    
    // Need not be the same everywhere
    static Automaton philo = null;
    
    static boolean inited = false;
    
    int length;
    
    public EatingPhilosopher()
    {
         states[0] = new State("think");
         states[1] = new State("lu");
         states[2] = new State("ru");
         states[3] = new State("ready");
         states[4] = new State("ld");
         states[5] = new State("rd");
         states[6] = new State("eat");

         events[0] = new LabeledEvent("L_take");    // pick up left
         events[1] = new LabeledEvent("R_take");    // pick up right
         events[2] = new LabeledEvent("L_put");    // put down left
         events[3] = new LabeledEvent("R_put");    // put down right
         events[4] = new LabeledEvent("Start_eating");
         events[5] = new LabeledEvent("Interm_event");
        
    }
    public EatingPhilosopher(boolean l_take, boolean r_take, boolean l_put, boolean r_put)
    throws Exception
    {       
        this();
        // Here we create the "template" automaton, philo
        philo = new Automaton("Philo template");
        philo.setType(AutomatonType.PLANT);
        
        // These are fivestate project
        states[0].setInitial(true);
        states[0].setAccepting(true);
        for (int i = 0; i < states.length; ++i)
        {
            philo.addState(states[i]);
        }
        
        // Now the events, these should be (re)named uniquely for each philosopher
        // (each fork-pair, actually)
          
        events[L_TAKE].setControllable(l_take);
        events[R_TAKE].setControllable(r_take);
        events[L_PUT].setControllable(l_put);
        events[R_PUT].setControllable(r_put);
        events[INTERM_EVENT].setControllable(true);
        events[START_EATING].setControllable(true);
        
        for (int i = 0; i < events.length; ++i)
            philo.getAlphabet().addEvent(events[i]);
        
        // And finally the arcs - first the left side (where the left is picked up
        // and put down first)
        philo.addArc(new Arc(states[INIT], states[L_UP], events[L_TAKE]));
        philo.addArc(new Arc(states[L_UP], states[READY], events[R_TAKE]));
        philo.addArc(new Arc(states[READY], states[EAT], events[START_EATING]));
        philo.addArc(new Arc(states[EAT], states[L_DN], events[L_PUT]));
        philo.addArc(new Arc(states[L_DN], states[INIT], events[R_PUT]));
        
        // And then the right side (where the right fork is picked up and put down first)
        philo.addArc(new Arc(states[INIT], states[R_UP], events[R_TAKE]));
        philo.addArc(new Arc(states[R_UP], states[READY], events[L_TAKE]));
        philo.addArc(new Arc(states[EAT], states[R_DN], events[R_PUT]));
        philo.addArc(new Arc(states[R_DN], states[INIT], events[L_PUT]));
        
        inited = true;
    }
    
    public Automaton getPhilo()
    {
        return philo;
    }
    
    // Fake renaming, must replace the event due to immutability
    public void renameEvent(Automaton sm, int ev_index, final String new_label)
    {
        Alphabet alpha = sm.getAlphabet();
        LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        LabeledEvent ev_new = new LabeledEvent(ev_old, new_label);
        sm.replaceEvent(ev_old, ev_new);        
    }
    
    public Automaton build(boolean i_l_take, Automaton spec_philo, int id, int l_fork, int r_fork)
    throws Exception
    {
        //logger.info(philo.getAlphabet().toDebugString());
        //AutomataToXML builder = new AutomataToXML(philo);
        //logger.debug(builder.serialize());
        // deep copy, I hope
        
        Automaton sm = new Automaton(spec_philo);
        sm.setName(PHILO_NAME + NAME_SEP + pad(id));
        
        // adjust the event names according to l_fork and r_fork
        // L_take becomes take<id>.<l_fork>
        // R_take becomes take<id>.<r_fork>
        // L_put becomes put<id>.<l_fork>
        // R_put becomes put<id>.<r_fork>
        renameEvent(sm, L_TAKE, "take" + pad(id)+ LABEL_SEP +pad(l_fork));
        renameEvent(sm, R_TAKE, "take" + pad(id)+ LABEL_SEP +pad(r_fork));
        renameEvent(sm, L_PUT, "put" + pad(id)+ LABEL_SEP +pad(l_fork));
        renameEvent(sm, R_PUT, "put" + pad(id)+ LABEL_SEP +pad(r_fork));
        
        renameEvent(sm, START_EATING, "start_eating" + pad(id));
        renameEvent(sm, INTERM_EVENT, "intermediate" + pad(id));
        
        if(i_l_take)
        {   
            if((id%2)==0)
            {
                sm.getAlphabet().getEvent("take" + id + LABEL_SEP + id).setControllable(false);
            }
        }
        
        // Used Automaton::replaceEvent, so no need to rehash
        // // must rehash since we've changed the label (that's the way it works (unfortunately))
        // alpha.rehash();
        //AutomataToXML builder = new AutomataToXML(sm);
        //logger.debug(builder.serialize());
        return sm;
    }
    
    public String pad(int num)
    {
        String returnValue = "" + num;
//        while (returnValue.length() < length)
//            returnValue = "0" + returnValue;
        return returnValue;
    }
    
    public void fixAnimation(Automaton currPhil, int id, int nextId, Actions currActions, Controls currControls)
    throws Exception
    {
        Alphabet alpha = currPhil.getAlphabet();
        //throw new Exception("Event labels are messed up!");
        //LabeledEvent lTake = alpha.getEvent(events[L_TAKE].getLabel());
        //LabeledEvent rTake = alpha.getEvent(events[R_TAKE].getLabel());
        //LabeledEvent lPut = alpha.getEvent(events[L_PUT].getLabel());
        //LabeledEvent rPut = alpha.getEvent(events[R_PUT].getLabel());
        //LabeledEvent startEating = alpha.getEvent(events[START_EATING].getLabel());
        LabeledEvent lTake = alpha.getEvent("take" + pad(id) + LABEL_SEP + pad(id));
        LabeledEvent rTake = alpha.getEvent("take" + pad(id) + LABEL_SEP + pad(nextId));
        LabeledEvent lPut = alpha.getEvent("put" + pad(id) + LABEL_SEP + pad(id));
        LabeledEvent rPut = alpha.getEvent("put" + pad(id) + LABEL_SEP + pad(nextId));
        LabeledEvent startEating = alpha.getEvent("start_eating" + pad(id));
        
        //              Actions currActions = project.getActions();
        //              Controls currControls = project.getControls();
        // The forks in the animation are numbered 0 to nbr of forks - 1
        Action lTakeAction = new Action(lTake.getLabel());
        
        currActions.addAction(lTakeAction);
        lTakeAction.addCommand(new Command("fork." + id + ".get"));
        lTakeAction.addCommand(new Command("phil." + id + ".leftfork"));
        
        Action rTakeAction = new Action(rTake.getLabel());
        
        currActions.addAction(rTakeAction);
        rTakeAction.addCommand(new Command("fork." + nextId + ".get"));
        rTakeAction.addCommand(new Command("phil." + id + ".rightfork"));
        
        Action lPutAction = new Action(lPut.getLabel());
        
        currActions.addAction(lPutAction);
        lPutAction.addCommand(new Command("fork." + id + ".put"));
        lPutAction.addCommand(new Command("phil." + id + ".thinking.begin"));
        
        Control lPutControl = new Control(lPut.getLabel());
        
        currControls.addControl(lPutControl);
        lPutControl.addCondition(new Condition("phil." + id + ".eating.end"));
        
        Action rPutAction = new Action(rPut.getLabel());
        
        currActions.addAction(rPutAction);
        rPutAction.addCommand(new Command("fork." + nextId + ".put"));
        rPutAction.addCommand(new Command("phil." + id + ".thinking.begin"));
        
        Control rPutControl = new Control(rPut.getLabel());
        
        currControls.addControl(rPutControl);
        rPutControl.addCondition(new Condition("phil." + id + ".eating.end"));
        
        Action startEatingAction = new Action(startEating.getLabel());
        
        currActions.addAction(startEatingAction);
        startEatingAction.addCommand(new Command("phil." + id + ".eating.begin"));
    }
}

interface ChopstickBuilder
{
    public Automaton build(boolean i_l_take, int id, int l_philo, int r_philo)
    throws Exception;
}

// Builds a chopstick automaton
class Chopstick
    implements ChopstickBuilder
{
    private final String FORK_NAME = "Fork";

    static State[] states = new State[2];
    
    static LabeledEvent[] events = new LabeledEvent[4];
    
    static Arc[] arcs = new Arc[4];
    
    final static int L_TAKE = 0;
    final static int R_TAKE = 1;
    final static int L_PUT = 2;
    final static int R_PUT = 3;
    final static String LABEL_SEP = "_";
    
    // note, must be the same in both Philosopher and Fork
    final static String NAME_SEP = ":";
    
    // Need not be the same everywhere
    static Automaton fork = null;
    static Automaton memoryfork = null;
    static boolean inited = false;
    private int length;
    
    public Chopstick()
    {
        states[0] = new State("0");
        states[1] = new State("1");
        
	events[0] = new LabeledEvent("L_up");
        events[1] = new LabeledEvent("R_up");
        events[2] =new LabeledEvent("L_dn");
        events[3] =new LabeledEvent("R_dn");

	arcs[0] = new Arc(states[0], states[1], events[0]);
        arcs[1] = new Arc(states[1], states[0], events[2]);
        arcs[2] = new Arc(states[0], states[1], events[1]);
        arcs[3] = new Arc(states[1], states[0], events[3]);
    }
    
    public Chopstick(boolean l_take, boolean r_take, boolean l_put, boolean r_put, int length)
    throws Exception
    {
        this();
     
        fork = new Automaton("Fork template");
        fork.setType(AutomatonType.SPECIFICATION);
        
        // First the states
        states[0].setInitial(true);
        states[0].setAccepting(true);
        for (int i = 0; i < states.length; ++i)
        {
            fork.addState(states[i]);
        }
        
        // Now the events
        events[L_TAKE].setControllable(l_take);
        events[R_TAKE].setControllable(r_take);
        events[L_PUT].setControllable(l_put);
        events[R_PUT].setControllable(r_put);
        for (int i = 0; i < events.length; ++i)
        {
            fork.getAlphabet().addEvent(events[i]);
        }
        
        // And finally the arcs
        for (int i = 0; i < arcs.length; ++i)
        {
            fork.addArc(arcs[i]);
        }
        
        inited = true;
    }
    
    // Fake renaming, must replace the event due to immutability
    private void renameEvent(Automaton sm, int ev_index, final String new_label)
    {
        Alphabet alpha = sm.getAlphabet();
        LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        LabeledEvent ev_new = new LabeledEvent(new_label);
//        System.out.println(""+ev_new.isControllable());
        sm.replaceEvent(ev_old, ev_new);
        
    }
    
    public Automaton build(boolean i_l_take, int id, int l_philo, int r_philo)
    throws Exception
    {
        Automaton sm = new Automaton(fork);
        
        // deep copy, I hope
        sm.setName(FORK_NAME + NAME_SEP + pad(id));
        
        // Alphabet alpha = sm.getAlphabet();
        
        renameEvent(sm, L_TAKE, "take" + pad(l_philo) + LABEL_SEP + pad(id));
        renameEvent(sm, R_TAKE, "take" + pad(r_philo) + LABEL_SEP + pad(id));
        renameEvent(sm, L_PUT, "put" + pad(l_philo) + LABEL_SEP + pad(id));
        renameEvent(sm, R_PUT, "put" + pad(r_philo) + LABEL_SEP + pad(id));
        
        if(i_l_take)
        {   
            if((id%2)==0)
            {        
                sm.getAlphabet().getEvent("take" + id + LABEL_SEP + id).setControllable(false);
            }
        }
        
        // alpha.getEvent(events[L_TAKE].getLabel()).setLabel("take" + l_philo + LABEL_SEP + id);
        // alpha.getEvent(events[R_TAKE].getLabel()).setLabel("take" + r_philo + LABEL_SEP + id);
        // alpha.getEvent(events[L_PUT].getLabel()).setLabel("put" + l_philo + LABEL_SEP + id);
        // alpha.getEvent(events[R_PUT].getLabel()).setLabel("put" + r_philo + LABEL_SEP + id);
        
        // // must rehash since we've changed the label (that's the way it works)
        // alpha.rehash();
        
        return sm;
    }
        
    private String pad(int num)
    {
        String returnValue = "" + num;
//        while (returnValue.length() < length)
//            returnValue = "0" + returnValue;
        return returnValue;
    }
}

// Builds a chopstick automaton
class MemoryChopstick
    implements ChopstickBuilder
{
    private final String FORK_NAME = "Fork";

    static State[] states = { new State("0"),
    new State("1"),
    new State("2") };
    static LabeledEvent[] events = { new LabeledEvent("L_up"),
    new LabeledEvent("R_up"),
    new LabeledEvent("L_dn"),
    new LabeledEvent("R_dn") };
    static Arc[] arcs = { new Arc(states[0], states[1], events[0]),
    new Arc(states[1], states[0], events[2]),
    new Arc(states[0], states[2], events[1]),
    new Arc(states[2], states[0], events[3]) };
    
    final static int L_TAKE = 0;
    final static int R_TAKE = 1;
    final static int L_PUT = 2;
    final static int R_PUT = 3;
    final static String LABEL_SEP = "_";
    final static String EVENT_SEP = ".";
    
    // note, must be the same in both Philosopher and Fork
    final static String NAME_SEP = ":";
    
    // Need not be the same everywhere
    static Automaton fork = null;
    static boolean inited = false;
    int length;
    
    public MemoryChopstick(boolean l_take, boolean r_take, boolean l_put, boolean r_put, int length)
    throws Exception
    {
        this.length = length;

        if (inited)
        {
            // The only thing that may need to be changed is the controllability
            Alphabet alpha = fork.getAlphabet();
            alpha.getEvent(events[L_TAKE].getLabel()).setControllable(l_take);
            alpha.getEvent(events[R_TAKE].getLabel()).setControllable(r_take);
            alpha.getEvent(events[L_PUT].getLabel()).setControllable(l_put);
            alpha.getEvent(events[R_PUT].getLabel()).setControllable(r_put);
            return;
        }
        
        fork = new Automaton("Fork template");
        fork.setType(AutomatonType.SPECIFICATION);
        
        // First the states
        states[0].setInitial(true);
        states[0].setAccepting(true);
        for (int i = 0; i < states.length; ++i)
        {
            fork.addState(states[i]);
        }
        
        // Now the events
        events[L_TAKE].setControllable(l_take);
        events[R_TAKE].setControllable(r_take);
        events[L_PUT].setControllable(l_put);
        events[R_PUT].setControllable(r_put);
        for (int i = 0; i < events.length; ++i)
        {
            fork.getAlphabet().addEvent(events[i]);
        }
        
        // And finally the arcs
        for (int i = 0; i < arcs.length; ++i)
        {
            fork.addArc(arcs[i]);
        }
        
        inited = true;
    }
    
    // Fake renaming, must replace the event due to immutability
    private void renameEvent(Automaton sm, int ev_index, final String new_label)
    {
        Alphabet alpha = sm.getAlphabet();
        LabeledEvent ev_old = alpha.getEvent(events[ev_index].getLabel());
        LabeledEvent ev_new = new LabeledEvent(new_label);
        sm.replaceEvent(ev_old, ev_new);
        
    }
    
    public Automaton build(boolean i_l_take, int id, int l_philo, int r_philo)
    throws Exception
    {
        Automaton sm = new Automaton(fork);
        
        // deep copy, I hope
        sm.setName(FORK_NAME + NAME_SEP + pad(id));
        
        // Alphabet alpha = sm.getAlphabet();
        
        renameEvent(sm, L_TAKE, "take" + pad(l_philo) + LABEL_SEP + pad(id));
        renameEvent(sm, R_TAKE, "take" + pad(r_philo) + LABEL_SEP + pad(id));
        renameEvent(sm, L_PUT, "put" + pad(l_philo) + LABEL_SEP + pad(id));
        renameEvent(sm, R_PUT, "put" + pad(r_philo) + LABEL_SEP + pad(id));
        
        // alpha.getEvent(events[L_TAKE].getLabel()).setLabel("take" + l_philo + LABEL_SEP + id);
        // alpha.getEvent(events[R_TAKE].getLabel()).setLabel("take" + r_philo + LABEL_SEP + id);
        // alpha.getEvent(events[L_PUT].getLabel()).setLabel("put" + l_philo + LABEL_SEP + id);
        // alpha.getEvent(events[R_PUT].getLabel()).setLabel("put" + r_philo + LABEL_SEP + id);
        
        // // must rehash since we've changed the label (that's the way it works)
        // alpha.rehash();
        
        return sm;
    }
    
    private String pad(int num)
    {
        String returnValue = "" + num;
//        while (returnValue.length() < length)
//            returnValue = "0" + returnValue;
        return returnValue;
    }
}

public class DiningPhilosophers
{
    Project project = new Project("Dining philosophers");
    
    // These are helpers for counting modulo num philos/forks
    // Note that we adjust for 0's, indices are from 1 to modulo
    int nextId(int id, int modulo)
    {
        int nxt = id + 1;
        
        if (nxt > modulo)
        {
            return nxt - modulo;
        }
        else
        {
            return nxt;
        }
    }
    
    int prevId(int id, int modulo)
    {
        int nxt = id - 1;
        
        if (nxt <= 0)
        {
            return modulo;
        }
        else
        {
            return nxt;
        }
    }
    public DiningPhilosophers(){}
    
    public DiningPhilosophers(int num, boolean l_take, boolean r_take,
        boolean l_put, boolean r_put, boolean animation, boolean forkmemory)
        throws Exception
    {
        // Add comment
        project.setComment("The classical dining philosophers problem, here with " + num + " philosophers.");
        
        int idLength = ("" + num).length();
        
        // Next the forks aka chopsticks
        ChopstickBuilder fork;
        if (forkmemory)
        {
            fork = new MemoryChopstick(l_take, r_take, l_put, r_put, idLength);
        }
        else
        {
            fork = new Chopstick(l_take, r_take, l_put, r_put, idLength);
        }
        for (int i = 0; i < num; ++i)
        {
            int id = i + 1;
            
            // id's are from 1...n
            project.addAutomaton(fork.build(false, id, prevId(id, num), id));
            
            // To its right a fork has philo #id, and to its left philo #id-1
        }
        
        // First the philosphers
        EatingPhilosopher philo = new EatingPhilosopher(l_take, r_take, l_put, r_put);
        
        for (int i = 0; i < num; ++i)
        {
            int id = i + 1;
            
            Automaton currPhil = philo.build(false,philo.getPhilo(), id, id, nextId(id, num));
            
//            Automaton currPhil = new EatingPhilosopher(l_take, r_take, l_put, r_put, id, id, nextId(id, num)).getPhilo();
            
            // id's are from 1...n
            project.addAutomaton(currPhil);
            
            // To his right a philo has fork #id, and to his left is fork #id-1
/*            if (animation)
            {
                // Needs to be fixed...
                philo.fixAnimation(currPhil, id, nextId(id, num), project.getActions(), project.getControls());
            }
 */
        }
        
        
        if (animation)
        {
            project.setAnimationURL(DiningPhilosophers.class.getResource("/scenebeans/mageekramer/xml/diners.xml"));
        }
    }
    
    public Project getProject()
    {
        return project;
    }
}
