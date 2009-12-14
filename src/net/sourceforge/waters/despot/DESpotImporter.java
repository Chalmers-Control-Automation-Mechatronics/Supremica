package net.sourceforge.waters.despot;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;
import net.sourceforge.waters.xsd.module.SplineKind;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author Rachel Francis
 */

public class DESpotImporter implements CopyingProxyUnmarshaller<ModuleProxy>
{

  // #########################################################################
  // # Constructors
  public DESpotImporter(final ModuleProxyFactory factory,
      final DocumentManager docman)
  {
    this(null, factory, docman);
  }

  public DESpotImporter(final File outputdir, final ModuleProxyFactory factory,
      final DocumentManager docman)
  {
    mOutputDir = outputdir;
    mFactory = factory;

    mDocumentManager = docman;

  }

  // #########################################################################
  // # Interface
  // net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller
  public File getOutputDirectory()
  {
    return mOutputDir;
  }

  public void setOutputDirectory(final File outputdir)
  {
    mOutputDir = outputdir;
  }

  public ModuleProxy unmarshalCopying(final URI uri) throws IOException,
      WatersMarshalException, WatersUnmarshalException
  {
    try {
      return convertDESpotHierarchy(uri);
    } catch (final ParserConfigurationException exception) {
      throw new WatersUnmarshalException(exception);
    } catch (final SAXException exception) {
      throw new WatersUnmarshalException(exception);
    } catch (final URISyntaxException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  // #########################################################################
  // # Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
  public ModuleProxy unmarshal(final URI uri) throws IOException,
      WatersUnmarshalException
  {
    try {
      return unmarshalCopying(uri);
    } catch (final WatersMarshalException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  public String getDefaultExtension()
  {
    return DESPOTEXT;
  }

  public Collection<String> getSupportedExtensions()
  {
    return EXTENSIONS;
  }

  public Collection<FileFilter> getSupportedFileFilters()
  {
    return FILTERS;
  }

  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
  }

  private ModuleProxy convertDESpotHierarchy(final URI uri)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException, WatersMarshalException
  {
    final URL url = uri.toURL();
    final InputStream stream = url.openStream();
    final DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    final Document doc = builder.parse(stream);
    // gets the root element
    final Element project = doc.getDocumentElement();
    ModuleProxy module = null;

    // maps the name of the interfaces to their filenames for this module
    final Map<String,String> interfaceMap = new HashMap<String,String>();
    final NodeList interfaces = project.getElementsByTagName("Interface");
    for (int j = 0; j < interfaces.getLength(); j++) {
      final Element interfaceDES = (Element) interfaces.item(j);
      final Element des =
          (Element) interfaceDES.getElementsByTagName("*").item(0);
      final String name = interfaceDES.getAttribute("name");
      final String location = des.getAttribute("location");
      interfaceMap.put(name, location);
    }
    final NodeList subsystemList = project.getElementsByTagName("Subsystem");
    // extracts each subsystem element and puts them into an array list
    final ArrayList<Element> subsystems = convertNodeList(subsystemList);

    // sorts the subsystems in order of their level (lowest to highest)
    Collections.sort(subsystems, new LevelComparator());

    for (int j = 0; j < subsystems.size(); j++) {
      // adds the accepting proposition to the events list
      addAcceptingProp();

      final Element subsystem = subsystems.get(j);

      final NodeList automaton = subsystem.getElementsByTagName("*");
      for (int i = 0; i < automaton.getLength(); i++) {
        final Element aut = (Element) automaton.item(i);
        if (aut.getTagName().equals("Supervisor")) {
          // builds all specification automata
          constructSimpleComponent(aut, ComponentKind.SPEC, uri);

        } else if (aut.getTagName().equals("Plant")) {
          // builds all plant automata
          constructSimpleComponent(aut, ComponentKind.PLANT, uri);

        } else if (aut.getTagName().equals("Implements")) {
          final Element interfaceRef =
              (Element) aut.getElementsByTagName("*").item(0);
          if (interfaceRef != null) {
            final String interfaceNm = interfaceRef.getAttribute("name");
            final String interfaceLocation = interfaceMap.get(interfaceNm);

            constructSimpleComponent(interfaceNm, interfaceLocation,
                ComponentKind.SPEC, uri);
          }

        } else if (aut.getTagName().equals("Uses")) {
          constructModuleInstance(aut);
        }
        clearStructures();
      }

      module = constructModule(subsystem);
      // stores the module
      mModules.put(module.getName(), module);

      mComponents.clear();
      mEvents.clear();
      final ProxyMarshaller<ModuleProxy> marshaller =
          mDocumentManager.findProxyMarshaller(ModuleProxy.class);
      final String ext = marshaller.getDefaultExtension();
      final String filename = subsystem.getAttribute("name");
      if (!mOutputDir.exists()) {
        mOutputDir.mkdirs();
      }
      final File file = new File(mOutputDir, filename + ext);
      mDocumentManager.saveAs(module, file);

    }
    return module;
  }

  /**
   * Converts a node list read from the DOM into an array list.
   *
   * @param nodeList
   * @return
   */
  private ArrayList<Element> convertNodeList(final NodeList nodeList)
  {
    // extracts each subsystem element and puts them into an array list
    final ArrayList<Element> list = new ArrayList<Element>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Element subsystem = (Element) nodeList.item(i);
      list.add(subsystem);
    }
    return list;
  }

  /*
   * This method adds the accepting proposition to the list of events.
   */
  private void addAcceptingProp()
  {
    final IdentifierProxy identifier =
        mFactory
            .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    final EventDeclProxy accepting =
        mFactory.createEventDeclProxy(identifier, EventKind.PROPOSITION, true,
            ScopeKind.OPTIONAL_PARAMETER, null, null, null);
    mEvents.put(EventDeclProxy.DEFAULT_MARKING_NAME, accepting);

  }

  /*
   * This method checks if an identifier (i.e., names of automata, events,
   * states) is in the correct format for waters (as DESpot allows characters
   * which waters does not). If it is not, the identifier name is translated to
   * something accepted by waters and returned.
   */
  private String formatIdentifier(String name)
  {
    int index = name.indexOf("-");
    if (index != -1) {
      // replaces - with {-}
      final String newName = "{" + name + "}";
      name = newName;
      // index = name.indexOf("-", index + 2);
    }
    index = name.indexOf(".");
    while (index != -1) {
      // replaces . with :
      final String newName =
          name.substring(0, index) + ":"
              + name.substring(index + 1, name.length());
      name = newName;
      index = name.indexOf(".", index + 1);
    }
    return name;
  }

  /**
   * This method creates an instance of a module. One call to this method
   * creates all the required instance for the current module.
   *
   * @param uses
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws URISyntaxException
   */
  private void constructModuleInstance(final Element uses)
  {
    final NodeList interfaceList = uses.getElementsByTagName("*");
    for (int i = 0; i < interfaceList.getLength(); i++) {
      final Element interfaceRef = (Element) interfaceList.item(i);
      final String moduleName = interfaceRef.getAttribute("provider");

      // gets the module to create an instance of
      final ModuleProxy module = mModules.get(moduleName);

      final List<ParameterBindingProxy> bindings =
          new ArrayList<ParameterBindingProxy>();

      // creates the parameter bindings for each event in the module
      final List<EventDeclProxy> eventList = module.getEventDeclList();

      for (int j = 0; j < eventList.size(); j++) {
        final EventDeclProxy event = eventList.get(j);
        final String eventName = event.getName();
        final ExpressionProxy identifier =
            mFactory.createSimpleIdentifierProxy(eventName);

        // No binding parameter is created if the event is local
        if (!event.getScope().equals(ScopeKind.LOCAL)) {

          // if the parameter is not already used by the module
          // referencing it, add it to the list of events for the module
          // referencing it
          if (!mEvents.containsKey(eventName)) {
            final EventDeclProxy eventCopy = (EventDeclProxy) event.clone();
            mEvents.put(eventName, eventCopy);
          }
          bindings.add(mFactory.createParameterBindingProxy(eventName,
              identifier));
        }
      }
      final SimpleIdentifierProxy identifier =
          mFactory.createSimpleIdentifierProxy(moduleName);
      mComponents.add(mFactory.createInstanceProxy(identifier, moduleName,
          bindings));
    }

  }

  // #########################################################################
  // # Main
  public static void main(final String[] args)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException, JAXBException, WatersMarshalException,
      WatersUnmarshalException
  {
    final File file = new File("/home/rmf18/Desktop/Data/testHISC.desp");
    final URI path = file.toURI();
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final OperatorTable opTable = CompilerOperatorTable.getInstance();
    final ProxyMarshaller<ModuleProxy> marshaller =
        new JAXBModuleMarshaller(factory, opTable);
    final DocumentManager docManager = new DocumentManager();
    docManager.registerMarshaller(marshaller);
    final File outputFile = new File("/home/rmf18/Desktop/DESpot");
    final DESpotImporter pdf =
        new DESpotImporter(outputFile, factory, docManager);
    pdf.unmarshalCopying(path);

  }

  // #########################################################################
  // # Initialisation
  /**
   * Initialises the data structures used to store the states, events and
   * transitions in the construction of a graph.
   */
  private void clearStructures()
  {
    mStates.clear();
    mEventIDs.clear();
    mTransitions.clear();
    mNodes.clear();
    mEdges.clear();
  }

  // #########################################################################
  // # Element Conversion

  /**
   * Checks if an event already exists. If the event hasn't been used already an
   * EventDeclProxy is created and stored.
   */
  private void constructEventDecl(final Element event)
  {
    final String eventName = formatIdentifier(event.getAttribute("nm"));
    if (!mEvents.containsKey(eventName)) {
      final IdentifierProxy identifier =
          mFactory.createSimpleIdentifierProxy(eventName);
      final String eventKind = event.getAttribute("ctrl");
      final String eventType = event.getAttribute("type");
      EventDeclProxy eventDecl = null;
      // the event is controllable
      if (eventKind.equals("1")) {
        if (eventType.equals("r")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier, EventKind.CONTROLLABLE,
                  true, ScopeKind.REQUIRED_PARAMETER, null, null,
                  HISCAttributes.ATTRIBUTES_REQUEST);
        } else if (eventType.equals("a")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier, EventKind.CONTROLLABLE,
                  true, ScopeKind.REQUIRED_PARAMETER, null, null,
                  HISCAttributes.ATTRIBUTES_ANSWER);
        } else if (eventType.equals("d")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier, EventKind.CONTROLLABLE,
                  true, ScopeKind.LOCAL, null, null, null);
        } else if (eventType.equals("ld")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier, EventKind.CONTROLLABLE,
                  true, ScopeKind.REQUIRED_PARAMETER, null, null,
                  HISCAttributes.ATTRIBUTES_LOWDATA);
        }
      }
      // the event is uncontrollable
      else if (eventKind.equals("0")) {
        if (eventType.equals("r")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier,
                  EventKind.UNCONTROLLABLE, true, ScopeKind.REQUIRED_PARAMETER,
                  null, null, HISCAttributes.ATTRIBUTES_REQUEST);
        } else if (eventType.equals("a")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier,
                  EventKind.UNCONTROLLABLE, true, ScopeKind.REQUIRED_PARAMETER,
                  null, null, HISCAttributes.ATTRIBUTES_ANSWER);
        } else if (eventType.equals("d")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier,
                  EventKind.UNCONTROLLABLE, true, ScopeKind.LOCAL, null, null,
                  null);
        } else if (eventType.equals("ld")) {
          eventDecl =
              mFactory.createEventDeclProxy(identifier,
                  EventKind.UNCONTROLLABLE, true, ScopeKind.REQUIRED_PARAMETER,
                  null, null, HISCAttributes.ATTRIBUTES_LOWDATA);
        }
      }
      mEvents.put(eventName, eventDecl);
    }
  }

  /**
   * Constructs the SimpleComponent section for the module of a
   * <CODE>.wmod</CODE> file.
   */
  private void constructSimpleComponent(final Element automaton,
      final ComponentKind kind, final URI path)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException
  {
    final NodeList desList = automaton.getElementsByTagName("*");
    for (int i = 0; i < desList.getLength(); i++) {
      clearStructures();
      final Element des = (Element) desList.item(i);
      final String autName = des.getAttribute("name");
      final String autFile = des.getAttribute("location");
      final URI uri = new URI(autFile);

      final GraphProxy graph = constructGraph(path.resolve(uri));
      final IdentifierProxy identifier =
          mFactory.createSimpleIdentifierProxy(formatIdentifier(autName));

      mComponents.add(mFactory.createSimpleComponentProxy(identifier, kind,
          graph));
    }

  }

  /**
   * Constructs the SimpleComponent section for the module of a
   * <CODE>.wmod</CODE> file.
   */

  private void constructSimpleComponent(final String desName,
      final String desLocation, final ComponentKind kind, final URI path)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException
  {
    final URI uri = new URI(desLocation);

    final GraphProxy graph = constructGraph(path.resolve(uri));
    final IdentifierProxy identifier =
        mFactory.createSimpleIdentifierProxy(formatIdentifier(desName));

    mComponents.add(mFactory.createSimpleComponentProxy(identifier, kind,
        graph, HISCAttributes.ATTRIBUTES_INTERFACE));
  }

  /**
   * Constructs a ModuleProxy for a given subsystem.
   *
   * @param subsystem
   * @return
   */
  private ModuleProxy constructModule(final Element subsystem)
  {

    return mFactory.createModuleProxy(subsystem.getAttribute("name"), null, URI
        .create("testModule"), null, mEvents.values(), null, mComponents);
  }

  /**
   * Constructs the Graph section for the module of a <CODE>.wmod</CODE> file.
   * Converts transitions to edges, and converts states to nodes.
   *
   * @param uri
   *          URI indicating a <CODE>.des</CODE> file to parse.
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
      final SimpleNodeProxy node = convertState(stElmnt);
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
    final NodeList transitionList =
        definition.getElementsByTagName("Trans-Function");
    final Element transitions = (Element) transitionList.item(0);
    final NodeList transElmntLst =
        transitions.getElementsByTagName("Transitions");
    final Element transition = (Element) transElmntLst.item(0);
    final NodeList trList = transition.getElementsByTagName("Tr");
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
    final NodeList transLoopList = transitions.getElementsByTagName("Tr");
    final LabelBlockProxy blockedEvents = findBlockedEvents(transLoopList);

    return mFactory.createGraphProxy(true, blockedEvents, mNodes, mEdges);

  }

  /**
   * Determines which events are blocked in this automata. Events are blocked if
   * they do not appear on a transition or selfloop.
   *
   * @param transLoopList
   *          The list of Transitions and selfloops from the DOM.
   */
  private LabelBlockProxy findBlockedEvents(final NodeList transitions)
  {
    boolean found = false;
    final List<SimpleIdentifierProxy> blockedEventList =
        new ArrayList<SimpleIdentifierProxy>();
    for (final int eventID : mEventIDs.keySet()) {
      found = false;

      for (int j = 0; j < transitions.getLength(); j++) {
        final Element tr = (Element) transitions.item(j);
        if (Integer.parseInt(tr.getAttribute("eID")) == eventID) {
          found = true;
          break;
        }
      }
      if (!found) {
        blockedEventList.add(mFactory.createSimpleIdentifierProxy(mEventIDs
            .get(eventID)));
      }
    }
    if (blockedEventList.size() == 0) {
      return null;
    } else {
      return mFactory.createLabelBlockProxy(blockedEventList, null);
    }
  }

  /**
   * Maps the IDs for all the events in the despot file to their name. Creates
   * an EventDecl for this event if it didn't already exist.
   *
   * @param events
   *          The list of event elements from the DOM.
   */
  private void storeEvents(final NodeList events)
  {
    for (int i = 0; i < events.getLength(); i++) {
      final Element event = (Element) events.item(i);
      final int eventID = Integer.parseInt(event.getAttribute("id"));
      final String eventName = formatIdentifier(event.getAttribute("nm"));
      mEventIDs.put(eventID, eventName);
      constructEventDecl(event);
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
   *
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
   *
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
    final String eventName = mEventIDs.get(Integer.parseInt(eventID));
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
    // reads and stores the position of the label for the edge
    String xPosStr = tr.getAttribute("lx");
    String yPosStr = tr.getAttribute("ly");
    LabelGeometryProxy labelPos = null;
    if (!xPosStr.equals("") && !yPosStr.equals("")) {
      final double xPos = Double.parseDouble(xPosStr);
      final double yPos = Double.parseDouble(yPosStr);
      final Point2D labelPoint = new Point2D.Double(xPos, yPos);
      labelPos = mFactory.createLabelGeometryProxy(labelPoint);
    }
    final LabelBlockProxy transEvents =
        mFactory.createLabelBlockProxy(eventList, labelPos);

    // reads and stores the geometry (layout) data for the edge
    final List<Point2D> points = new ArrayList<Point2D>(0);
    final NodeList posList = tr.getElementsByTagName("Pos");
    final List<Element> geoList = convertNodeList(posList);
    // DESpot files list points on the edge in opposite order to waters
    Collections.reverse(geoList);
    SplineGeometryProxy edgeShape = null;
    if (geoList.size() > 0) {
      for (int i = 0; i < geoList.size(); i++) {
        final Element pos = (Element) geoList.get(i);
        final Point2D sourcePos =
            mNodes.get(srcIndex).getPointGeometry().getPoint();
        final Point2D targetPos =
            mNodes.get(targetIndex).getPointGeometry().getPoint();
        xPosStr = pos.getAttribute("x");
        yPosStr = pos.getAttribute("y");
        double xPos;
        double yPos;
        if (!xPosStr.equals("") && !yPosStr.equals("")) {
          xPos = Double.parseDouble(xPosStr);
          yPos = Double.parseDouble(yPosStr);
          final Point2D point = new Point2D.Double(xPos, yPos);
          // if the distance between the new <Pos> and the source or target
          // state is less than 12 then that is within the area of the node
          // itself and the control point is unnecessary
          final double nodeAreaSq = 12 * 12;
          if ((point.distanceSq(sourcePos) > nodeAreaSq)
              && (point.distanceSq(targetPos) > nodeAreaSq)) {
            points.add(point);
          }
        }
      }
      edgeShape =
          mFactory.createSplineGeometryProxy(points, SplineKind.INTERPOLATING);
    }

    return mFactory.createEdgeProxy(mNodes.get(srcIndex), mNodes
        .get(targetIndex), transEvents, null, edgeShape, null, null);

  }

  /**
   * Converts a state from the despot file into a node for the waters file.
   *
   * @param state
   *          The state to be converted.
   */
  private SimpleNodeProxy convertState(final Element state)
  {
    final String marked = "1";
    final String stateName = formatIdentifier(state.getAttribute("nm"));
    // reads and stores the geometry (layout) data for the node
    String xPosStr = state.getAttribute("sx");
    String yPosStr = state.getAttribute("sy");
    PointGeometryProxy nodePos = null;
    double xPos;
    double yPos;
    if (!xPosStr.equals("") && !yPosStr.equals("")) {
      xPos = Double.parseDouble(xPosStr);
      yPos = Double.parseDouble(yPosStr);
      final Point2D nodePoint = new Point2D.Double(xPos, yPos);
      nodePos = mFactory.createPointGeometryProxy(nodePoint);
    }
    xPosStr = state.getAttribute("lx");
    yPosStr = state.getAttribute("ly");
    LabelGeometryProxy labelPos = null;
    if (!xPosStr.equals("") && !yPosStr.equals("")) {
      xPos = Double.parseDouble(xPosStr);
      yPos = Double.parseDouble(yPosStr);
      final Point2D labelPoint = new Point2D.Double(xPos, yPos);
      labelPos = mFactory.createLabelGeometryProxy(labelPoint);
    }

    if (state.getTagName().equals("St")) {
      // checks if the state is marked (i.e. accepting)
      if (state.getAttribute("mk").equals(marked)) {
        return markState(state, false, nodePos, labelPos);
      } else {
        return mFactory.createSimpleNodeProxy(stateName, null, false, nodePos,
            null, labelPos);
      }
    }
    // the state needs to be set as the initial state.
    else {
      // checks if the state is marked (i.e. accepting)
      if (state.getAttribute("mk").equals(marked)) {
        return markState(state, true, nodePos, labelPos);
      }
      return mFactory.createSimpleNodeProxy(stateName, null, true, nodePos,
          null, labelPos);
    }

  }

  /**
   * Marks a state as accepting.
   *
   * @param state
   *          The marked state.
   * @param initial
   *          States whether this is the initial state.
   * @param labelPos
   * @param nodePos
   */
  private SimpleNodeProxy markState(final Element state, final Boolean initial,
      final PointGeometryProxy nodePos, final LabelGeometryProxy labelPos)
  {
    final String stateName = formatIdentifier(state.getAttribute("nm"));
    // holds the :accepting constant
    final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;

    final List<SimpleIdentifierProxy> markList =
        new ArrayList<SimpleIdentifierProxy>(1);
    markList.add(mFactory.createSimpleIdentifierProxy(accepting));
    final PlainEventListProxy accept =
        mFactory.createPlainEventListProxy(markList);
    if (!initial) {
      return mFactory.createSimpleNodeProxy(stateName, accept, false, nodePos,
          null, labelPos);
    } else {
      return mFactory.createSimpleNodeProxy(stateName, accept, true, nodePos,
          null, labelPos);
    }
  }


  // #########################################################################
  // # Inner Class LevelComparator
  /**
   * A class used to compare the values of the levels of subsystems. Low level
   * subsystems are ordered before high level subsystems. This class is used as
   * a comparator for the sort method.
   */
  private static class LevelComparator implements Comparator<Element>
  {
    public int compare(final Element e1, final Element e2)
    {
      final String LEVEL = "level";
      final Integer e1Level = Integer.parseInt(e1.getAttribute(LEVEL));
      final Integer e2Level = Integer.parseInt(e2.getAttribute(LEVEL));

      return (e1Level > e2Level ? -1 : (e1Level == e2Level ? 0 : 1));
    }

  }


  // #########################################################################
  // # Inner Class IdPair
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

  // #########################################################################
  // # Data Members
  /**
   * Stores the ID number the despot file uses to reference a state and the
   * location of the state in the 'nodes' list.
   */
  private final Map<Integer,Integer> mStates = new HashMap<Integer,Integer>();
  /**
   * Stores the ID number the despot file uses to reference an event and the
   * name of the event.
   */
  private final Map<Integer,String> mEventIDs = new HashMap<Integer,String>();
  // The above two hash maps are needed for finding the correct source/target
  // state of a transition and correct event name.

  /**
   * Maps the source state and target state together (as an IdPair) for a
   * transition to the index number where this edge exists in the list 'edges'.
   * This is used to lookup quickly whether an edge already exists.
   */
  private final Map<IdPair,Integer> mTransitions =
      new HashMap<IdPair,Integer>();

  /**
   * This list stores all the nodes (states) for the current automaton.
   */
  private final List<SimpleNodeProxy> mNodes = new ArrayList<SimpleNodeProxy>();
  /**
   * This list stores all the edges (transitions) for the current automaton.
   */
  private final List<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
  /**
   * List that stores all the components for a module, such as the automata
   * (SimpleComponent's) or instances of other modules.
   */
  private final List<Proxy> mComponents = new ArrayList<Proxy>();
  /**
   * Maps the name of an event to the EventDecl for that event.
   */
  private final Map<String,EventDeclProxy> mEvents =
      new TreeMap<String,EventDeclProxy>();

  /**
   * Maps the name of a module to its ModuleProxy.
   */
  private final Map<String,ModuleProxy> mModules =
      new HashMap<String,ModuleProxy>();
  /**
   * The factory used to build up the modules for the <CODE>.wmod</CODE> files
   * we are converting into.
   */
  private final ModuleProxyFactory mFactory;
  // # Data Members
  private File mOutputDir;

  private DocumentManager mDocumentManager;

  private static final String DESPOTEXT = ".desp";
  private static final Collection<String> EXTENSIONS =
      Collections.singletonList(DESPOTEXT);
  private static final FileFilter DESPOTFILTER =
      new StandardExtensionFileFilter(DESPOTEXT, "DESpot project files [*"
          + DESPOTEXT + "]");
  private static final Collection<FileFilter> FILTERS =
      Collections.singletonList(DESPOTFILTER);
}
