//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFASynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class UnifiedEFASynchronousProductBuilderTest
  extends AbstractAnalysisTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(UnifiedEFASynchronousProductBuilderTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testSmallFactory2() throws Exception
  {
    final ModuleProxy module =
      loadModule("tests", "syncprod", "small_factory_2");
    synchronizeAndTest(module);
  }

  public void testSmallFactory2u() throws Exception
  {
    final ModuleProxy module =
      loadModule("tests", "syncprod", "small_factory_2u");
    synchronizeAndTest(module);
  }

  public void testNondeterministicCombinations() throws Exception
  {
    final ModuleProxy module =
      loadModule("tests", "syncprod", "nondeterministic_combinations");
    synchronizeAndTest(module);
  }


  //#########################################################################
  //# Customisation
  void configure(final UnifiedEFACompiler compiler)
  {
    compiler.setSourceInfoEnabled(true);
  }


  //#########################################################################
  //# Utilities
  private void synchronizeAndTest(final ModuleProxy module)
    throws IOException, WatersException
  {
    final List<Proxy> components = module.getComponentList();
    final int numComponents = components.size();
    final List<Proxy> inputComponents = new ArrayList<>(numComponents);
    SimpleComponentProxy expectedComp =  null;
    for (final Proxy proxy : components) {
      if (proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        if (comp.getName().equals(EXPECTED_NAME)) {
          expectedComp = comp;
          continue;
        }
      }
      inputComponents.add(proxy);
    }
    assertNotNull("Expected result automaton with name '" + EXPECTED_NAME +
                  "' not found in input module!", expectedComp);

    final ModuleProxyFactory factory = getModuleProxyFactory();
    final String name = module.getName();
    final String comment = module.getComment();
    final Collection<? extends ConstantAliasProxy> constantAliasList =
      module.getConstantAliasList();
    final Collection<EventDeclProxy> eventDeclList = module.getEventDeclList();
    final Collection<? extends Proxy> eventAliasList = module.getEventAliasList();
    final ModuleProxy inputModule = factory.createModuleProxy
      (name, comment, null, constantAliasList,
       eventDeclList, eventAliasList, inputComponents);

    final UnifiedEFACompiler efaCompiler =
      new UnifiedEFACompiler(mDocumentManager, inputModule);
    configure(efaCompiler);
    final UnifiedEFASystem system = efaCompiler.compile();
    final UnifiedEFASynchronousProductBuilder synchBuilder =
      new UnifiedEFASynchronousProductBuilder();
    synchBuilder.setInputTransitionRelations(system.getTransitionRelations());
    synchBuilder.run();
    final UnifiedEFATransitionRelation tr = synchBuilder.getSynchronousProduct();
    final SimpleComponentProxy comp = mImporter.importTransitionRelation(tr);
    final List<SimpleComponentProxy> compList = Collections.singletonList(comp);
    final ModuleProxy outputModule =
      mModuleFactory.createModuleProxy(name, null, null, null,
                                       module.getEventDeclList(), null,
                                       compList);
    final File outputDirectory = getOutputDirectory();
    final String extension = mModuleMarshaller.getDefaultExtension();
    final File outputFile = new File(outputDirectory, name + extension);
    mModuleMarshaller.marshal(outputModule, outputFile);
    final ModuleCompiler outputCompiler = new ModuleCompiler
      (mDocumentManager, getProductDESProxyFactory(), outputModule);
    final ProductDESProxy des = outputCompiler.compile();
    final AutomatonProxy aut = des.getAutomata().iterator().next();

    final List<SimpleComponentProxy> expectedList =
      Collections.singletonList(expectedComp);
    final ModuleProxy expectedModule = factory.createModuleProxy
      (name, comment, null, constantAliasList,
       eventDeclList, eventAliasList, expectedList);
    final ModuleCompiler expectedCompiler = new ModuleCompiler
      (mDocumentManager, getProductDESProxyFactory(), expectedModule);
    final ProductDESProxy expectedDES = expectedCompiler.compile();
    final AutomatonProxy expectedAut = expectedDES.getAutomata().iterator().next();
    mIsomorphismChecker.checkIsomorphism(aut, expectedAut);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mModuleFactory = ModuleElementFactory.getInstance();
    mOperatorTable = CompilerOperatorTable.getInstance();
    mModuleMarshaller =
      new JAXBModuleMarshaller(mModuleFactory, mOperatorTable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mIsomorphismChecker = new IsomorphismChecker(getProductDESProxyFactory(), true, true);
    mImporter = new UnifiedEFASystemImporter(mModuleFactory, mOperatorTable);

  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mModuleFactory = null;
    mOperatorTable = null;
    mModuleMarshaller = null;
    mDocumentManager = null;
    mIsomorphismChecker = null;
    mImporter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mModuleFactory;
  private CompilerOperatorTable mOperatorTable;
  private JAXBModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;

  private IsomorphismChecker mIsomorphismChecker;
  private UnifiedEFASystemImporter mImporter;


  //#########################################################################
  //# Class Constants
  private static final String EXPECTED_NAME = "sync";

}

