//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   EnumClassGlue
//###########################################################################
//# $Id: EnumClassGlue.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class EnumClassGlue extends ClassGlue {

  //#########################################################################
  //# Constructors
  EnumClassGlue(final String packname,
		final String classname,
		final ClassGlue baseclass,
		final ErrorReporter reporter)
  {
    super(packname, classname, baseclass, ClassModifier.M_GLUE, reporter);
  }


  //#########################################################################
  //# Simple Access
  boolean isEnum()
  {
    return true;
  }
   
}
