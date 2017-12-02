package main;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import entities.Exit;
import entities.Rock;
import entities.Wall;
import entities.agents.SimpleExplorer;
import entities.agents.SuperExplorer;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import repast.simphony.space.grid.WrapAroundBorders;
import sajas.core.Agent;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Launcher extends RepastSLauncher {
	SimpleExplorer simpleExplorers[];
	SuperExplorer superExplorers[];
	
	public static boolean DEBUG = false;
	
	public static final boolean SEPARATE_CONTAINERS = true;
	private ContainerController mainContainer;
	private ContainerController simpleExplorersContainer;
	private ContainerController superExplorersContainer;

	public static Agent getAgent(Context<?> context, AID aid) {
		for(Object obj : context.getObjects(Agent.class)) {
			if(((Agent) obj).getAID().equals(aid)) {
				return (Agent) obj;
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return "Unknown space exploring";
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		if(SEPARATE_CONTAINERS) {
			Profile p2 = new ProfileImpl();
			simpleExplorersContainer = rt.createAgentContainer(p2);
			Profile p3 = new ProfileImpl();
			superExplorersContainer = rt.createAgentContainer(p3);
		} else {
			simpleExplorersContainer = mainContainer;
			superExplorersContainer = mainContainer;
		}
		
		launchAgents();
	}
	
	private void launchAgents() {
		
try {
			
			for (int i=0; i< simpleExplorers.length ; i++) {
				simpleExplorersContainer.acceptNewAgent("SimpleExplorer" + i, simpleExplorers[i]).start();
			}
			
			for (int i=0; i< superExplorers.length ; i++) {
				superExplorersContainer.acceptNewAgent("SuperExplorer" + i, superExplorers[i]).start();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public Context build(Context<Object> context) {
		context.setId("feup-aiad");

		// Parse map
		int mapDim;
		char[][] map;
		Parameters params = RunEnvironment.getInstance().getParameters();
		try{
			// debug variable from parameters
			DEBUG = params.getBoolean("debug");
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
					new GridBuilderParameters<Object>(new StrictBorders(),
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
			simpleExplorers = new SimpleExplorer[nExplorers];
			superExplorers = new SuperExplorer[nSuperExplorers];
			for (int i=0; i< nExplorers ; i++) {
				simpleExplorers[i] = new SimpleExplorer(grid,visionRadius, mapDim, communicationRange);
				context.add(simpleExplorers[i]);
				int rx;
				int ry;
				do{
					rx = r.nextInt(mapDim);
					ry = r.nextInt(mapDim);
				} while (map[ry][rx] != ' ');

				grid.moveTo(simpleExplorers[i], rx, ry);
			}
			for (int i=0; i< nSuperExplorers ; i++) {
				superExplorers[i] = new SuperExplorer(grid, visionRadius, mapDim);
				context.add(superExplorers[i]);
				int rx;
				int ry;
				do{
					rx = r.nextInt(mapDim);
					ry = r.nextInt(mapDim);
				} while (map[ry][rx] != ' ');

				grid.moveTo(superExplorers[i], rx, ry);
			}

		}catch(Exception e){
			System.exit(1);
			e.printStackTrace();
		}

		return super.build(context);
	}

}
