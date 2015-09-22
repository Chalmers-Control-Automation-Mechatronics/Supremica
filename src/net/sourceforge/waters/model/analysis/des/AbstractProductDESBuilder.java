//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * An abstract base class to facilitate the implementation of product DES
 * builders. In addition to the model and factory members inherited from
 * {@link AbstractModelAnalyzer}, this class provides access to a product DES
 * result member, and uses this to implement access to the computed
 * result.
 *
 * @author Robi Malik
 */

public abstract class AbstractProductDESBuilder
  extends AbstractModelBuilder<ProductDESProxy>
  implements ProductDESBuilder
{

  //#########################################################################
  //# Constructors
  public AbstractProductDESBuilder(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public AbstractProductDESBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public AbstractProductDESBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  public AbstractProductDESBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory)
  {
    super(aut, factory);
  }

  public AbstractProductDESBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator)
  {
    super(aut, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  @Override
  public ProductDESResult getAnalysisResult()
  {
    return (ProductDESResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  public ProductDESResult createAnalysisResult()
  {
    return new DefaultProductDESResult();
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores a product DES result containing a single automaton,
   * indicating successful completion. Setting the result also marks the
   * analysis run as completed and sets the Boolean result.
   * @param  aut    The computed automaton, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  protected boolean setAutomatonResult(final AutomatonProxy aut)
  {
    if (aut == null) {
      return setBooleanResult(false);
    } else {
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy(aut, factory);
      return setProductDESResult(des);
    }
  }

  /**
   * Stores a product DES result indicating successful completion.
   * Setting the product DES also marks the analysis run as completed and
   * sets the Boolean result.
   * @param  des    The computed product DES, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  protected boolean setProductDESResult(final ProductDESProxy des)
  {
    return setProxyResult(des);
  }

}









