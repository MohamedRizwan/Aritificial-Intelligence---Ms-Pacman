package pacman.controllers.Mohamed_Rizwan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Game;

public class GeneticController extends Controller<MOVE> {
	private MOVE geneticMove = MOVE.NEUTRAL;

	public MOVE getMove(Game game, long timeDue) {
		ArrayList<TreeNode> list = new ArrayList<TreeNode>();
		ArrayList<TreeNode> tempList = new ArrayList<TreeNode>();
		ArrayList<TreeNode> offsprings = new ArrayList<TreeNode>();

		list.add(initializeTreeNode(game));

		for (int l = 0; l < 4; l++) {
			// Iterate over children till depth equal to 2
			for (int j = 0; j < 2; j++) {
				for (int i = 0; i < list.size(); i++) {
					TreeNode t = list.get(i);
					MOVE[] possibleMoves = t.gameState.getPossibleMoves(t.gameState.getPacmanCurrentNodeIndex());
					for (MOVE move : possibleMoves) {
						TreeNode next = evolve(t,move);
						tempList.add(next);
						offsprings.add(next);
					}
				}
				list = tempList;
				tempList = new ArrayList<TreeNode>();
			}

			for (int m = 0; m < offsprings.size(); m++) {
				TreeNode x = offsprings.get((int) (Math.random()*offsprings.size()));
				TreeNode y = offsprings.get((int)Math.random()*offsprings.size());

				TreeNode child = crossover(x,y);
				if (Math.random() <= 0.05) {
					MOVE[] moves = child.gameState
							.getPossibleMoves(child.gameState.getPacmanCurrentNodeIndex());
					MOVE move = moves[(int) (Math.random() * moves.length)];
					child = evolve(child,move);
				}
				tempList.add(child);

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
			geneticMove = game.getMoveToMakeToReachDirectNeighbour(game.getPacmanCurrentNodeIndex(), computedPath[1]);
		}
		return geneticMove;
	}

	public TreeNode crossover(TreeNode node1, TreeNode node2) {
		ArrayList<MOVE> moves = new ArrayList<MOVE>();
		int index = 0;
		while(node1.parent != null && node2.parent != null && index < 50){
			if (index % 2 == 0) {
				moves.add(0, node2.reachedBy);
			} else {
				moves.add(0, node1.reachedBy);
			}
			node1 = node1.parent;
			node2 = node2.parent;
			index++;
		}
		while (!moves.isEmpty()) {
			node2 = evolve(node2,moves.remove(0));
		}
		return node2;
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
}
