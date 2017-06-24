package ExCells26.Squirrel;

import ExCells26.Helper.Exceptions.NoTargetException;
import ExCells26.Helper.XYsupport;
import de.hsa.games.fatsquirrel.core.actions.OutOfViewException;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.ArrayList;

/**
 * Created by Shiroten on 24.06.2017.
 */
public class SquirrelHelper {

    public static XY safeField(ControllerContext view) throws NoTargetException {
        ArrayList<XY> freeFields = new ArrayList<>();
        for (int j = -2; j < 3; j++) {
            for (int i = -2; i < 3; i++) {
                //Get All fields
                if (goodField(view.getEntityAt(view.locate().plus(new XY(i, j))))) {
                    freeFields.add(view.locate().plus(new XY(i, j)));
                }
            }
        }
        ArrayList<XY> toRemove = new ArrayList<>();
        for (XY field : freeFields) {
            //Check for BadBeast and remove all neighbour fields
            if (view.getEntityAt(field) == EntityType.BAD_BEAST) {
                for (XY vector : XYsupport.directions()) {
                    toRemove.add(field.plus(vector));
                }
                continue;
            }
            //Remove add more bad fields to remove list
            if (!goodField(view.getEntityAt(field))) {
                toRemove.add(field);
            }
        }
        freeFields.removeAll(toRemove);

        if (freeFields.isEmpty()) {
            //throw error if no freeField is left
            throw new NoSuchFieldError();
        }

        //return with next freeField if possible
        return freeFields.iterator().next();
    }

    public static XY findNextBadBeast(ControllerContext view) throws NoTargetException {
        XY positionOfTentativelyTarget = new XY(999, 999);
        for (int j = view.getViewUpperLeft().y; j < view.getViewLowerRight().y; j++) {
            for (int i = view.getViewUpperLeft().x; i < view.getViewLowerRight().x; i++) {
                if (view.getEntityAt(new XY(i, j)) != EntityType.BAD_BEAST) {
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

    public static boolean goodField(EntityType entityType) {
        if (entityType == EntityType.NONE
                || entityType == EntityType.GOOD_BEAST
                || entityType == EntityType.GOOD_PLANT) {
            return true;
        } else {
            return false;
        }
    }

    public static XY findNextGoodies(ControllerContext view) throws NoTargetException {
        XY positionOfTentativelyTarget = new XY(999, 999);
        for (int j = view.getViewUpperLeft().y; j < view.getViewLowerRight().y; j++) {
            for (int i = view.getViewUpperLeft().x; i < view.getViewLowerRight().x; i++) {

                if (!goodField(view.getEntityAt(new XY(i, j)))) {
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


}
