
package org.supremica.gui.ide.actions;

import javax.swing.JFrame;

import net.sourceforge.waters.gui.EditorWindowInterface;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.IDEReportInterface;


public interface IDEActionInterface
    extends IDEReportInterface
{
    public DocumentContainer getActiveDocumentContainer();
    public JFrame getFrame();
    public IDE getIDE();
    public Actions getActions();
    public void repaint();
    public EditorWindowInterface getActiveEditorWindowInterface();
}
