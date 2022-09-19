/**
 * This class represents the Message objects of the Mobile Agent simulation
 * Each message object represents a single message that is being passed among
 * the nodes. A message could represent either an instruction (such as
 * instruct an adjacent node to change state) or it could
 * represent a log entry to be added on the log window by the base station.
 */

import java.awt.*;
import java.util.LinkedList;

public class Message implements Comparable<Message>{

    /* Point representing the Node where the message was created */
    private Point sourceHeader;
    /* Point representing the message's final destination */
    public final Point destinationHeader;
    /* Linked list of points indicating previously visited nodes */
    public final LinkedList<Point> previousHeader;
    /* String representing the actual message to be passed */
    public String infoMessage;
    /* Int representing the priority of the message (number from 1-3) */
    private int priority;

    /**
     * Message constructor.
     *
     * This constructor simply sets up the passed in variables to their
     * relative pre-defined correspondents.
     *
     * @param sourceHeader Point representing where the message was created
     *                     (its source)
     * @param destinationHeader Point representing the final destination of
     *                          the message (note that the point (-1,-1)
     *                          represents the base station.
     * @param previousHeader LinkedList of points representing the previously
     *                      visited nodes of the message.
     * @param infoMessage String representing the actual message
     * @param priority Priority, could be 1-2-3, 1 being the highest
     *                 priority, 3 being the lowest.
     */
    public Message(Point sourceHeader, Point destinationHeader,
                   LinkedList<Point> previousHeader, String infoMessage,
                   int priority){

        /* Initial location of the message */
        this.sourceHeader = sourceHeader;
        /* Final destination of the message */
        this.destinationHeader = destinationHeader;
        /* List of locations where the point has traveled */
        this.previousHeader = previousHeader;
        /* actual message to be passed in */
        this.infoMessage = infoMessage;
        /* priority - 3 levels, high:1, medium:2, low: 3 */
        this.priority = priority;
    }

    /**
     * compareTo override
     *
     * This method allows for the priorityBlockingQueue to pick a priority
     * between two Message objects. Note that priority are managed based on
     * the following scale: 1 - high, 2 - medium, 3 - low
     *
     * @param o representing the Message object.
     * @return integer representing the highest priority.
     */
    @Override
    public int compareTo(Message o) {
        if (this.priority == o.priority){
            return this.priority;
        }
        return this.priority-o.priority;
    }
}
