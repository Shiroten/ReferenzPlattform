package ExCells26.Squirrel.Mini;

public enum MiniType {
    FERAL(100), RECON(100), REAPER(200), NONE(0);
    private int energy;

    MiniType(int energy) {
        this.energy = energy;
    }

    //Recon spawn when: fieldLimit null and no Recon
    //Reaper spawn when: free Cell or grid4 not fully expanded
    //Bomb spawn when: else
}