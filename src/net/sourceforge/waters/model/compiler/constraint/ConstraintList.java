//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   ConstraintList
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A list of simple expressions, typically understood as a conjunction
 * guard constraints.
 *
 * @author Robi Malik
 */

public class ConstraintList
{

  //#########################################################################
  //# Constructors
  public ConstraintList(final List<SimpleExpressionProxy> constraints)
  {
    mConstraints = constraints;
    mUnmodifiableConstraints = Collections.unmodifiableList(constraints);
  }


  //#########################################################################
  //# Overrides for Baseclass java.lang.Object
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ProxyPrinter printer = new ModuleProxyPrinter(writer);
      printer.pprint(mConstraints, "{", ", ", "}");
      return writer.getBuffer().toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public boolean equals(final Object other)
  {
    if (other != null && other.getClass() == getClass()) {
      final ConstraintList clist = (ConstraintList) other;
      return ProxyTools.isEqualListByContents
        (mConstraints, clist.mConstraints);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return ProxyTools.getListHashCodeByContents(mConstraints);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Retrieves the list of constraints. Unmodifiable.
   */
  public List<SimpleExpressionProxy> getConstraints()
  {
    return mUnmodifiableConstraints;
  }


  //#########################################################################
  //# Sorting
  public void sort(final Comparator<SimpleExpressionProxy> comparator)
  {
    Collections.sort(mConstraints, comparator);
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleExpressionProxy> mConstraints;
  private final List<SimpleExpressionProxy> mUnmodifiableConstraints;

}
