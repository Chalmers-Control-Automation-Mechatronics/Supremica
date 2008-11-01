//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   FileWritableGlue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;


interface FileWritableGlue extends WritableGlue {

  public boolean isUpToDate(File rootdir, long outfiletime);

}
