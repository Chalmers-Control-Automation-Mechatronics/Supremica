//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IndexedIdentifierSubject
//###########################################################################
//# $Id: IndexedIdentifierSubject.java,v 1.8 2007-02-26 21:41:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;


/**
 * The subject implementation of the {@link IndexedIdentifierProxy} interface.
 *
 * @author Robi Malik
 */

public final class IndexedIdentifierSubject
  extends IdentifierSubject
  implements IndexedIdentifierProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new indexed identifier.
   * @param plainText The original text of the new indexed identifier, or <CODE>null</CODE>.
   * @param name The name of the new indexed identifier.
   * @param indexes The list of array indexes of the new indexed identifier, or <CODE>null</CODE> if empty.
   */
  public IndexedIdentifierSubject(final String plainText,
                                  final String name,
                                  final Collection<? extends SimpleExpressionProxy> indexes)
  {
    super(plainText, name);
    if (indexes == null) {
      mIndexes = new ArrayListSubject<SimpleExpressionSubject>();
    } else {
      mIndexes = new ArrayListSubject<SimpleExpressionSubject>
        (indexes, SimpleExpressionSubject.class);
    }
    mIndexes.setParent(this);
  }

  /**
   * Creates a new indexed identifier using default values.
   * This constructor creates an indexed identifier with
   * the original text set to <CODE>null</CODE>.
   * @param name The name of the new indexed identifier.
   * @param indexes The list of array indexes of the new indexed identifier, or <CODE>null</CODE> if empty.
   */
  public IndexedIdentifierSubject(final String name,
                                  final Collection<? extends SimpleExpressionProxy> indexes)
  {
    this(null,
         name,
         indexes);
  }


  //#########################################################################
  //# Cloning
  public IndexedIdentifierSubject clone()
  {
    final IndexedIdentifierSubject cloned = (IndexedIdentifierSubject) super.clone();
    cloned.mIndexes = mIndexes.clone();
    cloned.mIndexes.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final IndexedIdentifierSubject downcast = (IndexedIdentifierSubject) partner;
      return
        ProxyTools.isEqualListByContents
          (mIndexes, downcast.mIndexes);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final IndexedIdentifierSubject downcast = (IndexedIdentifierSubject) partner;
      return
        ProxyTools.isEqualListWithGeometry
          (mIndexes, downcast.mIndexes);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += ProxyTools.getListHashCodeByContents(mIndexes);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeWithGeometry();
    result *= 5;
    result += ProxyTools.getListHashCodeWithGeometry(mIndexes);
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitIndexedIdentifierProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.IndexedIdentifierProxy
  public List<SimpleExpressionProxy> getIndexes()
  {
    final List<SimpleExpressionProxy> downcast = Casting.toList(mIndexes);
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable list of array indexes of this identifier.
   */
  public ListSubject<SimpleExpressionSubject> getIndexesModifiable()
  {
    return mIndexes;
  }


  //#########################################################################
  //# Data Members
  private ListSubject<SimpleExpressionSubject> mIndexes;

}
