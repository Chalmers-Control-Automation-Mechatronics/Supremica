//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Base
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   SourceInfoCloner
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * An implementation of a {@link ModuleProxyCloner} that tracks source
 * information while cloning. Every time a {@link Proxy} is duplicated
 * by this cloner, a reference from the copy to the original is filed
 * in the cloner's {@link SourceInfoBuilder}.
 *
 * @author Robi Malik
 */

public class SourceInfoCloner extends ModuleProxyCloner
{

  //#########################################################################
  //# Constructor
  public SourceInfoCloner(final ModuleProxyFactory factory,
                          final SourceInfoBuilder builder)
  {
    super(factory);
    mSourceInfoBuilder = builder;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.module.ModuleProxyCloner
  @Override
  protected Proxy cloneProxy(final Proxy orig) throws VisitorException
  {
    if (orig ==  null) {
      return null;
    } else {
      final Proxy result = super.cloneProxy(orig);
      mSourceInfoBuilder.add(result, orig);
      return result;
    }
  }


  //#########################################################################
  //# Data Members
  private final SourceInfoBuilder mSourceInfoBuilder;

}
