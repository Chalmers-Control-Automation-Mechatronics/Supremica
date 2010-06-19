package net.sourceforge.waters.gui.simulator;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


class DisplayPanePopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  DisplayPanePopupFactory(final WatersPopupActionManager master,
                          final AutomatonDisplayPane displayPane,
                          final AutomatonDesktopPane desktopPane)
  {
    super(master);
    mVisitor = new DisplayPanePopupVisitor();
    mDisplayPane = displayPane;
    mDesktopPane = desktopPane;
  }


  //#########################################################################
  //# Menu Items
  protected void addDefaultMenuItems()
  {
    // Do nothing
  }

  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    try {
      proxy.acceptVisitor(mVisitor);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  protected void addCommonMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    AutomatonPopupFactory.setPopup(popup, master, mDesktopPane, mDisplayPane.getAutomaton());
  }


  //#########################################################################
  //# Inner Class DisplayPanePopupVisitor
  private class DisplayPanePopupVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      // do nothing
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      visitProxy(edge);
      if (mDisplayPane != null) {
        if (mDisplayPane.canExecute()) {
          final WatersPopupActionManager master = getMaster();
          final JPopupMenu popup = getPopup();
          final IDEAction execute =
            master.getDesktopExecuteAction(mDisplayPane.getAutomaton(), edge);
          popup.add(execute);
          popup.addSeparator();
        }
      }
      return null;
    }

    public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
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
  private final ModuleProxyVisitor mVisitor;
  private final AutomatonDisplayPane mDisplayPane;
  private final AutomatonDesktopPane mDesktopPane;

}
