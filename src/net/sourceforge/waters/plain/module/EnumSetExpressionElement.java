//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EnumSetExpressionElement
//###########################################################################
//# $Id: EnumSetExpressionElement.java,v 1.7 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.Proxy;
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
   * @param plainText The original text of the new enumerated range, or <CODE>null</CODE>.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionElement(final String plainText,
                                  final Collection<? extends SimpleIdentifierProxy> items)
  {
    super(plainText);
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
   * the original text set to <CODE>null</CODE>.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionElement(final Collection<? extends SimpleIdentifierProxy> items)
  {
    this(null,
         items);
  }


  //#########################################################################
  //# Cloning
  public EnumSetExpressionElement clone()
  {
    return (EnumSetExpressionElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EnumSetExpressionElement downcast = (EnumSetExpressionElement) partner;
      return
        EqualCollection.isEqualListByContents
          (mItems, downcast.mItems);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final EnumSetExpressionElement downcast = (EnumSetExpressionElement) partner;
      return
        EqualCollection.isEqualListWithGeometry
          (mItems, downcast.mItems);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mItems);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeWithGeometry();
    result *= 5;
    result += EqualCollection.getListHashCodeWithGeometry(mItems);
    return result;
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
  //# Data Members
  private final List<SimpleIdentifierProxy> mItems;

}
