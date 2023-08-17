//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

  protected int[] getIds()
  {
    return mInfoMap.values();
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
