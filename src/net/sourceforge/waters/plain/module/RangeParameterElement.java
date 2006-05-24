//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   RangeParameterElement
//###########################################################################
//# $Id: RangeParameterElement.java,v 1.4 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.RangeParameterProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link RangeParameterProxy} interface.
 *
 * @author Robi Malik
 */

public final class RangeParameterElement
  extends SimpleParameterElement
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
  public RangeParameterElement(final String name,
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
  public RangeParameterElement(final String name,
                               final SimpleExpressionProxy defaultValue)
  {
    this(name,
         true,
         defaultValue);
  }


  //#########################################################################
  //# Cloning
  public RangeParameterElement clone()
  {
    return (RangeParameterElement) super.clone();
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
