package simulator.actors.abstracts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Master;
import simulator.actors.Master.Ping;
import simulator.actors.interfaces.Events;
import simulator.utils.WorkLoad;

public abstract class Actor extends AbstractFSM<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> {
    private Map<Long, Set<Events>> workMap = new HashMap<>();
    protected WorkLoad workLoad = new WorkLoad();
    private Long step;

    protected akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processStep(Long step) {
        setStep(step);
        scheduleCurrentWork();

        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(new Ping(), ActorRef.noSender());
        }

        return stay();
    }

    protected akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> removeWork() {
        workLoad.removeWork();
        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(new Ping(), ActorRef.noSender());
        }

        return stay();
    }

    protected void addWork() {
        workLoad.addWork();
    }

    protected Set<Events> getWorkMapEvents(Long step) {
        Set<Events> events;
        if (workMap.containsKey(step)) {
            events = workMap.get(step);
        } else {
            events = new HashSet<>();
            workMap.put(step, events);
        }
        return events;
    }

    protected void scheduleCurrentWork() {
        if (workMap.containsKey(step)) {
            for (Events event : workMap.get(step)) {
                workLoad.addWork();
                self().tell(event, ActorRef.noSender());
            }
            workMap.remove(step);
        }
    }

    protected void scheduleEvent(Long step, Events event) {
        Set<Events> events = getWorkMapEvents(step);

        events.add(event);
    }

    protected Long getStep() {
        return step;
    }

    protected void setStep(Long step) {
        this.step = step;
    }
}
