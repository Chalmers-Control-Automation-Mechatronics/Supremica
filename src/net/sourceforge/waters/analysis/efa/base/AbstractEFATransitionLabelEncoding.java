//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.base
//# CLASS:   AbstractEFATransitionLabelEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Robi Malik
 */

public class AbstractEFATransitionLabelEncoding<L>
{

  //#########################################################################
  //# Constructors
  public AbstractEFATransitionLabelEncoding()
  {
    this(DEFAULT_SIZE);
  }

  public AbstractEFATransitionLabelEncoding(final int size)
  {
    mTransitionLabelMap = new TObjectIntHashMap<>(size, 0.5f, -1);
    mTransitionLabelList = new ArrayList<>(size);
  }

  public AbstractEFATransitionLabelEncoding
    (final AbstractEFATransitionLabelEncoding<L> encoding)
  {
    this(encoding.size());
    for (final L label : encoding.mTransitionLabelList) {
      createTransitionLabelId(label);
    }
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns the number of updates in this event encoding.
   * Note that this method always returns at least&nbsp;1 because the
   * true update is contained in every encoding.
   */
  public int size()
  {
    return mTransitionLabelMap.size();
  }

  public boolean isEmpty()
  {
    return mTransitionLabelMap.isEmpty();
  }

  public int getTransitionLabelId(final L label)
  {
    return mTransitionLabelMap.get(label);
  }

  public L getTransitionLabel(final int labelId)
  {
    return mTransitionLabelList.get(labelId);
  }

  public int createTransitionLabelId(final L label)
  {
    final int id = mTransitionLabelMap.get(label);
    if (id >= 0) {
      return id;
    } else {
      final int labelId = mTransitionLabelMap.size();
      mTransitionLabelMap.put(label, labelId);
      mTransitionLabelList.add(label);
      return labelId;
    }
  }

  /**
   * Adds all updates found in the given event encoding to this event
   * encoding.
   */
  public void merge(final AbstractEFATransitionLabelEncoding<L> enc)
  {
    for (final L label : enc.mTransitionLabelList) {
      createTransitionLabelId(label);
    }
  }


  //#########################################################################
  //# List Access
  /**
   * Retrieves the list of all transition labels in this encoding, except the
   * silent (tau) label.
   * @return An unmodifiable list backed by the encoding.
   */
  protected List<L> getTransitionLabelsExceptTau()
  {
    final List<L> sublist = mTransitionLabelList.subList(1, size());
    return Collections.unmodifiableList(sublist);
  }

  /**
   * Retrieves the list of all transition labels in this encoding, including
   * the silent (tau) label.
   * @return An unmodifiable list backed by the encoding.
   */
  protected List<L> getTransitionLabelsIncludingTau()
  {
    return Collections.unmodifiableList(mTransitionLabelList);
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    int e = 0;
    for (final L update : mTransitionLabelList) {
      buffer.append(e++);
      buffer.append(" : ");
      buffer.append(update);
      buffer.append("\n");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Class Constants
  protected static final int DEFAULT_SIZE = 16;


  //#########################################################################
  //# Data Members
  private final TObjectIntHashMap<L> mTransitionLabelMap;
  private final List<L> mTransitionLabelList;

}

