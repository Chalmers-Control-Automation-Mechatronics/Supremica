(*JAVA_BLOCKS for printing (no newline)*)
(*=====================================*)

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

(*JAVA_BLOCKS for printing (newline)*)
(*=====================================*)

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

(*=============================================================*)
(*=============================================================*)
(* Resultat:
	Titta vid XXX kommentarer för att hitta fel
   Innehåll:
     Test1
	Program LOGICtest: test av AND, ANDN, OR 
*)

(*Test1*)
PROGRAM LOGICtest
	VAR
		WSpln : PrintlnWSTRING;
		Dpln  : PrintlnDINT;
		Rpln  : PrintlnREAL;
		Bpln  : PrintlnBOOL;
		WSp   : PrintWSTRING;
		Dp    : PrintDINT;
		Rp    : PrintREAL;
		Bp    : PrintBOOL;
	END_VAR
   VAR
	FVAR, TVAR : BOOL;
	RV1, RV2 : BOOL;
	WW1, WW2 : WSTRING;
   END_VAR
(*BOOL*)
(*====*)

LD FALSE
ST FVAR
LD TRUE
ST TVAR

(*pre: FVAR = FALSE, TVAR = FALSE*)
(*AND-operatorn, operand: variabel*)
	CAL WSpln(output:="Test av AND operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	AND TVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	AND FVAR	
	ST  RV1
	ST  RV2


	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	AND TVAR
	ST  RV1
	ST  RV2


	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	AND FVAR
	ST  RV1
	ST  RV2


	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

(*pre: FVAR = FALSE, TVAR = FALSE*)
(*AND-operatorn, operand: konstant*)
	CAL WSpln(output:="Test av AND operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	AND TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	AND FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	AND TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	AND FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)


(*pre: FVAR = FALSE, TVAR = FALSE*)
(*OR-operatorn, operand: variabel*)
	CAL WSpln(output:="Test av OR operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	OR TVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	OR FVAR	
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	OR TVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	OR FVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

(*pre: FVAR = FALSE, TVAR = FALSE*)
(*OR-operatorn, operand: konstant*)
	CAL WSpln(output:="Test av OR operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	OR  TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	OR  FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	OR  TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	OR  FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)


(*pre: FVAR = FALSE, TVAR = FALSE*)
(*ANDN-operatorn, operand: variabel*)
	CAL WSpln(output:="Test av ANDN operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ANDN TVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ANDN FVAR	
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ANDN TVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ANDN FVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

(*pre: FVAR = FALSE, TVAR = FALSE*)
(*ANDN-operatorn, operand: konstant*)
	CAL WSpln(output:="Test av ANDN operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ANDN TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ANDN FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ANDN TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ANDN FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)


(*pre: FVAR = FALSE, TVAR = FALSE*)
(*ORN-operatorn, operand: variabel*)
	CAL WSpln(output:="Test av ORN operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ORN TVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ORN FVAR	
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ORN TVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ORN FVAR
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

(*pre: FVAR = FALSE, TVAR = FALSE*)
(*ORN-operatorn, operand: konstant*)
	CAL WSpln(output:="Test av ORN operatorn")
	(*T-T*)	
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ORN TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
	(*T-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD TRUE

	ORN FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-T*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ORN TRUE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? false == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)

	(*F-F*)
	LD FALSE
	ST RV1
	LD TRUE
	ST RV2 (*end init*)
	LD FALSE

	ORN FALSE
	ST  RV1
	ST  RV2

	CAL WSp(output := "?? true  == ")
	CAL Bp(output := RV1)
	CAL WSp(output := " ")
	CAL Bpln(output := RV2)
	
END_PROGRAM
