package org.supremica.external.operationframeworkto61131.rslogix.ladder;
/**
 * @author LC
 *
 */
import java.lang.StringBuffer;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.supremica.external.operationframeworkto61131.rslogix.CommonText;
import org.supremica.external.operationframeworkto61131.rslogix.Rung;



public class Node extends CommonText {

	private List<Branch> brancheList = new LinkedList<Branch>();

	private String type;

	private CommonObj commonObj;

	private Node next = null;

	public Node(CommonObj obj) {

		commonObj = obj;

	}

	public String getText() {
		StringBuffer buf = new StringBuffer();
		if (commonObj != null) {

			return this.getExpression();

		} else if (brancheList.size() == 1) {

			for (int i = 0; i < brancheList.size(); i++) {

				buf.append(brancheList.get(i).getText());

				if (i < brancheList.size() - 1) {
					buf.append(COMMA);
				}

			}

		} else if (brancheList.size() > 1) {

			buf.append(LEFT_SQUARE_BRACKET);

			for (int i = 0; i < brancheList.size(); i++) {

				buf.append(brancheList.get(i).getText());

				if (i < brancheList.size() - 1) {
					buf.append(COMMA);
				}

			}

			buf.append(RIGHT_SQUARE_BRACKET);

			return buf.toString();
		} else {
			log.error("Node does not have variable nor branches");
		}

		return null;

	}

	private String getExpression() {

		if (commonObj.isStart() || commonObj.isEnd()) {

			return "";
		}
		StringBuffer buf = new StringBuffer();

		buf.append(commonObj.getQualifier());

		buf.append(LEFT_ROUND_BRACKET);
		buf.append(commonObj.getVariable());
		buf.append(RIGHT_ROUND_BRACKET);

		return buf.toString();
	}

	public String getText_test() {

		if (commonObj != null) {

			if (commonObj.isStart() || commonObj.isEnd()) {

				return "";
			}

			return commonObj.getVariable();

		} else if (brancheList.size() == 1) {

			log.info("signle branch");

		} else if (brancheList.size() > 1) {
			StringBuffer buf = new StringBuffer();
			buf.append(LEFT_SQUARE_BRACKET);

			for (int i = 0; i < brancheList.size(); i++) {

				buf.append(brancheList.get(i).getText_test());

				if (i < brancheList.size() - 1) {
					buf.append(COMMA);
				}

			}

			buf.append(RIGHT_SQUARE_BRACKET);

			return buf.toString();
		} else {
			log.error("Node does not have variable nor branches");
		}

		return null;

	}

	// public void mergeBranch() {
	//
	// if (this.brancheList.size() > 1) {
	//
	// Branch branch = brancheList.get(0);
	//
	// Node nodeLast = getLastNode(branch);
	//
	// if (nodeLast.getCommonObj() != null) {
	//
	// for (int i = 1; i < brancheList.size(); i++) {
	//
	// Branch iBranch = brancheList.get(i);
	// // TODO HERE merge here
	// }
	//
	// } else {
	//
	// nodeLast.mergeBranch();
	//
	// }
	//
	// }
	//
	// }

	public Boolean equals(Node node) {

		if (this.commonObj != null && node.getCommonObj() != null) {

			if (commonObj.getLocalId().equals(node.getCommonObj().getLocalId())) {

				return true;
			} else {
				return false;
			}

		} else if (brancheList.size() > 0 && node.getBranches().size() > 0) {

			if (node.getText().equals(this.getText())) {

				return true;

			} else {

				return false;
			}

		} else {

			return false;
		}

	}

	// private Node getLastNode(Branch branch) {
	//
	// return branch.getNodeList().get(branch.getNodeList().size() - 1);
	// }

	public Node() {
	}

	public void addBranch(Branch branch) {

		brancheList.add(branch);
	}

	public List<Branch> getBranches() {
		return brancheList;
	}

	public void setBranches(List<Branch> brancheList) {
		this.brancheList = brancheList;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}

	public CommonObj getCommonObj() {
		return commonObj;
	}

	public void setCommonObj(CommonObj commonObj) {
		this.commonObj = commonObj;
	}

	public static void testNode() {

		CommonObj commonObj = new CommonObj();

		commonObj.setType(CommonObj.CONTACT);

		commonObj.setVariable("testVaraible");

		Node node = new Node(commonObj);

		System.out.println("Node text Contact:" + node.getText());

		CommonObj commonObj2 = new CommonObj();

		commonObj2.setType(CommonObj.COIL);

		commonObj2.setVariable("testVaraible");

		Node node2 = new Node(commonObj2);

		System.out.println("Node text Coil:" + node2.getText());
	}

	public static void testBranches() {

		CommonObj commonObj = new CommonObj();

		commonObj.setType(CommonObj.CONTACT);

		commonObj.setVariable("testVaraible1");

		Node node = new Node(commonObj);

		CommonObj commonObj2 = new CommonObj();

		commonObj2.setType(CommonObj.CONTACT);

		commonObj2.setVariable("testVaraible2");

		Node node2 = new Node(commonObj2);

		CommonObj commonObj3 = new CommonObj();

		commonObj3.setType(CommonObj.CONTACT);

		commonObj3.setVariable("testVaraible3");

		Node node3 = new Node(commonObj3);

		Branch branch = new Branch();

		branch.addNode(node);
		branch.addNode(node2);

		Branch branch2 = new Branch();

		branch2.addNode(node2);
		branch2.addNode(node3);
		branch2.addNode(node);

		Node node4 = new Node();

		node4.addBranch(branch);
		node4.addBranch(branch2);

		System.out.println("Node text branches:" + node4.getText());

		CommonObj commonObj5 = new CommonObj();

		commonObj5.setType(CommonObj.COIL);

		commonObj5.setVariable("coil5");

		Node node5 = new Node(commonObj5);

		node5.setNext(node4);

		StringBuffer buf = new StringBuffer();

		Node lastNode = node5;

		String vv = "";
		while (true) {

			vv = lastNode.getText() + vv;

			lastNode = lastNode.getNext();

			if (lastNode == null) {

				break;
			}
		}

		vv = Rung.RUNG_TYPE_N + COLON + vv + SEMICOLON;

		System.out.println("Node text branches and a coil:" + vv);
	}

	public static void main(String[] args) {

	}

}
