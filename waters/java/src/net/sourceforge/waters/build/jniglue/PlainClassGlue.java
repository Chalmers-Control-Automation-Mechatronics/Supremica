//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   PlainClassGlue
//###########################################################################
//# $Id: PlainClassGlue.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class PlainClassGlue extends ClassGlue {

  //#########################################################################
  //# Constructors
  PlainClassGlue(final String packname,
		 final String classname,
		 final ClassGlue baseclass,
		 final ErrorReporter reporter)
  {
    super(packname, classname, baseclass, reporter);
  }

  PlainClassGlue(final String packname,
		 final String classname,
		 final ClassGlue baseclass,
		 final ClassModifier mod,
		 final ErrorReporter reporter)
  {
    super(packname, classname, baseclass, mod, reporter);
  }


  //#########################################################################
  //# Simple Access
  boolean isEnum()
  {
    return false;
  }
   
}
