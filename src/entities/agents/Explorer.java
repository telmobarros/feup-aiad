package entities.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import entities.Entity;
import entities.Exit;
import entities.Rock;
import entities.Wall;
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
	private int[][] knownSpace;
	private int visionRadius;

	private Grid<Object> grid ;
	public Grid<Object> getGrid() { return grid; }
	public void setGrid(Grid<Object> grid) { this.grid = grid; }

	public Explorer(Grid <Object> grid, int visionRadius, int mapDim) {
		/*this.space = space ;*/
		this.grid = grid ;
		this.knownSpace = new int[mapDim][mapDim];
		this.visionRadius = visionRadius;

		addBehaviour(new MyBeh());
		this.step();

	}

	class MyBeh extends CyclicBehaviour {

		@Override
		public void action() {
			Explorer explorer = ((Explorer)this.myAgent);
			Grid<Object> grid = explorer.grid;
			GridPoint pt = grid.getLocation(explorer);

			//GridCellNgh<Entity> nghCreator = new GridCellNgh<Entity>(grid, pt, Entity.class, visionRadius, visionRadius);
			//List<GridCell<Entity>> gridCells = nghCreator.getNeighborhood(true); // true or false to include the center
			//SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

			ArrayList<GridPoint> possibleMoves = new ArrayList<GridPoint>();
			ArrayList<GridPoint> visibleCells = new ArrayList<GridPoint>();
			//ArrayList<GridCell<Entity>> visibleCellsContent = new ArrayList<GridCell<Entity>>();
			//possibleMoves.add(pt);
			/*GridCellNgh<Entity> curNghCreator = new GridCellNgh<Entity>(grid, pt, Entity.class, 1, 1);
			List<GridCell<Entity>> curGridCells = curNghCreator.getNeighborhood(true); // true or false to include the center
			for (GridCell<Entity> cell : curGridCells){
				if ((grid.getDistance(pt, cell.getPoint()) <= 1)){

					possibleMoves.add(cell.getPoint());
					visibleCells.add(cell.getPoint());
					visibleCellsContent.add(cell);

					updateKnownSpace(cell);
				}
			}*/

			GridCellNgh<Entity> nghCreator = new GridCellNgh<Entity>(grid, pt, Entity.class, visionRadius, visionRadius);
			List<GridCell<Entity>> gridCells = nghCreator.getNeighborhood(true); // true or false to include the center
			List<GridCell<Entity>> rangeCells = new ArrayList<GridCell<Entity>>(); // to hold cells between 1 and vision radius
			for (GridCell<Entity> cell : gridCells){
				if ((grid.getDistance(pt, cell.getPoint()) <= 1)){
					updateKnownSpace(cell);
					visibleCells.add(cell.getPoint());
					
					//if its an empty space near center or center add to possible moves
					if(knownSpace[cell.getPoint().getY()][cell.getPoint().getX()] == -1){
						possibleMoves.add(cell.getPoint());
					}
					
				} else if (grid.getDistance(pt, cell.getPoint()) > 1 && grid.getDistance(pt, cell.getPoint()) <= visionRadius){
					rangeCells.add(cell);
				}
			}
			
			/*for (GridCell<Entity> cell : gridCells){
				if (grid.getDistance(pt, cell.getPoint()) >= 1 && grid.getDistance(pt, cell.getPoint()) < visionRadius){
					rangeCells.add(cell);
				}
			}*/
			rangeCells.sort(createComparator(grid, pt));
			for (GridCell<Entity> cell : rangeCells){
				//System.out.println(cell.getPoint());

				int x = cell.getPoint().getX();
				int y = cell.getPoint().getY();

				// it will give -1, +1 or 0
				int difX = 0;
				if (pt.getX() != x) difX = (pt.getX()-x)/(Math.abs(pt.getX()-x));
				int difY = 0;
				if (pt.getY() != y) difY = (pt.getY()-y)/(Math.abs(pt.getY()-y));
				boolean isVisible = false;

				if(visibleCells.contains(new GridPoint(x+difX,y+difY)) && knownSpace[y+difY][x+difX] == -1){ // horizontal, vertical
					if(difX != 0 && difY != 0){		// all the other diagonals
						if((visibleCells.contains(new GridPoint(x+difX,y)) && knownSpace[y][x+difX] == -1)
								||
								(visibleCells.contains(new GridPoint(x,y+difY)) && knownSpace[y+difY][x] == -1)){
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

//				WORKING FINE BUT TOO BIG (DELETE AFTER 100% SURE SOLUTION ABOVE WORKS) 
//				if(x == pt.getX()){ // horizontal
//					if(y < pt.getY()){
//						if(visibleCells.contains(new GridPoint(x,y+1)) && knownSpace[y+1][x] == -1){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					}else if(y > pt.getY()){
//						if(visibleCells.contains(new GridPoint(x,y-1)) && knownSpace[y-1][x] == -1){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					}
//				} else if(y == pt.getY()){ // vertical
//					if(x < pt.getX()){
//						if(visibleCells.contains(new GridPoint(x+1,y)) && knownSpace[y][x+1] == -1){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					}else if(x > pt.getX()){
//						if(visibleCells.contains(new GridPoint(x-1,y)) && knownSpace[y][x-1] == -1){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					}
//				} else if(x-pt.getX() == y-pt.getY()){ // diagonal
//					if(y < pt.getY()){
//						if((visibleCells.contains(new GridPoint(x+1,y+1)) && knownSpace[y+1][x+1] == -1)
//								&&
//								(
//										(visibleCells.contains(new GridPoint(x+1,y)) && knownSpace[y][x+1] == -1)
//										||
//										(visibleCells.contains(new GridPoint(x,y+1)) && knownSpace[y+1][x] == -1))){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					} else if(y > pt.getY()){
//						if((visibleCells.contains(new GridPoint(x-1,y-1)) && knownSpace[y-1][x-1] == -1)
//								&&
//								(
//										(visibleCells.contains(new GridPoint(x-1,y)) && knownSpace[y][x-1] == -1)
//										||
//										(visibleCells.contains(new GridPoint(x,y-1)) && knownSpace[y-1][x] == -1))){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					}
//				} else if(x-pt.getX() == -(y-pt.getY())){ // diagonal
//					if(y < pt.getY()){
//						if((visibleCells.contains(new GridPoint(x-1,y+1)) && knownSpace[y+1][x-1] == -1)
//								&&
//								(
//										(visibleCells.contains(new GridPoint(x-1,y)) && knownSpace[y][x-1] == -1)
//										||
//										(visibleCells.contains(new GridPoint(x,y+1)) && knownSpace[y+1][x] == -1))){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					} else if(y > pt.getY()){
//						if((visibleCells.contains(new GridPoint(x+1,y-1)) && knownSpace[y-1][x+1] == -1)
//								&&
//								(
//										(visibleCells.contains(new GridPoint(x+1,y)) && knownSpace[y][x+1] == -1)
//										||
//										(visibleCells.contains(new GridPoint(x,y-1)) && knownSpace[y-1][x] == -1))){
//							updateKnownSpace(cell);
//							visibleCells.add(cell.getPoint());
//						}
//					}
//				} else{
//					// it will give or -1 or +1
//					int difX = (pt.getX()-x)/(Math.abs(pt.getX()-x));
//					int difY = (pt.getY()-y)/(Math.abs(pt.getY()-y));
//					if((visibleCells.contains(new GridPoint(x+difX,y+difY)) && knownSpace[y+difY][x+difX] == -1)
//							&&
//							(
//									(visibleCells.contains(new GridPoint(x+difX,y)) && knownSpace[y][x+difX] == -1)
//									||
//									(visibleCells.contains(new GridPoint(x,y+difY)) && knownSpace[y+difY][x] == -1))){
//						updateKnownSpace(cell);
//						visibleCells.add(cell.getPoint());
//					}
//				}

			}

			/*GridCellNgh<Entity> lastNghCreator = curNghCreator;
			List<GridCell<Entity>> lastGridCells = curGridCells; 
			for(int i = 2; i <= visionRadius; i++){
				lastNghCreator = curNghCreator;
				lastGridCells = curGridCells;
				curNghCreator = new GridCellNgh<Entity>(grid, pt, Entity.class, i, i);
				curGridCells = curNghCreator.getNeighborhood(false); // true or false to include the center

				for (GridCell<Entity> cell : curGridCells){
					if ((grid.getDistance(pt, cell.getPoint()) >= (i - 1)
							&&
							grid.getDistance(pt, cell.getPoint()) <= i)
							&&
							!visibleCells.contains(cell.getPoint())){

						int x = cell.getPoint().getX();
						int y = cell.getPoint().getY();

						if(x == pt.getX()){ // horizontal
							if(y < pt.getY()){
								if(visibleCells.contains(new GridPoint(x,y+1)) && knownSpace[y+1][x] == -1){
									updateKnownSpace(cell);
								}
							}else if(y > pt.getY()){
								if(visibleCells.contains(new GridPoint(x,y-1)) && knownSpace[y-1][x] == -1){
									updateKnownSpace(cell);
								}
							}
						} else if(y == pt.getY()){ // vertical
							if(x < pt.getX()){
								if(visibleCells.contains(new GridPoint(x+1,y)) && knownSpace[y][x+1] == -1){
									updateKnownSpace(cell);
								}
							}else if(x > pt.getX()){
								if(visibleCells.contains(new GridPoint(x-1,y)) && knownSpace[y][x-1] == -1){
									updateKnownSpace(cell);
								}
							}
						} else if(x-pt.getX() == y-pt.getY()){ // diagonal
							if(y < pt.getY()){
								if((visibleCells.contains(new GridPoint(x+1,y+1)) && knownSpace[y+1][x+1] == -1)
										&&
										(
												(visibleCells.contains(new GridPoint(x+1,y)) && knownSpace[y][x+1] == -1)
												||
												(visibleCells.contains(new GridPoint(x,y+1)) && knownSpace[y+1][x] == -1))){
									updateKnownSpace(cell);
								}
							}else if(y > pt.getY()){
								if((visibleCells.contains(new GridPoint(x-1,y-1)) && knownSpace[y-1][x-1] == -1)
										&&
										(
												(visibleCells.contains(new GridPoint(x-1,y)) && knownSpace[y][x-1] == -1)
												||
												(visibleCells.contains(new GridPoint(x,y-1)) && knownSpace[y-1][x] == -1))){
									updateKnownSpace(cell);
								}
							}
						}
						visibleCells.add(cell.getPoint());
					}
				}
			}*/


			// TODO delete (debugging)
			System.out.print(pt.getX() + ", " + pt.getY() + " - ");
			System.out.println(explorer.getName());
			System.out.println(possibleMoves);
			//System.out.println(visibleCells);
			// print knownspace
			printKnownSpace();

			Random r = new Random();
			int i = r.nextInt(possibleMoves.size());
			GridPoint nextPos = possibleMoves.get(i);
			explorer.grid.moveTo(explorer,nextPos.getX(),nextPos.getY());

		}

		public void updateKnownSpace(GridCell<Entity> cell){
			int x = cell.getPoint().getX();
			int y = cell.getPoint().getY();

			if(cell.size() == 0){
				knownSpace[y][x] = -1;
			} else if(cell.items().iterator().next().getClass() == Wall.class){
				knownSpace[y][x] = 1;
			} else if(cell.items().iterator().next().getClass() == Rock.class){
				knownSpace[y][x] = 2;
			} else if(cell.items().iterator().next().getClass() == Exit.class){
				knownSpace[y][x] = 3;
			}
		}



	}

	private void printKnownSpace(){
		for(int y=knownSpace.length-1; y >= 0; y--){
			for(int x=0; x < knownSpace.length; x++){
				System.out.print('|');
				if(knownSpace[y][x] == -1){
					System.out.print(' ');
				} else if(knownSpace[y][x] == 0){
					System.out.print('O');
				} else if(knownSpace[y][x] == 1){
					System.out.print('X');
				} else if(knownSpace[y][x] == 2){
					System.out.print('R');
				} else if(knownSpace[y][x] == 3){
					System.out.print('E');
				}
			}
			System.out.println('|');
		}
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
