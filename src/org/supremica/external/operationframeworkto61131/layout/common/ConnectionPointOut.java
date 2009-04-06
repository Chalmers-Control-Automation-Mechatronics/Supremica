package org.supremica.external.operationframeworkto61131.layout.common;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;
import java.math.BigInteger;

import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;




public class ConnectionPointOut {

	Position relPosition;

	String expression;

	String formalParameter;

	public ConnectionPointOut() {
	}

	public ConnectionPointOut(
			org.plcopen.xml.tc6.ConnectionPointOut connectionPointOut) {

		if (connectionPointOut.getExpression() != null) {
			this.expression = connectionPointOut.getExpression();

		}

		if (connectionPointOut.getRelPosition() != null) {

			this.relPosition = new Position(connectionPointOut.getRelPosition());
		}

	}

	public ConnectionPointOut(Position relPosition) {

		this.relPosition = relPosition;
	}

	public org.plcopen.xml.tc6.ConnectionPointOut getPLCOpenObject() {

		org.plcopen.xml.tc6.ConnectionPointOut connectionPointOut = CommonLayoutObject.objectFactory
				.createConnectionPointOut();

		if (relPosition != null) {
			connectionPointOut.setRelPosition(relPosition.getPLCOpenObject());
		}

		if (expression != null && !expression.isEmpty()) {

			connectionPointOut.setExpression(expression);

		}

		return connectionPointOut;

	}

	// For PLCobject that has its own connectionPointOut Type,
	// Such as Step.ConnectionPointOut Step.ConnectionPointOutAction,
	// But the object has the same method and fields as the common
	// ConnectionPointOut
	public void getPLCopenObject(Object subClassObject) throws Exception {

		ReflectionUtil reflectionUtil = new ReflectionUtil();

		if (relPosition != null) {
			reflectionUtil.invokeMethod(subClassObject, "setRelPosition",
					relPosition.getPLCOpenObject());
		}

		if (expression != null && !expression.isEmpty()) {

			reflectionUtil.invokeMethod(subClassObject, "setExpression",
					expression);
		}

		if (formalParameter != null && !formalParameter.isEmpty()) {

			if (reflectionUtil.hasMethod(subClassObject, "setFormalParameter")) {
				reflectionUtil.invokeMethod(subClassObject,
						"setFormalParameter", formalParameter);
			}

		} else {

			if (reflectionUtil.hasMethod(subClassObject, "setFormalParameter")) {
				reflectionUtil.invokeMethod(subClassObject,
						"setFormalParameter", "");
			}

		}

	}

	public String getFormalParameter() {
		return formalParameter;
	}

	public void setFormalParameter(String formalParameter) {
		this.formalParameter = formalParameter;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Position getRelPosition() {
		return new Position(relPosition);
	}

	public void setRelPosition(Position relPosition) {
		this.relPosition = new Position(relPosition);
	}

}
