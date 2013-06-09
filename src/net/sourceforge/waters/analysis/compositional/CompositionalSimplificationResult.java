//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSimplificationResult
//###########################################################################
//# $Id$
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
   * of the synthesised supervisors.
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
