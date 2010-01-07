//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   DESpotImporterTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.despot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleIdentifierChecker;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class DESpotImporterTest extends AbstractWatersTest
{

  //#########################################################################
  //# Successful Test Cases
  public void testImport_test2()
    throws Exception
  {
    testImport("testSimple", "test2");
  }

  public void testImport_testCont1()
    throws Exception
  {
    testImport("testSimple", "testCont1");
  }

  public void testImport_testInt()
    throws Exception
  {
    testImport("testSimple", "testInt");
  }

  public void testImport_testNB()
    throws Exception
  {
    testImport("testSimple", "testNB");
  }

  public void testImport_testNB2()
    throws Exception
  {
    testImport("testSimple", "testNB2");
  }

  public void testImport_testSync()
    throws Exception
  {
    testImport("testSimple", "testSync");
  }

  public void testImport_never_marked_1()
    throws Exception
  {
    testImport("testSimple", "never_marked_1");
  }

  public void testImport_never_marked_2()
    throws Exception
  {
    testImport("testSimple", "never_marked_2");
  }

  public void testImport_wicked_automata()
    throws Exception
  {
    testImport("testSimple", "wicked_automata");
  }

  public void testImport_wicked_events()
    throws Exception
  {
    testImport("testSimple", "wicked_events");
  }

  public void testImport_wicked_instances()
    throws Exception
  {
    testImport("testSimple", "wicked_instances");
  }

  public void testImport_wicked_states()
    throws Exception
  {
    testImport("testSimple", "wicked_states");
  }

  public void testImport_testHISC()
    throws Exception
  {
    testImport("testHISC", "testHISC");
  }

  public void testImport_testHISC1()
    throws Exception
  {
    testImport("testHISC", "testHISC1");
  }

  public void testImport_testHISC2()
    throws Exception
  {
    testImport("testHISC", "testHISC2");
  }

  public void testImport_testHISC3A()
    throws Exception
  {
    testImport("testHISC", "testHISC3A");
  }

  public void testImport_testHISC4()
    throws Exception
  {
    testImport("testHISC", "testHISC4");
  }

  public void testImport_testHISC5()
    throws Exception
  {
    testImport("testHISC", "testHISC5");
  }

  public void testImport_testHISC6()
    throws Exception
  {
    testImport("testHISC", "testHISC6");
  }

  public void testImport_testHISC7()
    throws Exception
  {
    testImport("testHISC", "testHISC7");
  }

  public void testImport_testHISC8()
    throws Exception
  {
    testImport("testHISC", "testHISC8");
  }

  public void testImport_testHISC9()
    throws Exception
  {
    testImport("testHISC", "testHISC9");
  }

  public void testImport_testHISC10()
    throws Exception
  {
    testImport("testHISC", "testHISC10");
  }

  public void testImport_testHISC11()
    throws Exception
  {
    testImport("testHISC", "testHISC11");
  }

  public void testImport_testHISC12()
    throws Exception
  {
    testImport("testHISC", "testHISC12");
  }

  public void testImport_testHISC13()
    throws Exception
  {
    testImport("testHISC", "testHISC13");
  }

  public void testImport_testHISC14()
    throws Exception
  {
    testImport("testHISC", "testHISC14");
  }

  public void testImport_testHISCld()
    throws Exception
  {
    testImport("testHISC", "testHISCld");
  }

  public void testImport_ManufacturingExampleSimple()
    throws Exception
  {
    testImport("simpleManufacturingExample", "Manufacturing-Example-Simple");
  }


  //#########################################################################
  //# Exception Throwing Test Cases
  public void testException_nonexist_des()
    throws Exception
  {
    testException("testSimple", "nonexist_des",
                  FileNotFoundException.class, "nonexist.des");
  }

  public void testException_nonexist_interface()
    throws Exception
  {
    testException("testSimple", "nonexist_interface",
                  WatersUnmarshalException.class, "nonexist_iface");
  }

  public void testException_nonexist_subsystem()
    throws Exception
  {
    testException("testSimple", "nonexist_subsystem",
                  WatersUnmarshalException.class, "nonexist_low");
  }


  //#########################################################################
  //# Utilities
  void testException(final String subdir,
                     final String name,
                     final Class<? extends Exception> exclass,
                     final String culprit)
    throws Exception
  {
    try {
      testImport(subdir, name);
      fail("Expected " + ProxyTools.getShortClassName(exclass) + " not caught!");
    } catch (final Exception exception) {
      if (exclass.isAssignableFrom(exception.getClass())) {
        final String msg = exception.getMessage();
        if (msg == null) {
          fail(ProxyTools.getShortClassName(exclass) +
               " caught as expected, but message is null!");
        } else if (msg.indexOf(culprit) < 0) {
          fail(ProxyTools.getShortClassName(exclass) +
               " caught as expected, but message '" + msg +
               "' does not mention culprit '" + culprit + "'!");
        }
      } else {
        throw exception;
      }
    }
  }

  void testImport(final String subdir, final String name)
    throws Exception
  {
    final String inextname = name + mImporter.getDefaultExtension();
    getLogger().info("Converting " + inextname + " ...");
    final File indirname = new File(mInputDirectory, subdir);
    final File infilename = new File(indirname, inextname);
    final URI despotURI = infilename.toURI();
    final File outdirname = new File(mOutputDirectory, name);
    createEmptyDirectory(outdirname);
    mImporter.setOutputDirectory(outdirname);
    final ModuleProxy module = mImporter.unmarshalCopying(despotURI);
    final String wmodextname =
      module.getName() + mModuleMarshaller.getDefaultExtension();
    final File wmodfilename = new File(outdirname, wmodextname);
    assertEquals("Unexpected location of output file!",
                 wmodfilename, module.getFileLocation());
    parseGeneratedModules(name, despotURI, indirname, outdirname);
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    final ProductDESProxy des = compiler.compile();
    final String desextname =
      name + mProductDESMarshaller.getDefaultExtension();
    final File outfilename = new File(outdirname, desextname);
    mProductDESMarshaller.marshal(des, outfilename);
  }

  private void createEmptyDirectory(final File outdirname)
  {
    if (outdirname.exists()) {
      final File[] children = outdirname.listFiles();
      if (children == null) {
        outdirname.delete();
      } else {
        for (final File child : children) {
          child.delete();
        }
      }
    }
    ensureParentDirectoryExists(outdirname);
  }

  private void parseGeneratedModules(final String testname,
                                     final URI despotURI,
                                     final File indirname,
                                     final File outdirname)
    throws Exception
  {
    try {
      final URL url = despotURI.toURL();
      final InputStream stream = url.openStream();
      final Document doc;
      try {
        final DocumentBuilder builder =
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = builder.parse(stream);
      } finally {
        stream.close();
      }
      final Element root = doc.getDocumentElement();
      final String ext = mModuleMarshaller.getDefaultExtension();
      for (Node node = root.getFirstChild();
           node != null;
           node = node.getNextSibling()) {
        if (node instanceof Element) {
          final Element element = (Element) node;
          if (element.getTagName().equals("Subsystem")) {
            final String sysname = element.getAttribute("name");
            final String extname = sysname + ext;
            final File outfile = new File(outdirname, extname);
            final URI outuri = outfile.toURI();
            final ModuleProxy outmodule = mModuleMarshaller.unmarshal(outuri);
            mIdentifierChecker.check(outmodule);
            final File expectfile = new File(indirname, extname);
            if (expectfile.exists()) {
              final URI expecturi = expectfile.toURI();
              final ModuleProxy expectmodule =
                mModuleMarshaller.unmarshal(expecturi);
              assertModuleProxyEquals
                ("Unexpected module contents for subsystem '" +
                 sysname + "' after parse back!",
                 outmodule, expectmodule);
            }
          }
        }
      }
    } catch (final ParserConfigurationException exception) {
      throw new WatersUnmarshalException(exception);
    } catch (final SAXException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mInputDirectory = new File(getWatersInputRoot(), "despot");
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mImporter = new DESpotImporter(moduleFactory, mDocumentManager);
    mIdentifierChecker =
      ModuleIdentifierChecker.getModuleIdentifierCheckerInstance();
  }

  protected void tearDown()
    throws Exception
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mImporter = null;
    mIdentifierChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private DESpotImporter mImporter;
  private ModuleIdentifierChecker mIdentifierChecker;

}
