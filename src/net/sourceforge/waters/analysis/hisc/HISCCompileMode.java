//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters HISC
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   HISCCompileMode
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.hisc;

/**
 * An enumeration to store different compiler settings to compile a
 * module hierarchy while supporting HISC.
 *
 * @see net.sourceforge.waters.model.compiler.ModuleCompiler ModuleCompiler
 *
 * @author Robi Malik
 */

public enum HISCCompileMode
{

  /**
   * Constant indicating that HISC is disabled (the default setting).
   */
  NOT_HISC,

  /**
   * Constant used when compiling a high-level module.
   * When compiling a high-level module, all automata are compiled.
   * Instances are accessed, and their modules are compiled as low-level
   * modules.
   */
  HISC_HIGH,

  /**
   * Constant used when compiling a low-level module.
   * When compiling a low-level module, only interface automata are
   * compiled and converted to plants. Other automata, variables, and
   * instances are ignored.
   */
  HISC_LOW;

}
