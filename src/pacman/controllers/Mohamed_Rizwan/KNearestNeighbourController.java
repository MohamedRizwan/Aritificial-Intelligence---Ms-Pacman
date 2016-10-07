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

public class KNearestNeighbourController extends Controller<MOVE> {
	private GHOST nearestGhost;
	private int nearestPill;
	private int nearestGhostDistance;
	private int nearestPillDistance;
	private static int K = 3;

	private static int[][] INSTANCES = new int[14][3];
	
	public KNearestNeighbourController(){
		readLearningData();
	}
	
	/*
	 * Training data 
	 * First index: distance of the nearest ghost 
	 * Second index: distance of the nearest pill (regular or power) 
	 * Third index: decision 0 means run from the nearest ghost, 1 means eat the nearest pill
	 */
	private void readLearningData(){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader("KNNData.txt"));
			String dataLine = "";
			int i = 0;
			while ((dataLine = reader.readLine()) != null) {
				String[] data = dataLine.split(",");
				if(data[0] == null){
					break;
				}
				INSTANCES[i][0]=Integer.parseInt(data[0]);
				INSTANCES[i][1]=Integer.parseInt(data[1]);
				INSTANCES[i][2]=Integer.parseInt(data[2]);
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

	public MOVE getMove(Game game, long timeDue) {

		int current = game.getPacmanCurrentNodeIndex();

		initialize(game);
		int decision = evaluateDecision();

		if (decision == 0)
			// run away from ghost
			return game.getNextMoveAwayFromTarget(current, game.getGhostCurrentNodeIndex(nearestGhost), DM.PATH);
		else
			// run towards pill
			return game.getNextMoveTowardsTarget(current, nearestPill, DM.PATH);

	}

	private void initialize(Game game) {
		int current = game.getPacmanCurrentNodeIndex();
		nearestGhostDistance = Integer.MAX_VALUE;
		int tmp;
		
		for (GHOST ghost : GHOST.values()) {
			if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
				tmp = game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost));
				if (tmp < nearestGhostDistance)
					nearestGhostDistance = tmp;
				nearestGhost = ghost;
			}
		}

		int[] pills = game.getPillIndices();
		int[] powerPills = game.getPowerPillIndices();

		ArrayList<Integer> targets = new ArrayList<Integer>();

		for (int i = 0; i < pills.length; i++) // check which pills are available
			if (game.isPillStillAvailable(i))
				targets.add(pills[i]);

		for (int i = 0; i < powerPills.length; i++) // check with power pills are available
			if (game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);

		int[] targetsArray = new int[targets.size()];

		for (int i = 0; i < targetsArray.length; i++)
			targetsArray[i] = targets.get(i);

		nearestPill = game.getClosestNodeIndexFromNodeIndex(current, targetsArray, DM.PATH);
		nearestPillDistance = game.getShortestPathDistance(current, nearestPill);
	}
	
	// Evaluate which K instances are nearest the current game state
	private int evaluateDecision() {
		// given closest ghost and pill evaluate which instances are closest K
		int[] distances = new int[K];
		int[] indexes = new int[K];
		int i;

		// initialize distances
		for (i = 0; i < K; i++)
			distances[i] = Integer.MAX_VALUE;

		int dist;
		for (i = 0; i < INSTANCES.length; i++) {
			// distance:sqrt( (difference of the ghost distance)^2 + (difference of pill distance)^2) )
			dist = (int) Math.sqrt(Math.pow(INSTANCES[i][0] - nearestGhostDistance, 2) + Math.pow(INSTANCES[i][1] - nearestPillDistance, 2));
			if (dist <= distances[0]) {
				/*for(int j=K-1;j>0;j--){
					distances[j] = distances[j-1];
					indexes[j] = indexes[j-1];
				}*/
				distances[2] = distances[1];
				indexes[2] = indexes[1];
				distances[1] = distances[0];
				indexes[1] = indexes[0];
				distances[0] = dist;	
				indexes[0] = i;
			} else if (dist <= distances[1]) {
				distances[2] = distances[1];
				indexes[2] = indexes[1];
				distances[1] = dist;				
				indexes[1] = i;
			} else if (dist <= distances[2]) {
				distances[2] = dist;
				indexes[2] = i;
			}
		}

		// add decisions of each of the K nearest instances
		int sum = 0;
		for (i = 0; i < K; i++) {
			sum += INSTANCES[indexes[i]][2];
		}

		int decision = sum / K;

		return decision;
	}
}
