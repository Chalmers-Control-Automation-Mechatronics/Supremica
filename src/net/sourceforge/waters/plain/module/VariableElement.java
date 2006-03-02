//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   VariableElement
//###########################################################################
//# $Id: VariableElement.java,v 1.2 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.plain.base.Element;


/**
 * An immutable implementation of the {@link VariableProxy} interface.
 *
 * @author Robi Malik
 */

public final class VariableElement
  extends Element
  implements VariableProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new variable.
   * @param name The name of the new variable.
   * @param type The type of the new variable.
   * @param initialValue The initial value of the new variable.
   */
  public VariableElement(final String name,
                         final SimpleExpressionProxy type,
                         final SimpleExpressionProxy initialValue)
  {
    mName = name;
    mType = type;
    mInitialValue = initialValue;
  }


  //#########################################################################
  //# Cloning
  public VariableElement clone()
  {
    return (VariableElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final VariableElement downcast = (VariableElement) partner;
      return
        mName.equals(downcast.mName) &&
        mType.equals(downcast.mType) &&
        mInitialValue.equals(downcast.mInitialValue);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitVariableProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.VariableProxy
  public String getName()
  {
    return mName;
  }

  public SimpleExpressionProxy getType()
  {
    return mType;
  }

  public SimpleExpressionProxy getInitialValue()
  {
    return mInitialValue;
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final SimpleExpressionProxy mType;
  private final SimpleExpressionProxy mInitialValue;

}
