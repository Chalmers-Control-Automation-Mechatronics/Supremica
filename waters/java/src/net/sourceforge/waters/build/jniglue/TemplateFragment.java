//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragment
//###########################################################################
//# $Id: TemplateFragment.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


abstract class TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragment(final int lineno)
  {
    mLineNo = lineno;
  }


  //#########################################################################
  //# Simple Access
  int getLineNo()
  {
    return mLineNo;
  }


  //#########################################################################
  //# Code Generation
  abstract void writeCppGlue(final CppGlueWriter writer,
			     final TemplateContext context);


  //#########################################################################
  //# Data Members
  private final int mLineNo;

}