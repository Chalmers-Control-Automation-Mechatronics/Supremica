//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   RangeParameterSubject
//###########################################################################
//# $Id: RangeParameterSubject.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.RangeParameterProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * The subject implementation of the {@link RangeParameterProxy} interface.
 *
 * @author Robi Malik
 */

public final class RangeParameterSubject
  extends SimpleParameterSubject
  implements RangeParameterProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new range parameter.
   * @param name The name of the new range parameter.
   * @param required The required status of the new range parameter.
   * @param defaultValue The default value of the new range parameter.
   */
  public RangeParameterSubject(final String name,
                               final boolean required,
                               final SimpleExpressionProxy defaultValue)
  {
    super(name, required, defaultValue);
  }

  /**
   * Creates a new range parameter using default values.
   * This constructor creates a range parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new range parameter.
   * @param defaultValue The default value of the new range parameter.
   */
  public RangeParameterSubject(final String name,
                               final SimpleExpressionProxy defaultValue)
  {
    this(name,
         true,
         defaultValue);
  }


  //#########################################################################
  //# Cloning
  public RangeParameterSubject clone()
  {
    return (RangeParameterSubject) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitRangeParameterProxy(this);
  }

}
