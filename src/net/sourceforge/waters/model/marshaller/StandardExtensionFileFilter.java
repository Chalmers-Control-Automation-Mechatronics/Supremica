//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   StandardExtensionFileFilter
//###########################################################################
//# $Id: StandardExtensionFileFilter.java,v 1.1 2006-09-15 09:26:13 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * A simple file filter for use with Swings's file chooser, to match
 * file names with a single extension.
 *
 * @see javax.swing.JFileChooser
 */

public class StandardExtensionFileFilter
  extends FileFilter
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a new file filter.
   * @param  ext         The filename extension accepted by the filter,
   *                     with preceding dot.
   * @param  description A textual description of the files accepted by
   *                     this filter, to be displayed in the file chooser's
   *                     file type selection box.
   */
  public StandardExtensionFileFilter(final String ext,
				     final String description)
  {
    mExtension = ext;
    mDescription = description;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass javax.swing.filechooser.FileFilter
  public boolean accept(final File file)
  {
    if (file.isDirectory()) {
      return true;
    } else {
      final String filename = file.getName();
      return accept(filename);
    }
  }
    
  public String getDescription()
  {
    return mDescription;
  }
    

  //#########################################################################
  //# Convenience
  public boolean accept(final String filename)
  {
    final int lastdot = filename.lastIndexOf('.');
    if (lastdot > 0 && lastdot < filename.length() - 1) {
      final String ext = filename.substring(lastdot);
      return mExtension.equalsIgnoreCase(ext);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Data Members
  private final String mExtension;
  private final String mDescription;

}
