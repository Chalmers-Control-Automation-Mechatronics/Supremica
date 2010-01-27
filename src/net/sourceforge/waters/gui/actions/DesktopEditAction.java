package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class DesktopEditAction extends WatersDesktopAction
{


  protected DesktopEditAction(final IDE ide, final AutomatonProxy autoToEdit)
  {
    super(ide);
    mAutomaton = autoToEdit;
    putValue(Action.NAME, "Edit Automaton");
    putValue(Action.SHORT_DESCRIPTION,
             "Open this automaton in the editor menu");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    final DocumentContainer docContainer = getIDE().getActiveDocumentContainer();
    final ModuleContainer modContainer = (ModuleContainer)docContainer;
    modContainer.getTabPane().setSelectedIndex(0);
    final SourceInfo sInfo = modContainer.getSourceInfoMap().get(mAutomaton);
    if (sInfo != null)
    {
    if (sInfo.getSourceObject() instanceof SimpleComponentSubject)
      modContainer.getEditorPanel().showEditor((SimpleComponentSubject)sInfo.getSourceObject()); // TODO: Make this work.
    }
    else
      getIDE().error("DEBUG: sInfo is null");
  }

  //private final AutomatonProxy mAutomaton;

  private final AutomatonProxy mAutomaton;
  private static final long serialVersionUID = -1644229513613033199L;
}
