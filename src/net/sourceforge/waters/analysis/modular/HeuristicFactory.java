//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TraceFinder;
import net.sourceforge.waters.model.analysis.JavaEnumFactory;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * The central access point to obtain a {@link HeuristicValueProvider} or a
 * {@link HeuristicEvaluator}, which implement automata selection procedures
 * used by the incremental/modular verification algorithms.
 *
 * @author Robi Malik
 */

public class HeuristicFactory
  extends JavaEnumFactory<HeuristicFactory.Method>
{

  //#########################################################################
  //# Singleton Pattern
  public static HeuristicFactory getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static final class SingletonHolder {
    private static final HeuristicFactory theInstance =
      new HeuristicFactory();
  }

  private HeuristicFactory()
  {
    super(Method.class, Method.MaxCommonEvents);
  }


  //#########################################################################
  //# Creation of Heuristics
  public HeuristicValueProvider createProvider(final Method method)
  {
    return method.createProvider();
  }

  public HeuristicEvaluator createEvaluator(final Preference pref,
                                            final Method method,
                                            final KindTranslator translator,
                                            final HeuristicTraceChecker checker)
  {
    final List<HeuristicValueProvider> providers =
      method.getListWithAlternatives();
    return new HeuristicEvaluator(translator, pref, providers, checker);
  }

  public HeuristicEvaluator createEvaluator(final Preference pref,
                                            final List<Method> methods,
                                            final KindTranslator translator,
                                            final HeuristicTraceChecker checker)
  {
    final List<HeuristicValueProvider> providers =
      new ArrayList<>(methods.size());
    for (final Method method : methods) {
      final HeuristicValueProvider provider = method.createProvider();
      providers.add(provider);
    }
    return new HeuristicEvaluator(translator, pref, providers, checker);
  }


  //#########################################################################
  //# Inner Enumeration Method
  public enum Method {
    All {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new DefaultHeuristicValueProvider(this) {
          @Override
          public CollectionMode getCollectionMode()
          {
            return CollectionMode.ALL;
          }
        };
      }
    },
    EarlyNotAccept {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new CounterExampleHeuristicValueProvider(this) {
          @Override
          protected int computeHeuristicValue(final TraceFinder.Result result)
          {
            return result.getTotalAcceptedSteps();
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinStates, MinTransitions);
      }
    },
    LateNotAccept {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new CounterExampleHeuristicValueProvider(this) {
          @Override
          protected int computeHeuristicValue(final TraceFinder.Result result)
          {
            return - result.getTotalAcceptedSteps();
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinStates, MinTransitions);
      }
    },
    MaxCommonEvents {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new CommonEventsHeuristicValueProvider(this);
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MaxCommonUncontrollableEvents, MinEvents,
                                       MinStates, MinTransitions);
      }
    },
    MaxCommonUncontrollableEvents {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new CommonEventsHeuristicValueProvider(this,
                                                      EventKind.UNCONTROLLABLE);
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MaxCommonEvents, MinEvents,
                                       MinStates, MinTransitions);
      }
    },
    MaxStates {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new DefaultHeuristicValueProvider(this) {
          @Override
          public float getHeuristicValue(final HeuristicEvaluator evaluator,
                                         final AutomatonProxy aut)
          {
            return - aut.getStates().size();
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinTransitions);
      }
    },
    MinEvents {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new CachingHeuristicValueProvider(this, false) {
          @Override
          protected float computeHeuristicValue(final HeuristicEvaluator evaluator,
                                                final AutomatonProxy aut)
          {
            int count = 0;
            for (final EventProxy event : aut.getEvents()) {
              if (event.getKind() != EventKind.PROPOSITION) {
                count++;
              }
            }
            return count;
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinStates, MinTransitions);
      }
    },
    MinNewEvents {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new CommonEventsHeuristicValueProvider(this) {
          @Override
          protected float computeHeuristicValue(final HeuristicEvaluator evaluator,
                                                final AutomatonProxy aut)
          {
            int result = 0;
            for (final EventProxy event : aut.getEvents()) {
              if (!isCurrentEvent(event)) {
                result++;
              }
            }
            return result;
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinStates, MinTransitions);
      }
    },
    MinStates {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new DefaultHeuristicValueProvider(this) {
          @Override
          public float getHeuristicValue(final HeuristicEvaluator evaluator,
                                         final AutomatonProxy aut)
          {
            return aut.getStates().size();
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinTransitions);
      }
    },
    MinTransitions {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new DefaultHeuristicValueProvider(this) {
          @Override
          public float getHeuristicValue(final HeuristicEvaluator evaluator,
                                         final AutomatonProxy aut)
          {
            return aut.getTransitions().size();
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinStates);
      }
    },
    One {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new DefaultHeuristicValueProvider(this) {
          @Override
          public CollectionMode getCollectionMode()
          {
            return CollectionMode.FIRST;
          }
        };
      }
    },
    Plant {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new KindPreferenceHeuristicValueProvider
          (this, Preference.PREFER_PLANT);
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinStates, MinTransitions);
      }
    },
    RealPlant {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new KindPreferenceHeuristicValueProvider
          (this, Preference.PREFER_REAL_PLANT);
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinStates, MinTransitions);
      }
    },
    RelMaxCommonEvents {
      @Override
      public HeuristicValueProvider createProvider()
      {
        return new CommonEventsHeuristicValueProvider(this) {
          @Override
          protected float computeHeuristicValue(final HeuristicEvaluator evaluator,
                                                final AutomatonProxy aut)
          {
            int includedCount = 0;
            int totalCount = 0;
            for (final EventProxy event : aut.getEvents()) {
              if (isCurrentEvent(event)) {
                includedCount++;
              }
              if (event.getKind() != EventKind.PROPOSITION) {
                totalCount++;
              }
            }
            return - (float) includedCount / totalCount;
          }
        };
      }
      @Override
      public List<HeuristicValueProvider> getListWithAlternatives()
      {
        return getListWithAlternatives(MinEvents, MinStates, MinTransitions);
      }
    };

    //#######################################################################
    //# Creation of Heuristics Objects
    public HeuristicValueProvider createProvider()
    {
      return new DefaultHeuristicValueProvider(this);
    }

    public List<HeuristicValueProvider> getListWithAlternatives()
    {
      final HeuristicValueProvider provider = createProvider();
      return Collections.singletonList(provider);
    }

    public List<HeuristicValueProvider> getListWithAlternatives(final Method... methods)
    {
      final List<HeuristicValueProvider> list =
        new ArrayList<>(methods.length + 1);
      HeuristicValueProvider provider = createProvider();
      list.add(provider);
      for (final Method method : methods) {
        if (method != this) {
          provider = method.createProvider();
          list.add(provider);
        }
      }
      return list;
    }
  }


  //#########################################################################
  //# Inner Enumeration Method
  public enum Preference {
    NOPREF,
    PREFER_PLANT,
    PREFER_REAL_PLANT
  }


  //#########################################################################
  //# Inner Enumeration Method
  public enum CollectionMode {
    FIRST,
    BEST,
    ALL
  }

}
