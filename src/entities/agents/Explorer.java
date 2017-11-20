package entities.agents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import entities.Entity;
import entities.Exit;
import entities.Rock;
import entities.Wall;
import jade.lang.acl.ACLMessage;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public abstract class Explorer extends Agent{
	private enum ExplorerState {
		EXPLORING, ASKING_HELP, HELPING, EXITING
	}

	private char[][] knownSpace;
	private int visionRadius;
	private ExplorerState state;

	private Grid<Object> grid ;
	public Grid<Object> getGrid() { return grid; }
	public void setGrid(Grid<Object> grid) { this.grid = grid; }

	public Explorer(Grid <Object> grid, int visionRadius, int mapDim) {
		/*this.space = space ;*/
		this.grid = grid ;
		this.knownSpace = new char[mapDim][mapDim];
		for(int y=knownSpace.length-1; y >= 0; y--){
			for(int x=0; x < knownSpace.length; x++){
				knownSpace[y][x] = 'O';
			}
		}
		this.visionRadius = visionRadius;
		this.state = ExplorerState.EXPLORING;

		addBehaviour(new MyBeh());
		this.step();

	}

	class MyBeh extends CyclicBehaviour {

		@Override
		public void action() {
			Explorer explorer = ((Explorer)this.myAgent);
			Grid<Object> grid = explorer.grid;
			GridPoint pt = grid.getLocation(explorer);

			GridCellNgh<SimpleExplorer> nghCreatorTest = new GridCellNgh<SimpleExplorer>(grid, pt, SimpleExplorer.class, visionRadius, visionRadius);
			List<GridCell<SimpleExplorer>> gridCellsTest = nghCreatorTest.getNeighborhood(true); // true or false to include the center

			// TODO
			ACLMessage acl = new ACLMessage();
			acl.setContent(state + "\n" + knownSpaceString());

			for(GridCell<SimpleExplorer> cell : gridCellsTest) {
				for(SimpleExplorer se : cell.items()){
					acl.addReceiver(se.getAID());
				}
			}

			myAgent.send(acl);


			acl = myAgent.receive();
			System.out.println(acl);

			ArrayList<GridPoint> possibleMoves = new ArrayList<GridPoint>();
			ArrayList<GridPoint> visibleCells = new ArrayList<GridPoint>();

			GridCellNgh<Entity> nghCreator = new GridCellNgh<Entity>(grid, pt, Entity.class, visionRadius, visionRadius);
			List<GridCell<Entity>> gridCells = nghCreator.getNeighborhood(true); // true or false to include the center
			List<GridCell<Entity>> rangeCells = new ArrayList<GridCell<Entity>>(); // to hold cells between 1 and vision radius
			for (GridCell<Entity> cell : gridCells){
				if ((grid.getDistance(pt, cell.getPoint()) <= 1)){
					updateKnownSpace(cell);
					visibleCells.add(cell.getPoint());

					//if its an empty space near center or center add to possible moves
					if(knownSpace[cell.getPoint().getY()][cell.getPoint().getX()] == ' '){
						possibleMoves.add(cell.getPoint());
					}

				} else if (grid.getDistance(pt, cell.getPoint()) > 1 && grid.getDistance(pt, cell.getPoint()) <= visionRadius){
					rangeCells.add(cell);
				}
			}

			
			rangeCells.sort(createComparator(grid, pt));
			for (GridCell<Entity> cell : rangeCells){

				int x = cell.getPoint().getX();
				int y = cell.getPoint().getY();

				// it will give -1, +1 or 0
				int difX = 0;
				if (pt.getX() != x) difX = (pt.getX()-x)/(Math.abs(pt.getX()-x));
				int difY = 0;
				if (pt.getY() != y) difY = (pt.getY()-y)/(Math.abs(pt.getY()-y));
				boolean isVisible = false;

				if(visibleCells.contains(new GridPoint(x+difX,y+difY)) && knownSpace[y+difY][x+difX] == ' '){ // horizontal, vertical
					if(difX != 0 && difY != 0){		// all the other diagonals
						if((visibleCells.contains(new GridPoint(x+difX,y)) && knownSpace[y][x+difX] == ' ')
								||
								(visibleCells.contains(new GridPoint(x,y+difY)) && knownSpace[y+difY][x] == ' ')){
							isVisible = true;
						}
					} else {
						isVisible = true;
					}

					if(isVisible){
						updateKnownSpace(cell);
						visibleCells.add(cell.getPoint());
					}
				}

			}
			
			// TODO delete (debugging)
			System.out.print(pt.getX() + ", " + pt.getY() + " - ");
			System.out.println(explorer.getName());
			System.out.println(possibleMoves);
			//System.out.println(visibleCells);
			// print knownspace
			System.out.println(knownSpaceString());

			Random r = new Random();
			int i = r.nextInt(possibleMoves.size());
			GridPoint nextPos = possibleMoves.get(i);
			explorer.grid.moveTo(explorer,nextPos.getX(),nextPos.getY());

		}

		public void updateKnownSpace(GridCell<Entity> cell){
			int x = cell.getPoint().getX();
			int y = cell.getPoint().getY();

			if(cell.size() == 0){
				knownSpace[y][x] = ' ';
			} else if(cell.items().iterator().next().getClass() == Wall.class){
				knownSpace[y][x] = 'X';
			} else if(cell.items().iterator().next().getClass() == Rock.class){
				knownSpace[y][x] = 'R';
			} else if(cell.items().iterator().next().getClass() == Exit.class){
				knownSpace[y][x] = 'E';
			}
		}
	}

	private String knownSpaceString(){
		String result = new String();
		for(int y=knownSpace.length-1; y >= 0; y--){
			for(int x=0; x < knownSpace.length; x++){
				result += knownSpace[y][x];
			}
			result += '\n';
		}
		return result;
	}

	private static Comparator<GridCell<Entity>> createComparator(Grid<Object> grid,GridPoint pt)
	{
		return new Comparator<GridCell<Entity>>()
		{
			@Override
			public int compare(GridCell<Entity> cell1, GridCell<Entity> cell2)
			{
				double ds1 = grid.getDistance(pt,cell1.getPoint());
				double ds2 = grid.getDistance(pt,cell2.getPoint());
				return Double.compare(ds1, ds2);
			}

		};
	}


}
