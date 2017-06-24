package ExCells26.Helper;

import ExCells26.Helper.Exceptions.FieldUnreachableException;
import ExCells26.Helper.Exceptions.FullFieldException;
import de.hsa.games.fatsquirrel.core.actions.OutOfViewException;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tillm on 02.06.2017.
 */
public class PathFinder {
    private List<Node> openList;
    private List<Node> closedList;
    private ControllerContext context;
    private BotCom botCom;
    private XY start, destination;
    private boolean walkOnMaster;

    public PathFinder(BotCom botCom) {
        this.botCom = botCom;
    }


    private static class Node {
        private final XY coordinate;
        private double fx;
        private Node predecessor;
        private boolean isInSight;
        private int destinationDistance;

        Node(XY coordinate, boolean isInSight) {
            this.coordinate = coordinate;
            this.isInSight = isInSight;
        }

        XY getCoordinate() {
            return coordinate;
        }

        double getFx() {
            return fx;
        }

        void setFx(double fx) {
            this.fx = fx;
        }

        Node getPredecessor() {
            return predecessor;
        }

        void setPredecessor(Node predecessor) {
            this.predecessor = predecessor;
        }

        boolean isInSight() {
            return isInSight;
        }

        int getDestinationDistance() {
            return destinationDistance;
        }

        void setDestinationDistance(int destinationDistance) {
            this.destinationDistance = destinationDistance;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Node && coordinate.equals(((Node) o).getCoordinate());
        }

        @Override
        public String toString() {
            return "" + coordinate + " F(x):" + fx + "In sight: " + isInSight;
        }
    }

    public XY directionTo(XY destination, ControllerContext context, boolean walkOnMaster) throws FullFieldException, FieldUnreachableException {
        openList = new ArrayList<>();
        closedList = new ArrayList<>();
        start = context.locate();
        this.destination = destination;
        this.walkOnMaster = walkOnMaster;

        openList.add(new Node(start, true));
        this.context = context;

        if (!isWalkable(destination, context))
            throw new FullFieldException();

        while (!openList.isEmpty()) {
            Node currentNode = popMinF(openList);
            if (currentNode.getCoordinate().equals(destination)) {
                return getSecondNode(currentNode).coordinate.minus(start);
            }
            if (!currentNode.isInSight)
                return getSecondNode(currentNode).coordinate.minus(start);

            closedList.add(currentNode);
            expandNode(currentNode);
        }
        throw new FieldUnreachableException();
    }

    private void expandNode(Node currentNode) {
        for (XY xy : XYsupport.directions()) {
            XY succXy = currentNode.getCoordinate().plus(xy);
            Node successor = new Node(succXy, XYsupport.isInRange(succXy, context.getViewUpperLeft(), context.getViewLowerRight()));
            if (closedList.contains(successor) || !isWalkable(successor.getCoordinate(), context) || openList.contains(successor))
                continue;

            //default = 5
            int distanceWeight = 1;
            int nodeWeightMultiplier = 1;
            //Magic happens here
            double tentativeFx = XYsupport.distanceInSteps(start, succXy) * distanceWeight
                    + nodeWeightMultiplier * nodeWeight(successor.getCoordinate()) + currentNode.getFx();
            successor.setFx(tentativeFx);
            successor.setDestinationDistance(XYsupport.distanceInSteps(succXy, destination));

            int position = containsPosition(openList, successor.coordinate);
            if (position != 0) {
                if (openList.get(position).getFx() > tentativeFx) {
                    openList.remove(position);
                    openList.add(successor);
                }
            } else
                openList.add(successor);

            successor.setPredecessor(currentNode);
        }
    }

    public boolean isWalkable(XY coordinate, ControllerContext context) {

        EntityType entityTypeAtNewField;
        if (!XYsupport.isInRange(coordinate, XY.ZERO_ZERO, botCom.getFieldLimit()))
            return false;

        try {
            entityTypeAtNewField = context.getEntityAt(coordinate);
            if (context.getEntityAt(start) == EntityType.MINI_SQUIRREL) {
                if (entityTypeAtNewField == EntityType.MASTER_SQUIRREL)
                    return context.isMine(coordinate) && walkOnMaster;
                else
                    return entityTypeAtNewField != EntityType.MINI_SQUIRREL
                            && entityTypeAtNewField != EntityType.WALL
                            && entityTypeAtNewField != EntityType.BAD_BEAST;
            }
            if (!context.isMine(coordinate) && (entityTypeAtNewField == EntityType.MASTER_SQUIRREL || entityTypeAtNewField == EntityType.MINI_SQUIRREL))
                return false;

        } catch (OutOfViewException e) {
            return true;
        }
        return entityTypeAtNewField != EntityType.WALL
                && entityTypeAtNewField != EntityType.BAD_BEAST
                && entityTypeAtNewField != EntityType.MASTER_SQUIRREL;
    }

    private Node popMinF(List<Node> openList) {
        Node min = null;
        for (Node n : openList) {
            if (min == null)
                min = n;
            else if (n.isInSight()) {
                if (n.getFx() + n.getDestinationDistance() < min.getFx() + min.getDestinationDistance())
                    min = n;
                else if (n.getFx() + n.getDestinationDistance() < min.getFx() + min.getDestinationDistance()
                        && XYsupport.distanceInSteps(start, n.getCoordinate()) <= XYsupport.distanceInSteps(start, min.getCoordinate()))
                    min = n;
            } else if (!n.isInSight())
                if (n.getFx() + n.getDestinationDistance() < min.getFx() + min.getDestinationDistance())
                    min = n;

        }
        openList.remove(min);

        return min;
    }

    private int containsPosition(List<Node> openList, XY position) {
        for (Node n : openList) {
            if (n.coordinate.equals(position))
                return openList.indexOf(n);
        }

        return 0;
    }

    private Node getSecondNode(Node lastNode) {
        Node predecessor = lastNode;
        if (predecessor.getPredecessor() == null)
            return predecessor;
        while (predecessor.getPredecessor().getPredecessor() != null) {
            predecessor = predecessor.getPredecessor();
        }

        return predecessor;
    }

    private double nodeWeight(XY position) {

        try {
            switch (context.getEntityAt(position)) {
                case BAD_PLANT:
                    return 100;
                case GOOD_BEAST:
                    return -200 + checkAdjacentBadBeast(position);
                case GOOD_PLANT:
                    return -100 + checkAdjacentBadBeast(position);
                case NONE:
                    return checkAdjacentBadBeast(position) + 20;
            }
        } catch (OutOfViewException e) {
            return 0;
        }
        return 0;
    }

    private double checkAdjacentBadBeast(XY position) {
        double cumulatedWeight = 0;
        int multiplier = 2;
        for (XY direction : XYsupport.directions()) {
            try {
                if (context.getEntityAt(position.plus(direction)) == EntityType.BAD_BEAST)
                    cumulatedWeight = cumulatedWeight + multiplier * 150;
                else if (context.getEntityAt(position.plus(direction)) == EntityType.MASTER_SQUIRREL
                        && !context.isMine(position.plus(direction)))
                    cumulatedWeight = cumulatedWeight + multiplier * 300;
            } catch (OutOfViewException e) {
                //Do nothing
            }
        }

        return cumulatedWeight;
    }

}

