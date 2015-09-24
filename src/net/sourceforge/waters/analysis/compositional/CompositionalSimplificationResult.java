//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A result returned by compositional simplification algorithms
 * ({@link CompositionalSimplifier}). This is just a subtype of
 * {@link CompositionalAnalysisResult} that carries the collection
 * of resultant automata in a {@link ProductDESProxy}.
 *
 * @author Robi Malik
 */

public class CompositionalSimplificationResult
  extends CompositionalAnalysisResult
  implements ProductDESResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simplification result representing an incomplete run.
   */
  public CompositionalSimplificationResult()
  {
    mSimplifiedAutomata = new LinkedList<AutomatonProxy>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProxyResult
  @Override
  public ProductDESProxy getComputedProxy()
  {
    return mProductDES;
  }

  @Override
  public void setComputedProxy(final ProductDESProxy des)
  {
    setSatisfied(des != null);
    mProductDES = des;
  }

  @Override
  public String getResultDescription()
  {
    return "abstraction";
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProductDESResult
  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  @Override
  public Collection<AutomatonProxy> getComputedAutomata()
  {
    return mSimplifiedAutomata;
  }

  @Override
  public void setComputedProductDES(final ProductDESProxy des)
  {
    setComputedProxy(des);
  }


  //#########################################################################
  //# Specific Access
  /**
   * Adds the given automaton to the list of simplified automata.
   */
  void addAutomaton(final AutomatonProxy aut)
  {
    mSimplifiedAutomata.add(aut);
  }

  /**
   * Clears the list of simplified automata.
   */
  void clearAutomata()
  {
    mSimplifiedAutomata.clear();
  }

  /**
   * Completes the result by constructing and storing a product DES consisting
   * of the simplified automata.
   * @param  factory  Factory used to construct the product DES.
   * @param  name     Name to be given to the product DES.
   */
  void close(final ProductDESProxyFactory factory, String name)
  {
    if (isSatisfied()) {
      final Collection<EventProxy> events =
        Candidate.getOrderedEvents(mSimplifiedAutomata);
      if (name == null) {
        name = Candidate.getCompositionName("", mSimplifiedAutomata);
      }
      final ProductDESProxy des =
        factory.createProductDESProxy(name, events, mSimplifiedAutomata);
      setComputedProductDES(des);
    }
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxy mProductDES;
  private final List<AutomatonProxy> mSimplifiedAutomata;

}
