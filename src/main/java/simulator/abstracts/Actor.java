package simulator.abstracts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Master;
import simulator.utils.WorkLoad;

public abstract class Actor extends AbstractFSM<TemplateState, TemplateData> {
    private Map<Long, Set<TemplateEvents>> workMap = new HashMap<>();
    private WorkLoad workLoad = new WorkLoad();
    private Long step;

    protected akka.actor.FSM.State<TemplateState, TemplateData> processStep(Long step) {
        setStep(step);
        scheduleCurrentWork();

        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        }

        return stay();
    }

    protected void addWork() {
        workLoad.addWork();
    }

    protected akka.actor.FSM.State<TemplateState, TemplateData> removeWork() {
        workLoad.removeWork();
        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        }
        return stay();
    }

    protected void scheduleCurrentWork() {
        if (workMap.containsKey(step)) {
            for (TemplateEvents event : workMap.get(step)) {
                workLoad.addWork();
                self().tell(event, ActorRef.noSender());
            }
            workMap.remove(step);
        }
    }

    protected void scheduleEvent(Long step, TemplateEvents event) {
        Set<TemplateEvents> events = getWorkMapEvents(step);

        events.add(event);
    }

    protected Set<TemplateEvents> getWorkMapEvents(Long step) {
        Set<TemplateEvents> events;
        if (workMap.containsKey(step)) {
            events = workMap.get(step);
        } else {
            events = new HashSet<>();
            workMap.put(step, events);
        }
        return events;
    }

    protected Long getStep() {
        return step;
    }

    protected void setStep(Long step) {
        this.step = step;
    }
}
