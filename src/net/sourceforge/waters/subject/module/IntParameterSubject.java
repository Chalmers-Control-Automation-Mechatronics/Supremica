//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IntParameterSubject
//###########################################################################
//# $Id: IntParameterSubject.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.IntParameterProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * The subject implementation of the {@link IntParameterProxy} interface.
 *
 * @author Robi Malik
 */

public final class IntParameterSubject
  extends SimpleParameterSubject
  implements IntParameterProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new integer parameter.
   * @param name The name of the new integer parameter.
   * @param required The required status of the new integer parameter.
   * @param defaultValue The default value of the new integer parameter.
   */
  public IntParameterSubject(final String name,
                             final boolean required,
                             final SimpleExpressionProxy defaultValue)
  {
    super(name, required, defaultValue);
  }

  /**
   * Creates a new integer parameter using default values.
   * This constructor creates an integer parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new integer parameter.
   * @param defaultValue The default value of the new integer parameter.
   */
  public IntParameterSubject(final String name,
                             final SimpleExpressionProxy defaultValue)
  {
    this(name,
         true,
         defaultValue);
  }


  //#########################################################################
  //# Cloning
  public IntParameterSubject clone()
  {
    return (IntParameterSubject) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitIntParameterProxy(this);
  }

}
