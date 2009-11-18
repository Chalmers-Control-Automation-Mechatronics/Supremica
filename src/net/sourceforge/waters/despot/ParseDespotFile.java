package net.sourceforge.waters.despot;

import java.io.File;
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
  // stores the ID number and name of the <State> elements from .des files
  HashMap<Integer,Integer> mStates;
  // stores the ID number and name of the <Event> elements from .des files
  HashMap<Integer,String> mEvents;
  // stores the source states and a list of all the states their direct
  // transitions
  // go to. This is used to lookup quickly whether a state has more than one
  // event on a transition to the same state
  HashMap<Integer,HashSet<Integer>> mTransitions;

  List<NodeProxy> nodes = new ArrayList<NodeProxy>();
  List<EdgeProxy> edges = new ArrayList<EdgeProxy>();
  // List<EdgeProxy> eventDecls = new ArrayList<EdgeProxy>();

  // used to build up the module for the .wmod file we are converting into
  ModuleProxyFactory factory = ModuleElementFactory.getInstance();

  public static void main(String[] args) throws ParserConfigurationException,
      SAXException, IOException
  {

    File file = new File("/home/rmf18/Desktop/Data/hsup1.des");
    ParseDespotFile pdf = new ParseDespotFile();
    GraphProxy graph = pdf.constructGraph(file);
    for (EdgeProxy edge : graph.getEdges()) {
      System.out.println("Source: " + edge.getSource().getName() + " Target: "
          + edge.getTarget().getName());
      for (int i = 0; i < edge.getLabelBlock().getEventList().size(); i++) {
        SimpleIdentifierProxy event =
            (SimpleIdentifierProxy) edge.getLabelBlock().getEventList().get(i);
        System.out.print("Event: " + event.getName());
      }
    }
    System.out.println("States");
    for (NodeProxy state : graph.getNodes()) {
      System.out.println(state.getName());
    }
  }

  /**
   *
   * @param file
   */
  private void parseDESFile(File despFile)
  {

  }

  /**
   * Constructs the SimpleComponent section for the module of a .wdom file.
   *
   * @param file
   * @return
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   */
  private SimpleComponentProxy constructSimpleComponent(File despFile)
      throws ParserConfigurationException, SAXException, IOException
  {

    DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(despFile);

    NodeList supervisors = doc.getElementsByTagName("Supervisor");
    for (int i = 0; i < supervisors.getLength(); i++) {
      Element supervisor = (Element) supervisors.item(i);
      Element des = null;

      // File desFile = GraphProxy graph = constructGraph(desFile);
      IdentifierProxy identifier = factory.createSimpleIdentifierProxy("name");
    }

    return null;
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
  private GraphProxy constructGraph(File file)
      throws ParserConfigurationException, SAXException, IOException
  {

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
      nodes.add(node);
      // stores the ID number and the index of this node in the list
      storeStateId(stElmnt, i);
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
    NodeList transElmntLst = transitions.getElementsByTagName("*");
    for (int i = 0; i < transElmntLst.getLength(); i++) {
      Element trElmnt = (Element) transElmntLst.item(i);

      // gets the source and target states ID numbers
      int srcID = Integer.parseInt(trElmnt.getAttribute("fID"));
      int targetID = Integer.parseInt(trElmnt.getAttribute("tID"));
      // checks if the transition already exists (i.e. so an event should be
      // added to an existing edge rather than creating a new edge
      boolean exists = false;
      if (mTransitions.containsKey(srcID)) {
        if (mTransitions.get(srcID).contains(targetID)) {
          exists = true;
        }
      }
      EdgeProxy edge = convertTransition(trElmnt, exists);
      if (!exists) {
        edges.add(edge);
        // stores this transition
        storeTransition(trElmnt);
      } else {
        // can find where this edge exists in the array list by using the hash
        // map which references it using the IDs
        int edgeIndex = mStates.get(srcID);
        edges.set(edgeIndex, edge);
      }

    }
    // NEED TO ADD BLOCKED EVENTS TO THIS GRAPHPROXY (rather than null)
    return factory.createGraphProxy(true, null, nodes, edges, null);

  }

  private void storeEvents(NodeList events)
  {
    for (int i = 0; i < events.getLength(); i++) {
      Element event = (Element) events.item(i);
      int eventID = Integer.parseInt(event.getAttribute("id"));
      String eventName = event.getAttribute("nm");
      mEvents.put(eventID, eventName);
    }
  }

  private void storeTransition(Element transition)
  {
    HashSet<Integer> currentTargets =
        mTransitions.get(transition.getAttribute("fID"));
    if (currentTargets == null) {
      currentTargets = new HashSet<Integer>();
    }
    currentTargets.add(Integer.parseInt(transition.getAttribute("tID")));
    mTransitions.put(Integer.parseInt(transition.getAttribute("fID")),
        currentTargets);
  }

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
    String eventName = mEvents.get(eventID);
    List<SimpleIdentifierProxy> eventList =
        new ArrayList<SimpleIdentifierProxy>();

    if (exists) {
      // there is already an existing edge in this direction between these two
      // states, so add this event to that edge rather than creating a new
      // edge

      // can find where this edge exists in the array list by using the hash
      // map which references it using the IDs
      int edgeIndex = mStates.get(srcID);

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
    private final int mSource;
    private final int mTarget;
  }
}
