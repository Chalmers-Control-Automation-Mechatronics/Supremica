//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   DefaultProcessorVariable
//###########################################################################
//# $Id: DefaultProcessorVariable.java,v 1.1 2005-02-18 01:30:10 robi Exp $
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