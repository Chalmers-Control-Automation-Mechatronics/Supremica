//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.compiler
//# CLASS:   CompilationObserver
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.compiler;

import net.sourceforge.waters.model.des.ProductDESProxy;

public interface CompilationObserver
{

  /**
   * Called when the compilation has finished successfully.
   *
   * @param compiledDES The result of the compilation.
   */
  public void compilationSucceeded(ProductDESProxy compiledDES);

  /**
   * Returns a verb describing the observer's action. The verb should fit in
   * the sentence "The module cannot be <I>verb</I> because it has errors".
   */
  public String getVerb();

}
