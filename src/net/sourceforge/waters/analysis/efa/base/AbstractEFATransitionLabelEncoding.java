//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

  protected int createTransitionLabelId(final L label)
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

  protected void replaceTransitionLabel(final int labelID, final L label)
  {
    final L oldL = mTransitionLabelList.get(labelID);
    mTransitionLabelMap.remove(oldL);
    mTransitionLabelMap.put(label, labelID);
    mTransitionLabelList.set(labelID, label);
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
