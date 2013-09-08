//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: org.supremica.automata.algorithms.HDS
//# CLASS:   TransitionLabelMask
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.HDS;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class TransitionLabelMask
{

  public TransitionLabelMask(final int size)
  {
    mMaskLabelsMap = new TIntObjectHashMap<>(size);
  }

  public TransitionLabelMask()
  {
    this(DEFAULT_SIZE);
  }

  public void createMask(final int... labels)
  {
    final int id = mMaskLabelsMap.size();
    mMaskLabelsMap.put(id, new TIntHashSet(labels));
  }

  public int getMask(final int label)
  {
    for (int mask = 0; mask < mMaskLabelsMap.size(); mask++) {
      if (mMaskLabelsMap.get(mask).contains(label)) {
        return mask;
      }
    }
    return -1;
  }

  public void removeLabel(final int label)
  {
    final int mask = getMask(label);
    if (mask >= 0) {
      mMaskLabelsMap.get(mask).remove(label);
    }
  }

  public void addLabelsToMask(final int mask, final int... labels)
  {
    if (mMaskLabelsMap.get(mask) != null) {
      mMaskLabelsMap.get(mask).addAll(labels);
    }
  }

  public TIntHashSet getMasks(final TIntHashSet obs)
  {
    final TIntHashSet masks = new TIntHashSet();
    for (final int e : obs.toArray()) {
      masks.add(getMask(e));
    }
    return masks;
  }

  @Override
  public String toString()
  {
    final StringBuilder str = new StringBuilder();
    for (final int key : mMaskLabelsMap.keys()) {
      str.append(key);
      str.append(":[");
      final TIntHashSet value = mMaskLabelsMap.get(key);
      if (!value.isEmpty()) {
        for (final int v : mMaskLabelsMap.get(key).toArray()) {
          str.append(v);
          str.append(",");
        }
        str.delete(str.length() - 1, str.length());
      }
      str.append("]");
    }
    return str.toString();
  }

  private final TIntObjectHashMap<TIntHashSet> mMaskLabelsMap;
  private static final int DEFAULT_SIZE = 16;
}
