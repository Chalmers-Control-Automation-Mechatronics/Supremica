public class logictestRunner{
    
    public static void main(String args[]) {

		short nrOfSignalsIn = 32;
		short nrOfSignalsOut = 32;

		// inputs to simulator [18]
		boolean[] in = { false, false, false, false, false, 
										false, 	false, false, false, false, 
										false, false, false, false, false, 
										false, false, false};

		// outputs from simulator [27]
		boolean[] out = { false, true, false, false, false, 
										 false, false,
				     true, false, false, false, false, false, false,
				     false, false, false, true, false, false, false,
				     false, false, true, true, false, false };
		
		LOGICtest ltest = new LOGICtest(in, out);
		
		ltest.run();
    }
 
}
