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
    
    public BookingPairsGraphExplorer(ArrayList<int[]>[] edges)
    {
        this.edges = edges;
        inComponent = new boolean[edges.length];
        rootIndex = new int[edges.length];
        
        //TODO: Implementera Tarjan's algorithm
        
        for (int i = 0; i < edges.length; i++)
        {
            for (int j = 0; j < edges[i].size(); j++)
            {
                logger.info("There is an edge between Z" + i + " and Z" + 
                        edges[i].get(j)[0] + " with color P" + edges[i].get(j)[1] + 
                        " and overlapping_property = " + edges[i].get(j)[4]);
            }
        }
    }
    
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
