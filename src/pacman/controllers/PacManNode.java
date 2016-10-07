package pacman.controllers;

import pacman.game.Game;

public class PacManNode 
{
    Game gameState;
    int depth;
    
    public PacManNode(Game game, int depth)
    {
        this.gameState = game;
        this.depth = depth;
    }
}
