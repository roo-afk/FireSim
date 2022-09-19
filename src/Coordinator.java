/**
 * This class is the coordinator of the Mobile Agent program. It will manage
 * and set up the simulation whenever the program is launched. Here, the
 * input file will be read, the graph and nodes will be set up and the
 * threads will initially be launched. The GUI will also be called several
 * times to initialize and set up our graphic interface.
 *
 */

import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

import javafx.scene.control.Button;

/**
 * This class holds main
 * It will have several purposes:
 *
 * - Read in the config files
 * - Set up the Threats/Start them
 * - Set up the GUI (this class extends application)
 */
public class Coordinator extends Application {

    /* Input file variable. CHANGE THIS VARIABLE FOR TESTS */
    private final String INPUT_FILE = "sample.txt";
    /*
     * Collection of Nodes holding all nodes existing in graph - used for
     set up
     */
    private LinkedList<Node> allNodes;
    /* Boolean to indicate whether the simulation has started or not */
    private boolean started;
    /* Display object used for the GUI */
    private Display GUI;



    /**
     * Coordinator constructor
     *
     * Constructor for the coordinator class. This constructor will simply
     * initialize the linked list of Nodes called allNodes.
     */
    public Coordinator() {
        allNodes = new LinkedList<>();
    }

    /**
     * main
     *
     * Here, we will launch start and begin the set up of the simulation and
     * graphic interface.
     *
     * @param args command line args (not necessary here)
     */
    public static void main(String[] args) {
        Application.launch(Coordinator.class, args);
    }

    /**
     * Override of start Method
     *
     * This is the beginning of our program set up and simulation. In start,
     * we will call a function to read the input file, will set up the button
     * to start the simulation and we will set up the GUI.
     *
     * @param primaryStage Stage in which the simulation will be drawn
     * @throws Exception for reading input files.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        started = false;
        /* Start setting up and reading elements from input file */
        readInputFile();
        /* Set up the start button */
        Button start = new Button("Start");
        start.setOnAction(e->{
            if (!started) {
                /* Start the Simulation */
                startSimulation();
                /* Set the started boolean to true */
                started = true;
            }
        });

        /* Instantiate a GUI Object */
        GUI = new Display(primaryStage,allNodes);
        /* Draw the circles on the canvas */
        GUI.drawCircleOnCanvas();

        /* Set the root scene */
        primaryStage.setScene(GUI.setRoot(start));
        primaryStage.setTitle("Mobile Agents");
        primaryStage.show();
    }

    /**
     * startSimulation method
     *
     * This method will be called when the user clicks on the start button in
     * the GUI. This method basically starts the simulation (hence the name),
     * creates and starts threads associated with each node and starts the
     * initial agent thread on the base station.
     */
    private void startSimulation() {
        HashMap<Node, Thread> nodeToThread = new HashMap<>();
        Thread firstAgentThread = null;
        /* Set all the nodes and their associated threads in a hashmap */
        for (Node n: allNodes){
            if (n.isBaseStation){
                /* Start the initial agent */
                firstAgentThread = new Thread(new Agent(new PriorityBlockingQueue<>(),
                        new LinkedList<>(),n,true));
            }
            nodeToThread.putIfAbsent(n,new Thread(n));
            n.setGUI(GUI);
        }
        if (firstAgentThread != null) {
            /* Start the agent thread */
            firstAgentThread.start();
        } else {
            System.out.println("Error setting up the first agent. Please " +
                    "check your input file ");
        }
        /* Start all the threads! */
        for (Thread t: nodeToThread.values()){
            t.start();

        }
    }

    /**
     * setUpBaseStation method
     * <p>
     * method to set up the state of the base station. Note that a state can
     * either be
     * green (base station), blue (safe), yellow (in danger), red (burning).
     *
     * @param locationBaseStation Point representing the location of the base
     *                            station.
     */
    private void setUpBaseStation(Point locationBaseStation) {
        /* Loop through each node */
        for (Node n : allNodes) {
            /* check if the node is the current base station */
            if (n.getNodeLocation().equals(locationBaseStation)) {
                n.setState("green");
                n.setBaseStation(true);
                /* The base station always starts with an agent on the node */
                n.setAgentOnNodeBoolean(true);
                return;
            }
        }
    }

    /**
     * setUpFire method
     * <p>
     * method to set up the node(s) that is/are currently burning. Node that
     * we could start with multiple fires.
     *
     * @param locationFire Point representing the location at which a node is
     *                     burning.
     */
    private void setUpFire(Point locationFire) {
        /* Loop through all the nodes */
        for (Node n1 : allNodes) {
            /* Find the initially burning node(s) */
            if (n1.getNodeLocation().equals(locationFire)) {
                n1.setState("red");
                return;
            }
        }
    }

    /**
     * setUpAllStates method
     *
     * This method sets up the state of all the remaining nodes after the
     * base station, the fire node and the nodes in danger have been set up.
     */
    private void setUpAllStates(){
        /* Loop through all the nodes */
        for (Node n : allNodes){
            /* set up the state of each node */
            if (n.getState()!=null){
                continue;
            }else{
                /* If our node has no state initialized yet, it is neither a
                base station, neither on fire and neither in danger, so set
                it to blue - safe.
                 */
                n.setState("blue");
            }
        }
    }

    /**
     * setUpNodes method
     * <p>
     * This method is called by readInputFile as long as nodes are read in.
     * For each node read, setUpNode will instantiate a new node with given
     * coordinates and add it to allNode list.
     */
    private void setUpNodes(int xLocation, int yLocation) {
        /* add a new node to the list of all nodes */
        allNodes.add(new Node(new LinkedList<>(), null,
                new Point(xLocation,
                        yLocation), false,
                new PriorityBlockingQueue<>(), false));
    }

    /**
     * setUpEdges method
     * <p>
     * This method simply sets up the edges of each node and adds them to
     * their respective Collection of adjacent nodes.
     *
     * @param firstNodePoint  location of the first node to which we will add the
     *                        adjacent point
     * @param secondNodePoint location of the second node to which we will add
     *                        the adjacent point
     */
    private void setUpEdges(Point firstNodePoint, Point secondNodePoint) {
        /* Set up two temporary node variables */
        Node tempFirst = null;
        Node tempSecond = null;
        /* Loop through the nodes */
        for (Node n : allNodes) {
            /* Store the correct nodes in the two temp variables */
            if (n.getNodeLocation().equals(firstNodePoint)) {
                tempFirst = n;
            } else if (n.getNodeLocation().equals(secondNodePoint)) {
                tempSecond = n;
            }
        }
        /*
         * Add the temp nodes to the list of adjacent nodes of our initial
         * ones
         */
        if (tempFirst!= null && tempSecond != null) {
            tempFirst.addAdjacentPoint(tempSecond);
            tempSecond.addAdjacentPoint(tempFirst);
        }else{
            System.err.println("Error in setting up the edges, please check " +
                    "the input file");
        }

    }

    /**
     * readInputFile method
     * <p>
     * This method reads input file and sets up the read-in elements into their
     * corresponding object.
     */
    private void readInputFile() throws IOException {
        /* Read the tiles from file */
        InputStream in = getClass().getClassLoader()
                .getResourceAsStream(INPUT_FILE);
        if (in == null){
            System.err.println("Error in reading the input file, make sure " +
                    "the file exists!");
            return;
        }
        /* This is not elegant coding but it works without changing too much */
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String ln;
        /* First loop through the file and search for nodes */
        while ((ln = br.readLine()) != null) {
            String[] arr = ln.split(" ");
            if (arr[0].equals("node")){
                /* Set up the node */
                setUpNodes(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
            }
        }
        /*
         * Now read the file in again and search for edges, base station, and
         * the fire node. Read the tiles from
         * file
         */
        in = getClass().getClassLoader()
                .getResourceAsStream(INPUT_FILE);
        if (in == null){
            System.err.println("Error in reading the input file, make sure " +
                    "the file exists!");
            return;
        }
        /*
         * Reset the buffered reader and get info on edges, base station and
         * fires.
         */
        br = new BufferedReader(new InputStreamReader(in));
        while ((ln = br.readLine()) != null) {
            String[] arr = ln.split(" ");
            switch (arr[0]) {
                case "edge":
                    /* Parse the location of the first node */
                    Point firstNodePoint = new Point(Integer.parseInt(arr[1]),
                            Integer.parseInt(arr[2]));
                    /* Parse the location of second node */
                    Point secondNodePoint = new Point(Integer.parseInt(arr[3]),
                            Integer.parseInt(arr[4]));
                    /* Set up the edges */
                    setUpEdges(firstNodePoint, secondNodePoint);
                    break;
                case "station":
                    /* Set up the base station */
                    setUpBaseStation(new Point(Integer.parseInt(arr[1]),
                            Integer.parseInt(arr[2])));
                    break;
                case "fire":
                    setUpFire(new Point(Integer.parseInt(arr[1]),
                            Integer.parseInt(arr[2])));
                    break;
            }
        }
        /*
         * We are done reading in the file, now set all the non-initialized
         * bases for the nodes to blue - safe or yellow (if adjacent to a fire).
         */
        setUpAllStates();
    }
}
