package ExCells26.Squirrel.Mini;


import ExCells26.Helper.BotCom;
import ExCells26.Helper.Cell;
import ExCells26.Helper.Exceptions.FieldUnreachableException;
import ExCells26.Helper.Exceptions.FullFieldException;
import ExCells26.Helper.Exceptions.NoTargetException;
import ExCells26.Helper.PathFinder;
import ExCells26.Helper.XYsupport;
import ExCells26.Squirrel.SquirrelHelper;
import de.hsa.games.fatsquirrel.Launcher;
import de.hsa.games.fatsquirrel.core.actions.OutOfViewException;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExCells26ReaperMini implements BotController {

    BotCom botCom;
    private Cell myCell;
    ArrayList<XY> unReachableGoodies = new ArrayList<>();
    private XY cornerVector = XY.UP;
    boolean goToMaster = false;
    private Logger logger = Logger.getLogger(Launcher.class.getName());
    private Cell linkList;
    private boolean init = true;

    public ExCells26ReaperMini(BotCom botCom) {
        this.botCom = botCom;
        this.linkList = botCom.getCellForNextMini();
    }

    public void setGoToMaster() {
        this.goToMaster = true;
    }

    @Override
    public void nextStep(ControllerContext view) {
        if (init) {
            init(view);
        }

        if (linkList != null) {
            linkList.setLastFeedback(view.getRemainingSteps());
        }
        if (XYsupport.isGreater(view.getViewLowerRight(), botCom.getFieldLimit())) {
            botCom.setFieldLimit(view.getViewLowerRight());
        }
        SquirrelHelper.checkRallyPoint(botCom, view.locate());

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

        linkList.setLastFeedback(view.getRemainingSteps());

        if (linkList != null) {
            if (!linkList.isInside(view.locate(), botCom)) {
                moveToTarget(view, linkList.getQuadrant(), false);
                return;
            }
        }

        XY positionOfNearestGoodies = null;
        try {
            positionOfNearestGoodies = SquirrelHelper.findNextGoodies(view);
        } catch (NoTargetException e) {
            logger.log(Level.WARNING, "NoTargetException of mini nextStep");
        }
        if (positionOfNearestGoodies != null) {
            moveToTarget(view, positionOfNearestGoodies, false);
            return;
        } else {
            try {
                runningCircle(view);
                return;
            } catch (NoTargetException e) {
                logger.log(Level.WARNING, "NoTargetException of mini nextStep/runningCircle");
            }
        }
    }

    private void changeCell (){
        linkList = linkList.getNextCell();
    }

    public void moveToTarget(ControllerContext view, XY target, boolean walkOnMaster) {
        PathFinder pf = new PathFinder(botCom);
        try {
            view.move(pf.directionTo(target, view, walkOnMaster));
        } catch (FullFieldException e) {
            logger.log(Level.FINER, "FullFieldException of mini nextStep");
            logger.log(Level.FINER, "Position: " + view.locate().toString());
            logger.log(Level.FINER, "Destination: " + target);
        } catch (FieldUnreachableException e) {
            logger.log(Level.FINER, "FieldUnreachableException of mini nextStep");
            logger.log(Level.FINER, "Position: " + view.locate().toString());
            logger.log(Level.FINER, "Destination: " + target);
        }
    }

    private void runningCircle(ControllerContext view) throws NoTargetException {
        PathFinder pf = new PathFinder(botCom);
        for (int i = 0; i < 8; i++) {
            if (pf.isWalkable(cornerVector.times(botCom.getCellsize() / 4), view)) {
                moveToTarget(view, cornerVector.times(botCom.getCellsize() / 4), false);
                return;
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
        moveToTarget(view, positionOfMaster, true);
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
                    logger.log(Level.WARNING, "OutOfViewException of mini getAccuratePositionOfMaster");
                }
            }
        }
        throw new NoTargetException();
    }

    private void init(ControllerContext view) {
        linkList = botCom.getNextRallyPoint(view.locate());
    }
}
