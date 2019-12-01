package org.supremica.automata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.algorithms.FT.ANDGateNode;
import org.supremica.automata.algorithms.FT.BasicEventNode;
import org.supremica.automata.algorithms.FT.EventNode;
import org.supremica.automata.algorithms.FT.GateNode;
import org.supremica.automata.algorithms.FT.NoBasicEventNode;
import org.supremica.automata.algorithms.FT.ORGateNode;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Zenuity 2019 Hackfest
 *
 * Build FT plant EFA from FT represented in the format of XML.
 *
 * @author zhefei
 */

public class FTEFABuilder
{

  // constants
  static HashMap<String, String> specialCharRep = new HashMap<>();
  {
    specialCharRep.put(" ", "_");
    specialCharRep.put(">", "gt");
    specialCharRep.put(":", "_");
    specialCharRep.put("&", "_and_");
    specialCharRep.put("w/o", "without");
  }

  static public String replaceSpecialChars(final String name) {
    String res = name;
    for (final String k: specialCharRep.keySet()) {
      if(name.contains(k))
       res = res.replace(k, specialCharRep.get(k));
    }
    return res;
  }

  private final HashMap<String, BasicEventNode> basicEventName2Node;
  private final HashMap<String, NoBasicEventNode> nonBasicEventName2Node;

  final static String PREFIX  = "/SystemWeaver4/Items/";

  private static Logger logger = LogManager.getLogger(FTEFABuilder.class);

  private final ModuleSubject module;
  private NoBasicEventNode rootNode;
  private final File ftInXml;

  public FTEFABuilder(final File ftInXml, final ModuleSubject module)
  {
    this.module = module;
    this.ftInXml = ftInXml;
    this.basicEventName2Node = new HashMap<>();
    this.nonBasicEventName2Node = new HashMap<>();

    logger.info("Parsing the FT file: " + ftInXml.getName() + "...");
    buildInternalFT();

    logger.debug("Creating EFAs...");
    buildFTEFA();
  }

  private void buildInternalFT()
  {
    // Creating a Java DOM XML Parser
    final DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      e.printStackTrace();
    }

    // Parsing XML with a Java DOM Parser
    Document xmlDocument = null;
    try {
      xmlDocument = builder.parse(new FileInputStream(this.ftInXml));
    } catch (final SAXException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    // Creating an XPath object
    final XPath xPath =  XPathFactory.newInstance().newXPath();

    // ------------------------------------------------------------------------
    // ROOT EVENT
    // ------------------------------------------------------------------------

    // find the root of the FT
    final String rootPartGroup = "Item/PartGroups/PartGroup[@sid='2IFW']/";
    final String rEx = PREFIX + rootPartGroup + "Parts/Part/Name";

    //read a string value
    String root = null;
    try {
      root = xPath.compile(rEx).evaluate(xmlDocument);
    } catch (final XPathExpressionException ex) {ex.printStackTrace();}

    // replace space with underscore in the names
    rootNode = new NoBasicEventNode(root);
    nonBasicEventName2Node.put(root, rootNode);
    logger.debug("Root event is " + rootNode.getName());

    // ------------------------------------------------------------------------
    // BASIC EVENTS
    // ------------------------------------------------------------------------

    // find all basic event item
    final String basicEventExp = PREFIX + "Item[@sid='2CET']";

    //read node list for all basic events
    try {
      final NodeList nodeList =
        (NodeList) xPath.compile(basicEventExp).evaluate(xmlDocument,
                                                         XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node n = nodeList.item(i);
        n = n.getFirstChild().getChildNodes().item(0);
        logger.debug("Basic event: " + n.getNodeValue());
        basicEventName2Node.put(n.getNodeValue(),
                                new BasicEventNode(n.getNodeValue()));
      }
    } catch (final XPathExpressionException exception) {
      exception.printStackTrace();
    }

    // ------------------------------------------------------------------------
    // GATES
    // ------------------------------------------------------------------------

    // find all AND gate items
    final String andGateExp = PREFIX + "Item[@sid='2CLD']";
    // read node list for and gates
    try {
      final NodeList nodeList =
        (NodeList) xPath.compile(andGateExp).evaluate(xmlDocument,
                                                      XPathConstants.NODESET);
      for(int i = 0; i < nodeList.getLength(); i++) {
        // AND item node
        final Node n = nodeList.item(i);

        // Naming the gate is random ...
        final ANDGateNode andGateNode = new ANDGateNode("AND-" + i);

        // get the non basic event (parent) of this gate node
        final Node nonBasicEventNode = n.getFirstChild().getChildNodes().item(0);
        final String eventName = nonBasicEventNode.getNodeValue();
        logger.debug("Non basic event for AND: " + eventName);
        if (nonBasicEventName2Node.containsKey(eventName)) {
          final NoBasicEventNode eventNode = nonBasicEventName2Node.get(eventName);
          eventNode.setGate(andGateNode);
        } else {
          final NoBasicEventNode eventNode = new NoBasicEventNode(eventName);
          eventNode.setGate(andGateNode);
          nonBasicEventName2Node.put(eventName, eventNode);
        }

        // deal with the children of this and gate
        final String partGroups = "PartGroups";
        int k = 0;
        final NodeList children = n.getChildNodes();
        for(k = 0; k < children.getLength(); k++) {
          if (children.item(k).getNodeName().equals(partGroups)) {
            break;
          }
        }
        // k is the index of partsGroup
        final Node partGroupsNode = children.item(k);
        // find all partGroup nodes under partGroupsNode
        final NodeList partGroupNodes = partGroupsNode.getChildNodes();
        for (int j = 0; j < partGroupNodes.getLength(); j++) {
          // each partGroup
          final Node partGroupNode = partGroupNodes.item(j);

          if(!partGroupNode.getAttributes().getNamedItem("sid")
            .getFirstChild().getNodeValue().equals("2IFI"))
            continue;

          // get "parts" node under partGroup
          final Node partsNode = partGroupNode.getChildNodes().item(1);
          // get all part nodes
          final NodeList partNodes = partsNode.getChildNodes();
          for (int ii = 0; ii < partNodes.getLength(); ii++) {
            final Node partNode = partNodes.item(ii);
            final Node nameNode = partNode.getFirstChild();
            final String name = nameNode.getFirstChild().getNodeValue();
            if (basicEventName2Node.containsKey(name)) {
              andGateNode.addChild(basicEventName2Node.get(name));
            }
            else if(nonBasicEventName2Node.containsKey(name)) {
              andGateNode.addChild(nonBasicEventName2Node.get(name));
            }
            else {
              // it is an intermediate event that has not been built
              final NoBasicEventNode nben = new NoBasicEventNode(name);
              andGateNode.addChild(nben);
              nonBasicEventName2Node.put(name, nben);
            }
            logger.debug("AND Child event: " + name);
          }
        }

      }
    } catch (final XPathExpressionException exception) {
      exception.printStackTrace();
    }

    // OR gate
    // find all OR gate items
    final String orGateExp = PREFIX + "Item[@sid='2CLO']";
    // read node list for and gates
    try {
      final NodeList nodeList =
        (NodeList) xPath.compile(orGateExp).evaluate(xmlDocument,
                                                      XPathConstants.NODESET);
      for(int i = 0; i < nodeList.getLength(); i++) {
        // OR item node
        final Node n = nodeList.item(i);

        // Naming the gate is random ...
        final ORGateNode orGateNode = new ORGateNode("OR-" + i);

        // get the non basic event (parent) of this gate node
        final Node nonBasicEventNode = n.getFirstChild().getChildNodes().item(0);
        final String eventName = nonBasicEventNode.getNodeValue();
        logger.debug("Non basic event for OR: " + eventName);
        if (nonBasicEventName2Node.containsKey(eventName)) {
          final NoBasicEventNode eventNode = nonBasicEventName2Node.get(eventName);
          eventNode.setGate(orGateNode);
        } else {
          final NoBasicEventNode eventNode = new NoBasicEventNode(eventName);
          eventNode.setGate(orGateNode);
          nonBasicEventName2Node.put(eventName, eventNode);
        }

        // deal with the children of this and gate
        final String partGroups = "PartGroups";
        int k = 0;
        final NodeList children = n.getChildNodes();
        for(k = 0; k < children.getLength(); k++) {
          if (children.item(k).getNodeName().equals(partGroups)) {
            break;
          }
        }
        // k is the index of partsGroup
        final Node partGroupsNode = children.item(k);
        // find all partGroup nodes under partGroupsNode
        final NodeList partGroupNodes = partGroupsNode.getChildNodes();
        for (int j = 0; j < partGroupNodes.getLength(); j++) {
          // each partGroup
          final Node partGroupNode = partGroupNodes.item(j);

          if(!partGroupNode.getAttributes().getNamedItem("sid")
            .getFirstChild().getNodeValue().equals("2IFI"))
            continue;

          // get "parts" node under partGroup
          final Node partsNode = partGroupNode.getChildNodes().item(1);
          // get all part nodes
          final NodeList partNodes = partsNode.getChildNodes();
          for (int ii = 0; ii < partNodes.getLength(); ii++) {
            final Node partNode = partNodes.item(ii);
            final Node nameNode = partNode.getFirstChild();
            final String name = nameNode.getFirstChild().getNodeValue();
            if (basicEventName2Node.containsKey(name)) {
              orGateNode.addChild(basicEventName2Node.get(name));
            }
            else if(nonBasicEventName2Node.containsKey(name)) {
              orGateNode.addChild(nonBasicEventName2Node.get(name));
            }
            else {
              // it is an intermediate event that has not been built
              final NoBasicEventNode nben = new NoBasicEventNode(name);
              orGateNode.addChild(nben);
              nonBasicEventName2Node.put(name, nben);
            }
            logger.debug("OR Child event: " + name);
          }
        }
      }
    } catch (final XPathExpressionException exception) {
      exception.printStackTrace();
    }
  }

  public void buildFTEFA() {
    final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
    // Starting with the rootNode and two maps
    final NoBasicEventNode root = this.rootNode;
    final HashMap<String, BasicEventNode> ben2Node = new HashMap<>();
    final HashMap<String, NoBasicEventNode> nben2Node = new HashMap<>();;
    for (final Map.Entry<String,BasicEventNode> e: basicEventName2Node.entrySet()) {
      final String rawName = e.getKey();
      final BasicEventNode n = e.getValue();
      final String replacedName = replaceSpecialChars(rawName);
      n.setName(replacedName);
      ben2Node.put(replacedName, n);
      exAutomata.addEvent(replacedName, EventKind.CONTROLLABLE.toString());
    }
    for (final Map.Entry<String,NoBasicEventNode> e: nonBasicEventName2Node.entrySet()) {
      final String rawName = e.getKey();
      final NoBasicEventNode n = e.getValue();
      final String replacedName = replaceSpecialChars(rawName);
      n.setName(replacedName);
      nben2Node.put(replacedName, n);
      if (n != root)
        exAutomata.addEvent(replacedName, EventKind.UNCONTROLLABLE.toString());
    }
    // Algorithm 1:
    final String LOCATION_PREFIX = "LC_";
    final String EFA_PREFIX = "EFA_";

    final LinkedList<EventNode> queue = new LinkedList<EventNode>();
    EventNode currNode = null;
    queue.add(root);
    while(!queue.isEmpty()) {
      // pop the first one in the queue
      currNode = queue.remove();
      final String currNodeName = currNode.getName();
      if (currNode instanceof NoBasicEventNode) {
        final GateNode gate = ((NoBasicEventNode) currNode).getGate();
        if (gate instanceof ORGateNode) {
          final ORGateNode orGate = (ORGateNode) gate;
          // create EFA
          final ExtendedAutomaton efa =
            new ExtendedAutomaton(EFA_PREFIX + currNodeName,
                                  ComponentKind.PLANT);
          // add locations
          final String source = "Init";
          final String target = LOCATION_PREFIX + currNodeName;
          efa.addState(source, false, true, false);
          if (currNode == root) {
            efa.addState(target, true, false, false);
          } else {
            efa.addState(target, false, false, false);
          }
          // add transitions
          for (final EventNode e : orGate.getChildren()) {
            if (e instanceof BasicEventNode)
              efa.addTransition(source, target, e.getName(), null, null);
            else { // non basic untested...
              final String guard =
                EFA_PREFIX + e.getName() + "==" + LOCATION_PREFIX + e.getName();
              efa.addTransition(source, target, e.getName(), guard, null);
              queue.add(e);
            }
          }
          exAutomata.addAutomaton(efa);
        } else { // ANDGateNode
          final ANDGateNode andGate = (ANDGateNode) gate;
          // create EFA
          final ExtendedAutomaton efa =
            new ExtendedAutomaton(EFA_PREFIX + currNodeName,
                                  ComponentKind.PLANT);
          // add 4 locations assuming that and gate always has two children
          final String source = "Init";
          final String target = LOCATION_PREFIX + currNodeName;
          efa.addState(source, false, true, false);
          if (currNode == root) {
            efa.addState(target, true, false, false);
          } else {
            efa.addState(target, false, false, false);
          }
          // add two more location depending on the two events
          final EventNode e1 = andGate.getChildren().get(0);
          final EventNode e2 = andGate.getChildren().get(1);
          final String n1 = e1.getName();
          final String n2 = e2.getName();
          final String intermediate1 = LOCATION_PREFIX + "by_" + n1;
          final String intermediate2 = LOCATION_PREFIX + "by_" + n2;
          efa.addState(intermediate1, false, false, false);
          efa.addState(intermediate2, false, false, false);
          // add transitions
          if (e1 instanceof BasicEventNode) {
            efa.addTransition(source, intermediate1, n1, null, null);
            efa.addTransition(intermediate2, target, n1, null, null);
          } else {
            final String guard = EFA_PREFIX + n1 + "==" + LOCATION_PREFIX + n1;
            efa.addTransition(source, intermediate1, n1, guard, null);
            efa.addTransition(intermediate2, target, n1, guard, null);
            queue.add(e1);
          }

          if (e2 instanceof BasicEventNode) {
            efa.addTransition(source, intermediate2, n2, null, null);
            efa.addTransition(intermediate1, target, n2, null, null);
          } else {
            final String guard = EFA_PREFIX + n2 + "==" + LOCATION_PREFIX + n2;
            efa.addTransition(source, intermediate2, n2, guard, null);
            efa.addTransition(intermediate1, target, n2, guard, null);
            queue.add(e2);
          }
          exAutomata.addAutomaton(efa);
        }
      }
    }
  }
}
