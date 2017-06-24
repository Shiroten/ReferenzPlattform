package GoodBeastChaser26;

import ExCells26.Mini.MiniType;
import GoodBeastChaser26.Helper.*;
import GoodBeastChaser26.Helper.Exceptions.FieldUnreachableException;
import GoodBeastChaser26.Helper.Exceptions.FullFieldException;
import GoodBeastChaser26.Helper.Exceptions.NoConnectingNeighbourException;
import de.hsa.games.fatsquirrel.core.actions.OutOfViewException;
import de.hsa.games.fatsquirrel.core.actions.SpawnException;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

public class GoodBeastChaser26Master implements BotController {

    private BotCom botCom;
    private Cell currentCell;
    private boolean firstCall = true;
    private ControllerContext view;
    private GoodBeastChaser26Mini miniOfCurrentCell;
    private boolean firstTimeInCell = true;
    private int waitCycleForFeral = 5;

    public GoodBeastChaser26Master(BotCom botCom) {
        this.botCom = botCom;
    }

    @Override
    public void nextStep(ControllerContext view) {
        toDoAtStartOfNextStep(view);
        if (firstCall) {
            initOfMaster(view);
        }


        if (view.getEnergy() > 500) {
            spawnMoreMinis();
        }


        if (view.getRemainingSteps() < 100) {
            collectingReapers();
            return;
        }

        if (currentCell.isInside(view.locate(), botCom) && firstTimeInCell) {
            return;
        }

        if (currentCell.getQuadrant().equals(view.locate()) && !firstTimeInCell) {
            try {
                botCom.expand();
                if (view.getEnergy() >= 100) {
                    if (currentCell.getMiniSquirrel() == null) {
                        spawningReaper(currentCell);
                    }
                } else {
                    //maybe something better
                    changeCurrentCell();
                }
            } catch (NoConnectingNeighbourException e) {
                //Todo: add to Log
                //e.printStackTrace();
            }
            changeCurrentCell();
        }
        moveToCurrentCell();
    }

    private void spawnMoreMinis() {
        Cell start = currentCell;
        Cell toCheck = start;
        while (true) {
            toCheck = toCheck.getNextCell();
            if (toCheck.getMiniSquirrel() == null) {
                break;
            } else {
                toCheck = toCheck.getNextCell();
            }

            if (toCheck.equals(start)) {
                try {
                    botCom.expand();
                } catch (NoConnectingNeighbourException e) {
                    return;
                }
            }
        }
        spawningReaper(toCheck);
        changeCurrentCell();
    }

    private void changeCurrentCell() {
        firstTimeInCell = true;
        currentCell = currentCell.getNextCell();
        //Todo: remove after debugging
        //System.out.println("\nGo to nextCell: " + currentCell);
    }

    private void toDoAtStartOfNextStep(ControllerContext view) {
        botCom.positionOfExCellMaster = view.locate();
        botCom.checkAttendance(view.getRemainingSteps());
        this.view = view;
    }

    private void spawningReaper(Cell cellForMini) {
        try {
            botCom.setNextMiniTypeToSpawn(MiniType.REAPER);
            botCom.setCellForNextMini(cellForMini);
            XY spawnDirection = cellForMini.getNextCell().getQuadrant().minus(view.locate());
            spawnDirection = XYsupport.normalizedVector(spawnDirection).times(-1);
            if (view.getEntityAt(view.locate().plus(spawnDirection)) == EntityType.NONE) {
                view.spawnMiniBot(spawnDirection, 100);
            } else {
                //Todo: adding can't spawn
            }
        } catch (SpawnException | OutOfViewException e) {
            //Todo: add to Log
            e.printStackTrace();
        }
    }

    private void moveToCurrentCell() {
        PathFinder pf = new PathFinder(botCom);
        XY betterMove = XY.ZERO_ZERO;
        try {
            betterMove = pf.directionTo(view.locate(), currentCell.getQuadrant(), view);
        } catch (FullFieldException | FieldUnreachableException e) {
            changeCurrentCell();
        }
        view.move(betterMove);
    }

    private void collectingReapers() {
        XY middle = new XY(botCom.getFieldLimit().x / 2, botCom.getFieldLimit().y / 2);
        PathFinder pf = new PathFinder(botCom);
        XY toMove = XY.ZERO_ZERO;
        try {
            toMove = pf.directionTo(view.locate(), middle, view);
        } catch (FullFieldException e) {
            //Todo: add to Log
            //e.printStackTrace();
        } catch (FieldUnreachableException e) {
        }
        view.move(XYsupport.normalizedVector(toMove));
    }

    private void initOfMaster(ControllerContext view) {
        botCom.setStartPositionOfMaster(view.locate());
        //Todo: set right after implementation of reconMini
        //botCom.setFieldLimit(view.locate());
        botCom.setFieldLimit(new XY(80, 60));
        botCom.setMaster(this);
        botCom.init();
        try {
            botCom.expand();
            botCom.expand();
            botCom.expand();
            botCom.expand();
        } catch (NoConnectingNeighbourException e) {
            //Todo: add to Log
            //e.printStackTrace();
        }
        firstCall = false;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    public void setCurrentCell(Cell currentCell) {
        this.currentCell = currentCell;
    }
}
