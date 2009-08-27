package org.supremica.automata.algorithms.Guard;

import java.util.HashSet;

/**
 *
 * @author sajed
 */
public class MDDEdge {

    private MDDNode fromNode;
    private MDDNode toNode;
    private HashSet<Integer> label = new HashSet<Integer>();
    private HashSet<String> labelString = new HashSet<String>();


    public MDDEdge(MDDNode fromNode, MDDNode toNode)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public MDDEdge(MDDNode fromNode, MDDNode toNode, HashSet<Integer> label,HashSet<String> labelString)
    {
        this(fromNode, toNode);
        this.label = label;
        this.labelString = labelString;

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

    public void setLabel(HashSet<Integer> l)
    {
        label = l;
    }

    public void setLabelString(HashSet<String> lString)
    {
        labelString = lString;
    }

    public MDDNode getFromNode()
    {
        return fromNode;
    }

    public MDDNode getToNode()
    {
        return toNode;
    }

    public HashSet<Integer> getLabel()
    {
        return label;
    }

    public HashSet<String> getLabelString()
    {
        return labelString;
    }

}
