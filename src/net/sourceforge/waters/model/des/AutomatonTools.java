//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AutomatonTools
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Collection;
import java.util.Collections;


/**
 * A collection of static methods commonly used in combination with
 * automata.
 *
 * @author Robi Malik
 */

public final class AutomatonTools
{

  /**
   * Calculates binary logarithm.
   * @return The largest number <I>k</I> such that
   *         2<sup><I>k</I></sup>&nbsp;&lt;=&nbsp;<I>x</I>,
   *         i.e., the number of bits needed to encode numbers from
   *         0..<I>x</I>-1.
   */
  public static int log2(int x)
  {
    int result = 0;
    if (x > 1) {
      x--;
      do {
        x >>= 1;
        result++;
      } while (x > 0);
    }
    return result;
  }

  /**
   * Creates a product DES consisting of a single automaton.
   * @param  aut     The automaton to be used in the product DES.
   * @param  factory Factory to construct objects.
   * @return A product DES with only one automaton. The product DES name
   *         and event list are taken from the given automaton.
   */
  public static ProductDESProxy createProductDESProxy
    (final AutomatonProxy aut, final ProductDESProxyFactory factory)
  {
    final String name = aut.getName();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<AutomatonProxy> automata = Collections.singletonList(aut);
    return factory.createProductDESProxy(name, events, automata);
  }

}
