//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.external.ttct
//# CLASS:   TTCTImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.ttct;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A simple command-line tool to import a collection of TTCT automata as
 * a Waters module. This version is incomplete, it does not support
 * initial and marked states in TTCT files.
 *
 * @author Robi Malik
 */

public class TTCTImporter
{

  //#########################################################################
  //# Main method
  public static void main(final String args[])
  {
    try {
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final TTCTImporter importer = new TTCTImporter(desFactory);
      final ProductDESProxy des = importer.createProductDES("ttct", args);
      final ModuleProxyFactory modFactory = ModuleElementFactory.getInstance();
      final CompilerOperatorTable opTable =
        CompilerOperatorTable.getInstance();
      final ProductDESImporter converter = new ProductDESImporter(modFactory);
      final ModuleProxy module = converter.importModule(des);
      final ProxyMarshaller<ModuleProxy> marshaller =
        new JAXBModuleMarshaller(modFactory, opTable);
      // TODO Set output file from command line
      final File outfile = new File("ttct.wmod");
      marshaller.marshal(module, outfile);
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR!");
      System.err.println(ProxyTools.getShortClassName(exception) +
                         " caught in main()!");
      exception.printStackTrace(System.err);
      System.exit(1);
    }
  }


  //#########################################################################
  //# Constructor
  public TTCTImporter(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Automaton Import
  public ProductDESProxy createProductDES(final String desName,
                                          final String[] filenames)
    throws IOException, WatersUnmarshalException
  {
    mMarking = mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                         EventKind.PROPOSITION);
    mMarkings = Collections.singletonList(mMarking);
    mGlobalAlphabet = new TIntObjectHashMap<EventProxy>();
    final Collection<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(filenames.length);
    for (final String filename : filenames) {
      final File file = new File(filename);
      final String tail = file.getName();
      final int dotpos = tail.lastIndexOf('.');
      final String name = tail.substring(0, dotpos);
      final InputStream stream = new FileInputStream(file);
      final DataInputStream data = new DataInputStream(stream);
      final AutomatonProxy aut = createAutomaton(name, data);
      automata.add(aut);
      data.close();
    }
    final int[] codes = mGlobalAlphabet.keys();
    final Collection<EventProxy> events = createEventList(codes);
    return mFactory.createProductDESProxy(desName, events, automata);
  }

  public AutomatonProxy createAutomaton(final String name,
                                        final DataInputStream stream)
    throws IOException, WatersUnmarshalException
  {
    stream.skipBytes(34);
    // TODO Check signature.
    // TODO Add support for other settings.
    final int endian = readInt(stream);
    checkParameter(endian, 0xff00aa55, "endian");
    final int blockType = readInt(stream);
    checkParameter(blockType, 0, "block type");
    stream.skipBytes(4); // skip block size
    final int numStates = readInt(stream);
    final StateProxy[] states = new StateProxy[numStates];
    final int init = readInt(stream);
    do {
      final int marked = readInt(stream);
      if (marked == 0xffffffff) {
        break;
      }
      states[marked] = createState(marked, marked == init, true);
    } while (true);
    for (int s = 0; s < numStates; s++) {
      if (states[s] == null) {
        states[s] = createState(s, s == init, false);
      }
    }
    final TIntHashSet localAlphabet = new TIntHashSet();
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>();
    do {
      final int source = readInt(stream);
      if (source == 0xffffffff) {
        break;
      }
      final int numTrans = readShort(stream);
      for (int t = 0; t < numTrans; t++) {
        final int transCode = readInt(stream);
        final int eventCode = (transCode >>> 22);
        localAlphabet.add(eventCode);
        final EventProxy event = getEvent(eventCode);
        final int target = transCode & 0x003fffff;
        final TransitionProxy trans =
          mFactory.createTransitionProxy(states[source], event, states[target]);
        transitions.add(trans);
      }
    } while (true);
    final int[] eventCodes = localAlphabet.toArray();
    final Collection<EventProxy> eventList = createEventList(eventCodes);
    final Collection<StateProxy> stateList = Arrays.asList(states);
    return mFactory.createAutomatonProxy(name, ComponentKind.SPEC,
                                         eventList, stateList, transitions);
  }


  //#########################################################################
  //# Auxiliary Methods
  private StateProxy createState(final int code,
                                 final boolean init,
                                 final boolean marked)
  {
    final String name = "s" + code;
    final Collection<EventProxy> props = marked ? mMarkings : null;
    return mFactory.createStateProxy(name, init, props);
  }

  private Collection<EventProxy> createEventList(final int[] codes)
  {
    Arrays.sort(codes);
    final Collection<EventProxy> events =
      new ArrayList<EventProxy>(codes.length + 1);
    for (final int code : codes) {
      final EventProxy event = getEvent(code);
      events.add(event);
    }
    events.add(mMarking);
    return events;
  }

  private EventProxy getEvent(final int code)
  {
    EventProxy event = mGlobalAlphabet.get(code);
    if (event == null) {
      final boolean controllable = (code & 1) != 0;
      final String name = "" + (controllable ? 'c' : 'u') + code;
      final EventKind kind =
        controllable ? EventKind.CONTROLLABLE : EventKind.UNCONTROLLABLE;
      event = mFactory.createEventProxy(name, kind);
      mGlobalAlphabet.put(code, event);
    }
    return event;
  }

  private int readShort(final DataInputStream stream)
    throws IOException
  {
    final int lo = stream.readByte() & 0xff;
    final int hi = stream.readByte() & 0xff;
    return (hi << 8) | lo;
  }

  private int readInt(final DataInputStream stream)
    throws IOException
  {
    final int b1 = stream.readByte() & 0xff;
    final int b2 = stream.readByte() & 0xff;
    final int b3 = stream.readByte() & 0xff;
    final int b4 = stream.readByte() & 0xff;
    return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
  }

  private void checkParameter(final int value,
                              final int expected,
                              final String name)
    throws WatersUnmarshalException
  {
    if (value != expected) {
      throw new WatersUnmarshalException
        ("Unsupported TTCT setting for '" + name + "': got " + value +
         ", but only " + expected + " is supported!");
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

  private TIntObjectHashMap<EventProxy> mGlobalAlphabet;
  private EventProxy mMarking;
  private Collection<EventProxy> mMarkings;

}
