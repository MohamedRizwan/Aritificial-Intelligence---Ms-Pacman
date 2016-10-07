package pacman.controllers.Mohamed_Rizwan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class PerceptronController extends Controller<MOVE>{
	private GHOST nearestGhost;
    private int nearestPill;
    private int nearestGhostDistance;
    private int nearestPillDistance;

    private static int training = 100;
    private double[] WEIGHTS = new double[2];

    /* Training data
     *		First index : distance to the nearest ghost
     *		Second index : distance to the nearest pill
     */
    double[][] instances = new double[24][2];
    int[] expected = new int[24];
    
    /*
	 * Training data 
	 * First index: distance of the nearest ghost 
	 * Second index: distance of the nearest pill (regular or power) 
	 * Third index: decision -1 means run from the nearest ghost, 1 means eat the nearest pill
	 */
	private void readLearningData(){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader("PerceptronData.txt"));
			String dataLine = "";
			int i = 0;
			while ((dataLine = reader.readLine()) != null) {
				String[] data = dataLine.split(",");
				if(data[0] == null){
					break;
				}
				instances[i][0]=Double.parseDouble(data[0]);
				instances[i][1]=Double.parseDouble(data[1]);
				expected[i]=Integer.parseInt(data[2]);
				i++;
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
	}

    public PerceptronController() {
    	readLearningData();
        int i, j;
        WEIGHTS[0] = Math.random();
        WEIGHTS[1] = Math.random();

        // Training
        for (i = 0; i < training; i++) {
            for (j = 0; j < instances.length; j++) {
                double dot = WEIGHTS[0] * instances[j][0] + WEIGHTS[1] * instances[j][1];
                if (expected[j] != Math.signum(dot)) {
                    WEIGHTS[0] += 1. / 5 * instances[j][0] * expected[j];
                    WEIGHTS[1] += 1. / 5 * instances[j][1] * expected[j];
                }
            }
        }
    }
    
    private void initializeData(Game game) {
        int current = game.getPacmanCurrentNodeIndex();
        nearestGhostDistance = Integer.MAX_VALUE;
        int tmp;
        
        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                tmp = game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost));
                if (tmp < nearestGhostDistance) {
                	nearestGhostDistance = tmp;
                    nearestGhost = ghost;
                }
            }
        }

        //Get distance of nearest pill (normal or power)
        int[] pills = game.getPillIndices();
        int[] powerPills = game.getPowerPillIndices();

        ArrayList<Integer> targets = new ArrayList<Integer>();

        for (int i = 0; i < pills.length; i++) 
        	//check which pills are available
            if (game.isPillStillAvailable(i))
                targets.add(pills[i]);

        for (int i = 0; i < powerPills.length; i++)            
        	//check with power pills are available
            if (game.isPowerPillStillAvailable(i))
                targets.add(powerPills[i]);

        int[] targetsArray = new int[targets.size()];
        for (int i = 0; i < targetsArray.length; i++)
            targetsArray[i] = targets.get(i);

        //return the next direction once the closest target has been identified
        nearestPill = game.getClosestNodeIndexFromNodeIndex(current, targetsArray, DM.PATH);
        nearestPillDistance = game.getShortestPathDistance(current, nearestPill);
    }

    public MOVE getMove(Game game, long timeDue) {

        int current = game.getPacmanCurrentNodeIndex();

        initializeData(game);
        int decision = (int) Math.signum(WEIGHTS[0] * nearestGhostDistance + WEIGHTS[1] * nearestPillDistance);
        
        if (nearestGhostDistance <= 5) {
            decision = -1;
        } else if (nearestGhostDistance > 30) {
            decision = 1;
        }

        if (decision == -1)
            //run from nearest ghost
            return game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(nearestGhost), DM.PATH);
        else
            //run to nearest pill
            return game.getNextMoveTowardsTarget(current, nearestPill, DM.PATH);

    }
}
