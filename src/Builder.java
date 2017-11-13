/**
 * 
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import entities.Exit;
import entities.Rock;
import entities.Wall;
import entities.agents.SimpleExplorer;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

/**
 *
 */
public class Builder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("feup-aiad");
		
		// Parse map
		String  line = null;
		int j = 0;
		char temp;
		char[][] map;
		try{
			// open input stream for reading purpose.
			BufferedReader br = new BufferedReader(new FileReader("map.txt"));
			//reads first line to get maze square size
			line = br.readLine();
			System.out.println(line);
			
			int mapDim = line.length();
			//initializes maze array with size of first line
			map = new char[mapDim][mapDim];
			
			//creates grid
			GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
			Grid<Object> grid = gridFactory.createGrid(
					"grid",
					context,
					new GridBuilderParameters<Object>(new WrapAroundBorders(),
							new SimpleGridAdder<Object>(),
							true,
							mapDim, mapDim));
			
			
			//fills first array with first line
			for(int i = 0; i < mapDim;i++){
				temp = line.charAt(i);
				map[0][i] = temp;
				Wall w = new Wall(grid);
				context.add(w);
				grid.moveTo(w, i, mapDim-1);
			}

			while ((line = br.readLine()) != null) {
				for(int i = 0; i < mapDim;i++){
					temp = line.charAt(i);
					map[j][i] = temp;
					switch (temp){
					case 'X':
						Wall w = new Wall(grid);
						context.add(w);
						grid.moveTo(w, i, mapDim-j-2);
						temp = ' ';
						break;

					case 'R':
						Rock r = new Rock(grid);
						context.add(r);
						grid.moveTo(r, i, mapDim-j-2);
						temp = ' ';
						break;

					case 'E':
						Exit e = new Exit(grid);
						context.add(e);
						grid.moveTo(e, i, mapDim-j-2);
						temp = ' ';
						break;

					default:
						break;
					}
				}
				j++;
			}

			br.close();
			
			Random r = new Random();
			Parameters params = RunEnvironment.getInstance().getParameters();
			int nExplorers = params.getInteger("nExplorers");
			int nSuperExplorers = params.getInteger("nSuperExplorers");
			for (int i=0; i< nExplorers ; i++) {
				SimpleExplorer se = new SimpleExplorer(grid);
				context.add(se);
				int x;
				int y;
				do{
					x = r.nextInt(mapDim);
					y = r.nextInt(mapDim);
				} while (map[y][x] != ' ');
				
				grid.moveTo(se, x, mapDim - y);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//int zombieCount = 5;
		//for (int i=0; i<zombieCount ; i++) {
		//context.add(new Wall(/*space,*/ grid));
		//}
		
		//for(Object obj : context) {
			//NdPoint pt = space.getLocation(obj);
			//grid.moveTo(obj, 10, 10);
			//} 

		return context;
				
	}

}
