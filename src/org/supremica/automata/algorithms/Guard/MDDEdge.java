package org.supremica.automata.algorithms.Guard;

import java.util.ArrayList;

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


    public MDDEdge(final MDDNode fromNode, final MDDNode toNode)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public MDDEdge(final MDDNode fromNode, final MDDNode toNode, final ArrayList<String> labelString,final String id)
    {
        this(fromNode, toNode);
        this.labelString = labelString;
        this.id = id;
    }

    public void setFromNode(final MDDNode fNode)
    {
        fromNode = fNode;
    }

    public void setToNode(final MDDNode tNode)
    {
        toNode = tNode;
    }

    public void setNodes(final MDDNode fNode, final MDDNode tNode)
    {
        setFromNode(fNode);
        setToNode(tNode);
    }

    public void setLabel(final ArrayList<Integer> l)
    {
        label = l;
    }

    public void setLabelString(final ArrayList<String> lString)
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
