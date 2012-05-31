//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Import/Export
//# PACKAGE: net.sourceforge.waters.external.susyna
//# CLASS:   SusynaImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.susyna;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A simple command-line tool to import a collection of Susyna automata as
 * a Waters module.
 *
 * @author Robi Malik
 */

public class SusynaImporter
{

  //#########################################################################
  //# Main method
  public static void main(final String args[])
  {
    try {
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final SusynaImporter importer = new SusynaImporter(desFactory);
      final ProductDESProxy des = importer.createProductDES("susyna", args);
      final ModuleProxyFactory modFactory = ModuleElementFactory.getInstance();
      final CompilerOperatorTable opTable =
        CompilerOperatorTable.getInstance();
      final ProductDESImporter converter = new ProductDESImporter(modFactory);
      final ModuleProxy module = converter.importModule(des);
      final ProxyMarshaller<ModuleProxy> marshaller =
        new JAXBModuleMarshaller(modFactory, opTable);
      // TODO Set output file from command line
      final File outfile = new File("susyna.wmod");
      marshaller.marshal(module, outfile);
    } catch (final SusynaParseException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
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
  public SusynaImporter(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mHandlersPass1 = new HashMap<String,ListHandler>(16);
    mHandlersPass1.put("states", new StatesListHandler());
    mHandlersPass1.put("initial-state", new InitialStateListHandler());
    mHandlersPass1.put("marker-states", new MarkerStateListHandler());
    mHandlersPass1.put("alphabet", new AlphabetListHandler());
    mHandlersPass1.put("controllable", new ControllableListHandler());
    mHandlersPass1.put("observable", new ObservableListHandler());
    mHandlersPass1.put("transitions", new IdleListHandler());
    mHandlersPass1.put("kind", new IdleListHandler());
    mHandlersPass2 = new HashMap<String,ListHandler>(16);
    mHandlersPass2.put("states", new IdleListHandler());
    mHandlersPass2.put("initial-state", new IdleListHandler());
    mHandlersPass2.put("marker-states", new IdleListHandler());
    mHandlersPass2.put("alphabet", new IdleListHandler());
    mHandlersPass2.put("controllable", new IdleListHandler());
    mHandlersPass2.put("observable", new IdleListHandler());
    mHandlersPass2.put("transitions", new TransitionListHandler());
    mHandlersPass2.put("kind", new IdleListHandler());
  }


  //#########################################################################
  //# Automaton Import
  public ProductDESProxy createProductDES(final String desName,
                                          final String[] filenames)
    throws IOException, SusynaParseException
  {
    mMarking = mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                         EventKind.PROPOSITION);
    mMarkings = Collections.singletonList(mMarking);
    mGlobalEventMap = new HashMap<String,EventInfo>();
    mLocalEventMap = new HashMap<String,EventInfo>();
    mStateMap = new HashMap<String,StateInfo>();
    mTransitions = new ArrayList<TransitionProxy>();
    final Collection<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(filenames.length);
    for (final String filename : filenames) {
      final File file = new File(filename);
      final String tail = file.getName();
      final int dotpos = tail.lastIndexOf('.');
      final String name = tail.substring(0, dotpos);
      readAutomaton(file, name, mHandlersPass1);
      final List<EventProxy> events = createLocalEvents(name);
      final List<StateProxy> states = createStates();
      readAutomaton(file, name, mHandlersPass2);
      final AutomatonProxy aut =
        mFactory.createAutomatonProxy(name, ComponentKind.PLANT,
                                      events, states, mTransitions);
      automata.add(aut);
      mLocalEventMap.clear();
      mStateMap.clear();
      mTransitions.clear();
    }
    final Collection<EventProxy> events = createGlobalEvents();
    return mFactory.createProductDESProxy(desName, events, automata);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void readAutomaton(final File file,
                             final String name,
                             final Map<String,ListHandler> handlers)
    throws IOException, SusynaParseException
  {
    final Reader stream = new FileReader(file);
    final BufferedReader reader = new BufferedReader(stream);
    mCurrentFile = file;
    mLineNo = 1;
    SusynaToken token = getNextToken(reader);
    checkTokenType(token, SusynaToken.Type.HEADER);
    token = getNextToken(reader);
    while (token.getTokenType() != SusynaToken.Type.EOF) {
      checkTokenType(token, SusynaToken.Type.IDENTIFIER);
      final String text = token.getText();
      final ListHandler handler = handlers.get(text);
      if (handler == null) {
        throw new SusynaParseException("Unknown list header '" + text + "'",
                                       mCurrentFile, mLineNo);
      }
      token = getNextToken(reader);
      checkTokenType(token, SusynaToken.Type.EQUALS);
      handler.parseList(reader);
      token = getNextToken(reader);
    }
    reader.close();
  }

  private List<EventProxy> createGlobalEvents()
  {
    final int size = mGlobalEventMap.size() + 1;
    final List<EventProxy> events = new ArrayList<EventProxy>(size);
    for (final EventInfo info : mGlobalEventMap.values()) {
      final EventProxy event = info.getEvent();
      events.add(event);
    }
    Collections.sort(events);
    events.add(mMarking);
    return events;
  }

  private List<EventProxy> createLocalEvents(final String autname)
  {
    final int size = mLocalEventMap.size() + 1;
    final List<EventProxy> events = new ArrayList<EventProxy>(size);
    final Iterator<Map.Entry<String,EventInfo>> iter =
      mLocalEventMap.entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<String,EventInfo> entry = iter.next();
      EventInfo local = entry.getValue();
      final EventProxy event;
      if (local.isObservable()) {
        final String name = entry.getKey();
        final EventInfo global = mGlobalEventMap.get(name);
        if (global == null) {
          mGlobalEventMap.put(name, local);
        } else {
          local = global;
          entry.setValue(global);
        }
        event = local.createEvent(autname);
      } else {
        event = local.createEvent(autname);
        final String name = event.getName();
        mGlobalEventMap.put(name, local);
      }
      events.add(event);
    }
    Collections.sort(events);
    events.add(mMarking);
    return events;
  }

  private List<StateProxy> createStates()
  {
    final int size = mStateMap.size();
    final List<StateProxy> states = new ArrayList<StateProxy>(size);
    for (final StateInfo info : mStateMap.values()) {
      final StateProxy state = info.createState();
      states.add(state);
    }
    Collections.sort(states);
    return states;
  }

  private EventInfo getLocalEvent(final String name)
    throws SusynaParseException
  {
    EventInfo info = mLocalEventMap.get(name);
    if (info ==  null) {
      info = new EventInfo(name);
      mLocalEventMap.put(name, info);
    }
    return info;
  }

  private StateInfo getState(final String name)
    throws SusynaParseException
  {
    StateInfo info = mStateMap.get(name);
    if (info ==  null) {
      info = new StateInfo(name);
      mStateMap.put(name, info);
    }
    return info;
  }


  //#########################################################################
  //# Scanning
  private SusynaToken getNextToken(final BufferedReader reader)
    throws IOException, SusynaParseException
  {
    if (mNextToken == null) {
      return readNextToken(reader);
    } else {
      final SusynaToken token = mNextToken;
      mNextToken = null;
      return token;
    }
  }

  private void putBack(final SusynaToken token)
  {
    assert mNextToken == null;
    mNextToken = token;
  }

  private SusynaToken readNextToken(final BufferedReader reader)
    throws IOException, SusynaParseException
  {
    int ch;
    do {
      ch = reader.read();
      if (ch < 0) {
        return SusynaToken.getToken(SusynaToken.Type.EOF);
      } else if (ch == '\n') {
        mLineNo++;
      }
    } while (Character.isWhitespace(ch));
    if (ch == '(') {
      return SusynaToken.getToken(SusynaToken.Type.OPENBR);
    } else if (ch == ')') {
      return SusynaToken.getToken(SusynaToken.Type.CLOSEBR);
    } else if (ch == ',') {
      return SusynaToken.getToken(SusynaToken.Type.COMMA);
    } else if (ch == '=') {
      return SusynaToken.getToken(SusynaToken.Type.EQUALS);
    } else if (ch == '[') {
      final StringBuffer buffer = new StringBuffer();
      while (true) {
        ch = reader.read();
        if (ch < 0) {
          throw new SusynaParseException
            ("End of file encountered while scanning header",
             mCurrentFile, mLineNo);
        } else if (ch == ']') {
          break;
        } else {
          buffer.append((char) ch);
        }
      }
      return new SusynaToken(SusynaToken.Type.HEADER, buffer);
    } else if (Character.isDigit(ch)) {
      final StringBuffer buffer = new StringBuffer();
      buffer.append((char) ch);
      while (true) {
        reader.mark(2);
        ch = reader.read();
        if (ch < 0) {
          break;
        } else if (Character.isDigit(ch)) {
          buffer.append((char) ch);
        } else {
          reader.reset();
          break;
        }
      }
      return new SusynaToken(SusynaToken.Type.INTCONST, buffer);
    } else if (Character.isJavaIdentifierStart(ch)) {
      final StringBuffer buffer = new StringBuffer();
      buffer.append((char) ch);
      while (true) {
        reader.mark(2);
        ch = reader.read();
        if (ch < 0) {
          break;
        } else if (ch == '-' || Character.isJavaIdentifierPart(ch)) {
          buffer.append((char) ch);
        } else {
          reader.reset();
          break;
        }
      }
      return new SusynaToken(SusynaToken.Type.IDENTIFIER, buffer);
    } else {
      throw new SusynaParseException("Unsupported character code " + ch,
                                     mCurrentFile, mLineNo);
    }
  }

  private void checkTokenType(final SusynaToken token,
                              final SusynaToken.Type type)
    throws SusynaParseException
  {
    if (token.getTokenType() != type) {
      throw new SusynaParseException(token, type, mCurrentFile, mLineNo);
    }
  }



  //#########################################################################
  //# Inner Class ListHandler
  private abstract class ListHandler
  {

    //#########################################################################
    //# Interface ListHandler
    void handleIdentifier(final String name)
      throws SusynaParseException
    {
    }

    void handleTransition(final String sourceName,
                          final String label,
                          final String targetName)
      throws SusynaParseException
    {
    }

    //#########################################################################
    //# Parsing
    private void parseList(final BufferedReader reader)
      throws IOException, SusynaParseException
    {
      final SusynaToken token = getNextToken(reader);
      switch (token.getTokenType()) {
      case IDENTIFIER:
      case INTCONST:
        putBack(token);
        parseIdentifierList(reader);
        break;
      case OPENBR:
        putBack(token);
        parseTransitionList(reader);
        break;
      default:
        throw new SusynaParseException(token, mCurrentFile, mLineNo);
      }
    }

    private void parseIdentifierList(final BufferedReader reader)
      throws IOException, SusynaParseException
    {
      SusynaToken token;
      do {
        final String name = parseStateName(reader);
        handleIdentifier(name);
        token = getNextToken(reader);
      } while (token.getTokenType() == SusynaToken.Type.COMMA);
      putBack(token);
    }

    private void parseTransitionList(final BufferedReader reader)
      throws IOException, SusynaParseException
    {
      SusynaToken token;
      do {
        token = getNextToken(reader);
        checkTokenType(token, SusynaToken.Type.OPENBR);
        final String srcName = parseStateName(reader);
        token = getNextToken(reader);
        checkTokenType(token, SusynaToken.Type.COMMA);
        final String targetName = parseStateName(reader);
        token = getNextToken(reader);
        checkTokenType(token, SusynaToken.Type.COMMA);
        token = getNextToken(reader);
        checkTokenType(token, SusynaToken.Type.IDENTIFIER);
        final String label = token.getText();
        token = getNextToken(reader);
        checkTokenType(token, SusynaToken.Type.CLOSEBR);
        handleTransition(srcName, label, targetName);
        token = getNextToken(reader);
      } while (token.getTokenType() == SusynaToken.Type.COMMA);
      putBack(token);
    }

    private String parseStateName(final BufferedReader reader)
      throws IOException, SusynaParseException
    {
      final SusynaToken token = getNextToken(reader);
      switch (token.getTokenType()) {
      case IDENTIFIER:
        return token.getText();
      case INTCONST:
        return token.getText();
      default:
        throw new SusynaParseException(token, SusynaToken.Type.IDENTIFIER,
                                       mCurrentFile, mLineNo);
      }
    }
  }


  //#########################################################################
  //# Inner Class IdleListHandler
  private class IdleListHandler extends ListHandler
  {
  }


  //#########################################################################
  //# Inner Class StatesListHandler
  private class StatesListHandler extends ListHandler
  {

    //#######################################################################
    //# Interface ListHandler
    @Override
    void handleIdentifier(final String name)
      throws SusynaParseException
    {
      getState(name);
    }

  }


  //#########################################################################
  //# Inner Class InitialStateListHandler
  private class InitialStateListHandler extends ListHandler
  {

    //#######################################################################
    //# Interface ListHandler
    @Override
    void handleIdentifier(final String name)
      throws SusynaParseException
    {
      final StateInfo info = getState(name);
      info.setInitial();
    }

  }


  //#########################################################################
  //# Inner Class MarkerStateListHandler
  private class MarkerStateListHandler extends ListHandler
  {

    //#######################################################################
    //# Interface ListHandler
    @Override
    void handleIdentifier(final String name)
      throws SusynaParseException
    {
      final StateInfo info = getState(name);
      info.setMarked();
    }

  }


  //#########################################################################
  //# Inner Class AlphabetListHandler
  private class AlphabetListHandler extends ListHandler
  {

    //#######################################################################
    //# Interface ListHandler
    @Override
    void handleIdentifier(final String name)
      throws SusynaParseException
    {
      getLocalEvent(name);
    }

  }


  //#########################################################################
  //# Inner Class ControllableListHandler
  private class ControllableListHandler extends ListHandler
  {

    //#######################################################################
    //# Interface ListHandler
    @Override
    void handleIdentifier(final String name)
      throws SusynaParseException
    {
      final EventInfo info = getLocalEvent(name);
      info.setControllable();
    }

  }


  //#########################################################################
  //# Inner Class ObservableListHandler
  private class ObservableListHandler extends ListHandler
  {

    //#######################################################################
    //# Interface ListHandler
    @Override
    void handleIdentifier(final String name)
      throws SusynaParseException
    {
      final EventInfo info = getLocalEvent(name);
      info.setObservable();
    }

  }


  //#########################################################################
  //# Inner Class TransitionListHandler
  private class TransitionListHandler extends ListHandler
  {

    //#######################################################################
    //# Interface ListHandler
    @Override
    void handleTransition(final String srcName,
                          final String label,
                          final String targetName)
      throws SusynaParseException
    {
      final StateInfo srcInfo = getState(srcName);
      final StateProxy srcState = srcInfo.getState();
      final EventInfo eventInfo = getLocalEvent(label);
      final EventProxy event = eventInfo.getEvent();
      final StateInfo targetInfo = getState(targetName);
      final StateProxy targetState = targetInfo.getState();
      final TransitionProxy trans =
        mFactory.createTransitionProxy(srcState, event, targetState);
      mTransitions.add(trans);
    }

  }


  //#########################################################################
  //# Inner Class StateInfo
  private class StateInfo
  {

    //#######################################################################
    //# Constructor
    private StateInfo(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Simple Access
    private void setInitial()
    {
      assert mState == null;
      mInitial = true;
    }

    private void setMarked()
    {
      assert mState == null;
      mMarked = true;
    }

    private StateProxy getState()
    {
      return mState;
    }

    private StateProxy createState()
    {
      if (mState == null) {
        final String name = getStandardisedName();
        final Collection<EventProxy> props = mMarked ? mMarkings : null;
        mState = mFactory.createStateProxy(name, mInitial, props);
      }
      return mState;
    }

    private String getStandardisedName()
    {
      final char ch = mName.charAt(0);
      if (Character.isDigit(ch)) {
        return "s" + mName;
      } else if (mName.indexOf('-') > 0) {
        return mName.replace('-', '_');
      } else {
        return mName;
      }
    }

    //#######################################################################
    //# Data Members
    private final String mName;
    private boolean mInitial;
    private boolean mMarked;
    private StateProxy mState;
  }


  //#########################################################################
  //# Inner Class EventInfo
  private class EventInfo
  {

    //#######################################################################
    //# Constructor
    private EventInfo(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Simple Access
    private void setControllable()
    {
      assert mEvent == null;
      mControllable = true;
    }

    private void setObservable()
    {
      assert mEvent == null;
      mObservable = true;
    }

    private boolean isObservable()
    {
      return mObservable;
    }

    private EventProxy getEvent()
    {
      return mEvent;
    }

    private EventProxy createEvent(final String autname)
    {
      if (mEvent == null) {
        final String name;
        if (mObservable) {
          name = mName;
        } else {
          name = mName + ":" + autname;
        }
        final EventKind kind =
          mControllable ? EventKind.CONTROLLABLE : EventKind.UNCONTROLLABLE;
        mEvent = mFactory.createEventProxy(name, kind);
      }
      return mEvent;
    }

    //#######################################################################
    //# Data Members
    private final String mName;
    private boolean mControllable;
    private boolean mObservable;
    private EventProxy mEvent;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final Map<String,ListHandler> mHandlersPass1;
  private final Map<String,ListHandler> mHandlersPass2;

  private Map<String,EventInfo> mGlobalEventMap;
  private Map<String,EventInfo> mLocalEventMap;
  private Map<String,StateInfo> mStateMap;
  private List<TransitionProxy> mTransitions;
  private EventProxy mMarking;
  private Collection<EventProxy> mMarkings;

  private SusynaToken mNextToken;
  private File mCurrentFile;
  private int mLineNo;

}