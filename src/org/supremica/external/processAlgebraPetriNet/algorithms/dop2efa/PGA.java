/**
 * Help class for convert dop to efa PGA stands for Process Guard and
 * Action.
 */
package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

/**
 *
 * @author David Millares
 *
 */
public class PGA {
	
	//String
	static final public String ONLY_START = "_START_";
	static final public String ONLY_STOP = "_STOP_";
	
	static final public String GUARD = "_GUARD_";
	static final public String ACTION = "_ACTION_";
	
	static final private String AND = "&";
	
	//
	private String process;
	private String startGuard, stopGuard;
	private String startAction, stopAction;
	
	private boolean only_start;
	private boolean only_stop;
	
	//constructors
	public PGA(){
		init("","","","","");
	}
	public PGA(String process){
		init(process,"","","","");
	}
	
	private void init(String p, String stag, String stog,
					  String staa, String stoa){
		
		only_start = false;
		only_stop = false;
		
		setProcess(p);
		
		setStartGuard(stag);
		setStopGuard(stog);
		
		setStartAction(staa);
		setStopAction(stoa);
	}
	
	//set process
	public void setProcess(String process){
		process = parseProcess(process);
		
		if(process.endsWith(";")){
			process = process.substring(0,process.length()-1);
		}
		
		this.process = process;
	}
	
	private String parseProcess(String process){
		String tmp = "";
		
		//take START or STOP
		
		if(process.startsWith(ONLY_START)){
			only_start = true;
			process = process.substring(ONLY_START.length());
		}else if(process.startsWith(ONLY_STOP)){
			only_stop = true;
			process = process.substring(ONLY_STOP.length());
		}
		
		//take guard
		
		if(process.startsWith(GUARD)){
			//remove first GUARD
			process = process.substring(GUARD.length());
			
			//search second GUARD
			while(process.length() > 0){
                
                //add first char to tmp
                tmp = tmp.concat(process.substring(0,1));
                
                //remove first char from process
                process = process.substring(1);
                
                //check if we have a GUARD
                if(process.startsWith(GUARD)){
        			break; //exit while
                }
            }
			//remove last GUARD
			process = process.substring(GUARD.length());
			
			//guard is in tmp
			if(only_start){
        		setStartGuard(tmp);
        	}else if(only_stop){
        		setStopGuard(tmp);
        	}else{
        		System.err.println("No start or stop");
        	}
		}
		
		//take action
		
		if(process.startsWith(ACTION)){
			//remove first ACTION
			process = process.substring(ACTION.length());
			
			//search second GUARD
			while(process.length() > 0){
                
                //add first char to tmp
                tmp = tmp.concat(process.substring(0,1));
                
                //remove first char from process
                process = process.substring(1);
                
                //check if we have a GUARD
                if(process.startsWith(ACTION)){
        			break; //exit while
                }
            }
			//remove last ACTION
			process = process.substring(ACTION.length());
			
			//guard is in tmp
			if(only_start){
        		setStartAction(tmp);
        	}else if(only_stop){
        		setStopAction(tmp);
        	}else{
        		System.err.println("No start or stop");
        	}
		}
		
		return process;
	}
	
	//set guard
	public void setStartGuard(String sg){
		startGuard = sg;
	}
	
	public void setStopGuard(String sg){
		stopGuard = sg;
	}
	
	//set action
	public void setStartAction(String sa){
		startAction = sa;
	}
	
	public void setStopAction(String sa){
		stopAction = sa; 
	}
	
	
	//add Process
	//not possible
	
	//add AND guard
	public void andStartGuard(String sg){
		if(sg.equals("")){
			return;
		}
		startGuard = startGuard + AND + sg;
	}
	
	public void andStopGuard(String sg){
		if(sg.equals("")){
			return;
		}
		stopGuard = stopGuard + AND + sg;
	}
	
	//add action
	public void andStartAction(String sa){
		if(sa.equals("")){
			return;
		}
		startAction = startAction + AND + sa;
	}
	
	public void addStopAction(String sa){
		if(!sa.equals("")){
			return;
		}
		stopAction = stopAction + AND + sa;
	}
	
	//get process
	public String getProcess(){
		return process;
	}
	
	//get guards
	public String getStartGuard(){
		return startGuard;
	}
	public String getStopGuard(){
		return stopGuard;
	}
	
	//get action
	public String getStartAction(){
		return startAction;
	}
	public String getStopAction(){
		return stopAction;
	}
	
	//get boolean
	public boolean getOnlyStart(){
		return only_start;
	}
	public boolean getOnlyStop(){
		return only_stop;
	}
}
