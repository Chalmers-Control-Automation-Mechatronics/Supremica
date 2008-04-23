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
 * The VerificationModelBuilder abstract class is used to build a verification 
 * model from the chosen RAC.
 *
 *
 * Created: Thu Apr 10 17:01:39 2008
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.external.rac;
    
import org.supremica.external.genericPLCProgramDescription.xsd.*;
import org.supremica.external.genericPLCProgramDescription.xsd.Project.Types.Pous.*;
import org.supremica.external.rac.verificationModel.*;
import java.util.List;

abstract public class VerificationModelBuilder {
    
    public VerificationModelBuilder() 
    {
    } 
    
    abstract public void createNewVerificationModel(Project plcProject, String rac);
	
    abstract public VerificationModel getVerificationModel();

    final protected Pou getRAC(Project plcProject, String rac)
    {
	for (Pou pou : plcProject.getTypes().getPous().getPou())
	{
	    if (pou.getName().equals(rac))
	    {
		//System.out.println("VerificationModelBuilder: RAC " + rac + " detected");
		return pou;
	    }
	}
	return null;
    }
}