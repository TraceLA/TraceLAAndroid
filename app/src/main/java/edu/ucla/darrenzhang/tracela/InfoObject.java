package edu.ucla.darrenzhang.tracela;

public class InfoObject {
    private boolean isInDatabase = false;

    public InfoObject(boolean isInDatabase) {
        this.isInDatabase = isInDatabase;
    }

    public boolean isInDatabase() {
        return isInDatabase;
    }

    public void setInDatabase(boolean inDatabase) {
        isInDatabase = inDatabase;
    }
}
