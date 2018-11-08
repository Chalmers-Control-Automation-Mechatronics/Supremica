//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.external.despot;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.analysis.hisc.HISCAttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.subject.module.GeometryTools;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;
import net.sourceforge.waters.xsd.module.SplineKind;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * A tool to import hierarchical models created by DESpot into the IDE.
 * This importer uses Waters module instantiation to preserve the hierarchical
 * structure of the DESpot models. Annotations are used to record HISC
 * interface and event types.
 *
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
  // # net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller
  @Override
  public File getOutputDirectory()
  {
    return mOutputDir;
  }

  @Override
  public void setOutputDirectory(final File outputdir)
  {
    mOutputDir = outputdir;
  }

  @Override
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
  @Override
  public ModuleProxy unmarshal(final URI uri) throws IOException,
      WatersUnmarshalException
  {
    try {
      return unmarshalCopying(uri);
    } catch (final WatersMarshalException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  @Override
  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  @Override
  public String getDefaultExtension()
  {
    return DESPOTEXT;
  }

  @Override
  public Collection<String> getSupportedExtensions()
  {
    return EXTENSIONS;
  }

  @Override
  public Collection<FileFilter> getSupportedFileFilters()
  {
    return FILTERS;
  }

  @Override
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  @Override
  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
  }

  // #########################################################################
  // # Algorithm
  private ModuleProxy convertDESpotHierarchy(final URI uri)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException, WatersMarshalException, WatersUnmarshalException
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
          (Element) interfaceDES.getElementsByTagName("Des").item(0);
      final String name = interfaceDES.getAttribute("name");
      if (des == null) {
        interfaceMap.put(name, "");
      } else {
        final String location = des.getAttribute("location");
        final URI fileURI = uri.resolve(location);
        final File file = new File(fileURI);
        if (file.exists()) {
          interfaceMap.put(name, location);
        } else {
          throw new FileNotFoundException("The interface file " + file
              + " could not be located.");
        }
      }
    }
    final NodeList subsystemList = project.getElementsByTagName("Subsystem");
    // extracts each subsystem element and puts them into an array list
    final ArrayList<Element> subsystems = convertNodeList(subsystemList);

    // sorts the subsystems in order of their level (lowest to highest)
    Collections.sort(subsystems, new LevelComparator());

    for (final Element subsystem : subsystems) {
      // adds the accepting proposition to the events list
      addAcceptingProp();

      final NodeList sections = subsystem.getElementsByTagName("*");
      for (int i = 0; i < sections.getLength(); i++) {
        final Element section = (Element) sections.item(i);
        if (section.getTagName().equals("Supervisor")) {
          // builds all specification automata
          constructSimpleComponent(section, ComponentKind.SPEC, uri);
        } else if (section.getTagName().equals("Plant")) {
          // builds all plant automata
          constructSimpleComponent(section, ComponentKind.PLANT, uri);
        } else if (section.getTagName().equals("Implements")) {
          final NodeList list = section.getElementsByTagName("*");
          for (int j = 0; j < list.getLength(); j++) {
            clearGraphStructures();
            final Element interfaceRef = (Element) list.item(i);
            final String interfaceNm = interfaceRef.getAttribute("name");
            final String interfaceLocation = interfaceMap.get(interfaceNm);
            if (interfaceLocation == null) {
              throw new WatersUnmarshalException
                ("The interface " + interfaceNm + " does not exist.");
            } else if (!interfaceLocation.equals("")) {
              constructSimpleComponent(interfaceNm, interfaceLocation,
                                       ComponentKind.SPEC, uri);
            }
          }
        } else if (section.getTagName().equals("Uses")) {
          constructModuleInstance(section);
        }
        clearGraphStructures();
      }

      module = constructModule(subsystem);
      if (module != null) {
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

    }
    return module;

  }

  /**
   * Converts a node list read from the DOM into an array list.
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

  /**
   * This method adds the accepting proposition to the list of events.
   */
  private void addAcceptingProp()
  {
    final IdentifierProxy identifier =
        mFactory
            .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
    final EventDeclProxy accepting =
        mFactory.createEventDeclProxy(identifier, EventKind.PROPOSITION, true,
                                      ScopeKind.OPTIONAL_PARAMETER, null, null,
                                      null);
    mEvents.put(EventDeclProxy.DEFAULT_MARKING_NAME, accepting);

  }

  /**
   * This method checks if an identifier (i.e., names of automata, events,
   * states) is in the correct format for Waters (as DESpot allows characters
   * which Waters does not). If it is not, the identifier name is translated to
   * something accepted by Waters and returned.
   */
  private String formatIdentifier(final String name)
  {
    // TODO Also replace '.'.
    // TODO Check for clashes.
    final String newName = name.replaceAll("-", ":");
    return newName;
  }

  /**
   * This method creates an instance of a module. One call to this method
   * creates all the required instance for the current module.
   */
  private void constructModuleInstance(final Element uses)
      throws FileNotFoundException, WatersUnmarshalException
  {
    final NodeList interfaceList = uses.getElementsByTagName("*");
    for (int i = 0; i < interfaceList.getLength(); i++) {
      final Element interfaceRef = (Element) interfaceList.item(i);
      final String moduleName = interfaceRef.getAttribute("provider");
      // Note, an instance has two names. The *identifier* identifies the
      // instance within the calling module, while the *module name*
      // specifies the file name of the submodule to be instantiated.
      // They can be different, e.g., in small factory the 'machine'
      // module is instantiated as 'machine1' and 'machine2'. Only the
      // *identifier* must obey syntax restrictions.

      // gets the module to create an instance of
      final ModuleProxy module = mModules.get(moduleName);
      if (module == null) {
        throw new WatersUnmarshalException("The subsystem " + moduleName
            + " could not be found.");
      }

      final List<ParameterBindingProxy> bindings =
          new ArrayList<ParameterBindingProxy>();

      // creates the parameter bindings for each event in the module
      final List<EventDeclProxy> eventList = module.getEventDeclList();

      for (int j = 0; j < eventList.size(); j++) {
        final EventDeclProxy event = eventList.get(j);
        final String eventName = event.getName();
        // No binding parameter is created if the event is local
        if (!event.getScope().equals(ScopeKind.LOCAL)) {
          // if the parameter is not already used by the module
          // referencing it, add it to the list of events for the module
          // referencing it
          if (!mEvents.containsKey(eventName)) {
            final IdentifierProxy identifier =
              mFactory.createSimpleIdentifierProxy(eventName);
            final EventDeclProxy newEvent =
                mFactory.createEventDeclProxy(identifier, event.getKind(),
                                              true, ScopeKind.LOCAL, null,
                                              null, event.getAttributes());
            mEvents.put(eventName, newEvent);
          }
          final IdentifierProxy identifier =
            mFactory.createSimpleIdentifierProxy(eventName);
          bindings.add(mFactory.createParameterBindingProxy(eventName,
                                                            identifier));
        }
      }
      final SimpleIdentifierProxy identifier =
          mFactory.createSimpleIdentifierProxy(formatIdentifier(moduleName));
      mComponents.add(mFactory.createInstanceProxy(identifier, moduleName,
                                                   bindings));
    }

  }

  // #########################################################################
  // # Initialisation
  /**
   * Initialises the data structures used to store the states, events and
   * transitions in the construction of a graph.
   */
  private void clearGraphStructures()
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
  private void constructEventDecl(final Element event,
                                  final boolean implementation)
  {
    final String eventName = formatIdentifier(event.getAttribute("nm"));
    if (!mEvents.containsKey(eventName)) {
      final IdentifierProxy identifier =
          mFactory.createSimpleIdentifierProxy(eventName);
      final String eventKind = event.getAttribute("ctrl");
      final String eventType = event.getAttribute("type");
      EventDeclProxy eventDecl = null;

      ScopeKind scope;
      if (implementation) {
        scope = ScopeKind.REQUIRED_PARAMETER;
      } else {
        scope = ScopeKind.LOCAL;
      }

      EventKind controllability;
      if (eventKind.equals("1")) {
        controllability = EventKind.CONTROLLABLE;
      } else {
        controllability = EventKind.UNCONTROLLABLE;
      }

      Map<String,String> attributes = null;
      if (eventType.equals("a")) {
        attributes = HISCAttributeFactory.ATTRIBUTES_ANSWER;
      } else if (eventType.equals("r")) {
        attributes = HISCAttributeFactory.ATTRIBUTES_REQUEST;
      } else if (eventType.equals("ld")) {
        attributes = HISCAttributeFactory.ATTRIBUTES_LOWDATA;
      }
      eventDecl =
          mFactory.createEventDeclProxy(identifier, controllability, true,
                                        scope, null, null, attributes);

      mEvents.put(eventName, eventDecl);
    }
  }

  /**
   * Constructs the SimpleComponent section for the module of a
   * <CODE>.wmod</CODE> file.
   *
   * @throws WatersUnmarshalException
   */
  private void constructSimpleComponent(final Element automaton,
                                        final ComponentKind kind, final URI path)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException, WatersUnmarshalException
  {
    final NodeList desList = automaton.getElementsByTagName("*");
    for (int i = 0; i < desList.getLength(); i++) {
      clearGraphStructures();
      final Element des = (Element) desList.item(i);
      // final String autName = formatIdentifier(des.getAttribute("name"));
      final String location = des.getAttribute("location");
      final URI uri = new URI(null, null, location, null);
      final Element root = openDESFile(path.resolve(uri));
      final Element definition =
          (Element) root.getElementsByTagName("Definition").item(0);
      final NodeList allHeaders = definition.getElementsByTagName("Header");
      final Element header = (Element) allHeaders.item(0);
      final String autName = formatIdentifier(header.getAttribute("name"));
      final GraphProxy graph = constructGraph(location, root, false);
      if (graph != null) {
        final IdentifierProxy identifier =
            mFactory.createSimpleIdentifierProxy(autName);
        mComponents.add(mFactory.createSimpleComponentProxy(identifier, kind,
                                                            graph));
      } else {
        continue;
      }
    }

  }

  /**
   * Constructs the SimpleComponent section for the module of a
   * <CODE>.wmod</CODE> file.
   *
   * @throws WatersUnmarshalException
   */
  private void constructSimpleComponent(final String desName,
                                        final String desLocation,
                                        final ComponentKind kind, final URI path)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException, WatersUnmarshalException
  {
    final URI uri = new URI(desLocation);
    final Element root = openDESFile(path.resolve(uri));
    final Element definition =
        (Element) root.getElementsByTagName("Definition").item(0);
    final NodeList allHeaders = definition.getElementsByTagName("Header");
    final Element header = (Element) allHeaders.item(0);
    final String newName = formatIdentifier(header.getAttribute("name"));
    final GraphProxy graph = constructGraph(desLocation, root, true);
    if (graph != null) {
      final IdentifierProxy identifier =
          mFactory.createSimpleIdentifierProxy(newName);

      mComponents.add(mFactory
          .createSimpleComponentProxy(identifier, kind, graph,
                                      HISCAttributeFactory.ATTRIBUTES_INTERFACE));
    }
  }

  private Element openDESFile(final URI uri)
      throws ParserConfigurationException, SAXException, IOException
  {
    final File file = new File(uri);
    final DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    if (!file.exists()) {
      throw new FileNotFoundException("The DES file " + file
          + " could not be located.");
    }
    final Document doc = builder.parse(file);
    // gets the root element
    return doc.getDocumentElement();
  }

  /**
   * Constructs a ModuleProxy for a given subsystem.
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
   * @param des
   *          The root element of the .des file that contains the automaton.
   * @throws WatersUnmarshalException
   */
  private GraphProxy constructGraph(final String autfilename,
                                    final Element des,
                                    final boolean implementation)
      throws ParserConfigurationException, SAXException, IOException,
      WatersUnmarshalException
  {

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
    storeEvents(evElmntLst, implementation);

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
      final EdgeProxy edge = convertTransition(autfilename, trElmnt, exists);
      if (!exists) {
        mEdges.add(edge);
        final int index = mEdges.indexOf(edge);
        // stores this transition
        storeTransition(trElmnt, index);
      } else {
        // can find where this edge exists in the 'edges' list by using the hash
        // map which references it using the source and target IDs
        final int edgeIndex = mTransitions.get(srcTargIds);
        mEdges.set(edgeIndex, edge);
      }

    }

    // gets the blocked events for this automata
    final NodeList transLoopList = transitions.getElementsByTagName("*");
    final LabelBlockProxy blockedEvents =
        findBlockedEvents(transLoopList, stElmntLst);

    return mFactory.createGraphProxy(true, blockedEvents, mNodes, mEdges);

  }

  /**
   * Determines which events are blocked in this automata. Events are blocked if
   * they do not appear on a transition or selfloop.
   * @param transitions
   *          The list of Transitions and self-loops from the DOM.
   */
  private LabelBlockProxy findBlockedEvents(final NodeList transitions,
                                            final NodeList states)
  {
    boolean found = false;
    final List<SimpleIdentifierProxy> blockedEventList =
        new ArrayList<SimpleIdentifierProxy>();

    // checks if the default marking proposition is used
    for (int i = 0; i < states.getLength(); i++) {
      final Element state = (Element) states.item(i);
      if (state.getAttribute("mk").equals("1")) {
        found = true;
        break;
      }
    }
    if (!found) {
      blockedEventList.add(mFactory
          .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME));
    }

    // checks if each event is used in this automaton
    found = false;
    for (final int eventID : mEventIDs.keySet()) {
      found = false;
      for (int j = 0; j < transitions.getLength(); j++) {
        final Element tr = (Element) transitions.item(j);
        if (tr.getAttribute("eID") != "") {
          if (Integer.parseInt(tr.getAttribute("eID")) == eventID) {
            found = true;
            break;
          }
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
  private void storeEvents(final NodeList events, final boolean implementation)
  {
    for (int i = 0; i < events.getLength(); i++) {
      final Element event = (Element) events.item(i);
      final int eventID = Integer.parseInt(event.getAttribute("id"));
      final String eventName = formatIdentifier(event.getAttribute("nm"));
      mEventIDs.put(eventID, eventName);
      constructEventDecl(event, implementation);
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
  private EdgeProxy convertTransition(final String autfilename,
                                      final Element tr, final boolean exists)
      throws WatersUnmarshalException
  {
    // gets the source and target states ID numbers
    final int srcID = Integer.parseInt(tr.getAttribute("fID"));
    final int targetID = Integer.parseInt(tr.getAttribute("tID"));

    // assigns the correct name to the edge
    final String eventID = tr.getAttribute("eID");
    final String eventName = mEventIDs.get(Integer.parseInt(eventID));
    final List<SimpleIdentifierProxy> eventList =
        new ArrayList<SimpleIdentifierProxy>();

    // gets the index number of the source and target states in the list
    final Integer srcIndex = mStates.get(srcID);
    if (srcIndex == null) {
      throw new WatersUnmarshalException("The source state ID " + srcID
          + " does not exist on the transition with target state ID "
          + targetID + " and event ID " + eventID + " in the file "
          + autfilename + ".");
    }
    final Integer targetIndex = mStates.get(targetID);
    if (targetIndex == null) {
      throw new WatersUnmarshalException("The target state ID " + targetID
          + " does not exist on the transition with source state ID " + srcID
          + " and event ID " + eventID + " in the file " + autfilename + ".");
    }

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
          mEdges.get(edgeIndex).getLabelBlock().getEventIdentifierList();

      // adds each event that already exists to the new list (since these
      // objects are immutable and can't be updated)
      for (int i = 0; i < existingEvents.size(); i++) {
        final SimpleIdentifierProxy event =
            (SimpleIdentifierProxy) existingEvents.get(i);
        eventList.add(mFactory.createSimpleIdentifierProxy(event.getName()));
      }
    }

    eventList.add(mFactory.createSimpleIdentifierProxy(eventName));

    // reads and stores the geometry (layout) data for the edge
    final List<Point2D> points = new ArrayList<Point2D>(0);
    final NodeList posList = tr.getElementsByTagName("Pos");
    final List<Element> geoList = convertNodeList(posList);
    // remove first and last point because they are within the range of the
    // source/target nodes
    int numCtrlPoints = geoList.size();
    if (numCtrlPoints >= 2) {
      geoList.remove(numCtrlPoints - 1);
      geoList.remove(0);
      // DESpot files list points on the edge in opposite order to waters
      Collections.reverse(geoList);
    }
    SplineGeometryProxy edgeShape = null;
    numCtrlPoints = geoList.size();
    if (numCtrlPoints > 0) {
      for (int i = 0; i < numCtrlPoints; i++) {
        final Element pos = geoList.get(i);

        final String xPosStr = pos.getAttribute("x");
        final String yPosStr = pos.getAttribute("y");
        double xPos;
        double yPos;
        if (!xPosStr.equals("") && !yPosStr.equals("")) {
          xPos = Double.parseDouble(xPosStr);
          yPos = Double.parseDouble(yPosStr);
          final Point2D point = new Point2D.Double(xPos, yPos);
          points.add(point);

        }
      }
      edgeShape =
          mFactory.createSplineGeometryProxy(points, SplineKind.INTERPOLATING);
    }
    // handles a self loop edge
    else if ((srcID == targetID) && (tr.getAttribute("ang") != "")) {
      final double ang = Double.parseDouble(tr.getAttribute("ang"));
      // Convert angle to radians and add 45 degrees
      final double radang = Math.toRadians(ang + 45.0);
      // Get scale factor for Waters selfloop of size 48
      final double scale = 48.0 * GeometryTools.SELFLOOP_DISTANCE_UNIT;
      // Compute relative coordinates of the control point
      final double dx = scale * Math.cos(radang);
      final double dy = scale * Math.sin(radang);
      // Now add these to the position of the node ...
      final Point2D srcPos = mNodes.get(srcIndex).getPointGeometry().getPoint();
      final Point2D ctrlPoint =
          new Point2D.Double(srcPos.getX() + dx, srcPos.getY() + dy);
      points.add(ctrlPoint);
      edgeShape =
          mFactory.createSplineGeometryProxy(points, SplineKind.INTERPOLATING);

    }

    // reads and stores the position of the label for the edge
    final String xPosStr = tr.getAttribute("lx");
    final String yPosStr = tr.getAttribute("ly");
    LabelGeometryProxy labelPos = null;
    if (!xPosStr.equals("") && !yPosStr.equals("")) {
      final double lblXPos = Double.parseDouble(xPosStr);
      final double lblYPos = Double.parseDouble(yPosStr);
      final Point2D abslblPoint = new Point2D.Double(lblXPos, lblYPos);

      // DESpot files give an absolute position for the label, waters require a
      // position relative to the edge
      Point2D relLblPoint = null;
      double centreX = 0;
      double centreY = 0;
      if (numCtrlPoints == 0) {
        final Point2D srcPos =
            mNodes.get(srcIndex).getPointGeometry().getPoint();
        final Point2D targPos =
            mNodes.get(targetIndex).getPointGeometry().getPoint();
        if (srcID != targetID) {
          centreX = ((srcPos.getX() - targPos.getX()) / 2) + targPos.getX();
          centreY = ((srcPos.getY() - targPos.getY()) / 2) + targPos.getY();
        }

      } else if ((numCtrlPoints % 2) != 0) {
        final Element centrePos = geoList.get(numCtrlPoints / 2);
        centreX = Double.parseDouble(centrePos.getAttribute("x"));
        centreY = Double.parseDouble(centrePos.getAttribute("y"));
      } else {
        final Element centrePos1 = geoList.get(numCtrlPoints / 2);
        final Element centrePos2 = geoList.get((numCtrlPoints / 2) - 1);
        double cntrPos1 = Double.parseDouble(centrePos1.getAttribute("x"));
        double cntrPos2 = Double.parseDouble(centrePos2.getAttribute("x"));
        centreX = ((cntrPos1 - cntrPos2) / 2) + cntrPos2;
        cntrPos1 = Double.parseDouble(centrePos1.getAttribute("y"));
        cntrPos2 = Double.parseDouble(centrePos2.getAttribute("y"));
        centreY = ((cntrPos1 - cntrPos2) / 2) + cntrPos2;
      }
      if (srcID != targetID) {
        relLblPoint =
            new Point2D.Double(abslblPoint.getX() - centreX, abslblPoint.getY()
                - centreY);
        labelPos = mFactory.createLabelGeometryProxy(relLblPoint);
      }
    }
    final LabelBlockProxy transEvents =
        mFactory.createLabelBlockProxy(eventList, labelPos);

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
    final boolean initial = !state.getTagName().equals("St");
    final boolean marked = state.getAttribute("mk").equals(ONE);
    return createNode(stateName, initial, marked, nodePos, labelPos);
  }

  private SimpleNodeProxy createNode(final String stateName,
                                     final boolean initial,
                                     final boolean marked,
                                     final PointGeometryProxy nodePos,
                                     final LabelGeometryProxy labelPos)
  {
    final PlainEventListProxy props;
    if (marked) {
      final String acceptingName = EventDeclProxy.DEFAULT_MARKING_NAME;
      final SimpleIdentifierProxy acceptingIdent =
        mFactory.createSimpleIdentifierProxy(acceptingName);
      final List<SimpleIdentifierProxy> identList =
        Collections.singletonList(acceptingIdent);
      props = mFactory.createPlainEventListProxy(identList);
    } else {
      props = null;
    }
    return mFactory.createSimpleNodeProxy(stateName, props, null,
                                          initial, nodePos, null, labelPos);
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
    @Override
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

    @Override
    public int hashCode()
    {
      return mSource + 5 * mTarget;
    }

    @Override
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
   * transition to the index number where this edge exists in the list 'mEdges'.
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

  private File mOutputDir;

  private DocumentManager mDocumentManager;

  // #########################################################################
  // # Class Constants
  private static final String DESPOTEXT = ".desp";
  private static final Collection<String> EXTENSIONS =
      Collections.singletonList(DESPOTEXT);
  private static final FileFilter DESPOTFILTER =
      new StandardExtensionFileFilter("DESpot project files [*"
          + DESPOTEXT + "]", DESPOTEXT);
  private static final Collection<FileFilter> FILTERS =
      Collections.singletonList(DESPOTFILTER);

  private static final String ONE = "1";

}
