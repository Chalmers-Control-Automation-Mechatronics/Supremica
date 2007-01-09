package org.supremica.external.sag.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ocl.query.QueryFactory;

public class OclHelper {
	public static boolean check(EObject objectToCheck, String oclExpression) {
		return QueryFactory.eINSTANCE.createQuery(oclExpression,
				objectToCheck.eClass()).check(objectToCheck);
	}
	public static Object evaluate(EObject objectToCheck, String oclExpression) {
		return QueryFactory.eINSTANCE.createQuery(oclExpression,
				objectToCheck.eClass()).evaluate(objectToCheck);
	}

}
