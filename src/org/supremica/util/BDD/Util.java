

package org.supremica.util.BDD;

public class Util {


    private static int free_memory; // free memory in MB
    private static final long MIN_NODES = 10000, MAX_NODES = 8000000;

    static {
	Runtime rt = Runtime.getRuntime();
	free_memory = (int)(rt.freeMemory() / (1024* 1024));
    }
    public static int suggest_nodecount(Automata a) {
	// I dont know why and I dont know how... :)
	int size = a.getVariableCount();
	double d = free_memory * 10000 * Math.log(size + 1) / Math.log(2);
	int nodes = (int) Math.max(Math.min( d, MAX_NODES), MIN_NODES);
	// System.out.println("NODES="+nodes);
	return nodes;
    }

    public static int log2ceil(int num) {
	if(num <= 1) return 1; // this is the minimum!

	for(int i = 0; i < 32; i++) 
	    if( (1 << i) >= num)  return i;	    	
	
	System.err.println("Cannot log2 " + num);
	System.exit(20);
	return 0; // damn compiler :)

    }

    public static int getNumber(BDDAutomata manager,
				int [] vars, int number) {
	int ret = manager.getOne();
	manager.ref(ret);

	for(int i = 0; i < vars.length; i++) {	   
	    ret = manager.andTo(ret,
			      ((number & (1 << i)) != 0)
			      ? vars[i] 
			      : manager.not(vars[i])
			      );	    

	    /*
	    System.out.println(i + " --> " + manager.internal_refcount(ret) + 
	    		       " / " + manager.internal_refcount(vars[i]));	    

	    if( manager.internal_refcount(ret) == 0) {
		System.out.println("(number & (1 << i)) == " + 
				   (number & (1 << i)));

		int not = manager.not(vars[i]);

		manager.print(vars[i]);
		manager.print(not);
		System.out.println("ID: " + vars[i] + " / " + not + " -- " +
				   "REFS: " + 
				   manager.internal_refcount(vars[i]) + " / " +
				   manager.internal_refcount(not));

		System.exit(20);
	    }
	    */
	}          
	return ret;	
    }
				
}
