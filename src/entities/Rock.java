package entities;

import java.util.List;

import entities.agents.Explorer;
import entities.agents.Explorer.ExplorerState;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Rock extends Entity{
	public Rock(Grid<Object> grid) {
		super(grid);
	}
	
	/*
	 * Method to self destroy rock when 2 or more explorers are around it
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		// get the grid location of this Rock
		GridPoint pt = grid.getLocation(this);
		
		GridCellNgh<Explorer> nghCreator = new GridCellNgh<Explorer>(grid, pt, Explorer.class, 1 , 1);
		List<GridCell<Explorer>> gridCells = nghCreator.getNeighborhood(false);
		
		
		int explorersCount = 0;
		for (GridCell<Explorer> cell : gridCells){
			if (grid.getDistance(pt, cell.getPoint()) <= 1){
				explorersCount += cell.size();
			}
		}
		
		if(explorersCount >= 2){
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
		}
	}
}
