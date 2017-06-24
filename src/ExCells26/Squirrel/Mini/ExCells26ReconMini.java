package ExCells26.Squirrel.Mini;


import ExCells26.Helper.*;
import ExCells26.Helper.Exceptions.FieldUnreachableException;
import ExCells26.Helper.Exceptions.FullFieldException;
import ExCells26.Helper.Exceptions.FullGridException;
import ExCells26.Helper.Exceptions.NoConnectingNeighbourException;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.utilities.XY;

/**
 * Created by Shiroten on 15.06.2017.
 */
public class ExCells26ReconMini implements BotController {

    private BotCom botCom;
    private int rightFieldLimit = 0;
    private int lowerFieldLimit = 0;
    private BotController reaperAI;

    public ExCells26ReconMini(BotCom botCom) {
        this.botCom = botCom;

    }


    @Override
    public void nextStep(ControllerContext view) {
        if(rightFieldLimit == 0 && lowerFieldLimit == 0){
            moveToPoint(view, view.getViewLowerRight());
            checkRightLimit(view);
            checkLowerLimit(view);

        } else if(rightFieldLimit == 0){
            moveToPoint(view, new XY(view.getViewLowerRight().x, view.locate().y));
            checkRightLimit(view);
        } else if(lowerFieldLimit == 0){
            moveToPoint(view, new XY(view.getViewLowerRight().y, view.locate().x));
            checkLowerLimit(view);
        } else if(!botCom.isFieldLimitFound()) {
            System.out.println("Ende gefunden, Captain!");
            botCom.setFieldLimit(new XY(rightFieldLimit, lowerFieldLimit));
            botCom.setFieldLimitFound(true);
        } else{
            if(reaperAI == null) {
                initReaperAi();
            }
        }

    }

    private void checkRightLimit(ControllerContext view){
        if(view.getViewLowerRight().x < view.locate().x + 10){
            rightFieldLimit = view.getViewLowerRight().x - 1;
        }
    }

    private void checkLowerLimit(ControllerContext view){
        if(view.getViewLowerRight().y < view.locate().y + 10){
            lowerFieldLimit = view.getViewLowerRight().y - 1;
        }
    }

    private void initReaperAi(){
        try {
            botCom.setCellForNextMini(botCom.freeCell());
            reaperAI = new ExCells26ReaperMini(botCom);
        } catch (FullGridException e){
            try{
                botCom.expand();
                initReaperAi();
            } catch (NoConnectingNeighbourException nE){
                //Do nothing
            }
        }
    }

    private void moveToPoint(ControllerContext view, XY destination){
        PathFinder pathFinder = new PathFinder(botCom);
        try {
            view.move(pathFinder.directionTo(view.locate(), destination, view));
        } catch (FullFieldException e){
            moveToPoint(view, destination.plus(XY.RIGHT_DOWN));
        } catch (FieldUnreachableException e){
            moveToPoint(view, destination.plus(XY.LEFT_UP));
        }
    }



}
