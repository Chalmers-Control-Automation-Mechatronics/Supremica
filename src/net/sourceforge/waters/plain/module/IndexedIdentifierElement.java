//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   IndexedIdentifierElement
//###########################################################################
//# $Id: IndexedIdentifierElement.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link IndexedIdentifierProxy} interface.
 *
 * @author Robi Malik
 */

public final class IndexedIdentifierElement
  extends IdentifierElement
  implements IndexedIdentifierProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new indexed identifier.
   * @param name The name of the new indexed identifier.
   * @param indexes The list of array indexes of the new indexed identifier, or <CODE>null</CODE> if empty.
   */
  public IndexedIdentifierElement(final String name,
                                  final Collection<? extends SimpleExpressionProxy> indexes)
  {
    super(name);
    if (indexes == null) {
      mIndexes = Collections.emptyList();
    } else {
      final List<SimpleExpressionProxy> indexesModifiable =
        new ArrayList<SimpleExpressionProxy>(indexes);
      mIndexes =
        Collections.unmodifiableList(indexesModifiable);
    }
  }

  /**
   * Creates a new indexed identifier using default values.
   * This constructor creates an indexed identifier with
   * an empty list of array indexes.
   * @param name The name of the new indexed identifier.
   */
  public IndexedIdentifierElement(final String name)
  {
    this(name,
         emptySimpleExpressionProxyList());
  }


  //#########################################################################
  //# Cloning
  public IndexedIdentifierElement clone()
  {
    return (IndexedIdentifierElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IndexedIdentifierElement downcast = (IndexedIdentifierElement) partner;
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
  private final List<SimpleExpressionProxy> mIndexes;

}
