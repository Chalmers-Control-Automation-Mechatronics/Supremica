package org.supremica.external.sag.diagram.expressions;

import java.lang.ref.WeakReference;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.ETypedElement;

import org.eclipse.emf.ocl.expressions.ExpressionsFactory;
import org.eclipse.emf.ocl.expressions.OCLExpression;
import org.eclipse.emf.ocl.expressions.OperationCallExp;
import org.eclipse.emf.ocl.expressions.Variable;

import org.eclipse.emf.ocl.expressions.util.AbstractVisitor;

import org.eclipse.emf.ocl.helper.HelperUtil;
import org.eclipse.emf.ocl.helper.IOCLHelper;
import org.eclipse.emf.ocl.helper.OCLParsingException;

import org.eclipse.emf.ocl.parser.EcoreEnvironment;
import org.eclipse.emf.ocl.parser.EcoreEnvironmentFactory;
import org.eclipse.emf.ocl.parser.Environment;
import org.eclipse.emf.ocl.parser.EvaluationEnvironment;

import org.eclipse.emf.ocl.query.Query;
import org.eclipse.emf.ocl.query.QueryFactory;

import org.eclipse.emf.ocl.types.util.Types;

import org.eclipse.emf.ocl.utilities.PredefinedType;

/**
 * @generated 
 */
public class SagOCLFactory {

	/**
	 * @generated 
	 */
	private SagOCLFactory() {
	}

	/**
	 * @generated 
	 */
	public static SagAbstractExpression getExpression(String body,
			EClassifier context, Map environment) {
		return new Expression(body, context, environment);
	}

	/**
	 * @generated 
	 */
	public static SagAbstractExpression getExpression(String body,
			EClassifier context) {
		return getExpression(body, context, Collections.EMPTY_MAP);
	}

	/**
	 * @generated 
	 */
	private static class Expression extends SagAbstractExpression {
		/**
		 * @generated 
		 */
		private WeakReference queryRef;

		/**
		 * @generated 
		 */
		public Expression(String body, EClassifier context, Map environment) {
			super(body, context, environment);
		}

		/**
		 * @generated 
		 */
		protected Query getQuery() {
			Query oclQuery = null;
			if (this.queryRef != null) {
				oclQuery = (Query) this.queryRef.get();
			}
			if (oclQuery == null) {
				IOCLHelper oclHelper = (environment().isEmpty()) ? HelperUtil
						.createOCLHelper() : HelperUtil
						.createOCLHelper(createCustomEnv(environment()));
				oclHelper.setContext(context());
				try {
					OCLExpression oclExpression = oclHelper.createQuery(body());
					oclQuery = QueryFactory.eINSTANCE
							.createQuery(oclExpression);
					this.queryRef = new WeakReference(oclQuery);
					setStatus(IStatus.OK, null, null);
				} catch (OCLParsingException e) {
					setStatus(IStatus.ERROR, e.getMessage(), e);
				}
			}
			return oclQuery;
		}

		/**
		 * @generated 
		 */
		protected Object doEvaluate(Object context, Map env) {
			Query oclQuery = getQuery();
			if (oclQuery == null) {
				return null;
			}
			EvaluationEnvironment evalEnv = oclQuery.getEvaluationEnvironment();
			// init environment
			for (Iterator it = env.entrySet().iterator(); it.hasNext();) {
				Map.Entry nextEntry = (Map.Entry) it.next();
				evalEnv.replace((String) nextEntry.getKey(), nextEntry
						.getValue());
			}

			try {
				initExtentMap(context);
				Object result = oclQuery.evaluate(context);
				return (result != Types.OCL_INVALID) ? result : null;
			} finally {
				evalEnv.clear();
				oclQuery.setExtentMap(Collections.EMPTY_MAP);
			}
		}

		/**
		 * @generated
		 */
		protected Object performCast(Object value, ETypedElement targetType) {
			if (targetType.getEType() instanceof EEnum) {
				if (value instanceof EEnumLiteral) {
					EEnumLiteral literal = (EEnumLiteral) value;
					return (literal.getInstance() != null) ? literal
							.getInstance() : literal;
				}
			}
			return super.performCast(value, targetType);
		}

		/**
		 * @generated
		 */
		private void initExtentMap(Object context) {
			if (!getStatus().isOK() || context == null) {
				return;
			}
			final Query queryToInit = getQuery();
			final Object extentContext = context;

			queryToInit.setExtentMap(Collections.EMPTY_MAP);
			if (queryToInit.queryText() != null
					&& queryToInit.queryText().indexOf("allInstances") >= 0) {
				AbstractVisitor visitior = new AbstractVisitor() {
					private boolean usesAllInstances = false;

					public Object visitOperationCallExp(OperationCallExp oc) {
						if (!usesAllInstances) {
							usesAllInstances = PredefinedType.ALL_INSTANCES == oc
									.getOperationCode();
							if (usesAllInstances) {
								queryToInit
										.setExtentMap(EcoreEnvironmentFactory.ECORE_INSTANCE
												.createExtentMap(extentContext));
							}
						}
						return super.visitOperationCallExp(oc);
					}
				};
				queryToInit.getExpression().accept(visitior);
			}
		}

		/**
		 * @generated 
		 */
		private static EcoreEnvironmentFactory createCustomEnv(Map environment) {
			final Map env = environment;
			return new EcoreEnvironmentFactory() {
				public Environment createClassifierContext(Object context) {
					Environment ecoreEnv = super
							.createClassifierContext(context);
					for (Iterator it = env.keySet().iterator(); it.hasNext();) {
						String varName = (String) it.next();
						EClassifier varType = (EClassifier) env.get(varName);
						ecoreEnv.addElement(varName,
								createVar(varName, varType), false);
					}
					return ecoreEnv;
				}
			};
		}

		/**
		 * @generated 
		 */
		private static Variable createVar(String name, EClassifier type) {
			Variable var = ExpressionsFactory.eINSTANCE.createVariable();
			var.setName(name);
			var.setType(EcoreEnvironment.getOCLType(type));
			return var;
		}
	}
}
