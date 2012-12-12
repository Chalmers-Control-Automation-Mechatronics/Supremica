//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   WatersIntIterator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;


/**
 * <P>An iterator over a collection of <CODE>int</CODE> values.</P>
 *
 * <P>The standard usage pattern is based on a combination of the methods
 * {@link #advance()} and {@link #getCurrentData()}.</P>
 * <PRE> WatersIntIterator iter = ...;
 * while (iter.{@link #advance()}) {
 *   int data = iter.{@link #getCurrentData()};
 *   // process data ...
 * }</PRE>
 *
 * @author Robi Malik
 */

public interface WatersIntIterator
{

  /**
   * Resets the iteration to start iterating over the underlying collection
   * again.
   */
  public void reset();

  /**
   * Advances iteration.
   * This method moves the iterator one step forward.
   * It must be called before trying to access the list using the
   * {@link #getCurrentData()}.
   * @return <CODE>true</CODE> if the iteration contains another element
   *         that is now accessible, or <CODE>false</CODE> if the end of
   *         the list has been reached.
   */
  public boolean advance();

  /**
   * Retrieves the data element at the current position of this iterator.
   */
  public int getCurrentData();

  /**
   * Deletes the data element at the current position of this iterator
   * from the underlying collection. Optional operation.
   */
  public void remove();

}
