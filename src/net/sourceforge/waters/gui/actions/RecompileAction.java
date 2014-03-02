//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   RecompileAction
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.compiler.CompilationObserver;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class RecompileAction
  extends WatersAction
  implements CompilationObserver
{

  //#########################################################################
  //# Constructor
  public RecompileAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Recompile");
    putValue(Action.SHORT_DESCRIPTION, "Force the module to be recompiled");
  }


  //#########################################################################
  //# Interface java.awt.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    final ModuleContainer container = getActiveModuleContainer();
    container.forceCompile(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.compiler.CompilationObserver
  @Override
  public void compilationSucceeded(final ProductDESProxy compiledDES)
  {
  }

  @Override
  public String getVerb()
  {
    return "compiled";
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3797986885054648213L;

}
