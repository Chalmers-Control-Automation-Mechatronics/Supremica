
package org.supremica.gui.ide.actions;

import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.EditorWindowInterface;
import javax.swing.JFrame;

public interface IDEActionInterface
{

	public ModuleContainer createNewModuleContainer();
	public ModuleContainer getActiveModuleContainer();
	public void add(ModuleContainer module);
	public void remove(ModuleContainer module);
	public void setActive(ModuleContainer module);
	public JFrame getFrame();
	public IDE getIDE();
	public void repaint();
	public void setEditorMode(IDEAction theAction);
	public EditorWindowInterface getEditorWindowInterface();
}
