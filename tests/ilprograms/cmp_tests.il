(*
Test av EQ, GT, GE, EQ, LT, LE, NE 
*)

JAVA_BLOCK JPrintWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
	VAR
		jblock: JPrintWSTRING;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
	VAR
		jblock: JPrintDINT;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK
JAVA_BLOCK JPrintREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
	VAR
		jblock:JPrintREAL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
	VAR
		jblock: JPrintBOOL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK


JAVA_BLOCK JPrintlnWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
	VAR
		jblock: JPrintlnWSTRING;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintlnBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
	VAR
		jblock: JPrintlnBOOL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintlnDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
	VAR
		jblock: JPrintlnDINT;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintlnREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
	VAR
		jblock:JPrintlnREAL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK



PROGRAM CMPtests
   VAR
	BB1, BB2, BB3 : BOOL;
	DD1, DD2, DD3 : DINT;
	RR1, RR2, RR3 : REAL;
	WW1, WW2, WW3 : WSTRING;
	PRINT_S : JPrintWSTRING;
	PRINT_B : JPrintBOOL;
	PRINT_I : JPrintDINT;
	PRINT_R : JPrintREAL;
	PRINTLN_S : JPrintlnWSTRING;
	PRINTLN_B : JPrintlnBOOL;
	PRINTLN_I : JPrintlnDINT;
	PRINTLN_R : JPrintlnREAL;
   END_VAR

	(*BOOL- jämför stack med variabel*)
	CAL PRINTLN_S(output:="EQ - BOOL")
(*T-T*)
	LD TRUE
	ST BB1
	LD TRUE
	EQ BB1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*T-F*)
	LD FALSE
	ST BB1
	LD TRUE
	EQ BB1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*F-T*)
	LD TRUE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*F-F*)
	LD FALSE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
	
	(*BOOL- jämför stack med konstant*)
(*T-T*)
	LD TRUE
	EQ TRUE
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*T-F*)
	LD FALSE
	EQ TRUE
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*F-T*)
	LD TRUE
	EQ FALSE
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*F-F*)
	LD FALSE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)


	(*DINT - jämför stack med variabel*)
	CAL PRINTLN_S(output:="EQ - DINT")
(*1-1*)
	LD DINT#1
	ST DD1
	LD DINT#1
	EQ DD1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 2147483647*)
	LD DINT#2147483647
	ST DD1
	LD DINT#2147483647
	EQ DD1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - -2147483647*)
	LD DINT#-2147483647
	ST DD1
	LD DINT#-2147483647
	EQ DD1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 2147483647*)
	LD DINT#2147483647
	ST DD1
	LD DINT#0
	EQ DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - -2147483647*)
	LD DINT#-2147483647
	ST DD1
	LD DINT#0
	EQ DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - 0*)
	LD DINT#0
	ST DD1
	LD DINT#-2147483647
	EQ DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 0*)
	LD DINT#0
	ST DD1
	LD DINT#2147483647
	EQ DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)

	(*DINT- jämför stack med konstant*)
(*1-1*)
	LD DINT#1
	EQ DINT#1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 2147483647*)
	LD DINT#2147483647
	EQ DINT#2147483647
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - -2147483647*)
	LD DINT#-2147483647
	EQ DINT#-2147483647
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - -2147483647*)
	LD DINT#2147483647
	EQ DINT#0
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - -2147483647*)
	LD DINT#-2147483647
	EQ DINT#0
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - 0*)
	LD DINT#0
	EQ DINT#-2147483647
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 0*)
	LD DINT#0
	EQ DINT#2147483647
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)


	(*EQ - REAL - jämför stack med variabel*)
	CAL PRINTLN_S(output:="EQ - REAL")
(*1.0 - 1.0*)
	LD REAL#1.0
	ST RR1
	LD REAL#1.0
	EQ RR1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 3.4028235E38*)
	LD REAL#3.4028235E38
	ST RR1
	LD REAL#3.4028235E38
	EQ RR1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 1.4E-45*)
	LD REAL#1.4E-45
	ST RR1
	LD REAL#1.4E-45
	EQ RR1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 3.4028235E38*)
	LD REAL#3.4028235E38
	ST RR1
	LD REAL#0.0
	EQ RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 1.4E-45*)
	LD REAL#1.4E-45
	ST RR1
	LD REAL#0.0
	EQ RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 0*)
	LD REAL#0.0
	ST RR1
	LD REAL#1.4E-45
	EQ RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 0*)
	LD REAL#0.0
	ST RR1
	LD REAL#3.4028235E38
	EQ RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)

	(*EQ - REAL- jämför stack med konstant*)
(*1-1*)
	LD REAL#1.0
	EQ REAL#1.0
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 3.4028235E38*)
	LD REAL#3.4028235E38
	EQ REAL#3.4028235E38
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 1.4E-45*)
	LD REAL#1.4E-45
	EQ REAL#1.4E-45
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 3.4028235E38*)
	LD REAL#3.4028235E38
	EQ REAL#0.0
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 1.4E-45*)
	LD REAL#1.4E-45
	EQ REAL#0.0
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 0*)
	LD REAL#0.0
	EQ REAL#1.4E-45
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 0*)
	LD REAL#0.0
	EQ REAL#3.4028235E38
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)



(*GT ÄR INTE KLAR ÄNNU!!!*)

	(*GT - DINT - jämför stack med variabel*)
	CAL PRINTLN_S(output:="GT - DINT")
(*1-1*)
	LD DINT#1
	ST DD1
	LD DINT#1
	GT DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 2147483647*)
	LD DINT#2147483647
	ST DD1
	LD DINT#2147483647
	GT DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - -2147483647*)
	LD DINT#-2147483647
	ST DD1
	LD DINT#-2147483647
	GT DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 2147483647*)
	LD DINT#2147483647
	ST DD1
	LD DINT#0
	GT DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - -2147483647*)
	LD DINT#-2147483647
	ST DD1
	LD DINT#0
	GT DD1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - 0*)
	LD DINT#0
	ST DD1
	LD DINT#-2147483647
	GT DD1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 0*)
	LD DINT#0
	ST DD1
	LD DINT#2147483647
	GT DD1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)

	(*GT - DINT - jämför stack med konstant*)
(*1-1*)
	LD DINT#1
	GT DINT#1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 2147483647*)
	LD DINT#2147483647
	GT DINT#2147483647
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - -2147483647*)
	LD DINT#-2147483647
	GT DINT#-2147483647
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*2147483647 - 0*)
	LD DINT#2147483647
	GT DINT#0
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*-2147483647 - 0*)
	LD DINT#-2147483647
	GT DINT#0
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - -2147483647*)
	LD DINT#0
	GT DINT#-2147483647
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 2147483647*)
	LD DINT#0
	GT DINT#2147483647
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)


	(*GT - REAL - jämför stack med variabel*)
	CAL PRINTLN_S(output:="GT - REAL")
(*1.0 - 1.0*)
	LD REAL#1.0
	ST RR1
	LD REAL#1.0
	GT RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 3.4028235E38*)
	LD REAL#3.4028235E38
	ST RR1
	LD REAL#3.4028235E38
	GT RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 1.4E-45*)
	LD REAL#1.4E-45
	ST RR1
	LD REAL#1.4E-45
	GT RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 3.4028235E38*)
	LD REAL#3.4028235E38
	ST RR1
	LD REAL#0.0
	GT RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 1.4E-45*)
	LD REAL#1.4E-45
	ST RR1
	LD REAL#0.0
	GT RR1
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 0*)
	LD REAL#0.0
	ST RR1
	LD REAL#1.4E-45
	GT RR1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 0*)
	LD REAL#0.0
	ST RR1
	LD REAL#3.4028235E38
	GT RR1
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)

	(*GT - REAL - jämför stack med konstant*)
(*1-1*)
	LD REAL#1.0
	GT REAL#1.0
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 3.4028235E38*)
	LD REAL#3.4028235E38
	GT REAL#3.4028235E38
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 1.4E-45*)
	LD REAL#1.4E-45
	GT REAL#1.4E-45
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*3.4028235E38 - 0*)
	LD REAL#3.4028235E38
	GT REAL#0.0
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*1.4E-45 - 0*)
	LD REAL#1.4E-45
	GT REAL#0.0
	ST BB2
	CAL PRINT_S(output:="?? true == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 1.4E-45*)
	LD REAL#0.0
	GT REAL#1.4E-45
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)
(*0 - 3.4028235E38*)
	LD REAL#0.0
	GT REAL#3.4028235E38
	ST BB2
	CAL PRINT_S(output:="?? false == ")
	CAL PRINTLN_B(output:=BB2)

END_PROGRAM
