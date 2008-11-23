//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   VariableContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.Collection;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * More detailed compiler context information that includes range
 * information for EFA variables.
 *
 * @author Robi Malik
 * @see net.sourceforge.waters.model.compiler.efa.EFACompiler
 */

public interface VariableContext extends BindingContext {
  
  /**
   * Gets the range associated with the given variable.
   * @param  varname An expression representing the name of the variable
   *                 to be looked up.
   *                 It can be identifier, simple or indexed, (to support
   *                 array lookups), or a unary expression (to support
   *                 next-state value of EFA variables.
   * @return The evaluated range determined for the variable with the given
   *         name, or <CODE>null</CODE> if the name is not associated with
   *         ant variable, or the range has not yet been determined.
   */
  public CompiledRange getVariableRange(SimpleExpressionProxy varname);

  /**
   * Gets a read-only collection containing the names of all EFA variables
   * defined in this context. The variable names are given as expressions
   * that can be either identifiers or unary expressions representing
   * next-state variables.
   */
  public Collection<SimpleExpressionProxy> getVariableNames();

}