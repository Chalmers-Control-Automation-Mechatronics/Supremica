package org.supremica.automata.algorithms.Guard;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author sajed
 */
public class MDDEdge {

    private MDDNode fromNode;
    private MDDNode toNode;
    private ArrayList<Integer> label = new ArrayList<Integer>();
    private ArrayList<String> labelString = new ArrayList<String>();
    private String id;


    public MDDEdge(MDDNode fromNode, MDDNode toNode)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public MDDEdge(MDDNode fromNode, MDDNode toNode, ArrayList<String> labelString,String id)
    {
        this(fromNode, toNode);
        this.labelString = labelString;
        this.id = id;
    }

    public void setFromNode(MDDNode fNode)
    {
        fromNode = fNode;
    }

    public void setToNode(MDDNode tNode)
    {
        toNode = tNode;
    }

    public void setNodes(MDDNode fNode, MDDNode tNode)
    {
        setFromNode(fNode);
        setToNode(tNode);
    }

    public void setLabel(ArrayList<Integer> l)
    {
        label = l;
    }

    public void setLabelString(ArrayList<String> lString)
    {
        labelString = lString;
    }

    public MDDNode getFromNode()
    {
        return fromNode;
    }

    public String getID()
    {
        return id;
    }

    public MDDNode getToNode()
    {
        return toNode;
    }

    public ArrayList<Integer> getLabel()
    {
        return label;
    }

    public ArrayList<String> getLabelString()
    {
        return labelString;
    }

}
