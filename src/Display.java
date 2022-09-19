/**
 * This class will be the main GUI class. In this class, all the drawings and
 * related Graphic elements will be made and performed. Everything that
 * relates to updating the GUI, drawing elements on canvases or setting the
 * simulation graphics are done in this class.
 *
 */


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.awt.*;
import java.util.LinkedList;


public class Display {
    /* log of all the messages gotten by the BaseStation */
    private LinkedList<String> log;
    /* HashMap linking a log text to a Message object - for duplicates */
    private LinkedList<Message> existingMessage;
    /* Integer representing the screenDimension */
    private int screenDimension;
    /* double representing the right shift of each node in the canvas */
    private double rightShift;
    /* double representing the down shift of each node in the canvas */
    private double downShift;
    /* double representing the diameter of each node in the canvas */
    private double diameter;
    /* double representing ratio that will be used to draw nodes in canvas */
    private double ratio;
    /* window Stage */
    private Stage window;
    /* list of all the nodes in our graph */
    private LinkedList<Node> allNodes;
    /* Vertical box that will hold the log text */
    private VBox logText;
    /* Canvas that will hold the tree drawing */
    private Canvas rootCanvas;
    /* Set up the scrollPane for the log */
    private ScrollPane sp;


    /**
     * Display constructor
     * <p>
     * The Display class will be the main GUI and javafx design class. All
     * the function of the mobile-agent that refer to
     * graphics or any form of drawing will be located in this class.
     *
     * @param window Stage representing the window
     */
    public Display(Stage window, LinkedList<Node> allNodes) {
        /* Set up the passed in window to our current window */
        this.window = window;
        /* set up the passed in list of nodes to our current list of nodes */
        this.allNodes = allNodes;
        /* Find the user's screen size */
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        /* Set up the screen dimension */
        screenDimension = (int) (0.85 * Math.min(screenSize.getWidth(),
                screenSize.getHeight()));
        /* Integer representing the dimension of a canvas */
        /* Integer representing the dimension of our main canvas */
        int canvasDimension = (int) (0.65 * screenDimension);

        /*
         * Now we set up some variables needed for the scaling and shifting
         * of the GUI. All this information is based on the screen of the
         * user and should automatically scale based on the screen size.
         * Drawing simple figures on a canvas are not a problem since no
         * interaction with the user is required. To update the state of the
         * GUI after everything is set up, simply call the function
         * drawCircleOnCanvas. We have used some special (and quite arbitrary)
         * rules concerning the scaling and placing of very large graphs. If
         * a graph has maximum node locations on the x or y axis that exceed
         * 30, we will turn the auto-scaling off and we will set up a general
         * ratio. For graphs that have max locations between 30 and 99, a
         * ratior of 30 will be used (arbitrary number chosen). For graphs
         * with max size over 100, a ratio of 1 will be used. These oversized
         * canvases will then be put in a scroll pane
         * from which the user will be able to see the whole graph. This is
         * especially important for graphs as big as lol.txt
         */

        /* Get the size of our list of nodes */
        int sizeNodes = allNodes.size();
        /*
         * Ratio that will be used to find actual x and y coordinate of each
         * node on the canvas
         */
        double tempMaxValue = getBiggestValue();
        /* If our max value is bigger than 100, use a ratio of 1 */
        if (tempMaxValue>=100){
            ratio = 1;
        }
        /*
         * If our max value is bigger than 30 but less than 100, use ratio =
         * 30
         */
        else if (tempMaxValue>30){
            ratio = 30;
        }
        /* Otherwise use a screen size based ratio */
        else{
            ratio =  (0.75 * canvasDimension) / tempMaxValue;
        }
        /* Now set up rest of variables */

        /* Boolean to know if we went out of bound or not */
        boolean scale;
        /*
         * If you max location is bigger than 30, do not use a right and down
         * shift, use an arbitrary hard-coded diameter
         */
        if (tempMaxValue>30){
            scale = false;
            rightShift = 0;
            downShift = 0;
            diameter = 15.0;
        } else {
            /* Otherwise use a screen dimension based scale */
            scale = true;
            /*
             * Right shift that allows the final graph to be in the middle of the
             * canvas. This shift will be based on the highest column elements of
             * our node positions.
             */
            rightShift = (canvasDimension - (ratio * findMaxCol())) / 2;
            /*
             * Down shift that allows the final graph to be in the middle of the
             * canvas. This shift will be based on the highest row elements of
             * our node positions.
             */
            downShift = (canvasDimension - (ratio * findMaxRow())) / 2;
            /*
             * Diameter of a circle. Based on the canvas dimension an on the
             * number of nodes we have in our list
             */
            diameter = canvasDimension / (3 * sizeNodes);
        }
        /* If we scaled our graph */
        if (scale) {
            /* Instantiate our root canvas (the graph will be drawn there) */
            rootCanvas = new Canvas(canvasDimension, canvasDimension);
        }
        else{
            /* Set up a (very) large canvas */
            rootCanvas = new Canvas(ratio*tempMaxValue+(diameter*2),
                    ratio*tempMaxValue+(diameter*2));
        }

        /* set up and draw the edges on the canvas */
        setUpEdges();
        /* instantiate the log  */
        log = new LinkedList<>();
        /* instantiate the existingMessage list */
        existingMessage = new LinkedList<>();
    }

    /* Get the biggest value between max row and max col */
    private double getBiggestValue(){
        return Math.max(findMaxRow(), findMaxCol());
    }

    /**
     * setRoot method
     * <p>
     * This method is the main Scene setting up method. It will set up and
     * combine all the elements that will appear on our root scene. These
     * include the command buttons, the log panel and the canvas in which the
     * simulation is made.
     * <p>
     * This method is public as it is called by start in the Coordinator
     * class.
     *
     * @return Scene that contains the graphic interface of the simulation.
     */
    public Scene setRoot(Button start) {
        /* This box will hold the rootCanvas and the logText panel */
        HBox hbox = new HBox(10);
        /*
         * This box will hold the hbox and the buttons to command the
         * simulation
         */
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        /* This box contains all the simulation command buttons */
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);
        /* Quit button, close the program when quit is pressed */
        Button quit = new Button("Quit");
        quit.setOnAction(e -> closeProgram(window));

        /* New scroll pane for the log */
        sp = new ScrollPane();
        sp.setPrefSize(0.30 * screenDimension,
                0.7 * screenDimension);

        /* Initialize the logText */
        logText = new VBox(40);
        logText.setMinSize(0.30 * screenDimension,
                0.7 * screenDimension);
        updateLog(new Message(null, null,
                null, "This is the Log Window",
                1));
        sp.setContent(logText);

        /* Set up the title */
        Text title = new Text("Mobile Agents Fire Propagation Simulation");
        title.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 30; " +
                "-fx-font-weight: bold;");

        /* Set up the scrollPane for the canvas */
        ScrollPane cp = new ScrollPane();
        cp.setPrefSize(0.7 * screenDimension,
                0.7 * screenDimension);
        cp.setContent(rootCanvas);

        /* Set up all the boxes and add the root to the return scene */
        buttons.getChildren().addAll(start, quit);
        hbox.getChildren().addAll(cp, sp);
        root.getChildren().addAll(title, hbox, buttons);
        return new Scene(root, screenDimension, screenDimension);
    }


    /**
     * setUpEdges method
     * <p>
     * This method is called at the beginning of the start up and draws the
     * edges between each node on the canvas. It will only be called once at
     * the beginning as the edges will never be modified once the program is
     * running.
     */
    private void setUpEdges() {
        /* Get the graphics context of our root canvas */
        GraphicsContext gc = rootCanvas.getGraphicsContext2D();
        gc.setLineWidth(1.5);
        gc.setStroke(Color.BLACK);
        /* find the radius */
        double radius = diameter / 2;
        /* Loop through each node in our list */
        for (Node initialNode : allNodes) {
            /* For each node, loop through the adjacent nodes */
            for (Node adj : initialNode.getAdjacentPoint()) {
                /*
                 * Find the starting and end locations of both the initial
                 * node and of the node adjacent to it. These coordinates
                 * will then be used to draw the line between two nodes.
                 */
                double startX = (ratio * initialNode.getNodeLocation().x)
                        + rightShift + radius;
                double startY = (ratio * initialNode.getNodeLocation().y)
                        + downShift + radius;
                double endX = (ratio * adj.getNodeLocation().x) + rightShift
                        + radius;
                double endY = (ratio * adj.getNodeLocation().y) + downShift
                        + radius;
                /* Draw the line between two nodes */
                gc.strokeLine(startX, startY, endX, endY);
            }
        }
    }


    /**
     * drawCircleOnCanvas method
     * <p>
     * This method simply draws all the nodes as circle on the canvas. This
     * method is public as it will need to be called whenever a node changes
     * state in order to illustrate that change on the GUI. This is probably
     * the most important method in our Display class as it will be called
     * repeatedly by every node (or thread) to draw updates on the graph.
     * Every time a node burns, turns yellow or an agent moves, this method
     * will be called.
     */
    public synchronized void drawCircleOnCanvas() throws InterruptedException {
        /* Loop through each node in our list of nodes */
        for (Node n : allNodes) {
            /* Set up the x and y coordinates to draw elements on our canvas */
            double x_coord = (ratio * n.getNodeLocation().x) + rightShift;
            double y_coord = (ratio * n.getNodeLocation().y) + downShift;
            /* get the graphics context of the root Canvas */
            GraphicsContext gc = rootCanvas.getGraphicsContext2D();
            /* Get the fill and stroke color based on the state of the node */
            gc.setFill(getStateColor(n.getState()));
            gc.setStroke(getStateColor(n.getState()));
            /* stroke and fill */
            gc.strokeOval(x_coord, y_coord, diameter, diameter);
            gc.fillOval(x_coord, y_coord, diameter, diameter);
            /* If the node has an agent on it, draw it! */
            if (n.getAgentOnNodeBoolean()) {
                /* Get the fill and stroke color based on the state of the node */
                gc.setStroke(Color.SANDYBROWN);
                gc.setLineWidth(4);
                /* stroke and fill */
                gc.strokeOval(x_coord, y_coord, diameter, diameter);
            }

        }

    }

    /**
     * getStateColor method
     * <p>
     * method that returns a color based on the String state given as a
     * parameter. This state represents the state of a node.
     *
     * @param state String representing the state of a node.
     * @return Color based on the state of a node
     */
    private Paint getStateColor(String state) {
        switch (state) {
            case "green":
                return Color.GREENYELLOW;
            case "yellow":
                return Color.YELLOW;
            case "red":
                return Color.RED;
            default:
                return Color.BLUE;
        }
    }

    /**
     * findMaxRow method
     * <p>
     * This method loops through the list of nodes and keeps track fo the
     * node that has the biggest row value. It will be used for the shifting
     * of the graph within the canvas.
     *
     * @return max row value in our list of nodes
     */
    private double findMaxRow() {
        int maxRow = 0;
        for (Node n : allNodes) {
            if (n.getNodeLocation().y > maxRow) {
                maxRow = n.getNodeLocation().y;
            }
        }
        return maxRow;
    }

    /**
     * findMaxCol method
     * <p>
     * This method loops through the list of nodes and keeps track fo the
     * node that has the biggest col value. It will be used for the shifting
     * of the graph within the canvas.
     *
     * @return max col value in our list of nodes
     */
    private double findMaxCol() {
        int maxCol = 0;
        for (Node n : allNodes) {
            if (n.getNodeLocation().x > maxCol) {
                maxCol = n.getNodeLocation().x;
            }
        }
        return maxCol;
    }


    /**
     * updateLog method
     * <p>
     * This method will update the log messages on the right side of the
     * window. This will give the user some information concerning the state
     * of the game and certain illegal moves that have been performed.
     * This method is public as it will need to be called by the Base Station
     * several times to update the Log Info.
     *
     * @param message String representing the message to show.
     */
    public synchronized void updateLog(Message message) {
        /* Check if the message already exists, if yes avoid duplicates */
        if (existingMessage.contains(message)) {
            return;
        }
        /* Clear the current logText, needed in order to update it */
        logText.getChildren().clear();
        /* Add the new message to existingMessage */
        existingMessage.add(message);
        /* Add the actual message String to the log */
        log.add(message.infoMessage + "\n");
        String info = "";
        for (String s : log) {
            info = info + s;
        }
        /* Update the GUI */
        Text log = new Text(info);
        log.setStyle("-fx-font-family: 'Comic Sans MS';");
        Label label2 = new Label(null, new TextFlow(log));
        logText.getChildren().addAll(label2);
        sp.setContent(logText);
    }


    /**
     * closeProgram method
     * <p>
     * This method simply closes a given window. Will be used throughout the
     * game whenever the user quits or restarts the game or when new decision
     * windows are called
     *
     * @param window Stage window to be closed.
     */
    private void closeProgram(Stage window) {
        window.close();
    }

}
