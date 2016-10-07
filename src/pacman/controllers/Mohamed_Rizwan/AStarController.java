package pacman.controllers.Mohamed_Rizwan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class AStarController extends Controller<MOVE> {

	final int MAX_DEPTH = 100;
	private MOVE aStarMove = MOVE.NEUTRAL;
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

			int[] bestPath = getPath(game.getPacmanCurrentNodeIndex(), game, MAX_DEPTH);
			
			if (bestPath.length > 0) {
				for(int i = 1; i < bestPath.length; i++){
					path.add(bestPath[i]);
				}
			}
		}
		
		if (path.size() > 0) {
			aStarMove = game.getMoveToMakeToReachDirectNeighbour(game.getPacmanCurrentNodeIndex(), path.remove(0));
		}
		else {
			aStarMove = MOVE.NEUTRAL;
		}
		return aStarMove;
	}

	public synchronized int[] getPath(int s, Game game, int maxDepth) {
		TreeNode start = graph[s];
		start.gameState = game.copy();
		start.reachedBy = MOVE.NEUTRAL;
		
		PriorityQueue<TreeNode> open = new PriorityQueue<TreeNode>();
		HashMap<Integer, TreeNode> closed = new HashMap<Integer, TreeNode>();
		
		open.add(start);
		TreeNode current = null;
		int count = 0;
		
		while (!open.isEmpty()) {

			current = open.poll();
			closed.put(current.index, current);

			count += 1;
			if (count > maxDepth) {
				break;
			}

			if (current.gameState.getScore() > game.getScore()) {
				break;
			}

			for (MOVE move : current.neighbors.keySet()) {
				System.out.println("Closed: "+closed.size()+" Open: "+open.size());
				if (move != current.reachedBy.opposite()) {
					TreeNode child = current.neighbors.get(move);
					child.g = current.g + 1;
					child.parent = current;
					child.reachedBy = move;
					Game gameState = current.gameState.copy();
					gameState.advanceGame(move, Executor.ghostAI.getMove(gameState, -1));
					
					if (!gameState.wasPacManEaten()) {
						child.gameState = gameState;
						int numActivePills = gameState.getNumberOfActivePills();
						
						if (numActivePills > game.getNumberOfActivePills()) {
							numActivePills = 0;
						}
						
						child.h = numActivePills;
						
						if (closed.containsKey(child.index) && 
								closed.get(child.index).f() > child.f()) {
							closed.remove(child.index);
							open.add(child);
						} else if (!closed.containsKey(child.index) && !open.contains(child)) {
							open.add(child);
						}
					}
				}
			}
		}
		return util.getPath(current);
	}

}
