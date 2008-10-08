/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.IO;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;
import org.xml.sax.SAXException;

/**
 * Converts Waters modules to NuSMV files
 * 
 * Precondition: all automata should be flattened out, no complex nodes, foreach
 * events etc. Otherwise result will symply be nonsense (e.g. there is filter for 
 * SimpleIdentifierProxy, that silently discard all the rest)
 * 
 * This code is no more than a hack to convert Waters modules to NuSMV files.
 * The code is probably inefficient and unclean. It uses a lot of 
 * functional programming constructs in many places, and sometimes they are not
 * consistent with the rest of the code. I will probably clean it up after reading some
 * "functional programming in java" resources. The reason for all this mess is that
 * I wrote conversion specification in the functional style
 * 
 * 
 * 
 * <pre>
 * MODULE main
 * VAR
 *   for each automaton:
 *     variable name (q_automatonName)
 *     domain of the variable: list of all states of the automaton ({q_1_1, q_1_2})
 *   event: list of all events
 *   list of all variables and their domains
 * INIT
 *   conjunction(for each automaton: its state variable "=" its initial state)
 *   and for all variables their initial states (their initial predicates in case of
 *     waters' non-deterministic variables)
 * TRANS
 *   conjunction [ disjunction [ conjunction [stVar a `eq` sou, event `eq` ev, (next stVar a) `eq` des, guard, next varInAction `eq` action] | (sou, dest, ev, guard, action) <- arcs a] | a <- ats]
 * 
 * CTLSPEC AG EF ( conjunction [ disjunction [ stVar a `eq` s | s <- markedStates a] | a <- ats] ) 
 * 
 * 
 * </pre>
 *
 * Since there are no marked states, but there are only propositions like 
 * :accepting and :forbidden, it is very difficult to find single right way to 
 * encode all the properties we would like to express. For now we will limit it to 
 * :accepting proposition only for verification of non-blocking, but it have to be reconsidered.
 * 
 * 
 * 
 * @author voronov
 */
public class EFAToNuSMV {
    
    
    private final static String EVENT_VAR_NAME = "event";
    private final static String INDENT = "  ";
    //private final static CompilerOperatorTable compilerOpTable = CompilerOperatorTable.getInstance();

    public static void main(String[] args) throws JAXBException, SAXException, WatersUnmarshalException, IOException, URISyntaxException {
        //String name = "";
        //ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
	//ModuleSubject module = new ModuleSubject(name, null);
        ModuleProxyFactory moduleFactory      = ModuleElementFactory.getInstance();
        OperatorTable optable                 = CompilerOperatorTable.getInstance();
        JAXBModuleMarshaller moduleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
        ModuleProxy module                    = moduleMarshaller.unmarshal(new URI(args[0]));
        
        PrintWriter pw = new PrintWriter(System.out);
        (new EFAToNuSMV()).print(module, pw, Arrays.asList(new SpecPrinterNonBlocking()));
        pw.flush();
    }

    public void print(ModuleProxy m, PrintWriter pw, Collection<? extends SpecPrinter> specs) {
        pw.println("MODULE main");
        printVars(m, pw);
        printInits(m, pw);
        printTrans(m, pw);
        for(SpecPrinter s: specs)
            s.print(m, pw);
        pw.println();
        pw.flush();
    }
    /**
     * Precondition: ComponentList of the ModuleSubject have to be flattened out, 
     * i.e.  there should be only SimpleComponentProxy and VariableProxy, and no
     * InstanceProxy or ForeachComponentProxy
     * @param m
     * @param pw
     */
    private void printVars(ModuleProxy m, PrintWriter pw) {
        pw.println("VAR");
        /* Automata variables */
        for(SimpleComponentProxy sc: getAutomata(m)){
            pw.println(INDENT + declVarAsSet(varName(sc), valueNames(getStates(sc))));
        }
        /* Current event variable (maybe it should be input variable IVAR?) */
        pw.println(INDENT + declVarAsSet(EVENT_VAR_NAME, valueNames(m.getEventDeclList())));
        /* EFA variables */
        for(VariableComponentProxy v: getEFAVariables(m)){
            pw.println(INDENT + declVar(v.getName(), efaVarDomain(v)));
        }
    }
    private void printInits(ModuleProxy m, PrintWriter pw) {
        pw.println("INIT");
        Collection<String> elems = new ArrayList<String>();
        for(SimpleComponentProxy sc: getAutomata(m)){
            elems.add(exprEqualsSet(varName(sc), valueNames(getInitialStates(sc))));
        }
        for(VariableComponentProxy v: getEFAVariables(m)){
            elems.add(watersExprToSmvExpr(v.getInitialStatePredicate()));
        }        
        pw.println(INDENT + conjunction(elems));
    }
    
    /**
     * this will only work if all arcs has their transitions as SimpleIndentifierProxy
     * @param m
     * @param pw
     */
    private void printTrans(ModuleProxy m, PrintWriter pw) {
        pw.println("TRANS");
        Collection<String> allAutomata = new ArrayList<String>();
        for(SimpleComponentProxy sc: getAutomata(m)){            
            Collection<String> allEdges = new ArrayList<String>();
            allEdges.addAll(transitionConditions(sc));
            allEdges.addAll(stayConditions(sc, m));
            allAutomata.add(disjunction(allEdges));
        }
        String allAutomataExpression = conjunction(allAutomata);
        pw.println(INDENT + allAutomataExpression);
        
    }
    
    private Collection<String> transitionConditions(final SimpleComponentProxy sc){        
        return map(new Function<EdgeProxy, String>() {
            public String f(EdgeProxy edge) {
                return transitionCondition(sc, edge.getSource(), edge.getTarget(), getEvents(edge), edge.getGuardActionBlock());
            }
        }, sc.getGraph().getEdges());        
    }
    private Collection<String> stayConditions(final SimpleComponentProxy sc, final ModuleProxy m){        
        Collection<String> pred = map(new Function<SimpleNodeProxy, String>() {
            public String f(SimpleNodeProxy n) {
                return transitionCondition(sc, n, n, 
                        filterType(SimpleIdentifierProxy.class, n.getPropositions().getEventList()), 
                        null);
            }
        }, filterStatesWithPredicates(getStates(sc)));
        pred.add(stayAllCondition(sc, eventsNotInAlphabet(m, sc)));
        return pred;
    }
     
    private Collection<SimpleNodeProxy> filterStatesWithPredicates(Collection<SimpleNodeProxy> nodes){
        return filter(new Filter<SimpleNodeProxy>() {

            public Boolean f(SimpleNodeProxy n) {
                if(n.getPropositions()==null)
                    return false;
                else if(n.getPropositions().getEventList() == null)
                    return false;
                else 
                    return n.getPropositions().getEventList().size() > 0;                    
            }
        }, nodes);
    }
    
    private Collection<SimpleIdentifierProxy> eventsNotInAlphabet(ModuleProxy m, SimpleComponentProxy sc){
        
        final Collection<SimpleIdentifierProxy> events = getEvents(sc);
        events.addAll(getPropositions(sc));
        return filter(new Filter<SimpleIdentifierProxy>() {
            public Boolean f(SimpleIdentifierProxy value) {
                return !contains(events, value);
            }
        }, getEvents(m));        
    }
    private boolean contains(Collection<SimpleIdentifierProxy> ids, SimpleIdentifierProxy v){
        for(SimpleIdentifierProxy i: ids)
            if(i.getName().equals(v.getName()))
                return true;
        return false;
    }
    
    private Collection<SimpleIdentifierProxy> getEvents(SimpleComponentProxy sc){
        return unlines(map(new Function<EdgeProxy, Collection<SimpleIdentifierProxy>>() {
            public Collection<SimpleIdentifierProxy> f(EdgeProxy value) {
                return filterType(SimpleIdentifierProxy.class, value.getLabelBlock().getEventList());
            }
        }, sc.getGraph().getEdges()));
    }
    
    private Collection<SimpleIdentifierProxy> getEvents(ModuleProxy m){
        return filterType(SimpleIdentifierProxy.class, map(new Function<EventDeclProxy, IdentifierProxy>() {
            public IdentifierProxy f(EventDeclProxy value) {
                return value.getIdentifier();
            }
        }, m.getEventDeclList()));
    }
    
    private <T> Collection<T> unlines(Collection<Collection<T>> cs){
        Collection<T> res = new ArrayList<T>();
        for(Collection<T> c: cs)
            for(T e: c)
                res.add(e);
        return res;
    }
    
    private String transitionCondition
            ( SimpleComponentProxy sc
            , NodeProxy source
            , NodeProxy target
            , Collection<SimpleIdentifierProxy> events
            , GuardActionBlockProxy gab
            ){
        Collection<String> elems = new ArrayList<String>();
        elems.add(exprEquals(varName(sc), valueName(source)));
        elems.add(disjunctionOfEvents(events));
        elems.add(nextEquals(varName(sc), valueName(target)));
        if(gab != null){
            Collection<SimpleExpressionProxy> gs = gab.getGuards();
            if(gs!=null && !gs.isEmpty())
                elems.add(allGuards(gs));
            Collection<BinaryExpressionProxy> as = gab.getActions();
            if(as!=null && !as.isEmpty())
                elems.add(allActions(as));
        }        
        return conjunction(elems);
    }
    
    private String stayAllCondition(SimpleComponentProxy sc, Collection<SimpleIdentifierProxy> events){
        return conjunction(Arrays.asList(nextEquals(varName(sc), varName(sc)), disjunctionOfEvents(events)));
    }
    
    private Collection<SimpleIdentifierProxy> getEvents(EdgeProxy edge){
        return filterType(SimpleIdentifierProxy.class, edge.getLabelBlock().getEventList());
    }
    
    private String disjunctionOfEvents(Collection<SimpleIdentifierProxy> events){
        return disjunction(map(new Function<SimpleIdentifierProxy, String>() {
                    public String f(SimpleIdentifierProxy value) {
                        return exprEquals(EVENT_VAR_NAME, eventName(value));
                    }
                }, events));        
    }
    private String disjunctionOfEnabledEvents(EdgeProxy edge){
        return disjunction(map(new Function<SimpleIdentifierProxy, String>() {
                    public String f(SimpleIdentifierProxy value) {
                        return exprEquals(EVENT_VAR_NAME, eventName(value));
                    }
                }, getEvents(edge)));
    }
    
    private static String exprEquals(String e1, String e2){
        return "("+e1+"="+e2+")";
    }
    private static String declVar(String v, String e){
        return v + " : " + e + ";";
    }
    private static String declVarAsSet(String v, Collection<String> es){
        switch(es.size()){
            case 0: return "";
            case 1: return declVar(v, es.iterator().next());
            default: return declVar(v, "{" + printDelimited(", ", es) + "}");
        }        
    }
    private static String exprEqualsSet(String v, Collection<String> es){
        switch(es.size()){
            case 0: return "";
            case 1: return exprEquals(v, es.iterator().next());
            default: return exprEquals(v, "{" + printDelimited(", ", es) + "}");
        }        
    }
    private static String disjunction(Collection<String> elems){
        return junction("|", elems);
    }
    private static String conjunction(Collection<String> elems){
        return junction("&", elems);
    }
    private static String junction(String jun, Collection<String> elems){
        return "("+printDelimited(" " + jun + " ", elems) + ")";
    }
    private static String nextEquals(String variable, String expression){
        return exprEquals("next(" + variable +")", expression); 
    }
    private static String stmtNextAssign(String v, String e){
        return declVar("next(" + v + ")", e);
    }  
    
    
    private static interface Function <S,D>{ public D f(S value); }
    
    private static interface Filter<T> extends Function<T, Boolean> { 
        /**
         * returns true if element is OK
         * @param value value to check
         * @return      true if element is OK, false otherwise
         */ 
        public Boolean f(T value);
    }
    
    private static <T> Collection<T> filter(Filter<T> filter, Collection<T> collection) {
        Collection<T> res = new ArrayList<T>();
        for(T elem: collection)
            if(filter.f(elem))
                res.add(elem);
        return res;        
    }

    private static <S,D> Collection<D> filterType(Class<D> type, Collection<S> collection) {
        Collection<D> res = new ArrayList<D>();
        for(S elem: collection)
            if(type.isInstance(elem))
                res.add(type.cast(elem));
        return res;        
    }
    private static <S,D> Collection<D> map(Function<S,D> fun, Collection<S> sou){
        Collection<D> res = new ArrayList<D>();
        for(S elem: sou)
            res.add(fun.f(elem));
        return res;            
    }
            
    private static Collection<SimpleComponentProxy> getAutomata(ModuleProxy m){
        return filterType( SimpleComponentProxy.class,m.getComponentList());                 
    }
    private static Collection<VariableComponentProxy> getEFAVariables(ModuleProxy m){
        return filterType( VariableComponentProxy.class,m.getComponentList());                 
    }
    private static Collection<SimpleNodeProxy> getStates(SimpleComponentProxy sc){
        return filterType( SimpleNodeProxy.class,sc.getGraph().getNodes());                 
    }
    private static Collection<SimpleNodeProxy> getInitialStates(SimpleComponentProxy sc){
        return filter(
              new Filter<SimpleNodeProxy>() {
                  public Boolean f(SimpleNodeProxy t) { return t.isInitial(); }
              }, 
              getStates(sc)
        );
    }
    
    private Collection<String> edgeElements(EdgeProxy edge, SimpleComponentProxy sc){
        Collection<String> elems = new ArrayList<String>();
        elems.add(exprEquals(varName(sc), valueName(edge.getSource())));
        elems.add(disjunctionOfEnabledEvents(edge));
        elems.add(nextEquals(varName(sc), valueName(edge.getTarget())));
        GuardActionBlockProxy gab = edge.getGuardActionBlock();
        if(gab != null){
            Collection<SimpleExpressionProxy> gs = gab.getGuards();
            if(!gs.isEmpty())
                elems.add(allGuards(gs));
            Collection<BinaryExpressionProxy> as = edge.getGuardActionBlock().getActions();
            if(!as.isEmpty())
                elems.add(allActions(as));
        }        
        return elems;
    }
    
    private String getPropositionAsSelfLoop(SimpleComponentProxy sc, NodeProxy n, SimpleIdentifierProxy idf){
        return conjunction(Arrays.asList(
                exprEquals(varName(sc), valueName(n))
                , nextEquals(varName(sc), valueName(n))
                , exprEquals(EVENT_VAR_NAME, eventName(idf))
                ));
    }
    
    private Collection<EventDeclProxy> filterPropositions(Collection<EventDeclProxy> evs){
        return filter(new Filter<EventDeclProxy>() {
            public Boolean f(EventDeclProxy value) {
                return value.getKind()==EventKind.PROPOSITION;
            }
        }, evs);
    }
    private Collection<SimpleIdentifierProxy> getPropositions(SimpleComponentProxy sc){
        Collection<SimpleIdentifierProxy> res  = new ArrayList<SimpleIdentifierProxy>();
        for(NodeProxy n: sc.getGraph().getNodes()){
            res.addAll(filterType(SimpleIdentifierProxy.class, n.getPropositions().getEventList()));
        }
        return res;
    }
                   
    private String varName(SimpleComponentProxy sc){
        return "a_" + escape(sc.getName());
    }
    private String valueName(NamedProxy p){
        if(p instanceof SimpleNodeProxy){            
            return escape(((SimpleNodeProxy)p).getName());  // state constants will have no prefix
        } else if(p instanceof EventDeclProxy){
            return eventName((EventDeclProxy)p);
        } else {
            throw new IllegalArgumentException("Unexpected element type: " + p.getClass().toString());
        }            
    }    
    private static String eventName(EventDeclProxy p){
        return eventName(p.getName());
    }
    private static String eventName(SimpleIdentifierProxy p){
        return eventName(p.getName());
    }
    private static String eventName(String name){
        return "e_" + escape(name);
    }
            
    private static String escape(String s){
        return s.replace(":", "dblColon");
    }
    private <T extends NamedProxy> Collection<String> valueNames(Collection<T> es){
        return map(
                new Function<T, String>() {
                    public String f(T value){return valueName(value);}
                }, 
                es);
    }
    private static String printDelimited(String delim, Collection<String> c){
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for(String s: c){
            if(isFirst)
                isFirst = false;
            else
                sb.append(delim);
            sb.append(s);
        }        
        return sb.toString();
    }
        
    private String efaVarDomain(VariableComponentProxy v){    
        if(v.getType() instanceof BinaryExpressionProxy){
            BinaryExpressionProxy b = (BinaryExpressionProxy) v.getType();
            if(b.getOperator().getName().equals("..")){
                if(b.getLeft() instanceof IntConstantProxy){
                    IntConstantProxy ic = (IntConstantProxy) b.getLeft();
                    int i = ic.getValue();
                    if(b.getRight() instanceof IntConstantProxy){
                        IntConstantProxy jc = (IntConstantProxy) b.getRight();
                        int j = jc.getValue();
                        return "" + i + ".." + j; 
                    } else {
                        throw new IllegalArgumentException("expected int constant on the right of ..");
                    }
                } else {
                    throw new IllegalArgumentException("expected int constant on the left of ..");
                }
            } else {
                throw new IllegalArgumentException("unrecognized operator for domain range (expected ..): " + b.getOperator().getName());
            }
        } else {
            throw new IllegalArgumentException("unrecognized type of binary expression for domain (expected binary ..): " + v.getType().getClass().toString());
        }
    }
        
    private static String watersExprToSmvExpr(SimpleExpressionProxy exp){
        try {
            return (String)exp.acceptVisitor(new ExpressionToSmvVisitorTransConstraint());
        } catch (VisitorException ex) {
            Logger.getLogger(EFAToNuSMV.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("unprintable expression: ", ex);
        } 
    }
    
    private String allGuards(Collection<SimpleExpressionProxy> c){
        return conjunction(map(new Function<SimpleExpressionProxy, String>() {
            public String f(SimpleExpressionProxy value) {
                return watersExprToSmvExpr(value);
            }
        }, c));
    }
    private String allActions(Collection<BinaryExpressionProxy> c){
        return conjunction(map(new Function<BinaryExpressionProxy, String>() {
            public String f(BinaryExpressionProxy value) {
                return watersExprToSmvExpr(value);
            }
        }, c));
    }
    
    
    private static class ExpressionToSmvVisitorTransConstraint implements ModuleProxyVisitor {

        public Object visitAliasProxy(AliasProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
        private static Map<String, String> replaceable = new HashMap<String, String>();
        private static Collection<String> unsupported = new ArrayList<String>();
        static {
            replaceable.put("+", "+");
            replaceable.put("-", "-");
            replaceable.put("*", "*");
            replaceable.put("/", "/");
            replaceable.put("==", "=");
            //replaceable.put("=", "next()=");  // this replacement is only valid when converting Action Expression to NuSMV TRANS constraint
            replaceable.put("&", "&");
            replaceable.put("|", "|");
            replaceable.put(">", ">");
            replaceable.put("<", "<");
            replaceable.put(">=", ">=");
            replaceable.put("<=", "<=");
            replaceable.put("!=", "!="); 
            //replaceable.put("", "");
            
            unsupported.add("..");
            unsupported.add("+=");
            unsupported.add("-=");
        }

        public Object visitBinaryExpressionProxy(BinaryExpressionProxy proxy) throws VisitorException {
            String op = proxy.getOperator().getName();
                
            if(unsupported.contains(op)){
                throw new IllegalArgumentException("operator " + op + " not supported");
            } else if(replaceable.containsKey(op)){
                return proxy.getLeft().acceptVisitor(this) 
                        + replaceable.get(op) 
                        + proxy.getRight().acceptVisitor(this);
            } else if (op.equals("=")) {
                return nextEquals((String)proxy.getLeft().acceptVisitor(this), (String)proxy.getRight().acceptVisitor(this));
            } else {
                throw new IllegalArgumentException("unrecognized operator: " + op);
            }
        }

        public Object visitBoxGeometryProxy(BoxGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitColorGeometryProxy(ColorGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitComponentProxy(ComponentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitConstantAliasProxy(ConstantAliasProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitEdgeProxy(EdgeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitEnumSetExpressionProxy(EnumSetExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitEventAliasProxy(EventAliasProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitEventDeclProxy(EventDeclProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitEventListExpressionProxy(EventListExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitExpressionProxy(ExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitForeachComponentProxy(ForeachComponentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitForeachEventAliasProxy(ForeachEventAliasProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitForeachEventProxy(ForeachEventProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitForeachProxy(ForeachProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitGraphProxy(GraphProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitGroupNodeProxy(GroupNodeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitGuardActionBlockProxy(GuardActionBlockProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitIdentifiedProxy(IdentifiedProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitIdentifierProxy(IdentifierProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitIndexedIdentifierProxy(IndexedIdentifierProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitInstanceProxy(InstanceProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitIntConstantProxy(IntConstantProxy proxy) throws VisitorException {
            return Integer.toString(proxy.getValue());
        }

        public Object visitLabelBlockProxy(LabelBlockProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitLabelGeometryProxy(LabelGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitModuleProxy(ModuleProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitNodeProxy(NodeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitParameterBindingProxy(ParameterBindingProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitPlainEventListProxy(PlainEventListProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitPointGeometryProxy(PointGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitQualifiedIdentifierProxy(QualifiedIdentifierProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitSimpleComponentProxy(SimpleComponentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitSimpleExpressionProxy(SimpleExpressionProxy proxy) throws VisitorException {
            System.err.println("\n\nHello world!!\n\n");
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitSimpleIdentifierProxy(SimpleIdentifierProxy proxy) throws VisitorException {
            return proxy.getName();
        }

        public Object visitSimpleNodeProxy(SimpleNodeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitSplineGeometryProxy(SplineGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitUnaryExpressionProxy(UnaryExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitVariableComponentProxy(VariableComponentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitVariableMarkingProxy(VariableMarkingProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitProxy(Proxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitGeometryProxy(GeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitNamedProxy(NamedProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object visitDocumentProxy(DocumentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public static interface SpecPrinter {
        public void print(ModuleProxy m, PrintWriter pw);
    }
    public static class SpecPrinterNonBlocking implements SpecPrinter {

        private String markingEventName;
        public SpecPrinterNonBlocking(String markingEventName){
            this.markingEventName = markingEventName;
        }
        public SpecPrinterNonBlocking(){
            this(EventDeclProxy.DEFAULT_MARKING_NAME);
        }
        public void print(ModuleProxy m, PrintWriter pw) {
            pw.print("CTLSPEC AG(EF(");
            Collection<String> parts = new ArrayList<String>();
            
            /* first part - marking event is fired */
            parts.add(exprEquals(EVENT_VAR_NAME, eventName(markingEventName)));
            
            /* second part - all EFA varables have "marked" values */
            for(VariableComponentProxy v: getEFAVariables(m)){
                for(VariableMarkingProxy vm: v.getVariableMarkings()){
                    // for now we do only :accepting proposition
                    if(vm.getProposition() instanceof SimpleIdentifierProxy){
                        if(((SimpleIdentifierProxy)vm.getProposition()).getName().equals(markingEventName))
                            parts.add(exprEquals(v.getName(), watersExprToSmvExpr(vm.getPredicate())));
                    } else {
                        throw new IllegalArgumentException("unsupported identifier class: " + vm.getProposition().getClass().toString());
                    }
                }
            }
            pw.print(conjunction(parts));
            pw.println("))");
        }        
    }
}
