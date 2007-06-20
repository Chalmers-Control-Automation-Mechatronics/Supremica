
package org.supremica.gui.ide.actions;

import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.IDEReportInterface;
import net.sourceforge.waters.gui.EditorWindowInterface;
import javax.swing.JFrame;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;

public interface IDEActionInterface
    extends IDEReportInterface
{
    public ModuleSubject createNewModuleSubject();
    public ModuleContainer getActiveModuleContainer();
    public void add(ModuleContainer module);
    public void remove(ModuleContainer module);
    public void setActive(ModuleContainer module);
    public JFrame getFrame();
    public IDE getIDE();
    public Actions getActions();
    public void repaint();
    public void setEditorMode(IDEAction theAction);
    public EditorWindowInterface getActiveEditorWindowInterface();
    public Automata getAllAutomata();
    // int addAutomata(Automata theAutomata);
}
