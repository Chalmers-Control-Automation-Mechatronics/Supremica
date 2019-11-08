//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this oftware.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.automata.waters;

import java.util.List;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.kindtranslator.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;

import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.VerificationType;


/**
 * <P>A wrapper to invoke a language inclusion check using Supremica's
 * monolithic controllability check algorithm through the {@link
 * LanguageInclusionChecker} interface of Waters.</P>
 *
 * <P>The language inclusion check is mapped to a controllability check
 * using a {@link LanguageInclusionKindTranslator}, so that Supremica
 * recognises properties consistently to the Waters language inclusion
 * check.</P>
 *
 * @author Robi Malik
 */

public class SupremicaMonolithicLanguageInclusionChecker
  extends SupremicaMonolithicVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SupremicaMonolithicLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SupremicaMonolithicLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, factory,
          LanguageInclusionKindTranslator.getInstance(),
          VerificationType.CONTROLLABILITY, true);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.SafetyVerifier
  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    return (SafetyCounterExampleProxy) super.getCounterExample();
  }

  @Override
  public LanguageInclusionDiagnostics getDiagnostics()
  {
    return LanguageInclusionDiagnostics.getInstance();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, SupremicaModelAnalyzerFactory.OPTION_SupremicaSynchronousProductBuilder_EnsuringUncontrollablesInPlant);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(SupremicaModelAnalyzerFactory.OPTION_SupremicaSynchronousProductBuilder_EnsuringUncontrollablesInPlant)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setEnsuringUncontrollablesInPlant(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer
  @Override
  public void setUp()
    throws AnalysisException
  {
    super.setUp();
    boolean specEmpty = false;
    for (final Automaton aut : getSupremicaAutomata()) {
      if (aut.getInitialState() == null) {
        switch (aut.getKind()) {
        case PLANT:
          setBooleanResult(true);
          return;
        case SPEC:
          specEmpty = true;
          break;
        default:
          break;
        }
      }
    }
    if (specEmpty) {
      setBooleanResult(false);
    }
  }

}
