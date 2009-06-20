package org.supremica.automata.algorithms.Guard;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Sajed Miremadi
 */
public class MDD {
    
    HashSet<MDDNode> nodes = new HashSet<MDDNode>();
    HashSet<MDDEdge> edges = new HashSet<MDDEdge>();

    private int nodeID = -1;
    private MDDNode root;
    private HashMap<MDDNode,HashSet<MDDEdge>> fromNode2edgesMap = new HashMap<MDDNode,HashSet<MDDEdge>>();
    private HashMap<MDDNode,HashSet<MDDEdge>> toNode2edgesMap = new HashMap<MDDNode,HashSet<MDDEdge>>();

    HashMap<Integer, MDDNode> nodeID2node = new HashMap<Integer, MDDNode>();

    public MDD()
    {

    }

    public void setRoot(MDDNode node)
    {
        root = node;
    }

    public MDDNode getRoot()
    {
        return root;
    }

    public void addEdge(MDDEdge edge)
    {
        edges.add(edge);
        edge.getFromNode().addChild(edge.getToNode());
        edge.getToNode().addParent(edge.getFromNode());
        edge.getToNode().setReachedBy(edge.getLabel());
        if(fromNode2edgesMap.get(edge.getFromNode()) == null)
        {
            HashSet<MDDEdge> temp = new HashSet<MDDEdge>();
            temp.add(edge);
            fromNode2edgesMap.put(edge.getFromNode(), temp);
        }
        else
        {
            fromNode2edgesMap.get(edge.getFromNode()).add(edge);
        }

        if(toNode2edgesMap.get(edge.getToNode()) == null)
        {
            HashSet<MDDEdge> temp = new HashSet<MDDEdge>();
            temp.add(edge);
            toNode2edgesMap.put(edge.getToNode(), temp);
        }
        else
        {
            toNode2edgesMap.get(edge.getToNode()).add(edge);
        }

    }

    public HashSet<MDDEdge> fromNode2edges(MDDNode node)
    {
        return fromNode2edgesMap.get(node);
    }

    public HashSet<MDDEdge> toNode2edges(MDDNode node)
    {
        return toNode2edgesMap.get(node);
    }

    public HashSet<MDDEdge> getEdges()
    {
        return edges;
    }

    public MDDNode createNode(String name)
    {
        nodeID++;
        return new MDDNode(nodeID,name);
    }

    public void addNode(MDDNode node)
    {
        if(!nodeID2node.containsKey(node.getID()))
        {
            nodes.add(node);
            nodeID2node.put(node.getID(), node);
        }
    }

    public MDDNode getNode(int id)
    {
        if(nodeID2node.containsKey(id))
            return nodeID2node.get(id);
        else
            return null;
    }

    public HashSet<MDDNode> getNodes()
    {
        return nodes;
    }

}
