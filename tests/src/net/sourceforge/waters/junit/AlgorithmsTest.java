//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   AlgorithmsTest
//###########################################################################
//# $Id: AlgorithmsTest.java,v 1.1 2005-02-21 02:51:15 knut Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;
import junit.framework.TestCase;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESMarshaller;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.samples.algorithms.Bisimulation;
import net.sourceforge.waters.samples.algorithms.ConflictEquiv;
import net.sourceforge.waters.samples.algorithms.Hiding;
import net.sourceforge.waters.valid.ValidUnmarshaller;


public class AlgorithmsTest extends TestCase
{

  //#########################################################################
  //# Test Cases
  public void testHide_machine_machine_break()
    throws IOException, JAXBException, WatersException
  {
    final String[] events = {"break"};
    final ProductDESProxy des = compileHandwritten("machine");
    final AutomatonProxy hidden = hide(des, "machine", events, false);
    save(des, hidden);
    final ProductDESProxy expected = getExpectedHandwritten(hidden);
    testExpected(hidden, expected);
    final AutomatonProxy reduced = reduce(hidden);
    testExpected(reduced, expected);
  }

  public void testHide_machine_machine_break_repair()
    throws IOException, JAXBException, WatersException
  {
    final String[] events = {"break", "repair"};
    final ProductDESProxy des = compileHandwritten("machine");
    final AutomatonProxy hidden = hide(des, "machine", events, false);
    save(des, hidden);
    final ProductDESProxy expected = getExpectedHandwritten(hidden);
    testExpected(hidden, expected);
    final AutomatonProxy reduced = reduce(hidden);
    testExpected(reduced, expected);
  }

  public void testHide_linwon90_subsystem()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    final String subdirname = "linwon90";
    final String[] events = {"put[1]"};
    final ProductDESProxy des = compileValid(subdirname, "subsystem");
    final AutomatonProxy hidden = hide(des, "subsystem", events, false);
    save(des, hidden);
    final ProductDESProxy expected = getExpectedValid(subdirname, hidden);
    testExpected(hidden, expected);
    final AutomatonProxy reduced1 = reduce(des, hidden, ":reduced");
    save(des, hidden);
    testExpected(reduced1, expected);
    final AutomatonProxy reduced2 = reduce(reduced1);
    testExpected(reduced2, expected);
  }

  /*
  public void testHide_linwon90_projsystem_synth1()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    final String subdirname = "linwon90";
    final String[] events = {"tau"};
    final ProductDESProxy des = compileValid(subdirname, "projsystem_synth1");
    final AutomatonProxy hidden = hide(des, "synth1_vhide", events, true);
    //save(des, hidden);
    //final ProductDESProxy expected = getExpectedValid(subdirname, hidden);
    //testExpected(hidden, expected);
    final AutomatonProxy reduced = iterativeReduce(hidden);
    //save(des, hidden);
    //testExpected(reduced, expected);
  }
  */

  /*
  public void testHide_linwon90_projsystem1()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    final String subdirname = "linwon90";
    final String[] events = {"tau"};
    final ProductDESProxy des = compileValid(subdirname, "projsystem1");
    final AutomatonProxy hidden = hide(des, "projsystem1", events, true);
    //save(des, hidden);
    //final ProductDESProxy expected = getExpectedValid(subdirname, hidden);
    //testExpected(hidden, expected);
    final AutomatonProxy reduced = iterativeReduce(hidden);
    //save(des, hidden);
    //testExpected(reduced, expected);
  }
  */

  /*
  public void testHide_linwon90_projsystem()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    final String subdirname = "linwon90";
    final ProductDESProxy des = compileValid(subdirname, "projsystem");
    final String[] names = {
      "load[1]", "unload[1]", "load[2]", "unload[2]",
      "load[3]", "unload[3]", "load[4]", "unload[4]",
      "start[1]", "start[2]"
    };
    AutomatonProxy result = des.findAutomaton("projsystem");
    for (int i = 0; i < names.length; i++) {
      System.err.println("Hiding " + names[i] + "...");
      final String name = names[i];
      final EventProxy event = des.findEvent(name);
      final Collection singleton = Collections.singletonList(event);
      final AutomatonProxy hidden = Hiding.hide(result, singleton);
      result = Bisimulation.reduce(hidden, ":reduced");
      System.gc();
    }
    des.addAutomaton(result);
    save(des);
  }
  */


  //#########################################################################
  //# Utilities
  private ProductDESProxy compileHandwritten(final String modname)
    throws IOException, JAXBException, WatersException
  {
    final String inextname = modname + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(mWatersInputDirectory, inextname);
    final ModuleProxy module =
      (ModuleProxy) mModuleMarshaller.unmarshal(infilename);
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
    final ModuleProxy module = mValidUnmarshaller.unmarshal(infilename);
    return compile(module);
  }

  private ProductDESProxy compile(final ModuleProxy module)
    throws WatersException
  {
    final ModuleCompiler compiler =
      new ModuleCompiler(module, mDocumentManager);
    return compiler.compile();
  }

  private AutomatonProxy hide(final ProductDESProxy des,
			      final String autname,
			      final String[] hiddennames,
			      final boolean conflicteq)
    throws WatersException
  {
    final Collection hidden = new LinkedList();
    final StringBuffer hiddenbuffer = new StringBuffer(autname);
    for (int i = 0; i < hiddennames.length; i++) {
      final String name = hiddennames[i];
      final EventProxy event = des.findEvent(name);
      hidden.add(event);
      hiddenbuffer.append(':');
      hiddenbuffer.append(name);
    }
    final String hiddenname = hiddenbuffer.toString();
    final AutomatonProxy aut = des.findAutomaton(autname);
    final AutomatonProxy result =
      Hiding.hide(aut, hidden, hiddenname, conflicteq);
    des.addAutomaton(result);
    return result;
  }

  private AutomatonProxy reduce(final AutomatonProxy aut)
    throws WatersException
  {
    final String name = aut.getName();
    return Bisimulation.reduce(aut, name);
  }

  private AutomatonProxy reduce(final ProductDESProxy des,
				final AutomatonProxy aut,
				final String name)
    throws WatersException
  {
    final AutomatonProxy result = Bisimulation.reduce(aut, name);
    des.addAutomaton(result);
    return result;
  }

  private AutomatonProxy iterativeReduce(final AutomatonProxy aut)
    throws WatersException
  {
    final String name = aut.getName();
    int numstates = aut.getStates().size();
    AutomatonProxy reduced = Bisimulation.reduce(aut, name);
    reduced = ConflictEquiv.reduce(reduced, name);
    int newnumstates = reduced.getStates().size();
    while (newnumstates < numstates) {
      numstates = newnumstates;
      reduced = Bisimulation.reduce(reduced, name);
      newnumstates = reduced.getStates().size();
      if (newnumstates == numstates) {
	break;
      }
      numstates = newnumstates;
      reduced = ConflictEquiv.reduce(reduced, name);
      newnumstates = reduced.getStates().size();
    }
    return reduced;
  }


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
    final String outextname = filename + mDESMarshaller.getDefaultExtension();
    final File outfilename = new File(mOutputDirectory, outextname);
    mDESMarshaller.marshal(des, outfilename);
  }			

  private Collection getEvents(final ProductDESProxy des,
			       final String[] names)
    throws WatersException
  {
    final Collection events = new LinkedList();
    for (int i = 0; i < names.length; i++) {
      final String name = names[i];
      final EventProxy event = des.findEvent(name);
      events.add(event);
    }
    return events;
  }

  private ProductDESProxy getExpectedHandwritten(final AutomatonProxy aut)
    throws IOException, JAXBException, WatersException
  {
    final String name = aut.getName();
    final String filename = eliminateStrangeCharacters(name);
    final String extname = filename + mDESMarshaller.getDefaultExtension();
    final File testfilename = new File(mWatersInputDirectory, extname);
    return (ProductDESProxy) mDESMarshaller.unmarshal(testfilename);
  }

  private ProductDESProxy getExpectedValid(final String subdirname,
					   final AutomatonProxy aut)
    throws IOException, JAXBException, WatersException
  {
    final String name = aut.getName();
    final String filename = eliminateStrangeCharacters(name);
    final String extname = filename + mDESMarshaller.getDefaultExtension();
    final File indirname = new File(mValidInputDirectory, subdirname);
    final File testfilename = new File(indirname, extname);
    return (ProductDESProxy) mDESMarshaller.unmarshal(testfilename);
  }

  private void testExpected(final AutomatonProxy aut,
			    final ProductDESProxy expected)
    throws WatersException
  {
    final String name = aut.getName();
    final AutomatonProxy expectedaut = expected.findAutomaton(name);
    assertTrue("Unexpected result!", aut.equals(expectedaut));
    assertTrue("Unexpected result!", expectedaut.equals(aut));
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
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  {
    mValidUnmarshaller = new ValidUnmarshaller();
    mModuleMarshaller = new ModuleMarshaller();
    mDESMarshaller = new ProductDESMarshaller();
    mDocumentManager = new DocumentManager();
    mDocumentManager.register(mModuleMarshaller);
    mDocumentManager.register(mDESMarshaller);
    mWatersInputDirectory = new File("examples", "handwritten");
    mValidInputDirectory = new File("examples", "valid");
    mOutputDirectory = new File("logs", "algorithms");
  }

  protected void tearDown()
  {
    mValidUnmarshaller = null;
    mModuleMarshaller = null;
    mDESMarshaller = null;
    mDocumentManager = null;
    mWatersInputDirectory = null;
    mValidInputDirectory = null;
    mOutputDirectory = null;
  }


  //#########################################################################
  //# Data Members
  private ValidUnmarshaller mValidUnmarshaller;
  private ModuleMarshaller mModuleMarshaller;
  private ProductDESMarshaller mDESMarshaller;
  private DocumentManager mDocumentManager;
  private File mWatersInputDirectory;
  private File mValidInputDirectory;
  private File mOutputDirectory;

}
