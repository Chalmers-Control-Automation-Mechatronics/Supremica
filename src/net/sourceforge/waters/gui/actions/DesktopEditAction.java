package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.model.base.Proxy;
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
    putValue(Action.SHORT_DESCRIPTION, "Open this automaton in the editor");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    final IDE ide = getIDE();
    final DocumentContainer docContainer = ide.getActiveDocumentContainer();
    final ModuleContainer modContainer = (ModuleContainer) docContainer;
    final SourceInfo info = modContainer.getSourceInfoMap().get(mAutomaton);
    if (info != null) {
      final Proxy source = info.getSourceObject();
      if (source instanceof SimpleComponentSubject) {
        final SimpleComponentSubject comp = (SimpleComponentSubject) source;
        modContainer.showEditor(comp);
      }
    } else {
      ide.error("Source Information is null!");
    }
  }

  private final AutomatonProxy mAutomaton;
  private static final long serialVersionUID = -1644229513613033199L;
}
