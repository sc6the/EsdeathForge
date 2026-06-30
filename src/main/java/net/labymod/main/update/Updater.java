package net.labymod.main.update;

/**
 * Phase 1 stub. The real Updater self-updated the LabyMod jar; under EsdeathForge that's
 * never wanted. LabyConnect flips these flags on server "UPDATE" addon messages; we ignore them.
 */
public class Updater {

    private boolean forceUpdate;
    private boolean backupMethod;

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public void setBackupMethod(boolean backupMethod) {
        this.backupMethod = backupMethod;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public boolean isBackupMethod() {
        return backupMethod;
    }
}
