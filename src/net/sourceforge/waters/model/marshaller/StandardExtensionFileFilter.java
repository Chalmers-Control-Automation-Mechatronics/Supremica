//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   StandardExtensionFileFilter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple file filter for use with Swing's file chooser, to match
 * file names with a single extension.
 *
 * @see javax.swing.JFileChooser
 */

public class StandardExtensionFileFilter
  extends javax.swing.filechooser.FileFilter
  implements java.io.FileFilter
{

  //#########################################################################
  //# Singletons
  /**
   * Returns a file filter for a given extension. This method tries to
   * ensure that only one file filter object is created for any given type,
   * by creating a lookup table indexed by the extension. If a file filter
   * with a given extension is found, that object is returned, ignoring the
   * description.
   * @param  description A textual description of the files accepted by
   *                     this filter, to be displayed in the file chooser's
   *                     file type selection box.
   * @param  ext         The filename extension accepted by the filter,
   *                     with preceding dot.
   */
  public static StandardExtensionFileFilter getFilter(final String description,
                                                      final String ext)
  {
    StandardExtensionFileFilter filter = mFilterMap.get(ext);
    if (filter == null) {
      filter = new StandardExtensionFileFilter(description, ext);
      mFilterMap.put(ext, filter);
    }
    return filter;
  }


  //#########################################################################
  //# Constructors
  /**
   * Creates a new file filter that accepts directories in addition to
   * files with the given extension.
   * @param  description A textual description of the files accepted by
   *                     this filter, to be displayed in the file chooser's
   *                     file type selection box.
   * @param  ext         The filename extension accepted by the filter,
   *                     with preceding dot.
   * @param  dirs        A flag, indicating that the filter accepts all
   *                     directories in addition to files with the given
   *                     extension.
   */
  public StandardExtensionFileFilter(final String description,
                                     final String ext)
  {
    this(description, ext, true);
  }

  /**
   * Creates a new file filter.
   * @param  description A textual description of the files accepted by
   *                     this filter, to be displayed in the file chooser's
   *                     file type selection box.
   * @param  ext         The filename extension accepted by the filter,
   *                     with preceding dot.
   * @param  dirs        A flag, indicating that the filter accepts all
   *                     directories in addition to files with the given
   *                     extension.
   */
  public StandardExtensionFileFilter(final String description,
                                     final String ext,
                                     final boolean dirs)
  {
    mExtension = ext;
    mDescription = description;
    mAcceptsDirectories = dirs;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class javax.swing.filechooser.FileFilter
  public boolean accept(final File file)
  {
    if (file.isDirectory()) {
      return mAcceptsDirectories;
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
  public File ensureDefaultExtension(final File file)
  {
    return ensureDefaultExtension(file, mExtension);
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
   * @param  filename    The file name to be checked.
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
  private final boolean mAcceptsDirectories;


  //#########################################################################
  //# Static Class Variables
  private static final Map<String,StandardExtensionFileFilter> mFilterMap =
    new HashMap<String,StandardExtensionFileFilter>();

}
