package entities;

import repast.simphony.space.grid.Grid;

public abstract class Entity {
	private Grid <Object> grid ;

	public Entity( Grid <Object> grid ) {
		this.grid = grid ;
	}

}
