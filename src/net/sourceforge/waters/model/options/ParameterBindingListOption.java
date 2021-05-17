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

package net.sourceforge.waters.model.options;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * A configurable parameter of a {@link ModelAnalyzer}, which can hold a
 * {@link List} of {@link ParameterBindingProxy} objects.
 *
 * @author Robi Malik
 */

public class ParameterBindingListOption
  extends Option<List<ParameterBindingProxy>>
{
  //#########################################################################
  //# Constructors
  public ParameterBindingListOption(final String id,
                                    final String shortName,
                                    final String description,
                                    final String commandLineOption)
  {
    super(id, shortName, description, commandLineOption,
          Collections.emptyList(), new LinkedList<>());
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.Option
  @Override
  public OptionEditor<List<ParameterBindingProxy>>
  createEditor(final OptionContext context)
  {
    return context.createParameterBindingListEditor(this);
  }

  @Override
  public void set(final String text)
    throws ParseException
  {
    final String[] parts = text.split("=");
    if (parts.length != 2) {
      throw new ParseException("Can't convert '" + text +
                               "' to a parameter binding.", 0);
    }
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final ExpressionParser parser = new ExpressionParser(factory, optable);
    final SimpleExpressionProxy expr = parser.parse(parts[1]);
    final ParameterBindingProxy binding =
      factory.createParameterBindingProxy(parts[0], expr);
    getValue().add(binding);
  }

  @Override
  public void restoreDefaultValue()
  {
    getValue().clear();
  }

  @Override
  public boolean isPersistent()
  {
    return false;
  }

}
