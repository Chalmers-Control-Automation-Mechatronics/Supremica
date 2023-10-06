//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.options;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A configurable parameter of a {@link ModelAnalyzer} of <CODE>file</CODE> type.
 *
 * @author Brandon Bassett
 */

public class FileOption extends Option<File>
{

  //#########################################################################
  //# Inner Enumeration Type
  public enum Type { INPUT_FILE, OUTPUT_FILE, DIRECTORY };


  //#########################################################################
  //# Constructors
  public FileOption(final String id,
                    final String shortName,
                    final String description,
                    final String commandLineOption,
                    final Type type,
                    final FileFilter... filters)
  {
    this(id, shortName, description, commandLineOption,
         null, type, filters);
  }

  public FileOption(final String id,
                    final String shortName,
                    final String description,
                    final String commandLineOption,
                    final File defaultValue,
                    final Type type,
                    final FileFilter... filters)
  {
    super(id, shortName, description, commandLineOption, defaultValue);
    mType = type;
    mFileFilters = filters.length > 0 ? filters : null;
  }


  //#########################################################################
  //# Simple Access
  public Type getType()
  {
    return mType;
  }

  public FileFilter[] getFileFilters()
  {
    return mFileFilters;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.Option
  @Override
  public OptionEditor<File> createEditor(final OptionContext context)
  {
    return context.createFileEditor(this);
  }

  @Override
  public void set(final String text)
  {
    final File value = new File(text);
    setValue(value);
  }


  //#########################################################################
  //# Data Members
  private final Type mType;
  private final FileFilter[] mFileFilters;

}
