package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public abstract class WatersAnalyzeAction
  extends WatersAction
  implements ModelObserver, Observer
{

  // #######################################################################
  // # Constructor
  protected WatersAnalyzeAction(final IDE ide)
  {
    super(ide);
    ide.attach(this);
    final DocumentContainer docContainer = ide.getActiveDocumentContainer();
    if (docContainer instanceof ModuleContainer && docContainer != null)
    {
      final ModuleContainer container = (ModuleContainer) docContainer;
      final ModuleSubject module = container.getModule();
      if (module != null)
      {
        module.addModelObserver(this);
      }
    }
    updateEnabledStatus();
  }

  //##############################################################################
  // # Interface ModelObserver

  public void modelChanged(final ModelChangeEvent event)
  {
    updateEnabledStatus();
  }

//##############################################################################
  // # Interface Observer

  public void update(final EditorChangedEvent e)
  {
    if (e.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH || e.getKind() == EditorChangedEvent.Kind.CONTAINER_SWITCH)
    updateEnabledStatus();
  }

  // ##############################################################################
  // # Abstract Methods

  protected abstract void updateEnabledStatus();

  // ##############################################################################
  // # Class Constants
  private static final long serialVersionUID = -3797986885054648213L;
}
