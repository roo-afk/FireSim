/**
 * This class represents the Agent objects of the Mobile Agent Simulation
 * program. Each Agent represents an independent Thread, located on a single
 * node.  
 *
 */

import javafx.application.Platform;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

public class Agent implements Runnable {

    /* AgentID, this will be xlocation-ylocation-nodeCounter */
    public String agentID;
    /* Agent blocking queue */
    private BlockingQueue<String> agentBlockQueue;
    /* Linked list of points where the agent previously was */
    private LinkedList<Point> previouslyVisited;
    /* Node on which the agent is located */
    private Node node;
    /* Boolean representing whether an agent is the first agent or not */
    private boolean firstAgent;

    /**
     * Agent constructor
     *
     * This constructor takes in an agent blocking queue, a linked list of
     * points representing the nodes the agent has previously visited, an
     * instand of the node the agent is currently on, and lastly a boolean
     * indicating whether the agent is the first agent created or not. The
     * constructor will set up all the passed in variables and will set up an
     * agent ID.
     *
     * @param agentBlockQueue blocking queue of the agent. Stores message
     *                        instructions.
     * @param previouslyVisited Linked list of previous nodes that the agent
     *                          visited.
     * @param node Node instance of the current location of the agent.
     * @param firstAgent Boolean indicating if the current agent is the first
     *                  agent created.
     */
    public Agent(BlockingQueue<String> agentBlockQueue,
                 LinkedList<Point> previouslyVisited,
                 Node node, boolean firstAgent) {

        /* Blocking queue of the agent thread */
        this.agentBlockQueue = agentBlockQueue;
        /* Linked list of nodes to check if a node was previously visited by
        the agent
         */
        this.previouslyVisited = previouslyVisited;
        /* Reference to the current node on which the agent currently is */
        this.node = node;
        /* Update the current node agent counter for ID purposes */
        node.updateAgentCounter();
        /* Unique ID of the agent */
        this.agentID =
                "" + node.getNodeLocation().x + node.getNodeLocation().y +
                        node.getAgentCounter();
        /* Boolean indicating if this agent is the first agent or not */
        this.firstAgent = firstAgent;
        /* Set the agent variable on the node to be true */
        this.node.setAgentOnNodeBoolean(true);
        /*
         * Add the current node in the list of previously visited nodes of
         * the agent
         */
        previouslyVisited.add(node.getNodeLocation());
        /* Set up the agent for the node */
        node.setAgent(this);
    }

    /**
     * addToBlockingQueue method
     * <p>
     * Simple method that adds a String (here a message) to the blocking
     * queue of our specific agent.
     *
     * @param message String representing the message to be passed in.
     * @throws InterruptedException exception handling for message passing.
     */
    public void addToBlockingQueue(String message) throws InterruptedException {
        if (!agentBlockQueue.contains(message)) {
            agentBlockQueue.put(message);
        }
    }

    /**
     * randomWalk method
     * <p>
     * This method simply makes the agent walk to the node specified in the
     * passed in argument. To do so, it will reset the agent dependent
     * variables of the node the agent is currently on, move to the specified
     * node and update its agent related information. Note that the agent is
     * ONLY updating information of the node it is currently on. It does not
     * update information of adjacent nodes.
     *
     * @param n Node to where the agent is required to move.
     * @throws InterruptedException Exception handling for the GUI calls.
     *
     */
    private void randomWalk(Node n) throws InterruptedException {
        /* if our node is not in previously visited */
        if (!previouslyVisited.contains(n.getNodeLocation())) {
            /* Reset the current node's agent info */
            node.setAgentOnNodeBoolean(false);
            node.setAgent(null);
            /* set the current node to the new node */
            this.node = n;
            /* Update the new node's agent info */
            node.setAgentOnNodeBoolean(true);
            node.setAgent(this);
            /* Update the GUI */
            Platform.runLater(() -> {
                try {
                    node.GUI.drawCircleOnCanvas();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            /* add the new node's location to previously visited */
            previouslyVisited.add(n.getNodeLocation());
            /* Wait one second before moving to new node */
            Thread.sleep(1000);
        }
    }

    /**
     * cloneAgent method
     * <p>
     * This method puts a message on the blocking queue of the Node it is
     * currently on to tell the Node to instruct the adjacent nodes to create
     * agents on the adjacent nodes
     *
     * @throws InterruptedException exception handling for message passing.
     */
    private void cloneAgent() throws InterruptedException {
        /* Add to the blocking queue of the current node a new message */
        node.addToBlockingQueue(new Message(node.getNodeLocation(),
                node.getNodeLocation(), new LinkedList<>(),
                "clone_agent", 1));
    }


    /**
     * run method
     *
     * This method will be called after each thread is started in Coordinator
     * .java or Node.java. In this method, the thread will at first (if we are
     * dealing
     * with the first agent on the graph) call randomWalk to make the agent
     * walk around the graph. As soon as the agent has reached a yellow node,
     * it will clone itself
     */
    @Override
    public void run() {
        try {
            while (true) {
                /* If we are currently on our first agent */
                if (this.firstAgent) {
                    boolean allVisited = true;
                    /* While the agent has not reached a yellow node, keep
                    walking!
                     */
                    while (node.getState().equals("blue") ||
                            node.getState().equals("green")) {
                        /* Grab a random node from the list of adjacent */
                        Collections.shuffle(node.getAdjacentPoint());
                        allVisited = true;
                        /* Loop through all adjacent nodes */
                        for (Node n : node.getAdjacentPoint()) {
                            /*
                             * Grab a node that has not been previously
                             * visited. This avoids constantly going back and
                             *  forth.
                             */
                            if (!previouslyVisited.contains(n.getNodeLocation())) {
                                /* Call randomWalk (move there) */
                                randomWalk(n);
                                allVisited = false;
                                break;
                            }
                        }
                        /*
                         * If we have visited all adjacent nodes, clear the
                         * list, this makes sure we do not stay stuck on a
                         * node.
                         */
                        if (allVisited){
                            previouslyVisited.clear();
                        }else{
                            /*
                             * Each time the agent walks on a node, send a
                             * message for the log at the base station
                             */
                            node.send(new Message(node.getNodeLocation(),
                                    new Point(-1, -1), new LinkedList<>(),
                                    "Agent: " + this.agentID +
                                    " walked on node: (" + node.getNodeLocation().x
                                    + ", " + node.getNodeLocation().y + ") "
                                    , 3));
                        }
                    }
                    /*
                     * If the agent reaches a yellow node, inform the base
                     * station and call clone agent
                     */
                    if (!allVisited) {
                        node.send(new Message(node.getNodeLocation(), new Point(-1, -1)
                                , new LinkedList<>(), "Agent: " + this.agentID +
                                " reached a yellow node at: (" + node.getNodeLocation().x
                                + ", " + node.getNodeLocation().y + ") "
                                , 3));
                        cloneAgent();
                    }
                }
                /*
                 * Grab element from the blocking queue - process it,
                 * otherwise wait. Note that we are not doing any busy waiting.
                 */
                String message = agentBlockQueue.take();
                /* If we find a message to kill the agent, set exit = true */
                if (message.equals("kill_yourself")) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            System.out.print("Agent Thread ID: " + this.agentID + " was " +
                    "interrupted");
        }
    }
}
