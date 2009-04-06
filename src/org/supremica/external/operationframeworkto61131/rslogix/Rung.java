package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.lang.StringBuffer;


import org.plcopen.xml.tc6.Project.Types.Pous.Pou;
import org.plcopen.xml.tc6.Project.Types.Pous.Pou.Interface;
import org.plcopen.xml.tc6.Body.SFC.LeftPowerRail;
import org.plcopen.xml.tc6.StorageModifierType;
import org.plcopen.xml.tc6.EdgeModifierType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.Set;
import org.plcopen.xml.tc6.Connection;
import org.plcopen.xml.tc6.ConnectionPointIn;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.rslogix.ladder.*;
import org.supremica.external.operationframeworkto61131.util.FileUtil;
import org.supremica.external.operationframeworkto61131.util.JAXButil;
import org.supremica.external.operationframeworkto61131.util.PLCopenXMLParser;
import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;


import com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaParsingConfig;

public class Rung extends CommonText {

	public static final String RUNG_TYPE_N = "N";
	public static final String CONTACT_NORMAL = "XIC";
	public static final String CONTACT_NEGATED = "XIO";
	public static final String COIL_NORMAL = "OTE";
	public static final String COIL_SET = "OTL";
	public static final String COIL_RESET = "OTU";
	public static final String COIL_RISING = "ONR";
	public static final String COIL_FALLING = "ONF";

	private static final String METHOD_IS_NEGATED = "isNegated";
	private static final String METHOD_GET_EDGE = "getEdge";
	private static final String METHOD_GET_STORAGE = "getStorage";

	private List<Object> pouElements;
	private String type = RUNG_TYPE_N;

	private BigInteger startLocalId = BigInteger.ZERO;
	private BigInteger endLocalId = BigInteger.ZERO;

	private HashMap<BigInteger, CommonObj> commonObjHashMap = new HashMap<BigInteger, CommonObj>();

	public Rung(org.plcopen.xml.tc6.Body.LD ld) {

		this.pouElements = ld.getCommentOrErrorOrConnector();

		getCommonObjList();
	}

	public Rung() {
	}

	public String getText(int nTabs) {

		String tabs = CommonText.getTabs(nTabs);

		// StringBuffer buf = new StringBuffer();
		//
		// buf.append(tabs);
		//
		// buf.append(this.type).append(COLON);

		log.info("Starting localId:" + startLocalId);
		log.info("End localId:" + endLocalId);

		Node lastNode = generateNode(endLocalId);
	
		mergeBranchNode(lastNode);

		String vv = "";
		while (true) {

			vv = lastNode.getText() + vv;

			lastNode = lastNode.getNext();

			if (lastNode == null) {

				break;
			}
		}

		vv = this.getType() + COLON + vv + SEMICOLON + NEW_LINE;
		vv = tabs + vv;
		return vv;
	}

	public String getText_test(int nTabs) {

		String tabs = CommonText.getTabs(nTabs);

		log.info("Starting localId:" + startLocalId);
		log.info("End localId:" + endLocalId);

		Node lastNode = generateNode(endLocalId);
		mergeBranchNode(lastNode);

		String vv = "";
		while (true) {

			vv = lastNode.getText_test() + vv;

			lastNode = lastNode.getNext();

			if (lastNode == null) {

				break;
			}
		}

		vv = this.getType() + COLON + vv + SEMICOLON + NEW_LINE;
		vv = tabs + vv;
		return vv;
	}

	private void getCommonObjList() {

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			for (Object obj : pouElements) {

				CommonObj commonObj = new CommonObj();

				BigInteger localId = (BigInteger) reflectionUtil.invokeMethod(
						obj, "getLocalId");

				commonObj.setLocalId(localId);

				Class typeClass = obj.getClass();

				if (typeClass
						.equals(org.plcopen.xml.tc6.Body.SFC.LeftPowerRail.class)) {

					commonObj.setStart();
					startLocalId = localId;
					commonObj.setVariable("LeftPowerRail");

					commonObjHashMap.put(localId, commonObj);
					continue;

				} else if (typeClass
						.equals(org.plcopen.xml.tc6.Body.SFC.RightPowerRail.class)) {
					commonObj.setEnd();
					endLocalId = localId;
					commonObj.setVariable("RightPowerRail");

					List<ConnectionPointIn> connectionPointInList = (List<ConnectionPointIn>) reflectionUtil
							.invokeMethod(obj, "getConnectionPointIn");

					// FIXME maybe need to put different connectionPointIn to
					// different Rung

					for (ConnectionPointIn connectionPointIn : connectionPointInList) {

						List<Connection> connections = connectionPointIn
								.getConnection();

						for (Connection connection : connections) {

							BigInteger refLocalId = connection.getRefLocalId();

							if (refLocalId != null) {

								commonObj.addPredecessor(refLocalId);
							}

						}
					}

					commonObjHashMap.put(localId, commonObj);
					continue;

				} else if (typeClass
						.equals(org.plcopen.xml.tc6.Body.SFC.Contact.class)) {

					// Parse contact qualifier
					commonObj.setType(CommonObj.CONTACT);

					Boolean isNegated = (Boolean) reflectionUtil.invokeMethod(
							obj, METHOD_IS_NEGATED);
					if (isNegated) {

						commonObj.setQualifier(CONTACT_NEGATED);

					} else {
						commonObj.setQualifier(CONTACT_NORMAL);
					}

					// FIXME parse coil properties edge="rising"/edge="falling"

				} else if (typeClass
						.equals(org.plcopen.xml.tc6.Body.SFC.Coil.class)) {

					// Parse coil qualifier
					commonObj.setType(CommonObj.COIL);

					Boolean isNegated = (Boolean) reflectionUtil.invokeMethod(
							obj, METHOD_IS_NEGATED);

					StorageModifierType storageModifierType = (StorageModifierType) reflectionUtil
							.invokeMethod(obj, METHOD_GET_STORAGE);

					EdgeModifierType edgeModifierType = (EdgeModifierType) reflectionUtil
							.invokeMethod(obj, METHOD_GET_EDGE);

					if (isNegated) {

						// FIXME fix negated coil
						commonObj.setQualifier(COIL_NORMAL);
					} else if (storageModifierType
							.equals(StorageModifierType.NONE)
							&& edgeModifierType.equals(EdgeModifierType.NONE)) {

						commonObj.setQualifier(COIL_NORMAL);

					} else if (storageModifierType
							.equals(StorageModifierType.SET)) {

						commonObj.setQualifier(COIL_SET);

					} else if (storageModifierType
							.equals(StorageModifierType.RESET)) {

						commonObj.setQualifier(COIL_RESET);
					} else if (edgeModifierType.equals(EdgeModifierType.RISING)) {

						commonObj.setQualifier(COIL_RISING);
					} else if (edgeModifierType
							.equals(EdgeModifierType.FALLING)) {

						commonObj.setQualifier(COIL_FALLING);
					} else {

						log
								.error("Cannot parse Coil's qualifier, Coil localId:"
										+ localId);
					}

				} else {

					log.error("Cannot parse obj of type: " + typeClass);
				}

				if (reflectionUtil.hasMethod(obj, "getVariable")) {

					String variable = (String) reflectionUtil.invokeMethod(obj,
							"getVariable");
					commonObj.setVariable(variable);

				}

				if (reflectionUtil.hasMethod(obj, "getConnectionPointIn")) {
					ConnectionPointIn connectionPointIn = (org.plcopen.xml.tc6.ConnectionPointIn) reflectionUtil
							.invokeMethod(obj, "getConnectionPointIn");

					List<Connection> connections = connectionPointIn
							.getConnection();

					for (Connection connection : connections) {

						BigInteger refLocalId = connection.getRefLocalId();

						if (refLocalId != null) {

							commonObj.addPredecessor(refLocalId);
						}

					}
				}

				commonObjHashMap.put(localId, commonObj);

			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private Node generateNode(BigInteger localId) {

		log.info("Generate Node lcoalId:" + localId);

		CommonObj commonObj = this.commonObjHashMap.get(localId);
		Node node = new Node(commonObj);

		// When read in the left power rail, the node has no Next field.
		if (localId.equals(this.startLocalId)) {

			return node;
		}

		Node nextNode = new Node();
		List<BigInteger> predecessorList = commonObj.getPredecessor();

		for (BigInteger bigInteger : predecessorList) {

			log.info("predecessor:" + bigInteger);
		}

		if (predecessorList.size() > 1) {

			nextNode = generateBranch(predecessorList);

		} else if (predecessorList.size() == 1) {

			nextNode = generateNode(predecessorList.get(0));

		} else {

			log.error("Unlinked node, localId:" + commonObj.getLocalId()
					+ " variable:" + commonObj.getVariable());
			return null;
		}
		node.setNext(nextNode);
		return node;
	}

	private Node generateBranch(List<BigInteger> predecessorList) {

		Node branchNode = new Node();

		for (BigInteger refLocalId : predecessorList) {

			Node branchElement = generateNode(refLocalId);

			if (branchElement != null) {

				Branch branch = new Branch();
				branch.addNode(branchElement);
				branchNode.addBranch(branch);
			}

		}

		getBranchEndNode(branchNode);
		return branchNode;

	}

	// return the node that all branches have in common
	private Node getBranchEndNode(Node branchNode) {

		List<Branch> branches = branchNode.getBranches();

		log.info("Node to trim:" + branchNode.getText());

		if (branches.size() == 1) {

			return branches.get(0).getNodeList().get(0);
		}
		Branch branch0 = branches.get(0);

		// if (branch0.getNodeList().size() < 2) {
		//
		// log.error("Found shortened connection");
		// }

		Node lastNode = null;
		for (int j = branch0.getNodeList().size() - 1; j >= 0; j--) {

			Node endNode = branch0.getNodeList().get(j);

			log.info("Check node:" + endNode.getText());

			if (lastNode == null) {

				lastNode = endNode;
			}

			Boolean isBranchEnd = false;

			for (int i = 1; i < branches.size(); i++) {

				if (branches.get(i).contains(endNode)) {

					log.info("branch contains endNode:"
							+ branches.get(i).getText());
					continue;

				} else {
					isBranchEnd = true;
					break;
				}

			}

			if (isBranchEnd) {
				trimBranchNode(branchNode, lastNode);

				// When the last node to be trimmed is a branch node, generate a
				// branch. Or generate a normal node.
				if (lastNode.getBranches().size() > 0) {

					List<Node> nodeListOfFirstBranch = branchNode.getBranches()
							.get(0).getNodeList();
					Node nextNode = generateBranch(nodeListOfFirstBranch.get(
							nodeListOfFirstBranch.size() - 1).getCommonObj()
							.getPredecessor());

					branchNode.setNext(nextNode);

				} else {

					branchNode.setNext(generateNode(lastNode.getCommonObj()
							.getLocalId()));

				}

				return lastNode;

			} else {

				lastNode = endNode;
				continue;
			}

		}

		log.error("Cannot find branch end");

		return null;

	}

	private void trimBranchNode(Node branchNode, Node endNode) {

		if (endNode.getBranches().size() > 0) {
			log.info("endNode :" + endNode.getText());
		} else {

			log.info("endNode:" + endNode.getCommonObj().getVariable());
		}

		for (Branch branch : branchNode.getBranches()) {

			branch.trimToNode(endNode);

		}

	}

	// Simplify branch node that has a premature convergence, the input node
	// should be the first node
	private void mergeBranchNode(Node branchNode) {

		if (branchNode == null) {

			return;
		}

		// branches==0, normal node, skip;
		// branches==1, single branch, skip;
		if (branchNode.getBranches().size() < 2) {

			mergeBranchNode(branchNode.getNext());

		} else {

			List<Branch> branches = branchNode.getBranches();

			Boolean isMerged = false;

			for (Branch branch : branches) {

				List<Node> nodeList = branch.getNodeList();

				for (int i = 1; i < nodeList.size(); i++) {

					Node iNode = nodeList.get(i);

					List<Branch> branchesToMerge = new LinkedList<Branch>();
					for (Branch iBranch : branchNode.getBranches()) {

						if (iBranch.contains(iNode)) {

							branchesToMerge.add(iBranch);

						}

					}

					if (branchesToMerge.size() > 1) {
						branches.removeAll(branchesToMerge);
						// The new branch node at the beginning of the branch
						Node mergedBrachNode = new Node();

						Branch mergedBrach = new Branch();
						List<Node> mergedNodeList = null;
						for (Branch iBranch : branchesToMerge) {

							mergedNodeList = iBranch.trimToNode(iNode);

							mergedBrachNode.addBranch(iBranch);

						}
						// merge the newly generated node also
						mergeBranchNode(mergedBrachNode);
						mergedBrach.addNode(mergedBrachNode);
						mergedBrach.getNodeList().addAll(mergedNodeList);

						branches.add(mergedBrach);

						// check if there is more branch again
						mergeBranchNode(branchNode);
						isMerged = true;
					} else {

						continue;
					}

				}

				if (isMerged) {

					break;
				}

			}

			mergeBranchNode(branchNode.getNext());

		}

	}

	public void test() {

		String path = "./";
		String configFileName = "config.xml";
		Constant.initialize(path, configFileName);

		org.plcopen.xml.tc6.Project plcopenProject = null;
		String manualInputFile = "C:\\Documents and Settings\\HAHA\\Desktop\\ladderTest.xml";

		String pouName = "IL_Test";

		// Create the PLCopen xml schema object tree.
		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		if (manualInputFile != null && !manualInputFile.isEmpty()) {

			plcopenProject = (org.plcopen.xml.tc6.Project) JC
					.getRootElementObject(manualInputFile);
		}

		FileUtil file = new FileUtil(
				"C:\\Documents and Settings\\HAHA\\Desktop\\test.txt");

		org.plcopen.xml.tc6.Project.Types.Pous.Pou po = null;
		for (org.plcopen.xml.tc6.Project.Types.Pous.Pou pou : plcopenProject
				.getTypes().getPous().getPou()) {

			if (pou.getName().equals(pouName)) {

				po = pou;

			}

		}

		Rung rung = new Rung(po.getBody().getLD());

		log.info(rung.getText(5));

		Tags tags = new Tags(po.getInterface());

		log.info(tags.getText(3));

		// HashMap<BigInteger, CommonObj> map = rung.getCommonObjList();
		//				
		// for (BigInteger key : map.keySet()) {
		//				
		// printCommonObj(map.get(key));
		// }

	}

	private void printCommonObj(CommonObj obj) {

		log.info("LocalId:" + obj.getLocalId());

		log.info("Varaible:" + obj.getVariable());

		for (BigInteger pre : obj.getPredecessor()) {

			log.info("predecessor:" + pre.toString());

		}

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static void main(String[] args) {

		Rung rung = new Rung();
		rung.test();

	}

}
