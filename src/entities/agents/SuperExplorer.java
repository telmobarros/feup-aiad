package entities.agents;

import behaviours.SimpleBehaviour;
import repast.simphony.space.grid.Grid;

public class SuperExplorer extends Explorer{

	public SuperExplorer(Grid<Object> grid, int visionRadius, int mapDim) {
		super(grid, visionRadius, mapDim);
		// TODO make a constructor of simple behavior without communication range
		addBehaviour(new SimpleBehaviour<SuperExplorer>());
		this.step();	
	}

	@Override
	public int getCommunicationRange() {
		return this.getGrid().getDimensions().getWidth()/2;
	}

}
