//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ImmutableOrderedSet;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.base.DocumentElement;


/**
 * A counterexample to show that a model fails to satisfy some property.
 * This is a simple immutable implementation of the {@link CounterExampleProxy}
 * interface.
 *
 * @author Robi Malik
 */

public abstract class CounterExampleElement
  extends DocumentElement
  implements CounterExampleProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new counterexample by specifying all arguments.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      counterexample, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  traces       The list of traces constituting the new
   *                      counterexample.
   */
  CounterExampleElement(final String name,
                        final String comment,
                        final URI location,
                        final ProductDESProxy des,
                        final Collection<? extends AutomatonProxy> automata,
                        final List<TraceProxy> traces)
  {
    super(name, comment, location);
    mProductDES = des;
    if (automata == null) {
      mAutomata = Collections.emptySet();
    } else {
      final Set<AutomatonProxy> modifiable = new AutomataSet(automata);
      mAutomata = Collections.unmodifiableSet(modifiable);
    }
    mTraces = Collections.unmodifiableList(traces);
  }

  /**
   * Creates a new counterexample using default values.
   * This constructor provides a simple interface with a <CODE>null</CODE>
   * file location, with a set of automata equal to that of the product DES.
   * @param  name         The name to be given to the new counterexample.
   * @param  des          The product DES for which the new counterexample is
   *                      generated.
   * @param  traces       The list of traces constituting the new
   *                      counterexample.
   */
  CounterExampleElement(final String name,
                        final ProductDESProxy des,
                        final List<TraceProxy> traces)
  {
    super(name);
    mProductDES = des;
    final Set<AutomatonProxy> automata = des.getAutomata();
    if (des instanceof ProductDESElement) {
      mAutomata = automata;
    } else {
      final Set<AutomatonProxy> modifiable = new AutomataSet(automata);
      mAutomata = Collections.unmodifiableSet(modifiable);
    }
    mTraces = Collections.unmodifiableList(traces);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.TraceProxy
  @Override
  public ProductDESProxy getProductDES()
  {
    return mProductDES;
  }

  @Override
  public Set<AutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  @Override
  public List<TraceProxy> getTraces()
  {
    return mTraces;
  }


  //#########################################################################
  //# Local Class AutomataSet
  private class AutomataSet extends ImmutableOrderedSet<AutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    AutomataSet(final Collection<? extends AutomatonProxy> automata)
    {
      super(automata);
    }

    //#######################################################################
    //# Overrides from abstract class
    //# net.sourceforge.waters.model.base.ImmutableOrderedSet
    @Override
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Counterexample '" + getName() +
         "' already contains an automaton named '" + name + "'!");
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -6127337323906565431L;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxy mProductDES;
  private final Set<AutomatonProxy> mAutomata;
  private final List<TraceProxy> mTraces;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1530145243581697120L;

}
