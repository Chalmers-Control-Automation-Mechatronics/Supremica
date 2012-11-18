//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeHISCCPInterfaceConsistencyAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.compositional.CompositionalSimplifier;
import net.sourceforge.waters.analysis.hisc.HISCCPInterfaceConsistencyChecker;
import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
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
 * The action to invoke the HISC-CP interface consistency check.
 *
 * @author Robi Malik
 */

public class AnalyzeHISCCPInterfaceConsistencyAction
  extends WatersAnalyzeAction
{

  //#########################################################################
  //# Constructor
  protected AnalyzeHISCCPInterfaceConsistencyAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Overrides for base class
  //# net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected String getCheckName()
  {
    return "HISC-CP Interface Consistency";
  }

  @Override
  protected String getFailureDescription()
  {
    return "is not interface consistent";
  }

  @Override
  protected ModelVerifier getModelVerifier
    (final ModelVerifierFactory factory,
     final ProductDESProxyFactory desFactory)
  {
    final ConflictChecker checker = factory.createConflictChecker(desFactory);
    if (checker == null) {
      return null;
    } else {
      final CompositionalSimplifier simplifier =
        new CompositionalSimplifier(desFactory);
      return new HISCCPInterfaceConsistencyChecker
        (desFactory, checker, simplifier);
    }
  }

  @Override
  protected String getSuccessDescription()
  {
    return "is interface consistent";
  }

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
