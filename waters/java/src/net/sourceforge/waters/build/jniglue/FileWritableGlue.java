//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   FileWritableGlue
//###########################################################################
//# $Id: FileWritableGlue.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;


interface FileWritableGlue extends WritableGlue {

  public boolean isUpToDate(File rootdir, long outfiletime);

}
