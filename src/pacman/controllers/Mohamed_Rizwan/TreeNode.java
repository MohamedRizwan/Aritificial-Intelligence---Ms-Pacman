package pacman.controllers.Mohamed_Rizwan;

import java.util.EnumMap;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

class TreeNode implements Comparable<TreeNode>
{
	public Game gameState;
	public int index;
	public EnumMap<MOVE, TreeNode> neighbors = new EnumMap<MOVE, TreeNode>(MOVE.class);
	public double g = 0;
	public double h = 0;
	public TreeNode parent = null;
	public MOVE reachedBy = null;

    public TreeNode(int index)
    {
        this.index=index;
    }
    
    public double f() {
    	return g + h;
    }
    
    public boolean isEqual(TreeNode other)
    {
        return index == other.index;
    }
    
    public int compareTo(TreeNode other)
    {
		if (f() < other.f()) {
			return -1;
		}
		else if (f() > other.f()) {
			return 1;
		}
		return 0;
    }
}
