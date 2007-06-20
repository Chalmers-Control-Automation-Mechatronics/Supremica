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
    
    public Project getActiveProject();
    
    public String getNewAutomatonName(String str, String def);
}
