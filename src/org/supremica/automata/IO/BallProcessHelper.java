/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.IO;

import java.io.*;

public class BallProcessHelper
	extends SattLineHelper
{

	private static BallProcessHelper theHelper;

	protected BallProcessHelper()
	{
	}

	public static IEC61131Helper getInstance()
	{
		if (theHelper == null)
		{
			theHelper = new BallProcessHelper();
		}
		return theHelper;
	}

	/**
	 * Helper functions for producing SattLine Code for the Ball Process
	 *
	 * @returns Nothing
	 * @param pw does this and that
	 * @see org.supremica.gui.Supremica
	 */

	public void printBeginProgram(PrintWriter pw, String fileName)
	{
		printFileHeader(pw, fileName);
		printBasePictureInvocation(pw);
		printTypeDefinitions(pw);
		printBeginLocalVariables(pw);
		printLocalVariables(pw);
		printEndLocalVariables(pw);
		printSubModules(pw);
		printBeginModuleDefinition(pw);
		printOutputSignalHandlers(pw);
	}

	public void printEndProgram(PrintWriter pw)
	{
		printEndModuleDefinition(pw);
	}

	public void printBasePictureInvocation(PrintWriter pw)
	{
		pw.println("BasePicture Invocation");
		pw.println("   ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 ");
		pw.println("    IgnoreMaxModule ) : MODULEDEFINITION DateCode_ 451751498 ( GroupConn = ");
		pw.println("ScanGroup ) ");
	}

	public void printTypeDefinitions(PrintWriter pw)
	{
		pw.println("TYPEDEFINITIONS");
		pw.println("   IO_intype = RECORD DateCode_ 161179876");
		pw.println("      KulaPortvakt, MätlyftNere, KulaMätlyft, MätlyftUppe, KulaMätstation, ");
		pw.println("      StorKula, LitenKula, HissNere, KulaHiss, HissVån1, KulaVån1, PlockaVån1, ");
		pw.println("      HissVån2, KulaVån2, PlockaVån2, ArmHemma, ArmVån1, ArmVån2, KulaFast, ");
		pw.println("      AutoStart, ManuellStart, NödStopp, LarmKvittering: BooleanSignal ;");
		pw.println("   ENDDEF");
		pw.println("    (*IO_intype*);");
		pw.println("   ");
		pw.println("   IO_outtype = RECORD DateCode_ 221276465");
		pw.println("      UrPortvakt, InPortvakt, UppMätlyft, UrMätning, Mät, LångaHissen, ");
		pw.println("      KortaHissen, UtVån1, LyftVån1, UtVån2, LyftVån2, KortArm, LångArm, ");
		pw.println("      VridArmHöger, Sug, TändLampa: BooleanSignal ;");
		pw.println("   ENDDEF");
		pw.println("    (*IO_outtype*);");
		pw.println("   ");
		pw.println("   Insignal = RECORD DateCode_ 184588196");
		pw.println("      KulaPortvakt, MätlyftNere, KulaMätlyft, MätlyftUppe, KulaMätstation, ");
		pw.println("      StorKula, LitenKula, HissNere, KulaHiss, HissVån1, KulaVån1, PlockaVån1, ");
		pw.println("      HissVån2, KulaVån2, PlockaVån2, ArmHemma, ArmVån1, ArmVån2, KulaFast: ");
		pw.println("      boolean ;");
		pw.println("      AutoStart, ManuellStart: boolean  := True;");
		pw.println("      NödStopp, LarmKvittering: boolean ;");
		pw.println("   ENDDEF");
		pw.println("    (*Insignal*);");
		pw.println("   ");
		pw.println("   Utsignal = RECORD DateCode_ 161103228");
		pw.println("      InPortvakt, UrPortvakt, UppMätlyft, UrMätning, Mät, UppHissVån1, ");
		pw.println("      UppHissVån2, UtVån1, LyftVån1, UtVån2, LyftVån2, UppArmVån1, UppArmVån2, ");
		pw.println("      VridArmHöger, Sug, TändLampa: boolean ;");
		pw.println("   ENDDEF");
		pw.println("    (*Utsignal*);");
	}

	public void printLocalVariables(PrintWriter pw)
	{
		pw.println("   IP: Insignal ;");
		pw.println("   OP, SET_OP, RESET_OP: Utsignal ;");
		pw.println("   SysName: string  := \"remsys1111\";");
		pw.println("   ProgStationData: ProgStationData ;");
		pw.println("   ScanGroup: GroupData ;");
		pw.println("   IPCpanel1 \"Board to rack input connection, Panel 1\", OPCpanel1 ");
		pw.println("   \"Board to rack output connection, Panel 1\", IPCpanel2 ");
		pw.println("   \"Board to rack input connection, Panel 2\", OPCpanel2 ");
		pw.println("   \"Board to rack output connection, Panel 2\", IPCpanel3 ");
		pw.println("   \"Board to rack input connection, Panel 3\", OPCpanel3 ");
		pw.println("   \"Board to rack output connection, Panel 3\", IPCpanel4 ");
		pw.println("   \"Board to rack input connection, Panel 4\", OPCpanel4 ");
		pw.println("   \"Board to rack output connection, Panel 4\": BoardConnection ;");
	}

	public void printEndLocalVariables(PrintWriter pw)
	{
		// Empty for the Ball Process
	}

	public void printSubModules(PrintWriter pw)
	{
		pw.println("SUBMODULES");
		pw.println("   ProgStationControl Invocation");
		pw.println("      ( -0.42 , 0.92 , 0.0 , 0.04 , 0.04 ");
		pw.println("       ) : ProgStationControl (");
		pw.println("   ReqCycleTimeInit => 10, ");
		pw.println("   ReqCycleTimeFastInit => 10);");
		pw.println("   ");
		pw.println("   ScanGroupControl Invocation");
		pw.println("      ( -0.34 , 0.92 , 0.0 , 0.04 , 0.04 ");
		pw.println("       ) : ScanGroupControl (");
		pw.println("   Name => \"ScanGroupControl\", ");
		pw.println("   Group => ScanGroup, ");
		pw.println("   System => SysName, ");
		pw.println("   ReqCycleTimeInit => 10);");
		pw.println("   ");
		pw.println("   LabIO Invocation");
		pw.println("      ( -0.72 , 0.28 , -2.27243E-07 , 0.1 , 0.44 ");
		pw.println("       ) : MODULEDEFINITION DateCode_ 260496933");
		pw.println("   MODULEPARAMETERS");
		pw.println("      SysName \"Name of system to run in\": string ;");
		pw.println("      IP \"Input record\": Insignal ;");
		pw.println("      OP \"Output record\": Utsignal ;");
		pw.println("      IPConnector \"Input board connection\", OPConnector ");
		pw.println("      \"Output board connection\": BoardConnection ;");
		pw.println("   LOCALVARIABLES");
		pw.println("      ErrorSumFromRack \"ErrorRack OR ErrorSum\", X1");
		pw.println("      \"Elevator in outer unsafe zone?\", X2 \"Elevator in inner unsafe zone?\", ");
		pw.println("      EnableControl: boolean ;");
		pw.println("      TändLampa, Sug, VridArmHöger, LångArm, KortArm, LyftVån2, UtVån2, ");
		pw.println("      LyftVån1, UtVån1 \"Output\", LångaHissen \"Output\", KortaHissen \"Output\", ");
		pw.println("      Mät \"Output\", UrMätning \"Output\", UppMätlyft \"Output\", UrPortvakt ");
		pw.println("      \"Output\", InPortvakt \"Output\", LarmKvittering \"Input\", NödStopp \"Input\", ");
		pw.println("      ManuellStart \"Input\", AutoStart \"Input\", KulaFast \"Input\", ArmVån2 ");
		pw.println("      \"Input\", ArmVån1 \"Input\", ArmHemma \"Input\", PlockaVån2 \"Input\", KulaVån2 ");
		pw.println("      \"Input\", HissVån2 \"Input\", PlockaVån1 \"Input\", KulaVån1 \"Input\", HissVån1 ");
		pw.println("      \"Input\", KulaHiss \"Input\", HissNere \"Input\", LitenKula \"Input\", StorKula ");
		pw.println("      \"Input\", KulaMätstation \"Input\", MätlyftUppe \"Input\", KulaMätlyft \"Input\"");
		pw.println("      , MätlyftNere \"Input\", KulaPortvakt \"Input\": BooleanSignal ;");
		pw.println("      ScanGroup \"Scan group\": GroupData ;");
		pw.println("   SUBMODULES");
		pw.println("      Inputs");
		pw.println("      (* It's possible to replace this board with one of the following boards:");
		pw.println("         ID32, ID16, OD32, OD16, IODP");
		pw.println("         IA8, OA4, OAH4, IPA4");
		pw.println("         Empty *)");
		pw.println("       Invocation");
		pw.println("         ( 2.56186 , -0.46448 , 0.0 , 0.91825 , 0.145 ");
		pw.println("          ) : ID32 (");
		pw.println("      Name => \"IDPG\", ");
		pw.println("      IX0 => KulaPortvakt, ");
		pw.println("      IX0TagName => \"KulaPortvakt\", ");
		pw.println("      IX1 => MätlyftNere, ");
		pw.println("      IX1TagName => \"MätlyftNere\", ");
		pw.println("      IX2 => KulaMätlyft, ");
		pw.println("      IX2TagName => \"KulaMätlyft\", ");
		pw.println("      IX3 => MätlyftUppe, ");
		pw.println("      IX3TagName => \"MätlyftUppe\", ");
		pw.println("      IX4 => KulaMätstation, ");
		pw.println("      IX4TagName => \"KulaMätstation\", ");
		pw.println("      IX5 => StorKula, ");
		pw.println("      IX5TagName => \"StorKula\", ");
		pw.println("      IX6 => LitenKula, ");
		pw.println("      IX6TagName => \"LitenKula\", ");
		pw.println("      IX7 => HissNere, ");
		pw.println("      IX7TagName => \"HissNere\", ");
		pw.println("      IX10 => KulaHiss, ");
		pw.println("      IX10TagName => \"KulaHiss\", ");
		pw.println("      IX11 => HissVån1, ");
		pw.println("      IX11TagName => \"HissVån1\", ");
		pw.println("      IX12 => KulaVån1, ");
		pw.println("      IX12TagName => \"KulaVån1\", ");
		pw.println("      IX13 => PlockaVån1, ");
		pw.println("      IX13TagName => \"PlockaVån1\", ");
		pw.println("      IX14 => HissVån2, ");
		pw.println("      IX14TagName => \"HissVån2\", ");
		pw.println("      IX15 => KulaVån2, ");
		pw.println("      IX15TagName => \"KulaVån2\", ");
		pw.println("      IX16 => PlockaVån2, ");
		pw.println("      IX16TagName => \"PlockaVån2\", ");
		pw.println("      IX17 => ArmHemma, ");
		pw.println("      IX17TagName => \"ArmHemma\", ");
		pw.println("      IX20 => ArmVån1, ");
		pw.println("      IX20TagName => \"ArmVån1\", ");
		pw.println("      IX21 => ArmVån2, ");
		pw.println("      IX21TagName => \"ArmVån2\", ");
		pw.println("      IX22 => KulaFast, ");
		pw.println("      IX22TagName => \"KulaFast\", ");
		pw.println("      IX23 => AutoStart, ");
		pw.println("      IX23TagName => \"AutoStart\", ");
		pw.println("      IX24 => ManuellStart, ");
		pw.println("      IX24TagName => \"ManuellStart\", ");
		pw.println("      IX25 => NödStopp, ");
		pw.println("      IX25TagName => \"NödStopp\", ");
		pw.println("      IX26 => LarmKvittering, ");
		pw.println("      IX26TagName => \"LarmKvittering\", ");
		pw.println("      SysName => SysName, ");
		pw.println("      EnableControl => True, ");
		pw.println("      EnableParChanges => True, ");
		pw.println("      ErrorSum => True, ");
		pw.println("      Connector => IPconnector);");
		pw.println("      ");
		pw.println("      Outputs");
		pw.println("      (* It's possible to replace this board with one of the following boards:");
		pw.println("         ID32, ID16, OD32, OD16, IODP");
		pw.println("         IA8, OA4, OAH4, IPA4");
		pw.println("         Empty *)");
		pw.println("       Invocation");
		pw.println("         ( 3.47446 , -0.46448 , 0.0 , 0.91825 , 0.145 ");
		pw.println("          ) : OD32 (");
		pw.println("      Name => \"ODPG_8\", ");
		pw.println("      QX0 => InPortvakt, ");
		pw.println("      QX0TagName => \"InPortvakt\", ");
		pw.println("      QX1 => UrPortvakt, ");
		pw.println("      QX1TagName => \"UrPortvakt\", ");
		pw.println("      QX2 => UppMätlyft, ");
		pw.println("      QX2TagName => \"UppMätlyft\", ");
		pw.println("      QX3 => UrMätning, ");
		pw.println("      QX3TagName => \"UrMätning\", ");
		pw.println("      QX4 => Mät, ");
		pw.println("      QX4TagName => \"Mät\", ");
		pw.println("      QX5 => KortaHissen, ");
		pw.println("      QX5TagName => \"KortaHissen\", ");
		pw.println("      QX6 => LångaHissen, ");
		pw.println("      QX6TagName => \"LångaHissen\", ");
		pw.println("      QX7 => UtVån1, ");
		pw.println("      QX7TagName => \"UtVån1\", ");
		pw.println("      QX10 => LyftVån1, ");
		pw.println("      QX10TagName => \"LyftVån1\", ");
		pw.println("      QX11 => UtVån2, ");
		pw.println("      QX11TagName => \"UtVån2\", ");
		pw.println("      QX12 => LyftVån2, ");
		pw.println("      QX12TagName => \"LyftVån2\", ");
		pw.println("      QX13 => KortArm, ");
		pw.println("      QX13TagName => \"KortArm\", ");
		pw.println("      QX14 => LångArm, ");
		pw.println("      QX14TagName => \"LångArm\", ");
		pw.println("      QX15 => VridArmHöger, ");
		pw.println("      QX15TagName => \"VridArmHöger\", ");
		pw.println("      QX16 => Sug, ");
		pw.println("      QX16TagName => \"Sug\", ");
		pw.println("      QX17 => TändLampa, ");
		pw.println("      QX17TagName => \"TändLampa\", ");
		pw.println("      SysName => SysName, ");
		pw.println("      EnableControl => True, ");
		pw.println("      EnableParChanges => True, ");
		pw.println("      ErrorSum => True, ");
		pw.println("      Connector => OPconnector);");
		pw.println("      ");
		pw.println("   ");
		pw.println("   ModuleDef");
		pw.println("   ClippingBounds = ( 2.215 , -0.84 ) ( 4.705 , 1.18 )");
		pw.println("   Grid = 0.005");
		pw.println("   ");
		pw.println("   ModuleCode");
		pw.println("   EQUATIONBLOCK Interface COORD 2.565, -0.675 OBJSIZE 1.83, 0.205 :");
		pw.println("      (* ======================= Insignaler/svar ============================= ");
		pw.println("      *);");
		pw.println("      IP.KulaPortvakt = KulaPortvakt.Value;");
		pw.println("      IP.MätlyftNere = MätlyftNere.Value;");
		pw.println("      IP.KulaMätlyft = KulaMätlyft.Value;");
		pw.println("      IP.MätlyftUppe = MätlyftUppe.Value;");
		pw.println("      IP.KulaMätstation = KulaMätstation.Value;");
		pw.println("      IP.StorKula = StorKula.Value;");
		pw.println("      IP.LitenKula = LitenKula.Value;");
		pw.println("      IP.HissNere = HissNere.Value;");
		pw.println("      IP.KulaHiss = KulaHiss.Value;");
		pw.println("      IP.HissVån1 = HissVån1.Value;");
		pw.println("      IP.KulaVån1 = KulaVån1.Value;");
		pw.println("      IP.PlockaVån1 = PlockaVån1.Value;");
		pw.println("      IP.HissVån2 = HissVån2.Value;");
		pw.println("      IP.KulaVån2 = KulaVån2.Value;");
		pw.println("      IP.PlockaVån2 = PlockaVån2.Value;");
		pw.println("      IP.ArmHemma = ArmHemma.Value;");
		pw.println("      IP.ArmVån1 = ArmVån1.Value;");
		pw.println("      IP.ArmVån2 = ArmVån2.Value;");
		pw.println("      IP.KulaFast = KulaFast.Value;");
		pw.println("      IP.AutoStart = AutoStart.Value;");
		pw.println("      IP.ManuellStart = ManuellStart.Value;");
		pw.println("      IP.NödStopp = NödStopp.Value;");
		pw.println("      IP.LarmKvittering = LarmKvittering.Value;");
		pw.println("      (* ================ Utsignaler/kommandon ================================ ");
		pw.println("      *);");
		pw.println("      InPortvakt.Value = OP.InPortvakt;");
		pw.println("      UrPortvakt.Value = OP.UrPortvakt;");
		pw.println("      Sug.Value = OP.Sug;");
		pw.println("      TändLampa.Value = OP.TändLampa;");
		pw.println("      (* ---------------- Mätningen --------------------------------------------- ");
		pw.println("      *);");
		pw.println("      (* Här ville jag programmera \"fel\", så att om UrMätning är ute eller Mät är ");
		pw.println("      *);");
		pw.println("      (* nere kommer Mätlyften att \"oscillera\" nere vid MätlyftNere *);");
		pw.println("      (* UppMätlyft.Value = OP.UppMätlyft AND (MätlyftNere.Value OR  *);");
		pw.println("      (*                          NOT UrMätning.Value); *);");
		pw.println("      (* Men kulan ramlar av då, så .... *);");
		pw.println("      (* Mätlyften får gå upp endast om UrMätning är inne och Mät inte är nere ");
		pw.println("      *);");
		pw.println("      (* Fast Mät får vara nere och UrMätning får vara ute när Mätlyften är uppe ");
		pw.println("      *);");
		pw.println("      UppMätlyft.Value = OP.UppMätlyft AND ( NOT (Mät.Value OR UrMätning.Value) ");
		pw.println("         OR MätlyftUppe.Value);");
		pw.println("      (* Mät får gå ner när som helst ?? *);");
		pw.println("      Mät.Value = OP.Mät;");
		pw.println("      (* UrMätning får inte gå ut om Mät är nere *);");
		pw.println("      UrMätning.Value = OP.UrMätning AND  NOT Mät.Value;");
		pw.println("      (* ----------------- Hissen ---------------------------------------------- ");
		pw.println("      *);");
		pw.println("      (* Vi har två \"säkerhetsområden\". X1 som täcker in både vån1 och vån2, och ");
		pw.println("      *);");
		pw.println("      (* X2 som bara säkrar vån2. Det är bara när Hissen befinner sig i nåt av ");
		pw.println("      *);");
		pw.println("      (* säkerhetsområdena som vi behöver kolla UtVån1/UtVån2 *);");
		pw.println("      X1 = (X1 OR HissVån1.Value AND LångaHissen.Value AND KortaHissen.Value) ");
		pw.println("         AND  NOT (HissVån1.Value AND  NOT LångaHissen.Value AND  NOT ");
		pw.println("         KortaHissen.Value);");
		pw.println("      X2 = (X2 OR HissVån2.Value) AND  NOT HissNere.Value;");
		pw.println("      (* Sätt LångaHissen om vi antingen vill till vån1 eller vån2 *);");
		pw.println("      (* Får gå till vån1 endast om UtVån1 är inne, men om vi är på vån1 *);");
		pw.println("      (* och inte ska vidare så får UtVån1 gå ut *);");
		pw.println("      (* Får gå till vån2 endast om UtVån1 och UtVån2 är inne, men om vi är på ");
		pw.println("      *);");
		pw.println("      (* vån2 så får UtVån2 gå ut *);");
		pw.println("      LångaHissen.Value = OP.UppHissVån1 AND ( NOT UtVån1.Value OR HissVån1.");
		pw.println("         Value AND  NOT KortaHissen.Value) AND  NOT OP.UppHissVån2 OR OP.");
		pw.println("         UppHissVån2 AND ( NOT (UtVån1.Value OR UtVån2.Value) OR HissVån2.Value ");
		pw.println("         AND OP.UtVån2) AND  NOT OP.UppHissVån1;");
		pw.println("      KortaHissen.Value = OP.UppHissVån2 AND  NOT (HissVån1.Value AND OP.UtVån1");
		pw.println("         );");
		pw.println("      UtVån1.Value = OP.UtVån1 AND  NOT X1;");
		pw.println("      (* Detta fungerar inte: NOT X1 OR HissVån1.Value AND  NOT Kortahissen.Value ");
		pw.println("      *);");
		pw.println("      UtVån2.Value = OP.UtVån2;");
		pw.println("      (* ----------------- Armen ---------------------------------- *);");
		pw.println("      LyftVån1.Value = OP.LyftVån1;");
		pw.println("      LyftVån2.Value = OP.LyftVån2;");
		pw.println("      (* Sätt KortArm om vi antingen vill till vån1 eller vån2 *);");
		pw.println("      (* Armen ska dessutom hållas på plats så länge våningen är lyft *);");
		pw.println("      (* Får alltså inte försöka gå ner (och tillbaka) med lyft våning *);");
		pw.println("      KortArm.Value = OP.UppArmVån1 OR OP.UppArmVån2 OR LyftVån1.Value AND ");
		pw.println("         ArmVån1.Value OR ArmVån1.Value AND OP.VridArmHöger OR ArmVån2.Value ");
		pw.println("         AND OP.VridArmHöger;");
		pw.println("      LångArm.Value = OP.UppArmVån2 OR LyftVån2.Value AND ArmVån2.Value OR ");
		pw.println("         ArmVån2.Value AND OP.VridArmHöger;");
		pw.println("      (* Vrid armen endast om våningen vi ska till ej är lyft *);");
		pw.println("      (* Håll också armen vriden om vi är där och våningen är lyft *);");
		pw.println("      VridArmHöger.Value = OP.VridArmHöger AND (KortArm.Value AND  NOT LyftVån1");
		pw.println("         .Value OR OP.UppArmVån2 AND  NOT LyftVån2.Value) OR ArmVån1.Value AND ");
		pw.println("         LyftVån1.Value OR ArmVån2.Value AND LyftVån2.Value;");
		pw.println("   ");
		pw.println("   ENDDEF (*LabIO*) (");
		pw.println("   SysName => SysName, ");
		pw.println("   IP => IP, ");
		pw.println("   OP => OP, ");
		pw.println("   IPConnector => IPCpanel1, ");
		pw.println("   OPConnector => OPCpanel1);");
		pw.println("   ");
		pw.println("   Rack Invocation");
		pw.println("      ( -0.46 , 0.78 , 0.000148473 , 0.08 , 0.07141 ");
		pw.println("       ) : Rack (");
		pw.println("   SysName => SysName, ");
		pw.println("   ExternalRack => False, ");
		pw.println("   Slot0 => IPCpanel1, ");
		pw.println("   Slot40 => OPCpanel1, ");
		pw.println("   Slot100 => IPCpanel2, ");
		pw.println("   Slot140 => OPCpanel2, ");
		pw.println("   Slot200 => IPCpanel3, ");
		pw.println("   Slot240 => OPCpanel3, ");
		pw.println("   Slot300 => IPCpanel4, ");
		pw.println("   Slot340 => OPCpanel4);");

	}

	public void printOutputSignalHandlers(PrintWriter pw)
	{
		int stepCounter = 0;
		int transitionCounter = 0;
		pw.println("SEQUENCE InPortvaktHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.InPortvakt" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.InPortvakt" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.InPortvakt" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.InPortvakt" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UrPortvaktHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UrPortvakt" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UrPortvakt" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UrPortvakt" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UrPortvakt" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppMätningHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppMätlyft" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppMätlyft" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppMätlyft" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppMätlyft" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UrMätningHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UrMätning" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UrMätning" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UrMätning" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UrMätning" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE MätHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.Mät" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.Mät" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.Mät" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.Mät" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppHissVån1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppHissVån1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppHissVån1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppHissVån1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppHissVån1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppHissVån2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppHissVån2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppHissVån2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppHissVån2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppHissVån2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UtVån1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UtVån1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UtVån1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UtVån1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UtVån1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UtVån2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UtVån2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UtVån2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UtVån2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UtVån2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE LyftVån1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.LyftVån1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.LyftVån1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.LyftVån1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.LyftVån1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE LyftVån2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.LyftVån2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.LyftVån2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.LyftVån2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.LyftVån2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppArmVån1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppArmVån1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppArmVån1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppArmVån1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppArmVån1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppArmVån2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppArmVån2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppArmVån2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppArmVån2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppArmVån2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE VridArmHögerHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.VridArmHöger" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.VridArmHöger" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.VridArmHöger" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.VridArmHöger" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE SugHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.Sug" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.Sug" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.Sug" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.Sug" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE TändLampaHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.TändLampa" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.TändLampa" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.TändLampa" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.TändLampa" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
	}

	public void printGFile(PrintWriter pw, String filename)
	{
		pw.println("\" Syntax version 2.19, date: 2004-01-27-20:33:34.140 N \" ");
	}

	public void printLFile(PrintWriter pw, String filename)
	{
		pw.println("pioracklib");
		pw.println("nucleuslib");
	}

	public void printPFile(PrintWriter pw, String filename)
	{
		pw.println("DistributionData");
		pw.println(" ( Version \"Distributiondata version 1.0\" )");
		pw.println("SourceCodeSystems");
		pw.println(" (  )");
		pw.println("ExecutingSystems");
		pw.println(" ( Controller");
		pw.println("    ( Name \"remsys1111\"");
		pw.println("      Download LowMemReconfig ) )");
	}
}
