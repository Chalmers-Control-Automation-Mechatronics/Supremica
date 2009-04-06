package org.supremica.external.operationframeworkto61131.layout.sfc.block;
/**
 * @author LC
 *
 */
import org.plcopen.xml.tc6.Body;
import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;




public class Variable {

	// <variable formalParameter="IN3">
	// <connectionPointIn>
	// <relPosition y="105" x="0" />
	// <connection refLocalId="10">
	// <position y="401" x="336" />
	// <position y="401" x="316" />
	// </connection>
	// </connectionPointIn>
	// </variable>

	// FIXME may need edgeModifier storageModifier in Class field

	private String formalParameter = "";

	private boolean negated = false;

	private org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn connectionPointIn;

	private org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut;

	public Variable(String formalParameter) {

		if (formalParameter != null) {
			this.formalParameter = formalParameter;
		}

	}

	public Variable(Object variableObj) {

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {
			if (reflectionUtil.hasMethod(variableObj, "getFormalParameter")) {

				Object value = reflectionUtil.invokeMethod(variableObj,
						"getFormalParameter");
				if (value != null) {

					this.formalParameter = (String) value;
				}

			}
			
			if (reflectionUtil.hasMethod(variableObj, "isNegated")) {

				Object value = reflectionUtil.invokeMethod(variableObj,
						"isNegated");
				if (value != null) {

					this.negated = (Boolean) value;
				}

			}

			if (reflectionUtil.hasMethod(variableObj, "getConnectionPointIn")) {

				Object value = reflectionUtil.invokeMethod(variableObj,
						"getConnectionPointIn");
				if (value != null) {

					this.connectionPointIn = new org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn(
							(org.plcopen.xml.tc6.ConnectionPointIn) value);
				}

			}

			if (reflectionUtil.hasMethod(variableObj, "getConnectionPointOut")) {

				Object value = reflectionUtil.invokeMethod(variableObj,
						"getConnectionPointOut");
				if (value != null) {

					this.connectionPointOut = new org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut(
							(org.plcopen.xml.tc6.ConnectionPointOut) value);
					
//					Only for connection , formalParameter does not exist in connectionPointOut
					if(formalParameter!=null){
						connectionPointOut.setFormalParameter(formalParameter);
						
					}
				}

			}

		} catch (Exception e) {

			e.printStackTrace();
			return;
		}

	}

	// org.plcopen.xml.tc6.Body.SFC.Block.InputVariables.Variable
	// org.plcopen.xml.tc6.Body.SFC.Block.InputVariables.Variable variable =
	// CommenLayoutObject.objectFactory
	// .createBodySFCBlockInputVariablesVariable();
	public void getPLCOpenObject(Object subClassObject) {

		ReflectionUtil reflectionUtil = new ReflectionUtil();

		try {

			if (this.negated == true) {

				reflectionUtil.invokeMethod(subClassObject, "setNegated",
						Boolean.TRUE);

			}

			if (this.formalParameter != null && !this.formalParameter.isEmpty()) {

				reflectionUtil.invokeMethod(subClassObject,
						"setFormalParameter", this.formalParameter);
			}

			if (this.connectionPointIn != null) {

				reflectionUtil.invokeMethod(subClassObject,
						"setConnectionPointIn", connectionPointIn
								.getPLCOpenObject());

			}

			if (this.connectionPointOut != null) {

				reflectionUtil.invokeMethod(subClassObject,
						"setConnectionPointOut", connectionPointOut
								.getPLCOpenObject());

			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn getConnectionPointIn() {
		return connectionPointIn;
	}

	public void setConnectionPointIn(
			org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn connectionPointIn) {
		if (connectionPointIn != null) {
			this.connectionPointIn = connectionPointIn;
		}
	}

	public org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut getConnectionPointOut() {
		return connectionPointOut;
	}

	public void setConnectionPointOut(
			org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut) {
		if (connectionPointOut != null) {
			this.connectionPointOut = connectionPointOut;
			
			if(this.formalParameter!=null){
				connectionPointOut.setFormalParameter(formalParameter);
				
			}
		}
		
		
	}

	public String getFormalParameter() {
		return formalParameter;
	}

	public void setNegated() {

		this.negated = true;

	}

}
