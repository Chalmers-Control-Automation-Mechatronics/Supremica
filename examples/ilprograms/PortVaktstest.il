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
		delay2, delay5 : TON;
		
		Init  : BOOL := TRUE;
	END_VAR
(*init*)
	LD Init
	JMPCN end_init
		LD DINT#1000 (*Time of delay*)
		ST delay2.tonPT
		ST delay5.tonPT

		LD FALSE
		ST Init
end_init:

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
			LD  TRUE
			ST  delay2.tonIN
			CAL delay2
			LD  delay2.tonQ
			STN InPortvakt
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

	LD KulaMatlyft
	AND MatlyftNere
	JMPCN END5
		LD TRUE
		ST delay5.tonIN
		CAL delay5
		LD delay5.tonQ
		ST Matlyft
END5:

END_PROGRAM
