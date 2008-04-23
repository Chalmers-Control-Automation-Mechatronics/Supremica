package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;

/**
 *
 * @author Avenir Kobetski
 */
public class ConnectedComponentVertice
{
    int VerticeIndex;
    int depthIndex = -1;
    int lowlinkIndex = -1;
    ArrayList<ConnectedComponentEdge> outEdges;
    //ArrayList<Edge> edgeCopies; @Deprecated
    
    public ConnectedComponentVertice (int VerticeIndex)
    {
        this.VerticeIndex = VerticeIndex;
        outEdges = new ArrayList<ConnectedComponentEdge>();
    }
    
    public ConnectedComponentVertice(int VerticeIndex, ArrayList<ConnectedComponentEdge> outgoingEdges)
    {
        this(VerticeIndex);
        outEdges = outgoingEdges;
    }
    
    public int getVerticeIndex() { return VerticeIndex; }
    public int getDepthIndex() { return depthIndex; }
    public void setDepthIndex(int newDepthIndex) { depthIndex = newDepthIndex; }
    public int getLowlinkIndex() { return lowlinkIndex; }
    public void setLowlinkIndex(int newLowlinkIndex) { lowlinkIndex = newLowlinkIndex; }
    public ArrayList<ConnectedComponentEdge> getOutEdges() { return outEdges; }
    public void setOutEdges(ArrayList<ConnectedComponentEdge> outEdges) { this.outEdges = outEdges; }
    public void addOutEdge(ConnectedComponentEdge edge) { outEdges.add(edge); }
    //public ArrayList<Edge> getEdgeCopies() { return edgeCopies; } @Deprecated
    /**public void resetEdgeCopies()
    {
        edgeCopies = new ArrayList<Edge>();
        for (Edge edge : outEdges)
        {
            edgeCopies.add(edge);
        }
    } @Deprecated */
    //public Edge removeEdgeCopy(int index) { return edgeCopies.remove(index); } @Deprecated
}
