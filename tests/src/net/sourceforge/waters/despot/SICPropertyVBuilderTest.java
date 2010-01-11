//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   DESpotImporterTest
//###########################################################################
//# $Id: DESpotImporterTest.java 5044 2009-12-30 20:14:13Z robi $
//###########################################################################

package net.sourceforge.waters.despot;

import java.io.File;
import java.net.URI;
import java.util.List;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleIdentifierChecker;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;


public class SICPropertyVBuilderTest extends AbstractWatersTest
{

  // #########################################################################
  // # tests integrity of the model built
  /*
   * public void testIntegrity() throws Exception {
   *
   * final List<EventProxy> answerEvents = (List<EventProxy>)
   * mBuilder.getAnswerEvents(); for (final EventProxy answer : answerEvents) {
   * final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);
   * System.out.print(modifiedDES);
   * ProductDESIntegrityChecker.getInstance().check(modifiedDES); }
   *
   * }
   */

  // #########################################################################
  // # tests the model built compared to a manually created model
  public void testBuild_parManEg_I_mfb_lowlevel() throws Exception
  {
    testBuild("SICPropertyV", "parManEg_I_mfb_lowlevel");

  }

  // #########################################################################
  // # Exception Throwing Test Cases
  /*
   * public void testException_nonexist() throws Exception { /*
   * testException("testSimple", "nonexist", FileNotFoundException.class,
   * "nonexist.des");
   *
   * }
   */

  // #########################################################################
  // # Utilities
  void testException(final String subdir, final String name,
                     final Class<? extends Exception> exclass,
                     final String culprit) throws Exception
  {
    try {
      // testBuild(subdir, name);
      fail("Expected " + ProxyTools.getShortClassName(exclass) + " not caught!");
    } catch (final Exception exception) {
      if (exclass.isAssignableFrom(exception.getClass())) {
        final String msg = exception.getMessage();
        if (msg == null) {
          fail(ProxyTools.getShortClassName(exclass)
              + " caught as expected, but message is null!");
        } else if (msg.indexOf(culprit) < 0) {
          fail(ProxyTools.getShortClassName(exclass)
              + " caught as expected, but message '" + msg
              + "' does not mention culprit '" + culprit + "'!");
        }
      } else {
        throw exception;
      }
    }
  }

  void testBuild(final String subdir, final String name) throws Exception
  {
    final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
    final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final ValidUnmarshaller importer =
        new ValidUnmarshaller(moduleFactory, optable);
    final JAXBModuleMarshaller moduleMarshaller =
        new JAXBModuleMarshaller(moduleFactory, optable, false);
    final JAXBProductDESMarshaller desMarshaller =
        new JAXBProductDESMarshaller(desFactory);
    final DocumentManager docManager = new DocumentManager();
    docManager.registerUnmarshaller(desMarshaller);
    docManager.registerUnmarshaller(moduleMarshaller);
    docManager.registerUnmarshaller(importer);

    final File indirname = new File(mInputDirectory, subdir);
    final String wmodext = ".wmod";
    final File infilename = new File(indirname, name + wmodext);
    final URI unmodifiedDESURI = infilename.toURI();

    createEmptyDirectory(mOutputDirectory);

    final DocumentProxy doc = docManager.load(unmodifiedDESURI);
    final ProductDESProxy des;
    if (doc instanceof ProductDESProxy) {
      des = (ProductDESProxy) doc;
    } else {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
          new ModuleCompiler(docManager, desFactory, module);
      des = compiler.compile();
    }
    mBuilder = new SICPropertyVBuilder(des, desFactory);

    final List<EventProxy> answerEvents =
        (List<EventProxy>) mBuilder.getAnswerEvents();
    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);

      /*
       * final File wmodfilename = new File(outdirname, answer.getName() + "_" +
       * name); assertEquals("Unexpected location of output file!",
       * wmodfilename, modifiedDES.getFileLocation());
       */

      final File outfilename =
          new File(mOutputDirectory, name + "_" + answer.getName() + wmodext);
      mProductDESMarshaller.marshal(modifiedDES, outfilename);
      final File expectfilename =
          new File(indirname, name + "_EXPECTED_" + answer.getName() + wmodext);
      parseGeneratedModules(name, answer.getName(), expectfilename, outfilename);
    }

    /*
     * if (result) { System.out.println("nonconflicting"); } else {
     * System.out.println("CONFLICTING"); System.out.println("Counterexample:");
     * final ConflictTraceProxy counterex = builder.getCounterExample();
     * System.out.println(counterex.toString()); }
     */
  }

  /**
   * Checks whether the built module matches the expected output.
   */
  private void parseGeneratedModules(final String testname,
                                     final String answername,
                                     final File expectfile,
                                     final File outfilename) throws Exception
  {
    /*
     * final URL url = unmodifiedDESURI.toURL(); /* final InputStream stream =
     * url.openStream();
     *
     * final Document doc; try { final DocumentBuilder builder =
     * DocumentBuilderFactory.newInstance().newDocumentBuilder(); doc =
     * builder.parse(stream); } finally { stream.close(); } /* final Element
     * root = doc.getDocumentElement(); final String ext =
     * mModuleMarshaller.getDefaultExtension();
     *
     * for (Node node = root.getFirstChild(); node != null; node =
     * node.getNextSibling()) { if (node instanceof Element) { final Element
     * element = (Element) node; if (element.getTagName().equals("Subsystem")) {
     * final String sysname = element.getAttribute("name"); final String extname
     * = sysname + ext;
     */
    final URI outuri = outfilename.toURI();
    final ModuleProxy outmodule = mModuleMarshaller.unmarshal(outuri);
    mIdentifierChecker.check(outmodule);
    if (expectfile.exists()) {
      final URI expecturi = expectfile.toURI();
      final ModuleProxy expectmodule = mModuleMarshaller.unmarshal(expecturi);
      assertModuleProxyEquals("Unexpected module contents for module '"
          + testname + "' with answer '" + answername + "' after parse back!",
                              outmodule, expectmodule);
    }
    /*
     * } } }
     *
     * } catch (final ParserConfigurationException exception) { throw new
     * WatersUnmarshalException(exception); } catch (final SAXException
     * exception) { throw new WatersUnmarshalException(exception); }
     */
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

  // #########################################################################
  // # Overrides for junit.framework.TestCase
  protected void setUp() throws Exception
  {
    super.setUp();
    mInputDirectory = new File(getWatersInputRoot(), "tests");
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mBuilder = new SICPropertyVBuilder(mProductDESFactory);
    mIdentifierChecker =
        ModuleIdentifierChecker.getModuleIdentifierCheckerInstance();
  }

  protected void tearDown() throws Exception
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mBuilder = null;
    mIdentifierChecker = null;
    super.tearDown();
  }

  // #########################################################################
  // # Data Members

  // location of the tests
  private File mInputDirectory;

  private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private SICPropertyVBuilder mBuilder;
  private ModuleIdentifierChecker mIdentifierChecker;

}
