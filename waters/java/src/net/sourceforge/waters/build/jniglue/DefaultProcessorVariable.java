//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   DefaultProcessorVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class DefaultProcessorVariable implements ProcessorVariable {

  //#########################################################################
  //# Constructors
  DefaultProcessorVariable(final String text)
  {
    mText = text;
  }

  DefaultProcessorVariable(final int number)
  {
    mText = Integer.toString(number);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.build.jniglue.ProcessorVariable
  public String getText()
  {
    return mText;
  }


  //#########################################################################
  //# Data Members
  private final String mText;

}