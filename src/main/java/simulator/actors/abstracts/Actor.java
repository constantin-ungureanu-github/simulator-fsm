package simulator.actors.abstracts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Master;
import simulator.actors.interfaces.DataInterface;
import simulator.actors.interfaces.EventInterface;
import simulator.actors.interfaces.StateInterface;
import simulator.utils.WorkLoad;

public abstract class Actor<State, Data> extends AbstractFSM<StateInterface, DataInterface> {
    private Map<Long, Set<EventInterface>> workMap = new HashMap<>();
    private WorkLoad workLoad = new WorkLoad();
    private Long step;

    protected akka.actor.FSM.State<StateInterface, DataInterface> processStep(Long step) {
        setStep(step);
        scheduleCurrentWork();

        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        }

        return stay();
    }

    protected akka.actor.FSM.State<StateInterface, DataInterface> removeWork() {
        workLoad.removeWork();
        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        }
        return stay();
    }

    protected void addWork() {
        workLoad.addWork();
    }

    protected Set<EventInterface> getWorkMapEvents(Long step) {
        Set<EventInterface> events;
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
            for (EventInterface event : workMap.get(step)) {
                workLoad.addWork();
                self().tell(event, ActorRef.noSender());
            }
            workMap.remove(step);
        }
    }

    protected void scheduleEvent(Long step, EventInterface event) {
        Set<EventInterface> events = getWorkMapEvents(step);

        events.add(event);
    }


    protected Long getStep() {
        return step;
    }

    protected void setStep(Long step) {
        this.step = step;
    }
}
