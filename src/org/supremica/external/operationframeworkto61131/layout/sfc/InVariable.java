package org.supremica.external.operationframeworkto61131.layout.sfc;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;

import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.main.Constant;




public class InVariable extends org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject {

	private String expression = "";

	public InVariable(int localId, String expression) {

		if (expression == null) {

			expression = "error var";
		}

		// round to integer
		int halfHeight = Constant.InVariableHeight.divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		int height = Constant.InVariableHeight.intValue();
		int width = Constant.InVariableWidthUnit.intValue()
				* expression.length()+1;

		// Set default conOut relPosition

		Position conOutRelPosition = new Position(width, halfHeight);

		
		super.setDefault(localId, height, width, null, conOutRelPosition);

		this.expression = expression;

	}

	public org.plcopen.xml.tc6.Body.SFC.InVariable getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.InVariable inVariable = CommonLayoutObject.objectFactory
				.createBodySFCInVariable();

		try {

			super.getPLCopenObject(inVariable);
			super.getCommenConOutPLCOpenObject(inVariable);

			if (!this.expression.isEmpty()) {
				inVariable.setExpression(this.expression);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return inVariable;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}



	

}
