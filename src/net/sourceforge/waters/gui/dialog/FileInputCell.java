//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.dialog;

import java.io.File;
import java.text.ParseException;

import javax.swing.text.DocumentFilter;


/**
 * <P>A text field to enter a file name.</P>
 *
 * <P>Performs validation checks to see whether file is readable or
 * writable.</P>
 *
 * @author Robi Malik
 */

public class FileInputCell
  extends ValidatingTextCell<File>
{

  //#########################################################################
  //# Constructors
  public FileInputCell(final File defaultDirectory,
                       final boolean writing)
  {
    super(new FileInputHandler(defaultDirectory, writing));
  }


  //#########################################################################
  //# Simple Access
  public File getDefaultDirectory()
  {
    final FileInputHandler handler = (FileInputHandler) getInputHandler();
    return handler.getDefaultDirectory();
  }


  //#########################################################################
  //# Overrides for javax.swing.JFormattedTextField
  @Override
  public File getValue()
  {
    return (File) super.getValue();
  }


  //#########################################################################
  //# Inner Class FileInputHandler
  private static class FileInputHandler
    implements FormattedInputHandler<File>
  {
    //#######################################################################
    //# Constructors
    private FileInputHandler(final File defaultDirectory,
                             final boolean writing)
    {
      mDefaultDirectory = defaultDirectory;
      mWriting = writing;
    }

    //#######################################################################
    //# Simple Access
    private File getDefaultDirectory()
    {
      return mDefaultDirectory;
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.gui.dialog.FormattedInputHandler<File>
    @Override
    public String format(final Object value)
    {
      if (value == null) {
        return "";
      } else {
        return value.toString();
      }
    }

    @Override
    public File parse(final String text) throws ParseException
    {
	  if (text.equals("")) {
        return null;
      }
      File file = new File(text);
      if (!file.isAbsolute()) {
        file = new File(mDefaultDirectory, text);
      }
      if (mWriting) {
        final File parent = file.getParentFile();

//		System.err.println("Parsing: \"" + text + "\"");
//		System.err.println("File: \"" + file.toString() + "\"");
//      if(parent != null) System.err.println("Parent: \"" + parent.toString() + "\"");
//      else System.err.println("Parent is null!");

        if (parent != null && !parent.isDirectory()) { // guard against root folder
          final StringBuilder builder = new StringBuilder();
          builder.append("The folder '");
          builder.append(parent.toString());
          builder.append("' does not exist.");
          throw new ParseException(builder.toString(), 0);
        }
//        else if (file.exists() && java.nio.file.Files.isWritable(java.nio.file.Paths.get(file.toString())))
        else if (file.exists() && !file.canWrite())
        {
          final StringBuilder builder = new StringBuilder();
          builder.append("Cannot write to file '");
          builder.append(file.toString());
          builder.append("'.");
          throw new ParseException(builder.toString(), 0);
        }
      } else {
        if (!file.exists()) {
          final StringBuilder builder = new StringBuilder();
          builder.append("The file '");
          builder.append(file.toString());
          builder.append("' does not exist.");
          throw new ParseException(builder.toString(), 0);
        } else if (!file.canRead()) {
          final StringBuilder builder = new StringBuilder();
          builder.append("Cannot read from file '");
          builder.append(file.toString());
          builder.append("'.");
          throw new ParseException(builder.toString(), 0);
        }
      }
      return file;
    }

    @Override
    public DocumentFilter getDocumentFilter()
    {
      return new DocumentFilter();
    }

    //#########################################################################
    //# Data Members
    private final File mDefaultDirectory;
    private final boolean mWriting;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -4165121235564896599L;

}
