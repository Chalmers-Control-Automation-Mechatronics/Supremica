//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentConditional
//###########################################################################
//# $Id: TemplateFragmentConditional.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class TemplateFragmentConditional extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentConditional(final String name,
			      final TemplateFragment thenpart,
			      final int lineno)
  {
    this(name, thenpart, null, lineno);
  }

  TemplateFragmentConditional(final String name,
			      final TemplateFragment thenpart,
			      final TemplateFragment elsepart,
			      final int lineno)
  {
    super(lineno);
    mName = name;
    mThen = thenpart;
    mElse = elsepart;
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }

  TemplateFragment getThen()
  {
    return mThen;
  }

  TemplateFragment getElse()
  {
    return mElse;
  }

  void setElse(final TemplateFragment elsepart)
  {
    mElse = elsepart;
  }


  //#########################################################################
  //# Code Generation
  void writeCppGlue(final CppGlueWriter writer, final TemplateContext context)
  {
    final ProcessorConditional processor =
      context.getProcessorConditional(mName);
    if (processor == null) {
      writer.reportError("Undefined instruction $IF-" + mName, getLineNo());
    } else if (processor.isConditionSatisfied()) {
      mThen.writeCppGlue(writer, context);
    } else if (mElse != null) {
      mElse.writeCppGlue(writer, context);
    }
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final TemplateFragment mThen;
  private TemplateFragment mElse;

}