//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ModuleSubjectIntegrityChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.module.ModuleIntegrityChecker;


public class ModuleSubjectIntegrityChecker
  extends ModuleIntegrityChecker
{

  //#########################################################################
  //# Constructors
  public static ModuleSubjectIntegrityChecker getInstance()
  {
    if (theInstance == null) {
      theInstance = new ModuleSubjectIntegrityChecker();
    }
    return theInstance;
  }

  protected ModuleSubjectIntegrityChecker()
  {
    mHierarchyChecker = ModuleHierarchyChecker.getInstance();
  }


  //#########################################################################
  //# Invocation
  public void check(final DocumentProxy doc)
    throws Exception
  {
    final ModuleSubject module = (ModuleSubject) doc;
    check(module);
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


  //#########################################################################
  //# Class Variables
  private static ModuleSubjectIntegrityChecker theInstance = null;

}
