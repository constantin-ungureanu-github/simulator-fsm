package simulator.utils;

public class WorkLoad {
    private long workLoad;

    public WorkLoad() {
    }

    public boolean isWorkDone() {
        return workLoad == 0;
    }

    public void addWork(long workLoad) {
        this.workLoad += workLoad;
    }

    public void addWork() {
        workLoad++;
    }

    public void removeWork() {
        workLoad--;
    }

    public void removeWork(long workLoad) {
        workLoad -= workLoad;
    }
}
