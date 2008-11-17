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

  public SourceInfoBuilder(final Map<Proxy,SourceInfo> parentmap)
  {
    mParentMap = parentmap;
    mResultMap = new HashMap<Proxy,SourceInfo>();
  }


  //#########################################################################
  //# Access
  public void reset()
  {
    reset(null);
  }

  public void reset(final Map<Proxy,SourceInfo> parentmap)
  {
    mParentMap = parentmap;
    if (parentmap == null) {
      mResultMap = new HashMap<Proxy,SourceInfo>();
    } else {
      final int size = parentmap.size();
      mResultMap = new HashMap<Proxy,SourceInfo>(size);
    }
  }

  public void shift()
  {
    reset(mResultMap);
  }

  public SourceInfo add(final Proxy target, final Proxy source)
  {
    return add(target, source, null);
  }

  public SourceInfo add(final Proxy target,
                        final Proxy source,
                        final BindingContext context)
  {
    if (mParentMap == null) {
      final SourceInfo info = new SourceInfo(source, context);
      return add(target, info);
    }
    final SourceInfo pinfo = mParentMap.get(source);
    if (pinfo == null) {
      return null;
    } else if (context == null) {
      return add(target, pinfo);
    } else {
      final Proxy psource = pinfo.getSourceObject();
      final SourceInfo info = new SourceInfo(psource, context);
      return add(target, info);
    }
  }

  public SourceInfo add(final Proxy target, final SourceInfo info)
  {
    mResultMap.put(target, info);
    return info;
  }

  public Map<Proxy,SourceInfo> getResultMap()
  {
    return mResultMap;
  }


  //#########################################################################
  //# Data Members
  private Map<Proxy,SourceInfo> mParentMap;
  private Map<Proxy,SourceInfo> mResultMap;

}

