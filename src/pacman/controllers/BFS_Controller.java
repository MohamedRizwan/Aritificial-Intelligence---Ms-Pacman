/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import pacman.controllers.Controller;
import pacman.controllers.examples.StarterGhosts;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Node;

/**
 *
 * @author amy
 */
public class BFS_Controller extends Controller<MOVE>{


        
        /*public enum MOVE 
	{
		UP 		{ public MOVE opposite(){return MOVE.DOWN;		};},	
		RIGHT 	{ public MOVE opposite(){return MOVE.LEFT;		};}, 	
		DOWN 	{ public MOVE opposite(){return MOVE.UP;		};},		
		LEFT 	{ public MOVE opposite(){return MOVE.RIGHT;		};}, 	
		NEUTRAL	{ public MOVE opposite(){return MOVE.NEUTRAL;	};};	
		
		public abstract MOVE opposite();
	};*/
	

    public static StarterGhosts ghosts = new StarterGhosts();
	public MOVE getMove(Game game,long timeDue)
	{
            Random rnd=new Random();
            MOVE[] allMoves=MOVE.values();
        
            int highScore = -1;
            MOVE highMove = null;
            
           
            for(MOVE m: allMoves)
            {
                //System.out.println("Trying Move: " + m);
                Game gameCopy = game.copy();
                Game gameAtM = gameCopy;
                gameAtM.advanceGame(m, ghosts.getMove(gameAtM, timeDue));
                int tempHighScore = this.bfs_amy(new PacManNode(gameAtM, 0), 7);
                
                if(highScore < tempHighScore)
                {
                    highScore = tempHighScore;
                    highMove = m;
                }
                
                System.out.println("Trying Move: " + m + ", Score: " + tempHighScore);
               
            }
            
            System.out.println("High Score: " + highScore + ", High Move:" + highMove);
              return highMove;
                
	}
        
        public int bfs_amy(PacManNode rootGameState, int maxdepth)
	{
            MOVE[] allMoves=Constants.MOVE.values();
            int depth = 0;
            int highScore = -1;
		
            Queue<PacManNode> queue = new LinkedList<PacManNode>();
            queue.add(rootGameState);

		//System.out.println("Adding Node at Depth: " + rootGameState.depth);
                
  
            while(!queue.isEmpty())
                {
                    PacManNode pmnode = queue.remove();
                    //System.out.println("Removing Node at Depth: " + pmnode.depth);
                    
                    if(pmnode.depth >= maxdepth)
                    {
                        int score = pmnode.gameState.getScore();
                         if (highScore < score)
                                  highScore = score;
                    }
                    else
                    {

                        //GET CHILDREN
                        for(MOVE m: allMoves)
                        {
                            Game gameCopy = pmnode.gameState.copy();
                            gameCopy.advanceGame(m, ghosts.getMove(gameCopy, 0));
                            PacManNode node = new PacManNode(gameCopy, pmnode.depth+1);
                            queue.add(node);
                        }
                    }

		}
                
                return highScore;
	}
        
    
}
