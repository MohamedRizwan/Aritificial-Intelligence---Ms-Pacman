package pacman.controllers.Mohamed_Rizwan;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class HillClimberController extends Controller<MOVE> {

	
	private MOVE hillClimberMove = MOVE.NEUTRAL;
	
	@Override
	public MOVE getMove(Game game, long timeDue)
	{
		int currValue = Integer.MIN_VALUE;

		MOVE[] moves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
		for(MOVE move : moves){

			Game gameCopy = game.copy();
			gameCopy.advanceGame(move, Executor.ghostAI.getMove());

			if(getNextState(gameCopy) > currValue){
				hillClimberMove = move;
				currValue = getNextState(gameCopy);
			}
		}
		return hillClimberMove;
	}
	
	private int getNextState(Game game){
		
		if(game.wasPacManEaten()) 
			return Integer.MIN_VALUE;

		int score = game.getScore();

		for(GHOST ghost: GHOST.values()){
			if(game.isGhostEdible(ghost)){
				score += 100;
				score += game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost), DM.PATH);
			} else {
				score -= game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost), DM.PATH);
			}
		}
		return score;
	}
}
