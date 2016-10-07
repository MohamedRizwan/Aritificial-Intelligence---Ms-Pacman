package pacman.controllers.Mohamed_Rizwan;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class SimulatedAnnealingController extends Controller<MOVE> {

	private MOVE simulatedMove = MOVE.NEUTRAL;

	public MOVE getMove(Game game, long timeDue)
	{
		int currValue = Integer.MIN_VALUE;

		MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());

		for(MOVE move : possibleMoves){

			Game gameCopy = game.copy();
			gameCopy.advanceGame(move, Executor.ghostAI.getMove());

			int score = getNextState(gameCopy);

			if(score>currValue){
				simulatedMove = move;
				currValue = score;
			} else if(score != Integer.MIN_VALUE && score != currValue 
					&& Math.random() <= Math.exp(((score-currValue)/10*(game.getScore())))){
				simulatedMove = move;
				currValue = score;
			}
		}
		return simulatedMove;
	}
	
	private int getNextState(Game game){		

		if(game.wasPacManEaten()) return Integer.MIN_VALUE;

		int score = game.getScore();

		for(GHOST ghost: GHOST.values()){
			if(game.isGhostEdible(ghost)){
				score += 50;
				score += game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost), DM.PATH);
			} else {
				score -= game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost), DM.PATH);
			}
		}
		return score;
	}

}
