package org.supremica.external.operationframeworkto61131.layout.sfc;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;

import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.main.Constant;




public class OutVariable extends org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject {

	private String expression = "";

	public OutVariable(int localId, String expression) {

		if (expression == null) {

			expression = "error var";
		}

		// round to integer
		int halfHeight = Constant.InVariableHeight.divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		int height = Constant.InVariableHeight.intValue();
		int width = Constant.InVariableWidthUnit.intValue()
				* expression.length() + 1;

		// Set default conOut relPosition

		Position conInRelPosition = new Position(0, halfHeight);

		super.setDefault(localId, height, width,conInRelPosition,null);

		this.expression = expression;

	}

	public org.plcopen.xml.tc6.Body.SFC.OutVariable getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.OutVariable outVariable = CommonLayoutObject.objectFactory
				.createBodySFCOutVariable();

		try {

			super.getPLCopenObject(outVariable);
			super.getCommenConInPLCOpenObject(outVariable);

			if (!this.expression.isEmpty()) {
				outVariable.setExpression(this.expression);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return outVariable;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

}
