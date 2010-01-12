package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class EventBranchNode extends DefaultMutableTreeNode
{
  public EventBranchNode(final EventProxy event, final int currentTime)
  {
    super(event.toString(), true);
    mEvent = event;
    mTime = currentTime;

  }

  public void addAutomata(final Simulation sim, final HashMap<AutomatonProxy, StateProxy> currentStates)
  {
    if (this.getChildAt(0).getClass() != AutomatonLeafNode.class)
    {
      this.removeAllChildren();
      final ArrayList<AutomatonProxy> automatonInEvent = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy automaton : sim.getAutomata())
        if (automaton.getEvents().contains(mEvent))
          automatonInEvent.add(automaton);
      for (final AutomatonProxy automaton : automatonInEvent)
      {
        if (currentStates == null)
          this.add(new AutomatonLeafNode(automaton, null));
        else
          this.add(new AutomatonLeafNode(automaton, currentStates.get(automaton)));
      }
    }
  }

  public EventProxy getEvent()
  {
    return mEvent;
  }
  public int getTime()
  {
    return mTime;
  }
  public String getData(final int indents)
  {
    String output = getIndents(indents) + this.toString();
    for (int childLoop = 0; childLoop < this.getChildCount(); childLoop++)
    {
      if (this.getChildAt(childLoop).getClass() == EventBranchNode.class)
      {
        final EventBranchNode node = (EventBranchNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
      else if (this.getChildAt(childLoop).getClass() == AutomatonLeafNode.class)
      {
        final AutomatonLeafNode node = (AutomatonLeafNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
    }
    return output;
  }
  private String getIndents(final int indents)
  {
    String output = "";
    for (int looper = 0; looper < indents; looper++)
      output += "-";
    return output;
  }

  private final EventProxy mEvent;
  private final int mTime;

  static ImageIcon enabledEventControllableIcon;
  static ImageIcon disabledEventControllableIcon;
  static ImageIcon enabledEventUncontrollableIcon;
  static ImageIcon disabledEventUncontrollableIcon;

  private static final long serialVersionUID = 1581075011997555080L;


}
