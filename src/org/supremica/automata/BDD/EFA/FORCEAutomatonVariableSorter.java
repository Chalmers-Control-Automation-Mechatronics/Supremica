package org.supremica.automata.BDD.EFA;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.util.BDD.graph.Edge;
import org.supremica.util.BDD.graph.Graph;
import org.supremica.util.BDD.graph.Node;

/**
 * Sort extended finite automata together with variables at the same time. 
 * The algorithm is the combination of PCG and FORCE
 * Some implementations are adopted from Arash's work. 
 * 
 * @author zhennan
 * @version 1.0
 * 
 */
public class FORCEAutomatonVariableSorter {
    
    private ExtendedAutomata origin;
    List<Object> variableOrdering; // the result variable ordering list to return
    List<String> variableOrderingNames;  // the result name list to return
    List<String> initialOrderingNames;
    
    
    public FORCEAutomatonVariableSorter(ExtendedAutomata originAutomata, List<String> variableOrderingNames) {
        this.origin = originAutomata;
        this.variableOrdering = new ArrayList<Object>(origin.size() + origin.getVars().size());
        this.variableOrderingNames = new ArrayList<String>(origin.size() + origin.getVars().size());
        this.initialOrderingNames = variableOrderingNames;
    }
    
    public void sort(){

        // Retrieve the automaton list and variable list from the original automata
        List<ExtendedAutomaton> automatonList = origin.getExtendedAutomataList();
        List<VariableComponentProxy> variableList = origin.getVars();

        List<AutVar> autVarList = new ArrayList<AutVar>(automatonList.size() + variableList.size());

        // Build the autVarList by iterating each efa and extracting variables from transitiions
        Map<String, AutVar> var2AutVar = new HashMap<String, AutVar>(variableList.size());

        for (ExtendedAutomaton efa : automatonList) {
            var2AutVar.put(efa.getName(), new AutVar(AutVar.TYPE_EFA, efa, efa.getName(), new HashSet<EventDeclProxy>(efa.getAlphabet())));
            for (EdgeProxy anEdge : efa.getTransitions()) {
                HashSet<EventDeclProxy> eventsOfCurrEdge = new HashSet<EventDeclProxy>();
                for (Iterator<Proxy> eventIterator = anEdge.getLabelBlock().getEventList().iterator(); eventIterator.hasNext();) {
                    String eventName = ((SimpleIdentifierSubject) eventIterator.next()).getName();
                    eventsOfCurrEdge.add(origin.eventIdToProxy(eventName));
                }
                if(anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getGuards() != null && 
                        !anEdge.getGuardActionBlock().getGuards().isEmpty()){
                    Set<VariableComponentProxy> variablesOfCurrEdge = efa.extractVariablesFromExpr(anEdge.getGuardActionBlock().getGuards().get(0));
                    for(VariableComponentProxy aVar: variablesOfCurrEdge){
                        String varName = aVar.getName();
                        if(!var2AutVar.containsKey(varName))
                            var2AutVar.put(varName, new AutVar(AutVar.TYPE_VAR, aVar, aVar.getName(), eventsOfCurrEdge));
                        else
                            var2AutVar.get(varName).getRelatedAlphabet().addAll(eventsOfCurrEdge);
                    }
                }
                if(anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getActions() != null && 
                        !anEdge.getGuardActionBlock().getActions().isEmpty()){
                    for(BinaryExpressionProxy anAction: anEdge.getGuardActionBlock().getActions()){
                        String variableName = anAction.getLeft().toString();
                        VariableComponentProxy aVar = origin.getVariableByName(variableName);
                        if(!var2AutVar.containsKey(variableName))
                            var2AutVar.put(variableName, new AutVar(AutVar.TYPE_VAR, aVar, aVar.getName(), eventsOfCurrEdge));
                        else
                            var2AutVar.get(variableName).getRelatedAlphabet().addAll(eventsOfCurrEdge);
                    }
                }
            }
        }
        
        
        for(int i = 1; i < initialOrderingNames.size() - 1; i ++){
            autVarList.add(var2AutVar.get(initialOrderingNames.get(i)));
        }
        
        // Build the graph from the autVarList which is necessary for FORCE algorithm of Arash's version
        Graph graph = buildGraph(autVarList);
        
        // Call FORCE algorithm to get the ordering
        FORCEAlgorithmAdapter algo = new FORCEAlgorithmAdapter(graph, false);
        algo.init();
        int [] ordering = algo.ordering();
        
        // Fill in these two field
        for(int i = 0; i < ordering.length; i++){
            variableOrdering.add(autVarList.get(ordering[i]).getOwner());
            variableOrderingNames.add(autVarList.get(ordering[i]).getLabel());
        }
    }
    
    private Graph buildGraph( List<AutVar> autVarList ){

        int size = autVarList.size();
        Graph graph = new Graph(false);
        HashMap<AutVar, Node> autVar2node = new HashMap<AutVar, Node>();

        // add the nodes:
        for (int i = 0; i < autVarList.size(); i++) {
            AutVar anAutvar = autVarList.get(i);
            Node n = new Node(i);
            n.owner = anAutvar.getOwner();
            n.label = anAutvar.getLabel();
            n.extra1 = i;
            autVar2node.put(anAutvar, n);
            graph.addNode(n);
        }

        // in order to add the edges, the weight matrix needs to be constructed from the autVarList
        int[][] weightMatrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            weightMatrix[i][i] = autVarList.get(i).getRelatedAlphabet().size();
            for (int j = 0; j < i; j++) {
                HashSet<EventDeclProxy> prevAutVarAlphbet = new HashSet<EventDeclProxy>(autVarList.get(j).getRelatedAlphabet());
                prevAutVarAlphbet.retainAll(autVarList.get(i).getRelatedAlphabet());
                weightMatrix[i][j] = weightMatrix[j][i] = prevAutVarAlphbet.size();
            }
        }

        // add the edges
        for (Enumeration<Node> e = graph.getNodes().elements(); e.hasMoreElements();) {
            
            Node n1 = e.nextElement();
            int j = n1.extra1;
            
            for (int i = 0; i < size; i++) {
                if (weightMatrix[j][i] > 0 && i != j) {
                    AutVar a2 = autVarList.get(i);
                    Node n2 = autVar2node.get(a2);
                    Edge ed = graph.addEdge(n1, n2);
                    ed.weight = weightMatrix[j][i];
                }
            }
        }

        return graph;
    }

    public List<Object> getVariableOrdering() {
        return variableOrdering;
    }

    public List<String> getVariableOrderingNames() {
        return variableOrderingNames;
    }
    
    /**
     * A helper class for EFA and variables
     */
    static class AutVar{
        
        static final int TYPE_EFA = 0;
        static final int TYPE_VAR = 1;
        
        private int type;
        private Object owner;
        private String label;
        private HashSet<EventDeclProxy> relatedAlphabet;
        
        public AutVar(int type, Object owner, String label, HashSet<EventDeclProxy> relatedAlphabet){
            this.type = type;
            this.owner = owner;
            this.label = label;
            this.relatedAlphabet = relatedAlphabet;
        }

        public Object getOwner() {
            return owner;
        }
        
        public String getLabel(){
            return label;
        }

        public HashSet<EventDeclProxy> getRelatedAlphabet() {
            return relatedAlphabet;
        }

        public int getType() {
            return type;
        }        
    }
    
}
