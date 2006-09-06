//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SimpleIdentifierElement
//###########################################################################
//# $Id: SimpleIdentifierElement.java,v 1.6 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * An immutable implementation of the {@link SimpleIdentifierProxy} interface.
 *
 * @author Robi Malik
 */

public final class SimpleIdentifierElement
  extends IdentifierElement
  implements SimpleIdentifierProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simple identifier.
   * @param plainText The original text of the new simple identifier, or <CODE>null</CODE>.
   * @param name The name of the new simple identifier.
   */
  public SimpleIdentifierElement(final String plainText,
                                 final String name)
  {
    super(plainText, name);
  }

  /**
   * Creates a new simple identifier using default values.
   * This constructor creates a simple identifier with
   * the original text set to <CODE>null</CODE>.
   * @param name The name of the new simple identifier.
   */
  public SimpleIdentifierElement(final String name)
  {
    this(null,
         name);
  }


  //#########################################################################
  //# Cloning
  public SimpleIdentifierElement clone()
  {
    return (SimpleIdentifierElement) super.clone();
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
