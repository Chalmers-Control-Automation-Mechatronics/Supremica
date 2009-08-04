//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SerializableKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A serialisable kind translator used for language inclusion checking.</P>
 *
 * <P>Not all kind translator implemenations can be serialisable. This
 * helper class can be used to obtain a serialisable kind translator for a
 * given model from a non-serialisable kind translator.</P>
 *
 * @author Robi Malik
 */

public class SerializableKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Static Methods
  /**
   * <P>Produces a serialisable kind translator for the given model
   * that produces the same results as the given kind translator, if
   * this kind translator is not already serialisable.</P>
   * @param  translator   The kind translator that defines the mappings
   *                      for the new kind translator.
   * @param  des          A {@link ProductDESProxy} that contains all
   *                      the events and components to be translated by
   *                      the new kind translator.
   * @return If the kind translator passed as the first argument is
   *         serialisable, it is returned; otherwise a new
   *         SerializableKindTranslator equivalent to the first argument
   *         for the given model is created and returned.
   * @throws DuplicateNameException to indicate that an event or component
   *                      name is used more than once in the given model.
   * @see #SerializableKindTranslator(KindTranslator,ProductDESProxy)
   */
  public static KindTranslator getSerializableKindTranslator
    (final KindTranslator translator, final ProductDESProxy des)
  {
    if (translator instanceof Serializable) {
      return translator;
    } else {
      return new SerializableKindTranslator(translator, des);
    }
  }


  //#########################################################################
  //# Constructor
  /**
   * <P>Creates a serialisable kind translator for the given model
   * that produces the same results as the given kind translator.</P>
   * <P>The serialisable kind translator stores the event and automaton
   * names from the model to create serialisable event and automaton kind
   * mappings. Therefore, this kind translator should only be used for
   * events and automatons from the given model: the translation result
   * for other items is not defined.</P>
   * @param  translator   The kind translator that defines the mappings
   *                      for the new kind translator.
   * @param  des          A {@link ProductDESProxy} that contains all
   *                      the events and components to be translated by
   *                      the new kind translator.
   * @throws DuplicateNameException to indicate that an event or component
   *                      name is used more than once in the given model.
   *                      This error makes it impossible to set up a kind
   *                      translation mapping based on names.
   */
  public SerializableKindTranslator(final KindTranslator translator,
                                    final ProductDESProxy des)
  {
    final Collection<AutomatonProxy> automata = des.getAutomata();
    mAutomatonKindMap = new HashMap<String,ComponentKind>(automata.size());
    for (final AutomatonProxy aut : automata) {
      final ComponentKind kind = translator.getComponentKind(aut);
      if (kind != aut.getKind()) {
        final String name = aut.getName();
        if (mAutomatonKindMap.containsKey(name)) {
          throw createDuplicateNameException(des, "automaton", name);
        }
        mAutomatonKindMap.put(name, kind);
      }
    }
    final Collection<EventProxy> events = des.getEvents();
    mEventKindMap = new HashMap<String,EventKind>(events.size());
    for (final EventProxy event : events) {
      final EventKind kind = translator.getEventKind(event);
      if (kind != event.getKind()) {
        final String name = event.getName();
        if (mEventKindMap.containsKey(name)) {
          throw createDuplicateNameException(des, "event", name);
        }
        mEventKindMap.put(name, kind);
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final String name = aut.getName();
    if (mAutomatonKindMap.containsKey(name)) {
      return mAutomatonKindMap.get(name);
    } else {
      return aut.getKind();
    }
  }

  public EventKind getEventKind(final EventProxy event)
  {
    final String name = event.getName();
    if (mEventKindMap.containsKey(name)) {
      return mEventKindMap.get(name);
    } else {
      return event.getKind();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private DuplicateNameException createDuplicateNameException
    (final ProductDESProxy des, final String type, final String name)
  {
    return new DuplicateNameException
      ("Name '" + name + "' is used for more than one " + type +
       " in ProductDES '" + des.getName() + "'!");
  }


  //#########################################################################
  //# Data Members
  private final Map<String,ComponentKind> mAutomatonKindMap;
  private final Map<String,EventKind> mEventKindMap;

}
