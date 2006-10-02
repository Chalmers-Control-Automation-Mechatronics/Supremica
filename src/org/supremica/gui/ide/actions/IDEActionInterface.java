
package org.supremica.gui.ide.actions;

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
	public ModuleContainer createNewModuleContainer();
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
	public Automata getSelectedAutomata();
	public Project getActiveProject();
	public Automata getAllAutomata();
	public Automata getUnselectedAutomata();
//	int addAutomata(Automata theAutomata);

}
