//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   EnabledEventsCompositionalModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier factory to produce an enabled-events compositional
 * conflict checker.
 *
 * @author Robi Malik
 */

public class EnabledEventsCompositionalModelVerifierFactory
  extends CompositionalModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static EnabledEventsCompositionalModelVerifierFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final EnabledEventsCompositionalModelVerifierFactory INSTANCE =
      new EnabledEventsCompositionalModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private EnabledEventsCompositionalModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
    removeArgument("-method");
    addArgument(new LanguageInclusionArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public EnabledEventsCompositionalConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new EnabledEventsCompositionalConflictChecker(factory);
  }


  //#########################################################################
  //# Inner Class LanguageInclusionArgument
  private static class LanguageInclusionArgument
    extends CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private LanguageInclusionArgument()
    {
      super("-li", "State limit for language inclusion to find enabled events");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configure(final ModelAnalyzer analyzer)
    {
      final EnabledEventsCompositionalConflictChecker checker =
        (EnabledEventsCompositionalConflictChecker) analyzer;
      final int value = getValue();
      checker.setEnabledEventSearchStateLimit(value);
    }

  }

}
