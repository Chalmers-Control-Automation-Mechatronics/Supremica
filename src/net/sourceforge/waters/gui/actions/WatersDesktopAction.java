package net.sourceforge.waters.gui.actions;

import java.awt.Component;
import net.sourceforge.waters.gui.simulator.AutomatonDesktopPane;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public abstract class WatersDesktopAction extends WatersAction
{
  // #############################################################
  // # Constructor
  protected WatersDesktopAction(final IDE ide)
  {
    super(ide);
    updateDesktop();
  }

  // #############################################################
  // # Accessor Methods
  public AutomatonDesktopPane getDesktop()
  {
    return mDesktop;
  }
  public void updateDesktop()
  {
    mDesktop = findDesktop();
  }

  // ###############################################################
  // # Auxillary Methods
  protected AutomatonDesktopPane findDesktop()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final ModuleContainer mcontainer = (ModuleContainer) container;
    final Component panel = mcontainer.getActivePanel();
    if (panel instanceof SimulatorPanel) {
      return ((SimulatorPanel) panel).getDesktop();
    } else {
      return null;
    }
  }

  // #################################################################
  // # Data Members
  protected AutomatonDesktopPane mDesktop;

  // #################################################################
  // # Class Constants
  private static final long serialVersionUID = -2271826996405606135L;
}
