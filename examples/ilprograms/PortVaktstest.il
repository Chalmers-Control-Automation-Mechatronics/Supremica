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
	END_VAR

(*	if (Start && !(KulaPortvakt || UrPortvakt))
		Släpp in kula
	fi
*)
		LD Start
		ANDN(KulaPortvakt
		 	OR UrPortvakt
		)
		JMPCN END1
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
END_PROGRAM
(*
	bonus
	if (KulaMätlyft && MätlyftNere)

*)


