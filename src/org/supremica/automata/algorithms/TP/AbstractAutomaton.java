/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.util.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import org.supremica.automata.ExtendedAutomaton;

/**
 *
 * @author Mohammad Reza
 */
public class AbstractAutomaton {
    HashSet<String> nodes;
    HashSet<String> alphabet;
    HashSet<Edge> edges;
    HashSet<String> markednodes;
    HashSet<String> initialnodes;
    
    public AbstractAutomaton(){
        nodes = new HashSet<String>();
        alphabet = new HashSet<String>();
        edges = new HashSet<Edge>();    
    }
    
    public void build(ExtendedAutomaton efa){
        AddAllNodes(efa.getNodes());
        addAlphabet(efa.getAlphabet());
        AddAllTransitions(efa.getTransitions());
    }
    
    public void AddAllNodes(List<NodeProxy> nodes){
        for(NodeProxy n : nodes)
            if(!this.nodes.contains(n.getName()))
                this.nodes.add(n.getName());
    }
    
    public void addAlphabet(List<EventDeclProxy> alphabet){
        for(EventDeclProxy e : alphabet)
            if(!this.alphabet.contains(e.getName()))
                this.alphabet.add(e.getName());
    }
    
    public void AddAllTransitions(Collection<EdgeProxy> transitions){
        for(EdgeProxy tran : transitions){
            for(Proxy e:tran.getLabelBlock().getEventList()){
                String source = tran.getSource().getName();
                String target = tran.getTarget().getName();
                String event = ((SimpleIdentifierSubject)e).getName();
                String guard = "";
                String action = "";
                if(tran.getGuardActionBlock() != null && !tran.getGuardActionBlock().getGuards().isEmpty())
                    guard = tran.getGuardActionBlock().getGuards().get(0).toString();

                if(tran.getGuardActionBlock() != null && !tran.getGuardActionBlock().getActions().isEmpty())
                    action = tran.getGuardActionBlock().getActions().get(0).toString();
                
                this.edges.add(new Edge(source, target, event, guard, action));
            }
        }
    }

    public List<List<String>> getOutgoingTransitions(NodeProxy node){
        List<List<String>> out = new ArrayList<List<String>>();
        for(Edge t : edges)
            if(t.getSource().equals(node.getName()))
                out.add(t.toArray());
        return out;
    }

    public List<List<String>> getIncommingTransitions(NodeProxy node){
        List<List<String>> out = new ArrayList<List<String>>();
        for(Edge t:edges)
            if(t.getSource().equals(node.getName()))
                out.add(t.toArray());
        return out;
    }
    
    public List<List<String>> getNdEdges(){
        List<List<String>> ndEdges = new ArrayList<List<String>>();
        List<String> strEdges = edgesToString();
        List<String> duplicates = new ArrayList<String>(new DuplicatesOnlySet<String>(strEdges)) ;
        if(!duplicates.isEmpty())
            for(Edge edge : edges)
                if(duplicates.contains(edge.toString()))
                    ndEdges.add(edge.toArray());
        return ndEdges;
    }
    
    private boolean hasEdge(Edge edge){
        boolean itHas = false;
        for(Edge e:edges)
            if(e.toString().equals(edge.toString()))
                itHas = true;
        
        return itHas;
    }
    
    private List<String> edgesToString(){
        List<String> array = new ArrayList<String>();
        for(Edge edge:edges)
            array.add(edge.toString());
        return array;
    }
    
    class Edge {
        private String source;
        private String target;
        private String event;
        private String guard;
        private String action;
        public Edge(){
            source = "";
            target = "";
            event = "";
            guard = "";
            action = "";
        }

        public Edge(String source, String target, String event, String guard, String action){
            this.source = source;
            this.target = target;
            this.event = event;
            this.guard = guard;
            this.action = action;
        }

        public Edge(NodeProxy source, NodeProxy target, EventDeclProxy event, 
                SimpleExpressionProxy guard, BinaryExpressionProxy action){
            this(source.getName(), target.getName(), event.getName(), guard.toString(), action.toString());
        }

        public List<String> toArray(){
            return new ArrayList<String>(){{add(source);add(target);add(event);add(guard);add(action);}};
        }

        public String getSource(){
            return source;
        }

        public String getTarget(){
            return target;
        }

        public String getEvent(){
            return event;
        }

        public String getGuard(){
            return guard;
        }

        public String getAction(){
            return action;
        }
        
        @Override
        public String toString(){
            return source + "," + target + "," + event + "," + guard + "," + action;
        }

    }

    class DuplicatesOnlySet<E> extends HashSet<E>{
        private final Set<E> uniques = new HashSet<E>();
        public  DuplicatesOnlySet(Collection<? extends E> c){
            super.addAll(c);
        }
        
        @Override
        public boolean add(E e){               
            if(!this.uniques.add(e))
                return super.add(e);
        return false;
        }
    }
    
}
