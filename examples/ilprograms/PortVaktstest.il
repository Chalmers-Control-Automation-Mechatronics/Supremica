(*
FUNCTION_BLOCK Delayer_KK
	VAR_IN_OUT
		LOOPS : DINT;
		L : DINT;
	END_VAR
	LD DINT#0
	ST L
LOOP :  LD L
	EQ LOOPS
	JMPC ENDING
	CAL PRINT("HEJ")
	JMP LOOP
ENDING:
END_FUNCTION_BLOCK	
*)
PROGRAM PortVaktstest
	VAR
		(*Insignaler*)
		Start 		 AT %IX24 : BOOL;
		KulaPortvakt AT %IX0  : BOOL;
		MatlyftNere  AT %IX1  : BOOL;
		KulaMatlyft  AT %IX2  : BOOL;
		(*Utsignaler*)
		InPortvakt   AT %QX0  : BOOL;
		UrPortvakt   AT %QX1  : BOOL;
		Matlyft      AT %QX2  : BOOL;
	END_VAR
	VAR
		Lcnt : DINT;
		tmp  : BOOL;
	END_VAR
(*
	VAR
		delay : Delayer;
	END_VAR
*)

(*Print start*)
CAL PRINTLN("Start =", Start)
CAL PRINTLN("Urportvakt=", UrPortvakt)
	LD FALSE
	ST %QX1


(*	if !(Start || (KulaPortvakt || UrPortvakt))
		Släpp in kula
	fi
*)
		LD Start
		OR(KulaPortvakt
		   OR UrPortvakt
		)
		ST tmp
		CAL PRINTLN("TMP=",tmp)
		CAL PRINTLN("KulaPvakt=",KulaPortvakt)
		CAL PRINTLN("UrPortvakt=",UrPortvakt)
		JMPC END1
			
			LD TRUE
			ST InPortvakt
END1 :

(*
	if (KulaPortvakt)
		Stäng InPortvakt
	fi
*)

		LD KulaPortvakt
		JMPCN END2
			LD FALSE
			ST InPortvakt
END2 :

(*
	if (KulaPortvakt && MätlyftNere && !KulaMätlyft)
		Släpp ut kula ur portvakt
	fi
*)
		LD KulaPortvakt
		AND MatlyftNere
		ANDN KulaMatlyft
		JMPCN END3
			LD TRUE
			ST UrPortvakt
END3 :

(*
	if (!KulaPortvakt)
		Stäng UrPortvakt
	fi
*)

		LD KulaPortvakt
		JMPC END4
			LD FALSE
			ST UrPortvakt
END4 :

(*
	bonus
	if (KulaMätlyft && MätlyftNere)

*)
(*	LD DINT#600
	ST delay.LOOPS
*)

	LD KulaMatlyft
	AND MatlyftNere
	JMPCN END5


		LD DINT#0
		ST Lcnt
	LOOP :  LD Lcnt
		EQ DINT#400
		JMPC ENDING
		CAL PRINT("HEJ")
		LD DINT#1
		ADD Lcnt
		ST Lcnt
		JMP LOOP
	ENDING:

		LD TRUE
		ST Matlyft
END5:

END_PROGRAM
