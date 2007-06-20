
package org.supremica.gui.ide.actions;

import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.IDEReportInterface;
import net.sourceforge.waters.gui.EditorWindowInterface;
import javax.swing.JFrame;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.gui.ide.DocumentContainer;

public interface IDEActionInterface
    extends IDEReportInterface
{
    public ModuleSubject createNewModuleSubject();
    public DocumentContainer getActiveDocumentContainer();
    public void add(DocumentContainer container);
    public void remove(DocumentContainer container);
    public void setActive(DocumentContainer container);
    public JFrame getFrame();
    public IDE getIDE();
    public Actions getActions();
    public void repaint();
    public void setEditorMode(IDEAction theAction);
    public EditorWindowInterface getActiveEditorWindowInterface();
}
