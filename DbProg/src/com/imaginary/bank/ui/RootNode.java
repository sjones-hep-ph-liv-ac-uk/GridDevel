package com.imaginary.bank.ui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.tree.TreeNode;

public class RootNode implements TreeNode {
    private ArrayList nodes = new ArrayList();

    static public class IteratorEnumeration implements Enumeration {
        private Iterator iterator;
        
        public IteratorEnumeration(Iterator it) {
            super();
            iterator = it;
        }
        
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        public Object nextElement() {
            return iterator.next();
        }
    }

    public RootNode() {
        super();
        nodes.add(new AccountNode(this));
        nodes.add(new CustomerNode(this));
    }
    
    public Enumeration children() {
        return new IteratorEnumeration(nodes.iterator());
    }

    public boolean getAllowsChildren() {
        return true;
    }
    
    public TreeNode getChildAt(int ind) {
        return (TreeNode)nodes.get(ind);
    }

    public int getChildCount() {
        return nodes.size();
    }
    
    public int getIndex(TreeNode chld) {
        return nodes.indexOf(chld);
    }

    public TreeNode getParent() {
        return null;
    }

    public boolean isLeaf() {
        return false;
    }
    
    public String toString() {
        return "Root";
    }
}
