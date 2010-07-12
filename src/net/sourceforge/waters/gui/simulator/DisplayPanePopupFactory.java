package net.sourceforge.waters.gui.simulator;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


class DisplayPanePopupFactory
  extends SimulatorPopupFactory
{

  //#########################################################################
  //# Constructor
  DisplayPanePopupFactory(final Simulation sim,
                          final AutomatonDisplayPane displayPane)
  {
    super(sim);
    mDisplayPane = displayPane;
    mVisitor = new PopupVisitor();
  }


  //#########################################################################
  //# Menu Items
  @Override
  protected void addDefaultMenuItems()
  {
    final AutomatonProxy aut = mDisplayPane.getAutomaton();
    super.addItemSpecificMenuItems(aut);
  }

  @Override
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    mVisitor.addMenuItems(proxy);
    final AutomatonProxy aut = mDisplayPane.getAutomaton();
    super.addItemSpecificMenuItems(aut);
  }


  //#########################################################################
  //# Inner Class DisplayPanePopupVisitor
  private class PopupVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void addMenuItems(final Proxy proxy)
    {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      // do nothing
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      visitProxy(edge);
      if (mDisplayPane != null && mDisplayPane.canExecute()) {
        final WatersPopupActionManager master = getMaster();
        final JPopupMenu popup = getPopup();
        final IDEAction execute =
          master.getDesktopExecuteAction(mDisplayPane.getAutomaton(), edge);
        popup.add(execute);
        popup.addSeparator();
      }
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      visitProxy(ident);
      if (mDisplayPane != null && mDisplayPane.canExecute()) {
        final WatersPopupActionManager master = getMaster();
        final JPopupMenu popup = getPopup();
        final IDEAction execute =
          master.getDesktopExecuteAction(mDisplayPane.getAutomaton(), ident);
        popup.add(execute);
        popup.addSeparator();
      }
      return null;
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      visitProxy(node);
      if (mDisplayPane != null && mDisplayPane.canSetState(node)) {
        final AutomatonProxy aut = mDisplayPane.getAutomaton();
        final WatersPopupActionManager master = getMaster();
        final IDEAction teleport = master.getDesktopSetStateAction(aut, node);
        final JPopupMenu popup = getPopup();
        popup.add(teleport);
        popup.addSeparator();
      }
      return null;
    }

  }


  //#######################################################################
  //# Data Members
  private final PopupVisitor mVisitor;
  private final AutomatonDisplayPane mDisplayPane;

}
