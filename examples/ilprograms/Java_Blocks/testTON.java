public class testTON {
    public static void main(String[] args) {
	OnDelayTimer odt = new OnDelayTimer();
	
	odt.tonPT = 5000;
	odt.tonIN = true;
	for (;;) {
	    odt.tonPT = 5000;
	    odt.tonIN = true;
	    odt.run();
	    System.out.println("ut = " + Integer.toString(odt.tonET) + " " + Boolean.toString(odt.tonQ));
	}


    }
}
