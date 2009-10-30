package org.supremica.external.avocades.specificationsynthesis;

import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.w3c.dom.Document;


    // This adapter converts the current Document (a DOM) into a JTree model.
    public class Dom implements javax.swing.tree.TreeModel {

		static Document document;

		public Dom(Document doc) {
			document = doc;
		}

		// Basic TreeModel operations
		public Object  getRoot() {
			return new Node(document);
		}


		public boolean isLeaf(Object aNode) {
			Node node = (Node) aNode;
			if (node.childCount() > 0) return false;
			return true;
		}


		public int getChildCount(Object parent) {
			Node node = (Node) parent;
			return node.childCount();
		}


		public Object getChild(Object parent, int index) {
			Node node = (Node) parent;
			return node.child(index);
		}


		public int getIndexOfChild(Object parent, Object child) {
			Node node = (Node) parent;
			return node.index((Node) child);
		}


		public void valueForPathChanged(TreePath path, Object newValue) {}

      private Vector<TreeModelListener> listenerList = new Vector<TreeModelListener>();

      public void addTreeModelListener(TreeModelListener listener) {
        if ( listener != null
        && ! listenerList.contains( listener ) ) {
           listenerList.addElement( listener );
        }
      }

      public void removeTreeModelListener(TreeModelListener listener) {
        if ( listener != null ) {
           listenerList.removeElement( listener );
        }
      }
    }