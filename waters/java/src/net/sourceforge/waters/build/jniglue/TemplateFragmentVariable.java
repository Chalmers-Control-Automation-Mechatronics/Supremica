//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentVariable
//###########################################################################
//# $Id: TemplateFragmentVariable.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class TemplateFragmentVariable extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentVariable(final String name, final int lineno)
  {
    super(lineno);
    mName = name;
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }


  //#########################################################################
  //# Code Generation
  void writeCppGlue(final CppGlueWriter writer, final TemplateContext context)
  {
    final ProcessorVariable processor = context.getProcessorVariable(mName);
    if (processor != null) {
      final String text = processor.getText();
      writer.print(text);
    } else {
      writer.reportError("Undefined variable $" + mName, getLineNo());
    }
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}