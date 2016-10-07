package pacman.controllers.Mohamed_Rizwan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;

import pacman.game.Constants.MOVE;
import pacman.game.internal.Node;

public class Utilities {
	
	public TreeNode[] createGraph(Node[] nodes)
	{
		TreeNode[] graph = new TreeNode[nodes.length];
	
		for(int i=0;i<nodes.length;i++) {
			graph[i]=new TreeNode(nodes[i].nodeIndex);
		}
		
		for(int i=0;i<nodes.length;i++) {
			EnumMap<MOVE,Integer> edges=nodes[i].neighbourhood;
			MOVE[] moves=MOVE.values();
			for(int j=0;j<moves.length;j++) {
				if(edges.containsKey(moves[j])) {
					TreeNode x = graph[edges.get(moves[j])];
					graph[i].neighbors.put(moves[j], x);
				}
			}
		}
		
		return graph;
	}
	
	public int[] getPath(TreeNode target)
    {
		ArrayList<Integer> route = new ArrayList<Integer>();
		TreeNode current = target;
		route.add(current.index);
		while (current.parent != null) {
			current = current.parent;
			if(route.contains(current.index))
				break;
			else
				route.add(current.index);
			//TODO: Check loop detection
			/*if(current == target)
				break;*/
		}
		Collections.reverse(route);
		int[] routeArray=new int[route.size()];
        for(int i=0;i<routeArray.length;i++) {
        	routeArray[i]=route.get(i);
    	}
        return routeArray;
    }

}
