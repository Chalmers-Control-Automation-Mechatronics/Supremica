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
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


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
    final IDEAction closeAll = master.getDesktopCloseAllAction();
    popup.add(closeAll);
    final IDEAction showAll = master.getDesktopShowAllAction();
    popup.add(showAll);
    final IDEAction cascade = master.getDesktopCascadeAction();
    popup.add(cascade);
    if (mDesktopPane.canResizeAll())
    {
      final IDEAction resizeAll = master.getResizeAllAction();
      popup.add(resizeAll);
    }
    if (mDisplayPane != null) {
      final AutomatonProxy aut = mDisplayPane.getAutomaton();
      final IDEAction closeOther = master.getDesktopCloseOtherAction(aut);
      popup.add(closeOther);
      final IDEAction close = master.getDesktopCloseWindowAction(aut);
      popup.add(close);
      if (mDesktopPane.canResize(aut.getName()))
      {
        final IDEAction resize = master.getResizeAction(aut);
        popup.add(resize);
      }
      if (mDesktopPane.canResizeOther(aut.getName()))
      {
        final IDEAction resizeOther = master.getResizeOtherAction(aut);
        popup.add(resizeOther);
      }
    }
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
        }
      }
      return null;
    }

    public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
    {
      /*final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
      final SimpleNodeSubject node = (SimpleNodeSubject) subject.getParent();
      return visitSimpleNodeProxy(node);*/
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

}
