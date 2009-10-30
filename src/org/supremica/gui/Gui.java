package org.supremica.gui;

import java.awt.*;
import java.util.*;
import javax.swing.JFrame;
import org.supremica.gui.ide.IDEReportInterface;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.Automaton;
import org.supremica.gui.VisualProjectContainer;

public interface Gui
    extends IDEReportInterface
{
    void repaint();
    
    String getNewAutomatonName(String str, String def);
    
    // who uses this one?
    // ActionMan does!!
    void clearSelection();
    
    void invertSelection();
    
    void selectAll();
    
    void selectAutomata(int[] a);
    
    void selectAutomata(Collection<?> a);
    
    void unselectAutomaton(int s);
    
    void close();
    
    int addAutomata(Automata a);
//		throws Exception;
    
    // returns number added
    int addProject(Project p)
    throws Exception;
    
    // returns true if added
    boolean addAutomaton(Automaton a);
    
    Component getComponent();
    
    void addAttributes(Project otherProject);
    
    //public void addActions(Actions theActions);
    //public void addControls(Controls theControls);
    // Do we need this one?
    JFrame getFrame();
    
    // this should be the main frame
    VisualProjectContainer getVisualProjectContainer();
    
    Collection getSelectedAutomataAsCollection();
    
    Project getSelectedProject();
    
    void show();


    // WHAT?
    public Automata getSelectedAutomata();
    
    public Automata getUnselectedAutomata();
}
