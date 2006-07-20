//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleIdentifierSubject
//###########################################################################
//# $Id: SimpleIdentifierSubject.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * The subject implementation of the {@link SimpleIdentifierProxy} interface.
 *
 * @author Robi Malik
 */

public final class SimpleIdentifierSubject
  extends IdentifierSubject
  implements SimpleIdentifierProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simple identifier.
   * @param name The name of the new simple identifier.
   */
  public SimpleIdentifierSubject(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Cloning
  public SimpleIdentifierSubject clone()
  {
    return (SimpleIdentifierSubject) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitSimpleIdentifierProxy(this);
  }

}
