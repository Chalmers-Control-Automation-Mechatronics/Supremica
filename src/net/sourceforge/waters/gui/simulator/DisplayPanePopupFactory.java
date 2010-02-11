package net.sourceforge.waters.gui.simulator;

import java.util.Map;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


class DisplayPanePopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  DisplayPanePopupFactory(final WatersPopupActionManager master,
                          final AutomatonDisplayPane displayPane,
                          final AutomatonDesktopPane desktopPane,
                          final Map<Proxy, SourceInfo> infoMap)
  {
    super(master);
    mVisitor = new DisplayPanePopupVisitor();
    mDisplayPane = displayPane;
    mDesktopPane = desktopPane;
    mMap = infoMap;
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
            master.getDesktopExecuteAction(mDisplayPane.getAutomaton());
          popup.add(execute);
          popup.addSeparator();
        }
      }
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
    {
      if (mDisplayPane != null) {
        if (node instanceof SimpleNodeProxy)
        {
          for (final StateProxy state : mDisplayPane.getAutomaton().getStates())
          {
            if (node == mMap.get(state).getSourceObject())
            {

              final WatersPopupActionManager master = getMaster();
              final JPopupMenu popup = getPopup();
              final IDEAction teleport =
                master.getDesktopSetStateAction(mDisplayPane.getAutomaton(), node);
              popup.add(teleport);
              popup.addSeparator();
              return null;
            }
          }
        }
      }
      return null;
    }

    public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
    {
      return null;
    }

    public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    {
      visitProxy(ident);
      if (mDisplayPane != null) {
        if (mDisplayPane.canExecute()) {
          final WatersPopupActionManager master = getMaster();
          final JPopupMenu popup = getPopup();
          final IDEAction execute =
            master.getDesktopExecuteAction(mDisplayPane.getAutomaton());
          popup.add(execute);
          popup.addSeparator();
        }
      }
      return null;
    }
  }


  //#######################################################################
  //# Data Members
  private final ModuleProxyVisitor mVisitor;
  private final AutomatonDisplayPane mDisplayPane;
  private final AutomatonDesktopPane mDesktopPane;
  private final Map<Proxy,SourceInfo> mMap;
}
