//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SynthesisStateSpace
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.map.hash.TLongIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.LongSynchronisationEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class SynthesisStateSpace implements AutomatonProxy
{

  //#########################################################################
  //# Constructor
  public SynthesisStateSpace(final ProductDESProxyFactory factory,
                             final KindTranslator translator,
                             final ProductDESProxy des,
                             final String name)
  {
    mFactory = factory;
    mStateMaps = new LinkedList<>();
    final List<AutomatonProxy> automataList =
      new ArrayList<>(des.getAutomata().size());
    mStateEncodingMap = new HashMap<>(des.getAutomata().size());
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
      case SPEC:
        automataList.add(aut);
        break;
      default :
        break;
      }
    }
    mDES = factory.createProductDESProxy
      (des.getName(), des.getEvents(), automataList);
    if (name == null) {
      mName = Candidate.getCompositionName("sup:", automataList);
    } else {
      mName = name;
    }
  }

  //#########################################################################
  //# Initialisation
  public SynthesisStateMap createStateEncodingMap
    (final AutomatonProxy automaton, final StateEncoding encoding)
  {
    mStateEncodingMap.put(automaton, encoding);
    return new StateEncodingMap(automaton, encoding);
  }

  public SynthesisStateMap createPartitionMap
  (final TRPartition partition, final SynthesisStateMap parent)
  {
    return new PartitionMap(partition, parent);
  }

  public SynthesisStateMap createSynchronisationMap
                            (final TLongIntHashMap map,
                             final LongSynchronisationEncoding encoding,
                             final List<SynthesisStateMap> parents)
  {
    return new SynchronisationMap( map, encoding, parents);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public AutomatonProxy clone()
  {
    createAutomaton();
    return mAutomaton.clone();
  }

  @Override
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }


  //#########################################################################
  //# Interface AutomatonProxy
  @Override
  public String getName()
  {
    return mName;
  }

  @Override
  public boolean refequals(final NamedProxy partner)
  {
    return mName.equals(partner.getName());
  }

  @Override
  public int refHashCode()
  {
    return mName.hashCode();
  }

  @Override
  public Class<? extends Proxy> getProxyInterface()
  {
    return AutomatonProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor = (ProductDESProxyVisitor) visitor;
    return desVisitor.visitAutomatonProxy(this);
  }

  @Override
  public int compareTo(final NamedProxy o)
  {
    return mName.compareTo(o.getName());
  }

  @Override
  public ComponentKind getKind()
  {
    return ComponentKind.SUPERVISOR;
  }

  @Override
  public Set<EventProxy> getEvents()
  {
    return mDES.getEvents();
  }

  @Override
  public Set<StateProxy> getStates()
  {
    createAutomaton();
    return mAutomaton.getStates();
  }

  @Override
  public Collection<TransitionProxy> getTransitions()
  {
    createAutomaton();
    return mAutomaton.getTransitions();
  }

  @Override
  public Map<String,String> getAttributes()
  {
    createAutomaton();
    return mAutomaton.getAttributes();
  }


  //#########################################################################
  //# Simple Access
  public boolean isSafeState(final Map<AutomatonProxy,StateProxy> tuple)
  {
    for (final SynthesisStateMap map : mStateMaps) {
      if (map.getStateNumber(tuple) < 0) {
        return false;
      }
    }
    return true;
  }

  public void addStateMap(final SynthesisStateMap map)
  {
    mStateMaps.add(map);
  }


  //#########################################################################
  //# Automaton Construction
  public AutomatonProxy createAutomaton()
  {
    if (mAutomaton == null) {
      try {
        final MonolithicSynchronousProductBuilder syncBuilder =
          new MonolithicSynchronousProductBuilder(mDES, mFactory);
        syncBuilder.setOutputName(mName);
        syncBuilder.setOutputKind(ComponentKind.SUPERVISOR);
//        syncBuilder.setRemovingSelfloops(true);
        final SynthesisStateCallBack callBack = new SynthesisStateCallBack();
        syncBuilder.setStateCallback(callBack);
        syncBuilder.run();
        mAutomaton = syncBuilder.getComputedAutomaton();
      } catch(final AnalysisException exception) {
        throw exception.getRuntimeException();
      }
    }
    return mAutomaton;
  }


  //#########################################################################
  //# Inner Class
  abstract static class SynthesisStateMap
  {
    abstract int getStateNumber(Map<AutomatonProxy,StateProxy> tuple);
  }


  //#########################################################################
  //# Inner Class
  private static class StateEncodingMap extends SynthesisStateMap
  {
    //#######################################################################
    //# Constructor
    private StateEncodingMap(final AutomatonProxy automaton,
                             final StateEncoding encoding)
    {
      mAutomaton = automaton;
      mStateEncoding = encoding;
    }
    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> tuple)
    {
      final StateProxy state = tuple.get(mAutomaton);
      return mStateEncoding.getStateCode(state);
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private final StateEncoding mStateEncoding;
  }


  //#########################################################################
  //# Inner Class
  private static class PartitionMap extends SynthesisStateMap
  {
    //#######################################################################
    //# Constructor
    private PartitionMap(final TRPartition partition,
                         final SynthesisStateMap parent)
    {
      mStateToClass = partition.getStateToClass();
      mParent = parent;
    }

    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> tuple)
    {
      final int state = mParent.getStateNumber(tuple);
      return state < 0 ? -1 : mStateToClass[state];
    }

    //#######################################################################
    //# Data Members
    private final int[] mStateToClass;
    private final SynthesisStateMap mParent;
  }


  //#########################################################################
  //# Inner Class
  private static class SynchronisationMap extends SynthesisStateMap
  {
    //#######################################################################
    //# Constructor
    private SynchronisationMap(final TLongIntHashMap map,
                               final LongSynchronisationEncoding encoding,
                               final List<SynthesisStateMap> parents)
    {
      mSynchronisationMap = map;
      mSynchronisationEncoding = encoding;
      mParents = parents;
    }

    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> fullTuple)
    {
      final int[] tuple = new int[mParents.size()];
      for (int i = 0; i < mParents.size(); i++) {
        tuple[i] = mParents.get(i).getStateNumber(fullTuple);
        if (tuple[i] < 0) {
          return -1;
        }
      }
      final long key = mSynchronisationEncoding.encode(tuple);
      return mSynchronisationMap.get(key);
    }

    //#######################################################################
    //# Data Members
    private final TLongIntHashMap mSynchronisationMap;
    private final LongSynchronisationEncoding mSynchronisationEncoding;
    private final List<SynthesisStateMap> mParents;
  }


  //#########################################################################
  //# Inner class
  private class SynthesisStateCallBack implements
    MonolithicSynchronousProductBuilder.StateCallback
  {
    //#######################################################################
    //# Interface MonolithicSynchronousProductBuilder.StateCallBack
    @Override
    public boolean newState(final int[] tuple) throws OverflowException
    {
      final Set<AutomatonProxy> automata = mDES.getAutomata();
      final Map<AutomatonProxy,StateProxy> map =
        new HashMap<>(automata.size());
      int i = 0;
      for (final AutomatonProxy aut : automata) {
        final StateEncoding encoding = mStateEncodingMap.get(aut);
        final int s = tuple[i];
        final StateProxy state = encoding.getState(s);
        map.put(aut, state);
        i++;
      }
      return isSafeState(map);
    }

    @Override
    public void recordStatistics(final AutomatonResult result)
    {
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final ProductDESProxy mDES;
  private final Map<AutomatonProxy,StateEncoding> mStateEncodingMap;
  private final List<SynthesisStateMap> mStateMaps;
  private final String mName;
  private AutomatonProxy mAutomaton;

}
