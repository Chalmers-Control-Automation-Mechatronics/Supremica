//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentSequence
//###########################################################################
//# $Id: Template.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;


class Template extends TemplateFragmentSequence {

  //#########################################################################
  //# Constructors
  Template()
  {
    this(null);
  }

  Template(final File infilename)
  {
    mInputFileName = infilename;
  }


  //#########################################################################
  //# Simple Access
  File getInputFileName()
  {
    return mInputFileName;
  }


  //#########################################################################
  //# Data Members
  private final File mInputFileName;

}