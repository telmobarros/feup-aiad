package behaviours;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import main.Launcher;
import entities.Entity;
import entities.Exit;
import entities.Rock;
import entities.Wall;
import entities.agents.Explorer;
import entities.agents.Explorer.ExplorerState;
import entities.agents.SimpleExplorer;
import entities.agents.SuperExplorer;
import jade.lang.acl.ACLMessage;
import main.Launcher;
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

	private GridPoint help = null;
	private Stack<GridPoint> helpPath;
	private ArrayList<GridPoint> helpRequests;
	private boolean someoneNearIsAlreadyHelping;

	private int helpCount = 0;
	private int maxHelpCount = 5;

	private double lastKnownSpaceArea = 0;
	private double knownSpaceArea = 0;
	private boolean hasInformedLastStep = true;

	private Stack<GridPoint> exploringPath;

	@Override
	public void action() {
		// init variables
		this.agent = (T) this.myAgent;
		this.grid = agent.getGrid();
		this.visionRadius = agent.getVisionRadius();
		this.communicationRange = agent.getCommunicationRange();
		this.visibleAgents = new ArrayList<T>();

		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " started new step");}
		// update known space based on new info by other agents
		readNewMessages();
		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " udpated map knowledge based on others info");}
		// update own known space
		updateKnownSpace();
		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " udpated map knowledge based on my vision");}
		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " kwown map:\n" + knownSpaceString());}
		// update known space are
		updateKnownSpaceArea();

		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " was " + agent.getState().toString() + " [" + help + ", " + exit + "]");}
		//update state
		updateState();
		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " is " + agent.getState().toString() + " [" + help + ", " + exit + "]");}

		if(((knownSpaceArea - lastKnownSpaceArea) > 0.03) || agent.getState().equals(ExplorerState.EXITING)){
			hasInformedLastStep = !hasInformedLastStep;
			if(!hasInformedLastStep){
				lastKnownSpaceArea = knownSpaceArea;
				// find visible agents
				findVisibleAgents();
				if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " will inform new state to: " + visibleAgents.toString());}
				// inform visible agents about the known space (send known space to a method that accepts what we want to send and sends it to agents that we want)
				if (visibleAgents.size() != 0) {
					informStateAndKnownSpace(visibleAgents);
				}
			}
		}

		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " moves");}
		// make a move based on the state
		move();
	}

	private void updateKnownSpaceArea() {
		knownSpaceArea = 0;
		for(int y=0; y < agent.getKnownSpace().length; y++){
			for(int x=0; x < agent.getKnownSpace().length; x++){
				if(agent.getKnownSpace(y,x) != 'O'){
					knownSpaceArea++;
				}
			}
		}
		knownSpaceArea /= (agent.getKnownSpace().length*agent.getKnownSpace().length);
	}

	private void updateState() {
		if(exit != null && exit.getX() == myPoint.getX() && exit.getY() == myPoint.getY()){ // agent is in the exiting position
			agent.removeBehaviour(this);
			agent.doDelete();
			this.done();
			agent.setState(ExplorerState.EXITED);
			synchronized (Launcher.class) {
				Launcher.N_EXPLORERS--;
			}
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
		if(!agent.getState().equals(ExplorerState.HELPING) && !helpRequests.isEmpty()){
			help = getNearestHelpRequest();
			if(help != null){
				helpPath = AStar.getShortestPath(agent.getKnownSpace(), myPoint.getY(), myPoint.getX(), help.getY(), help.getX());
				if(!helpPath.isEmpty()){ // check if there is path between agent position and rock
					helpPath.pop(); // pop starting position that is the position where the agent is currently
					agent.setState(ExplorerState.HELPING);
					return;
				}
			}
		}

		// agent is exploring and sees a rock in its vision radius will ask for help to remove it
		if(!agent.getState().equals(ExplorerState.ASKING_HELP)){
			Random r = new Random();
			if(r.nextDouble() < Explorer.ASK_HELP_PROB){ // only asks for help with some probability
				help = getRockPoint();
				if(help != null){
					helpPath = AStar.getShortestPath(agent.getKnownSpace(), myPoint.getY(), myPoint.getX(), help.getY(), help.getX());
					if(!helpPath.isEmpty()){ // check if there is path between agent position and rock
						helpPath.pop(); // pop starting position that is the position where the agent is currently
						agent.setState(ExplorerState.ASKING_HELP);
						return;
					}
				}
			}
		}

		// gives up from trying to remove a rock if nobody helps him in 5 steps or the rock ir already been removed
		if (((agent.getState().equals(ExplorerState.ASKING_HELP)|| agent.getState().equals(ExplorerState.HELPING)) && helpCount >= maxHelpCount) // tired of waiting
				|| (agent.getState().equals(ExplorerState.ASKING_HELP) || agent.getState().equals(ExplorerState.HELPING)) //&& agent.getKnownSpace(askHelp.getX(),askHelp.getY()) == ' ') { // rock has been already removed
				|| (agent.getState().equals(ExplorerState.HELPING) && someoneNearIsAlreadyHelping)){ // someone near is already helping
			//System.out.println("Not receiving help :( I'm out after " + helpCount + " steps!");
			helpCount = 0;
			agent.setState(ExplorerState.EXPLORING);
			exploringPath = null;
		}
		if (agent.getState().equals(ExplorerState.EXPLORING) && (exploringPath == null || exploringPath.empty())) {
			// pick a point in unexplored area
			// make a path to that point
			//System.out.println("path: " + exploringPath);
			GridPoint unexpPt = getUnexploredPoint();
			if(unexpPt != null){
				Stack<GridPoint> testPath = AStar.getPathToUnexploredSpace(agent.getKnownSpace(), myPoint.getY(), myPoint.getX(), unexpPt.getY(), unexpPt.getX());
				exploringPath = testPath;
				if (exploringPath != null) {
					//exploringPath.pop();
					//System.out.println(myPoint + " New exploring path " + agent.getName() + " " + unexpPt + "\nNew exploring path: " + exploringPath);
				}
			}
		}
		updatePossibleMoves();
	}

	private GridPoint getNearestHelpRequest() {
		GridPoint nearestHelp = null;

		double maxDist = agent.getVisionRadius();
		for(GridPoint help: helpRequests){
			double dist = grid.getDistance(help, myPoint);
			if(dist < maxDist){
				maxDist = dist;
				nearestHelp = help;
			}
		}

		return nearestHelp;
	}

	private GridPoint getUnexploredPoint() {
		Random r = new Random();
		int x, y,
		visionRadiusMultiplier = 1,
		newPointRange, right, left, up, down,
		nLoops = 15; 
		do {
			visionRadiusMultiplier++;
			newPointRange = agent.getVisionRadius() * visionRadiusMultiplier;
			right= ((myPoint.getX() + newPointRange < agent.getKnownSpace().length-1) ? myPoint.getX() + newPointRange : agent.getKnownSpace().length-1);
			left = ((myPoint.getX() - newPointRange > 0) ? myPoint.getX() - newPointRange : 0);
			up= ((myPoint.getY() + newPointRange < agent.getKnownSpace().length-1) ? myPoint.getY() + newPointRange : agent.getKnownSpace().length-1);
			down= ((myPoint.getY() - newPointRange > 0) ? myPoint.getY() - newPointRange : 0);
			x = r.nextInt(right - left) + left;
			y = r.nextInt(up - down) + down;
			// because it might last too long or infinite

			nLoops--;
		} while (agent.getKnownSpace(y, x) != 'O' && nLoops >= 0);
		// returns null if unexplored point cannot be fined in reasonable time
		if (nLoops < 0)
			return null;

		return new GridPoint(x,y);
	}


	/*
	 * Get nearest rock within vision radius
	 */
	private GridPoint getRockPoint() {
		GridPoint rockPoint = null;

		double maxDist = agent.getVisionRadius();
		for(int y=0; y<agent.getKnownSpace().length; y++){
			for(int x=0; x < agent.getKnownSpace().length; x++){
				if(agent.getKnownSpace(y,x) == 'R'){
					double dist = grid.getDistance(new GridPoint(x,y), myPoint);
					if(dist < maxDist){
						maxDist = dist;
						rockPoint = new GridPoint(x,y);
					}
				}
			}
		}

		return rockPoint;
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

		// reset help requests and helping informations
		helpRequests = new ArrayList<GridPoint>();
		someoneNearIsAlreadyHelping = false;

		acl = myAgent.receive();
		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " reading new messages");}
		while(acl != null){
			parseAndUpdate(acl.getContent());
			acl = myAgent.receive();
		}
		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " received " + helpRequests.size() + " help requests");}
	}

	/*
	 * Merges two known spaces, the ones that the agent know and the possibly new received in a message
	 */
	private void parseAndUpdate(String message) {
		String[] lines = message.split("\n");
		String[] state = lines[0].split(" ");
		if(state[0].equals("ASKING_HELP") && (!agent.getState().equals("ASKING_HELP") || !agent.getState().equals("HELPING"))){
			int x = Integer.parseInt(state[1]);
			int y = Integer.parseInt(state[2]);
			helpRequests.add(new GridPoint(x,y));
		} else if(state[0].equals("HELPING") && agent.getState().equals("HELPING")){
			int x = Integer.parseInt(state[1]);
			int y = Integer.parseInt(state[2]);
			int nSteps = Integer.parseInt(state[3]);
			if(help == new GridPoint(x,y) && helpPath.size() > nSteps){
				someoneNearIsAlreadyHelping = true;
			}
		}

		if(!agent.getState().equals(ExplorerState.EXITING)){
			int mapDim = agent.getKnownSpace().length;
			// update known space
			for(int y = mapDim-1; y >= 0; y--){
				for(int x=0; x < mapDim; x++){
					agent.setKnownSpace(y, x, lines[mapDim-y].charAt(x));
				}
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
		content += state + " ";
		if(state.equals(ExplorerState.ASKING_HELP)){
			content += help.getX() + " " + help.getY();
		} else if(state.equals(ExplorerState.HELPING)) {
			content += help.getX() + " " + help.getY() + " " + helpPath.size();
		}
		content += "\n" + knownSpaceString();
		acl.setContent(content);

		for(T se : visibleAgents) {
			acl.addReceiver(se.getAID());
		}

		agent.send(acl);
		if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " sent: " + acl);}
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
			GridPoint nextPos;
			if (exploringPath != null && !exploringPath.empty()) {
				if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " is exploring " + exploringPath);}
				nextPos = exploringPath.pop();
			}
			else {
				if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " is exploring randomly");}
				Random r = new Random();
				int i = r.nextInt(possibleMoves.size());
				nextPos = possibleMoves.get(i);
			}
			grid.moveTo(agent,nextPos.getX(),nextPos.getY());
		} else if (agent.getState().equals(ExplorerState.ASKING_HELP) || agent.getState().equals(ExplorerState.HELPING)) {
			if(grid.getDistance(help, myPoint) > 1){// agent can be asking for help when moving towards the rock
				if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " is moving to a rock in " + help);}
				GridPoint nextHelpMove = helpPath.pop();
				grid.moveTo(agent, nextHelpMove.getX(), nextHelpMove.getY());
			}else{ // agent can be already next to the rock waiting for help and that way we will only wait 5 steps asking for help
				helpCount++;
			}
		} else if (agent.getState().equals(ExplorerState.EXITING)) {	// if the explorer already found the exit it starts to go to the exit
			if (Launcher.DEBUG) {System.out.println(agent.getLocalName() + " is exiting " + exitPath);}
			GridPoint nextExitMove = exitPath.pop();
			grid.moveTo(agent, nextExitMove.getX(), nextExitMove.getY());
		}
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
