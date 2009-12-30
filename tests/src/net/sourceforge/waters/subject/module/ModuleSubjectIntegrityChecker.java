//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ModuleSubjectIntegrityChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.module.ModuleIntegrityChecker;
import net.sourceforge.waters.model.module.ModuleProxy;


public class ModuleSubjectIntegrityChecker
  extends ModuleIntegrityChecker
{

  //#########################################################################
  //# Constructors
  public static ModuleSubjectIntegrityChecker
    getModuleSubjectIntegrityCheckerInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    static final ModuleSubjectIntegrityChecker INSTANCE =
      new ModuleSubjectIntegrityChecker();
  }


  //#########################################################################
  //# Constructor
  protected ModuleSubjectIntegrityChecker()
  {
    mHierarchyChecker = ModuleHierarchyChecker.getInstance();
  }


  //#########################################################################
  //# Invocation
  public void check(final ModuleProxy module)
    throws Exception
  {
    final ModuleSubject subject = (ModuleSubject) module;
    check(subject);
  }

  public void check(final ModuleSubject module)
    throws Exception
  {
    super.check(module);
    mHierarchyChecker.check(module);
  }


  //#########################################################################
  //# Data Members
  private final ModuleHierarchyChecker mHierarchyChecker;

}
