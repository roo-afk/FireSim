/**
 * This class represents the Node objects of the Mobile Agent Simulation
 * program. Each Node represents an independent Thread, located on a single
 * node. Most of the message passing and state controlling will be done in
 * this class.
 *
 */

import javafx.application.Platform;
import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;


public class Node implements Runnable {

    /* Node's personal blocking queue */
    private final BlockingQueue<Message> nodeBlockQueue;
    /* Boolean indicating whether the node is the base station or not */
    public boolean isBaseStation;
    /* integer keeping track of the number of agents created on the node */
    private int agentCounter;
    /* Linked list of adjacent nodes */
    private LinkedList<Node> adjacentNodes;
    /* string representing the state of the node */
    private String state;
    /* Point representing the location of the node */
    private Point nodeLocation;
    /* Instance of the GUI - does NOT affect the simulation */
    public Display GUI;
    /* boolean indicating whether we can exit the thread or not */
    private boolean exit;
    /* boolean indicating whether there currently is an agent on the node */
    private boolean isAgentOnNode;
    /* Instance of the current agent on the node */
    private Agent agent;

    /**
     * Node Constructor
     *
     * This constructor takes in a linked list of adjacent nodes, an initial
     * state, a location, an initial blocking queue, and a boolean indicating
     * whether there is an agent on the node or not. It will simply set up
     * and initialize all the passed in variables.
     * @param adjacentNodes Linked list of adjacent nodes
     * @param state string representing the state of the node
     * @param nodeLocation Point representing the location of the node
     * @param isBaseStation Boolean indicating whether the node is the base
     *                      station or not
     * @param nodeBlockQueue Node's personal blocking queue
     * @param isAgentOnNode boolean indicating whether there currently is an
     *                      agent on the node
     */
    public Node(LinkedList<Node> adjacentNodes, String state, Point nodeLocation,
                boolean isBaseStation,
                BlockingQueue<Message> nodeBlockQueue,
                boolean isAgentOnNode) {
        /* List of adjacent points */
        this.adjacentNodes = adjacentNodes;
        /* State can be green, blue, yellow or red */
        this.state = state;
        /* nodeLocation refers to x-y coordinates of the node */
        this.nodeLocation = nodeLocation;
        /* Boolean that indicates if a node is the base station */
        this.isBaseStation = isBaseStation;
        /* Instantiate the node's blocking queue */
        this.nodeBlockQueue = nodeBlockQueue;
        /* Set the initial exit variable to false */
        exit = false;
        /* set up the agent on node boolean */
        this.isAgentOnNode = isAgentOnNode;
        /* Initialize the agentCounter to 0 */
        agentCounter = 0;

    }

    /**
     * agentCounter getter
     * @return integer representing the agent counter
     */
    public int getAgentCounter() {
        return agentCounter;
    }

    /**
     * agent setter. Takes in the agent on the node as argument and sets it
     * to the node's agent variable.
     * @param agent Agent on the current node.
     */
    public void setAgent(Agent agent){
        this.agent = agent;
    }

    /**
     * updateAgentCounter method
     * Simple method to increament the agent counter variable. Will be called
     * whenever a new agent is created on the node.
     */
    public void updateAgentCounter(){
        agentCounter++;
    }

    /**
     * getAgentOnNodeBoolean method
     *
     * This method simply returns true or false depending if there is an
     * agent on the current node or not
     * @return Boolean indicating if an agent is on the node or not.
     */
    public boolean getAgentOnNodeBoolean(){
        return this.isAgentOnNode;
    }

    /**
     * setAgentOnNodeBoolean method
     *
     * simple method to update the isAgentOnNode variable
     *
     * @param isAgentOnNode boolean indicating if an agent is on a current
     *                      node or not.
     * @return the current node.
     */
    public Node setAgentOnNodeBoolean(boolean isAgentOnNode){
        this.isAgentOnNode = isAgentOnNode;
        return this;
    }

    /**
     * addAdjacentPoint method
     * <p>
     * This method adds an adjacent point to the list of adjacent points of
     * our current point.
     *
     * @param p Point to add to the list.
     */
    public void addAdjacentPoint(Node p) {
        this.adjacentNodes.add(p);
    }

    /**
     * state getter
     * @return current state of the node as String.
     */
    public String getState() {
        return state;
    }

    /**
     * setState method
     * <p>
     * State setter method. Set the state of a specific point.
     *
     * @param state State to set at current point.
     * @return the specific Point updated.
     */
    public Node setState(String state) {
        this.state = state;
        return this;
    }

    /**
     * getNodeLocation method
     * <p>
     * return the location of the node (in Point)
     *
     * @return Point location of node
     */
    public Point getNodeLocation() {
        return this.nodeLocation;
    }

    /**
     * getAdjacentPoint method
     * <p>
     * get the list of nodes adjacent to the one we are currently on.
     *
     * @return HashSet of adjacent nodes
     */
    public LinkedList<Node> getAdjacentPoint() {
        return this.adjacentNodes;
    }

    /**
     * isBaseStation setter
     * <p>
     * simply set the isBaseStation boolean
     *
     * @param isBaseStation boolean indicating if node is the base station
     * @return current Node
     */
    public Node setBaseStation(boolean isBaseStation) {
        this.isBaseStation = isBaseStation;
        return this;
    }


    /**
     * setGUI method
     * <p>
     * This method simply references the GUI
     *
     * @param GUI our actual GUI reference
     */
    public void setGUI(Display GUI) {
        this.GUI = GUI;
    }

    /**
     * addToBlockingQueue method
     * <p>
     * Simple method that adds a Message object to the blocking
     * queue of our current node.
     *
     * @param message Message representing the message to be passed in.
     * @throws InterruptedException exception handling for message passing.
     */
    public void addToBlockingQueue(Message message) throws InterruptedException {
        if (!nodeBlockQueue.contains(message)) {
            nodeBlockQueue.put(message);
        }
    }

    /**
     * send Method
     * <p>
     * This method simply sends a message to all the adjacent node of our
     * current Node
     *
     * @param message Message object to be sent
     */
    public void send(Message message) throws InterruptedException {
        /*
         * If the message has reached the based station, no need to keep
         * sending it. Note that this takes care of the case when a message
         * is sent from the Base Station to the Base Station.
         */
        if (message.destinationHeader.equals(new Point(-1, -1))
                && isBaseStation) {
            /* We have reached our base station, add the log to GUI */
            Platform.runLater(() -> GUI.updateLog(message));
        }
        /* Add the current node to the list of visited nodes by the message */
        message.previousHeader.add(nodeLocation);
        for (Node n : adjacentNodes) {
            /* Except if it is already contained in the previousHeader */
            if (message.previousHeader.contains(n.getNodeLocation())) {
                continue;
            /* Or if the state of the node is red */
            } else if (n.getState().equals("red")) {
                continue;
            } else {
                n.addToBlockingQueue(message);
            }
        }
    }

    /**
     * changeStatesToRed method
     *
     * This method changes the states of the adjacent node of a current RED
     * node to red after a random delay. Note that the currently yellow nodes
     * (adjacent nodes) will be notified to become red by their current red
     * neighbor. As soon as the current red node notifies all of its neighbor
     * to become red, the current thread will exit.
     *
     * @throws InterruptedException exception handling for the thread sleep.
     */
    private void changeStatesToRed() throws InterruptedException {
        /* Loop through the list of adjacent nodes */
        for (Node n : adjacentNodes) {
            /* grab a random value representing the time to wait */
            int randTime = (int) (Math.random() * (7000 - 1000)) + 1000;
            /*
             * Sleep the thread for a specific amount of time, this will be
             * necessary as we do not want our newly alert nodes to directly
             * turn red. Give a certain delay before instructing these nodes
             * to become red (on fire).
             */
            Thread.sleep(randTime);
            /* Now we can instruct the adj node to burn */
            if (n.state.equals("yellow")) {
                n.nodeBlockQueue.add(new Message(this.nodeLocation,
                        n.getNodeLocation(), new LinkedList<>(),
                        "change_to_burn", 1));
            }
        }
    }

    /**
     * changeStatesToYellow method
     *
     * This method sends a message to all of the adjacent nodes of a
     * currently red node and instructs them to change their state to alert -
     * yellow.
     */
    private void changeStatesToYellow() {
        /* Loop through the list of nodes */
        for (Node n : adjacentNodes) {
            try {
                /* If the state of an adj node is blue or green */
                if (n.state.equals("blue") || n.state.equals("green")) {
                    /*
                     * add a message to the blocking queue of that node,
                     * instructing it to change its state to yellow
                     */
                    n.addToBlockingQueue(new Message(this.nodeLocation,
                            n.getNodeLocation(),
                            new LinkedList<>(), "change_to_alert",
                            2));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * processMessage Method
     * <p>
     * This Method process the messages left in the Node's blocking queue.
     * Those messages will basically be instructions for the Node to complete.
     * For instance, a message could be create_agent, meaning that the node
     * should create an agent on itself.
     *
     * @param message Message object to be processed.
     */
    private void processMessage(Message message) throws InterruptedException {
        /*
         * A destinationHeader of -1, -1 means we are looking for the base
         * station!
         */
        if (message.destinationHeader.equals(new Point(-1, -1))
                && isBaseStation) {
            /* We have reached our base station, add the log to GUI */
            Platform.runLater(() -> GUI.updateLog(message));
        }
        /*
         * If the message passed in is not at the correct destination, keep
         * passing it
         */
        else if (!message.destinationHeader.equals(nodeLocation)) {
            send(message);
            /* We are at our correct location */
        } else {
            switch (message.infoMessage) {
                /* Change the state to alert */
                case "change_to_alert":
                    /* Send a log message to the base station */
                    send(new Message(nodeLocation, new Point(-1, -1),
                            new LinkedList<>(), "Node: (" +
                            nodeLocation.x +
                            ", " + nodeLocation.y + ")" +
                            " " +
                            "became in danger", 1));
                    /*
                     * make the agent clone itself if there is an agent on
                     * the node
                     */
                    if (isAgentOnNode) {
                        cloneAgent();
                    }
                    this.state = "yellow";
                    /* Update the GUI */
                    Platform.runLater(() -> {
                        try {
                            GUI.drawCircleOnCanvas();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                    /* Change the node state to burning */
                case "change_to_burn":
                    this.state = "red";
                    /* Send a message to Base station saying node is on fire */
                    send(new Message(nodeLocation, new Point(-1, -1),
                            new LinkedList<>(), "Node: (" +
                            nodeLocation.x +
                            ", " + nodeLocation.y + ")" +
                            " " +
                            "caught on fire", 1));
                    if (agent != null) {
                        send(new Message(nodeLocation,
                                new Point(-1,-1), new LinkedList<>(),
                                "Agent " +
                                "with ID: "+ agent.agentID + " died on Node: ("
                                        + nodeLocation.x +
                                ", " + nodeLocation.y + ")",1));
                        isAgentOnNode = false;
                        agent.addToBlockingQueue("kill_yourself");
                    }
                    /* Update the GUI */
                    Platform.runLater(() -> {
                        try {
                            GUI.drawCircleOnCanvas();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                /* Case if which we need to clone the agent */
                case "clone_agent":
                    cloneAgent();
                    break;
                /* Case in which we need to create a new agent */
                case "create_agent":
                    createAgent();
                    /* Update the GUI */
                    Platform.runLater(() -> {
                        try {
                            GUI.drawCircleOnCanvas();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    /*
                     * Send a message to the base station saying a new agent
                     * has been created
                     */
                    send(new Message(nodeLocation,
                            new Point(-1,-1), new LinkedList<>(),
                            "Agent " +
                            "with ID: "+ agent.agentID + " created on Node: " +
                            "(" + nodeLocation.x +
                            ", " + nodeLocation.y + ")",1));
                    break;
            }
        }
    }

    /**
     * createAgent method
     *
     * Create an agent on your current node. This will be run as a new thread
     * that needs to be started.
     */
    private void createAgent() throws InterruptedException {
        /* Only create an agent if there is no other agent on the node */
        if (!isAgentOnNode) {
            /* Set isAgentOnNode to true */
            isAgentOnNode = true;
            /* Create the new thread agent */
            Thread t = new Thread(new Agent(new PriorityBlockingQueue<>(),
                    new LinkedList<>(), this, false));
            /* Start it */
            t.start();
        }
        /* If we create an agent on a yellow node, clone this agent */
        if (state.equals("yellow")){
            cloneAgent();
        }

    }

    /**
     * cloneAgent method
     *
     * This method sends a message to all adjacent nodes that are not red to
     * tell them to create an agent on their node!
     *
     * @throws InterruptedException exception handling for the message passing.
     */
    private void cloneAgent() throws InterruptedException {
        /* Loop through the adjacent nodes */
        for (Node n: adjacentNodes){
            /*
             * Do not clone if an agent already exists on the node or if the
             * node is burning
             */
            if (!n.getState().equals("red") && !n.isAgentOnNode) {
                /*
                 * Add a message to the adjacent node's blocking queue
                 * telling it to clone itself.
                 */
                n.addToBlockingQueue(new Message(nodeLocation,
                        n.getNodeLocation(),new LinkedList<>(),
                        "create_agent"
                        ,1));
            }
        }
    }

    /**
     * run method
     *
     * This method will be called after each thread is started in Coordinator
     * .java. In this method, the thread will at first check if the current
     * node is on fire. If yes, it will ask its neighbors to turn yellow,
     * then red. Otherwise, the thread will simply grab messages from its
     * blocking queue, process them and wait for new messages. Whenever the
     * node is burning, the thread will be exited and stopped.
     */
    @Override
    public void run() {
        try {
            while (!exit) {
                 if (this.state.equals("red")) {
                    /* Change the state of all the adjacent nodes to alert */
                    changeStatesToYellow();
                    /*
                     * Change the states of all adjacent nodes to red after a
                     * delay
                     */
                     changeStatesToRed();
                    exit = true;
                } else {
                /* Grab element from the blocking queue - process it,
                otherwise wait.
                 */
                    Message message = nodeBlockQueue.take();
                    /* call message processing method */
                    processMessage(message);
                }
            }
        } catch (InterruptedException e) {
            System.out.print("Thread at: " + nodeLocation + " was interrupted");
        }
    }

    /**
     * equals method Override.
     *
     * Simply overriding the equals method in order to be able to compare two
     * Node objects. This will be used in checking if a Node has already been
     * visited before or not.
     * @param obj Node passed in for comparison
     * @return Boolean, true if the nodes are the same, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        }
        /* Nodes are the same if their x and y coordinates match. */
        return ((this.nodeLocation.x == ((Node) obj).nodeLocation.x
                && (this.nodeLocation.y == ((Node) obj).nodeLocation.y)));
    }
}
