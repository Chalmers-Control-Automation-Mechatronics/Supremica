
/** OperationBasedSystems.java ***************** */
package org.supremica.testcases;

import java.util.StringTokenizer;
import java.util.TreeSet;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.SynchronizationOptions;


public class OperationBasedSystems
{
    Project project = new Project("Operation-based Systems");
    final String SEPARATOR = "_";
    final String initLabel = "init";
    final String execLabel = "exec";
    final String compLabel = "comp";

    final String upLabel = "U";
    final String downLabel = "D";
    final String OPR_NAME = "O";

    final String OR = "v";
    final String AND = "^";

    public OperationBasedSystems()
    {
        project.addAutomata(createPlants(6));
        try
        {
        project.addAutomata(createSpec("O1","O0_comp^O3_comp"));
        project.addAutomata(createSpec("O2","O1_comp^O5_comp"));
        project.addAutomata(createSpec("O4","O3_comp^O1_comp"));
        project.addAutomata(createSpec("O5","O4_comp"));
        }
        catch(Exception exc)
        {
                System.err.println("Something is wrong...");
        }
    }
    
    public TreeSet<?> generateAllStates()
    {
        TreeSet<?> output = new TreeSet<Object>();
        return output;
        
    }

    public Automata createPlants(int nbrOperations)
    {
            Automaton opr;
            Automata plants = new Automata();
            State[][] states = new State[nbrOperations][3];
            LabeledEvent[][] events = new LabeledEvent[nbrOperations][2];


            for(int i=0; i<nbrOperations;i++)
            {
                    opr = null;
                    opr = new Automaton(OPR_NAME+i);
                    opr.setType(AutomatonType.PLANT);

                    states[i][0] = new State(opr.getName()+SEPARATOR+initLabel);
                    states[i][1] = new State(opr.getName()+SEPARATOR+execLabel);
                    states[i][2] = new State(opr.getName()+SEPARATOR+compLabel);

                    states[i][0].setInitial(true);
                    states[i][2].setAccepting(true);

                    opr.addState(states[i][0]);
                    opr.addState(states[i][1]);
                    opr.addState(states[i][2]);

                    events[i][0] = new LabeledEvent(opr.getName()+upLabel);
            events[i][1] = new LabeledEvent(opr.getName()+downLabel);

            events[i][0].setControllable(true);
            events[i][1].setControllable(false);

            opr.getAlphabet().addEvent(events[i][0]);
            opr.getAlphabet().addEvent(events[i][1]);

            opr.addArc(new Arc(states[i][0], states[i][1], events[i][0]));
            opr.addArc(new Arc(states[i][1], states[i][2], events[i][1]));

            plants.addAutomaton(opr);

            }

            return plants;

    }

    public Automata createSpec(String operation, String restriction) throws Exception
    {
            StringTokenizer stOR = new StringTokenizer(restriction,OR);
            Automata output = new Automata();
            int j=0;


            while(stOR.hasMoreTokens())
            {
                    String currRestriction = stOR.nextToken().trim();
                    StringTokenizer stAND = new StringTokenizer(currRestriction,AND);


                    Automata specAut = new Automata();
                    int nbrTerms = stAND.countTokens();
                    Automaton singleSpec;

                    State[][] states = new State[nbrTerms][3];
                    LabeledEvent[][] events = new LabeledEvent[nbrTerms][2];

                    int i =0;

                    while(stAND.hasMoreTokens())
                    {
                            String currState = stAND.nextToken().trim();
                            singleSpec = null;
                            singleSpec = new Automaton("S"+j+i+operation);
                            singleSpec.setType(AutomatonType.SPECIFICATION);

                            states[i][0] = new State("1");
                            states[i][1] = new State("2");
                            states[i][2] = new State("3");

                            states[i][0].setInitial(true);

                            singleSpec.addState(states[i][0]);
                            singleSpec.addState(states[i][1]);
                            singleSpec.addState(states[i][2]);

                            events[i][0] = new LabeledEvent(convert2Event(currState));
                            events[i][1] = new LabeledEvent(convert2Event(operation+SEPARATOR+execLabel));

                            states[i][1].setName(currState);


                            if(convert2Event(currState).endsWith(upLabel))
                                    events[i][0].setControllable(true);
                            else
                                    events[i][0].setControllable(false);

                            events[i][1].setControllable(true);

                            singleSpec.getAlphabet().addEvent(events[i][0]);
                            singleSpec.getAlphabet().addEvent(events[i][1]);

                            singleSpec.addArc(new Arc(states[i][0], states[i][1], events[i][0]));
                            singleSpec.addArc(new Arc(states[i][1], states[i][2], events[i][1]));

                            specAut.addAutomaton(singleSpec);

                            i++;
                    }
                    SynchronizationOptions options = new SynchronizationOptions();
                    AutomataSynchronizer as = new AutomataSynchronizer(specAut, options);
                    as.execute();

                    output.addAutomaton(as.getAutomaton());

                    j++;
            }

/*		Automaton finalSpec = new Automaton(output.getAutomatonAt(0));
            finalSpec.setName("Spec_"+operation);
            for(int k=1; k<output.nbrOfAutomata;k++)
            {

                    Automaton currAutomaton = new Automaton(output.getAutomatonAt(k));
                    Iterator<Arc> outgoingIt =  currAutomaton.getInitialState().outgoingArcsIterator();
                    while (outgoingIt.hasNext())
                    {
                            State toState = outgoingIt.next().getToState();

                            finalSpec.addArc(new Arc(finalSpec.getInitialState(), toState, events[i][0]));


        }
            }
*/

            return output;

    }



    public String convert2Event(String state)
    {
            StringTokenizer st = new StringTokenizer(state,SEPARATOR);
            String event = st.nextToken();
            if(st.nextToken().equals(compLabel))
                    event += downLabel;
            else
                    event += upLabel;

            return event;
    }


    public Project getProject()
    {
        return project;
    }
}