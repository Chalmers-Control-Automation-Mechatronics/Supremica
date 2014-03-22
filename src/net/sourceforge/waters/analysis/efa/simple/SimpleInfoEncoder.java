//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: net.sourceforge.waters.analysis.efa.simple
//# CLASS:   SimpleInfoEncoder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleInfoEncoder<L>
{
  //#########################################################################
  //# Constructors
  protected SimpleInfoEncoder()
  {
    this(DEFAULT_SIZE);
  }

  protected SimpleInfoEncoder(final int size)
  {
    mInfoMap = new TObjectIntHashMap<>(size, 0.5f, -1);
    mInfoList = new ArrayList<>(size);
  }

  protected SimpleInfoEncoder(final SimpleInfoEncoder<L> encoding)
  {
    this(encoding.size());
    for (final L info : encoding.getInformation()) {
      encode(info);
    }
  }

  protected int size()
  {
    return mInfoMap.size();
  }

  protected boolean isEmpty()
  {
    return mInfoMap.isEmpty();
  }

  protected int getInfoId(final L info)
  {
    return mInfoMap.get(info);
  }

  protected L decode(final int infoId)
  {
    return mInfoList.get(infoId);
  }

  protected final int encode(final L info)
  {
    final int id = mInfoMap.get(info);
    if (id >= 0) {
      return id;
    } else {
      final int infoId = mInfoMap.size();
      mInfoMap.put(info, infoId);
      mInfoList.add(info);
      return infoId;
    }
  }

  protected void replaceInfo(final int infoId, final L info)
  {
    final L oldL = mInfoList.get(infoId);
    mInfoMap.remove(oldL);
    mInfoMap.put(info, infoId);
    mInfoList.set(infoId, info);
  }

  protected void merge(final SimpleInfoEncoder<L> enc)
  {
    for (final L info : enc.getInformation()) {
      encode(info);
    }
  }

  protected List<L> getInformation()
  {
    return Collections.unmodifiableList(mInfoList);
  }

  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    int e = 0;
    for (final L info : mInfoList) {
      buffer.append(e++);
      buffer.append(" : ");
      buffer.append(info);
      buffer.append('\n');
    }
    return buffer.toString();
  }

  //#########################################################################
  //# Class Constants
  protected static final int DEFAULT_SIZE = 16;

  //#########################################################################
  //# Data Members
  private final TObjectIntHashMap<L> mInfoMap;
  private final List<L> mInfoList;
}
