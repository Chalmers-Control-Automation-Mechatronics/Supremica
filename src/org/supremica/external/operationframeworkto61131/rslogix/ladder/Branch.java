package org.supremica.external.operationframeworkto61131.rslogix.ladder;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;
import java.lang.StringBuffer;
import java.math.BigInteger;

import org.supremica.external.operationframeworkto61131.rslogix.CommonText;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;



public class Branch extends CommonText {

	List<Node> nodeList = new LinkedList<Node>();

	public void addNode(Node node) {

		if (nodeList.size() > 0) {

			nodeList.get(nodeList.size() - 1).setNext(node);
		}

		this.nodeList.add(node);

		if (node.getNext() != null) {

			addNode(node.getNext());
		}

	}

	public void fromBranch(Branch branch) {

		
		this.nodeList = branch.getNodeList();

	}

	public Boolean contains(Node node) {

		for (Node iNode : nodeList) {

			if (node.equals(iNode)) {

				return true;
			}

		}

		return false;

	}

	public Boolean equals(Branch branch) {

		if (branch.getText().equals(getText())) {

			return true;
		} else {

			return false;
		}

	}

	public List<Node> trimToNode(Node nodeToTrimTo) {

		int indexOfToTrimNode = 0;
		for (int i = 0; i < nodeList.size(); i++) {

			if (nodeToTrimTo.equals(nodeList.get(i))) {

				indexOfToTrimNode = i;
				break;
			}

		}
		List<Node> toRemove = new LinkedList<Node>();
		for (int i = indexOfToTrimNode; i < nodeList.size(); i++) {
			toRemove.add(nodeList.get(i));
		}

		nodeList.removeAll(toRemove);
		
		return toRemove;

	}
	public String getText() {

		StringBuffer buf = new StringBuffer();

		for (int i = nodeList.size() - 1; i >= 0; i--) {

			buf.append(nodeList.get(i).getText());
			buf.append(SPACE);
		}

		return buf.toString();

	}


	public String getText_test() {

		StringBuffer buf = new StringBuffer();

		for (int i = nodeList.size() - 1; i >= 0; i--) {

			buf.append(nodeList.get(i).getText_test());
			buf.append(SPACE);
		}

		return buf.toString();

	}

	public List<Node> getNodeList() {
		return nodeList;
	}

	public static void main(String[] args) {

		CommonObj commonObj = new CommonObj();
		commonObj.setLocalId(BigInteger.valueOf(1));
		commonObj.setType(CommonObj.CONTACT);

		commonObj.setVariable("testVaraible1");

		Node node = new Node(commonObj);

		CommonObj commonObj2 = new CommonObj();

		commonObj2.setType(CommonObj.CONTACT);
		commonObj2.setLocalId(BigInteger.valueOf(2));
		commonObj2.setVariable("testVaraible2");

		Node node2 = new Node(commonObj2);

		CommonObj commonObj3 = new CommonObj();

		commonObj3.setType(CommonObj.COIL);
		commonObj3.setLocalId(BigInteger.valueOf(3));
		commonObj3.setVariable("testVaraible3");

		Node node3 = new Node(commonObj3);

		Branch branch = new Branch();

		branch.addNode(node);
		branch.addNode(node2);
		branch.addNode(node3);

		System.out.println("branch.getText():" + branch.getText());

		branch.trimToNode(node2);
		System.out.println("branch.getText():" + branch.getText());

	}

}
