(*
Skall testa EQ 
*)
PROGRAM EQtest
   VAR
	BB1, BB2, BB3 : BOOL;
	DD1, DD2, DD3 : DINT;
	RR1, RR2, RR3 : REAL;
	WW1, WW2, WW3 : WSTRING;
   END_VAR
	(*BOOL- jämför stack med variabel*)
(*T-T*)
	LD TRUE
	ST BB1
	LD TRUE
	EQ BB1
	ST BB2
	CAL PRINT("?? true == ",BB2)
	CAL PRINTLN("")
(*T-F*)
	LD FALSE
	ST BB1
	LD TRUE
	EQ BB1
	ST BB2
	CAL PRINT("?? false == ",BB2)
	CAL PRINTLN("")
(*F-T*)
	LD TRUE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL PRINT("?? false == ",BB2)
	CAL PRINTLN("")
(*F-F*)
	LD FALSE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL PRINT("?? true == ",BB2)
	CAL PRINTLN("")

	(*BOOL- jämför stack med konstant*)
(*T-T*)
	LD TRUE
	EQ TRUE
	ST BB2
	CAL PRINT("?? true == ",BB2)
	CAL PRINTLN("")
(*T-F*)
	LD FALSE
	EQ TRUE
	ST BB2
	CAL PRINT("?? false == ",BB2)
	CAL PRINTLN("")
(*F-T*)
	LD FALSE
	EQ TRUE
	ST BB2
	CAL PRINT("?? false == ",BB2)
	CAL PRINTLN("")
(*F-F*)
	LD FALSE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL PRINT("?? true == ",BB2)
	CAL PRINTLN("")


END_PROGRAM
