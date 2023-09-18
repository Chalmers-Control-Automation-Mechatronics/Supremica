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

package net.sourceforge.waters.model.analysis.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

/**
 * @author Robi Malik
 */

public class EventAnnotator
{

  //#########################################################################
  //# Constructor
  public EventAnnotator(final ProductDESProxyFactory factory,
                        final File annotationsFile)
    throws IOException
  {
    mFactory = factory;
    mPatterns = new LinkedList<>();

    final FileReader in = new FileReader(annotationsFile);
    final BufferedReader reader = new BufferedReader(in);
    int lineNumber = 0;
    try {
      String line = reader.readLine();
      lineNumber++;
      while (line != null) {
        final String[] words = line.split("  *");
        if (words.length < 3) {
          throw new IOException("Missing key or value annotation on line " +
                                lineNumber + ".");
        }
        final String key = words[0];
        if (key.equalsIgnoreCase("event")) {
          final EventPattern pattern = new EventPattern(words, lineNumber);
          mPatterns.add(pattern);
        } else {
          throw new IOException("Unsupported pattern type '" + key +
                                "' on line " + lineNumber + ".");
        }
        line = reader.readLine();
      }
    } finally {
      reader.close();
    }
  }


  //#########################################################################
  //# Invocation
  public ProductDESProxy apply(final ProductDESProxy des)
  {
    if (mPatterns.isEmpty()) {
      return des;
    }

    final Collection<EventProxy> oldEvents = des.getEvents();
    final Map<EventProxy,EventProxy> eventMap = new HashMap<>(oldEvents.size());
    for (final EventProxy event : oldEvents) {
      Map<String,String> attribs = null;
      for (final EventPattern pattern : mPatterns) {
        if (pattern.matches(event)) {
          if (attribs == null) {
            attribs = new HashMap<>(event.getAttributes());
          }
          pattern.addAttribute(attribs);
        }
      }
      if (attribs != null) {
        final String name = event.getName();
        final EventKind kind = event.getKind();
        final boolean observable = event.isObservable();
        final EventProxy newEvent =
          mFactory.createEventProxy(name, kind, observable, attribs);
        eventMap.put(event, newEvent);
      }
    }
    if (eventMap.isEmpty()) {
      return des;
    }

    final String name = des.getName();
    final String comment = des.getComment();
    final Collection<EventProxy> newEvents = annotateEvents(oldEvents, eventMap);
    final Collection<AutomatonProxy> oldAutomata = des.getAutomata();
    final Collection<AutomatonProxy> newAutomata =
      annotateAutomata(oldAutomata, eventMap);
    return mFactory.createProductDESProxy(name, comment, null,
                                          newEvents, newAutomata);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Collection<EventProxy> annotateEvents
    (final Collection<EventProxy> oldEvents,
     final Map<EventProxy,EventProxy> eventMap)
  {
    final Collection<EventProxy> newEvents = new ArrayList<>(oldEvents.size());
    boolean change = false;
    for (final EventProxy oldEvent : oldEvents) {
      final EventProxy newEvent = eventMap.get(oldEvent);
      if (newEvent == null) {
        newEvents.add(oldEvent);
      } else {
        newEvents.add(newEvent);
        change = true;
      }
    }
    return change ? newEvents : oldEvents;
  }

  private Collection<AutomatonProxy> annotateAutomata
    (final Collection<AutomatonProxy> oldAutomata,
     final Map<EventProxy,EventProxy> eventMap)
  {
    final Collection<AutomatonProxy> newAutomata =
      new ArrayList<>(oldAutomata.size());
    for (final AutomatonProxy oldAut : oldAutomata) {
      final Collection<EventProxy> oldEvents = oldAut.getEvents();
      final Collection<EventProxy> newEvents = annotateEvents(oldEvents, eventMap);
      if (oldEvents == newEvents) {
        newAutomata.add(oldAut);
      } else {
        final String name = oldAut.getName();
        final ComponentKind kind = oldAut.getKind();
        final Collection<StateProxy> states = oldAut.getStates();
        final Collection<TransitionProxy> oldTransitions =
          oldAut.getTransitions();
        final Collection<TransitionProxy> newTransitions =
          annotateTransitions(oldTransitions, eventMap);
        final Map<String,String> attribs = oldAut.getAttributes();
        final AutomatonProxy newAut = mFactory.createAutomatonProxy
          (name, kind, newEvents, states, newTransitions, attribs);
        newAutomata.add(newAut);
      }
    }
    return newAutomata;
  }

  private Collection<TransitionProxy> annotateTransitions
    (final Collection<TransitionProxy> oldTransitions,
     final Map<EventProxy,EventProxy> eventMap)
  {
    final Collection<TransitionProxy> newTransitions =
      new ArrayList<>(oldTransitions.size());
    for (final TransitionProxy oldTrans : oldTransitions) {
      final EventProxy oldEvent = oldTrans.getEvent();
      final EventProxy newEvent = eventMap.get(oldEvent);
      if (newEvent == null) {
        newTransitions.add(oldTrans);
      } else {
        final StateProxy source = oldTrans.getSource();
        final StateProxy target = oldTrans.getTarget();
        final TransitionProxy newTrans =
          mFactory.createTransitionProxy(source, newEvent, target);
        newTransitions.add(newTrans);
      }
    }
    return newTransitions;
  }


  //#########################################################################
  //# Inner Class EventPattern
  private static class EventPattern
  {
    //#########################################################################
    //# Constructor
    private EventPattern(final String[] words, final int lineNumber)
      throws IOException
    {
      assert words[0].equalsIgnoreCase("event");
      final int len = words.length;
      for (int i = 1; i < len - 2; i++) {
        final String word = words[i];
        if (word.equalsIgnoreCase("controllable") &&
            mEventKind == null) {
          mEventKind = EventKind.CONTROLLABLE;
        } else if (word.equalsIgnoreCase("uncontrollable") &&
                   mEventKind == null) {
          mEventKind = EventKind.UNCONTROLLABLE;
        } else if (word.equalsIgnoreCase("observable") &&
                   mObservability == null) {
          mObservability = true;
        } else if (word.equalsIgnoreCase("unobservable") &&
                   mObservability == null) {
          mObservability = true;
        } else if (word.indexOf('=') >= 0) {
          final String[] parts = word.split("=");
          if (mExpectedAttributes == null) {
            mExpectedAttributes = new HashMap<>();
          }
          mExpectedAttributes.put(parts[0], parts[1]);
        } else if (mPattern == null) {
          mPattern = Pattern.compile(word);
        } else {
          throw new IOException("Bad annotation pattern on line " + lineNumber);
        }
      }
      mKey = words[len - 2];
      mValue = words[len - 1];
    }

    //#######################################################################
    //# Invocation
    private boolean matches(final EventProxy event)
    {
      final String name = event.getName();
      final Matcher matcher = mPattern.matcher(name);
      if (!matcher.matches()) {
        return false;
      } else if (mEventKind != null && mEventKind != event.getKind()) {
        return false;
      } else if (mObservability != null && mObservability != event.isObservable()) {
        return false;
      } else if (mExpectedAttributes != null) {
        final Map<String,String> attribs = event.getAttributes();
        for (final Map.Entry<String,String> entry : mExpectedAttributes.entrySet()) {
          final String key = entry.getKey();
          final String value = attribs.get(key);
          final String expected = entry.getValue();
          if (!expected.equals(value)) {
            return false;
          }
        }
      }
      return true;
    }

    private void addAttribute(final Map<String,String> attribs)
    {
      attribs.put(mKey, mValue);
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mPattern.toString();
    }

    //#######################################################################
    //# Data Members
    private Pattern mPattern;
    private EventKind mEventKind;
    private Boolean mObservability;
    private Map<String,String> mExpectedAttributes;
    private final String mKey;
    private final String mValue;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final List<EventPattern> mPatterns;

}
