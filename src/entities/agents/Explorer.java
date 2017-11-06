package entities.agents;

import repast.simphony.space.grid.Grid;
import sajas.core.Agent;

public abstract class Explorer extends Agent{
	private char[][] knownSpace;
	private int visionRadius;

	/*private ContinuousSpace <Object> space ;*/
	private Grid <Object> grid ;

	public Explorer(/*ContinuousSpace <Object> space ,*/ Grid <Object> grid ) {
		/*this.space = space ;*/
		this.grid = grid ;
	}
	

}
