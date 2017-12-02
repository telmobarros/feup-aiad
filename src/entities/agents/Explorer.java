package entities.agents;

import repast.simphony.space.grid.Grid;
import sajas.core.Agent;

public abstract class Explorer extends Agent{
	public enum ExplorerState {
		EXPLORING, ASKING_HELP, HELPING, EXITING, EXITED
	}

	public static double ASK_HELP_PROB = 0.4;

	private Grid<Object> grid;
	private int visionRadius;
	private char[][] knownSpace;
	private ExplorerState state;
	private boolean moved = true;

	public Explorer(Grid <Object> grid, int visionRadius, int mapDim) {
		/* this.space = space ;*/
		this.grid = grid;
		this.visionRadius = visionRadius;
		this.knownSpace = new char[mapDim][mapDim];
		// initialize known space
		for(int y = knownSpace.length-1; y >= 0; y--){
			for(int x = 0; x < knownSpace.length; x++){
				knownSpace[y][x] = 'O';
			}
		}
		this.state = ExplorerState.EXPLORING;
	}

	public String knownSpaceString(){
		String result = new String();
		for(int y=knownSpace.length-1; y >= 0; y--){
			for(int x=0; x < knownSpace.length; x++){
				result += knownSpace[y][x];
			}
			result += '\n';
		}
		return result;
	}

	public int getVisionRadius() {
		return visionRadius;
	}

	public ExplorerState getState() {
		return state;
	}

	public void setKnownSpace(int y, int x, char c) {
		knownSpace[y][x] = c;
	}

	public char getKnownSpace(int y, int x) {
		return knownSpace[y][x];
	}

	public char[][] getKnownSpace() {
		return knownSpace;
	}

	public Grid<Object> getGrid() { 
		return grid; 
	}

	public void setGrid(Grid<Object> grid) { 
		this.grid = grid; 
	}

	public abstract int getCommunicationRange();

	public void setState(ExplorerState state) {
		this.state = state;
	}
}
