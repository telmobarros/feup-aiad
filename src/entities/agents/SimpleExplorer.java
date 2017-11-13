package entities.agents;

import repast.simphony.space.grid.Grid;

public class SimpleExplorer extends Explorer{

	private int communicationRange;

	public SimpleExplorer(Grid<Object> grid, int visionRadius, int mapDim, int communicationRange) {
		super(grid, visionRadius, mapDim);
		this.communicationRange = communicationRange;
	}

}
