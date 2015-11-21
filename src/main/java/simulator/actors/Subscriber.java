package simulator.actors;

import static simulator.actors.Subscriber.State.Available;
import static simulator.actors.Subscriber.State.Sleeping;
import static simulator.actors.Subscriber.State.Working;
import static simulator.actors.events.DeviceEvents.AddDevice;
import static simulator.actors.events.DeviceEvents.PickedBySubscriber;
import static simulator.actors.events.DeviceEvents.RemoveDevice;
import static simulator.actors.events.DiscreteEvent.RemoveWork;
import static simulator.actors.events.NetworkEvents.MakeVoiceCall;
import static simulator.actors.events.NetworkEvents.RequestDataSession;
import static simulator.actors.events.NetworkEvents.SendSMS;
import static simulator.actors.events.SubscriberEvents.GoToSleep;
import static simulator.actors.events.SubscriberEvents.GoToWork;
import static simulator.actors.events.SubscriberEvents.ReturnFromWork;
import static simulator.actors.events.SubscriberEvents.WakeUp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import simulator.actors.Subscriber.Data;
import simulator.actors.Subscriber.State;
import simulator.actors.abstracts.Actor;
import simulator.actors.events.DeviceEvents;
import simulator.actors.events.NetworkEvents;
import simulator.actors.interfaces.TemplateData;
import simulator.actors.interfaces.TemplateState;

public class Subscriber extends Actor<State, Data> {
    private static Logger log = LoggerFactory.getLogger(Subscriber.class);

    public enum State implements simulator.actors.interfaces.TemplateState {
        Sleeping, Working, Available, Unavailable, Walking, Flying
    }

    public class Data implements simulator.actors.interfaces.TemplateData {
    }

    private List<ActorRef> devices = new ArrayList<ActorRef>();

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
                matchEvent(Master.Step.class, (step, data) -> processStep(step.getStep()))
                .eventEquals(AddDevice, (state, data) -> addDevice())
                .eventEquals(RemoveDevice, (state, data) -> removeDevice())
                .eventEquals(RemoveWork, (state, data) -> removeWork())
                .anyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }

    @Override
    protected void scheduleCurrentWork() {
        super.scheduleCurrentWork();

        addWork();
        self().tell(SendSMS, ActorRef.noSender());
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> addDevice() {
        devices.add(sender());
        sender().tell(PickedBySubscriber, self());
        Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> removeDevice() {
        devices.remove(sender());
        sender().tell(PickedBySubscriber, self());
        Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> processWakeUp() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(50, 60), GoToSleep);
        log.info("{} woke up.", self().path().name());
        removeWork();
        return goTo(Available);
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> processGoToSleep() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(20, 30), WakeUp);
        log.info("{} went to sleep.", self().path().name());
        removeWork();
        return goTo(Sleeping);
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> processGoToWork() {
        removeWork();
        return goTo(Working);
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> processReturnFromWork() {
        removeWork();
        return goTo(Available);
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> sendSMS() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        device.tell(DeviceEvents.SendSMS, self());

        return stay();
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> makeVoiceCall() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(DeviceEvents.MakeVoiceCall, self());
        }
        return stay();
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> requestDataSession() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(DeviceEvents.RequestDataSession, self());
        }
        return stay();
    }
}
