//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersAnalyzeHISCAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * An abstract class of analysis actions for HISC properties.
 * These actions require the module to be compiled separately with
 * HISC settings.
 *
 * @author Robi Malik
 */

public abstract class WatersAnalyzeHISCAction
  extends WatersAnalyzeAction
{

  //#########################################################################
  //# Constructor
  protected WatersAnalyzeHISCAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Overrides for base class
  //# net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected ProductDESProxy getCompiledDES()
    throws EvalException
  {
    final IDE ide = getIDE();
    if (ide == null) {
      return null;
    }
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final ModuleContainer mContainer = (ModuleContainer) container;
    final ModuleProxy module = mContainer.getModule();
    final DocumentManager manager = ide.getDocumentManager();
    final ProductDESProxyFactory desfactory =
      ProductDESElementFactory.getInstance();
    final ModuleCompiler compiler =
      new ModuleCompiler(manager, desfactory, module);
    compiler.setSourceInfoEnabled(false);
    compiler.setOptimizationEnabled(true);
    compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
    final List<String> accepting =
      Collections.singletonList(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(accepting);
    return compiler.compile();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
