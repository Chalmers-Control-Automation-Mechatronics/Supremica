//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.module.AbstractModuleConflictChecker;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMConflictChecker extends AbstractModuleConflictChecker
{

  /**
   * @param factory
   */
  public EFSMConflictChecker(final ModuleProxyFactory factory)
  {
    super(factory);
  }

  /**
   * @param model
   * @param factory
   */
  public EFSMConflictChecker(final ModuleProxy model, final ModuleProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * @param model
   * @param marking
   * @param factory
   */
  public EFSMConflictChecker(final ModuleProxy model, final IdentifierProxy marking,
                             final ModuleProxyFactory factory)
  {
    super(model, marking, factory);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  public int getInternalTransitionLimit()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public CompilerOperatorTable getOperatorTable()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
