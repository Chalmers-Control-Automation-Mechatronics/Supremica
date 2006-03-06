//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   EnumSetExpressionSubject
//###########################################################################
//# $Id: EnumSetExpressionSubject.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;


/**
 * The subject implementation of the {@link EnumSetExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class EnumSetExpressionSubject
  extends SimpleExpressionSubject
  implements EnumSetExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new enumerated range.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionSubject(final Collection<? extends SimpleIdentifierProxy> items)
  {
    if (items == null) {
      mItems = new ArrayListSubject<SimpleIdentifierSubject>();
    } else {
      mItems = new ArrayListSubject<SimpleIdentifierSubject>
        (items, SimpleIdentifierSubject.class);
    }
    mItems.setParent(this);
  }

  /**
   * Creates a new enumerated range using default values.
   * This constructor creates an enumerated range with
   * an empty list of items.
   */
  public EnumSetExpressionSubject()
  {
    this(emptySimpleIdentifierProxyList());
  }


  //#########################################################################
  //# Cloning
  public EnumSetExpressionSubject clone()
  {
    final EnumSetExpressionSubject cloned = (EnumSetExpressionSubject) super.clone();
    cloned.mItems = mItems.clone();
    cloned.mItems.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EnumSetExpressionSubject downcast = (EnumSetExpressionSubject) partner;
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
    final List<SimpleIdentifierProxy> downcast = Casting.toList(mItems);
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable list of items in this enumeration.
   */
  public ListSubject<SimpleIdentifierSubject> getItemsModifiable()
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
  private ListSubject<SimpleIdentifierSubject> mItems;

}
