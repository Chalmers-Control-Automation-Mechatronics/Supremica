/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.util.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.*;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;

/**
 * 
 * @author shoaei
 * 
 */
public class AutomataTP {
    
    private final Logger logger = LoggerFactory.createLogger(IDE.class);        
    private final ExtendedAutomata exAutomata;
    private final ActionTimer TPTimer;
    private HashSet<EventDeclProxy> sharedEvents;
    private HashSet<EventDeclProxy> localEvents;
    private final ModuleSubjectFactory factory;
    
    public AutomataTP(ExtendedAutomata exAutomata){
        this.exAutomata = exAutomata;
        this.factory = ModuleSubjectFactory.getInstance();
        TPTimer = new ActionTimer();        
        localEvents = getLocalEventSet(exAutomata);
        sharedEvents = eventDifference(new HashSet<EventDeclProxy>(exAutomata.getUnionAlphabet()), localEvents);
    }
    
    public void compute(){
        HashSet<ExtendedAutomaton> EFAs = new HashSet<ExtendedAutomaton>();
        System.err.println("Transition Projections Computing ...");
        TPTimer.reset();
        TPTimer.start();
        for(ExtendedAutomaton efa:exAutomata){
            HashSet<EventDeclProxy> lEvents = eventIntersection(localEvents, new HashSet<EventDeclProxy>(efa.getAlphabet()));
            if(lEvents.isEmpty()){
                ExtendedAutomaton tp = new ExtendedAutomaton(exAutomata, efa.getComponent().clone());
                tp.getComponent().setIdentifier(new SimpleIdentifierSubject(efa.getName() + "_QUO"));
                EFAs.add(tp);
                continue;
            }
            
            if(lEvents.size() == efa.getAlphabet().size()){
                String name = "{";
                for (Iterator<NodeProxy> it = efa.getNodes().iterator(); it.hasNext();) {
                    NodeProxy node = it.next();
                    if(it.hasNext())
                        name+=node.getName() + ", ";
                    else
                        name+=node.getName();
                }
                name += "}";
                SimpleIdentifierSubject identifier = factory.createSimpleIdentifierProxy(efa.getName()+"_QUO");
                GraphSubject graph = factory.createGraphProxy();
                IndexedSetSubject<NodeSubject> nodes = graph.getNodesModifiable();
                final List<Proxy> propList = new LinkedList<Proxy>();
                if (!efa.getMarkedLocations().isEmpty())
                    propList.add(factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME));

                final PlainEventListSubject markingProposition = factory.createPlainEventListProxy(propList);
                SimpleNodeSubject node = factory.createSimpleNodeProxy(name, markingProposition, true, null, null, null);
                nodes.add(node);
                SimpleComponentSubject component = factory.createSimpleComponentProxy(identifier, efa.getKind(), graph);
                ExtendedAutomaton tp = new ExtendedAutomaton(exAutomata, component);
                EFAs.add(tp);
                continue;
                
            }
                
            AutomatonTP tp = new AutomatonTP(exAutomata, efa, lEvents);
            ExtendedAutomaton tpEFA = tp.compute();
            HashSet<EventDeclProxy> newSharedEvents = tp.getAllSharedEvents();
            addAllSharedEvent(newSharedEvents);
            EFAs.add(tpEFA);
        }
        
        for(ExtendedAutomaton efa:EFAs)
            exAutomata.addAutomaton(efa);
       
        TPTimer.stop();
    }
    
    public void setLocalEvents(HashSet<EventDeclProxy> localEvents){
        this.localEvents = localEvents;
        sharedEvents = eventDifference(new HashSet<EventDeclProxy>(exAutomata.getUnionAlphabet()), localEvents);
    }    
    
    public void setSharedEvents(HashSet<EventDeclProxy> sharedEvents){
        this.sharedEvents = sharedEvents;
        localEvents = eventDifference(new HashSet<EventDeclProxy>(exAutomata.getUnionAlphabet()), sharedEvents);
    }    
    
    private HashSet<EventDeclProxy> getLocalEventSet(ExtendedAutomata exAutomata){        
        HashSet<EventDeclProxy> locEvents = new HashSet<EventDeclProxy>();
        CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        for(ExtendedAutomaton efa1:exAutomata){            
            HashSet<EventDeclProxy> sigma1 = new HashSet<EventDeclProxy>(efa1.getAlphabet());
            HashSet<EventDeclProxy> sigma2 = new HashSet<EventDeclProxy>();
            HashSet<VariableComponentProxy> guardVar = new HashSet<VariableComponentProxy>();
            HashSet<VariableComponentProxy> actionVar = new HashSet<VariableComponentProxy>();
            for(EventDeclProxy ev:efa1.getAlphabet()){
                if(efa1.getGuardVariables(ev)!=null)
                    guardVar.addAll(efa1.getGuardVariables(ev));
                if(efa1.getActionVariables(ev)!=null)
                    actionVar.addAll(efa1.getActionVariables(ev));
            }
                
            for(ExtendedAutomaton efa2:exAutomata){
                if(!efa2.getName().equals(efa1.getName())){
                    for(EventDeclProxy ev:efa2.getAlphabet()){
                        sigma2.add(ev);
                        if(efa2.getGuardVariables(ev)!=null)
                            guardVar.addAll(efa2.getGuardVariables(ev));
                    }
                }
            }
            
            if(guardVar.isEmpty() && actionVar.isEmpty()){
                locEvents.addAll(eventDifference(sigma1, sigma2));
                continue;
            }
            
            HashSet<EventDeclProxy> uniqueEvents1 = (sigma2.isEmpty())?sigma1:eventDifference(sigma1, sigma2);
            outerloop:
            for(EventDeclProxy e1:uniqueEvents1){
                if(efa1.getGuardVariables(e1) != null && !efa1.getGuardVariables(e1).isEmpty()) 
                    continue outerloop;

                Set<VariableComponentProxy> actionVar1 = efa1.getActionVariables(e1);
                if(actionVar1 != null && !actionVar1.isEmpty()){
                    for(VariableComponentProxy a1:actionVar1)
                       if(guardVar.contains(a1)) 
                           continue outerloop;
                    
                    for(EdgeProxy t1:efa1.getTransitions())
                        for(Proxy te1:t1.getLabelBlock().getEventList())
                            if(getEvent(te1).getName().equals(e1.getName()) && t1.getGuardActionBlock() != null)
                                for(BinaryExpressionProxy expr:t1.getGuardActionBlock().getActions())
                                    if(!efa1.extractVariablesFromExpr(expr.getRight()).isEmpty() 
                                            || expr.getOperator().equals(cot.getIncrementOperator()) 
                                            || expr.getOperator().equals(cot.getDecrementOperator())) 
                                        continue outerloop;
                }
                locEvents.add(e1);
            }
        }
        return locEvents;
    }

    private HashSet<EventDeclProxy> eventIntersection(HashSet<EventDeclProxy> x, HashSet<EventDeclProxy> y){
        if(x.isEmpty() || y.isEmpty())
            return new HashSet<EventDeclProxy>();
        HashSet<EventDeclProxy> result = new HashSet<EventDeclProxy>(x);
        result.retainAll(y);
        return result;
    }
    
    private HashSet<EventDeclProxy> eventDifference(HashSet<EventDeclProxy> x, HashSet<EventDeclProxy> y){
        HashSet<EventDeclProxy> result = new HashSet<EventDeclProxy>();
        if(y.isEmpty())
            return x;
        if(x.isEmpty())
            return result;
        
        for (EventDeclProxy n:x)
            if(!y.contains(n))
                result.add(n);
        return result;
    }    

    private EventDeclProxy getEvent(Proxy event){
        return exAutomata.eventIdToProxy(((SimpleIdentifierSubject)event).getName());
    }
    
    public long getElapsedTime(){
        return TPTimer.elapsedTime();
    }

    public String getTimer(){
        return TPTimer.toString();
    }

    public HashSet<EventDeclProxy> getAllLocalEvents() {
        return localEvents;
    }

    private void addAllLocalEvent(HashSet<EventDeclProxy> events){
        for(EventDeclProxy e:events){
            localEvents.add(e);
            sharedEvents.remove(e);
        }
    }
    
    private void addAllSharedEvent(HashSet<EventDeclProxy> events){
        for(EventDeclProxy e:events){
            sharedEvents.add(e);
            localEvents.remove(e);
        }
    }
    
}
