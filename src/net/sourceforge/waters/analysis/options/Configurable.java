//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.options;

import java.util.List;


/**
 * <P>An interface for algorithms and other objects that can be configured
 * through the options interface of Waters.</P>
 *
 * <P>After obtaining a configurable object, the code can call its
 * {@link #getOptions(OptionPage) getOptions()} method to obtain the
 * list of all supported options. Then after editing their values,
 * e.g., through their respective option editors ({@link OptionEditor}),
 * the values are sent to the configurable using {@link #setOption(Option)
 * setOption()}, before starting the algorithm.</P>
 *
 * @author Robi Malik
 */

public interface Configurable
{

  /**
   * Returns the options supported by this configurable.
   * @param  db   Option database containing parameters.
   * @return List of options from the given database that are supported
   *         by the configurable. The list should be ordered to support
   *         reasonable presentation to the user.
   */
  public List<Option<?>> getOptions(OptionPage db);

  /**
   * Configures the configurable using the given option. This method
   * retrieves the current value from the option and assigns it to
   * the configurable.
   * @param  option  The option to be used, which should be an element
   *                 of the list returned by a previous call to
   *                 {@link #getOptions(OptionPage) getOptions()}.
   */
  public void setOption(Option<?> option);

}
