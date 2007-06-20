package org.supremica.gui.ide;

import javax.swing.JFrame;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;

public interface IDEReportInterface
{
    void error(String msg);
    
    // outputs an error message
    void error(String msg, Throwable t);
    
    void info(String msg);
    
    void debug(String msg);
    
    JFrame getFrame();
    
    boolean addAutomaton(Automaton theAutomaton);
    
    int addAutomata(Automata theAutomata);
    
    public Project getActiveProject();
    
    public Automata getSelectedAutomata();
    
    public Automata getUnselectedAutomata();
    
    public String getNewAutomatonName(String str, String def);
}
