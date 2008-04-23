package org.supremica.automata.algorithms.scheduling;

/**
 *
 * @author Avenir Kobetski
 */
public class ConnectedComponentEdge
{
    int edgeIndex;
    int color;
    int fromTic;
    int toTic;
    boolean bufferExists;
    ConnectedComponentVertice fromVertice;
    ConnectedComponentVertice toVertice;
    
    public ConnectedComponentEdge(ConnectedComponentVertice fromVertice, ConnectedComponentVertice toVertice, int[] edgeInfo) 
    {
        this.fromVertice = fromVertice;
        this.toVertice = toVertice;
        this.color = edgeInfo[1];
        this.fromTic = edgeInfo[2];
        this.toTic = edgeInfo[3];
        this.bufferExists = edgeInfo[4] != 0;
    }
    
    public ConnectedComponentVertice getFromVertice() { return fromVertice; }
    public ConnectedComponentVertice getToVertice() { return toVertice; }
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
