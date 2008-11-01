//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   SpaceProcessor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class SpaceProcessor implements ProcessorVariable {

  //#########################################################################
  //# Constructors
  SpaceProcessor(final String text)
  {
    mLength = text.length();
    mSpace = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.build.jniglue.ProcessorVariable
  public String getText()
  {
    if (mSpace == null) {
      final StringBuffer buffer = new StringBuffer(mLength);
      for (int i = 0; i < mLength; i++) {
	buffer.append(' ');
      }
      mSpace = buffer.toString();
    }
    return mSpace;
  }


  //#########################################################################
  //# Data Members
  private final int mLength;
  private String mSpace;

}
