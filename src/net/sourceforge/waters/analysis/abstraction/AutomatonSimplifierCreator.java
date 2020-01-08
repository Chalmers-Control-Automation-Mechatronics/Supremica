//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

/**
 * @author Benjamin Wheeler
 */

public abstract class AutomatonSimplifierCreator implements Configurable
{

  //#########################################################################
  //# Constructors
  protected AutomatonSimplifierCreator(final String name, final String description)
  {
    mName = name;
    mDescription = description;
  }


  //#########################################################################
  //# Override for java.lang.Object
  @Override
  public String toString()
  {
    return getName();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.options.Configurable
  /**
   * Returns the options supported by this configurable.
   * @param  db   Option database containing parameters.
   * @return List of options from the given database that are supported
   *         by the configurable. The list should be ordered to support
   *         reasonable presentation to the user.
   */
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = new LinkedList<Option<?>>();
    return options;
  }

  /**
   * Configures the configurable using the given option. This method
   * retrieves the current value from the option and assigns it to
   * the configurable.
   * @param  option  The option to be used, which should be an element
   *                 of the list returned by a previous call to
   *                 {@link #getOptions(OptionPage) getOptions()}.
   */
  @Override
  public void setOption(final Option<?> option)
  {
  }



  //#########################################################################
  //# Factory Methods
  /**
   * Returns the name of the tool created by this tool creator.
   */
  public String getName()
  {
    return mName;
  }

  /**
   * Returns the name of the tool created by this tool creator.
   */
  public String getDescription()
  {
    return mDescription;
  }

  /**
   * Creates a tool to be used by the given model analyser.
   */
  public abstract AutomatonBuilder createBuilder(final ProductDESProxyFactory factory);


  //#########################################################################
  //# Data Members
  private final String mName;
  private final String mDescription;

}
