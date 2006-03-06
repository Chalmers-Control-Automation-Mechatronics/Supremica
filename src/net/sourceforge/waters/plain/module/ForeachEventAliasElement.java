//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ForeachEventAliasElement
//###########################################################################
//# $Id: ForeachEventAliasElement.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link ForeachEventAliasProxy} interface.
 *
 * @author Robi Malik
 */

public final class ForeachEventAliasElement
  extends ForeachElement
  implements ForeachEventAliasProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach construct for aliases.
   * @param name The name of the new foreach construct for aliases.
   * @param range The range of the new foreach construct for aliases.
   * @param guard The guard of the new foreach construct for aliases, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for aliases, or <CODE>null</CODE> if empty.
   */
  public ForeachEventAliasElement(final String name,
                                  final SimpleExpressionProxy range,
                                  final SimpleExpressionProxy guard,
                                  final Collection<? extends Proxy> body)
  {
    super(name, range, guard, body);
  }

  /**
   * Creates a new foreach construct for aliases using default values.
   * This constructor creates a foreach construct for aliases with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for aliases.
   * @param range The range of the new foreach construct for aliases.
   */
  public ForeachEventAliasElement(final String name,
                                  final SimpleExpressionProxy range)
  {
    this(name,
         range,
         null,
         emptyProxyList());
  }


  //#########################################################################
  //# Cloning
  public ForeachEventAliasElement clone()
  {
    return (ForeachEventAliasElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitForeachEventAliasProxy(this);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }

}
