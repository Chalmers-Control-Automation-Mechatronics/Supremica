package net.sourceforge.waters.despot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ParseDespotFile
{
  // #Data Members
  // stores the ID number and name of the <State> elements in .des files
  HashMap<Integer,Integer> mStates;
  // stores the ID number and name of the <Event> elements in .des files
  HashMap<Integer,String> mEvents;


  List<NodeProxy> nodes = new ArrayList<NodeProxy>();
  List<EdgeProxy> edges = new ArrayList<EdgeProxy>();

  // used to build up the module for the .wmod file we are converting into
  ModuleProxyFactory factory = ModuleElementFactory.getInstance();

  @SuppressWarnings("unused")
  private GraphProxy parseDESFile(File file)
  {
    try {
      DocumentBuilder builder =
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = builder.parse(file);

      // converts each State in the despot file into nodes for waters
      NodeList states = doc.getElementsByTagName("States");
      for (int i = 0; i < states.getLength(); i++) {
        Element state = (Element) states.item(i);
        NodeProxy node = convertState(state);
        nodes.add(node);
        // stores the ID number and the index of this node in the list
        storeStateId(state, i);
      }

      // converts each transition in the despot file into edges for waters
      NodeList transitions = doc.getElementsByTagName("Transitions");
      for (int i = 0; i < transitions.getLength(); i++) {
        Element tr = (Element) transitions.item(i);
        EdgeProxy edge = convertTransition(tr);
        edges.add(edge);
      }

      return null;
    } catch (Exception e) {
      System.out.print(e.getMessage());
      return null;
    }
  }

  private void storeStateId(Element state, int index)
  {
    mStates.put(Integer.parseInt(state.getAttribute("id")), index);
  }

  private EdgeProxy convertTransition(Element tr)
  {
    // gets the index number of the source and target states in the list
    int srcIndex = mStates.get(tr.getAttribute("fID"));
    int targetIndex = mStates.get(tr.getAttribute("tID"));

    //String eventName = tr.getAttribute("eID");
    return factory.createEdgeProxy(nodes.get(srcIndex), nodes.get(targetIndex), null, null, null, null, null);
  }

  private NodeProxy convertState(Element state)
  {
    //final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
    if (state.getTagName() == "St") {
      return factory.createSimpleNodeProxy(state.getAttribute("nm"));
    } else {
      return factory.createSimpleNodeProxy(state.getAttribute("nm"), null,
          true, null, null, null);
    }

  }
}
