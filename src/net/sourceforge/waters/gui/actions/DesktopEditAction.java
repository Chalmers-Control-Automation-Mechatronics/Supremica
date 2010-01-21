package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.model.des.AutomatonProxy;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class DesktopEditAction extends WatersDesktopAction
{

  protected DesktopEditAction(final IDE ide, final AutomatonProxy autoToEdit)
  {
    super(ide);
    //mAutomaton = autoToEdit;
    putValue(Action.NAME, "Edit Automata");
    putValue(Action.SHORT_DESCRIPTION, "Open this automata in the editor menu");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    final DocumentContainer docContainer = getIDE().getActiveDocumentContainer();
    final ModuleContainer modContainer = (ModuleContainer)docContainer;
    modContainer.getTabPane().setSelectedIndex(0);
    //modContainer.getEditorPanel().showEditor(mAutomaton); // TODO: Make this work.
  }

  //private final AutomatonProxy mAutomaton;

  private static final long serialVersionUID = -1644229513613033199L;
}
