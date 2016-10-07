package pacman.controllers.Mohamed_Rizwan;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Random;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class AlphaBetaController extends Controller<MOVE> {
	
	private Controller<EnumMap<Constants.GHOST, MOVE>> ghost;
	
	public AlphaBetaController( Controller<EnumMap<Constants.GHOST, MOVE>> ghost ){
        this.ghost = ghost;
    }

    public MOVE getMove(Game game, long timeDue) {
    	MOVE alphaBetaMove = MOVE.NEUTRAL;
    	double bestScore = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        
        int depth = 10;
        Game bestGame = game;
        double score = 0;
        
        ghost = Executor.ghostAI;
        
        HashMap<Game,MOVE> gameMove = new HashMap<>();

        while ( System.currentTimeMillis() < timeDue ) {
                MOVE moves[] = game.getPossibleMoves( game.getPacmanCurrentNodeIndex() );
                for (MOVE move: moves) {
                    Game myGame = game.copy();
                    myGame.advanceGame(move, ghost.getMove(game,-1));
                    if( !gameMove.containsKey(myGame) ){
                        gameMove.put(myGame, move);
                    }
                    score = min(game, depth-1, alpha, beta); 

                    if (score > bestScore) {
                        bestScore = score;
                        bestGame = myGame;
                    }

                    if (score < beta)
                        alpha = Math.max(alpha, score);
                    else
                        break;    
                }
        }
        alphaBetaMove = gameMove.get(bestGame);
        return alphaBetaMove;
    }

    public double min(Game game, int depth, double alpha, double beta) {
        if (depth < 1) return fitness(game);

        MOVE moves[] = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
        for (MOVE move: moves) {
            Game myGame = game.copy();
            myGame.advanceGame(move, ghost.getMove(game,-1));
            double value = max(myGame, depth-1, alpha, beta);
            if (value <= alpha) {
                break;
            } else {
                beta = value;
            }
        }
        return beta;
    }

    public double max(Game game, int depth, double alpha, double beta) {
        if (depth < 1) return fitness(game);

        MOVE moves[] = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
        for (MOVE move: moves) {
            Game myGame = game.copy();
            myGame.advanceGame(move, ghost.getMove(game,-1));
            double value = min(myGame, depth-1, alpha, beta);
            if (value >= beta) {
                break;
            } else {
                alpha = value;
            }
        }
        return alpha;
    }
    
    public double fitness ( Game myGame ){
        double score = myGame.getScore();
        int current = myGame.getPacmanCurrentNodeIndex();
        if ( myGame.wasPacManEaten() ) {
            score = 0;
        }
        for(Constants.GHOST aGhost : Constants.GHOST.values()) {
            double x = myGame.getDistance(current, myGame.getGhostCurrentNodeIndex(aGhost), Constants.DM.PATH);

            if (x < 25) {
                score -= 10;
            }
        }
        Random generator = new Random();
        double factor = generator.nextDouble();
        return score + factor;
    }
}
