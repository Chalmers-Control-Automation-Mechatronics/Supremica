//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   SourceInfoBuilder
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.context;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;


/**
 * A utility class that helps to collect source information records
 * while compiling a module.
 *
 * @author Robi Malik
 */

public class SourceInfoBuilder
{

  //#########################################################################
  //# Constructors
  public SourceInfoBuilder()
  {
    this(null);
  }

  public SourceInfoBuilder(final Map<Object,SourceInfo> parentmap)
  {
    mParentMap = parentmap;
    mResultMap = new HashMap<Object,SourceInfo>();
  }


  //#########################################################################
  //# Access
  public void reset()
  {
    reset(null);
  }

  public void reset(final Map<Object,SourceInfo> parentmap)
  {
    mParentMap = parentmap;
    if (parentmap == null) {
      mResultMap = new HashMap<Object,SourceInfo>();
    } else {
      final int size = parentmap.size();
      mResultMap = new HashMap<Object,SourceInfo>(size);
    }
  }

  public void shift()
  {
    reset(mResultMap);
  }

  public SourceInfo add(final Object target, final Proxy source)
  {
    return add(target, source, null);
  }

  public SourceInfo add(final Object target,
                        final Proxy source,
                        final BindingContext context)
  {
    SourceInfo info = getSourceInfo(source);
    if (info == null) {
      if (mParentMap == null) {
        info = new SourceInfo(source, context);
        return add(target, info);
      } else {
        return null;
      }
    }
    if (context != null) {
      info = new SourceInfo(info.getSourceObject(), context);
    }
    return add(target, info);
  }

  public SourceInfo add(final Object target, final SourceInfo info)
  {
    mResultMap.put(target, info);
    return info;
  }

  public Map<Object,SourceInfo> getResultMap()
  {
    return mResultMap;
  }

  public SourceInfo getSourceInfo(final Object target)
  {
    SourceInfo info = mResultMap.get(target);
    if (info == null && mParentMap != null) {
      info = mParentMap.get(target);
    }
    return info;
  }


  //#########################################################################
  //# Data Members
  private Map<Object,SourceInfo> mParentMap;
  private Map<Object,SourceInfo> mResultMap;

}

