package simulator.actors;

import static simulator.actors.Subscriber.DeviceEvents.AddDevice;
import static simulator.actors.Subscriber.DeviceEvents.RemoveDevice;
import static simulator.actors.Subscriber.DiscreteEvent.RemoveWork;
import static simulator.actors.Subscriber.NetworkEvents.MakeVoiceCall;
import static simulator.actors.Subscriber.NetworkEvents.RequestDataSession;
import static simulator.actors.Subscriber.NetworkEvents.SendSMS;
import static simulator.actors.Subscriber.State.Available;
import static simulator.actors.Subscriber.State.Sleeping;
import static simulator.actors.Subscriber.State.Working;
import static simulator.actors.Subscriber.SubscriberEvents.GoToSleep;
import static simulator.actors.Subscriber.SubscriberEvents.GoToWork;
import static simulator.actors.Subscriber.SubscriberEvents.ReturnFromWork;
import static simulator.actors.Subscriber.SubscriberEvents.WakeUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Subscriber.State;
import simulator.network.NE.Data;
import simulator.network.UE;
import simulator.utils.WorkLoad;

public class Subscriber extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(Subscriber.class);

    public enum State {
        Sleeping, Working, Available, Unavailable, Walking, Flying
    }

    interface Events {}

    public enum SubscriberEvents implements Events {
        WakeUp, GoToSleep, ReturnFromWork, GoToWork
    }

    public enum DeviceEvents implements Events {
        AddDevice, RemoveDevice
    }

    public enum DiscreteEvent implements Events {
        RemoveWork
    }

    public enum NetworkEvents implements Events {
        SendSMS, MakeVoiceCall, RequestDataSession
    }

    private List<ActorRef> devices = new ArrayList<ActorRef>();
    private Map<Long, Set<Events>> workMap = new HashMap<>();
    private WorkLoad workLoad = new WorkLoad();
    private Long step;

    {
        startWith(Available, null);
        scheduleEvent((long) ThreadLocalRandom.current().nextInt(20, 30), GoToSleep);

        when(Sleeping, matchEventEquals(WakeUp, (state, data) -> processWakeUp()));
        when(Available, matchEventEquals(GoToSleep, (state, data) -> processGoToSleep()));
        when(Available, matchEventEquals(GoToWork, (state, data) -> processGoToWork()));
        when(Working, matchEventEquals(ReturnFromWork, (state, data) -> processReturnFromWork()));

        when(Available,
                matchEvent(NetworkEvents.class, (event, data) -> (event == SendSMS), (state, data) -> sendSMS())
                .event(NetworkEvents.class, (event, data) -> (event == MakeVoiceCall), (state, data) -> makeVoiceCall())
                .event(NetworkEvents.class, (event, data) -> (event == RequestDataSession), (state, data) -> requestDataSession()));

        when(Working,
                matchEvent(NetworkEvents.class, (event, data) -> (event == SendSMS), (state, data) -> sendSMS())
                .event(NetworkEvents.class, (event, data) -> (event == MakeVoiceCall), (state, data) -> makeVoiceCall())
                .event(NetworkEvents.class, (event, data) -> (event == RequestDataSession), (state, data) -> requestDataSession()));

        when(Sleeping,
                matchEvent(NetworkEvents.class, (event, data) -> (event == SendSMS), (state, data) -> sendSMS())
                .event(NetworkEvents.class, (event, data) -> (event == MakeVoiceCall), (state, data) -> makeVoiceCall())
                .event(NetworkEvents.class, (event, data) -> (event == RequestDataSession), (state, data) -> requestDataSession()));

        whenUnhandled(
                matchEvent(Master.Step.class, (step, data) -> addWork(step.getStep()))
                .eventEquals(AddDevice, (state, data) -> addDevice())
                .eventEquals(RemoveDevice, (state, data) -> removeDevice())
                .eventEquals(RemoveWork, (state, data) -> removeWork())
                .anyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }

    private akka.actor.FSM.State<State, Data> addWork(Long step) {
        setStep(step);

        scheduleCurrentWork();
        return stay();
    }

    private void scheduleCurrentWork() {
        if (workMap.containsKey(step)) {
            for (Events event : workMap.get(step)) {
                workLoad.addWork();
                self().tell(event, ActorRef.noSender());
            }
            workMap.remove(step);
        };

        workLoad.addWork();
        self().tell(SendSMS, ActorRef.noSender());
    }

    private akka.actor.FSM.State<State, Data> removeWork() {
        workLoad.removeWork();
        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        }
        return stay();
    }

    private akka.actor.FSM.State<State, Data> processWakeUp() {
        scheduleEvent(step + ThreadLocalRandom.current().nextInt(50, 60), GoToSleep);
        log.info("{} woke up.", self().path().name());
        removeWork();
        return goTo(Available);
    }

    private akka.actor.FSM.State<State, Data> processGoToSleep() {
        scheduleEvent(step + ThreadLocalRandom.current().nextInt(20, 30), WakeUp);
        log.info("{} went to sleep.", self().path().name());
        removeWork();
        return goTo(Sleeping);
    }

    private akka.actor.FSM.State<State, Data> processGoToWork() {
        removeWork();
        return goTo(Working);
    }

    private akka.actor.FSM.State<State, Data> processReturnFromWork() {
        removeWork();
        return goTo(Available);
    }

    private akka.actor.FSM.State<State, Data> sendSMS() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        device.tell(UE.Events.SendSMS, self());

        return stay();
    }

    private akka.actor.FSM.State<State, Data> makeVoiceCall() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(UE.Events.MakeVoiceCall, self());
        }
        return stay();
    }

    private akka.actor.FSM.State<State, Data> requestDataSession() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(UE.Events.RequestDataSession, self());
        }
        return stay();
    }

    private akka.actor.FSM.State<State, Data> addDevice() {
        devices.add(sender());
        sender().tell(UE.Events.PickedBySubscriber, self());
        Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<State, Data> removeDevice() {
        devices.remove(sender());
        sender().tell(UE.Events.PickedBySubscriber, self());
        Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        return stay();
    }

    private void setStep(Long step) {
        this.step = step;
    }

    private void scheduleEvent(Long step, Events event) {
        Set<Events> events = getWorkMapEvents(step);

        events.add(event);
    }

    private Set<Events> getWorkMapEvents(Long step) {
        Set<Events> events;
        if (workMap.containsKey(step)) {
            events = workMap.get(step);
        } else {
            events = new HashSet<>();
            workMap.put(step, events);
        }
        return events;
    }
}
