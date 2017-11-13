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
import entities.agents.SuperExplorer;
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
		int mapDim;
		char[][] map;
		Parameters params = RunEnvironment.getInstance().getParameters();
		try{
			String mapFile = params.getString("mapFile");
			// open input stream for reading purpose.
			BufferedReader br = new BufferedReader(new FileReader(mapFile));


			//reads first line to get maze square size
			String  line = null;
			char temp;
			line = br.readLine();

			mapDim = line.length();
			int y = mapDim - 1;
			//initializes maze array with size of first line
			map = new char[mapDim][mapDim];

			//initializes grid
			GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
			Grid<Object> grid = gridFactory.createGrid(
					"grid",
					context,
					new GridBuilderParameters<Object>(new WrapAroundBorders(),
							new SimpleGridAdder<Object>(),
							true,
							mapDim, mapDim));

			do{
				for(int x = 0; x < mapDim;x++){
					temp = line.charAt(x);
					map[y][x] = temp;
					switch (temp){
					case 'X':
						Wall w = new Wall(grid);
						context.add(w);
						grid.moveTo(w, x, y);
						temp = ' ';
						break;

					case 'R':
						Rock r = new Rock(grid);
						context.add(r);
						grid.moveTo(r, x, y);
						temp = ' ';
						break;

					case 'E':
						Exit e = new Exit(grid);
						context.add(e);
						grid.moveTo(e, x, y);
						temp = ' ';
						break;

					default:
						break;
					}
				}
				y--;
			} while ((line = br.readLine()) != null);
			br.close();

			Random r = new Random();
			int nExplorers = params.getInteger("nExplorers");
			int nSuperExplorers = params.getInteger("nSuperExplorers");
			int visionRadius = params.getInteger("visionRadius");
			int communicationRange = params.getInteger("communicationRange");
			for (int i=0; i< nExplorers ; i++) {
				SimpleExplorer se = new SimpleExplorer(grid,visionRadius, mapDim, communicationRange);
				context.add(se);
				int rx;
				int ry;
				do{
					rx = r.nextInt(mapDim);
					ry = r.nextInt(mapDim);
				} while (map[ry][rx] != ' ');

				grid.moveTo(se, rx, ry);
			}
			for (int i=0; i< nSuperExplorers ; i++) {
				SuperExplorer se = new SuperExplorer(grid, visionRadius, mapDim);
				context.add(se);
				int rx;
				int ry;
				do{
					rx = r.nextInt(mapDim);
					ry = r.nextInt(mapDim);
				} while (map[ry][rx] != ' ');

				grid.moveTo(se, rx, ry);
			}

		}catch(Exception e){
			System.exit(1);
			e.printStackTrace();
		}

		return context;

	}

}
