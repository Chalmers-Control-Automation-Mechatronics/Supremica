//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragment
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


abstract class TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragment(final int lineno)
  {
    this(0, lineno);
  }

  TemplateFragment(final int numskipped, final int lineno)
  {
    mLineNo = lineno;
    mNumSkippedFrames = numskipped;
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
  //# Auxiliary Methods
  TemplateContext getRelevantContext(final TemplateContext context)
  {
    TemplateContext result = context;
    for (int i = 0; i < mNumSkippedFrames; i++) {
      result = result.getParent();
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final int mLineNo;
  private final int mNumSkippedFrames;

}
