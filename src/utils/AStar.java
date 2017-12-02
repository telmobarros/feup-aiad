package utils;
import java.util.*;

import repast.simphony.space.grid.GridPoint;

public class AStar {
	public static final int COST = 1;

	static class Cell{  
		int heuristicCost = 0; //Heuristic cost
		int finalCost = 0; //G+H
		int x, y;
		Cell parent; 

		Cell(int y, int x){
			this.y = y;
			this.x = x; 
		}

		public int getX(){
			return x;
		}

		public int getY(){
			return y;
		}
		public GridPoint getPoint(){
			return new GridPoint(x,y);
		}


		@Override
		public String toString(){
			return "["+this.x+", "+this.y+"]";
		}
	}

	//Blocked cells are just null Cell values in grid
	static Cell [][] grid = new Cell[5][5];

	static PriorityQueue<Cell> open;

	static boolean closed[][];
	static int startX, startY;
	static int endX, endY;

	public static void setBlocked(int x, int y){
		grid[y][x] = null;
	}

	public static void setStartCell(int x, int y){
		startX = x;
		startY = y;
	}

	public static void setEndCell(int x, int y){
		endX = x;
		endY = y;
	}

	static void checkAndUpdateCost(Cell current, Cell t, int cost){
		if(t == null || closed[t.y][t.x])return;
		int t_final_cost = t.heuristicCost+cost;

		boolean inOpen = open.contains(t);
		if(!inOpen || t_final_cost<t.finalCost){
			t.finalCost = t_final_cost;
			t.parent = current;
			if(!inOpen)open.add(t);
		}
	}

	public static void AStar(){ 

		//add the start location to open list.
		open.add(grid[startY][startX]);

		Cell current;

		while(true){ 
			current = open.poll();
			if(current==null)break;
			closed[current.y][current.x]=true; 

			if(current.equals(grid[endY][endX])){
				return; 
			} 

			Cell t;  
			if(current.y-1>=0){ // down cell
				t = grid[current.y-1][current.x];
				checkAndUpdateCost(current, t, current.finalCost+COST);
			} 

			if(current.x-1>=0){ // left cell
				t = grid[current.y][current.x-1];
				checkAndUpdateCost(current, t, current.finalCost+COST); 
			}

			if(current.x+1<grid[0].length){	// right cell
				t = grid[current.y][current.x+1];
				checkAndUpdateCost(current, t, current.finalCost+COST); 
			}

			if(current.y+1<grid.length){ // up cell
				t = grid[current.y+1][current.x];
				checkAndUpdateCost(current, t, current.finalCost+COST); 
			}
		} 
	}

	public static Stack<GridPoint> getShortestPath(char[][] knownSpace, int sy, int sx, int ey, int ex){
		int mapDim = knownSpace.length;
		//Reset
		grid = new Cell[mapDim][mapDim];
		closed = new boolean[mapDim][mapDim];
		open = new PriorityQueue<>((Object o1, Object o2) -> {
			Cell c1 = (Cell)o1;
			Cell c2 = (Cell)o2;

			return c1.finalCost<c2.finalCost?-1:
				c1.finalCost>c2.finalCost?1:0;
		});
		//Set start position
		setStartCell(sx, sy);

		//Set End Location
		setEndCell(ex, ey);

		for(int y=0;y<mapDim;++y){
			for(int x=0;x<mapDim;++x){
				if(knownSpace[y][x] == ' ' || (x == ex && y == ey)){
					grid[y][x] = new Cell(y, x);
					grid[y][x].heuristicCost = Math.abs(y-endY)+Math.abs(x-endX);
				}
			}
		}
		grid[sy][sx].finalCost = 0;

		AStar();
		if(closed[endY][endX]){
			//Trace back the path
			Cell current = grid[endY][endX];
			Stack<GridPoint> stack = new Stack<GridPoint>();
			stack.push(current.getPoint());
			while(current.parent!=null){
				current = current.parent;
				stack.push(current.getPoint());
			}
			return stack;
		}else{
			return new Stack<GridPoint>();
		}
	}
	
	public static Stack<GridPoint> getPathToUnexploredSpace(char[][] knownSpace, int sy, int sx, int ey, int ex){
		int mapDim = knownSpace.length;
		//Reset
		grid = new Cell[mapDim][mapDim];
		closed = new boolean[mapDim][mapDim];
		open = new PriorityQueue<>((Object o1, Object o2) -> {
			Cell c1 = (Cell)o1;
			Cell c2 = (Cell)o2;

			return c1.finalCost<c2.finalCost?-1:
				c1.finalCost>c2.finalCost?1:0;
		});
		//Set start position
		setStartCell(sx, sy);

		//Set End Location
		setEndCell(ex, ey);

		for(int y=0;y<mapDim;++y){
			for(int x=0;x<mapDim;++x){
				if(knownSpace[y][x] == ' ' || knownSpace[y][x] == 'E' || knownSpace[y][x] == 'O'){
					grid[y][x] = new Cell(y, x);
					grid[y][x].heuristicCost = Math.abs(y-endY)+Math.abs(x-endX);
				}
			}
		}
		grid[sy][sx].finalCost = 0;

		AStar(); 
		
		if(closed[endY][endX]){
			Cell current = grid[endY][endX];
			Stack<GridPoint> stack = new Stack<GridPoint>();
			stack.push(current.getPoint());
			while(current.parent!=null){
				current = current.parent;
				stack.push(current.getPoint());
			} 
			Stack<GridPoint> helpStack = new Stack<GridPoint>();
			while (!stack.empty()) {
				GridPoint curr = stack.pop();
				if (knownSpace[curr.getY()][curr.getX()] == 'O') break;
				helpStack.push(curr);
			} 
			stack.clear();
			while (!helpStack.empty()) {
				stack.push(helpStack.pop());
			}
			return stack;
		}else{
			return new Stack<GridPoint>();
		}
	}
}