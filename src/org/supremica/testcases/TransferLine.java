
/**
 * TransferLine.java
 *
 * "industrial" productions systems transfer line.
 * See Wonhams lecture notes, Sec 4.6
 *
 *   /Arash
 */
package org.supremica.testcases;

import org.supremica.automata.*;

public class TransferLine
    extends Automata
{
    private static final long serialVersionUID = 1L;

    private Project project;
    private LabeledEvent[] events_vector;
    private boolean sanchez_models;
    private int length;
    
    public TransferLine(int cells, int cap1, int cap2, boolean sanchez_models)
    {
        length = ("" + cells).length();
        
        this.sanchez_models = sanchez_models;
        project = new Project("Transfer line");
        if (!sanchez_models)
        {
            project.setComment("This testcase is based on the Transfer Line " +
                "example from 'Notes on Control of Discrete-Event Systems' " +
                "my W.M Wonham. Here with " + cells + " serially connected, " +
                "identical cells.");
        }
        else
        {
            project.setComment("This testcase has some resemblance to the Transfer Line " +
                "example from 'Notes on Control of Discrete-Event Systems' " +
                "my W.M Wonham. The main difference being that, strangely " +
                "enough, the different cells are not connected.");
        }
        
        // create those shared events between the cells:
        if (!sanchez_models)
        {
            events_vector = new LabeledEvent[cells + 1];
            
            for (int i = 0; i <= cells; i++)
            {
                if (i == 0)
                {
                    events_vector[i] = new LabeledEvent("start");
                }
                else if (i == cells)
                {
                    events_vector[i] = new LabeledEvent("finish");
                }
                else
                {
                    events_vector[i] = new LabeledEvent("transfer_" + i);
                }
                
                if (i > 0)
                {
                    events_vector[i].setControllable(false);
                }
            }
        }
        
        for (int i = 0; i < cells; i++)
        {
            createCell(i, cap1, cap2);
        }
    }
    
    public Project getProject()
    {
        return project;
    }
    
    /* --------------------------------------------- */
    private void createCell(int i, int cap1, int cap2)
    {
        String idString = "" + (i+1);
        while (idString.length() < length)
            idString = "0" + idString;                
        Automaton m1 = new Automaton("M1_" + idString);
        Automaton m2 = new Automaton("M2_" + idString);
        Automaton tu = new Automaton("TU_" + idString);
        LabeledEvent e2 = new LabeledEvent("M1_" + idString + "_finished");
        LabeledEvent e3 = new LabeledEvent("M2_" + idString + "_started");
        LabeledEvent e4 = new LabeledEvent("M2_" + idString + "_finished");
        LabeledEvent e5 = new LabeledEvent("TU_" + idString + "_take");
        LabeledEvent e8 = new LabeledEvent("TU_" + idString + "_reject");
        LabeledEvent e1, e6;
        
        if (sanchez_models)
        {
            e1 = new LabeledEvent("start_" + idString);
            e6 = new LabeledEvent("finish_" + idString);
            
            e6.setControllable(false);
        }
        else
        {
            e1 = events_vector[i];
            e6 = events_vector[i + 1];
        }
        
        // adjust controllability flags, e1 and e6 already taken care of!
        e2.setControllable(false);
        e4.setControllable(false);
        e8.setControllable(false);
        
        // M1:
        createMachine(m1, e1, e2);
        
        // M2:
        createMachine(m2, e3, e4);
        
        if (sanchez_models)
        {
            
            // B1 & B2
            Automaton b12 = new Automaton("B_" + idString);
            
            b12.setType(AutomatonType.SPECIFICATION);
            createBuffer2(b12, cap1, e2, e3, e4, e5, e8);
            project.addAutomaton(b12);
        }
        else
        {
            
            // B1 & B2
            Automaton b1 = new Automaton("B1_" + idString);
            
            b1.setType(AutomatonType.SPECIFICATION);
            createBuffer(b1, cap1, e2, e3, e8);
            project.addAutomaton(b1);
            
            Automaton b2 = new Automaton("B2_" + idString);
            
            b2.setType(AutomatonType.SPECIFICATION);
            createBuffer(b2, cap2, e4, e5, null);
            project.addAutomaton(b2);
        }
        
        // TU:
        createTU(tu, e5, e6, e8);
        m1.setType(AutomatonType.PLANT);
        m2.setType(AutomatonType.PLANT);
        tu.setType(AutomatonType.PLANT);
        project.addAutomaton(m1);
        project.addAutomaton(m2);
        project.addAutomaton(tu);
    }
    
    private void createMachine(Automaton m, LabeledEvent a, LabeledEvent b)
    {
        State s0 = new State("0");
        
        s0.setInitial(true);
        s0.setAccepting(true);
        
        State s1 = new State("1");
        
        m.addState(s0);
        m.addState(s1);
        
        Alphabet sigma = m.getAlphabet();
        
        sigma.addEvent(a);
        sigma.addEvent(b);
        m.addArc(new Arc(s0, s1, a));
        m.addArc(new Arc(s1, s0, b));
    }
    
    private void createBuffer(Automaton buf, int cap, LabeledEvent a, LabeledEvent b, LabeledEvent c)
    {
        State s0 = new State("0");
        
        s0.setInitial(true);
        s0.setAccepting(true);
        buf.addState(s0);
        
        Alphabet sigma = buf.getAlphabet();
        
        sigma.addEvent(a);
        sigma.addEvent(b);
        
        if (c != null)
        {
            sigma.addEvent(c);
        }
        
        State last = s0;
        
        for (int i = 0; i < cap; i++)
        {
            State next = new State("" + (i+1));
            
            buf.addState(next);
            buf.addArc(new Arc(last, next, a));
            buf.addArc(new Arc(next, last, b));
            
            if (c != null)
            {
                buf.addArc(new Arc(last, next, c));
            }
            
            last = next;
        }
    }
    
    // create B1 x B2, as done in Sanchezs benchmarks!
    private void createBuffer2(Automaton b12, int cap, LabeledEvent e2, LabeledEvent e3, LabeledEvent e4, LabeledEvent e5, LabeledEvent e7)
    {
        State ss[] = new State[4];
        
        for (int i = 0; i < 4; i++)
        {
            ss[i] = new State("" + i);
            
            if (i == 0)
            {
                ss[i].setInitial(true);
            }
            
            ss[i].setAccepting(true);
            b12.addState(ss[i]);
        }
        
        Alphabet sigma = b12.getAlphabet();
        
        sigma.addEvent(e2);
        sigma.addEvent(e3);
        sigma.addEvent(e4);
        sigma.addEvent(e5);
        sigma.addEvent(e7);
        b12.addArc(new Arc(ss[0], ss[2], e2));
        b12.addArc(new Arc(ss[0], ss[2], e7));
        b12.addArc(new Arc(ss[0], ss[1], e4));
        b12.addArc(new Arc(ss[1], ss[3], e2));
        b12.addArc(new Arc(ss[1], ss[3], e7));
        b12.addArc(new Arc(ss[1], ss[0], e5));
        b12.addArc(new Arc(ss[2], ss[0], e3));
        b12.addArc(new Arc(ss[2], ss[3], e4));
        b12.addArc(new Arc(ss[3], ss[1], e3));
        b12.addArc(new Arc(ss[3], ss[2], e5));
    }
    
    private void createTU(Automaton tu, LabeledEvent a, LabeledEvent b, LabeledEvent c)
    {
        State s0 = new State("0");
        
        s0.setInitial(true);
        s0.setAccepting(true);
        tu.addState(s0);
        
        State s1 = new State("1");
        
        tu.addState(s1);
        
        Alphabet sigma = tu.getAlphabet();
        
        sigma.addEvent(a);
        sigma.addEvent(b);
        sigma.addEvent(c);
        tu.addArc(new Arc(s0, s1, a));
        tu.addArc(new Arc(s1, s0, b));
        tu.addArc(new Arc(s1, s0, c));
    }
}
