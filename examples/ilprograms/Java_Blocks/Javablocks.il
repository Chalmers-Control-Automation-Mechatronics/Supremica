JAVA_BLOCK OnDelayTimer
	VAR_IN_OUT
		tonIN : BOOL;
		tonPT : DINT;
		tonET : DINT;
		tonQ  : BOOL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK TON
	VAR_IN_OUT
		tonIN : BOOL;
		tonPT : DINT;
		tonET : DINT;
		tonQ  : BOOL;
	END_VAR
	VAR
		odt : OnDelayTimer;
	END_VAR
	LD tonIN
	ST odt.tonIN
	LD tonPT
	ST odt.tonPT
	CAL_JAVA odt
	LD odt.tonET
	ST tonET
	LD odt.tonQ
	ST tonQ
END_FUNCTION_BLOCK
PROGRAM TONtest
	VAR
		t : TON;
		b : BOOL;
	END_VAR
	LD DINT#3000
	ST t.tonPT
	LD TRUE
	ST t.tonIN
	LD t.tonQ
	ST b
	CAL PRINTLN("b= ",b)
END_PROGRAM
