//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   StandardExtensionFileFilter
//###########################################################################
//# $Id: StandardExtensionFileFilter.java,v 1.3 2007-06-26 12:59:14 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.filechooser.FileFilter;


/**
 * A simple file filter for use with Swing's file chooser, to match
 * file names with a single extension.
 *
 * @see javax.swing.JFileChooser
 */

public class StandardExtensionFileFilter
  extends FileFilter
{

  //#########################################################################
  //# Singletons
  /**
   * Returns a file filter for a given extension. This method tries to
   * ensure that only one file filter object is created for any given type,
   * by creating a lookup table indexed by the extension. If a file filter
   * with a given extension is found, that object is returned, ignoring the
   * description.
   * @param  ext         The filename extension accepted by the filter,
   *                     with preceding dot.
   * @param  description A textual description of the files accepted by
   *                     this filter, to be displayed in the file chooser's
   *                     file type selection box.
   */
  public static StandardExtensionFileFilter getFilter(final String ext,
						      final String description)
  {
    StandardExtensionFileFilter filter = mFilterMap.get(ext);
    if (filter == null) {
      filter = new StandardExtensionFileFilter(ext, description);
      mFilterMap.put(ext, filter);
    }
    return filter;
  }


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
  //# Overrides for Abstract Base Class javax.swing.filechooser.FileFilter
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
    return hasExtension(filename, mExtension);
  }

  /**
   * Ensures that the given file name has an extension.
   * If the file name part of the path does not include any dot character,
   * the filter's default extension is appended.
   * @param  file        The file name to be checked.
   * @return A file with extension appended if necessary.
   */
  public File ensureDefaultExtension(final File filename)
  {
    return ensureDefaultExtension(filename, mExtension);
  }


  //#########################################################################
  //# Static Extension Checking
  /**
   * Checks whether a file has the given extension.
   * @param  file        The file to be checked.
   * @param  ext         The extension looked for, with preceding dot.
   */ 
  public static boolean hasExtension(final File file, final String ext)
  {
    return hasExtension(file.getPath(), ext);
  }

  /**
   * Checks whether a file name string has the given extension.
   * @param  file        The file name to be checked.
   * @param  ext         The extension looked for, with preceding dot.
   */ 
  public static boolean hasExtension(final String filename, final String ext)
  {
    final int lastdot = filename.lastIndexOf('.');
    if (lastdot > 0 && lastdot < filename.length() - 1) {
      final String fileext = filename.substring(lastdot);
      return fileext.equalsIgnoreCase(ext);
    } else {
      return false;
    }
  }

  /**
   * Ensures that the given file name has an extension.
   * If the file name part of the path does not include any dot character,
   * the given extension is appended.
   * @param  file        The file name to be checked.
   * @param  ext         The default extension, with preceding dot.
   * @return A file with extension appended if necessary.
   */
  public static File ensureDefaultExtension(final File file, final String ext)
  {
    if (hasExtension(file, ext)) {
      return file;
    } else {
      return new File(file.getPath() + ext);
    }
  }


  //#########################################################################
  //# Data Members
  private final String mExtension;
  private final String mDescription;


  //#########################################################################
  //# Static Class Variables
  private static final Map<String,StandardExtensionFileFilter> mFilterMap =
    new HashMap<String,StandardExtensionFileFilter>();

}
