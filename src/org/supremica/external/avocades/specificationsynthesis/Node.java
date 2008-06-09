package org.supremica.external.avocades.specificationsynthesis;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;

public class Node {

		org.w3c.dom.Node domNode;

		final String[] typeName = {
			"none",
			"Element",
			"Attr",
			"Text",
			"CDATA",
			"EntityRef",
			"Entity",
			"ProcInstr",
			"Comment",
			"Document",
			"DocType",
			"DocFragment",
			"Notation",
		};


		// Construct an Adapter node from a DOM node
		public Node(org.w3c.dom.Node node) {
			domNode = node;
		}

		public String toString() {
			String s = typeName[domNode.getNodeType()];
			String nodeName = domNode.getNodeName();

			if (! nodeName.startsWith("#")) {
			s += ": " + nodeName;
			}
			if (domNode.getNodeValue() != null) {
			if (s.startsWith("ProcInstr"))
			s += ", ";
			else
			s += ": ";

			// Trim the value to get rid of NL's at the front
			String t = domNode.getNodeValue().trim();
			int x = t.indexOf("\n");
			if (x >= 0) t = t.substring(0, x);
			s += t;
			}
		return s;
		}


		public int index(Node child) {
		int count = childCount();
		for (int i=0; i<count; i++) {
		Node n = this.child(i);
		if (child.domNode == n.domNode) return i;
		}
		return -1;
		}

		public Node child(int searchIndex) {
		org.w3c.dom.Node node = domNode.getChildNodes().item(searchIndex);
		return new Node(node);
		}

		public int childCount() {
		return domNode.getChildNodes().getLength();
		}


}