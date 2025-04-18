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

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class DirectReplacementRule extends SimplificationRule {

  //#########################################################################
  //# Constructors
  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy replacement,
                        final PlaceHolder placeholder)
  {
    super(template, placeholder);
    mReplacements = Collections.singletonList(replacement);
  }

  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy[] replacements,
                        final PlaceHolder placeholder)
  {
    super(template, placeholder);
    mReplacements = Arrays.asList(replacements);
  }

  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy replacement,
                        final PlaceHolder[] placeholders)
  {
    super(template, placeholders);
    mReplacements = Collections.singletonList(replacement);
  }
  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy[] replacements,
                        final PlaceHolder[] placeholders)
  {
    super(template, placeholders);
    mReplacements = Arrays.asList(replacements);
  }


  //#########################################################################
  //# Invocation Interface
  @Override
  boolean isMakingReplacement()
  {
    return true;
  }

  @Override
  void execute(final ConstraintPropagator propagator)
  {
    final ModuleProxyFactory factory = propagator.getFactory();
    final PlaceholderReplacementVisitor visitor =
      PlaceholderReplacementVisitor.getInstance();
    for (final SimpleExpressionProxy template : mReplacements) {
      final SimpleExpressionProxy replacement =
        visitor.replace(template, this, factory);
      propagator.addConstraint(replacement);
    }
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleExpressionProxy> mReplacements;

}
