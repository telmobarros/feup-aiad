package creator;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class MazeBuilder {
	/**
	 * Generates a random maze using the Depth-first Search algorithm
	 * Maze will be created in a 2D-Array of chars with the following configuration:
	 * ' ' - Free space
	 * 'X' - Wall
	 * 'R' - Rock
	 * 'E' - Exit
	 * @param size Dimension of the squared maze (even numbers will turn into the next existing odd number since is required for the algorithm works properly)
	 * @return 2D-Array of chars containing the symbols of the maze
	 */
	public char[][] buildMaze(int size) throws IllegalArgumentException{

		/**
		 * transform size in odd number by adding 1
		 */
		if (size%2 == 0)
			size++;

		char[][] maze = new char[size][size];
		char[][] visited = new char[size/2][size/2];
		Point guide = new Point();
		Stack<Point> history = new Stack<Point>();

		/**
		 * Initialize base maze "wall blocked"
		 */
		Arrays.fill(maze[0], 'X');
		for (int y = 1; y < size; y++){
			for (int x = 0; x < size; x++){
				if (x%2 == 0)
					maze[y][x] = 'X';
				else
					maze[y][x] = ' ';
			}
			y++;
			Arrays.fill(maze[y], 'X');
		}

		/**
		 * Initialize base visited 2Darray
		 */
		for (int y = 0; y < visited.length; y++){
			Arrays.fill(visited[y], '.');
		}

		/**
		 * Places guide starting position randomly near borders
		 * Places exit in border
		 */
		Random r = new Random();
		int tmp;
		tmp = r.nextInt(4);
		switch (tmp){
		case 0:
			tmp = r.nextInt(size/2);
			guide.setLocation(0, tmp);
			maze[tmp*2+1][0] = 'E';
			break;

		case 1:
			tmp = r.nextInt(size/2);
			guide.setLocation(tmp, 0);
			maze[0][tmp*2+1] = 'E';
			break;

		case 2:
			tmp = r.nextInt(size/2);
			guide.setLocation(size/2 - 1, tmp);
			maze[tmp*2+1][size - 1] = 'E';
			break;

		case 3:
			tmp = r.nextInt(size/2);
			guide.setLocation(tmp,size/2 - 1);
			maze[size - 1][tmp*2+1] = 'E';
			break;
		}

		visited[(int)guide.y][(int)guide.x] = 'X';
		history.push(guide);



		String possDir = "";
		while (!history.empty()){
			possDir = "";
			try{
				if (visited[guide.y-1][guide.x] == '.')
					possDir += 'N';
			}catch (IndexOutOfBoundsException e){}
			try{
				if (visited[guide.y+1][guide.x] == '.')
					possDir += 'S';
			}catch (IndexOutOfBoundsException e){}
			try{
				if (visited[guide.y][guide.x-1] == '.')
					possDir += 'O';
			}catch (IndexOutOfBoundsException e){}
			try{
				if (visited[guide.y][guide.x+1] == '.')
					possDir += 'E';
			}catch (IndexOutOfBoundsException e){}

			if (possDir.length() != 0){
				switch(possDir.charAt(r.nextInt(possDir.length()))){
				case 'N':
					guide.y--;
					visited[guide.y][guide.x] = 'X';
					maze[guide.y*2+2][guide.x*2+1] = ' ';
					break;
				case 'S':
					guide.y++;
					visited[guide.y][guide.x] = 'X';
					maze[guide.y*2][guide.x*2+1] = ' ';
					break;
				case 'O':
					guide.x--;
					visited[guide.y][guide.x] = 'X';
					maze[guide.y*2+1][guide.x*2+2] = ' ';
					break;
				case 'E':
					guide.x++;
					visited[guide.y][guide.x] = 'X';
					maze[guide.y*2+1][guide.x*2] = ' ';
					break;
				default:
					break;
				}
				history.push(new Point(guide));
			}else{
				guide = history.pop();
			}
		}

		//Randomly place of dragon
		int rx, ry;
		do{
			rx = r.nextInt(size);
			ry = r.nextInt(size);
			if (maze[ry][rx] == ' '){
				maze[ry][rx] = 'R';
				break;
			}

		}while(true);

		return  maze;
	}

	/**
	 * Generates a maze and writes into a .txt file
	 * @param path Path of the .txt file to write
	 * @param size Dimensions of the squared maze
	 */
	public void buildMazetoTXT(String path, int size){
		Random r = new Random();
		char[][] maze;
		int rx, ry;

		maze = buildMaze(size);

		for(int i = 0; i < ((size*size)/32); i++){
			do{
				rx = r.nextInt(size);
				ry = r.nextInt(size);
				if (maze[ry][rx] == ' '){
					maze[ry][rx] = 'R';
					break;
				}
			}while(true);
		}

		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			for ( int y = 0; y < maze.length; y++)
			{
				for ( int x = 0; x < maze[y].length; x++)
				{    
					writer.write(maze[y][x]);
				}
				if(y != maze.length-1)
					writer.write("\n");
			}
			writer.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}
