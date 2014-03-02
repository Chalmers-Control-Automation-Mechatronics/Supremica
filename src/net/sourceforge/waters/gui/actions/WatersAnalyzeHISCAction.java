//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersAnalyzeHISCAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.gui.compiler.CompilationDialog;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

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
  //# Interface java.awt.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    // TODO: Make this run in the background like normal compilation.
    final IDE ide = getIDE();
    final ModuleContainer container = getActiveModuleContainer();
    final ModuleProxy module = container.getModule();
    final DocumentManager manager = ide.getDocumentManager();
    final ProductDESProxyFactory desfactory =
      ProductDESElementFactory.getInstance();
    final ModuleCompiler compiler =
      new ModuleCompiler(manager, desfactory, module);
    compiler.setSourceInfoEnabled(false);
    compiler.setMultiExceptionsEnabled(true);
    compiler.setOptimizationEnabled(true);
    compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
    final List<String> accepting =
      Collections.singletonList(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(accepting);
    try {
      final ProductDESProxy des = compiler.compile();
      compilationSucceeded(des);
    } catch (final EvalException exception) {
      final CompilationDialog dialog = new CompilationDialog(ide, null);
      dialog.setEvalException(exception, getVerb());
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
