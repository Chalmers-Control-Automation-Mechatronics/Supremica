(*This program was run successfully by Anders on thursday 25 April 09:00*)
PROGRAM Jmptest
   VAR
      A : BOOL; 
   END_VAR
(*
	CAL PRINTLN("Error")
	CAL PRINTLN("OK")
*)
CAL PRINTLN("Basic tests....")

(*Test: JMP*)
(*===========*)
    LD TRUE
    JMP e1
        CAL PRINTLN("Error")
e1: 
    CAL PRINTLN("JMP OK")
    (*Check that TRUE remains in resultregister*)
    ST A
	CAL PRINTLN("OK if true:",A)
    
(*Test: JMPC*)
    (*True should be on the stack*)
    JMPC e2
        CAL PRINTLN("Error: JMPC")
e2:	
    ST A
	CAL PRINTLN("OK if true:",A)

(*Test: JMPCN*)
    (*True should be on the stack*)
    JMPCN e3
	CAL PRINTLN("JMPCN OK") 	
e3:    
(*-------------------------*)
(*Reversed tests*)
(*Expected output:
	JMPC  OK
	JMPCN OK
*)
CAL PRINTLN("Reversed tests....")
    LD FALSE
    JMPC e4
	CAL PRINTLN("JMPC OK")
e4:
    (*false in RR*)
    JMPCN e5
        CAL PRINTLN("ERROR: JMPCN")
	JMP e6
e5: CAL PRINTLN("JMPC OK")
e6:  
 
END_PROGRAM
