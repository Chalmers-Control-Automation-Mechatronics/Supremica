//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   SourceInfoBuilder
//###########################################################################
//# $Id: SourceInfoBuilder.java,v 1.2 2008-06-19 19:10:10 robi Exp $
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
    mResultMap = new HashMap<Proxy,SourceInfo>();
  }

  public void shift()
  {
    reset(mResultMap);
  }

  public SourceInfo add(final Proxy target, final Proxy source)
  {
    SourceInfo info = getParentInfo(source);
    if (info == null) {
      info = new SourceInfo(source, null);
    }
    return add(target, info);
  }

  public SourceInfo add(final Proxy target,
                        final Proxy source,
                        final BindingContext context)
  {
    final SourceInfo pinfo = getParentInfo(source);
    final SourceInfo info;
    if (pinfo == null) {
      info = new SourceInfo(source, context);
    } else {
      final Proxy psource = pinfo.getSourceObject();
      info = new SourceInfo(psource, context);
    }
    return add(target, info);
  }

  public SourceInfo add(final Proxy target, final SourceInfo info)
  {
    mResultMap.put(target, info);
    return info;
  }

  public SourceInfo getParentInfo(final Proxy target)
  {
    if (mParentMap == null) {
      return null;
    } else {
      return mParentMap.get(target);
    }
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

