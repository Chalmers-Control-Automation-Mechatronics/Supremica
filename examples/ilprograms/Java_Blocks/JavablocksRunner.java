public class JavablocksRunner {
	public static void main(String[] args) {
		boolean[] ain = new boolean[32];
		boolean[] aout = new boolean[32];
		ain[24] = true;
		//TONtest prog = new TONtest(ain,aout);
		//PRINTtest prog = new PRINTtest(ain,aout);
		PRINTLNtest prog = new PRINTLNtest(ain,aout);
		/*
		for (int i = 0; i<ain.length;i++){
			System.out.println(i +" "+ain[i] +" "+ aout[i]);
		}
		*/
		for(;;) {
		    //System.out.print("loop");
		    prog.run();
		}
		/*
		for (int i = 0; i<ain.length;i++){
			System.out.println(i +" "+ain[i] +" "+ aout[i]);
		}
		*/
	}
}


