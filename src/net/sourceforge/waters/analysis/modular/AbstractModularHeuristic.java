package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.KindTranslator;
import java.util.Comparator;
import java.util.Set;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import java.util.HashMap;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import java.util.Map;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractModularHeuristic
  implements ModularHeuristic
{
  Map<AutomatonProxy, Map<Key, StateProxy>> mAutomata;
  
  public AbstractModularHeuristic()
  {
    mAutomata = new HashMap<AutomatonProxy, Map<Key, StateProxy>>();
  }
  
  protected AutomatonProxy checkAutomata(boolean specs,
                                         Set<AutomatonProxy> automata,
                                         Comparator<AutomatonProxy> comp,
                                         TraceProxy counterExample,
                                         KindTranslator translator) {
    return checkAutomata(specs, automata, comp, counterExample, translator);
  }
  
  protected AutomatonProxy checkAutomata(AutomatonProxy bestautomaton,
                                         boolean specs,
                                         Set<AutomatonProxy> automata,
                                         Comparator<AutomatonProxy> comp,
                                         TraceProxy counterExample,
                                         KindTranslator translator) {
    for (AutomatonProxy automaton : automata) {
      int i = accepts(automaton, counterExample);
      if (i != counterExample.getEvents().size()) {
        if (!specs || translator.getEventKind(counterExample.getEvents().get(i)) 
            == EventKind.CONTROLLABLE) {
          if (comp.compare(bestautomaton, automaton) < 0) {
            bestautomaton = automaton;
          }
        }
      }
    }
    return bestautomaton;
  }
  
  protected int accepts(AutomatonProxy automaton, TraceProxy counterExample)
  {
    Map<Key, StateProxy> mapAutomaton = mAutomata.get(automaton);
    if (mapAutomaton == null) {
      mapAutomaton = createMap(automaton);
      mAutomata.put(automaton, mapAutomaton);
    }
    int i = 0;
    StateProxy state = null;
    for (StateProxy s : automaton.getStates()) {
      if (s.isInitial()) {
        state = s;
        break;
      }
    }
    for (EventProxy e : counterExample.getEvents()) {
      if (automaton.getEvents().contains(e)) {
        Key k = new Key(state, e);
        state = mapAutomaton.get(k);
        if (state == null) {
          break;
        }
      }
      i++;
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
