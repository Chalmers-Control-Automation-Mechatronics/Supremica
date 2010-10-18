//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   VariableOrdering
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * @author Robi Malik
 */

public enum VariableOrdering
{

  //#########################################################################
  //# Enumeration
  DEFAULT {
    Collection<AutomatonProxy> getOrder(final ProductDESProxy des,
                                        final KindTranslator translator)
    {
      final Collection<AutomatonProxy> automata = des.getAutomata();
      final int size = automata.size();
      final Collection<AutomatonProxy> reduced =
        new ArrayList<AutomatonProxy>(size);
      for (final AutomatonProxy aut : automata) {
        final ComponentKind kind = translator.getComponentKind(aut);
        if (kind == ComponentKind.PLANT || kind == ComponentKind.SPEC) {
          reduced.add(aut);
        }
      }
      return reduced;
    }
  },

  GREEDY {
    Collection<AutomatonProxy> getOrder(final ProductDESProxy des,
                                        final KindTranslator translator)
    {
      return new GreedyVariableOrdering(des, translator);
    }
  },

  FORCE {
    Collection<AutomatonProxy> getOrder(final ProductDESProxy des,
                                        final KindTranslator translator)
    {
      final Collection<AutomatonProxy> greedy =
        GREEDY.getOrder(des, translator);
      return new ForceVariableOrdering(des, greedy);
    }
  };

  //#########################################################################
  //# Abstract Methods
  abstract Collection<AutomatonProxy> getOrder(ProductDESProxy des,
                                               KindTranslator translator);

}
