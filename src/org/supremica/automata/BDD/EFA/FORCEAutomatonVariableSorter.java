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

    private final ExtendedAutomata origin;
    List<Object> variableOrdering; // the result variable ordering list to return
    List<String> variableOrderingNames;  // the result name list to return
    List<String> initialOrderingNames;


    public FORCEAutomatonVariableSorter(final ExtendedAutomata originAutomata, final List<String> variableOrderingNames) {
        this.origin = originAutomata;
        this.variableOrdering = new ArrayList<Object>(origin.size() + origin.getVars().size());
        this.variableOrderingNames = new ArrayList<String>(origin.size() + origin.getVars().size());
        this.initialOrderingNames = variableOrderingNames;
    }

    public void sort(){

        // Retrieve the automaton list and variable list from the original automata
        final List<ExtendedAutomaton> automatonList = origin.getExtendedAutomataList();
        final List<VariableComponentProxy> variableList = origin.getVars();

        final List<AutVar> autVarList = new ArrayList<AutVar>(automatonList.size() + variableList.size());

        // Build the autVarList by iterating each efa and extracting variables from transitiions
        final Map<String, AutVar> var2AutVar = new HashMap<String, AutVar>(variableList.size());

        for (final ExtendedAutomaton efa : automatonList) {
            var2AutVar.put(efa.getName(), new AutVar(AutVar.TYPE_EFA, efa, efa.getName(), new HashSet<EventDeclProxy>(efa.getAlphabet())));
            for (final EdgeProxy anEdge : efa.getTransitions()) {
                final HashSet<EventDeclProxy> eventsOfCurrEdge = new HashSet<EventDeclProxy>();
                for (final Iterator<Proxy> eventIterator = anEdge.getLabelBlock().getEventIdentifierList().iterator(); eventIterator.hasNext();) {
                    final String eventName = ((SimpleIdentifierSubject) eventIterator.next()).getName();
                    eventsOfCurrEdge.add(origin.eventIdToProxy(eventName));
                }
                if(anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getGuards() != null &&
                        !anEdge.getGuardActionBlock().getGuards().isEmpty()){
                    @SuppressWarnings("deprecation")
                    final
                    Set<VariableComponentProxy> variablesOfCurrEdge = efa.extractVariablesFromExpr(anEdge.getGuardActionBlock().getGuards().get(0));
                    for(final VariableComponentProxy aVar: variablesOfCurrEdge){
                        final String varName = aVar.getName();
                        if(!var2AutVar.containsKey(varName))
                            var2AutVar.put(varName, new AutVar(AutVar.TYPE_VAR, aVar, aVar.getName(), eventsOfCurrEdge));
                        else
                            var2AutVar.get(varName).getRelatedAlphabet().addAll(eventsOfCurrEdge);
                    }
                }
                if(anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getActions() != null &&
                        !anEdge.getGuardActionBlock().getActions().isEmpty()){
                    for(final BinaryExpressionProxy anAction: anEdge.getGuardActionBlock().getActions()){
                        final String variableName = anAction.getLeft().toString();
                        final VariableComponentProxy aVar = origin.getVariableByName(variableName);
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
        final Graph graph = buildGraph(autVarList);

        // Call FORCE algorithm to get the ordering
        final FORCEAlgorithmAdapter algo = new FORCEAlgorithmAdapter(graph, false);
        algo.init();
        final int [] ordering = algo.ordering();

        // Fill in these two field
        for(int i = 0; i < ordering.length; i++){
            variableOrdering.add(autVarList.get(ordering[i]).getOwner());
            variableOrderingNames.add(autVarList.get(ordering[i]).getLabel());
        }
    }

    private Graph buildGraph( final List<AutVar> autVarList ){

        final int size = autVarList.size();
        final Graph graph = new Graph(false);
        final HashMap<AutVar, Node> autVar2node = new HashMap<AutVar, Node>();

        // add the nodes:
        for (int i = 0; i < autVarList.size(); i++) {
            final AutVar anAutvar = autVarList.get(i);
            final Node n = new Node(i);
            n.owner = anAutvar.getOwner();
            n.label = anAutvar.getLabel();
            n.extra1 = i;
            autVar2node.put(anAutvar, n);
            graph.addNode(n);
        }

        // in order to add the edges, the weight matrix needs to be constructed from the autVarList
        final int[][] weightMatrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            weightMatrix[i][i] = autVarList.get(i).getRelatedAlphabet().size();
            for (int j = 0; j < i; j++) {
                final HashSet<EventDeclProxy> prevAutVarAlphbet = new HashSet<EventDeclProxy>(autVarList.get(j).getRelatedAlphabet());
                prevAutVarAlphbet.retainAll(autVarList.get(i).getRelatedAlphabet());
                weightMatrix[i][j] = weightMatrix[j][i] = prevAutVarAlphbet.size();
            }
        }

        // add the edges
        for (final Enumeration<Node> e = graph.getNodes().elements(); e.hasMoreElements();) {

            final Node n1 = e.nextElement();
            final int j = n1.extra1;

            for (int i = 0; i < size; i++) {
                if (weightMatrix[j][i] > 0 && i != j) {
                    final AutVar a2 = autVarList.get(i);
                    final Node n2 = autVar2node.get(a2);
                    final Edge ed = graph.addEdge(n1, n2);
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

        private final int type;
        private final Object owner;
        private final String label;
        private final HashSet<EventDeclProxy> relatedAlphabet;

        public AutVar(final int type, final Object owner, final String label, final HashSet<EventDeclProxy> relatedAlphabet){
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
