//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A monolithic conflict checker implementation, written in C++.</P>
 *
 * <P><STRONG>Supported Featues.</STRONG></P>
 *
 * <P>This implementation supports both deterministic and nondeterministic
 * models. Counterexamples are computed for all blocking models, and
 * efforts are made to distinguish between deadlock and livelock. If a
 * blocking model contains a state without any outgoing transitions, or
 * with only selfloops outgoing, a trace marked {@link
 * net.sourceforge.waters.xsd.des.ConflictKind#DEADLOCK DEADLOCK} leading
 * to that state is returned; otherwise a shortest trace marked {@link
 * net.sourceforge.waters.xsd.des.ConflictKind#LIVELOCK LIVELOCK} leading
 * to a non-coreachable state is returned.</P>
 *
 * <P><STRONG>Algorithm.</STRONG></P>
 *
 * <P>This algorithm proceeds in two passes. In the first pass, the entire
 * state space of the synchronous product is constructed and stored, and in
 * the second pass, a backwards search starting from the marked states is
 * performed to determine whether all the states constructed in the first
 * pass are coreachable.</P>
 *
 * <P>State tuples are stored in bit-packed form in a hash table. The number
 * and size of the state tuples is the main limiting factor in terms of
 * memory requirements. The number of states can be limited by specifying
 * the node limit ({@link #setNodeLimit(int) setNodeLimit()}).</P>
 *
 * <P>The algorithm can be configured to choose between two different
 * approaches for the coreachability search in the second pass. In the
 * first approach, all transitions encountered in the first pass are stored
 * and used to speed up the coreachability search in the second pass. In
 * the second approach, no transitions are stored in the first pass, and the
 * reverse transitions are calculated from the component automata in the
 * second pass. The first approach (storing transitions) can be up to ten
 * times faster than the second approach, but it requires up to&nbsp;100
 * times more memory. The approach to be taken is specified using the
 * {@link #setTransitionLimit(int) setTransitionLimit()} configuration
 * option, which is also used to limit the maximum number of transitions
 * that can be stored for the first approach (storing transitions).</P>
 *
 * <P>The algorithm to determine which transitions are enabled is highly
 * optimised. A lot of effort is taken to suppress the exploration of
 * disabled transitions or transitions known to be selfloops early.</P>
 *
 * <P><STRONG>Configuration.</STRONG></P>
 *
 * <UL>
 * <LI>If the node limit is specified ({@link #setNodeLimit(int)
 *     setNodeLimit()}), it defines the maximum number of states that can
 *     be constructed. If the synchronous product state space turns out to
 *     be larger during the first pass, the verification attempt is aborted
 *     and an {@link net.sourceforge.waters.model.analysis.OverflowException
 *     OverflowException} is thrown.</LI>
 * <LI>If the transition limit is specified ({@link #setTransitionLimit(int)
 *     setTransitionLimit()}, it defines the maximum number of transitions
 *     that can be stored. If the transition limit is set to&nbsp;0, no
 *     transitions will be stored during the first pass, and the reverse
 *     transition relation will be computed from the component transitions
 *     in the second pass. If the transition limit is nonzero, all
 *     transitions discovered during the first pass (except selfloops and
 *     multiple transitions) will be stored and used for faster
 *     coreachability computation in the second pass. If the synchronous
 *     product turns out to include more transitions than specified by the
 *     transition limit, the verification attempt is aborted and an {@link
 *     net.sourceforge.waters.model.analysis.OverflowException
 *     OverflowException} is thrown.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class NativeConflictChecker
  extends NativeModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public NativeConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeConflictChecker(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  public NativeConflictChecker(final ProductDESProxy model,
                               final EventProxy marking,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mMarking = marking;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public void setModel(final ProductDESProxy model)
  {
    super.setModel(model);
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  public void setMarkingProposition(EventProxy marking)
  {
    mMarking = marking;
    mUsedMarking = null;
    clearAnalysisResult();
  }

  public EventProxy getMarkingProposition()
  {
    return mMarking;
  }

  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Auxiliary Methods
  public EventProxy getUsedMarkingProposition()
  {
    if (mUsedMarking == null) {
      if (mMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedMarking = mMarking;
      }
    }
    return mUsedMarking;
  }

  public KindTranslator getKindTranslator()
  {
    return ConflictKindTranslator.getInstance();
  }

  
  //#########################################################################
  //# Native Methods
  native VerificationResult runNativeAlgorithm();

  public String getTraceName()
  {
    return getModel().getName() + ":conflicting";
  }

    
  //#########################################################################
  //# Data Members
  private EventProxy mMarking;
  private EventProxy mUsedMarking;

}
