//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ForeachComponentElement
//###########################################################################
//# $Id: ForeachComponentElement.java,v 1.3 2006-02-22 03:35:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link ForeachComponentProxy} interface.
 *
 * @author Robi Malik
 */

public final class ForeachComponentElement
  extends ForeachElement
  implements ForeachComponentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach construct for module components.
   * @param name The name of the new foreach construct for module components.
   * @param range The range of the new foreach construct for module components.
   * @param guard The guard of the new foreach construct for module components, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for module components, or <CODE>null</CODE> if empty.
   */
  public ForeachComponentElement(final String name,
                                 final SimpleExpressionProxy range,
                                 final SimpleExpressionProxy guard,
                                 final Collection<? extends Proxy> body)
  {
    super(name, range, guard, body);
  }

  /**
   * Creates a new foreach construct for module components using default values.
   * This constructor creates a foreach construct for module components with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for module components.
   * @param range The range of the new foreach construct for module components.
   */
  public ForeachComponentElement(final String name,
                                 final SimpleExpressionProxy range)
  {
    this(name,
         range,
         null,
         emptyProxyList());
  }


  //#########################################################################
  //# Cloning
  public ForeachComponentElement clone()
  {
    return (ForeachComponentElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitForeachComponentProxy(this);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }

}
