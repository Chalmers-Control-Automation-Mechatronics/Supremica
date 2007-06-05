/**
 * this class holds function to build single relations
 * to EFA.
 * 
 */
package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.util.Iterator;
import java.util.List;

public class DOPrelation extends EFAbase{
	
	/**
	 * 
	 * 
	 * @param start is start state for sequence
	 * @param end is end state for sequence
	 * @param ega 
	 */
	protected static void sequence(String start, String end, List<String> egaList){
		
		//check indata
		if(egaList == null || egaList.isEmpty()){
			return;
		}
		
		//debugg
		System.out.println();
		System.out.println("sequence( " + start + ", " + end + ", " + egaList + ")");
		//debugg
		
		//add first state
		if(start == null || start.length() == 0){
			start = nextState();
		}
		if(!stateExist(start)){
			addState(start);
		}
		
		//special case
		if(egaList.size() == 1){
			//add last state
			if(end == null || end.length() == 0){
				end = nextState();
			}
			if(!stateExist(end)){
				addState(end);	
			}
			
			String[] ega = parseega(egaList.get(0));
			addTransition(start,end,ega[0],ega[1],ega[2]);
			
			//debugg
			System.out.println("sequence done");
			System.out.println();
			//debugg
			
			return;
		}
		
		//everything ok continue
		Iterator<String> i = egaList.iterator();
		String[] ega = parseega(i.next());
		
		addTransition(start,newUniqueState(),ega[0],ega[1],ega[2]);
		
		while(i.hasNext()){
			ega = parseega(i.next());
			if(i.hasNext()){
				addTransition(lastState(),newUniqueState(),ega[0],ega[1],ega[2]);
			}else{
				String tmp = lastState();
				
				//add last state
				if(end == null || end.length() == 0){
					end = nextState();
				}
				if(!stateExist(end)){
					addState(end);	
				}
				
				addTransition(tmp,end,ega[0],ega[1],ega[2]);
			}
		}
		
		//debugg
		System.out.println("sequence done");
		System.out.println();
		//debugg
	}
	
	protected static void alternative(String start, String end, List<String> egaList){
		//chech indata
		if(egaList == null || egaList.size() == 0){
			return;
		}
		
		//debugg
		System.out.println();
		System.out.println("alternative( " + start + ", " + end + ", " + egaList + ")");
		//debugg
		
		egaList = removeDouble(egaList);
		egaList = concatEvents(egaList);
		
		//add first state
		if(start == null || start.length() == 0){
			start = nextState();
		}
		if(!stateExist(start)){
			addState(start);
		}
		
		//add last state
		if(end == null || end.length() == 0){
			end = nextState();
		}
		if(!stateExist(end)){
			addState(end);	
		}
		
		//everything ok continue
		Iterator<String> i = egaList.iterator();
		String[] ega;
		
		while(i.hasNext()){
			ega = parseega(i.next());
			addTransition(start,end,ega[0],ega[1],ega[2]);
		}
		
		//debugg
		System.out.println("alternative done");
		System.out.println();
		//debugg
	}
	
	/* ------------------ega list code------------------------------ */
	
	/**
	 * ega event:guard:action
	 * ex a;:i==1:i=2 makes one transition with label a
	 * and guard i==1 and action i=2
	 * 
	 * @param ega
	 * @return String[]{EVENT,GUARD,ACTION}
	 */
	protected static String[] parseega(String ega){
		
		//check indata
		if(ega == null || ega.length() == 0){
			return new String[]{"","",""};
		}
		if(!ega.contains(":")){
			return new String[]{ega,"",""};
		}
		
		String[] tmp = new String[3];
		
		for(int i = 0; i < tmp.length; i++){
			if(ega.contains(":")){
				//take one
				tmp[i] = ega.substring(0,ega.indexOf(":"));
				ega = ega.substring(ega.indexOf(":")+1);
			}else{
				//this is last
				tmp[i] = ega;
				ega = "";
			}
		}
		
		//event must end whit ;
		if(!tmp[0].equals("") && !tmp[0].endsWith(";")){
			tmp[0] = tmp[0] + ";";
		}
		
		//action must end whit ;
		if(!tmp[2].equals("") && !tmp[2].endsWith(";")){
			tmp[2] = tmp[2] + ";";
		}
		
		return tmp;
	}
	
	/**
	 * removes doubles in a list of Strings.
	 * @param egaList
	 * @return
	 */
	protected static List<String> removeDouble(List<String> egaList){
		
		//check indata
		if(egaList == null){
			return null;
		}
		
		//debugg
		System.out.println();
		System.out.println("removeDouble( " + egaList + " )");
		//debugg
		
		for(int i = 0; i < egaList.size(); i++){
			
			int li = egaList.lastIndexOf(egaList.get(i));
			
			if(li != -1 && li != i){
				egaList.remove(li);
			}
		}
		
		//debugg
		System.out.println("removeDouble return " + egaList);
		System.out.println();
		//debugg
		
		return egaList;
	}
	
	
	/**
	 * concat all event whit same guard and action
	 * ex. {a:i==0:i=0, b:i==0:i=0} -> {a;b:i==0:i=0} 
	 * @param egaList
	 * @return
	 */
	protected static List<String> concatEvents(List<String> egaList){
		
		//check indata
		if(egaList == null){
			return null;
		}
		
		//debugg
		System.out.println();
		System.out.println("concatEvents( " + egaList + ")");
		//debugg
		
		//loop over all element. but no need for last.
		for(int i = 0; i < egaList.size()-1; i++){
			
			String event = egaList.get(i);
			String guardaction;
			
			//find pattern event and guardaction 
			if(event.contains(":")){
				guardaction = event.substring(event.indexOf(':'),event.length());
				event = event.substring(0,event.indexOf(':'));
			}else{
				guardaction = "";
			}
			
			//inner loop from next i to end of list
			//search for guardaction
			int ii = i+1;
			while(ii < egaList.size()){
				String next = egaList.get(ii);
				
				//check if same guard action
				if(next.contains(guardaction)){
					//add this event to existing events
					if(next.contains(":")){
						event = event + ";" + next.substring(0,next.indexOf(':'));
					}else{
						event = event + ";" + next;
					}
					//remove from list
					egaList.remove(ii);
				}else{
					//go to next
					ii = ii + 1;
				}
			}
			
			//replace and add new event
			egaList.remove(i);
			egaList.add(i, event + guardaction);
		}
		
		//debugg
		System.out.println("concatEvents return " + egaList);
		System.out.println();
		//debugg
		
		return egaList;
	}
}
