//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public abstract class AbstractAnalysisTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractAnalysisTest()
  {
  }

  public AbstractAnalysisTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mProductDESMarshaller =
      new SAXProductDESMarshaller(mProductDESProxyFactory);
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller =
      new SAXModuleMarshaller(mModuleProxyFactory, optable, false);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mProductDESImporter = new ProductDESImporter(mModuleProxyFactory);
  }


  //#########################################################################
  //# Compiling
  protected ModuleProxy loadModule(final String... path)
    throws IOException, WatersException
  {
    File dir = getWatersInputRoot();
    final int numDirs = path.length - 1;
    for (int i = 0; i < numDirs; i++) {
      final String name = path[i];
      dir = new File(dir, name);
    }
    String extname = path[numDirs];
    if (extname.indexOf('.') < 0) {
      extname += mModuleMarshaller.getDefaultExtension();
    }
    final File filename = new File(dir, extname);
    final URI uri = filename.toURI();
    return mModuleMarshaller.unmarshal(uri);
  }


  protected ProductDESProxy getCompiledDES(final String... path)
    throws Exception
  {
    return getCompiledDESRaw(null, path);
  }

  protected ProductDESProxy getCompiledDES
    (final List<ParameterBindingProxy> bindings,
     final String... names)
    throws Exception
  {
    return getCompiledDESRaw(bindings, names);
  }

  protected ProductDESProxy getCompiledDESRaw
    (final List<ParameterBindingProxy> bindings, final String[] names)
    throws Exception
  {
    File dir = getWatersInputRoot();
    final int numDirs = names.length - 1;
    for (int i = 0; i < numDirs; i++) {
      final String name = names[i];
      dir = new File(dir, name);
    }
    final String name = names[numDirs];
    return getCompiledDES(dir, name, bindings);
  }

  protected ProductDESProxy getCompiledDES
    (final File dir,
     final String name,
     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final File filename = new File(dir, name);
    return getCompiledDES(filename, bindings);
  }

  protected ProductDESProxy getCompiledDES(final File filename)
    throws Exception
  {
    return getCompiledDES(filename, null);
  }

  protected ProductDESProxy getCompiledDES
    (final File pathName,
     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final DocumentProxy doc = mDocumentManager.load(pathName);
    final String docName = doc.getName();
    final String fileName = pathName.getName();
    final int dotPos = fileName.lastIndexOf('.');
    final String expectedName =
      (dotPos >= 0 ? fileName.substring(0, dotPos) : fileName);
    assertEquals("Name of " + ProxyTools.getShortClassName(doc) +
                 " does not match file name " + pathName + "!",
                 expectedName, docName);
    if (doc instanceof ProductDESProxy) {
      assertTrue("Can't apply bindings to ProductDES!",
                 bindings == null || bindings.isEmpty());
      final ProductDESProxy des = (ProductDESProxy) doc;
      mProductDESIsDeterministic = AutomatonTools.isDeterministic(des);
      return des;
    } else if (doc instanceof ModuleProxy) {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
      configure(compiler);
      final ProductDESProxy des = compiler.compile(bindings);
      mProductDESIsDeterministic = AutomatonTools.isDeterministic(des);
      return des;
    } else {
      fail("Unknown document type " + doc.getClass().getName() + "!");
      return null;
    }
  }

  protected void configure(final ModuleCompiler compiler)
  {
    compiler.setNormalizationEnabled(true);
    compiler.setAutomatonVariablesEnabled(true);
  }

  protected DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }


  //#########################################################################
  //# Utilities
  protected ModuleProxyFactory getModuleProxyFactory()
  {
    return mModuleProxyFactory;
  }

  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return mProductDESProxyFactory;
  }

  protected ParameterBindingProxy createBinding(final String name,
                                                final int value)
  {
    final IntConstantProxy expr =
      mModuleProxyFactory.createIntConstantProxy(value);
    return mModuleProxyFactory.createParameterBindingProxy(name, expr);
  }

  protected EventProxy findEvent(final ProductDESProxy des,
                                 final String name)
    throws NameNotFoundException
  {
    final EventProxy event = getEvent(des, name);
    if (event != null) {
      return event;
    } else {
      throw new NameNotFoundException
        ("DES '" + des.getName() + "' does not have any event named '" +
         name + "'!");
    }
  }

  protected EventProxy getEvent(final ProductDESProxy des, final String name)
  {
    for (final EventProxy event : des.getEvents()) {
      if (event.getName().equals(name)) {
        return event;
      }
    }
    return null;
  }

  protected EventProxy getEvent(final AutomatonProxy aut, final String name)
  {
    for (final EventProxy event : aut.getEvents()) {
      if (event.getName().equals(name)) {
        return event;
      }
    }
    return null;
  }

  protected List<EventProxy> getUnobservableEvents(final ProductDESProxy des)
  {
    final List<EventProxy> result = new LinkedList<EventProxy>();
    for (final EventProxy event : des.getEvents()) {
      if (!event.isObservable()) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Returns a combination of states bits read from event attributes in order
   * to create an event encoding. This method checks the event's
   * controllability based on the event kind and the names of known status
   * flags in the event's attribute map, and if present, sets the
   * corresponding status bits.
   * @return Status byte with relevant status bits set.
   */
  protected byte getEventStatusFromAttributes(final EventProxy event)
  {
    byte status;
    if (event.getKind() == EventKind.CONTROLLABLE) {
      status = EventStatus.STATUS_CONTROLLABLE;
    } else {
      status = EventStatus.STATUS_NONE;
    }
    final Map<String,String> attribs = event.getAttributes();
    for (final byte flag : STATUS_FROM_ATTRIBUTES) {
      final String name = EventStatus.getStatusName(flag);
      if (attribs.containsKey(name)) {
        status |= flag;
      }
    }
    return status;
  }

  protected AutomatonProxy findAutomaton(final ProductDESProxy des,
                                         final String name)
    throws NameNotFoundException
  {
    final AutomatonProxy aut = getAutomaton(des, name);
    if (aut == null) {
      throw new NameNotFoundException
        ("DES '" + des.getName() + "' does not have any automaton named '" +
         name + "'!");
    }
    return aut;
  }

  protected AutomatonProxy getAutomaton(final ProductDESProxy des,
                                        final String name)
  {
    for (final AutomatonProxy automaton : des.getAutomata()) {
      if (automaton.getName().equals(name)) {
        return automaton;
      }
    }
    return null;
  }

  protected StateProxy findState(final AutomatonProxy aut, final String name)
    throws NameNotFoundException
  {
    final StateProxy state = getState(aut, name);
    if (state == null) {
      throw new NameNotFoundException
        ("Automaton '" + aut.getName() +
         "' does not have any state named '" + name + "'!");
    }
    return state;
  }

  protected StateProxy getState(final AutomatonProxy aut, final String name)
  {
    for (final StateProxy state : aut.getStates()) {
      if (state.getName().equals(name)) {
        return state;
      }
    }
    return null;
  }

  protected String appendSuffixes(final String name,
                                  final List<ParameterBindingProxy> bindings)
  {
    if (bindings == null) {
      return name;
    } else {
      final StringBuilder buffer = new StringBuilder(name);
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
      return buffer.toString();
    }
  }

  protected void saveAutomaton(final AutomatonProxy aut,
                               final String basename, final String comment)
    throws WatersMarshalException, IOException, ParseException
  {
    assertNotNull(aut);
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<AutomatonProxy> automata =
      Collections.singletonList(aut);
    final ProductDESProxy des = mProductDESProxyFactory.createProductDESProxy
      (basename, comment, null, events, automata);
    saveDES(des, basename);
  }

  protected void saveDES(final ProductDESProxy des, final String basename)
    throws WatersMarshalException, IOException, ParseException
  {
    assertNotNull(des);
    final String desext = mProductDESMarshaller.getDefaultExtension();
    final String desname = basename + desext;
    assertTrue("File name '" + desname + "' contains colon, " +
               "which does not work on all platforms!",
               desname.indexOf(':') < 0);
    final File dir = getOutputDirectory();
    final File desfilename = new File(dir, desname);
    mDocumentManager.saveAs(des, desfilename);
    final ModuleProxy module = mProductDESImporter.importModule(des);
    final String modext = mModuleMarshaller.getDefaultExtension();
    final String modname = basename + modext;
    final File modfilename = new File(dir, modname);
    mDocumentManager.saveAs(module, modfilename);
  }

  protected void saveModule(final ModuleProxy module, final String basename)
    throws WatersMarshalException, IOException, ParseException
  {
    assertNotNull(module);
    final String ext = mModuleMarshaller.getDefaultExtension();
    final String filename = basename + ext;
    assertTrue("File name '" + filename + "' contains colon, " +
               "which does not work on all platforms!",
               filename.indexOf(':') < 0);
    final File dir = getOutputDirectory();
    final File fullname = new File(dir, filename);
    mDocumentManager.saveAs(module, fullname);
  }

  protected boolean isProductDESDeterministic()
  {
    return mProductDESIsDeterministic;
  }


  void setNodeLimit(final ModelAnalyzer analyzer)
  {
    if (analyzer.getNodeLimit() == Integer.MAX_VALUE) {
      final String prop = System.getProperty("waters.analysis.statelimit");
      if (prop != null) {
        final int limit = Integer.parseInt(prop);
        analyzer.setNodeLimit(limit);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mProductDESProxyFactory =
    ProductDESElementFactory.getInstance();
  private final ModuleProxyFactory mModuleProxyFactory =
    ModuleElementFactory.getInstance();

  private SAXProductDESMarshaller mProductDESMarshaller;
  private SAXModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private ProductDESImporter mProductDESImporter;

  private boolean mProductDESIsDeterministic;


  //#########################################################################
  //# Class Constants
  private static final byte[] STATUS_FROM_ATTRIBUTES = {
    EventStatus.STATUS_LOCAL,
    EventStatus.STATUS_SELFLOOP_ONLY,
    EventStatus.STATUS_ALWAYS_ENABLED,
    EventStatus.STATUS_BLOCKED,
    EventStatus.STATUS_FAILING,
    EventStatus.STATUS_UNUSED
  };

}
