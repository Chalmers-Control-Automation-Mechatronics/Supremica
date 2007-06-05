/**
 * class to convert from DOP to EFA 
 */
package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;


import java.io.File;
import java.util.List;
import java.util.LinkedList;

import java.util.Iterator;

import org.supremica.external.iec61499fb2efa.*;
import org.supremica.manufacturingTables.xsd.rop.*;

/**
 * @author David Millares
 *
 */
public class DOPtoEFA extends DOPrelation{
	
	private static String outputFileName = "testModule.wmod";
	
	public DOPtoEFA(){}
	
	public static void createEFA(ROP rop){
		createEFA(rop,new File(outputFileName));
	}
	
	public static void createEFA(ROP rop, File f){
		
		String comment = rop.getComment(); 
		if(comment == null || comment.length() == 0){
			comment = "no comment";
		}
		
		String machine = rop.getMachine(); 
		if(machine == null || machine.length() == 0){
			machine = "no machine";
		}
		
		init(machine,comment);
		
		createEFA(lastState(),"",rop.getRelation());
		
		writeToFile(f);
	}
	
	private static void createEFA(String start, String end, Relation r){
		
		//check indata
		if(r == null){
			return;
		}
	
		if(start == null || start.length() == 0){
			start = newUniqueState();
		}
		if(end == null || end.length() == 0){
			end = newUniqueState();
		}
		
		Object o = null;
		List<String> egaList = new LinkedList<String>();
		
		String myLastState = start;
		String tmp;
			
		List activityList = r.getActivityRelationGroup();
		Iterator i = activityList.iterator();
		
		/* start build EFA
		 * go down in tre structure
		 * and handel activities and relations
		 */
		while(i.hasNext() || o != null){
			
			if(o == null){
				o = i.next();
			}
			
			/* ------------ activity code -------------- */
			while(o instanceof Activity){
				egaList.add(((Activity)o).getOperation());
					
				if(!i.hasNext()){
					o = null;
					break; //exit first while
				}else{	
					o = i.next();
				}
			}
			
			if(!egaList.isEmpty()){
				if(RelationType.SEQUENCE.equals(r.getType())){
					if(o == null){
						sequence(myLastState,end,egaList);
					}else{
						tmp = newUniqueState();
						sequence(myLastState,tmp,egaList);
						myLastState = tmp;
					}
				}else if(RelationType.ALTERNATIVE.equals(r.getType())){
					alternative(start,end,egaList);
				}
				
				egaList.clear();
			}
			/* ------------ end activity code ---------------- */
			
			
			
			/* --------------- relation code ----------------- */
			while(o instanceof Relation){
				
				//recursion
				if(RelationType.SEQUENCE.equals(r.getType())){
					if(i.hasNext()){
						tmp = newUniqueState();
						createEFA(myLastState, tmp, (Relation)o);
						myLastState = tmp;
					}else{
						createEFA(myLastState, end, (Relation)o);
					}
				}else if(RelationType.ALTERNATIVE.equals(r.getType())){
					createEFA(start, end, (Relation)o);
				}
				
				
				if(!i.hasNext()){
					o = null;
					break; //exit while
				}else{
					o = i.next();
				}
			}
			/* --------------- end  relation code ------------ */
			
		}
	}
	
	
	
	
	public static void createEFA(){
		
		ExtendedAutomata automata = new ExtendedAutomata("automata");
		
 		ExtendedAutomaton test = new ExtendedAutomaton("test", automata);
 		
 		test.addState("s0", true);
 		test.addState("s1");
 		
 		test.addIntegerVariable("var1", 0, 5, 0, null);
 		
 		automata.addEvent("e1", "controllable");
 		
 		test.addTransition("s0","s1","e1;e2;","var1 == 1","var1 = 4;");
 		
 		automata.addAutomaton(test);
 		
 		ExtendedAutomaton test2 = new ExtendedAutomaton("test2", automata);
 		test2.addState("s0", true);
 		test2.addState("s1");
 		test2.addIntegerVariable("var1", 0, 5, 0, null);
 		test2.addTransition("s0","s1","e1;e2;","var1 == 1","var1  = 4;");
 		automata.addAutomaton(test2);

		automata.writeToFile(new File(outputFileName));
	}
	
	public static Relation flattenSequence(Relation r){
		
		//check indata
		if(r == null ||
		   !RelationType.SEQUENCE.equals(r.getType())){
			return r;
		}
		
		//get element in sequence
		List list = r.getActivityRelationGroup();
		
		//loop over all element in sequence
		int i = 0;
		while(i < list.size()){
			Object o = list.get(i);
			//search for relation and sequence
			if(o instanceof Relation &&
				RelationType.SEQUENCE.equals(((Relation)o).getType())){
					list.remove(i);
					list.addAll(i,((Relation)o).getActivityRelationGroup());
			}else{
				i = i + 1; //next element
			}
		}
		return r;
	}
	
	
	/**
	 * Test main method to test this class
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*
		System.out.println("Try to create EFA");
		DOPtoEFA.createEFA();
		System.out.println("Done create EFA");
		*/
		
		
		ObjectFactory factory  = new ObjectFactory();
		
		
		//test activities
		Activity a = factory.createActivity();
		a.setOperation("a");
		
		Activity b = factory.createActivity();
		b.setOperation("b");
		
		Activity c = factory.createActivity();
		c.setOperation("c");
		
		Activity d = factory.createActivity();
		d.setOperation("d");
		
		Activity e = factory.createActivity();
		e.setOperation("e");
		
		Activity f = factory.createActivity();
		f.setOperation("f");
		
		Activity g = factory.createActivity();
		g.setOperation("g");
		
		Activity h = factory.createActivity();
		h.setOperation("h");
		
		Activity i = factory.createActivity();
		i.setOperation("i");
		
		Activity j = factory.createActivity();
		j.setOperation("j");
		
		
		
		//test relations
		Relation seq1 = factory.createRelation();
		seq1.setType(RelationType.SEQUENCE);
		
		Relation seq2 = factory.createRelation();
		seq2.setType(RelationType.SEQUENCE);
		
		Relation seq3 = factory.createRelation();
		seq3.setType(RelationType.SEQUENCE);
		
		
		Relation alt1 = factory.createRelation();
		alt1.setType(RelationType.ALTERNATIVE);
		
		Relation alt2 = factory.createRelation();
		alt2.setType(RelationType.ALTERNATIVE);
		
		Relation alt3 = factory.createRelation();
		alt3.setType(RelationType.ALTERNATIVE);
		
		//relation alt1..3 seq1..3
		//activity a b c d e f g h i j
		
		
		ROP rop = (new ObjectFactory()).createROP();
		rop.setMachine("machine");
		rop.setComment("Test sequence alternative");
		
		//create test relation
		
		//seq1
		seq1.getActivityRelationGroup().add(a);
		
		  alt1.getActivityRelationGroup().add(b);
		  alt1.getActivityRelationGroup().add(c);
		
		seq1.getActivityRelationGroup().add(alt1);
		
		seq1.getActivityRelationGroup().add(d);
		//end seq1
		
		//seq2
		seq2.getActivityRelationGroup().add(e);
		
		  alt2.getActivityRelationGroup().add(f);
		  alt2.getActivityRelationGroup().add(g);
		
		seq2.getActivityRelationGroup().add(alt2);
		
		seq2.getActivityRelationGroup().add(h);
		//end seq2
		
		//alt3
		alt3.getActivityRelationGroup().add(seq1);
		alt3.getActivityRelationGroup().add(i);
		alt3.getActivityRelationGroup().add(seq2);
		//end alt3
		
		seq3.getActivityRelationGroup().add(seq1);
		seq3.getActivityRelationGroup().add(seq2);
		
		seq3 = flattenSequence(seq3);
		
		//seq3.getActivityRelationGroup().add(c);
		//seq3.getActivityRelationGroup().add(d);
		
		rop.setRelation(seq3);

		System.out.println("Try to createEFA(rop)");
		createEFA(rop);
		System.out.println("Done createEFA(rop)");
		
	}

}
