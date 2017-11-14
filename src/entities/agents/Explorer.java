package entities.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import entities.Entity;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public abstract class Explorer extends Agent{
	private char[][] knownSpace;
	private int visionRadius;

	private Grid <Object> grid ;
	public Grid<Object> getGrid() { return grid; }
	public void setGrid(Grid<Object> grid) { this.grid = grid; }

	public Explorer(Grid <Object> grid, int visionRadius, int mapDim) {
		/*this.space = space ;*/
		this.grid = grid ;
		this.knownSpace = new char[mapDim][mapDim];
		this.visionRadius = visionRadius;
		
		addBehaviour(new MyBeh());
		this.step();
		
	}
	
	class MyBeh extends CyclicBehaviour {

		@Override
		public void action() {
			Explorer explorer = ((Explorer)this.myAgent);
			Grid grid = explorer.grid;
			GridPoint pt = grid.getLocation(explorer);
			
			GridCellNgh<Entity> nghCreator = new GridCellNgh<Entity>(grid, pt, Entity.class, visionRadius, visionRadius);
			List<GridCell<Entity>> gridCells = nghCreator.getNeighborhood(true); // true or false to include the center
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			
			// Get empty cells arround
			ArrayList<GridPoint> possibleMoves = new ArrayList<GridPoint>();
			for (GridCell<Entity> cell : gridCells){
				if (cell.size() == 0){
					possibleMoves.add(cell.getPoint());
				}
			}
			//if (((Collection<Object>)grid.getObjectsAt(pt.getX()+1, pt.getY()+1)).size() == 0) System.out.println("esta vaxio");
			System.out.println(explorer);
			System.out.println(pt.getX() + ", " + pt.getY());
			System.out.println(possibleMoves);
			/*for(Object obj: objs){
				System.out.println(obj);
			}*/
			Random r = new Random();
			int i = r.nextInt(possibleMoves.size());
			GridPoint nextPos = possibleMoves.get(i);
			explorer.grid.moveTo(explorer,nextPos.getX(),nextPos.getY());
			
		}
		
	}
	

}
