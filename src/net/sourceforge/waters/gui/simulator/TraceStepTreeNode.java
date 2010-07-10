package net.sourceforge.waters.gui.simulator;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.EventKind;


class TraceStepTreeNode extends DefaultMutableTreeNode
{

  //#########################################################################
  //# Factory Methods
  static TraceStepTreeNode createTraceStepNode(final SimulatorState state,
                                               final int time)
  {
    final EventProxy event = state.getEvent();
    if (event != null) {
      return createEventStepNode(event, time);
    } else if (time == 0) {
      return createInitialStepNode();
    } else {
      return createTeleportStepNode(time);
    }
  }

  static TraceStepTreeNode createInitialStepNode()
  {
    return new TraceStepTreeNode("Initial state", null, 0);
  }

  static TraceStepTreeNode createEventStepNode(final EventProxy event,
                                               final int time)
  {
    return new TraceStepTreeNode(event.getName(), event, time);
  }

  static TraceStepTreeNode createTeleportStepNode(final int time)
  {
    return new TraceStepTreeNode("State set manually", null, time);
  }


  //#########################################################################
  //# Constructor
  private TraceStepTreeNode(final String description,
                            final EventProxy event,
                            final int time)
  {
    super(description, true);
    mEvent = event;
    mTime = time;
    // To ensure that the events list can be expanded. This is removed as soon
    // as the node is expanded. Still, it appears as a grey box on the tree.
    add(new DefaultMutableTreeNode("You shouldn't ever see this", false));
  }


  //#########################################################################
  //# Simple Access
  String getText()
  {
    return (String) getUserObject();
  }

  EventProxy getEvent()
  {
    return mEvent;
  }

  int getTime()
  {
    return mTime;
  }

  Icon getIcon()
  {
    if (mEvent != null) {
      final EventKind kind = mEvent.getKind();
      final boolean observable = mEvent.isObservable();
      return ModuleContext.getEventKindIcon(kind, observable);
    } else {
      return PropositionIcon.getUnmarkedIcon();
    }
  }


  //#########################################################################
  //# Tree Access
  void expand(final Simulation sim)
  {
    if (getChildAt(0).getClass() != AutomatonLeafNode.class) {
      removeAllChildren();
      final SimulatorState tuple = sim.getHistoryState(mTime);
      for (final AutomatonProxy aut : sim.getOrderedAutomata()) {
        final AutomatonStatus status = tuple.getStatus(aut);
        if (mTime == 0 || status.compareTo(AutomatonStatus.OK) >= 0) {
          final StateProxy state = tuple.getState(aut);
          final AutomatonLeafNode node =
            new AutomatonLeafNode(aut, state, mTime);
          add(node);
        }
      }
    }
  }

  String getToolTipText(final Simulation sim)
  {
    if (mEvent != null) {
      final ToolTipVisitor visitor = sim.getToolTipVisitor();
      return visitor.getToolTip(mEvent, false);
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final EventProxy mEvent;
  private final int mTime;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1581075011997555080L;

}
