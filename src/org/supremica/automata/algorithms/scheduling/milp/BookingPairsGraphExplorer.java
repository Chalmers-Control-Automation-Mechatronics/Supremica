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
    
    private ArrayList<int[]>[] edges = null;
    private boolean[] inComponent;
    private int[] rootIndex;
    private Vertice[] vertices;
    
    ArrayList<Vertice> tarjanStack = new ArrayList<Vertice>();
    int tarjanIndex = 0; 
    
    ArrayList<Vertice>[] unpromisingVertices;
    ArrayList<Edge> johnsonStack = new ArrayList<Edge>(); // We modify the stack used in Johnson to store edges.
    boolean[] verticeBlocked;
    boolean[] colorBlocked;
    
    int nrOfColors;
    
    /** The vertex-edge-vertex-edge-...-sequences of rainbow-cycles found in the current graph. */
    ArrayList<ArrayList<Edge>> cycleSequences;
    
    ArrayList<ArrayList<Vertice>> maxSCCList = new ArrayList<ArrayList<Vertice>>();
    
    public BookingPairsGraphExplorer(ArrayList<int[]>[] edges, int nrOfColors)
    {
        this.edges = edges;
        this.nrOfColors = nrOfColors;
        inComponent = new boolean[edges.length];
        rootIndex = new int[edges.length];
        
        vertices = new Vertice[edges.length];
        for (int i = 0; i < vertices.length; i++)
        {
            vertices[i] = new Vertice(i);
        }
        
        for (int i = 0; i < edges.length; i++)
        {
            for (int j = 0; j < edges[i].size(); j++)
            {
                vertices[i].addOutEdge(new Edge(vertices[i], vertices[edges[i].get(j)[0]], edges[i].get(j)));
            }
        }
    }
    
    /**
     * Starts the procedure of finding and returning all cycles in the booking pairs 
     * graph, that have rainbow-colored edges. Johnson's algorithm, see 
     * http://scitation.aip.org/getabs/servlet/GetabsServlet?prog=normal&id=SMJCAT000004000001000077000001&idtype=cvips&gifs=yes
     * B. D. Johnson, "Finding all the elementary cicruits of a directed graph",
     * is extended with a color check to find such cycles. 
     */
    public ArrayList<ArrayList<Edge>> enumerateAllCycles()
    {
        cycleSequences = new ArrayList<ArrayList<Edge>>();
        
        //TODO:
        // Möjligt att pre-processing med tarjan(all_vertices) och sedan utplockning av
        // mindre-SCC genom att sönderdela max-SCC i submängder med gemensam rotnod kan 
        // minska antalet cykler i findCyclesFrom. Vore det bra eller dåligt???
        //tarjan(vertices[0]);
        
        johnsonStack = new ArrayList<Edge>();
        unpromisingVertices =  new ArrayList[vertices.length];
        verticeBlocked = new boolean[vertices.length];
        colorBlocked = new boolean[nrOfColors];
        for (int i = 0; i < vertices.length - 1; i++)
        {
            // Reset the info about blocking and unpromising vertices
            for (int j = 0; j < verticeBlocked.length; j++)
            {           
                if (unpromisingVertices[j] == null)
                {
                    unpromisingVertices[j] = new ArrayList<Vertice>();
                }
                else
                {
                    unpromisingVertices[j].clear();
                }
                
                verticeBlocked[j] = false;
            }
            
            for (int j = 0; j < nrOfColors; j++)
            {
                colorBlocked[j] = false;                
            }
            
            findCyclesFrom(vertices[i], null);
        }  
        
        return cycleSequences;
    }
    
    /**
     * This method is called recursively and is used to take a step from inVertice 
     * in the search for a cycle having startVertice as its root.
     *
     * @param - inVerice is the vertice from which a search step is taken;
     * @param - startVertice is the root vertice of the path being currently explored;
     * @param - inEdge is the edge leading to the inVertice along the current path;
     * @return - true if a cycle from startVertice, including inVertice, has been found.
     */
    private boolean findCyclesFrom(Vertice startVertice, Edge inEdge)
    {        
        boolean cycleFound = false; 
        //johnsonStack.add(inVertice); // stack v;
        
        Vertice inVertice = startVertice;
        if (inEdge != null)
        {
            johnsonStack.add(inEdge);
            colorBlocked[inEdge.getColor()] = true;
            inVertice = inEdge.getToVertice();
        }
        verticeBlocked[inVertice.getVerticeIndex()] = true; // blocked(v) := true;
        
        for (Edge edge : inVertice.getOutEdges())
        {
            if (! colorBlocked[edge.getColor()])
            { // Only search for cycle if the color of the current edge has not been used yet         
                Vertice toVertice = edge.getToVertice();

                if (toVertice.getVerticeIndex() >= startVertice.getVerticeIndex())
                { // Look only in forward direction to avoid cycle repetition                
                    if (toVertice.equals(startVertice))
                    { // If the start Vertice if found again, we have a cycle
                        ArrayList newCycleSequence = new ArrayList<Edge>();
                        
                        //temp (output circuit)
//                        String str = "Johnson-circuit: ";
//                        str += "v" + startVertice.getVerticeIndex();
                        for (Edge e : johnsonStack)
                        {
//                            str += " - C" + e.getColor();
//                            str += " - v" + e.getToVertice().getVerticeIndex();
                            
                            newCycleSequence.add(e);
                        }
//                        str += " - C" + edge.getColor();
                        newCycleSequence.add(edge);
                        
                        cycleSequences.add(newCycleSequence);
                            
//                        System.out.println(str);
                        
                        cycleFound = true; 
                    }
                    else if (!verticeBlocked[toVertice.getVerticeIndex()])
                    { // Else loop in DFS-manner
                        if (findCyclesFrom(startVertice, edge))
                        {
                            cycleFound = true; 
                        }
                    }
                }
            }
        }  
        
        // Unblock the currently used color before returning, regardless of whether the cycle was found or not.
        if (inEdge != null)
        {
            colorBlocked[inEdge.getColor()] = false;
        }

        if (cycleFound)
        {
            //UNBLOCK(v);
            verticeBlocked[inVertice.getVerticeIndex()] = false;
            for (Vertice unpromising : unpromisingVertices[inVertice.getVerticeIndex()])
            {
                verticeBlocked[unpromising.getVerticeIndex()] = false;
            }
            unpromisingVertices[inVertice.getVerticeIndex()].clear();
        }
        else
        {
            for (Edge edge : inVertice.getOutEdges())
            {
                Vertice toVertice = edge.getToVertice();
                if (toVertice.getVerticeIndex() >= startVertice.getVerticeIndex())
                { // Look only in forward direction to avoid cycle repetition
                    if (!unpromisingVertices[toVertice.getVerticeIndex()].contains(inVertice))
                    {
                        unpromisingVertices[toVertice.getVerticeIndex()].add(inVertice);
                    }
                }
            }
        }
        
        //unstack v;
        //johnsonStack.remove(inVertice);
        if (inEdge != null)
        {
            johnsonStack.remove(inEdge);
        }
        
        return cycleFound;
        
//        for (ArrayList<Vertice> currSCC : maxSCCList)
//        {
//            for (Vertice vStart : currSCC)
//            {
//                vStart.resetEdgeCopies();  
//                while (vStart.getEdgeCopies().size() > 0)
//                {
//                    ArrayList<Integer> visitedColors = new ArrayList<Integer>();
//                    ArrayList<Vertice> visitedVertices = new ArrayList<Vertice>();
//                    ArrayList<Edge> visitedEdges = new ArrayList<Edge>();
//                    
//                    visitedVertices.add(vStart);  
//                    
//                    //findMinSCC(vStart, visitedColors, visitedVertices, visitedEdges);
//                    
//                    Edge edge = vStart.removeEdgeCopy(0);
//                    
//                    Vertice toVertice = edge.getToVertice();
//                    if (haveSameRoot(vStart, toVertice, currSCC))
//                    {
//                        if (!visitedVertices.contains(toVertice))
//                        {
//                            visitedVertices.add(toVertice);
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
     *
     * @param - v is the root vertice of the path being currently explored by the Tarjan's alg. 
     */
    private void tarjan(Vertice v)
    {
        v.setDepthIndex(tarjanIndex);
        v.setLowlinkIndex(tarjanIndex);
        
        tarjanIndex++;
        tarjanStack.add(v);
        
        for (Edge edge : v.getOutEdges())
        {
            Vertice toVertice = edge.getToVertice();
            if (toVertice.depthIndex == -1)
            {
                tarjan(toVertice);
                v.setLowlinkIndex(Math.min(v.getLowlinkIndex(), toVertice.getLowlinkIndex()));
            }
            else
            {
                v.setLowlinkIndex(Math.min(v.getLowlinkIndex(), toVertice.getDepthIndex()));
            }
        }
        
        if (v.getLowlinkIndex() == v.getDepthIndex())
        {
            System.out.println("Connected component (acc to Tarjan):");
            ArrayList<Vertice> currMaxSCCList = new ArrayList<Vertice>();
            
            Vertice toVertice = null;
            while (!v.equals(toVertice))
            {
                toVertice = tarjanStack.remove(tarjanStack.size()-1);
                System.out.println("v" + toVertice.getVerticeIndex() + ": lowlink = v" + toVertice.getLowlinkIndex());
                currMaxSCCList.add(toVertice);
            }
            System.out.println("");
            maxSCCList.add(currMaxSCCList);
        }
    }
    
    /**
     * Checks whether the vertices v1 and v2 could belong to the same minimal SCC. 
     */
    private boolean haveSameRoot(Vertice v1, Vertice v2, ArrayList<Vertice> currSCC)
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
    
//    private void findMinSCC(Vertice v, ArrayList<Integer> visitedColors, 
//            ArrayList<Vertice>visitedVertices, ArrayList<Edge> visitedEdges)
//    {
//        for (Edge edge : visitedEdges)
//        {
//            Vertice toVertice = edge.getToVertice();
//            if (!haveSameRoot(v, toVertice, ))
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

class Vertice
{
    int VerticeIndex;
    int depthIndex = -1;
    int lowlinkIndex = -1;
    ArrayList<Edge> outEdges;
    //ArrayList<Edge> edgeCopies; @Deprecated
    
    public Vertice (int VerticeIndex)
    {
        this.VerticeIndex = VerticeIndex;
        outEdges = new ArrayList<Edge>();
    }
    
    public Vertice(int VerticeIndex, ArrayList<Edge> outgoingEdges)
    {
        this(VerticeIndex);
        outEdges = outgoingEdges;
    }
    
    public int getVerticeIndex() { return VerticeIndex; }
    public int getDepthIndex() { return depthIndex; }
    public void setDepthIndex(int newDepthIndex) { depthIndex = newDepthIndex; }
    public int getLowlinkIndex() { return lowlinkIndex; }
    public void setLowlinkIndex(int newLowlinkIndex) { lowlinkIndex = newLowlinkIndex; }
    public ArrayList<Edge> getOutEdges() { return outEdges; }
    public void setOutEdges(ArrayList<Edge> outEdges) { this.outEdges = outEdges; }
    public void addOutEdge(Edge edge) { outEdges.add(edge); }
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

class Edge
{
    int edgeIndex;
    int color;
    int fromTic;
    int toTic;
    boolean bufferExists;
    Vertice fromVertice;
    Vertice toVertice;
    
    public Edge(Vertice fromVertice, Vertice toVertice, int[] edgeInfo) 
    {
        this.fromVertice = fromVertice;
        this.toVertice = toVertice;
        this.color = edgeInfo[1];
        this.fromTic = edgeInfo[2];
        this.toTic = edgeInfo[3];
        this.bufferExists = edgeInfo[4] != 0;
    }
    
    public Vertice getFromVertice() { return fromVertice; }
    public Vertice getToVertice() { return toVertice; }
    public int getColor() { return color; }
    public int getFromTic() { return fromTic; }
    public int getToTic() { return toTic; }
    public boolean getBufferExists() { return bufferExists; }
//    public void setFromVertice(Vertice fromVertice) { this.fromVertice = fromVertice; }
//    public void setToVertice(Vertice toVertice) { this.toVertice = toVertice; }
//    public void setColor(int color) { this.color = color; }
//    public void setFromTic(int tic) { this.fromTic = tic; }
//    public void setToTic(int tic) { this.toTic = tic; }
//    public void setBufferExists(boolean bufferExists) { this.bufferExists = bufferExists; }
}