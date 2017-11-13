package entities;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;

public class Exit {

	/*private ContinuousSpace <Object> space ;*/
	private Grid <Object> grid ;

	public Exit(/*ContinuousSpace <Object> space ,*/ Grid <Object> grid ) {
		/*this.space = space ;*/
		this.grid = grid ;
	}
	

	
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		
	}

}
