package pacman.controllers.Mohamed_Rizwan;

import java.util.ArrayList;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class IterativeDeepeningController extends Controller<MOVE>
{
	final int MAX_DEPTH = 100;
	private MOVE iddfsMove=MOVE.NEUTRAL;
	ArrayList<Integer> path = new ArrayList<Integer>();
	Utilities util = new Utilities();
	private TreeNode[] graph;

	public MOVE getMove(Game game, long timeDue)
	{
		if(game.wasPacManEaten()){
			path = new ArrayList<Integer>();
		}
		if(path.isEmpty()){
			graph = util.createGraph(game.getCurrentMaze().graph);

			int[] bestPath = new int[0];
			int depth = 1;
			while(bestPath.length <= 1){
				bestPath = getPath(game.getPacmanCurrentNodeIndex(), game, depth++);
				if (depth >= MAX_DEPTH) {
					break;
				}
			}

			if (bestPath.length > 0) {
				for(int i = 1; i < bestPath.length; i++){
					path.add(bestPath[i]);
				}
			}
		}
		
		if (path.size() > 0) {
			iddfsMove = game.getMoveToMakeToReachDirectNeighbour(game.getPacmanCurrentNodeIndex(), path.remove(0));
		}
		else {
			iddfsMove = MOVE.NEUTRAL;
		}
		return iddfsMove;
	}
	
	public int[] getPath(int s, Game game, int depth)
    {
		ArrayList<TreeNode> open = new ArrayList<TreeNode>();
		ArrayList<TreeNode> closed = new ArrayList<TreeNode>();
		TreeNode start = graph[s];
		start.gameState = game.copy();
		start.reachedBy = MOVE.NEUTRAL;
		open.add(start);
		TreeNode current = null;
		Boolean foundSolution = false;
		while (!open.isEmpty()) {
			
			current = open.remove(open.size() - 1);
			closed.add(current);
			
			if (current.gameState.getScore() > game.getScore()) {
				foundSolution = true;
				break;
			}
			
			for (MOVE move : current.neighbors.keySet()) {
				if(move != current.reachedBy.opposite()) {
					TreeNode child = current.neighbors.get(move);
					if (closed.contains(child)) {
						continue;
					}
					child.g = current.g + 1;
					if(child.g < depth){
						child.parent = current;
						child.reachedBy = move;
						Game gameState = current.gameState.copy();
						gameState.advanceGame(move, Executor.ghostAI.getMove(gameState, -1));
						if (!gameState.wasPacManEaten()) {
							child.gameState = gameState;
							open.add(child);
						}
					}
				}
			}
		}

        if(foundSolution) {
        	return util.getPath(current);
        }
        else {
        	return new int[0];
        }
    }
}
