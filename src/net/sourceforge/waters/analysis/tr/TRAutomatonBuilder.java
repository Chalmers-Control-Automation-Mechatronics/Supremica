package net.sourceforge.waters.analysis.tr;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TRAutomatonBuilder extends AbstractAutomatonBuilder
{

  public TRAutomatonBuilder(final ProductDESProxyFactory factory, final TransitionRelationSimplifier simp) {
    super(factory);

    mSimp = simp;
    mFactory = factory;
  }

  @Override
  public void setModel(final AutomatonProxy aut) {
    try {
      mTrAut = TRAutomatonProxy.createTRAutomatonProxy(aut);

      final ProductDESProxy des = AutomatonTools.createProductDESProxy(mTrAut, mFactory);
      super.setModel(des);

    } catch (final OverflowException exception) {
      final Logger logger = LogManager.getLogger();
      logger.error(exception.getMessage());
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public void setModel(final ProductDESProxy des)
  {
    final AutomatonProxy aut = des.getAutomata().iterator().next();
    setModel(aut);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();

      final ListBufferTransitionRelation tr = mTrAut.getTransitionRelation();
      mSimp.setTransitionRelation(tr);
      mSimp.run();

      return this.setProxyResult(mTrAut);

    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    mTrAut = null;
    mSimp = null;
    mFactory = null;
    super.tearDown();
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public void setOption(final Option<?> option)
  {

    if (option.hasID(TRSimplifierFactory.OPTION_AbstractMarking_PreconditionMarkingID)) {
      final PropositionOption propOption = (PropositionOption) option;
      mPreconditionMarkingIDFromOption = propOption.getIntegerValue(mTrAut.getEventEncoding());
      mSimp.setPropositions(mPreconditionMarkingIDFromOption, mDefaultMarkingIDFromOption);
    }
    else if (option.hasID(TRSimplifierFactory.OPTION_AbstractMarking_DefaultMarkingID)) {
      final PropositionOption propOption = (PropositionOption) option;
      mDefaultMarkingIDFromOption = propOption.getIntegerValue(mTrAut.getEventEncoding());
      mSimp.setPropositions(mPreconditionMarkingIDFromOption, mDefaultMarkingIDFromOption);
    }
    else mSimp.setOption(option);
  }

  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    return mSimp.getOptions(db);
  }

  private TRAutomatonProxy mTrAut;
  private TransitionRelationSimplifier mSimp;
  private ProductDESProxyFactory mFactory;

  private int mPreconditionMarkingIDFromOption = -1;
  private int mDefaultMarkingIDFromOption = -1;

}
