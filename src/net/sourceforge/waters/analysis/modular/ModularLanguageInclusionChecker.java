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

package net.sourceforge.waters.analysis.modular;

import java.util.List;

import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;


/**
 * <P>The modular language inclusion check algorithm.,
 * implemented based on {@link AbstractModularVerifier}</P>
 *
 * <P><I>Reference:</I><BR>
 * Bertil A. Brandin, Robi Malik, Petra Malik. Incremental verification
 * and synthesis of discrete-event systems guided by counter-examples.
 * IEEE Transactions on Control Systems Technology,
 * <STRONG>12</STRONG>&nbsp;(3), 387&ndash;401, 2004.</P>
 *
 * @author Simon Ware, Robi Malik
 */

public class ModularLanguageInclusionChecker
  extends AbstractModularSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructor
  public ModularLanguageInclusionChecker(final ProductDESProxyFactory factory,
                                         final SafetyVerifier mono)
  {
    this(null, factory, mono);
  }

  public ModularLanguageInclusionChecker(final ProductDESProxy model,
                                         final ProductDESProxyFactory factory,
                                         final SafetyVerifier mono)
  {
    super(model,
          factory,
          LanguageInclusionKindTranslator.getInstance(),
          mono);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, ModularModelVerifierFactory.
                       OPTION_ModularLanguageInclusionChecker_Chain);
    return options;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.SafetyVerifier
  @Override
  public LanguageInclusionDiagnostics getDiagnostics()
  {
    return LanguageInclusionDiagnostics.getInstance();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.modular.AbstractModularVerifier
  @Override
  protected boolean isMultiSpecsEnabled()
  {
    return false;
  }

}
