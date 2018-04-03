//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import gnu.trove.set.hash.THashSet;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;


public class WatersAnalyzer implements ModelObserver, Observer
{

  //#########################################################################
  //# Constructor
  WatersAnalyzer(final ModuleContainer container)
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();


    mModuleContainer = container;
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, optable);
    mModelObserver = new ArrayList<ModelObserver>();

    final ModuleSubject module = container.getModule();
    module.addModelObserver(this);
    container.attach(this);
    setCompiledDES(null);
  }


  //#########################################################################
  //# Simple Access
  public ModuleContainer getModuleContainer()
  {
    return mModuleContainer;
  }

  public SimpleExpressionCompiler getSimpleExpressionCompiler()
  {
    return mSimpleExpressionCompiler;
  }


  //###################################################################################
  //# Control

  //#########################################################################
  //# Accessing the Product DES
  ProductDESProxy getCompiledDES()
  {
    return mCompiledDES;
  }

  public List<AutomatonProxy> getOrderedAutomata()
  {
    updateAutomata();
    return mOrderedAutomata;
  }

  public AutomatonProxy getAutomatonFromName(final String name)
  {
    updateAutomata();
    return mAutomataMap.get(name);
  }

  List<EventProxy> getOrderedEvents()
  {
    updateEvents();
    return mOrderedEvents;
  }

  public List<AutomatonProxy> getAutomataSensitiveToEvent
    (final EventProxy event)
  {
    final List<AutomatonProxy> list = mAutomataSensitiveToEvent.get(event);
    if (list == null) {
      return Collections.emptyList();
    } else {
      return list;
    }
  }

  /**
   * @param state
   *          The state to be drawn
   * @param automaton
   *          The automaton the state belongs to
   * @return The icon of a state, taking into account propositions
   */
  Icon getMarkingIcon(final StateProxy state,
                      final AutomatonProxy automaton)
  {
    final PropositionIcon.ColorInfo info =
      getMarkingColorInfo(state, automaton);
    return info.getIcon();
  }

  /**
   * @param state
   *          The state to be drawn
   * @param automaton
   *          The automaton the state belongs to
   * @return The color of a state, taking into account propositions.
   */
  PropositionIcon.ColorInfo getMarkingColorInfo
    (final StateProxy state, final AutomatonProxy automaton)
  {
    final Collection<EventProxy> props = state.getPropositions();
    if (props.isEmpty()) {
      if (hasNonForbiddenPropositions(automaton)) {
        return PropositionIcon.getUnmarkedColors();
      } else {
        return PropositionIcon.getNeutralColors();
      }
    } else {
      final Map<Object,SourceInfo> infomap =
        mModuleContainer.getSourceInfoMap();
      if (infomap == null) {
        return PropositionIcon.getNeutralColors();
      }
      final int size = props.size();
      final Set<Color> colorset;
      final List<Color> colorlist;
      if (hasNonForbiddenPropositions(automaton)) {
        colorset = new THashSet<Color>(size);
        colorlist = new ArrayList<Color>(size);
      } else {
        colorset = null;
        colorlist = null;
      }
      boolean forbidden = false;
      for (final EventProxy prop : props) {
        final SourceInfo info = infomap.get(prop);
        final EventDeclProxy decl = (EventDeclProxy) info.getSourceObject();
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo != null) {
          if (colorset != null) {
            for (final Color color : geo.getColorSet()) {
              if (colorset.add(color)) {
                colorlist.add(color);
              }
            }
          }
        } else if (decl.getName().equals
                     (EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          forbidden = true;
        } else if (colorset != null) {
          if (colorset.add(EditorColor.DEFAULTMARKINGCOLOR)) {
            colorlist.add(EditorColor.DEFAULTMARKINGCOLOR);
          }
        }
      }
      return new PropositionIcon.ColorInfo(colorlist, forbidden);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.subject.base.Observer
  /**
   * Invalidates the current compiled DES; it will be recompiled later when
   * the simulator tab is activated.
   */
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    final int kind = event.getKind();
    switch (kind) {
    case ModelChangeEvent.NAME_CHANGED:
    case ModelChangeEvent.STATE_CHANGED:
      if (!(event.getSource() instanceof ModuleProxy)) {
        setCompiledDES(null);
      }
      break;
    case ModelChangeEvent.GEOMETRY_CHANGED:
      break;
    default:
      setCompiledDES(null);;
      break;
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.RENDERING_PRIORITY;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Observer
  // This event is received when a new tab has been activated in a
  // module container, after recompiling the module. If the activated
  // tab was the simulator, it is now safe to get an updated compiled
  // DES from the module container. After storing the new simulation
  // state in the simulator, all registered views are notified to
  // update themselves.
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH &&
        mModuleContainer.getActivePanel() instanceof WatersAnalyzerPanel) {
      final ProductDESProxy newdes = mModuleContainer.getCompiledDES();
      setCompiledDES(newdes);
    }
  }

  public void attach(final ModelObserver observer)
  {
    if (!mModelObserver.contains(observer)) {
      mModelObserver.add(observer);
    }
  }

  public void detach(final ModelObserver observer)
  {
    mModelObserver.remove(observer);
  }


  //#########################################################################
  //# Updating the DES
  /**
   * Stores a new compiled DES. Recomputes all data associated with it
   * and notifies registered views of the change.
   */
  private void setCompiledDES(final ProductDESProxy des)
  {
    if (des != mCompiledDES) {
      mCompiledDES = des;
      mOrderedEvents = null;
      mOrderedAutomata = null;
      mAutomataMap = null;
      mAutomataSensitiveToEvent = null;
    }
  }

  private void updateAutomata()
  {
    if (mOrderedAutomata == null) {
      if (mCompiledDES == null) {
        mOrderedAutomata = Collections.emptyList();
      } else {
        final Collection<AutomatonProxy> automata = mCompiledDES.getAutomata();
        final int numAutomata = automata.size();
        mOrderedAutomata = new ArrayList<AutomatonProxy>(numAutomata);
        mAutomataMap = new HashMap<String,AutomatonProxy>(numAutomata);
        for (final AutomatonProxy aut : automata) {
          mOrderedAutomata.add(aut);
          final String name = aut.getName();
          mAutomataMap.put(name, aut);
        }
        Collections.sort(mOrderedAutomata);
      }
    }
  }


  private void updateEvents()
  {
    if (mOrderedEvents == null) {
      if (mCompiledDES == null) {
        mOrderedEvents = Collections.emptyList();
      } else {
        final Collection<EventProxy> events = mCompiledDES.getEvents();
        final int numEvents = events.size();
        mOrderedEvents = new ArrayList<EventProxy>(numEvents);
        for (final EventProxy event : events) {
          if (event.getKind() != EventKind.PROPOSITION) {
            mOrderedEvents.add(event);
          }
        }
        Collections.sort(mOrderedEvents);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static boolean hasNonForbiddenPropositions
    (final AutomatonProxy automaton)
  {
    for (final EventProxy event : automaton.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {
        final String name = event.getName();
        if (!name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          return true;
        }
      }
    }
    return false;
  }



  //#########################################################################
  //# Data Members

  // Variables remaining unchanged throughout simulator lifetime:
  private final ModuleContainer mModuleContainer;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final List<ModelObserver> mModelObserver;

  // Variables recalculated when the product DES is recompiled:
  private ProductDESProxy mCompiledDES;
  private List<EventProxy> mOrderedEvents;
  private List<AutomatonProxy> mOrderedAutomata;
  private Map<String,AutomatonProxy> mAutomataMap;
  private Map<EventProxy, List<AutomatonProxy>> mAutomataSensitiveToEvent;
}
