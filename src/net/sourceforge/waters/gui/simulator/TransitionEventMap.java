package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

public class TransitionEventMap
{

  public TransitionEventMap(final ProductDESProxy des)
  {
    data = new HashMap<AutomatonProxy, Map<StateProxy, List<TransitionProxy>>>();
    if (des != null)
    {
      for (final AutomatonProxy auto : des.getAutomata())
      {
        final HashMap<StateProxy, List<TransitionProxy>> map = new HashMap<StateProxy, List<TransitionProxy>>();
        for (final StateProxy state : auto.getStates())
        {
          final ArrayList<TransitionProxy> allTrans = new ArrayList<TransitionProxy>();
          for (final TransitionProxy trans : auto.getTransitions())
          {
            if (trans.getSource() == state)
            {
              allTrans.add(trans);
            }
          }
          map.put(state, allTrans);
        }
        data.put(auto, map);
      }
    }
  }

  public List<TransitionProxy> getTransition(final AutomatonProxy auto, final StateProxy state)
  {
    if (data.get(auto) != null)
      return data.get(auto).get(state);
    else
      return Collections.emptyList();
  }

  public boolean isInvalid()
  {
    return data.size() == 0;
  }

  public String getData()
  {
    String output = "";
    for (final AutomatonProxy auto : data.keySet())
    {
      output += auto.getName() + "\r\n";
      for (final StateProxy state : data.get(auto).keySet())
      {
        output += "   " + state.getName() + "\r\n";
        for (final TransitionProxy trans : data.get(auto).get(state))
        {
          output += "      " + trans.getEvent().getName() + "\r\n";
        }
      }
    }
    return output;
  }

  HashMap<AutomatonProxy, Map<StateProxy, List<TransitionProxy>>> data;
}
