//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFAHelper
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.module.ScopeKind;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class EFAHelper {

  public EFAHelper(final ModuleProxyFactory factory,
                   final CompilerOperatorTable optable)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mCloner = ModuleSubjectFactory.getCloningInstance();
  }

  /**
   * Using operation table {(
   * <p/>
   * @CompilerOperatorTable)} instances
   * @param factory Factory to be used for components construction
   */
  public EFAHelper(final ModuleProxyFactory factory)
  {
    this(factory, CompilerOperatorTable.getInstance());
  }

  /**
   * Using subject factory {(
   * <p/>
   * @ModuleSubjectFactory)} and operation table {(
   * @CompilerOperatorTable)} instances
   */
  public EFAHelper()
  {
    this(ModuleSubjectFactory.getInstance(),
         CompilerOperatorTable.getInstance());
  }

  public Collection<EventDeclProxy> getEventDeclProxy(
   final Collection<SimpleEFAEventDecl> list)
  {
    final Collection<EventDeclProxy> decls =
     new THashSet<>(list.size());
    for (final SimpleEFAEventDecl e : list) {
      final IdentifierProxy identifier =
       mFactory.createSimpleIdentifierProxy(e.getName());
      final EventDeclProxy event =
       mFactory.createEventDeclProxy(identifier,
                                     e.getKind(),
                                     e.isObservable(),
                                     ScopeKind.LOCAL,
                                     e.getRanges(),
                                     null,
                                     null);
      decls.add(event);
    }
    return decls;
  }

  public SimpleIdentifierProxy getMarkingIdentifier()
  {
    return mFactory
     .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);

  }
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final ModuleProxyCloner mCloner;
}
