package net.sourceforge.waters.gui.simulator;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class DisplayPanePopupFactory extends PopupFactory
{
  DisplayPanePopupFactory(final WatersPopupActionManager master, final AutomatonDisplayPane displayPane)
  {
    super(master);
    mDisplayPane = displayPane;
  }


  //#########################################################################
  //# Shared Menu Items
  protected void addDefaultMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction closeAll = master.getDesktopCloseAllAction();
    popup.add(closeAll);
    final IDEAction showAll = master.getDesktopShowAllAction();
    popup.add(showAll);
    final IDEAction cascade = master.getDesktopCascadeAction();
    popup.add(cascade);
  }

  protected void addCommonMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    if (mDisplayPane != null)
    {
      final IDEAction closeOther = master.getDesktopCloseOtherAction(mDisplayPane.getAutomaton());
      popup.add(closeOther);
      final IDEAction close = master.getDesktopCloseWindowAction(mDisplayPane.getAutomaton());
      popup.add(close);
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
  public Object visitEdgeProxy(final EdgeProxy edge)
  {
    //visitProxy(edge);
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    if (mDisplayPane != null)
    {
      final IDEAction execute = master.getDesktopExecuteAction(mDisplayPane.getAutomaton());
      popup.add(execute);
    }
    return null;
  }

  public Object visitGuardActionBlockProxy(final GuardActionBlockProxy block)
  {
    // Never does anything... these are those conditional things on the side of edges
    return null;
  }

  public Object visitLabelBlockProxy(final LabelBlockProxy block)
  {
    return null;
  }

  public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
  {
    /*final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
    final SimpleNodeSubject node = (SimpleNodeSubject) subject.getParent();
    return visitSimpleNodeProxy(node);*/
    return null;
  }

  public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
  {
   // visitProxy(node);
    // Node
    return null;
  }


  //#######################################################################
  //# Data Members
  private final AutomatonDisplayPane mDisplayPane;

}
