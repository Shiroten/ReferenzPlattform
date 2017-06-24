package ExCells26.Squirrel.Mini;


import ExCells26.Helper.BotCom;
import ExCells26.Helper.Cell;
import ExCells26.Helper.Exceptions.*;
import ExCells26.Helper.PathFinder;
import ExCells26.Helper.XYsupport;
import de.hsa.games.fatsquirrel.core.actions.OutOfViewException;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.ArrayList;

/**
 * Created by Shiroten on 15.06.2017.
 */
public class ExCells26ReaperMini implements BotController {

    BotCom botCom;
    private Cell myCell;

    ArrayList<XY> unReachableGoodies = new ArrayList<>();
    private XY cornerVector = XY.UP;
    boolean goToMaster = false;

    public ExCells26ReaperMini(BotCom botCom) {
        this.botCom = botCom;
        this.myCell = botCom.getCellForNextMini();
        this.myCell.setMiniSquirrel(this);
    }

    @Override
    public void nextStep(ControllerContext view) {
        if (view.getRemainingSteps() < 200) {
            goToMaster = true;
        }

        if (goToMaster) {
            executeGoToMaster(view);
            return;
        }

        if (view.getEnergy() > 1000 && view.getRemainingSteps() > 1600) {
            goToMaster = true;
        }

        if (view.getEnergy() > 2000) {
            goToMaster = true;
        }

        myCell.setLastFeedback(view.getRemainingSteps());

        XY toMove = XYsupport.normalizedVector(myCell.getQuadrant().minus(view.locate()));
        try {
            toMove = calculateTarget(view);
        } catch (NoTargetException e) {
            //If no Goodies
            try {
                toMove = runningCircle(view);
            } catch (NoTargetException e1) {
                //Run back to myCell middle
                PathFinder pf = new PathFinder(botCom);
                try {
                    toMove = pf.directionTo(view.locate(), myCell.getQuadrant(), view);
                } catch (FullFieldException e2) {
                    try {
                        myCell.setUsableCell(false);
                        myCell = botCom.freeCell();
                    } catch (FullGridException e3) {
                        try {
                            botCom.expand();
                        } catch (NoConnectingNeighbourException e4) {
                            //Bad Position
                        }
                    }

                } catch (FieldUnreachableException e2) {

                }
            }
        }
        if (!goToMaster) {
            try {
                if (view.isMine(view.locate().plus(toMove))) {
                    view.doNothing();
                }
            } catch (OutOfViewException e) {
                //Todo: add log
            }
        }
        view.move(toMove);
    }

    public void setMyCell(Cell myCell) {
        this.myCell = myCell;
    }

    public void setGoToMaster() {
        this.goToMaster = true;
    }

    private XY runningCircle(ControllerContext view) throws NoTargetException {
        PathFinder pf = new PathFinder(botCom);
        for (int i = 0; i < 8; i++) {
            if (myCell == null){
                try {
                    myCell = botCom.freeCell();
                } catch (FullGridException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (view.locate().equals(myCell.getQuadrant().plus(cornerVector.times(botCom.getCellsize() / 4)))) {
                    cornerVector = XYsupport.rotate(XYsupport.Rotation.clockwise, cornerVector, 1);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if (pf.isWalkable(myCell.getQuadrant().plus(cornerVector.times(botCom.getCellsize() / 4)), view)) {
                try {
                    return pf.directionTo(view.locate(),
                            myCell.getQuadrant().plus(cornerVector.times(botCom.getCellsize() / 4)),
                            view);
                } catch (FullFieldException | FieldUnreachableException e) {
                    cornerVector = XYsupport.rotate(XYsupport.Rotation.clockwise, cornerVector, 1);
                }
            } else {
                cornerVector = XYsupport.rotate(XYsupport.Rotation.clockwise, cornerVector, 1);
            }
        }
        throw new NoTargetException();
    }

    protected void executeGoToMaster(ControllerContext view) {
        XY positionOfMaster;
        if (botCom.positionOfExCellMaster.minus(view.locate()).length() > 10) {
            positionOfMaster = botCom.positionOfExCellMaster;
        } else {
            try {
                positionOfMaster = getAccuratePositionOfMaster(view);
            } catch (NoTargetException e) {
                positionOfMaster = botCom.positionOfExCellMaster;
            }
        }
        PathFinder pf = new PathFinder(botCom);
        try {
            view.move(pf.directionTo(view.locate(), positionOfMaster, view));
        } catch (FullFieldException | FieldUnreachableException e) {
            //Todo: add to Log
            String s = "executeGoToMaster Error";
        }
    }

    protected XY getAccuratePositionOfMaster(ControllerContext view) throws NoTargetException {
        for (int j = view.getViewUpperLeft().y; j < view.getViewLowerRight().y; j++) {
            for (int i = view.getViewUpperLeft().x; i < view.getViewLowerRight().x; i++) {
                try {
                    if (view.getEntityAt(new XY(i, j)) != EntityType.MASTER_SQUIRREL) {
                        continue;
                    }
                    if (view.isMine(new XY(i, j))) {
                        return new XY(i, j);
                    }
                } catch (OutOfViewException e) {
                    //Todo: add to Log
                    //e.printStackTrace();
                }
            }
        }
        throw new NoTargetException();
    }

    protected XY calculateTarget(ControllerContext view) throws NoTargetException {
        unReachableGoodies.clear();
        XY positionOfGoodTarget;
        XY toMove;
        PathFinder pf = new PathFinder(botCom);

        //numberOfTries can be incremented if needed
        int numberOfTries = 10;
        for (int i = 0; i < numberOfTries; i++) {
            positionOfGoodTarget = findNextGoodies(view);
            try {
                toMove = pf.directionTo(view.locate(), positionOfGoodTarget, view);
                return toMove;
            } catch (FullFieldException | FieldUnreachableException e) {
                unReachableGoodies.add(positionOfGoodTarget);
            }
        }
        //Todo: add to Log
        System.out.println("calculateTarget Error");
        throw new NoTargetException();
    }

    private XY findNextGoodies(ControllerContext view) throws NoTargetException {
        XY positionOfTentativelyTarget = new XY(999, 999);
        for (int j = view.getViewUpperLeft().y; j < view.getViewLowerRight().y; j++) {
            for (int i = view.getViewUpperLeft().x; i < view.getViewLowerRight().x; i++) {

                if (unReachableGoodies.contains(new XY(i, j))) {
                    continue;
                }

                if (myCell != null) {
                    if (!myCell.isInside(new XY(i, j), botCom)) {
                        continue;
                    }
                }

                if (!isGoodTargetAt(view, new XY(i, j))) {
                    continue;
                }
                if (new XY(i, j).minus(view.locate()).length() < positionOfTentativelyTarget.minus(view.locate()).length()) {
                    positionOfTentativelyTarget = new XY(i, j);
                }
            }
        }
        if (positionOfTentativelyTarget.length() > 1000) {
            throw new NoTargetException();
        }
        return positionOfTentativelyTarget;
    }

    private boolean isGoodTargetAt(ControllerContext view, XY position) {
        try {
            if (view.getEntityAt(position) == EntityType.GOOD_BEAST ||
                    view.getEntityAt(position) == EntityType.GOOD_PLANT) {
                return true;
            }
            if (view.getEntityAt(position) == EntityType.MINI_SQUIRREL && view.isMine(position)) {
                return false;
            }
        } catch (OutOfViewException e) {
            e.printStackTrace();
        }
        return false;
    }


}
