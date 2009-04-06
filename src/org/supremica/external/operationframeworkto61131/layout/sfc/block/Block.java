package org.supremica.external.operationframeworkto61131.layout.sfc.block;
/**
 * @author LC
 *
 */
import java.lang.reflect.Method;

import java.math.BigDecimal;
import java.util.*;

import org.plcopen.xml.tc6.Body;
import org.supremica.external.operationframeworkto61131.layout.common.CommonConnectionIn;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Connection;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.layout.sfc.*;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.JAXButil;
import org.supremica.external.operationframeworkto61131.util.PLCopenXMLParser;
import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;



public class Block extends CommonLayoutObject {

	private String typeName;

	private String instanceName;

	private List<Variable> inputVariableList;

	private List<Variable> inOutVariableList;

	private List<Variable> outputVariableList;

	// FIXME when needed?
	private Body.SFC.Block xmlBlock;

	// private may need to add in/out variable;

	public Block() {

		super();
	}

	public Block(Body.SFC.Block block) {

		super();

		this.xmlBlock = block;
		inputVariableList = new LinkedList<Variable>();
		inOutVariableList = new LinkedList<Variable>();
		outputVariableList = new LinkedList<Variable>();

		if (block.getLocalId() != null) {
			this.setLocalId(block.getLocalId().intValue());
		}

		if (block.getHeight() != null) {
			this.setHeight(block.getHeight().intValue());
		}
		if (block.getWidth() != null) {
			this.setWidth(block.getWidth().intValue());
		}

		if (block.getTypeName() != null) {

			this.typeName = block.getTypeName();
		}

		if (block.getInstanceName() != null) {

			this.instanceName = block.getInstanceName();
		}

		if (block.getPosition() != null) {

			super.setPosition(new Position(block.getPosition()));
		}

		if (block.getInputVariables() != null
				&& block.getInputVariables().getVariable() != null) {

			for (Body.SFC.Block.InputVariables.Variable inputVarialbe : block
					.getInputVariables().getVariable()) {

				this.inputVariableList.add(new Variable(inputVarialbe));

			}

		}

		if (block.getOutputVariables() != null
				&& block.getOutputVariables().getVariable() != null) {

			for (Body.SFC.Block.OutputVariables.Variable outputVarialbe : block
					.getOutputVariables().getVariable()) {

				this.outputVariableList.add(new Variable(outputVarialbe));

			}

		}

		if (block.getInOutVariables() != null
				&& block.getInOutVariables().getVariable() != null) {

			for (Body.SFC.Block.InOutVariables.Variable inoutVarialbe : block
					.getInOutVariables().getVariable()) {

				this.outputVariableList.add(new Variable(inoutVarialbe));

			}

		}

	}

	public Block(int localId, String typeName, String[] inputs, String[] outputs) {

		super();
		this.typeName = typeName;
		inputVariableList = new LinkedList<Variable>();
		inOutVariableList = new LinkedList<Variable>();
		outputVariableList = new LinkedList<Variable>();

		// height will be adjusted according to the names in
		// action.reference

		super.setLocalId(localId);
		super.setWidth(Constant.BlockANDWidth.intValue());
		this.adjustSize(inputs, outputs);

		for (int i = 0; i < inputs.length; i++) {

			this.addToInputVariableList(inputs[i]);

		}

		for (int i = 0; i < outputs.length; i++) {

			this.addToOutputVariableList(outputs[i]);
		}

	}

	public Body.SFC.Block getPLCOpenObject() {

		Body.SFC.Block block = CommonLayoutObject.objectFactory
				.createBodySFCBlock();
	

		Body.SFC.Block.InputVariables inputVariables = CommonLayoutObject.objectFactory
				.createBodySFCBlockInputVariables();

		Body.SFC.Block.OutputVariables outputVariables = CommonLayoutObject.objectFactory
				.createBodySFCBlockOutputVariables();

		Body.SFC.Block.InOutVariables inoutVariables = CommonLayoutObject.objectFactory
				.createBodySFCBlockInOutVariables();

		try {

			super.getPLCopenObject(block);

			block.setTypeName(this.typeName);

			if (this.instanceName != null && !this.instanceName.isEmpty()) {

				block.setInstanceName(this.instanceName);
			}

			if (!this.inputVariableList.isEmpty()) {
				// FIXME could cause problem if other type of value (FBD, IL)are
				// required.

				for (Variable var : this.inputVariableList) {
					Body.SFC.Block.InputVariables.Variable variable = CommonLayoutObject.objectFactory
							.createBodySFCBlockInputVariablesVariable();
					var.getPLCOpenObject(variable);
					inputVariables.getVariable().add(variable);

				}
			}

			if (!this.inOutVariableList.isEmpty()) {
				// FIXME could cause problem if other type of value (FBD, IL)are
				// required.
				for (Variable var : this.inOutVariableList) {
					Body.SFC.Block.InOutVariables.Variable variable = CommonLayoutObject.objectFactory
							.createBodySFCBlockInOutVariablesVariable();
					var.getPLCOpenObject(variable);
					inoutVariables.getVariable().add(variable);

				}
			}

			if (!this.outputVariableList.isEmpty()) {
				// FIXME could cause problem if other type of value (FBD, IL)are
				// required.

				for (Variable var : this.outputVariableList) {
					Body.SFC.Block.OutputVariables.Variable variable = CommonLayoutObject.objectFactory
							.createBodySFCBlockOutputVariablesVariable();
					var.getPLCOpenObject(variable);
					outputVariables.getVariable().add(variable);

				}
			}

			block.setInputVariables(inputVariables);
			block.setInOutVariables(inoutVariables);
			block.setOutputVariables(outputVariables);

		} catch (Exception e) {

			e.printStackTrace();

		}

		return block;

	}

	public void adjustSize(String[] inputs, String[] outputs) {

		// heightUnit*(INs/OUTs+1)
		int height = Constant.BlockANDHeightUnit.intValue()
				* ((inputs.length > outputs.length ? inputs.length

				: outputs.length) + 1);

		super.setHeight(height);

		int extendLength = 0;
		int longestIN = 0;
		int longestOUT = 0;
		for (int i = 0; i < inputs.length; i++) {

			if (inputs[i].length() > longestIN) {

				longestIN = inputs[i].length();
			}
		}

		for (int i = 0; i < outputs.length; i++) {

			if (outputs[i].length() > longestOUT) {

				longestOUT = outputs[i].length();
			}
		}

		extendLength = longestIN + longestOUT;

		if (extendLength < this.typeName.length()) {

			extendLength = this.typeName.length();
		}

		// super.setWidth(Constant.BlockANDHeightUnit.intValue() *
		// extendLength);
		super.setWidth(Constant.ActionBlockWidthExtendUnit.intValue()
				* extendLength);

		// super.setWidth(Constant.BlockANDWidth.intValue());

	}

	// The ConnectionPointInList/ConnectionPointOutList in the super Class
	// CommonLayoutObject
	// is still needed to enable connecting two objects. So when a new
	// inVariable is added,
	// ConnectionPointInList/ConnectionPointOutList in the super Class
	// CommonLayoutObject also needs to be updated
	// with the ConnectionPointIn/ConnectionPointOut in the variable
	public void addToInputVariableList(String formalParameter) {
		// IN is supposed to be on the left of block
		int sizeOfInVariableList = this.inputVariableList.size();
		int relPositionX = 0;
		int relPositionY = (sizeOfInVariableList + 1)
				* Constant.BlockANDHeightUnit.intValue();

		Position relPosition = new Position(relPositionX, relPositionY);

		org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn conIn = new org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn();
		conIn.setRelPosition(relPosition);

		Variable variable = new Variable(formalParameter);
		variable.setConnectionPointIn(conIn);

		this.inputVariableList.add(variable);
		super.addToConnectionPointInList(conIn);

	}

	public void setNegatedInput(int refLocalId) {

		for (Variable variable : this.inputVariableList) {

			for (Connection con : variable.getConnectionPointIn()
					.getConnections()) {

				if (con.getRefLocalId() == refLocalId) {

					variable.setNegated();

				}

			}

		}

	}

	public void addToOutputVariableList(String formalParameter) {

		// OUT is supposed to be on the right of block
		int sizeOfInVariableList = this.outputVariableList.size();
		int relPositionX = super.getWidth();
		int relPositionY = (sizeOfInVariableList + 1)
				* Constant.BlockANDHeightUnit.intValue();

		Position relPosition = new Position(relPositionX, relPositionY);

		org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut conOut = new org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut();
		conOut.setRelPosition(relPosition);

		Variable variable = new Variable(formalParameter);
		variable.setConnectionPointOut(conOut);

		this.outputVariableList.add(variable);
		super.addToConnectionPointOutList(conOut);

	}

	public List<Variable> getInputVariableList() {
		return inputVariableList;
	}

	public void setInputVariableList(List<Variable> inVariableList) {
		this.inputVariableList = inVariableList;
	}

	public List<Variable> getOutputVariableList() {
		return outputVariableList;
	}

	public void setOutputVariableList(List<Variable> outVariableList) {
		this.outputVariableList = outVariableList;
	}

	public String getTypeName() {
		return typeName;
	}

	// override, not applicable in block

	public void setConnectionPointIn(ConnectionPointIn conIn) {

	}

	public void setConnectionPointOut(ConnectionPointOut conOut) {

	}

	public void addToConnectionPointInList(ConnectionPointIn conIn) {

	}

	public void addToConnectionPointOutList(ConnectionPointOut conOut) {

	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public void setConnectionPointInList(
			List<ConnectionPointIn> connectionPointInList) {

	}

	public void setConnectionPointOutList(
			List<ConnectionPointOut> connectionPointOutList) {

	}

	public List<Variable> getInOutVariableList() {
		return inOutVariableList;
	}

	public void setInOutVariableList(List<Variable> inOutVariableList) {
		this.inOutVariableList = inOutVariableList;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	// FIXME override only this one is enough? Call the super method first, then
	// add the negated field.
	public void connectToOut(CommonLayoutObject target,
			ConnectionPointOut conOut, CommonConnectionIn commonConIn,
			Position midWayPoint, Position distanceToTarget) {

		super.connectToOut(target, conOut, commonConIn, midWayPoint,
				distanceToTarget);

		// If the target is negeted, set the Variable refering target.localId to
		// be negated
		if (target.getNegated()) {

			this.setNegatedInput(target.getLocalId());

		}

	}

	private Variable getVariable(List<Variable> variableList,
			String variableName) {

		for (Variable variable : variableList) {

			if (variable.getFormalParameter().equals(variableName)) {

				return variable;

			}

		}

		return null;
	}

	public Variable getInputVariable(String variableName) {

		return getVariable(this.inputVariableList, variableName);
	}

	public Variable getOutputVariable(String variableName) {

		return getVariable(this.outputVariableList, variableName);
	}

	public Variable getInOutputVariable(String variableName) {

		return getVariable(this.inOutVariableList, variableName);
	}

	public org.plcopen.xml.tc6.Body.SFC.Block.InputVariables.Variable getXMLInputVariable(
			String variableName) {

		if (this.xmlBlock == null) {
			return null;
		}
		org.plcopen.xml.tc6.Body.SFC.Block.InputVariables.Variable variable = null;
		for (org.plcopen.xml.tc6.Body.SFC.Block.InputVariables.Variable var : xmlBlock
				.getInputVariables().getVariable()) {

			if (var.getFormalParameter().equals(variableName)) {

				variable = var;
				return variable;
			}

		}

		return null;

	}

	public org.plcopen.xml.tc6.Body.SFC.Block.OutputVariables.Variable getXMLOutputVariable(
			String variableName) {

		if (this.xmlBlock == null) {
			return null;
		}
		org.plcopen.xml.tc6.Body.SFC.Block.OutputVariables.Variable variable = null;
		for (org.plcopen.xml.tc6.Body.SFC.Block.OutputVariables.Variable var : xmlBlock
				.getOutputVariables().getVariable()) {

			if (var.getFormalParameter().equals(variableName)) {

				variable = var;

				return variable;
			}

		}

		return null;

	}
	
//	FIXME need IN/OUT VARIALBE from xmlBlock?

	// override end , not applicable in block

	public static void main(String[] args) {

		String path = "./";
		String configFileName = "config.xml";
		Constant.initialize(path, configFileName);

		org.supremica.external.operationframeworkto61131.util.log.LogUtil log = org.supremica.external.operationframeworkto61131.util.log.LogUtil.getInstance();

		String inputFileName = "output001.xml";

		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		org.plcopen.xml.tc6.Project plcopenProject = (org.plcopen.xml.tc6.Project) JC
				.getRootElementObject(Constant.PLCOPEN_OUT_PUT_FILE_PATH,
						inputFileName);

		// String pouName = "FB_C1F1C1_Two_Position_Movement";
		// String pouName = "IL_IL_C1_41";
		// String pouName = "IL_IL_F1C1_WORK_POS";
		String pouName = "FB_C1F1C1_Two_Position_Movement";
		int refLocalId = 16;

		// String formalParameter = "output backward";
		String formalParameter = null;
		org.plcopen.xml.tc6.Project.Types.Pous.Pou po = null;
		for (org.plcopen.xml.tc6.Project.Types.Pous.Pou pou : plcopenProject
				.getTypes().getPous().getPou()) {

			if (pou.getName().equals(pouName)) {

				po = pou;

			}

		}

		String instanceName = "FB_C1F1C1_Two_Position_Movement";
		PLCopenXMLParser parser = new PLCopenXMLParser(po.getBody().getFBD()
				.getCommentOrErrorOrConnector());

		String[] inputs = { "IN1", "IN2" };

		String[] outputs = { "AND" };
		Block block = new Block(1, "AND", inputs, outputs);
		block = new Block((Body.SFC.Block) parser
				.getObjByInstanceName(instanceName));

		log.info("block.width:" + block.getWidth());
		log.info("block.height:" + block.getHeight());
		log.info("block.localId:" + block.getLocalId());
		log.info("block.type:" + block.getTypeName());

		List<ConnectionPointIn> conInList = new LinkedList<ConnectionPointIn>();

		for (Variable var : block.getInputVariableList()) {

			// ConnectionPointIn conIn = var.getConnectionPointIn();

			conInList.add(var.getConnectionPointIn());
		}

		int i = 0;
		// the return value is live list
		for (Variable var : block.getInputVariableList()) {

			Connection con = new Connection();

			con.addPosition(new Position(2 + i, 6 + i));
			con.addPosition(new Position(5 + i, 4 + i));
			con.setRefLocalId(i);

			// ConnectionPointIn conIn = var.getConnectionPointIn();

			// conIn.addToConnections(con);

			conInList.get(i).addToConnections(con);

			i++;

		}

		for (Variable var : block.getInputVariableList()) {

			Connection con = var.getConnectionPointIn().getConnections().get(0);
			log.info("localId:" + con.getRefLocalId());
			log.info("Position:" + con.getPositionList().get(0));
			log.info("Position:" + con.getPositionList().get(1));
		}

		ConnectionPointIn con2 = conInList.get(0);

		con2.setRelPosition(new Position(100, 100));

		conInList.add(con2);

		for (Variable var : block.getInputVariableList()) {

			Connection con = var.getConnectionPointIn().getConnections().get(0);
			log.info("refposition:"
					+ var.getConnectionPointIn().getRelPosition());
			log.info("localId:" + con.getRefLocalId());
			log.info("Position:" + con.getPositionList().get(0));
			log.info("Position:" + con.getPositionList().get(1));
		}

		log.info("size:" + conInList.size());

		Object blockObj = block;
		Method[] methods = blockObj.getClass().getMethods();
		try {

			for (int j = 0; j < methods.length; j++) {

				if (methods[j].getName().contains("get")) {

					Class[] paramTypes = methods[j].getParameterTypes();

					if (paramTypes.length == 0) {

						Object obj = methods[j].invoke(blockObj, new Object[0]);

						if (obj != null) {
							log.info("Method:" + methods[j].getName());
							log.info("Class:" + obj.getClass().getSimpleName());

							if (obj.getClass().equals(String.class)) {

								log.info("hahah");
							}
						}
					}
				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
