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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
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
  //# Test Cases
  public void testImport_test2()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "test2");
  }

  public void testImport_testCont1()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "testCont1");
  }

  public void testImport_testInt()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "testInt");
  }

  public void testImport_testNB()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "testNB");
  }

  public void testImport_testNB2()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "testNB2");
  }

  public void testImport_testSync()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "testSync");
  }

  public void testImport_wicked_events()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "wicked_events");
  }

  public void testImport_wicked_states()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testSimple", "wicked_states");
  }

  public void testImport_testHISC()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC");
  }

  public void testImport_testHISC1()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC1");
  }

  public void testImport_testHISC2()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC2");
  }

  public void testImport_testHISC3A()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC3A");
  }

  public void testImport_testHISC4()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC4");
  }

  public void testImport_testHISC5()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC5");
  }

  public void testImport_testHISC6()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC6");
  }

  public void testImport_testHISC7()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC7");
  }

  public void testImport_testHISC8()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC8");
  }

  public void testImport_testHISC9()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC9");
  }

  public void testImport_testHISC10()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
            EvalException
  {
    testImport("testHISC", "testHISC10");
  }

  public void testImport_testHISC11()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC11");
  }

  public void testImport_testHISC12()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC12");
  }

  public void testImport_testHISC13()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC13");
  }

  public void testImport_testHISC14()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISC14");
  }

  public void testImport_testHISCld()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("testHISC", "testHISCld");
  }

  public void testImport_ManufacturingExampleSimple()
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    testImport("simpleManufacturingExample", "Manufacturing-Example-Simple");
  }


  //#########################################################################
  //# Utilities
  void testImport(final String subdir, final String name)
    throws IOException, WatersMarshalException, WatersUnmarshalException,
           EvalException
  {
    final String inextname = name + mImporter.getDefaultExtension();
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
    parseGeneratedModules(despotURI, indirname, outdirname);
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

  private void parseGeneratedModules(final URI despotURI,
                                     final File indirname,
                                     final File outdirname)
    throws IOException, WatersUnmarshalException
  {
    try {
      final String ext = mModuleMarshaller.getDefaultExtension();
      final URL url = despotURI.toURL();
      final InputStream stream = url.openStream();
      DocumentBuilder builder;
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final Document doc = builder.parse(stream);
      stream.close();
      final Element root = doc.getDocumentElement();
      for (Node node = root.getFirstChild();
           node != null;
           node = node.getNextSibling()) {
        if (node instanceof Element) {
          final Element element = (Element) node;
          if (element.getTagName().equals("Subsystem")) {
            final String name = element.getAttribute("name");
            final String extname = name + ext;
            final File outfile = new File(outdirname, extname);
            final URI outuri = outfile.toURI();
            final ModuleProxy outmodule = mModuleMarshaller.unmarshal(outuri);
            mIdentifierChecker.check(outmodule);
            final File expectfile = new File(indirname, extname);
            if (expectfile.exists()) {
              final URI expecturi = expectfile.toURI();
              final ModuleProxy expectmodule =
                mModuleMarshaller.unmarshal(expecturi);
              assertTrue("Unexpected module contents after parse back!",
                         outmodule.equalsByContents(expectmodule));
              /*
              assertTrue("Unexpected module geometry after parse back!",
                         outmodule.equalsWithGeometry(expectmodule));
              */
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
    throws JAXBException, SAXException
  {
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
    mIdentifierChecker = ModuleIdentifierChecker.getInstance();
  }

  protected void tearDown()
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mImporter = null;
    mIdentifierChecker = null;
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
