//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentText
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class TemplateFragmentText extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentText(final String text, final int lineno)
  {
    super(lineno);
    mText = text;
  }


  //#########################################################################
  //# Simple Access
  String getText()
  {
    return mText;
  }


  //#########################################################################
  //# Code Generation
  void writeCppGlue(final CppGlueWriter writer, final TemplateContext context)
  {
    writer.print(mText);
  }


  //#########################################################################
  //# Data Members
  private final String mText;

}