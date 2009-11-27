package net.sourceforge.waters.despot;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author Rachel Francis
 */

public class ParseDespotFile
{

  //#########################################################################
  //# Main
  public static void main(final String[] args) throws ParserConfigurationException,
      SAXException, IOException, URISyntaxException
  {
    final File file = new File("/home/rmf18/Desktop/Data/testHISC.desp");
    final URI path = file.toURI();
    final ParseDespotFile pdf = new ParseDespotFile();
    final DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    final Document doc = builder.parse(file);
    // gets the root element
    final Element project = doc.getDocumentElement();
    // currently parses the first subsystem only
    final Element subsystem =
        (Element) project.getElementsByTagName("Subsystem").item(0);
    //NodeList subsystems = project.getElementsByTagName("Subsystem");
    //for(int j=0;j<subsystems.getLength();j++){
    //Element subsystem = subsystems.item(j);

    final NodeList automaton = subsystem.getElementsByTagName("*");
    for (int i = 0; i < automaton.getLength(); i++) {
      final Element aut = (Element) automaton.item(i);
      if (aut.getTagName().equals("Supervisor")) {
        final SimpleComponentProxy scp =
            pdf.constructSimpleComponent(aut, ComponentKind.SPEC, path);
        System.out.println(scp);
      } else if (aut.getTagName().equals("Plant")) {
        final SimpleComponentProxy scp =
            pdf.constructSimpleComponent(aut, ComponentKind.PLANT, path);
        System.out.println(scp);
      }
      pdf.clearStructures();
    }

  }


  //#########################################################################
  //# Initialisation
  /**
   * Initialises the data structures used to store the states, events and
   * transitions in the construction of a graph.
   */
  private void clearStructures()
  {
    mStates.clear();
    mEvents.clear();
    mTransitions.clear();
    mNodes.clear();
    mEdges.clear();
  }


  //#########################################################################
  //# Element Conversion
  /**
   * Constructs the SimpleComponent section for the module of a
   * <CODE>.wmod</CODE> file.
   */
  private SimpleComponentProxy constructSimpleComponent(final Element automaton,
      final ComponentKind kind, final URI path) throws ParserConfigurationException,
      SAXException, IOException, URISyntaxException
  {
    final Element des = (Element) automaton.getElementsByTagName("*").item(0);
    final String autName = des.getAttribute("name");
    final String autFile = des.getAttribute("location");
    final URI uri = new URI(autFile);

    final GraphProxy graph = constructGraph(path.resolve(uri));
    final IdentifierProxy identifier = mFactory.createSimpleIdentifierProxy(autName);

    return mFactory.createSimpleComponentProxy(identifier, kind, graph);
  }

  /**
   * Constructs the Graph section for the module of a <CODE>.wmod</CODE> file.
   * Converts transitions to edges, and converts states to nodes.
   * @param  uri    URI indicating a <CODE>.des</CODE> file to parse.
   */
  private GraphProxy constructGraph(final URI uri)
      throws ParserConfigurationException, SAXException, IOException
  {
    final File file = new File(uri);
    final DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    final Document doc = builder.parse(file);
    // gets the root element
    final Element des = doc.getDocumentElement();
    final Element definition =
        (Element) des.getElementsByTagName("Definition").item(0);

    // converts each State in the despot file into nodes for waters
    final NodeList allStates = definition.getElementsByTagName("States");
    final Element states = (Element) allStates.item(0);
    final NodeList stElmntLst = states.getElementsByTagName("*");
    for (int i = 0; i < stElmntLst.getLength(); i++) {
      final Element stElmnt = (Element) stElmntLst.item(i);
      final NodeProxy node = convertState(stElmnt);
      storeStateId(stElmnt, i);
      mNodes.add(node);
    }

    // create a hash map of the ID numbers and the name they belong to for the
    // events
    final NodeList allEvents = definition.getElementsByTagName("Events");
    final Element events = (Element) allEvents.item(0);
    final NodeList evElmntLst = events.getElementsByTagName("*");
    storeEvents(evElmntLst);

    // converts each transition in the despot file into edges for waters
    final NodeList transitionList = definition.getElementsByTagName("Trans-Function");
    final Element transitions = (Element) transitionList.item(0);
    final NodeList transElmntLst = transitions.getElementsByTagName("Transitions");
    final Element transition = (Element) transElmntLst.item(0);
    final NodeList trList = transition.getElementsByTagName("*");
    for (int i = 0; i < trList.getLength(); i++) {
      final Element trElmnt = (Element) trList.item(i);

      // gets the source and target states ID numbers
      final int srcID = Integer.parseInt(trElmnt.getAttribute("fID"));
      final int targetID = Integer.parseInt(trElmnt.getAttribute("tID"));
      // checks if the transition already exists (i.e. so an event should be
      // added to an existing edge rather than creating a new edge
      boolean exists = false;
      final IdPair srcTargIds = new IdPair(srcID, targetID);
      if (mTransitions.containsKey(srcTargIds)) {
        exists = true;
      }
      final EdgeProxy edge = convertTransition(trElmnt, exists);
      if (!exists) {
        mEdges.add(edge);
        // stores this transition
        storeTransition(trElmnt, i);
      } else {
        // can find where this edge exists in the 'edges' list by using the hash
        // map which references it using the source and target IDs
        final int edgeIndex = mTransitions.get(srcTargIds);
        mEdges.set(edgeIndex, edge);
      }

    }

    // gets the blocked events for this automata
    final NodeList transLoopList = transitions.getElementsByTagName("*");
    final LabelBlockProxy blockedEvents = findBlockedEvents(transLoopList);

    return mFactory.createGraphProxy(true, blockedEvents, mNodes, mEdges);

  }

  /**
   * Determines which events are blocked in this automata. Events are blocked if
   * they do not appear on a transition or selfloop.
   * @param transLoopList
   *          The list of Transitions and selfloops from the DOM.
   */
  private LabelBlockProxy findBlockedEvents(final NodeList transLoopList)
  {
    boolean found = false;
    final List<SimpleIdentifierProxy> blockedEventList =
        new ArrayList<SimpleIdentifierProxy>();
    for (final int eventID : mEvents.keySet()) {
      found = false;
      for (int i = 0; i < transLoopList.getLength(); i++) {
        final Element trElmnt = (Element) transLoopList.item(i);
        final NodeList transitions = trElmnt.getElementsByTagName("*");
        for (int j = 0; j < transitions.getLength(); j++) {
          final Element tr = (Element) transitions.item(j);
          if (Integer.parseInt(tr.getAttribute("eID")) == eventID) {
            found = true;
            break;
          }
        }
        if (found)
          break;
      }
      if (!found) {
        blockedEventList.add(mFactory.createSimpleIdentifierProxy(mEvents
            .get(eventID)));
      }

    }
    return mFactory.createLabelBlockProxy(blockedEventList, null);

  }

  /**
   * Maps the IDs for all the events in the despot file to their name.
   * @param events
   *          The list of event elements from the DOM.
   */
  private void storeEvents(final NodeList events)
  {
    for (int i = 0; i < events.getLength(); i++) {
      final Element event = (Element) events.item(i);
      final int eventID = Integer.parseInt(event.getAttribute("id"));
      final String eventName = event.getAttribute("nm");
      mEvents.put(eventID, eventName);
    }
  }

  /**
   * Creates an {@link IdPair} for a transitions source and target states IDs
   * and maps it to the index of the transition in the 'edge' list.
   */
  private void storeTransition(final Element transition, final int index)
  {
    final int srcId = Integer.parseInt(transition.getAttribute("fID"));
    final int targetId = Integer.parseInt(transition.getAttribute("tID"));
    final IdPair srcTargIds = new IdPair(srcId, targetId);
    mTransitions.put(srcTargIds, index);
  }

  /**
   * Adds to the hash map which maps the despot ID numbers to the position of
   * the relevant {@link NodeProxy} in the 'nodes' list.
   * @param state
   *          The state being referenced, whose ID needs storing.
   * @param index
   *          The index number of the matching NodeProxy in the 'nodes' list.
   */
  private void storeStateId(final Element state, final int index)
  {
    mStates.put(Integer.parseInt(state.getAttribute("id")), index);
  }

  /**
   * Converts a transition from the despot file into an edge for the waters
   * file.
   * @param tr
   *          The transition to be converted.
   */
  private EdgeProxy convertTransition(final Element tr, final boolean exists)
  {
    // gets the source and target states ID numbers
    final int srcID = Integer.parseInt(tr.getAttribute("fID"));
    final int targetID = Integer.parseInt(tr.getAttribute("tID"));

    // gets the index number of the source and target states in the list
    final int srcIndex = mStates.get(srcID);
    final int targetIndex = mStates.get(targetID);

    // assigns the correct name to the edge
    final String eventID = tr.getAttribute("eID");
    // assert mEvents.containsKey((eventID));
    final String eventName = mEvents.get(Integer.parseInt(eventID));
    final List<SimpleIdentifierProxy> eventList =
        new ArrayList<SimpleIdentifierProxy>();

    if (exists) {
      // there is already an existing edge in this direction between these two
      // states, so add this event to that edge rather than creating a new
      // edge

      // can find where this edge exists in the array list by using the hash
      // map which references it using the IDs
      final IdPair srcTargIds = new IdPair(srcID, targetID);
      final int edgeIndex = mTransitions.get(srcTargIds);

      // gets the existing events
      final List<Proxy> existingEvents =
          mEdges.get(edgeIndex).getLabelBlock().getEventList();

      // adds each event that already exists to the new list (since these
      // objects are immutable and can't be updated)
      for (int i = 0; i < existingEvents.size(); i++) {
        final SimpleIdentifierProxy event =
            (SimpleIdentifierProxy) existingEvents.get(i);
        eventList.add(mFactory.createSimpleIdentifierProxy(event.getName()));
      }

    }
    eventList.add(mFactory.createSimpleIdentifierProxy(eventName));
    final LabelBlockProxy transEvents =
        mFactory.createLabelBlockProxy(eventList, null);

    return mFactory.createEdgeProxy(mNodes.get(srcIndex), mNodes.get(targetIndex),
        transEvents, null, null, null, null);

  }

  /**
   * Converts a state from the despot file into a node for the waters file.
   * @param state
   *          The state to be converted.
   */
  private NodeProxy convertState(final Element state)
  {
    final String marked = "1";
    final String stateName = state.getAttribute("nm");
    if (state.getTagName().equals("St")) {
      // checks if the state is marked (i.e. accepting)
      if (state.getAttribute("mk").equals(marked)) {
        return markState(state, false);
      } else {
        return mFactory.createSimpleNodeProxy(stateName);
      }
    }
    // the state needs to be set as the initial state.
    else {
      // checks if the state is marked (i.e. accepting)
      if (state.getAttribute("mk").equals(marked)) {
        return markState(state, true);
      }
      return mFactory.createSimpleNodeProxy(stateName, null, true, null, null,
          null);
    }

  }

  /**
   * Marks a state as accepting.
   * @param state
   *          The marked state.
   * @param initial
   *          States whether this is the initial state.
   */
  private NodeProxy markState(final Element state, final Boolean initial)
  {
    final String stateName = state.getAttribute("nm");
    // holds the :accepting constant
    final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;

    final List<SimpleIdentifierProxy> markList =
        new ArrayList<SimpleIdentifierProxy>(1);
    markList.add(mFactory.createSimpleIdentifierProxy(accepting));
    final PlainEventListProxy accept = mFactory.createPlainEventListProxy(markList);
    if (!initial) {
      return mFactory.createSimpleNodeProxy(stateName, accept, false, null,
          null, null);
    } else {
      return mFactory.createSimpleNodeProxy(stateName, accept, true, null, null,
          null);
    }
  }


  //#########################################################################
  //# Inner Class IdPair
  /**
   * A class used to pair the ID numbers for the source and target states of a
   * transition/event.
   */
  private static class IdPair
  {
    private IdPair(final int source, final int target)
    {
      mSource = source;
      mTarget = target;
    }

    public int hashCode()
    {
      return mSource + 5 * mTarget;
    }

    public boolean equals(final Object other)
    {
      if (other != null && getClass() == other.getClass()) {
        final IdPair pair = (IdPair) other;
        return mSource == pair.mSource && mTarget == pair.mTarget;
      } else {
        return false;
      }
    }

    // ID number of the source state of the transition as used by despot
    private final int mSource;
    // ID number of the target state of the transition as used by despot
    private final int mTarget;
  }


  //#########################################################################
  //# Data Members
  /**
   * Stores the ID number the despot file uses to reference a state and the
   * location of the state in the 'nodes' list.
   */
  private final Map<Integer,Integer> mStates = new HashMap<Integer,Integer>();
  /**
   * Stores the ID number the despot file uses to reference an event and the
   * name of the event.
   */
  private final Map<Integer,String> mEvents = new HashMap<Integer,String>();
  // The above two hash maps are needed for finding the correct source/target
  // state of a transition and correct event name.

  /**
   * Maps the source state and target state together (as an IdPair) for a
   * transition to the index number where this edge exists in the list 'edges'.
   * This is used to lookup quickly whether an edge already exists.
   */
  private final Map<IdPair,Integer> mTransitions = new HashMap<IdPair,Integer>();

  /**
   * This list stores all the nodes (states) for the current automaton.
   */
  private final List<NodeProxy> mNodes = new ArrayList<NodeProxy>();
  /**
   * This list stores all the edges (transitions) for the current automaton.
   */
  private final List<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();

  /**
   * The factory used to build up the modules for the <CODE>.wmod</CODE>
   * files we are converting into.
   */
  private final ModuleProxyFactory mFactory = ModuleElementFactory.getInstance();

}
