//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.algorithms
//# CLASS:   AlgorithmsTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.samples.algorithms;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;

import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;

import net.sourceforge.waters.junit.AbstractWatersTest;

import org.xml.sax.SAXException;


public class AlgorithmsTest extends AbstractWatersTest
{

  //#########################################################################
  //# Test Cases
  public void testHide_machine_machine_break()
    throws IOException, JAXBException, WatersException
  {
    final String[] hidden = {"break"};
    final String name = "machine";
    final ProductDESProxy des1 = compileHandwritten(name);
    final Collection<EventProxy> events = des1.getEvents();
    final Collection<AutomatonProxy> automata = des1.getAutomata();
    final Collection<AutomatonProxy> newautomata =
      new LinkedList<AutomatonProxy>(automata);
    final AutomatonProxy aut2 = hide(des1, name, hidden, false);
    newautomata.add(aut2);
    final ProductDESProxy des2 = mProductDESFactory.createProductDESProxy
      (name, events, newautomata);
    save(des2, aut2);
    final ProductDESProxy expected = getExpectedHandwritten(aut2);
    testExpected(aut2, expected);
    final AutomatonProxy aut3 = reduce(aut2);
    testExpected(aut3, expected);
  }

  public void testHide_machine_machine_break_repair()
    throws IOException, JAXBException, WatersException
  {
    final String[] hidden = {"break", "repair"};
    final String name = "machine";
    final ProductDESProxy des1 = compileHandwritten(name);
    final Collection<EventProxy> events = des1.getEvents();
    final Collection<AutomatonProxy> automata = des1.getAutomata();
    final Collection<AutomatonProxy> newautomata =
      new LinkedList<AutomatonProxy>(automata);
    final AutomatonProxy aut2 = hide(des1, name, hidden, false);
    newautomata.add(aut2);
    final ProductDESProxy des2 = mProductDESFactory.createProductDESProxy
      (name, events, newautomata);
    save(des2, aut2);
    final ProductDESProxy expected = getExpectedHandwritten(aut2);
    testExpected(aut2, expected);
    final AutomatonProxy aut3 = reduce(aut2);
    testExpected(aut3, expected);
  }

  public void testHide_linwon90_subsystem()
    throws IOException, JAXBException, WatersException,
           TransformerConfigurationException
  {
    final String subdirname = "linwon90";
    final String name = "subsystem";
    final String[] hidden = {"put[1]"};
    final ProductDESProxy des1 = compileValid(subdirname, name);
    final Collection<EventProxy> events = des1.getEvents();
    final Collection<AutomatonProxy> automata = des1.getAutomata();
    final Collection<AutomatonProxy> newautomata =
      new LinkedList<AutomatonProxy>(automata);
    final AutomatonProxy aut2 = hide(des1, name, hidden, false);
    newautomata.add(aut2);
    final ProductDESProxy des2 = mProductDESFactory.createProductDESProxy
      (name, events, newautomata);
    save(des2, aut2);
    final ProductDESProxy expected = getExpectedValid(subdirname, aut2);
    testExpected(aut2, expected);
    final AutomatonProxy aut3 = reduce(aut2, ":reduced");
    newautomata.add(aut3);
    final ProductDESProxy des3 = mProductDESFactory.createProductDESProxy
      (name, events, newautomata);
    save(des3, aut2);
    testExpected(aut3, expected);
    final AutomatonProxy aut4 = reduce(aut3);
    testExpected(aut4, expected);
  }

  public void testHide_linwon90_hugo11()
    throws IOException, JAXBException, WatersException,
           TransformerConfigurationException
  {
    final String subdirname = "linwon90";
    final String name = "hugo11";
    final String[] hidden = {"tau1", "tau2"};
    final ProductDESProxy des1 = compileValid(subdirname, name);
    final Collection<EventProxy> events = des1.getEvents();
    final Collection<AutomatonProxy> automata = des1.getAutomata();
    final Collection<AutomatonProxy> newautomata =
      new LinkedList<AutomatonProxy>(automata);
    final AutomatonProxy aut2 = hide(des1, name, hidden, false);
    newautomata.add(aut2);
    final ProductDESProxy des2 = mProductDESFactory.createProductDESProxy
      (name, events, newautomata);
    save(des2, aut2);
    final ProductDESProxy expected = getExpectedValid(subdirname, aut2);
    testExpected(aut2, expected);
    final AutomatonProxy aut3 = reduce(aut2, ":reduced");
    newautomata.add(aut3);
    final ProductDESProxy des3 = mProductDESFactory.createProductDESProxy
      (name, events, newautomata);
    save(des3, aut2);
    testExpected(aut3, expected);
    final AutomatonProxy aut4 = reduce(aut3);
    testExpected(aut4, expected);
  }


  //#########################################################################
  //# Utilities
  private ProductDESProxy compileHandwritten(final String modname)
    throws IOException, JAXBException, WatersException
  {
    final String inextname = modname + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(mWatersInputDirectory, inextname);
    final URI uri = infilename.toURI();
    final ModuleProxy module = (ModuleProxy) mModuleMarshaller.unmarshal(uri);
    return compile(module);
  }

  private ProductDESProxy compileValid(final String subdirname,
                                       final String modname)
    throws IOException, JAXBException, WatersException,
           TransformerConfigurationException
  {
    final String inextname = modname + "_main.vmod";
    final File indirname = new File(mValidInputDirectory, subdirname);
    final File infilename = new File(indirname, inextname);
    final URI uri = infilename.toURI();
    final ModuleProxy module = mValidUnmarshaller.unmarshal(uri);
    return compile(module);
  }

  private ProductDESProxy compile(final ModuleProxy module)
    throws WatersException
  {
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    return compiler.compile();
  }

  private AutomatonProxy hide(final ProductDESProxy des,
                              final String autname,
                              final String[] hiddennames,
                              final boolean conflicteq)
    throws WatersException
  {
    final Collection<EventProxy> hidden = new LinkedList<EventProxy>();
    final StringBuffer hiddenbuffer = new StringBuffer(autname);
    for (int i = 0; i < hiddennames.length; i++) {
      final String name = hiddennames[i];
      final EventProxy event = findEvent(des, name);
      hidden.add(event);
      hiddenbuffer.append(':');
      hiddenbuffer.append(name);
    }
    final String hiddenname = hiddenbuffer.toString();
    final AutomatonProxy aut = findAutomaton(des, autname);
    return Hiding.hide
      (aut, hidden, hiddenname, conflicteq, mProductDESFactory);
  }

  private AutomatonProxy reduce(final AutomatonProxy aut)
    throws WatersException
  {
    final String name = aut.getName();
    return reduce(aut, name);
  }

  private AutomatonProxy reduce(final AutomatonProxy aut,
                                final String name)
    throws WatersException
  {
    return Bisimulation.reduce(aut, name, mProductDESFactory);
  }

  @SuppressWarnings("unused")
  private AutomatonProxy iterativeReduce(final AutomatonProxy aut)
    throws WatersException
  {
    final String name = aut.getName();
    int numstates = aut.getStates().size();
    AutomatonProxy reduced =
      Bisimulation.reduce(aut, name, mProductDESFactory);
    reduced = ConflictEquiv.reduce(reduced, name, mProductDESFactory);
    int newnumstates = reduced.getStates().size();
    while (newnumstates < numstates) {
      numstates = newnumstates;
      reduced = Bisimulation.reduce(reduced, name, mProductDESFactory);
      newnumstates = reduced.getStates().size();
      if (newnumstates == numstates) {
        break;
      }
      numstates = newnumstates;
      reduced = ConflictEquiv.reduce(reduced, name, mProductDESFactory);
      newnumstates = reduced.getStates().size();
    }
    return reduced;
  }


  @SuppressWarnings("unused")
  private void save(final ProductDESProxy des)
    throws IOException, JAXBException, WatersException
  {
    save(des, null);
  }

  private void save(final ProductDESProxy des, final AutomatonProxy namegiver)
    throws IOException, JAXBException, WatersException
  {
    final String name;
    if (namegiver != null) {
      name = namegiver.getName();
    } else {
      name = des.getName();
    }
    final String filename = eliminateStrangeCharacters(name);
    final String outextname =
      filename + mProductDESMarshaller.getDefaultExtension();
    final File outfilename = new File(mOutputDirectory, outextname);
    ensureParentDirectoryExists(outfilename);
    mProductDESMarshaller.marshal(des, outfilename);
  }                     

  @SuppressWarnings("unused")
  private Collection<EventProxy> getEvents(final ProductDESProxy des,
					   final String[] names)
    throws WatersException
  {
    final Collection<EventProxy> events = new LinkedList<EventProxy>();
    for (int i = 0; i < names.length; i++) {
      final String name = names[i];
      final EventProxy event = findEvent(des, name);
      events.add(event);
    }
    return events;
  }

  private ProductDESProxy getExpectedHandwritten(final AutomatonProxy aut)
    throws IOException, JAXBException, WatersException
  {
    final String name = aut.getName();
    final String filename = eliminateStrangeCharacters(name);
    final String extname =
      filename + mProductDESMarshaller.getDefaultExtension();
    final File testfilename = new File(mWatersInputDirectory, extname);
    final URI uri = testfilename.toURI();
    return mProductDESMarshaller.unmarshal(uri);
  }

  private ProductDESProxy getExpectedValid(final String subdirname,
                                           final AutomatonProxy aut)
    throws IOException, JAXBException, WatersException
  {
    final String name = aut.getName();
    final String filename = eliminateStrangeCharacters(name);
    final String extname =
      filename + mProductDESMarshaller.getDefaultExtension();
    final File indirname = new File(mValidInputDirectory, subdirname);
    final File testfilename = new File(indirname, extname);
    final URI uri = testfilename.toURI();
    return mProductDESMarshaller.unmarshal(uri);
  }

  private void testExpected(final AutomatonProxy aut,
                            final ProductDESProxy expected)
    throws WatersException
  {
    final String name = aut.getName();
    final AutomatonProxy expectedaut = findAutomaton(expected, name);
    assertTrue("Unexpected result!", aut.equalsByContents(expectedaut));
  }

  private String eliminateStrangeCharacters(final String name)
  {
    final StringBuffer buffer = new StringBuffer(name);
    final int len = buffer.length();
    for (int i = 0; i < len; i++) {
      final char ch = buffer.charAt(i);
      if (!Character.isJavaIdentifierPart(ch)) {
        buffer.setCharAt(i, '_');
      }
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Auxiliary Methods
  private static EventProxy findEvent(final ProductDESProxy des,
                                      final String name)
    throws NameNotFoundException
  {
    for (final EventProxy event : des.getEvents()) {
      if (event.getName().equals(name)) {
        return event;
      }
    }
    throw new NameNotFoundException
      ("DES '" + des.getName() + "' does not have any event named '" +
       name + "'!");
  }

  private static AutomatonProxy findAutomaton(final ProductDESProxy des,
                                              final String name)
    throws NameNotFoundException
  {
    for (final AutomatonProxy automaton : des.getAutomata()) {
      if (automaton.getName().equals(name)) {
        return automaton;
      }
    }
    throw new NameNotFoundException
      ("DES '" + des.getName() + "' does not have any automaton named '" +
       name + "'!");
  }



  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException, SAXException
  {
    mWatersInputDirectory = new File(getWatersInputRoot(), "handwritten");
    mValidInputDirectory = new File(getWatersInputRoot(), "valid");
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mValidUnmarshaller = new ValidUnmarshaller(moduleFactory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mValidUnmarshaller);
  }

  protected void tearDown()
  {
    mWatersInputDirectory = null;
    mValidInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mValidUnmarshaller = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private File mWatersInputDirectory;
  private File mValidInputDirectory;
  private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private ValidUnmarshaller mValidUnmarshaller;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;

}
