package pacman.controllers.Mohamed_Rizwan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Constants;
import pacman.game.Game;

public class MyPacManController extends Controller<MOVE> {
	public final static int MIN_GHOST_DISTANCE = 15; // Minimum distance from ghost to pacman that pacman will evade
	public final static int MIN_HUNT_DISTANCE = 70; // Minimum distance from vulnerable ghost to pacman that pacman will hunt
	public final static float MIN_GHOST_EDIBLE_TIME = 0.5f; // Minimum remaining time that ghost will remain vulnerable that pacman will hunt
	public final static int MIN_POWER_PILL_DISTANCE = 10;

	final int MAX_DEPTH = 20;
	private MOVE aStarMove = MOVE.NEUTRAL;
	ArrayList<Integer> path = new ArrayList<Integer>();
	Utilities util = new Utilities();
	private TreeNode[] graph;
	
	private enum States{
		EAT,
		CHASE,
		EVADE
	}
	
	PriorityQueue<TreeNode> open = new PriorityQueue<TreeNode>();
	HashMap<Integer, TreeNode> closed = new HashMap<Integer, TreeNode>();

	public MOVE getMove(Game game, long timeDue) {
		if (game.wasPacManEaten()) {
			path = new ArrayList<Integer>();
		}
		if (path.isEmpty()) {
			graph = util.createGraph(game.getCurrentMaze().graph);

			int[] bestPath = getPath(game.getPacmanCurrentNodeIndex(), game, MAX_DEPTH);

			if (bestPath.length > 0) {
				for (int i = 1; i < bestPath.length; i++) {
					path.add(bestPath[i]);
				}
			}
		}

		if (path.size() > 0) {
			aStarMove = game.getMoveToMakeToReachDirectNeighbour(game.getPacmanCurrentNodeIndex(), path.remove(0));
		} else {
			aStarMove = MOVE.NEUTRAL;
		}
		return aStarMove;
	}

	public synchronized int[] getPath(int s, Game game, int maxDepth) {
		TreeNode start = graph[s];
		start.gameState = game.copy();
		start.reachedBy = MOVE.NEUTRAL;

		/*PriorityQueue<TreeNode> open = new PriorityQueue<TreeNode>();
		HashMap<Integer, TreeNode> closed = new HashMap<Integer, TreeNode>();*/

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
				if (move != current.reachedBy.opposite()) {
					/*TreeNode child = current.neighbors.get(move);
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

						if (closed.containsKey(child.index) && closed.get(child.index).f() > child.f()) {
							closed.remove(child.index);
							open.add(child);
						} else if (!closed.containsKey(child.index) && !open.contains(child)) {
							open.add(child);
						}
					}*/
					getPathStateMachine(game,move,current);
				}
			}
		}
		return util.getPath(current);
	}
	
	public void getPathStateMachine(Game game, 
			MOVE move,TreeNode current) {
		
		String s = StateMachine(game, move,current);
		switch(s){
				case "EAT":
					System.out.println("EAT");
					huntFood(game,current);
					break;
				case "EVADE" :
					System.out.println("EVADE");
					evadeGhosts(game,current);
					break;
				case "CHASE" :
					System.out.println("CHASE");
					huntVunerableGhosts(game, current);
					break;
		}
	}

	private String StateMachine(Game game, MOVE move,TreeNode current){
			if(!NearGhost(game, move,current) && !PowerPill(game,move, current)){
				//If NO ghost near and NO Power Pill Evade
				return States.EAT.toString();
				}
			if(NearGhost(game, move, current) && PowerPill(game, move,current)){
				//If ghost near and Power Pill ON Chase
			return States.CHASE.toString();
			}
			//By default return Evade
			return States.EVADE.toString();//If NO ghost near EAT
	}
	
	private Boolean NearGhost(Game game,MOVE move,TreeNode current){
		Boolean nearGhost = false;
		for (Constants.GHOST ghost : Constants.GHOST.values()) {
			//int distance = game.getShortestPathDistance(current.index, game.getGhostCurrentNodeIndex(ghost));
			int distance = game.getManhattanDistance(current.index, game.getGhostCurrentNodeIndex(ghost));
			
			if (distance < MIN_GHOST_DISTANCE)
				nearGhost |= true;
		}
		
		return nearGhost;
		
	}
	
	private Boolean PowerPill(Game game,MOVE move,TreeNode current){
		/*int activePowerPillsBefore=game.getActivePowerPillsIndices().length;
		game.advanceGame(move, Executor.ghostAI.getMove(game, -1));
		int activePowerPillsAfter=game.getActivePowerPillsIndices().length;

		System.out.println("Power Pill: "+activePowerPillsBefore +" "+ activePowerPillsAfter);
		
		if(activePowerPillsAfter < activePowerPillsBefore)
			return true;*/
		Boolean powerPillActive = false;
		for (Constants.GHOST ghost : Constants.GHOST.values()) {
			int distance = game.getShortestPathDistance(current.index, game.getGhostCurrentNodeIndex(ghost));
			if (game.getGhostEdibleTime(ghost) > MIN_GHOST_EDIBLE_TIME && distance < MIN_HUNT_DISTANCE)
				powerPillActive |= true;
		}
		
		return powerPillActive;
	}

	public void evadeGhosts(Game game,TreeNode pacManCurrentNode) {
				
		for (Constants.GHOST ghost : Constants.GHOST.values()) {
			if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
				if (game.getShortestPathDistance(pacManCurrentNode.index,
						game.getGhostCurrentNodeIndex(ghost)) < MIN_GHOST_DISTANCE) {
					MOVE move = game.getNextMoveAwayFromTarget(pacManCurrentNode.index, game.getGhostCurrentNodeIndex(ghost),
							Constants.DM.PATH);
					if (!game.wasPacManEaten()) {
					TreeNode child = pacManCurrentNode.neighbors.get(move);
					child.g = pacManCurrentNode.g + 1;
					child.parent = pacManCurrentNode;
					child.reachedBy = move;
					child.gameState = game;
					int numActivePills = game.getNumberOfActivePills();

					if (numActivePills > game.getNumberOfActivePills()) {
						numActivePills = 0;
					}

					child.h = numActivePills;								
					
					if (closed.containsKey(child.index) && closed.get(child.index).f() > child.f()) {
						closed.remove(child.index);
						open.add(child);
					} else if (!closed.containsKey(child.index) && !open.contains(child)) {
						open.add(child);
					}
					/*return game.getNextMoveAwayFromTarget(pacManCurrentNode, game.getGhostCurrentNodeIndex(ghost),
							Constants.DM.PATH);*/
					}
				}
			}
		}
		//return null;
	}

	public void huntVunerableGhosts(Game game, TreeNode pacManCurrentNode) {
		for (Constants.GHOST ghost : Constants.GHOST.values()) {
			int distance = game.getShortestPathDistance(pacManCurrentNode.index, game.getGhostCurrentNodeIndex(ghost));
			if (game.getGhostEdibleTime(ghost) > MIN_GHOST_EDIBLE_TIME && distance < MIN_HUNT_DISTANCE) {
				MOVE move =  game.getNextMoveTowardsTarget(pacManCurrentNode.index, game.getGhostCurrentNodeIndex(ghost),
						Constants.DM.PATH);
				
				if (!game.wasPacManEaten()) {
				TreeNode child = pacManCurrentNode.neighbors.get(move);
				child.g = pacManCurrentNode.g + 1;
				child.parent = pacManCurrentNode;
				child.reachedBy = move;
				child.gameState = game;
				int numActivePills = game.getNumberOfActivePills();

				if (numActivePills > game.getNumberOfActivePills()) {
					numActivePills = 0;
				}

				child.h = numActivePills;
				
				if (closed.containsKey(child.index) && closed.get(child.index).f() > child.f()) {
					closed.remove(child.index);
					open.add(child);
				} else if (!closed.containsKey(child.index) && !open.contains(child)) {
					open.add(child);
				}
				}
			}
		}
		//return null;
	}

	public void huntFood(Game game, TreeNode pacManCurrentNode) {
		System.out.println("Closed: "+closed.size()+" Open: "+open.size());
		// get all active pills + power pills
		int[] activePills = game.getActivePillsIndices();
		int[] activePowerPills = game.getActivePowerPillsIndices();

		// create a target array that includes all ACTIVE pills and power pills
		int[] targetNodeIndices = new int[activePills.length + activePowerPills.length];

		for (int i = 0; i < activePills.length; i++)
			targetNodeIndices[i] = activePills[i];

		for (int i = 0; i < activePowerPills.length; i++)
			targetNodeIndices[activePills.length + i] = activePowerPills[i];

		MOVE move =  game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),
				game.getClosestNodeIndexFromNodeIndex(pacManCurrentNode.index, targetNodeIndices, Constants.DM.PATH),
				Constants.DM.PATH);
		if (!game.wasPacManEaten()) {
		TreeNode child = pacManCurrentNode.neighbors.get(move);
		if(child != null){
			child.g = pacManCurrentNode.g + 1;
			child.parent = pacManCurrentNode;
			child.reachedBy = move;
			child.gameState = game;
			int numActivePills = game.getNumberOfActivePills();

			if (numActivePills > game.getNumberOfActivePills()) {
				numActivePills = 0;
			}

			child.h = numActivePills;
			
			if (closed.containsKey(child.index) && closed.get(child.index).f() > child.f()) {
				closed.remove(child.index);
				open.add(child);
			} else if (!closed.containsKey(child.index) && !open.contains(child)) {
				open.add(child);
			}
		}
		}
		
	}

}
