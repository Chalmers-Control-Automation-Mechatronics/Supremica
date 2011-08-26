/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import org.supremica.automata.EFAState;
import org.supremica.automata.GuardActionLoc;
import org.supremica.automata.LocationEvent;

/**
 *
 * @author Sajed, Alexey, Zhennan
 */
public class EFAMonlithicReachability {

    private SimpleComponentProxy efa;
    private List<VariableComponentProxy> vars;
    private Map<LocationEvent, List<GuardActionLoc>> le2gals;
    Map<String,Integer> var2val;
    List<EventDeclProxy> alphabet;
    NodeProxy initLocation = null;
    Map<String,Integer> var2initialVal;
    Set<EFAState> reachableStates;

    public EFAMonlithicReachability(SimpleComponentProxy efa, List<VariableComponentProxy> vars, List<EventDeclProxy> alphabet){
        this.efa = efa;
        this.vars = vars;
        this.alphabet = alphabet;
        computeLe2GalMap();
    }

    public String createStateName(EFAState state)
    {
        String name = state.getLocation().getName()+"[";
        for(String var:state.getVar2val().keySet())
        {
            name+= (var+"="+state.getVar2val().get(var)+", ");
        }
        name = name.substring(0, name.length()-2);
        return name+"]";
    }

    public SimpleComponentSubject createEFA()
    {
        Map<String,VariableComponentProxy> varStr2VarCP = new HashMap<String, VariableComponentProxy>();
        for(VariableComponentProxy var:vars)
        {
            varStr2VarCP.put(var.getName(), var);
        }
        Set<EFAState> reachableStates = computeReachableStates();
        Set<NodeSubject> newStates = new HashSet<NodeSubject>();
        Map<EFAState,NodeProxy> state2node = new HashMap<EFAState, NodeProxy>();
        for(EFAState es:reachableStates)
        {            
            boolean isInitial = es.getLocation().equals(initLocation);
            if(isInitial)
            {
for2:           for(String var:es.getVar2val().keySet())
                {
                    if(!es.getVar2val().get(var).equals(var2initialVal.get(var)))
                    {
                        isInitial = false;
                        break for2;
                    }
                }
            }
            boolean isMarked = true;
for3:       for(String var:es.getVar2val().keySet())
            {
                var2val = es.getVar2val();
                for(VariableMarkingProxy vmp:varStr2VarCP.get(var).getVariableMarkings())
                {
                    if(eval(vmp.getPredicate()) == 0)
                    {
                        isMarked = false;
                        break for3;
                    }
                }
            }
            PlainEventListProxy props = new PlainEventListSubject();            
            if(isMarked)
                props = (PlainEventListProxy)es.getLocation().getPropositions().clone();

            SimpleNodeSubject newState = new SimpleNodeSubject(createStateName(es),props,isInitial,null,null,null);
            state2node.put(es, newState);
            newStates.add(newState);
        }

        Set<EdgeSubject> newEdges = new HashSet<EdgeSubject>();
        for(EFAState es:reachableStates)
        {
            for(EventDeclProxy currEvent:alphabet)
            {
                for(EFAState currState: next(es, currEvent.getName()))
                {
                    List<SimpleIdentifierSubject> events = new ArrayList<SimpleIdentifierSubject>();
                    events.add(new SimpleIdentifierSubject(currEvent.getName()));
                    EdgeSubject edge = new EdgeSubject(state2node.get(es), state2node.get(currState), new LabelBlockSubject(events, null), new GuardActionBlockSubject(), null, null, null);
                    newEdges.add(edge);
                }
            }
        }

        GraphSubject graph = new GraphSubject(true, null, newStates, newEdges);
        return new SimpleComponentSubject(new SimpleIdentifierSubject(efa.getName()+"_reachabilityGraph"), efa.getKind(), graph);
    }

    public Set<EFAState> computeReachableStates()
    {
        reachableStates = new HashSet<EFAState>();
        Map<String,Integer> var2val = new HashMap<String,Integer>();
        for(VariableComponentProxy var:vars)
        {
            int i = Integer.parseInt(((BinaryExpressionProxy)(var.getInitialStatePredicate())).getRight().toString());
            var2val.put(var.getName(), i);
        }
        var2initialVal = new HashMap<String, Integer>(var2val);

        for(NodeProxy loc:efa.getGraph().getNodes())
        {                      
            if(new StringTokenizer(loc.toString(), " ").nextToken().equals("initial"))
            {
                initLocation = loc;
                break;
            }
        }
        if(initLocation == null)
            throw new IllegalArgumentException("The EFA has not an initial location!");

        EFAState initState = new EFAState(initLocation, var2val);

        Set<EFAState> currentReachableStates = new HashSet<EFAState>();
        currentReachableStates.add(initState);
        Set<EFAState> freshStates = new HashSet<EFAState>();
        freshStates.add(initState);

        while(freshStates.size() > 0)
        {
            Set<EFAState> nextStates = new HashSet<EFAState>();
            for(EFAState currState:freshStates)
            {                
                for(EventDeclProxy currEvent:alphabet)
                {
                    nextStates.addAll(next(currState, currEvent.getName()));
                }
            }
            nextStates.removeAll(currentReachableStates);
            freshStates = nextStates;
            currentReachableStates.addAll(freshStates);
        }

        return currentReachableStates;

    }

    public List<EFAState> next(EFAState aState, String aEvent) {

        reachableStates.add(aState);
        LocationEvent le = new LocationEvent(aState.getLocation(), aEvent);
        List<EFAState> result = new ArrayList<EFAState>();        
        if (le2gals.containsKey(le)) {
            List<GuardActionLoc> gals = le2gals.get(le);
            for (GuardActionLoc gal : gals) 
            {
                var2val = aState.getVar2val();                
                if (evaluateGuard(gal.getGuard()))
                {
                    EFAState eState = new EFAState(gal.getLocation(), evaluateActions(gal.getActions()));
                    EFAState efaState = extractState(eState);
                    reachableStates.add(efaState);
                    result.add(efaState);
                }
            }
        }
 
        return result;
    }

    public EFAState extractState(EFAState state)
    {
        for(EFAState st:reachableStates)
        {
            if(state.equals(st))
                return st;
        }

        return state;
    }

    public Map<LocationEvent, List<GuardActionLoc>> getLe2gal() {
        return le2gals;
    }

    private void computeLe2GalMap() {
        le2gals = new HashMap<LocationEvent, List<GuardActionLoc>>();
        for (EdgeProxy anEdge : efa.getGraph().getEdges()) {
            for (Proxy anEvent : anEdge.getLabelBlock().getEventList()) {
                LocationEvent tmpLocEvent = new LocationEvent(anEdge.getSource(), ((SimpleIdentifierProxy)anEvent).getName());
                GuardActionLoc gal = new GuardActionLoc(anEdge.getGuardActionBlock(), anEdge.getTarget());
                if(!le2gals.containsKey(tmpLocEvent))
                    le2gals.put(tmpLocEvent, new ArrayList<GuardActionLoc>());
                le2gals.get(tmpLocEvent).add(gal);
            }
        }
    }

    private Map<String,Integer> evaluateActions(List<BinaryExpressionProxy> actions)
    {
        Map<String,Integer> tmpVar2val = new HashMap<String, Integer>(var2val);
        for(BinaryExpressionProxy action : actions)
        {
            if(action.getOperator().equals(CompilerOperatorTable.getInstance().getAssignmentOperator()))
            {
                tmpVar2val.put(((SimpleIdentifierProxy)action.getLeft()).getName(), eval(action.getRight()));
            }           
            else
            {
                throw new IllegalArgumentException("Binary operator "+action.getOperator()+" not known or not implemented yet!");
            }

        }
        return tmpVar2val;
    }

    private boolean evaluateGuard(SimpleExpressionProxy guard)
    {
        return ( eval(guard) != 0);
    }

    int eval(SimpleExpressionProxy expr)
    {

        if(expr instanceof UnaryExpressionProxy)
        {
            UnaryExpressionProxy unExpr = (UnaryExpressionProxy)expr;
            if(unExpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotOperator()))
            {
                if(eval(unExpr.getSubTerm()) == 0)
                    return 1;
                else
                    return 0;
            }
            else if(((UnaryExpressionProxy)expr).getOperator().equals(CompilerOperatorTable.getInstance().getUnaryMinusOperator()))
            {
                return 0 - eval(unExpr.getSubTerm());
            }
            else
            {
                throw new IllegalArgumentException("Type of operator not known!");
            }

        }
        else if(expr instanceof BinaryExpressionProxy)
        {
            BinaryExpressionProxy bexpr = (BinaryExpressionProxy)expr;
            if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getAndOperator()))
            {
                if(eval(bexpr.getLeft()) != 0 &&  eval(bexpr.getRight())!=0)
                    return 1;
                else
                    return 0;

            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getOrOperator()))
            {
                if(eval(bexpr.getLeft()) != 0 ||  eval(bexpr.getRight())!=0)
                    return 1;
                else
                    return 0;

            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getModuloOperator()))
            {
                return eval(bexpr.getLeft()) % eval(bexpr.getRight());
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getTimesOperator()))
            {
                return eval(bexpr.getLeft()) * eval(bexpr.getRight());
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getMinusOperator()))
            {
                return eval(bexpr.getLeft()) - eval(bexpr.getRight());
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getPlusOperator()))
            {
                return eval(bexpr.getLeft()) + eval(bexpr.getRight());
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getEqualsOperator()))
            {
                if(eval(bexpr.getLeft()) == eval(bexpr.getRight()))
                    return 1;
                else
                    return 0;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotEqualsOperator()))
            {
                if(eval(bexpr.getLeft()) != eval(bexpr.getRight()))
                    return 1;
                else
                    return 0;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterThanOperator()))
            {
                if(eval(bexpr.getLeft()) > eval(bexpr.getRight()))
                    return 1;
                else
                    return 0;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterEqualsOperator()))
            {
                if(eval(bexpr.getLeft()) >= eval(bexpr.getRight()))
                    return 1;
                else
                    return 0;

            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessThanOperator()))
            {
                if(eval(bexpr.getLeft()) < eval(bexpr.getRight()))
                    return 1;
                else
                    return 0;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessEqualsOperator()))
            {
                if(eval(bexpr.getLeft()) <= eval(bexpr.getRight()))
                    return 1;
                else
                    return 0;

            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getDivideOperator()))
            {
                return (eval(bexpr.getLeft()) / eval(bexpr.getRight()));
            }
            else
            {
                throw new IllegalArgumentException("Binary operator is not known!");
            }

        }
        else if(expr instanceof SimpleIdentifierProxy)
        {
            return var2val.get(((SimpleIdentifierProxy)expr).getName());
        }
        else if(expr instanceof IntConstantProxy)
        {
            return ((IntConstantProxy)expr).getValue();
        }

        throw new IllegalArgumentException("Type of expression not known!");
    }


}
