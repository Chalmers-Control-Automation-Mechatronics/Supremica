//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EnumSetExpressionElement
//###########################################################################
//# $Id: EnumSetExpressionElement.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * An immutable implementation of the {@link EnumSetExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class EnumSetExpressionElement
  extends SimpleExpressionElement
  implements EnumSetExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new enumerated range.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionElement(final Collection<? extends SimpleIdentifierProxy> items)
  {
    if (items == null) {
      mItems = Collections.emptyList();
    } else {
      final List<SimpleIdentifierProxy> itemsModifiable =
        new ArrayList<SimpleIdentifierProxy>(items);
      mItems =
        Collections.unmodifiableList(itemsModifiable);
    }
  }

  /**
   * Creates a new enumerated range using default values.
   * This constructor creates an enumerated range with
   * an empty list of items.
   */
  public EnumSetExpressionElement()
  {
    this(emptySimpleIdentifierProxyList());
  }


  //#########################################################################
  //# Cloning
  public EnumSetExpressionElement clone()
  {
    return (EnumSetExpressionElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EnumSetExpressionElement downcast = (EnumSetExpressionElement) partner;
      return
        mItems.equals(downcast.mItems);
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
    return downcast.visitEnumSetExpressionProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.EnumSetExpressionProxy
  public List<SimpleIdentifierProxy> getItems()
  {
    return mItems;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<SimpleIdentifierProxy> emptySimpleIdentifierProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleIdentifierProxy> mItems;

}
