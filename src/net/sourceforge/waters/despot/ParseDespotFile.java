package net.sourceforge.waters.despot;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.GraphProxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


@SuppressWarnings("unused")
public class ParseDespotFile
{
  // #Data Members
  // stores the ID number the despot file uses to reference a state and the
  // location of the state in the 'nodes' list
  HashMap<Integer,Integer> mStates = new HashMap<Integer,Integer>();
  // stores the ID number the despot file uses to reference an event and the
  // name of the event
  HashMap<Integer,String> mEvents = new HashMap<Integer,String>();
  // (the above two hash maps are needed for finding the correct source/target
  // state of a transition and correct event name

  // maps the source state and target state together (as an IdPair) for a
  // transition to the index number where this edge exists in the list 'edges'.
  // This is used to lookup quickly whether an edge already exists.
  HashMap<IdPair,Integer> mTransitions = new HashMap<IdPair,Integer>();

  // this list stores all the nodes (states) for this automata
  List<NodeProxy> nodes = new ArrayList<NodeProxy>();
  // this list stores all the edges (transitions) for this automata
  List<EdgeProxy> edges = new ArrayList<EdgeProxy>();

  // used to build up the module for the .wmod file we are converting into
  ModuleProxyFactory factory = ModuleElementFactory.getInstance();

  public static void main(String[] args) throws ParserConfigurationException,
      SAXException, IOException, URISyntaxException
  {
    File file = new File("/home/rmf18/Desktop/Data/testHISC.desp");
    URI path = file.toURI();
    ParseDespotFile pdf = new ParseDespotFile();
    DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(file);
    // gets the root element
    Element project = doc.getDocumentElement();
    // currently parses the first subsystem only
    Element subsystem =
        (Element) project.getElementsByTagName("Subsystem").item(0);
    NodeList automaton = subsystem.getElementsByTagName("*");
    for (int i = 0; i < automaton.getLength(); i++) {
      Element aut = (Element) automaton.item(i);
      if (aut.getTagName().equals("Supervisor")) {
        SimpleComponentProxy scp =
            pdf.constructSimpleComponent(aut, ComponentKind.SUPERVISOR, path);
        System.out.println(scp);
      } else if (aut.getTagName().equals("Plant")) {
        SimpleComponentProxy scp =
            pdf.constructSimpleComponent(aut, ComponentKind.PLANT, path);
        System.out.println(scp);
      }
      pdf.clearStructures();
    }

  }

  /**
   * Initialises the data structures used to store the states, events and
   * transitions in the construction of a graph.
   */
  private void clearStructures()
  {
    mStates = new HashMap<Integer,Integer>();
    mEvents = new HashMap<Integer,String>();
    mTransitions = new HashMap<IdPair,Integer>();
    nodes = new ArrayList<NodeProxy>();
    edges = new ArrayList<EdgeProxy>();

  }

  /**
   * Constructs the SimpleComponent section for the module of a .wdom file.
   *
   * @param file
   * @return
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws URISyntaxException
   */
  private SimpleComponentProxy constructSimpleComponent(Element automaton,
      ComponentKind kind, URI path) throws ParserConfigurationException,
      SAXException, IOException, URISyntaxException
  {
    Element des = (Element) automaton.getElementsByTagName("*").item(0);
    String autName = des.getAttribute("name");
    String autFile = des.getAttribute("location");
    URI uri = new URI(autFile);

    GraphProxy graph = constructGraph(path.resolve(uri));
    IdentifierProxy identifier = factory.createSimpleIdentifierProxy(autName);

    return factory.createSimpleComponentProxy(identifier, kind, graph);

  }

  /**
   * Constructs the Graph section for the module of a .wdom file. Converts
   * transitions to edges, and converts states to nodes.
   *
   * @param file
   *          The despot file to parse.
   * @return
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   */
  private GraphProxy constructGraph(URI uri)
      throws ParserConfigurationException, SAXException, IOException
  {
    File file = new File(uri);
    DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(file);
    // gets the root element
    Element des = doc.getDocumentElement();
    Element definition =
        (Element) des.getElementsByTagName("Definition").item(0);

    // converts each State in the despot file into nodes for waters
    NodeList allStates = definition.getElementsByTagName("States");
    Element states = (Element) allStates.item(0);
    NodeList stElmntLst = states.getElementsByTagName("*");
    for (int i = 0; i < stElmntLst.getLength(); i++) {
      Element stElmnt = (Element) stElmntLst.item(i);
      NodeProxy node = convertState(stElmnt);
      storeStateId(stElmnt, i);
      nodes.add(node);
    }

    // create a hash map of the ID numbers and the name they belong to for the
    // events
    NodeList allEvents = definition.getElementsByTagName("Events");
    Element events = (Element) allEvents.item(0);
    NodeList evElmntLst = events.getElementsByTagName("*");
    storeEvents(evElmntLst);

    // converts each transition in the despot file into edges for waters
    NodeList transitionList = definition.getElementsByTagName("Trans-Function");
    Element transitions = (Element) transitionList.item(0);
    NodeList transElmntLst = transitions.getElementsByTagName("Transitions");
    Element transition = (Element) transElmntLst.item(0);
    NodeList trList = transition.getElementsByTagName("*");
    for (int i = 0; i < trList.getLength(); i++) {
      Element trElmnt = (Element) trList.item(i);

      // gets the source and target states ID numbers
      int srcID = Integer.parseInt(trElmnt.getAttribute("fID"));
      int targetID = Integer.parseInt(trElmnt.getAttribute("tID"));
      // checks if the transition already exists (i.e. so an event should be
      // added to an existing edge rather than creating a new edge
      boolean exists = false;
      IdPair srcTargIds = new IdPair(srcID, targetID);
      if (mTransitions.containsKey(srcTargIds)) {
        exists = true;
      }
      EdgeProxy edge = convertTransition(trElmnt, exists);
      if (!exists) {
        edges.add(edge);
        // stores this transition
        storeTransition(trElmnt, i);
      } else {
        // can find where this edge exists in the 'edges' list by using the hash
        // map which references it using the source and target IDs
        int edgeIndex = mTransitions.get(srcTargIds);
        edges.set(edgeIndex, edge);
      }

    }

    // gets the blocked events for this automata
    NodeList transLoopList = transitions.getElementsByTagName("*");
    LabelBlockProxy blockedEvents = findBlockedEvents(transLoopList);

    return factory.createGraphProxy(true, blockedEvents, nodes, edges, null);

  }

  /**
   * Determines which events are blocked in this automata. Events are blocked if
   * they don't appear on a transition or self loop.
   *
   * @param transLoopList
   *          The list of Transitions and self-loops from the DOM.
   * @return
   */
  private LabelBlockProxy findBlockedEvents(NodeList transLoopList)
  {
    boolean found = false;
    List<SimpleIdentifierProxy> blockedEventList =
        new ArrayList<SimpleIdentifierProxy>();
    for (int eventID : mEvents.keySet()) {
      found = false;
      for (int i = 0; i < transLoopList.getLength(); i++) {
        Element trElmnt = (Element) transLoopList.item(i);
        NodeList transitions = trElmnt.getElementsByTagName("*");
        for (int j = 0; j < transitions.getLength(); j++) {
          Element tr = (Element) transitions.item(j);
          if (Integer.parseInt(tr.getAttribute("eID")) == eventID) {
            found = true;
            break;
          }
        }
        if (found)
          break;
      }
      if (!found) {
        blockedEventList.add(factory.createSimpleIdentifierProxy(mEvents
            .get(eventID)));
      }

    }
    return factory.createLabelBlockProxy(blockedEventList, null);

  }

  /**
   * Maps the ID's for all the events in the despot file to their name.
   *
   * @param events
   *          The list of event elements from the DOM.
   */
  private void storeEvents(NodeList events)
  {
    for (int i = 0; i < events.getLength(); i++) {
      Element event = (Element) events.item(i);
      int eventID = Integer.parseInt(event.getAttribute("id"));
      String eventName = event.getAttribute("nm");
      mEvents.put(eventID, eventName);
    }
  }

  /**
   * Creates an IdPair for a transitions source and target states id's and maps
   * it to the index of the transition in the 'edge' list.
   *
   * @param transition
   * @param index
   */
  private void storeTransition(Element transition, int index)
  {
    int srcId = Integer.parseInt(transition.getAttribute("fID"));
    int targetId = Integer.parseInt(transition.getAttribute("tID"));
    IdPair srcTargIds = new IdPair(srcId, targetId);
    mTransitions.put(srcTargIds, index);
  }

  /**
   * Adds to the hash map which maps the despot ID numbers to the position of
   * the relevant NodeProxy in the 'nodes' list.
   *
   * @param state
   *          The state being referenced, whose ID needs storing.
   * @param index
   *          The index number of the matching NodeProxy in the 'nodes' list.
   */
  private void storeStateId(Element state, int index)
  {
    mStates.put(Integer.parseInt(state.getAttribute("id")), index);
  }

  /**
   * Converts a transition from the despot file into an edge for the waters
   * file.
   *
   * @param tr
   *          The transition to be converted.
   * @return
   */
  private EdgeProxy convertTransition(Element tr, boolean exists)
  {
    // gets the source and target states ID numbers
    int srcID = Integer.parseInt(tr.getAttribute("fID"));
    int targetID = Integer.parseInt(tr.getAttribute("tID"));

    // gets the index number of the source and target states in the list
    int srcIndex = mStates.get(srcID);
    int targetIndex = mStates.get(targetID);

    // assigns the correct name to the edge
    String eventID = tr.getAttribute("eID");
    // assert mEvents.containsKey((eventID));
    String eventName = mEvents.get(Integer.parseInt(eventID));
    List<SimpleIdentifierProxy> eventList =
        new ArrayList<SimpleIdentifierProxy>();

    if (exists) {
      // there is already an existing edge in this direction between these two
      // states, so add this event to that edge rather than creating a new
      // edge

      // can find where this edge exists in the array list by using the hash
      // map which references it using the IDs
      IdPair srcTargIds = new IdPair(srcID, targetID);
      int edgeIndex = mTransitions.get(srcTargIds);

      // gets the existing events
      List<Proxy> existingEvents =
          edges.get(edgeIndex).getLabelBlock().getEventList();

      // adds each event that already exists to the new list (since these
      // objects are immutable and can't be updated)
      for (int i = 0; i < existingEvents.size(); i++) {
        SimpleIdentifierProxy event =
            (SimpleIdentifierProxy) existingEvents.get(i);
        eventList.add(factory.createSimpleIdentifierProxy(event.getName()));
      }

    }
    eventList.add(factory.createSimpleIdentifierProxy(eventName));
    LabelBlockProxy transEvents =
        factory.createLabelBlockProxy(eventList, null);

    return factory.createEdgeProxy(nodes.get(srcIndex), nodes.get(targetIndex),
        transEvents, null, null, null, null);

  }

  /**
   * Converts a state from the despot file into a node for the waters file.
   *
   * @param state
   *          The state to be converted.
   * @return
   */
  private NodeProxy convertState(Element state)
  {
    final String marked = "1";
    final String stateName = state.getAttribute("nm");
    if (state.getTagName().equals("St")) {
      // checks if the state is marked (i.e. accepting)
      if (state.getAttribute("mk").equals(marked)) {
        return markState(state, false);
      } else {
        return factory.createSimpleNodeProxy(stateName);
      }
    }
    // the state needs to be set as the initial state.
    else {
      // checks if the state is marked (i.e. accepting)
      if (state.getAttribute("mk").equals(marked)) {
        return markState(state, true);
      }
      return factory.createSimpleNodeProxy(stateName, null, true, null, null,
          null);
    }

  }

  /**
   * Marks a state as accepting.
   *
   * @param state
   *          The marked state.
   * @param initial
   *          States whether this is the initial state.
   * @return
   */
  private NodeProxy markState(Element state, Boolean initial)
  {
    final String stateName = state.getAttribute("nm");
    // holds the :accepting constant
    final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;

    List<SimpleIdentifierProxy> markList =
        new ArrayList<SimpleIdentifierProxy>(1);
    markList.add(factory.createSimpleIdentifierProxy(accepting));
    PlainEventListProxy accept = factory.createPlainEventListProxy(markList);
    if (!initial) {
      return factory.createSimpleNodeProxy(stateName, accept, false, null,
          null, null);
    } else {
      return factory.createSimpleNodeProxy(stateName, accept, true, null, null,
          null);
    }
  }


  /**
   * A class used to pair the ID numbers for the source and target states of a
   * transition/event.
   *
   * @author rmf18
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
}
