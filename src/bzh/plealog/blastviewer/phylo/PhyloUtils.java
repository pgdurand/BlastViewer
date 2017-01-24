/* Copyright (C) 2008-2017 Patrick G. Durand
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/agpl-3.0.txt
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 */
package bzh.plealog.blastviewer.phylo;

import epos.model.tree.Tree;
import epos.model.tree.TreeNode;

/**
 * This class contains utility methods of this package.
 * 
 * @author Patrick Durand, Korilog SARL
 */
public class PhyloUtils {
	private static final String ID_KEY = "uniqueID";
	
	private static int   _counter = 1;
	
	/**
	 * Given a tree, this method sets a unique ID as the node label. This method do
	 * this only for leaf nodes.
	 */
	public static void setLeafNodeId(Tree t){
		String   id;
		TreeNode tn = t.getRoot();
		
		for (TreeNode tn2 : tn.depthFirstIterator()){
			if (tn2.isLeaf()){
				id = tn2.getLabel();
				if (id==null)
					id = PhyloUtils.getUniqueId();
				PhyloUtils.setNodeId(tn2, id);
			}
		}
	}
	private static synchronized String getUniqueId(){
		String id = "tn"+_counter;
		_counter++;
		return id;
	}
	public static void setNodeId(TreeNode tn, String id){
		tn.setProperty(ID_KEY, id);
	}
	public static String getNodeId(TreeNode tn){
		String id;
		id = (String)tn.getProperty(ID_KEY);
		return (id==null ? tn.getLabel() : id);
	}
}
