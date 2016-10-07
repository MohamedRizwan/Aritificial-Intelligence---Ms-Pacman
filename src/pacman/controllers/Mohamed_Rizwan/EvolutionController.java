package pacman.controllers.Mohamed_Rizwan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class EvolutionController extends Controller<MOVE> {

	private MOVE evolutionMove = MOVE.NEUTRAL;
	private int noOfOffspring = 10; 

	public MOVE getMove(Game game, long timeDue) {

		ArrayList<TreeNode> list = new ArrayList<TreeNode>();
		ArrayList<TreeNode> tempList = new ArrayList<TreeNode>();
		ArrayList<TreeNode> offsprings = new ArrayList<TreeNode>();

		list.add(initializeTreeNode(game));

		for (int l = 0; l < 32; l++) {
			// Iterate over children till depth equal to 4
			for (int j = 0; j < 4; j++) {
				for (int i = 0; i < list.size(); i++) {
					TreeNode t = list.get(i);
					MOVE[] possibleMoves = t.gameState.getPossibleMoves(t.gameState.getPacmanCurrentNodeIndex());
					for (MOVE move : possibleMoves) {
						TreeNode next = evolve(t, move);
						tempList.add(next);
						offsprings.add(next);
					}
				}
				list = tempList;
				tempList = new ArrayList<TreeNode>();
			}

			// Collections.sort(offsprings, new );
			Collections.sort(offsprings, new Comparator<TreeNode>() {
				@Override
				public int compare(TreeNode o1, TreeNode o2) {
					if (evaluate(o1.gameState) > evaluate(o2.gameState)) {
						return -1;
					} else if (evaluate(o1.gameState) < evaluate(o2.gameState)) {
						return 1;
					}
					return 0;
				}
			});

			// retain only k offsprings
			for (int i = offsprings.size() - 1; i >= noOfOffspring; i--) {
				offsprings.remove(i);
			}

			// mutate by moving to a random position near them
			for (int m = 0; m < offsprings.size(); m++) {
				MOVE[] moves = offsprings.get(m).gameState
						.getPossibleMoves(offsprings.get(m).gameState.getPacmanCurrentNodeIndex());
				MOVE randomMOVE = moves[(int) (Math.random() * moves.length)];
				TreeNode mutatedNode = evolve(offsprings.get(m), randomMOVE);
				tempList.add(mutatedNode);
				tempList.add(offsprings.get(m));
			}
			list = tempList;
			tempList = new ArrayList<TreeNode>();
			offsprings = new ArrayList<TreeNode>();

		}
		
		Collections.sort(list, new Comparator<TreeNode>() {
			@Override
			public int compare(TreeNode o1, TreeNode o2) {
				if (evaluate(o1.gameState) > evaluate(o2.gameState)) {
					return -1;
				} else if (evaluate(o1.gameState) < evaluate(o2.gameState)) {
					return 1;
				}
				return 0;
			}
		});

		int[] computedPath = extractPath(list.get(0));

		if (computedPath.length > 1) {
			evolutionMove = game.getMoveToMakeToReachDirectNeighbour(game.getPacmanCurrentNodeIndex(), computedPath[1]);
		}
		return evolutionMove;
	}

	public TreeNode initializeTreeNode(Game game) {
		TreeNode t = new TreeNode(game.getPacmanCurrentNodeIndex());
		t.gameState = game;
		t.reachedBy = game.getPacmanLastMoveMade();

		return t;
	}

	public TreeNode evolve(TreeNode t, MOVE move) {
		Game game = t.gameState.copy();
		game.advanceGame(move, Executor.ghostAI.getMove());
		TreeNode next = initializeTreeNode(game);
		next.parent = t;
		next.reachedBy = move;
		return next;
	}

	public int evaluate(Game game) {

		if (game.wasPacManEaten())
			return Integer.MIN_VALUE;

		int score = 100 * game.getScore();

		for (GHOST ghost : GHOST.values()) {
			if (game.isGhostEdible(ghost)) {
				score += 50;
				score -= game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost),
						DM.PATH);
			} else {
				score += game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost),
						DM.PATH);
			}
		}
		return score;
	}

	public int[] extractPath(TreeNode target) {
		ArrayList<Integer> route = new ArrayList<Integer>();
		TreeNode current = target;
		route.add(current.index);
		while (current.parent != null) {
			current = current.parent;
			route.add(current.index);
		}
		Collections.reverse(route);
		int[] routeArray = new int[route.size()];
		for (int i = 0; i < routeArray.length; i++) {
			routeArray[i] = route.get(i);
		}
		return routeArray;
	}

}
