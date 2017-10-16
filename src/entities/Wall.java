package entities;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Wall {

	/*private ContinuousSpace <Object> space ;*/
	private Grid <Object> grid ;

	public Wall(/*ContinuousSpace <Object> space ,*/ Grid <Object> grid ) {
		/*this.space = space ;*/
		this.grid = grid ;
	}
	

	
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		
	}
}
