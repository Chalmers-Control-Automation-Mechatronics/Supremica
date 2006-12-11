package net.sourceforge.waters.analysis.modular;

import java.util.Set;
import net.sourceforge.waters.model.des.TransitionProxy;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.TraceProxy;

public class CheckSuffix
{
  public static void checkSuffix(TraceProxy counter, Set<AutomatonProxy> plants,
                                 Set<AutomatonProxy> specs)
  {
    boolean none = true;
    for (AutomatonProxy plant : plants) {
      if (accepts(plant, counter) == 0) {
        none = false;
        System.out.println(plant.getName());
      }
    }
    if (none) {
      System.out.println("none");
    }
  }
  
  protected static int accepts(AutomatonProxy automaton, TraceProxy counterExample)
  {
    Map<Key, StateProxy> mapAutomaton = createMap(automaton);
    int i = 0;
    for (StateProxy state : automaton.getStates()) {
      boolean breakearly = false;
      for (EventProxy e : counterExample.getEvents()) {
        if (automaton.getEvents().contains(e)) {
          Key k = new Key(state, e);
          state = mapAutomaton.get(k);
          if (state == null) {
            breakearly = true;
            break;
          }
        }
      }
      if (!breakearly) {
        i++;
      }
    }
    return i;
  }
  
  private static Map<Key, StateProxy> createMap(AutomatonProxy automaton) 
  {
    Map<Key, StateProxy> mapAutomaton =
      new HashMap<Key, StateProxy>(automaton.getTransitions().size());
    for (TransitionProxy trans : automaton.getTransitions()) {
      mapAutomaton.put(new Key(trans.getSource(), trans.getEvent())
                     , trans.getTarget());
    }
    return mapAutomaton;
  }
  
  private static final class Key
  {
    private final StateProxy mSource;
    private final EventProxy mEvent;
    
    public Key(StateProxy source, EventProxy event)
    {
      mSource = source;
      mEvent = event;
    }
    
    public boolean equals(Object o)
    {
      if (!(o instanceof Key)) {
        return false;
      }
      Key k = (Key)o;
      return k.mSource.equals(mSource) && mEvent.equals(k.mEvent);
    }
    
    public int hashCode()
    {
      int hashCode = 17;
      hashCode += 31 * mSource.hashCode();
      hashCode += 31 * mEvent.hashCode();
      return hashCode;
    }
  }
}
