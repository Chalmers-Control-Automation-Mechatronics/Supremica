//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ForeachEventSubject
//###########################################################################
//# $Id: ForeachEventSubject.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * The subject implementation of the {@link ForeachEventProxy} interface.
 *
 * @author Robi Malik
 */

public final class ForeachEventSubject
  extends ForeachSubject
  implements ForeachEventProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach construct for events.
   * @param name The name of the new foreach construct for events.
   * @param range The range of the new foreach construct for events.
   * @param guard The guard of the new foreach construct for events, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct for events, or <CODE>null</CODE> if empty.
   */
  public ForeachEventSubject(final String name,
                             final SimpleExpressionProxy range,
                             final SimpleExpressionProxy guard,
                             final Collection<? extends Proxy> body)
  {
    super(name, range, guard, body);
  }

  /**
   * Creates a new foreach construct for events using default values.
   * This constructor creates a foreach construct for events with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct for events.
   * @param range The range of the new foreach construct for events.
   */
  public ForeachEventSubject(final String name,
                             final SimpleExpressionProxy range)
  {
    this(name,
         range,
         null,
         emptyProxyList());
  }


  //#########################################################################
  //# Cloning
  public ForeachEventSubject clone()
  {
    return (ForeachEventSubject) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitForeachEventProxy(this);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }

}
