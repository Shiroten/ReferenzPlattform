package ExCells26.Helper;

import de.hsa.games.fatsquirrel.utilities.XY;

import static java.lang.Math.PI;

/**
 * Functions to manipulate XY-coordinates
 */
public class XYsupport {
    public enum Rotation {
        clockwise,
        anticlockwise,
    }

    public static XY[] directions() {
        return new XY[]{XY.RIGHT, XY.LEFT, XY.DOWN, XY.UP, XY.LEFT_UP, XY.LEFT_DOWN, XY.RIGHT_DOWN, XY.RIGHT_UP};
    }

    public static XY randomDirection() {
        return new XY(randomWithRange(-1, 1), randomWithRange(-1, 1));
    }

    public static XY rotate(Rotation r, XY toRotate, int numberOfRotation) {

        double rotationClockwise = (PI / 4 * numberOfRotation);
        double rotationAntiClockwise = (-PI / 4 * numberOfRotation);

        switch (r) {
            case clockwise:
                return new XY(
                        (int) Math.round(toRotate.x * Math.cos(rotationClockwise)
                                - toRotate.y * Math.sin(rotationClockwise)),
                        (int) Math.round(toRotate.x * Math.sin(rotationClockwise)
                                + toRotate.y * Math.cos(rotationClockwise)));
            case anticlockwise:
                return new XY(
                        (int) Math.round(toRotate.x * Math.cos(rotationAntiClockwise)
                                - toRotate.y * Math.sin(rotationAntiClockwise)),
                        (int) Math.round(toRotate.x * Math.sin(rotationAntiClockwise)
                                + toRotate.y * Math.cos(rotationAntiClockwise)));

        }
        return toRotate;
    }

    public static boolean isInRange(XY middle, XY upperLeftEnd, XY lowerRightEnd) {
        try {
            return middle.x <= lowerRightEnd.x && middle.x >= upperLeftEnd.x
                    && middle.y <= lowerRightEnd.y && middle.y >= upperLeftEnd.y;
        } catch (Exception e){
            System.out.println(middle.x);
            System.out.println(middle.y);
        }
        return false;
    }

    private static int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    public static XY normalizedVector(XY xy) {

        int newX, newY, oldX = xy.x, oldY = xy.y;
        if (oldX == 0) {
            if (oldY == 0)
                return XY.ZERO_ZERO;
            else if (oldY < 0) {
                return XY.UP;
            } else {
                return XY.DOWN;
            }
        } else if (oldY == 0) {
            if (oldX < 0) {
                return XY.LEFT;
            } else {
                return XY.RIGHT;
            }
        } else {
            if (oldX < 0)
                newX = Math.round(-1 * (Math.abs((float) oldX / (float) oldY)));
            else{
                newX = Math.round(Math.abs((float) oldX / (float) oldY));
            }

            if (oldY < 0)
                newY = Math.round(-1 * (Math.abs((float) oldY / (float) oldX)));
            else {
                newY = Math.round(Math.abs((float) oldY / (float) oldX));
            }
        }
        newX = normalizeNumber(newX);
        newY = normalizeNumber(newY);
        return new XY(newX, newY);
    }

    private static int normalizeNumber(int i) {
        if (i >= 1)
            return 1;
        else if (i <= -1)
            return -1;
        else
            return 0;
    }

    public static XY stringToXY(String s){
        String[] splitted = s.split(" ");
        return new XY(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[3]));
    }

    public static int distanceInSteps(XY start, XY destination){
        int distanceX = Math.abs(destination.x - start.x);
        int distanceY = Math.abs(destination.y - start.y);

        return distanceX > distanceY ? distanceX : distanceY;
    }
}
