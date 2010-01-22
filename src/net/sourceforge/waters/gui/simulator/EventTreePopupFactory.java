package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

public class EventTreePopupFactory extends PopupFactory //implements ProductDESProxyVisitor
{
  EventTreePopupFactory(final WatersPopupActionManager master)
  {
    super(master);
    selectedEvent = null;
  }

  //#########################################################################
  //# Shared Menu Items
  public void maybeShowPopup(final Component invoker,
                             final MouseEvent event,
                             final Proxy proxy)
  {
    if (EventProxy.class.isInstance(proxy))
    {
      selectedEvent = (EventProxy)proxy;
      super.maybeShowPopup(invoker, event, proxy);
    }
  }

  protected void addDefaultMenuItems()
  {
    // Do nothing
  }

  protected void addCommonMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction fireEvent = master.getEventExecuteAction(selectedEvent);
    popup.add(fireEvent);
  }

  //#######################################################################
  //# Data Members
  private EventProxy selectedEvent;

  public Object visitAutomatonProxy(final AutomatonProxy proxy)
      throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitConflictTraceProxy(final ConflictTraceProxy proxy)
      throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitEventProxy(final EventProxy proxy) throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitLoopTraceProxy(final LoopTraceProxy proxy)
      throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitProductDESProxy(final ProductDESProxy proxy)
      throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitSafetyTraceProxy(final SafetyTraceProxy proxy)
      throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitStateProxy(final StateProxy proxy) throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitTraceProxy(final TraceProxy proxy) throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitTraceStepProxy(final TraceStepProxy proxy)
      throws VisitorException
  {
    // Do nothing
    return null;
  }

  public Object visitTransitionProxy(final TransitionProxy proxy)
      throws VisitorException
  {
    // Do nothing
    return null;
  }
}
