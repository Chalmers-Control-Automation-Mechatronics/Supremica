//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IndexedIdentifierSubject
//###########################################################################
//# $Id: IndexedIdentifierSubject.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
   * @param name The name of the new indexed identifier.
   * @param indexes The list of array indexes of the new indexed identifier, or <CODE>null</CODE> if empty.
   */
  public IndexedIdentifierSubject(final String name,
                                  final Collection<? extends SimpleExpressionProxy> indexes)
  {
    super(name);
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
   * an empty list of array indexes.
   * @param name The name of the new indexed identifier.
   */
  public IndexedIdentifierSubject(final String name)
  {
    this(name,
         emptySimpleExpressionProxyList());
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
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IndexedIdentifierSubject downcast = (IndexedIdentifierSubject) partner;
      return
        mIndexes.equals(downcast.mIndexes);
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
  //# Auxiliary Methods
  private static List<SimpleExpressionProxy> emptySimpleExpressionProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private ListSubject<SimpleExpressionSubject> mIndexes;

}
