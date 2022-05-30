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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.options.FileOption;


/**
 * <P>A text field to enter a file name.</P>
 *
 * <P>Performs validation checks to see whether file or directory is
 * readable or writable.</P>
 *
 * @author Robi Malik
 */

public class FileInputCell
  extends ValidatingTextCell<File>
{

  //#########################################################################
  //# Constructors
  public FileInputCell(final File defaultDirectory,
                       final FileOption.Type type)
  {
    super(new FileInputHandler(defaultDirectory, type));
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
                             final FileOption.Type type)
    {
      mDefaultDirectory = defaultDirectory;
      mType = type;
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
      Path path = Paths.get(text);
      if (!path.isAbsolute()) {
        final Path defaultPath = mDefaultDirectory.toPath();
        path = defaultPath.resolve(text);
      }
      switch (mType) {
      case INPUT_FILE:
        if (!Files.exists(path)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("The file '");
          builder.append(path.toString());
          builder.append("' does not exist.");
          throw new ParseException(builder.toString(), 0);
        } else if (!Files.isDirectory(path)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("The path '");
          builder.append(path.toString());
          builder.append("' is a directory and not a file.");
          throw new ParseException(builder.toString(), 0);
        } else if (!Files.isReadable(path)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("Cannot read from file '");
          builder.append(path.toString());
          builder.append("'.");
          throw new ParseException(builder.toString(), 0);
        }
        break;
      case OUTPUT_FILE:
        if (Files.isDirectory(path)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("The path '");
          builder.append(path.toString());
          builder.append("' is a directory and not a file.");
          throw new ParseException(builder.toString(), 0);
        } else if (!Files.exists(path)) {
          final Path parent = path.getParent();
          if (parent == null) { // Non-existing drive, e.g., X: on Windows
            final StringBuilder builder = new StringBuilder();
            builder.append("The path '");
            builder.append(path.toString());
            builder.append("' is not a file.");
            throw new ParseException(builder.toString(), 0);
          } else if (!Files.isDirectory(parent)) {
            final StringBuilder builder = new StringBuilder();
            builder.append("The directory '");
            builder.append(parent.toString());
            builder.append("' does not exist.");
            throw new ParseException(builder.toString(), 0);
          }
        } else if (!Files.isWritable(path)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("Cannot write to file '");
          builder.append(path.toString());
          builder.append("'.");
          throw new ParseException(builder.toString(), 0);
        }
        break;
      case DIRECTORY:
        if (!Files.exists(path)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("The directory '");
          builder.append(path.toString());
          builder.append("' does not exist.");
          throw new ParseException(builder.toString(), 0);
        } else if (!Files.isDirectory(path)) {
          final StringBuilder builder = new StringBuilder();
          builder.append("The path '");
          builder.append(path.toString());
          builder.append("' is not a directory.");
          throw new ParseException(builder.toString(), 0);
        }
        break;
      default:
        break;
      }
      return path.toFile();
    }

    @Override
    public DocumentFilter getDocumentFilter()
    {
      return new DocumentFilter();
    }

    //#########################################################################
    //# Data Members
    private final File mDefaultDirectory;
    private final FileOption.Type mType;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -4165121235564896599L;

}
