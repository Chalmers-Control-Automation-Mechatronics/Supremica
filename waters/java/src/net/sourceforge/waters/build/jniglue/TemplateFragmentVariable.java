//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class TemplateFragmentVariable extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentVariable(final String name,
			   final int numskipped,
			   final int lineno)
  {
    super(numskipped, lineno);
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
  void writeCppGlue(final CppGlueWriter writer, TemplateContext context)
  {
    context = getRelevantContext(context);
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
