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

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.sourceforge.waters.model.module.SimpleComponentProxy;
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

  //#########################################################################
  //# Constructors
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

  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller
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


  //#########################################################################
  //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
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


  //#########################################################################
  //# Algorithm
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
    final Map<String,Element> interfaceMap = new HashMap<>();
    final NodeList interfaces = project.getElementsByTagName("Interface");
    for (int j = 0; j < interfaces.getLength(); j++) {
      final Element iface = (Element) interfaces.item(j);
      final String name = iface.getAttribute("name");
      final Element des = (Element) iface.getElementsByTagName("Des").item(0);
      interfaceMap.put(name, des);
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
        final String tagName = section.getTagName();
        if (tagName.equals("Supervisor")) {
          constructSimpleComponents(section, ComponentKind.SPEC, uri);
        } else if (tagName.equals("Plant")) {
          constructSimpleComponents(section, ComponentKind.PLANT, uri);
        } else if (tagName.equals("Template")) {
          constructTemplateComponents(section, uri);
        } else if (tagName.equals("Implements")) {
          final NodeList list = section.getElementsByTagName("InterfaceRef");
          for (int j = 0; j < list.getLength(); j++) {
            final Element interfaceRef = (Element) list.item(i);
            final String name = interfaceRef.getAttribute("name");
            final Element des = interfaceMap.get(name);
            if (des != null) {
              final DESpotParameters params = new DESpotNoParameters();
              constructSimpleComponent(des, params,
                                       ComponentKind.SPEC,
                                       HISCAttributeFactory.ATTRIBUTES_INTERFACE,
                                       true,
                                       uri);
            } else if (!interfaceMap.containsKey(name)) {
              throw new WatersUnmarshalException
                ("The interface " + name + " does not exist.");
            }
          }
        } else if (section.getTagName().equals("Uses")) {
          constructModuleInstance(section);
        }
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
      final IdentifierProxy identifier =
        mComponentIdentification.createIdentifier(moduleName);
      mComponents.add(mFactory.createInstanceProxy(identifier, moduleName,
                                                   bindings));
    }
  }


  //#########################################################################
  //# Initialisation
  /**
   * Initialises the data structures used to store the states, events and
   * transitions in the construction of a graph.
   */
  private void startNewGraph(final int numStates, final int numTransitions)
  {
    mStates = new TIntIntHashMap(numStates, 0.5f, -1, -1);
    mEventIDs.clear();
    mTransitions = new TObjectIntHashMap<>(numTransitions, 0.5f, -1);
    mNodes.clear();
    mEdges.clear();
  }


  //#########################################################################
  //# Element Conversion
  /**
   * Checks if an event already exists. If the event has not been used already
   * an {@link EventDeclProxy} is created and stored.
   * @param  event        The <CODE>&lt;Ev%gt;</CODE> element from the
   *                      <CODE>.des</CODE> file to be converted.
   * @param  eventName    The name to be used for the new event, after
   *                      replacements.
   * @param  implementation Whether the automaton is being converted as part of
   *                      an <CODE>&lt;Implementation&gt;</CODE> section of a
   *                      <CODE>.desp</CODE> file.
   */
  private void constructEventDecl(final Element event,
                                  final String eventName,
                                  final boolean implementation)
  {
    if (!mEvents.containsKey(eventName)) {
      final IdentifierProxy identifier =
          mFactory.createSimpleIdentifierProxy(eventName);
      final String eventKind = event.getAttribute("ctrl");
      final String eventType = event.getAttribute("type");

      final ScopeKind scope;
      if (implementation) {
        scope = ScopeKind.REQUIRED_PARAMETER;
      } else {
        scope = ScopeKind.LOCAL;
      }

      final EventKind controllability;
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
      final EventDeclProxy eventDecl =
        mFactory.createEventDeclProxy(identifier, controllability, true,
                                      scope, null, null, attributes);

      mEvents.put(eventName, eventDecl);
    }
  }

  /**
   * Constructs all simple components for a section of the DESpot file.
   * @param  section  The <CODE>&lt;Plant&gt;</CODE> or
   *                  <CODE>&lt;Supervisor&gt;</CODE> section to be
   *                  converted.
   * @param  kind     The type of components to be generated.
   * @param  path     The URI of the the DESpot file being converted.
   */
  private void constructSimpleComponents(final Element section,
                                         final ComponentKind kind,
                                         final URI path)
    throws ParserConfigurationException, SAXException, IOException,
           URISyntaxException, WatersUnmarshalException
  {
    final NodeList desList = section.getElementsByTagName("Des");
    for (int i = 0; i < desList.getLength(); i++) {
      final Element des = (Element) desList.item(i);
      final DESpotParameters params = new DESpotNoParameters();
      constructSimpleComponent(des, params, kind, null, false, path);
    }
  }

  private void constructTemplateComponents(final Element section,
                                           final URI path)
    throws ParserConfigurationException, SAXException, IOException,
           URISyntaxException, WatersUnmarshalException
  {
    final NodeList desList = section.getElementsByTagName("Des");
    for (int i = 0; i < desList.getLength(); i++) {
      final Element des = (Element) desList.item(i);
      final NodeList instList = des.getElementsByTagName("Instantiation");
      for (int j = 0; j < instList.getLength(); j++) {
        final Element inst = (Element) instList.item(j);
        final String type = inst.getAttribute("Type");
        final ComponentKind kind;
        if ("P".equals(type)) {
          kind = ComponentKind.PLANT;
        } else if ("S".equals(type)) {
          kind = ComponentKind.SPEC;
        } else {
          throw new WatersUnmarshalException
            ("Unsupported instantiation type '" + type + "' in DESpot file.");
        }
        final String inputType = inst.getAttribute("InputType");
        final NodeList paramList = inst.getElementsByTagName("Parameter");
        final DESpotParameters params;
        if (paramList.getLength() == 0) {
          params = new DESpotNoParameters();
        } else if ("Range".equals(inputType)) {
          params = new DESpotRangeParameters(paramList);
        } else if ("Tuple".equals(inputType)) {
          if (paramList.getLength() > 1) {
            throw new WatersUnmarshalException
              ("Tuple instantiation with more than one parameters unsupported.");
          }
          final Element param = (Element) paramList.item(0);
          params = new DESpotTupleParameters(param);
        } else {
          throw new WatersUnmarshalException
            ("Unsupported input type '" + inputType + "' in DESpot file.");
        }
        constructSimpleComponent(des, params, kind, null, false, path);
      }
    }
  }

  /**
   * Constructs a simple component for an entry of the DESpot file.
   * @param  des      The <CODE>&lt;Des&gt;</CODE> element to be
   *                  converted.
   * @param  params   Instantiation parameter object for DESpot templates.
   * @param  kind     The type of component to be generated.
   * @param  attribs  The attribute map used for the simple component,
   *                  or <CODE>null</CODE>.
   * @param  implementation Whether the automaton is being converted as part
   *                  of an <CODE>&lt;Implementation&gt;</CODE> section of a
   *                  <CODE>.desp</CODE> file.
   * @param  path     The URI of the the DESpot file being converted.
   */
  private void constructSimpleComponent(final Element des,
                                        final DESpotParameters params,
                                        final ComponentKind kind,
                                        final Map<String,String> attribs,
                                        final boolean implementation,
                                        final URI path)
      throws ParserConfigurationException, SAXException, IOException,
      URISyntaxException, WatersUnmarshalException
  {
    while (params.advance()) {
      final String location = des.getAttribute("location");
      final URI uri = new URI(null, null, location, null);
      final Element root = openDESFile(path.resolve(uri));
      final Element definition =
        (Element) root.getElementsByTagName("Definition").item(0);
      final NodeList allHeaders = definition.getElementsByTagName("Header");
      final Element header = (Element) allHeaders.item(0);
      final GraphProxy graph =
        constructGraph(location, root, params, implementation);
      if (graph != null) {
        final String name = header.getAttribute("name");
        final IdentifierProxy identifier =
          mComponentIdentification.createIdentifier(name, params);
        final SimpleComponentProxy comp =
          mFactory.createSimpleComponentProxy(identifier, kind, graph, attribs);
        mComponents.add(comp);
      }
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
   * Constructs the {@link GraphProxy} object for a Waters module
   * from a <CODE>.des</CODE> file. This method converts transitions to edges,
   * and states to nodes.
   * @param  autfilename  The name of the <CODE>.des</CODE> file that contains
   *                      the automaton.
   * @param  des          The root <CODE>&lt;DES&gt;</CODE> element of the
   *                      <CODE>.des</CODE> file that contains the automaton.
   * @param  params       Instantiation parameter object for DESpot templates.
   * @param  implementation Whether the file is being converted as part of
   *                      an <CODE>&lt;Implementation&gt;</CODE> section of a
   *                      <CODE>.desp</CODE> file.
   */
  private GraphProxy constructGraph(final String autfilename,
                                    final Element des,
                                    final DESpotParameters params,
                                    final boolean implementation)
      throws ParserConfigurationException, SAXException, IOException,
      WatersUnmarshalException
  {

    final Element definition =
        (Element) des.getElementsByTagName("Definition").item(0);
    final NodeList allStates = definition.getElementsByTagName("States");
    final Element states = (Element) allStates.item(0);
    final int numStates = Integer.parseInt(states.getAttribute("count"));
    final NodeList transFunctionList =
      definition.getElementsByTagName("Trans-Function");
    final Element transFunction = (Element) transFunctionList.item(0);
    final int numTransitions =
      Integer.parseInt(transFunction.getAttribute("count"));
    startNewGraph(numStates, numTransitions);

    // converts each State in the despot file into nodes for waters
    final NodeList stElmntLst = states.getElementsByTagName("*");
    for (int i = 0; i < stElmntLst.getLength(); i++) {
      final Element stElmnt = (Element) stElmntLst.item(i);
      final SimpleNodeProxy node = convertState(stElmnt, params);
      storeStateId(stElmnt, i);
      mNodes.add(node);
    }
    mStateIdentification.clear();

    // create a hash map of the ID numbers and the name they belong to for the
    // events
    final NodeList allEvents = definition.getElementsByTagName("Events");
    final Element events = (Element) allEvents.item(0);
    final NodeList evElmntLst = events.getElementsByTagName("*");
    storeEvents(evElmntLst, params, implementation);

    // converts each transition in the despot file into edges for waters
    final NodeList transitionList =
      transFunction.getElementsByTagName("Transitions");
    final Element transitions = (Element) transitionList.item(0);
    final NodeList trList = transitions.getElementsByTagName("Tr");
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
    final NodeList transLoopList = transFunction.getElementsByTagName("*");
    final LabelBlockProxy blockedEvents =
        findBlockedEvents(transLoopList, stElmntLst);
    final boolean det = !mNodes.isEmpty();
    return mFactory.createGraphProxy(det, blockedEvents, mNodes, mEdges);
  }

  /**
   * Determines which events are blocked in this automaton. Events are
   * blocked if they do not appear on a transition or selfloop.
   * @param transitions
   *          The list of Transitions and self-loops from the DOM.
   */
  private LabelBlockProxy findBlockedEvents(final NodeList transitions,
                                            final NodeList states)
  {
    // forget about blocked events if there are no states
    if (states.getLength() == 0) {
      return null;
    }

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
    final TIntObjectIterator<String> iter = mEventIDs.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final int eventID = iter.key();
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
   * Maps the IDs for all the events in the DESpot file to their name. Creates
   * an {@link EventDeclProxy} object for each event if that does not already
   * exist in the module being created.   *
   * @param  events       The list of event from the <CODE>&lt;Events&gt;</CODE>
   *                      element of the <CODE>.des</CODE> file.
   * @param  params       Instantiation parameter object for DESpot templates.
   * @param  implementation Whether the automaton is being converted as part of
   *                      an <CODE>&lt;Implementation&gt;</CODE> section of a
   *                      <CODE>.desp</CODE> file.
   */
  private void storeEvents(final NodeList events,
                           final DESpotParameters params,
                           final boolean implementation)
  {
    for (int i = 0; i < events.getLength(); i++) {
      final Element event = (Element) events.item(i);
      final int eventID = Integer.parseInt(event.getAttribute("id"));
      final String name = event.getAttribute("nm");
      final String eventName =
        mEventIdentification.getReplacementName(name, params);
      mEventIDs.put(eventID, eventName);
      constructEventDecl(event, eventName, implementation);
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
    if (srcIndex < 0) {
      throw new WatersUnmarshalException("The source state ID " + srcID
          + " does not exist on the transition with target state ID "
          + targetID + " and event ID " + eventID + " in the file "
          + autfilename + ".");
    }
    final Integer targetIndex = mStates.get(targetID);
    if (targetIndex < 0) {
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

      // DESpot files give an absolute position for the label, Waters requires
      // a position relative to the edge
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
   * Converts a state from the DESpot file into a node for the Waters file.
   * @param  state        The <CODE>&lt;St&gt;</CODE> or
   *                      <CODE>&lt;InitSt&gt;</CODE> element to be converted.
   * @param  params       Instantiation parameter object for DESpot templates.
   */
  private SimpleNodeProxy convertState(final Element state,
                                       final DESpotParameters params)
  {
    final String name = state.getAttribute("nm");
    final String stateName =
      mStateIdentification.getReplacementName(name, params);
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
    final boolean initial = state.getTagName().equals("InitSt");
    final boolean marked = ONE.equals(state.getAttribute("mk"));
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


  //#########################################################################
  //# Inner Class LevelComparator
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


  //#########################################################################
  //# Inner Class IdentifierReplacement
  /**
   * <P>Auxiliary class to ensure that identifiers (i.e., names of automata,
   * events, states) is in the correct format for Waters.</P>
   *
   * <P>DESpot allows characters that Waters does not). If an identifier
   * including illegal characters is encountered, those characters are
   * replaced with something accepted by Waters. The identifiers obtained
   * by this translation are stored in a map to avoid possible clashes.</P>
   */
  private class IdentifierReplacement
  {
    //#######################################################################
    //# Simple Access
    private void clear()
    {
      mBackwardsMap.clear();
    }

    private IdentifierProxy createIdentifier(final String name,
                                             final DESpotParameters params)
    {
      final String replacement = params.replace(name);
      return createIdentifier(replacement);
    }

    private String getReplacementName(final String name,
                                      final DESpotParameters params)
    {
      final String replacement = params.replace(name);
      return getReplacementName(replacement);
    }

    private IdentifierProxy createIdentifier(final String name)
    {
      final String replacement = getReplacementName(name);
      return mFactory.createSimpleIdentifierProxy(replacement);
    }

    private String getReplacementName(final String name)
    {
      final int len = name.length();
      final StringBuilder builder = new StringBuilder(len);
      for (int i = 0; i < len; i++) {
        final char ch = name.charAt(i);
        switch (ch) {
        case ' ':
        case '-':
        case '.':
          builder.append('_');
          break;
        case '%':
          builder.append(':');
          break;
        default:
          builder.append(ch);
          break;
        }
      }
      final String replacement0 = builder.toString();
      String replacement = replacement0;
      String reverseLookUp = mBackwardsMap.get(replacement);
      int suffix = 1;
      while (reverseLookUp != null && !reverseLookUp.equals(name)) {
        replacement = replacement0 + ':' + (suffix++);
        reverseLookUp = mBackwardsMap.get(replacement);
      }
      if (reverseLookUp == null) {
        mBackwardsMap.put(replacement, name);
      }
      return replacement;
    }

    //#######################################################################
    //# Data Members
    private final Map<String,String> mBackwardsMap = new HashMap<>();
  }


  //#########################################################################
  //# Inner Interface DESpotParameters
  private interface DESpotParameters
  {
    //#######################################################################
    //# Iteration
    public boolean advance();
    public String replace(final String text);
  }


  //#########################################################################
  //# Inner Class DESpotNoParameters
  private static class DESpotNoParameters implements DESpotParameters
  {
    //#######################################################################
    //# Iteration
    @Override
    public boolean advance()
    {
      if (mDone) {
        return false;
      } else {
        return mDone = true;
      }
    }

    @Override
    public String replace(final String text)
    {
      return text;
    }

    //#######################################################################
    //# Data Members
    private boolean mDone = false;
  }


  //#########################################################################
  //# Inner Class DESpotRangeParameters
  private static class DESpotRangeParameters implements DESpotParameters
  {
    //#######################################################################
    //# Constructor
    private DESpotRangeParameters(final NodeList list)
      throws WatersUnmarshalException
    {
      this (list, 0);
    }

    private DESpotRangeParameters(final NodeList list, final int index)
      throws WatersUnmarshalException
    {
      final Element param = (Element) list.item(index);
      final String name = param.getAttribute("Name");
      mPattern = Pattern.compile("%" + name + "%");
      final String value = param.getAttribute("Value");
      final String[] values = value.split(",");
      mValues = new ArrayList<>(values.length);
      for (final String entry : values) {
        final int dotPos = entry.indexOf("..");
        if (dotPos < 0) {
          mValues.add(entry);
        } else {
          final String first = entry.substring(0, dotPos);
          final String last = entry.substring(dotPos + 2);
          try {
            final int firstInt = Integer.parseInt(first);
            final int lastInt = Integer.parseInt(last);
            for (int i = firstInt; i <= lastInt; i++) {
              mValues.add(Integer.toString(i));
            }
          } catch (final NumberFormatException exception) {
            boolean ok = false;
            if (first.length() == 1 && last.length() == 1) {
              final char firstChar = first.charAt(0);
              final char lastChar = last.charAt(0);
              if (firstChar >= 'a' && lastChar <= 'z' ||
                  firstChar >= 'A' && lastChar <= 'Z') {
                for (char ch = firstChar; ch <= lastChar; ch++) {
                  mValues.add(Character.toString(ch));
                }
                ok = true;
              }
            }
            if (!ok) {
              throw new WatersUnmarshalException
                ("Unsupported instantiation range '" + entry +
                 "' in DESpot file.");
            }
          }
        }
      }
      mIterator = mValues.isEmpty() ? null : mValues.iterator();
      if (index + 1 < list.getLength()) {
        mNext = new DESpotRangeParameters(list, index + 1);
      } else {
        mNext = null;
      }
    }

    //#######################################################################
    //# Iteration
    @Override
    public boolean advance()
    {
      if (mIterator == null) {
        return false;
      } else if (mIterator.hasNext()) {
        mCurrentValue = mIterator.next();
        return true;
      } else if (mNext != null && mNext.advance()) {
        mIterator = mValues.iterator();
        mCurrentValue = mIterator.next();
        return true;
      } else {
        return false;
      }
    }

    @Override
    public String replace(String text)
    {
      final Matcher matcher = mPattern.matcher(text);
      text = matcher.replaceAll(mCurrentValue);
      if (mNext != null) {
        text = mNext.replace(text);
      }
      return text;
    }

    //#######################################################################
    //# Data Members
    private final Pattern mPattern;
    private final List<String> mValues;
    private Iterator<String> mIterator;
    private String mCurrentValue;
    private final DESpotRangeParameters mNext;
  }


  //#########################################################################
  //# Inner Class DESpotTupleParameters
  private static class DESpotTupleParameters implements DESpotParameters
  {
    //#######################################################################
    //# Constructors
    private DESpotTupleParameters(final Element param)
      throws WatersUnmarshalException
    {
      String name = param.getAttribute("Name");
      final int len = name.length();
      if (len == 0) {
        throw new WatersUnmarshalException
          ("Found empty name in tuple instantiation in DESpot file.");
      }
      int start = name.charAt(0) == '(' ? 1 : 0;
      int end = name.charAt(len - 1) == ')' ? len - 1 : len;
      name = name.substring(start, end);
      final String[] names = name.split(",");
      final int width = names.length;
      mPatterns = new Pattern[width];
      for (int i = 0; i < width; i++) {
        mPatterns[i] = Pattern.compile("%" + names[i] + "%");
      }
      mTuples = new ArrayList<>();
      final String value = param.getAttribute("Value");
      int p = 0;
      while (p < value.length()) {
        if (p > 0 && value.charAt(p++) != ',') {
          throw new WatersUnmarshalException
            ("Error parsing tuple values in DESpot file.");
        }
        if (value.charAt(p++) != '(') {
          throw new WatersUnmarshalException
            ("Error parsing tuple values in DESpot file.");
        }
        final String[] tuple = new String[width];
        for (int i = 0; i < width; i++) {
          if (i > 0 && value.charAt(p++) != ',') {
            throw new WatersUnmarshalException
              ("Error parsing tuple values in DESpot file.");
          }
          start = p;
          char ch;
          do {
            ch = value.charAt(p++);
          } while (ch != ')' && ch != ',');
          end = --p;
          tuple[i] = value.substring(start, end);
        }
        if (value.charAt(p++) != ')') {
          throw new WatersUnmarshalException
            ("Error parsing tuple values in DESpot file.");
        }
        mTuples.add(tuple);
      }
      mIterator = mTuples.iterator();
    }

    //#######################################################################
    //# Iteration
    @Override
    public boolean advance()
    {
      if (mIterator.hasNext()) {
        mCurrentTuple = mIterator.next();
        return true;
      } else {
        return false;
      }
    }

    @Override
    public String replace(String text)
    {
      for (int i = 0; i < mPatterns.length; i++) {
        final Matcher matcher = mPatterns[i].matcher(text);
        text = matcher.replaceAll(mCurrentTuple[i]);
      }
      return text;
    }

    //#######################################################################
    //# Data Members
    private final Pattern[] mPatterns;
    private final List<String[]> mTuples;
    private final Iterator<String[]> mIterator;
    private String[] mCurrentTuple;
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


  //#########################################################################
  //# Data Members
  /**
   * Maps the name of a module to its ModuleProxy.
   */
  private final Map<String,ModuleProxy> mModules =
    new HashMap<String,ModuleProxy>();
  /**
   * Replacement map to convert event names from DESpot to Waters.
   */
  private final IdentifierReplacement mEventIdentification =
    new IdentifierReplacement();
  /**
   * Replacement map to convert component names from DESpot to Waters.
   */
  private final IdentifierReplacement mComponentIdentification =
    new IdentifierReplacement();
  /**
   * Replacement map to convert state names from DESpot to Waters.
   */
  private final IdentifierReplacement mStateIdentification =
    new IdentifierReplacement();

  /**
   * Stores the ID number the DESpot file uses to reference a state and the
   * location of the state in the 'nodes' list.
   */
  private TIntIntHashMap mStates;
  /**
   * Stores the ID number the despot file uses to reference an event and the
   * name of the event.
   */
  private final TIntObjectHashMap<String> mEventIDs = new TIntObjectHashMap<>();
  // The above two hash maps are needed for finding the correct source/target
  // state of a transition and correct event name.

  /**
   * Maps the source state and target state together (as an IdPair) for a
   * transition to the index number where this edge exists in the list 'mEdges'.
   * This is used to lookup quickly whether an edge already exists.
   */
  private TObjectIntHashMap<IdPair> mTransitions;

  /**
   * This list stores all the nodes (states) for the current automaton.
   */
  private final List<SimpleNodeProxy> mNodes = new ArrayList<>();
  /**
   * This list stores all the edges (transitions) for the current automaton.
   */
  private final List<EdgeProxy> mEdges = new ArrayList<>();
  /**
   * List that stores all the components for a module, such as the automata
   * (SimpleComponent's) or instances of other modules.
   */
  private final List<Proxy> mComponents = new ArrayList<>();
  /**
   * Maps the name of an event to the EventDecl for that event.
   */
  private final Map<String,EventDeclProxy> mEvents = new TreeMap<>();

  /**
   * The factory used to build up the modules for the <CODE>.wmod</CODE> files
   * we are converting into.
   */
  private final ModuleProxyFactory mFactory;

  private File mOutputDir;

  private DocumentManager mDocumentManager;


  //#########################################################################
  //# Class Constants
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
