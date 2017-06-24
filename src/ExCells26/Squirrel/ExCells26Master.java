package ExCells26.Squirrel;

import ExCells26.Helper.BotCom;
import ExCells26.Helper.Cell;
import ExCells26.Helper.Exceptions.FieldUnreachableException;
import ExCells26.Helper.Exceptions.FullFieldException;
import ExCells26.Helper.Exceptions.NoTargetException;
import ExCells26.Helper.PathFinder;
import ExCells26.Helper.XYsupport;
import ExCells26.Squirrel.Mini.ExCells26ReaperMini;
import ExCells26.Squirrel.Mini.MiniType;
import de.hsa.games.fatsquirrel.Launcher;
import de.hsa.games.fatsquirrel.core.actions.OutOfViewException;
import de.hsa.games.fatsquirrel.core.actions.SpawnException;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExCells26Master implements BotController {

    private BotCom botCom;
    private Cell currentCell;
    private boolean firstCall = true;
    private ControllerContext view;
    private ExCells26ReaperMini miniOfCurrentCell;
    private boolean firstTimeInCell = true;
    private XY lastPosition;
    private boolean initialisation = true;
    private Cell linkList;
    private Logger logger = Logger.getLogger(Launcher.class.getName());

    public Cell getCurrentCell() {
        return linkList;
    }

    public void setCurrentCell(Cell linkList) {
        this.linkList = linkList;
    }

    public ExCells26Master(BotCom botCom) {
        this.botCom = botCom;
    }

    @Override
    public void nextStep(ControllerContext view) {
        if (firstCall) {
            initOfMaster(view);
            return;
        }
        toDoAtStartOfNextStep(view);

        if (view.getRemainingSteps() < 100) {
            System.out.println("getRemainingSteps");
            collectingReapers();
        }

        if (checkForDanger() < 8.5) {
            System.out.println("checkForDanger");
            System.out.println("checkDanger: " + checkForDanger());
            moveToCurrentCell();
            return;
        } else {
            if (initialisation) {
                if (view.getEnergy() > 400) {
                    spawningReaper(botCom.getNextRallyPoint(view.locate()), 200);
                    return;
                } else {
                    initialisation = false;
                }
            }
        }

        if (view.getEnergy() > 1000) {
            System.out.println("spawningReaper");
            spawningReaper(botCom.getNextRallyPoint(view.locate()), 200);
            return;
        }

        if (!(view.getRemainingSteps() > 100
                && maximumDensityOfNodes() && isSaturated())) {
            if (linkList.getQuadrant().minus(view.locate()).length() < 10) {
                if (firstTimeInCell && linkList.getMiniSquirrel() != null) {
                    System.out.println("collectMiniOfCell");
                    collectMiniOfCell();
                    return;
                }
                if (linkList.getMiniSquirrel() == null && view.getEnergy() > 200) {
                    System.out.println("spawningReaper and changeCurrentCell");
                    spawningReaper(linkList, 200);
                    changeCurrentCell();
                    return;
                }
            }

            System.out.println("moveToCurrentCell");
            moveToCurrentCell();
            return;

        }

        System.out.println("getMoreMinis");
        getMoreMinis();

    }


    private void getMoreMinis() {
        if (allActiveCellsAreUsed()) {

            spawnMoreMinis();
            return;


        }

        if (currentCell.getQuadrant().minus(view.locate()).length() < 7.1) {
            collectMiniOfCell();
        }
    }

    private boolean isSaturated() {
        //Todo: rework
        return false;
    }

    private boolean maximumDensityOfNodes() {
        int treshold = 5;
        if (botCom.getCellDistanceX() < treshold && botCom.getCellDistanceY() < treshold) {
            return true;
        }
        return false;
    }

    private double checkForDanger() {
        try {
            XY positionOfNearestBadBeast = SquirrelHelper.findNextEnemy(view);
            return positionOfNearestBadBeast.minus(view.locate()).length();
        } catch (NoTargetException e) {
            return 999;
        }
    }

    private void spawnMoreMinis() {

        if (view.getEnergy() > 200)
            spawningReaper(botCom.getNextRallyPoint(view.locate()), 200);

    }

    private boolean allActiveCellsAreUsed() {
        //Checks if all possible nodes for cells within parameters of cellDistance are used
        for (Cell cell : botCom.grid4.values()) {
            if (cell.isUsableCell() && !cell.isActive()) {
                return false;
            }
        }
        return true;
    }

    private void collectMiniOfCell() {
        firstTimeInCell = false;
        if (linkList.getMiniSquirrel() != null) {
            miniOfCurrentCell = linkList.getMiniSquirrel();
            miniOfCurrentCell.setGoToMaster();
            linkList.setMiniSquirrel(null);
        }
    }

    private void changeCurrentCell() {
        firstTimeInCell = true;
        linkList = linkList.getNextCell();
        System.out.println("\nGo to nextCell: " + linkList);
    }

    private void toDoAtStartOfNextStep(ControllerContext view) {
        botCom.positionOfExCellMaster = view.locate();

        this.view = view;
        if (XYsupport.isGreater(view.getViewLowerRight(), botCom.getFieldLimit())) {
            botCom.setFieldLimit(view.getViewLowerRight());
        }
        SquirrelHelper.checkRallyPoint(botCom, view.locate());
        if (linkList == null) {
            linkList = botCom.getNextRallyPoint(view.locate());
        }
    }

    private void spawningReaper(Cell cellForMini, int startEnergy) {
        try {
            botCom.setNextMiniTypeToSpawn(MiniType.REAPER);
            botCom.setCellForNextMini(cellForMini);
            for (XY vector : XYsupport.directions()) {
                if (view.getEntityAt(view.locate().plus(vector)) == EntityType.NONE) {
                    view.spawnMiniBot(vector, startEnergy);
                }
            }
        } catch (SpawnException | OutOfViewException e) {
            logger.log(Level.WARNING, "OutOfViewException of spawningReaper");
        }
    }

    private void moveToCurrentCell() {
        PathFinder pf = new PathFinder(botCom);
        XY betterMove = XY.ZERO_ZERO;
        if (linkList.getQuadrant().minus(view.locate()).length() < 10) {
            changeCurrentCell();
        }
        try {
            betterMove = pf.directionTo(linkList.getQuadrant(), view, false);
            System.out.println(betterMove + " " + linkList.getQuadrant());
        } catch (FieldUnreachableException e) {
            logger.log(Level.WARNING, "FieldUnreachableException of moveToCurrentCell");
            logger.log(Level.WARNING, "Position: " + view.locate().toString());
            logger.log(Level.WARNING, "Destination: " + linkList.getQuadrant());
            changeCurrentCell();
        } catch (FullFieldException e) {
            logger.log(Level.WARNING, "FullFieldException of moveToCurrentCell");
            logger.log(Level.WARNING, "Position: " + view.locate().toString());
            logger.log(Level.WARNING, "Destination: " + linkList.getQuadrant());
            changeCurrentCell();
        }

        if (view.getEntityAt(view.locate().plus(betterMove)) == EntityType.WALL) {
            changeCurrentCell();
            return;
        }
        view.move(betterMove);
    }

    private void collectingReapers() {
        XY middle = new XY(botCom.getFieldLimit().x / 2, botCom.getFieldLimit().y / 2);
        PathFinder pf = new PathFinder(botCom);
        XY toMove = XY.ZERO_ZERO;
        try {
            toMove = pf.directionTo(middle, view, false);
        } catch (FullFieldException e) {
            logger.log(Level.WARNING, "FullFieldException of collectingReapers");
        } catch (FieldUnreachableException e) {
            logger.log(Level.WARNING, "FieldUnreachableException of collectingReapers");
        }
        view.move(XYsupport.normalizedVector(toMove));
    }

    private void initOfMaster(ControllerContext view) {
        this.view = view;
        botCom.setStartPositionOfMaster(view.locate());
        //Todo: set right after implementation of reconMini
        //botCom.setFieldLimit(view.locate());
        botCom.setFieldLimit(new XY(80, 60));
        botCom.setMaster(this);
        this.lastPosition = view.locate();
        botCom.addRallyPoint(view.locate());
        linkList = botCom.getNextRallyPoint(view.locate());
        if (view.locate().equals(linkList.getQuadrant())) {
            calculateNextReallyPoint();
        }

        botCom.setNextMiniTypeToSpawn(MiniType.RECON);
        for (XY direction : XYsupport.directions()) {
            if (view.getEntityAt(view.locate().plus(direction)) == EntityType.NONE) {
                view.spawnMiniBot(direction, 100);
            }
        }
        firstCall = false;
    }

    private XY calculateNextReallyPoint() {
        for (int i = view.getViewUpperLeft().x; i < view.getViewLowerRight().x; i++) {
            for (int j = view.getViewUpperLeft().y; j < view.getViewLowerRight().y; j++) {
                if (view.getEntityAt(new XY(i, j)) == EntityType.NONE
                        && XYsupport.distanceInSteps(view.locate(), new XY(i, j)) == 15
                        && SquirrelHelper.checkRallyPoint(botCom, new XY(i, j))) {
                    return new XY(i, j);
                }
            }
        }
        return null;
    }


}
