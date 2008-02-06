/*
 * BookingPairsGraphExplorer.java
 *
 * Created on den 16 augusti 2007, 10:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.util.ArrayList;
import org.supremica.automata.LabeledEvent;

/**
 *
 * @author Avenir Kobetski
 */
public class BookingPairsGraphExplorer
{
    //temp
    private org.supremica.log.Logger logger = org.supremica.log.LoggerFactory.createLogger(BookingPairsGraphExplorer.class);
 
//    private ArrayList<int[]>[] neighbors;
//    private int[][] vertices;
    
    private ArrayList<int[]>[] edges = null;
    private boolean[] inComponent;
    private int[] rootIndex;
    private Vertex[] vertices;
    
    ArrayList<Vertex> tarjanStack = new ArrayList<Vertex>();
    int tarjanIndex = 0; 
    
    ArrayList<Vertex>[] unpromisingVertices;
    ArrayList<Vertex> johnsonStack = new ArrayList<Vertex>();
    boolean[] blockedStatus;
    
    ArrayList<ArrayList<Vertex>> maxSCCList = new ArrayList<ArrayList<Vertex>>();
    
    public BookingPairsGraphExplorer(ArrayList<int[]>[] edges)
    {
        this.edges = edges;
        inComponent = new boolean[edges.length];
        rootIndex = new int[edges.length];
        
        vertices = new Vertex[edges.length];
        for (int i = 0; i < vertices.length; i++)
        {
            vertices[i] = new Vertex(i); //, edges[i]);
        }
        
        for (int i = 0; i < edges.length; i++)
        {
            for (int j = 0; j < edges[i].size(); j++)
            {
                vertices[i].addOutEdge(new Edge(vertices[i], vertices[edges[i].get(j)[0]], 
                        edges[i].get(j)[1], edges[i].get(j)[4]));
            }
        }

        for (int i = 0; i < edges.length; i++)
        {
            for (int j = 0; j < edges[i].size(); j++)
            {
                logger.info("There is an edge between Z" + i + " and Z" + 
                        edges[i].get(j)[0] + " with color P" + edges[i].get(j)[1] + 
                        " and overlapping_property = " + edges[i].get(j)[4]);
            }
        }
        
        tarjan(vertices[0]);
        
        johnsonStack = new ArrayList<Vertex>();
        unpromisingVertices =  new ArrayList[vertices.length];
        blockedStatus = new boolean[vertices.length];
        for (int i = 0; i < vertices.length - 1; i++)
        {
            // Reset the info about blocking and unpromising vertices
            for (int j = 0; j < blockedStatus.length; j++)
            {           
                if (unpromisingVertices[j] == null)
                {
                    unpromisingVertices[j] = new ArrayList<Vertex>();
                }
                else
                {
                    unpromisingVertices[j].clear();
                }
                blockedStatus[j] = false;
            }
            
            findCycles(vertices[i], vertices[i]);
        }
    }
    
    private boolean findCycles(Vertex inVertex, Vertex startVertex)
    {
        boolean cycleFound = false; 
        johnsonStack.add(inVertex); // stack v;
        blockedStatus[inVertex.getVertexIndex()] = true; // blocked(v) := true;
        
        String s = "s";
        
        for (Edge edge : inVertex.getOutEdges())
        {
            Vertex toVertex = edge.getToVertex();

            if (toVertex.getVertexIndex() >= startVertex.getVertexIndex())
            { // Look only in forward direction to avoid cycle repetition                
                if (toVertex.equals(startVertex))
                { // If the start vertex if found again, we have a cycle
                    //temp (output circuit)
                    String str = "Johnson-circuit: ";
                    for (Vertex v : johnsonStack)
                    {
                        str += "v" + v.getVertexIndex() + " - ";
                    }
                    str += "v" + startVertex.getVertexIndex();
                    System.out.println(str);

                    cycleFound = true; 
                }
                else if (!blockedStatus[toVertex.getVertexIndex()])
                { // Else loop in DFS-manner
                    if (findCycles(toVertex, startVertex))
                    {
                        cycleFound = true; 
                    }
                }
            }
        }  

        if (cycleFound)
        {
            //UNBLOCK(v);
            blockedStatus[inVertex.getVertexIndex()] = false;
            for (Vertex unpromising : unpromisingVertices[inVertex.getVertexIndex()])
            {
                blockedStatus[unpromising.getVertexIndex()] = false;
            }
            unpromisingVertices[inVertex.getVertexIndex()].clear();
        }
        else
        {
            for (Edge edge : inVertex.getOutEdges())
            {
                Vertex toVertex = edge.getToVertex();
                if (toVertex.getVertexIndex() >= startVertex.getVertexIndex())
                { // Look only in forward direction to avoid cycle repetition
                    if (!unpromisingVertices[toVertex.getVertexIndex()].contains(inVertex))
                    {
                        unpromisingVertices[toVertex.getVertexIndex()].add(inVertex);
                    }
                }
            }
        }
        
        //unstack v;
        johnsonStack.remove(inVertex);
        
        return cycleFound;
        
//        for (ArrayList<Vertex> currSCC : maxSCCList)
//        {
//            for (Vertex vStart : currSCC)
//            {
//                vStart.resetEdgeCopies();  
//                while (vStart.getEdgeCopies().size() > 0)
//                {
//                    ArrayList<Integer> visitedColors = new ArrayList<Integer>();
//                    ArrayList<Vertex> visitedVertices = new ArrayList<Vertex>();
//                    ArrayList<Edge> visitedEdges = new ArrayList<Edge>();
//                    
//                    visitedVertices.add(vStart);  
//                    
//                    //findMinSCC(vStart, visitedColors, visitedVertices, visitedEdges);
//                    
//                    Edge edge = vStart.removeEdgeCopy(0);
//                    
//                    Vertex toVertex = edge.getToVertex();
//                    if (haveSameRoot(vStart, toVertex, currSCC))
//                    {
//                        if (!visitedVertices.contains(toVertex))
//                        {
//                            visitedVertices.add(toVertex);
//                            visitedEdges.add(edge);
//                            visitedColors.add(edge.getColor()); //behövs här???
//                        }
//                        else
//                        {
//                            System.out.println("MinSCC found");
//                        }
//                    }
//
//                }
//            }
//        }
//>>>>>>> 1.4
    }
    
    /**
     * Finds maximal strongly connected components (SCC) using the Tarjan's algorithm, 
     * see http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm.
     */
    private void tarjan(Vertex v)
    {
        v.setDepthIndex(tarjanIndex);
        v.setLowlinkIndex(tarjanIndex);
        
        tarjanIndex++;
        tarjanStack.add(v);
        
        for (Edge edge : v.getOutEdges())
        {
            Vertex toVertex = edge.getToVertex();
            if (toVertex.depthIndex == -1)
            {
                tarjan(toVertex);
                v.setLowlinkIndex(Math.min(v.getLowlinkIndex(), toVertex.getLowlinkIndex()));
            }
            else
            {
                v.setLowlinkIndex(Math.min(v.getLowlinkIndex(), toVertex.getDepthIndex()));
            }
        }
        
        if (v.getLowlinkIndex() == v.getDepthIndex())
        {
            System.out.println("Connected component (acc to Tarjan):");
            ArrayList<Vertex> currMaxSCCList = new ArrayList<Vertex>();
            
            Vertex toVertex = null;
            while (!v.equals(toVertex))
            {
                toVertex = tarjanStack.remove(tarjanStack.size()-1);
                System.out.println("v" + toVertex.getVertexIndex() + ": lowlink = v" + toVertex.getLowlinkIndex());
                currMaxSCCList.add(toVertex);
            }
            System.out.println("");
            maxSCCList.add(currMaxSCCList);
        }
    }
    
    /**
     * Checks whether the vertices v1 and v2 could belong to the same minimal SCC. 
     */
    private boolean haveSameRoot(Vertex v1, Vertex v2, ArrayList<Vertex> currSCC)
    {
        if (v1.getLowlinkIndex() != v2.getDepthIndex() && v1.getLowlinkIndex() != v2.getLowlinkIndex())
        {
            return false;
        }
        if (!currSCC.contains(v2) || !currSCC.contains(v1))
        {
            return false;
        }
        
        return true;
    }
    
//    private void findMinSCC(Vertex v, ArrayList<Integer> visitedColors, 
//            ArrayList<Vertex>visitedVertices, ArrayList<Edge> visitedEdges)
//    {
//        for (Edge edge : visitedEdges)
//        {
//            Vertex toVertex = edge.getToVertex();
//            if (!haveSameRoot(v, toVertex, ))
//            {
//                
//            }
//        }
//    }
    
//    /** Creates a new instance of BookingPairsGraphExplorer */
//    public BookingPairsGraphExplorer(int[][] bPairIndices)
//    {
//        // Store the vertices
//        vertices = bPairIndices; 
//        
//        // Initialize the lists of pointers to the neighboring vertices
//        neighbors = new ArrayList[vertices.length];
//        for (int i = 0; i < vertices.length; i++)
//        {
//            neighbors[i] = new ArrayList<int[]>();
//        }
//        
//        for (int i = 0; i < vertices.length - 1; i++)
//        {
//            for (int j = i + 1; j < vertices.length; j++)
//            {
//                if (bookSameZone(vertices[i], vertices[j]))
//                {
//                    neighbors[i].add(vertices[j]);
//                    neighbors[j].add(vertices[i]);
//                }
//            }
//        }
//    }
//    
//    public ArrayList<LabeledEvent[]> findConnectedCycles()
//    {
//        for (int i = 0; i < vertices.length; i++)
//        {
//            logger.warn("v: " + vertices[i][0] + " " + vertices[i][1] + " " + vertices[i][2]);
//            for (int[] nb : neighbors[i])
//            {
//                logger.info("nb: " + nb[0] + " " + nb[1] + " " + nb[2]);
//            }
//        }
//        
//        
//        
//        
//        //temp
//        return null;
//    }
//    
//    /**
//     * This method checks whether two vertices should be connected by an edge. 
//     * This should only be done if the they represent booking of at least one common zone. 
//     *
//     * @param   firstVertice    containing the indices of some robot and two zones, 
//     *                          booked by this robot in a sequence
//     * @param   secondVertice   containing the indices of some robot and two zones, 
//     *                          booked by this robot in a sequence
//     * @return  true if the vertices represent booking of at least one common zone.
//     */
//    private boolean bookSameZone(int[] firstVertice, int[] secondVertice)
//    {       
//        // If any of the zone booking indices match for different plants, the vertices should be connected
//        if (firstVertice[0] != secondVertice[0])
//        {
//            for (int i = 1; i < firstVertice.length; i++)
//            {
//                for (int j = 1; j < secondVertice.length; j++)
//                {
//                    if (firstVertice[i] == secondVertice[j])
//                    {
//                        return true;
//                    }   
//                }
//            }   
//        }
//        
//        return false;
//    }
}

class Vertex
{
    int vertexIndex;
    int depthIndex = -1;
    int lowlinkIndex = -1;
    ArrayList<Edge> outEdges;
    ArrayList<Edge> edgeCopies;
    
    public Vertex (int vertexIndex)
    {
        this.vertexIndex = vertexIndex;
        outEdges = new ArrayList<Edge>();
    }
    
    public Vertex(int vertexIndex, ArrayList<Edge> outgoingEdges)
    {
        this(vertexIndex);
        outEdges = outgoingEdges;
    }
    
    public int getVertexIndex() { return vertexIndex; }
    public int getDepthIndex() { return depthIndex; }
    public void setDepthIndex(int newDepthIndex) { depthIndex = newDepthIndex; }
    public int getLowlinkIndex() { return lowlinkIndex; }
    public void setLowlinkIndex(int newLowlinkIndex) { lowlinkIndex = newLowlinkIndex; }
    public ArrayList<Edge> getOutEdges() { return outEdges; }
    public void setOutEdges(ArrayList<Edge> outEdges) { this.outEdges = outEdges; }
    public void addOutEdge(Edge edge) { outEdges.add(edge); }
    public ArrayList<Edge> getEdgeCopies() { return edgeCopies; }
    public void resetEdgeCopies()
    {
        edgeCopies = new ArrayList<Edge>();
        for (Edge edge : outEdges)
        {
            edgeCopies.add(edge);
        }
    }
    public Edge removeEdgeCopy(int index) { return edgeCopies.remove(index); }
}

class Edge
{
    int edgeIndex;
    int color;
    int overlappingProperty;
    Vertex fromVertex;
    Vertex toVertex;
    
    public Edge(Vertex fromVertex, Vertex toVertex, int color, int overlappingProperty)
    {
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
        this.color = color;
        this.overlappingProperty = overlappingProperty;
    }
    
    public Vertex getFromVertex() { return fromVertex; }
    public Vertex getToVertex() { return toVertex; }
    public int getColor() { return color; }
    public int getOverlappingProperty() { return overlappingProperty; }
    public void setFromVertex(Vertex fromVertex) { this.fromVertex = fromVertex; }
    public void setToVertex(Vertex toVertex) { this.toVertex = toVertex; }
    public void setColor(int color) { this.color = color; }
    public void setOverlappingProperty(int overlappingProperty) { this.overlappingProperty = overlappingProperty; }
}