/*
 * This RAC package is external to the Supremica tool and developed by 
 * Oscar Ljungkrantz. Contact:
 *
 * Oscar Ljungkrantz 
 * oscar.ljungkrantz@chalmers.se
 * +46 (0)708-706278
 * SWEDEN
 *
 * for technical discussions about RACs (Reusable Automation Components).
 * For questions about Supremica license or other technical discussions,
 * contact Supremica according to License Agreement below.
 */

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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

/**
 * The StandardFBs is a HashMap that contains the names of standard FBs 
 * mapped to the files (filename) with the SMV code.
 *
 *
 * Created: Thu May 15 10:52:12 2008
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.external.rac.verificationModel.smv;

import java.util.HashMap;

public class StandardFBs<K, V> extends HashMap<String, String>
{

    public StandardFBs()
    {
	super();
	String standardPath = "..\\src\\org\\supremica\\external\\rac\\verificationModel\\smv\\smv_standardFBs\\";
	this.put("TON", standardPath + "TON.smv");
	this.put("TON_RSLogix", standardPath + "TON_RSLogix.smv");
	this.put("TOF_RSLogix", standardPath + "TOF_RSLogix.smv");
	this.put("TIMER_100_FB_M", standardPath + "TIMER_100_FB_M.smv");
	this.put("R_TRIG", standardPath + "R_TRIG.smv");
	this.put("F_TRIG", standardPath + "F_TRIG.smv");
	this.put("EDGE_CHECK", standardPath + "EDGE_CHECK.smv");
    }


}
