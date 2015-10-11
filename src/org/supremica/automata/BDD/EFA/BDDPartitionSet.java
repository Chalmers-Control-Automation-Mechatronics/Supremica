//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import java.util.List;

import net.sf.javabdd.BDD;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomataIndexMap;
import org.supremica.automata.ExtendedAutomaton;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;


/**
 * The abstract class for partitioning EFAs or DFAs in BDDs
 *
 * @author zhennan
 */

public abstract class BDDPartitionSet
{

  protected BDDExtendedAutomata bddExAutomata;
  protected ExtendedAutomata orgAutomata;
  protected List<ExtendedAutomaton> theExAutomata;
  protected BDDExtendedManager manager;
  protected ExtendedAutomataIndexMap theIndexMap;

  /**
   * Constructs an instance of BDDPartitionSet from bddAutomata.
   */
  public BDDPartitionSet(final BDDExtendedAutomata bddExAutomata)
  {
    this.bddExAutomata = bddExAutomata;
    this.orgAutomata = bddExAutomata.orgExAutomata;
    this.theExAutomata = bddExAutomata.theExAutomata;
    this.manager = bddExAutomata.getManager();
    this.theIndexMap = bddExAutomata.theIndexMap;
  }

  /**
   * Returns a map where the key is the component index while the value is the
   * component transition in BDD
   *
   * @return a TTIntObjectHashMap object where the key is the component index
   *         and the value is the correspond BDD.
   */
  public abstract TIntObjectHashMap<BDD> getCompIndexToCompBDDMap();

  /**
   * Returns a map where the key is the component index and the value is an
   * set of forward dependent component indices.
   */
  protected abstract TIntObjectHashMap<TIntHashSet> getForwardDependentComponentMap();

  /**
   * Returns a map where the key is the component index and the value is an
   * set of backward dependent component indices.
   */
  protected abstract TIntObjectHashMap<TIntHashSet> getBackwardDependentComponentMap();

  /**
   * Returns a set of component indices of which BDD partitions are qualified
   * as the initial component candidates.
   */
  protected abstract TIntHashSet getInitialComponentCandidates();

  /**
   * Returns a set of component indices of which BDD partitions are qualified
   * as the marked component candidates.
   */
  protected abstract TIntHashSet getMarkedComponentCandidates();

  /**
   * Returns a BDD which represents all of the transitions labeled by
   * uncontrollable events.
   */
  protected abstract BDD getUncontrollableTransitionRelationBDD();
}
