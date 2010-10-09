package org.supremica.automata.algorithms.Guard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sajed Miremadi
 */
public class MDD {

    private final ArrayList<MDDNode> nodes = new ArrayList<MDDNode>();
    private final ArrayList<MDDEdge> edges = new ArrayList<MDDEdge>();

    private int nodeID = -1;
    private MDDNode root;
    private final Map<MDDNode,ArrayList<MDDEdge>> node2OutEdgesMap = new HashMap<MDDNode,ArrayList<MDDEdge>>();
    private final Map<MDDNode,ArrayList<MDDEdge>> node2InEdgesMap = new HashMap<MDDNode,ArrayList<MDDEdge>>();
    private final Map<Integer, MDDNode> nodeID2node = new HashMap<Integer, MDDNode>();

    public MDD()
    {

    }

    public void setRoot(final MDDNode node)
    {
        root = node;
    }

    public MDDNode getRoot()
    {
        return root;
    }

    public MDDEdge getEdgeWithID(final String id)
    {
        for(final MDDEdge edge:edges)
        {
            if(edge.getID().equals(id))
                return edge;
        }

        return null;
    }

    public void addEdge(final MDDEdge edge)
    {
        edges.add(edge);
        edge.getFromNode().addChild(edge.getToNode());
        edge.getToNode().addParent(edge.getFromNode());
        edge.getToNode().setReachedBy(edge.getLabel());
        if(node2OutEdgesMap.get(edge.getFromNode()) == null)
        {
            final ArrayList<MDDEdge> temp = new ArrayList<MDDEdge>();
            temp.add(edge);
            node2OutEdgesMap.put(edge.getFromNode(), temp);
        }
        else
        {
            node2OutEdgesMap.get(edge.getFromNode()).add(edge);
        }

        if(node2InEdgesMap.get(edge.getToNode()) == null)
        {
            final ArrayList<MDDEdge> temp = new ArrayList<MDDEdge>();
            temp.add(edge);
            node2InEdgesMap.put(edge.getToNode(), temp);
        }
        else
        {
            node2InEdgesMap.get(edge.getToNode()).add(edge);
        }

    }

    public ArrayList<MDDEdge> node2OutEdges(final MDDNode node)
    {
        return node2OutEdgesMap.get(node);
    }

    public ArrayList<MDDEdge> node2InEdges(final MDDNode node)
    {
        return node2InEdgesMap.get(node);
    }

    public ArrayList<MDDEdge> getEdges()
    {
        return edges;
    }

    public MDDNode createNode(final String name)
    {
        nodeID++;
        return new MDDNode(nodeID,name);
    }

    public void addNode(final MDDNode node)
    {
        if(!nodeID2node.containsKey(node.getID()))
        {
            nodes.add(node);
            nodeID2node.put(node.getID(), node);
        }
    }

    public MDDNode getNode(final int id)
    {
        if(nodeID2node.containsKey(id))
            return nodeID2node.get(id);
        else
            return null;
    }

    public ArrayList<MDDNode> getNodes()
    {
        return nodes;
    }

    public int nodeCount()
    {
        return nodes.size()-1;
    }

}
