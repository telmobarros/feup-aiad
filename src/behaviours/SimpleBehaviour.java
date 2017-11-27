package behaviours;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import entities.Entity;
import entities.Exit;
import entities.Rock;
import entities.Wall;
import entities.agents.Explorer;
import entities.agents.Explorer.ExplorerState;
import entities.agents.SimpleExplorer;
import entities.agents.SuperExplorer;
import jade.lang.acl.ACLMessage;
import repast.simphony.context.Context;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import sajas.core.behaviours.CyclicBehaviour;
import utils.AStar;

public class SimpleBehaviour<T extends Explorer> extends CyclicBehaviour {

	private T agent;
	private Grid<Object> grid;
	private int visionRadius;
	private int communicationRange; 

	private GridPoint myPoint;
	private ArrayList<T> visibleAgents;
	private ArrayList<GridPoint> possibleMoves;
	
	private GridPoint exit = null;
	private Stack<GridPoint> exitPath;
	
	private GridPoint askHelp = null;

	private int helpCount = 0;
	private int maxHelpCount = 5;

	@Override
	public void action() {
		// init variables
		this.agent = (T) this.myAgent;
		this.grid = agent.getGrid();
		this.visionRadius = agent.getVisionRadius();
		this.communicationRange = agent.getCommunicationRange();
		this.visibleAgents = new ArrayList<T>();


		// update known space based on new info by other agents
		readNewMessages();
		// update own known space
		updateKnownSpace();


		//update state
		updateState();


		// find visible agents
		findVisibleAgents();
		// inform visible agents about the known space (send known space to a method that accepts what we want to send and sends it to agents that we want)
		if (visibleAgents.size() != 0) {
			// TODO delete print
			System.out.println("#visible agents " + visibleAgents.size());
			informStateAndKnownSpace(visibleAgents);
		}


		// make a move based on the state
		move();
	}

	private void updateState() {
		if(exit != null && exit.getX() == myPoint.getX() && exit.getY() == myPoint.getY()){ // agent is in the exiting position
			agent.removeBehaviour(this);
			agent.doDelete();
			this.done();
			agent.setState(ExplorerState.EXITED);
			return;
		} 
		if(!agent.getState().equals(ExplorerState.EXITING)){	// if the agent is not already exiting the map it will check if it is already possible
			exit = checkExit();
			if(exit != null){
				exitPath = AStar.getShortestPath(agent.getKnownSpace(), myPoint.getY(), myPoint.getX(), exit.getY(), exit.getX());
				if(!exitPath.isEmpty()){ // check if there is path between agent position and exit
					exitPath.pop(); // pop starting position that is the position where the agent is currently
					agent.setState(ExplorerState.EXITING);
					return;
				}
			}
		}
		if(!agent.getState().equals(ExplorerState.ASKING_HELP)){
			askHelp = getRockPoint();
			if(askHelp != null){
				agent.setState(ExplorerState.ASKING_HELP);
				return;
			}
		}
		if (agent.getState().equals(ExplorerState.ASKING_HELP) && helpCount >= maxHelpCount) {
			System.out.println("Not receiving help :( I'm out after " + helpCount + " steps!");
			helpCount = 0;
			agent.setState(ExplorerState.EXPLORING);
		}
		updatePossibleMoves();
	}

	private GridPoint getRockPoint() {
		ArrayList<GridPoint> rockPoints = new ArrayList<GridPoint>();
		int x = myPoint.getX();
		int y = myPoint.getY();
		if(agent.getKnownSpace(y-1,x) == 'R'){
			rockPoints.add(new GridPoint(x,y-1));
		}
		if(agent.getKnownSpace(y+1,x) == 'R'){
			rockPoints.add(new GridPoint(x,y+1));
		}
		if(agent.getKnownSpace(y,x-1) == 'R'){
			rockPoints.add(new GridPoint(x-1,y));
		}
		if(agent.getKnownSpace(y,x+1) == 'R'){
			rockPoints.add(new GridPoint(x+1,y));
		}
		
		if (rockPoints.size() > 0){
			Random r = new Random();
			int i = r.nextInt(rockPoints.size());
			return rockPoints.get(i);
		} else {
			return null;
		}
	}

	private void updatePossibleMoves() {
		possibleMoves = new ArrayList<GridPoint>();
		int x = myPoint.getX();
		int y = myPoint.getY();
		if(agent.getKnownSpace(y,x) == ' '){
			possibleMoves.add(new GridPoint(x,y));
		}
		if(agent.getKnownSpace(y-1,x) == ' '){
			possibleMoves.add(new GridPoint(x,y-1));
		}
		if(agent.getKnownSpace(y+1,x) == ' '){
			possibleMoves.add(new GridPoint(x,y+1));
		}
		if(agent.getKnownSpace(y,x-1) == ' '){
			possibleMoves.add(new GridPoint(x-1,y));
		}
		if(agent.getKnownSpace(y,x+1) == ' '){
			possibleMoves.add(new GridPoint(x+1,y));
		}
	}

	private GridPoint checkExit() {
		for(int y=0; y<agent.getKnownSpace().length; y++){
			for(int x=0; x < agent.getKnownSpace().length; x++){
				if(agent.getKnownSpace(y,x) == 'E'){
					return new GridPoint(x,y);
				}
			}
		}
		return null;
	}

	/*
	 *  reads all messages and calls update known space method
	 */
	private void readNewMessages() {
		ACLMessage acl = new ACLMessage();

		acl = myAgent.receive();

		while(acl != null){
			System.out.println(acl);
			parseAndUpdate(acl.getContent());
			acl = myAgent.receive();
		}

	}

	/*
	 * Merges two known spaces, the ones that the agent know and the possibly new received in a message
	 */
	private void parseAndUpdate(String message) {
		String[] lines = message.split("\n");
		int mapDim = agent.getKnownSpace().length;
		// update known space
		for(int y = mapDim-1; y >= 0; y--){
			for(int x=0; x < mapDim; x++){
				agent.setKnownSpace(y, x, lines[mapDim-y].charAt(x));
			}
		}
	}


	private void updateKnownSpace() {
		this.myPoint = grid.getLocation(agent);

		// get neighboring entities to update the knowledge map
		ArrayList<GridPoint> possibleMoves = new ArrayList<GridPoint>();
		ArrayList<GridPoint> visibleCells = new ArrayList<GridPoint>();

		// create neighborhood of entities
		GridCellNgh<Entity> nghCreator = new GridCellNgh<Entity>(grid, myPoint, Entity.class, visionRadius, visionRadius);
		List<GridCell<Entity>> gridCells = nghCreator.getNeighborhood(true); // true or false to include the center

		// to hold cells between 1 and vision radius
		List<GridCell<Entity>> rangeCells = new ArrayList<GridCell<Entity>>(); 
		for (GridCell<Entity> cell : gridCells){
			if ((grid.getDistance(myPoint, cell.getPoint()) <= 1)){
				updateKnownSpaceOn(cell);
				visibleCells.add(cell.getPoint());
				//				//if its an empty space near center or center add to possible moves
				//				if(agent.getKnownSpace(cell.getPoint().getY(), cell.getPoint().getX()) == ' '){
				//					possibleMoves.add(cell.getPoint());
				//				}
				//				else if(agent.getKnownSpace(cell.getPoint().getY(), cell.getPoint().getX()) == 'R'){
				//					agent.setState(ExplorerState.ASKING_HELP);
				//				}
			} else if (grid.getDistance(myPoint, cell.getPoint()) > 1 && grid.getDistance(myPoint, cell.getPoint()) <= visionRadius){
				rangeCells.add(cell);
			}
		}
		rangeCells.sort(createComparator(grid, myPoint));
		for (GridCell<Entity> cell : rangeCells){

			int x = cell.getPoint().getX();
			int y = cell.getPoint().getY();

			// it will give -1, +1 or 0
			int difX = 0;
			if (myPoint.getX() != x) difX = (myPoint.getX()-x)/(Math.abs(myPoint.getX()-x));
			int difY = 0;
			if (myPoint.getY() != y) difY = (myPoint.getY()-y)/(Math.abs(myPoint.getY()-y));
			boolean isVisible = false;

			if(visibleCells.contains(new GridPoint(x+difX,y+difY)) && agent.getKnownSpace(y+difY, x+difX) == ' '){ // horizontal, vertical
				if(difX != 0 && difY != 0){		// all the other diagonals
					if((visibleCells.contains(new GridPoint(x+difX,y)) && agent.getKnownSpace(y, x+difX) == ' ')
							||
							(visibleCells.contains(new GridPoint(x,y+difY)) && agent.getKnownSpace(y+difY, x) == ' ')){
						isVisible = true;
					}
				} else {
					isVisible = true;
				}

				if(isVisible){
					updateKnownSpaceOn(cell);
					visibleCells.add(cell.getPoint());
				}
			}
		}
		//this.possibleMoves = possibleMoves;
	}

	private void updateKnownSpaceOn(GridCell<Entity> cell){
		int x = cell.getPoint().getX();
		int y = cell.getPoint().getY();

		if(cell.size() == 0){
			agent.setKnownSpace(y, x, ' ');
		} else if(cell.items().iterator().next().getClass() == Wall.class){
			agent.setKnownSpace(y, x, 'X');
		} else if(cell.items().iterator().next().getClass() == Rock.class){
			agent.setKnownSpace(y, x, 'R');
		} else if(cell.items().iterator().next().getClass() == Exit.class){
			agent.setKnownSpace(y, x, 'E');
		}
	}

	private void findVisibleAgents() { 
		// TODO make them not see each other across the bound walls
		this.visibleAgents.clear();
		GridCellNgh<Explorer> agentsNghCreator = new GridCellNgh<Explorer>(grid, myPoint, Explorer.class , communicationRange, communicationRange);
		List<GridCell<Explorer>> gridCellsAgents = agentsNghCreator.getNeighborhood(true); // true or false to include the center
		for(GridCell<Explorer> cell : gridCellsAgents) {
			for(Explorer se : cell.items()){
				if(se.getClass().equals(agent.getClass()) && !this.visibleAgents.contains((T)se) && se != agent) {
					this.visibleAgents.add((T)se);
				}
			}
		}
	}

	// TODO method inform whose arguments are agents we want to inform and info we want to inform about

	private void informStateAndKnownSpace(ArrayList<T> visibleAgents) {
		ExplorerState state = agent.getState();
		ACLMessage acl = new ACLMessage(ACLMessage.INFORM);
		String content = new String();
		content += state;
		if(state.equals(ExplorerState.ASKING_HELP)){
			content += askHelp;
		}
		content += "\n" + knownSpaceString();
		acl.setContent(content);

		System.out.println(acl);

		for(T se : visibleAgents) {
			acl.addReceiver(se.getAID());
			System.out.println(se);
		}

		agent.send(acl);
	}

	/*
	 * Returns a string of the known space 2d array that can be used to debug or send to others agents
	 */
	private String knownSpaceString() {
		String result = new String();
		for(int y=agent.getKnownSpace().length-1; y >= 0; y--){
			for(int x=0; x < agent.getKnownSpace().length; x++){
				result += agent.getKnownSpace(y, x);
			}
			result += '\n';
		}
		return result;
	}

	private void move() {
		if (agent.getState().equals(ExplorerState.EXPLORING)) {
			Random r = new Random();
			int i = r.nextInt(possibleMoves.size());
			GridPoint nextPos = possibleMoves.get(i);
			grid.moveTo(agent,nextPos.getX(),nextPos.getY());
		} else if (agent.getState().equals(ExplorerState.ASKING_HELP) && helpCount < maxHelpCount) {
			helpCount++;
			// askForHelp(visibleAgents);
			//return;
		} else if (agent.getState().equals(ExplorerState.EXITING)) {	// if the explorer already found the exit it starts to go to the exit
			System.out.println("OMW to exit");
			GridPoint nextExitMove = exitPath.pop();
			grid.moveTo(agent, nextExitMove.getX(), nextExitMove.getY());
		}
	}

	private void askForHelp(ArrayList<T> visibleAgents) {
		ExplorerState state = agent.getState();
		helpCount++;
		ACLMessage acl = new ACLMessage();
		acl.setContent(state + "\n" + myPoint);

		for(T se : visibleAgents) {
			acl.addReceiver(se.getAID());
			System.out.println(se);
		}

		agent.send(acl);
		System.out.println("Help request: " + acl + "\nAsking " + (maxHelpCount-helpCount+1) + " more times!");

		//acl = agent.receive();
	}

	/*
	 *  util method to order cells by distance from point pt
	 */
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
