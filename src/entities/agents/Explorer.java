package entities.agents;

import repast.simphony.space.grid.Grid;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public abstract class Explorer extends Agent{
	private char[][] knownSpace;
	private int visionRadius;

	private Grid <Object> grid ;

	public Explorer(Grid <Object> grid, int visionRadius, int mapDim) {
		/*this.space = space ;*/
		this.grid = grid ;
		this.knownSpace = new char[mapDim][mapDim];
		this.visionRadius = visionRadius;
		
		addBehaviour(new MyBeh());
	}
	
	class MyBeh extends CyclicBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}
		
	}
	

}
