package simulator.actors;

import static simulator.actors.Subscriber.State.Available;
import static simulator.actors.Subscriber.State.Sleeping;
import static simulator.actors.Subscriber.State.Working;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import simulator.actors.abstracts.Actor;
import simulator.actors.events.DeviceEvents.AddDevice;
import simulator.actors.events.DeviceEvents.MakeVoiceCall;
import simulator.actors.events.DeviceEvents.PickedBySubscriber;
import simulator.actors.events.DeviceEvents.RemoveDevice;
import simulator.actors.events.DeviceEvents.RequestDataSession;
import simulator.actors.events.DeviceEvents.SendSMS;
import simulator.actors.events.DiscreteEvent;
import simulator.actors.events.SubscriberEvents.GoToSleep;
import simulator.actors.events.SubscriberEvents.GoToWork;
import simulator.actors.events.SubscriberEvents.ReturnFromWork;
import simulator.actors.events.SubscriberEvents.WakeUp;

public class Subscriber extends Actor {
    private static Logger log = LoggerFactory.getLogger(Subscriber.class);

    public enum State implements simulator.actors.interfaces.State {
        Sleeping, Working, Available, Unavailable, Walking, Flying
    }

    private List<ActorRef> devices = new ArrayList<ActorRef>();

    {
        startWith(Available, null);
        scheduleEvent((long) ThreadLocalRandom.current().nextInt(20, 30), new GoToSleep());

        when(Sleeping, matchEvent(WakeUp.class, (state, data) -> processWakeUp()));
        when(Available, matchEvent(GoToSleep.class, (state, data) -> processGoToSleep()));
        when(Available, matchEvent(GoToWork.class, (state, data) -> processGoToWork()));
        when(Working, matchEvent(ReturnFromWork.class, (state, data) -> processReturnFromWork()));

        when(Available,
                matchEvent(SendSMS.class, (event, data) -> sendSMS())
                .event(MakeVoiceCall.class, (event, data) -> makeVoiceCall())
                .event(RequestDataSession.class, (event, data) -> requestDataSession()));

        when(Working,
                matchEvent(SendSMS.class, (event, data) -> sendSMS())
                .event(MakeVoiceCall.class, (event, data) -> makeVoiceCall())
                .event(RequestDataSession.class, (event, data) -> requestDataSession()));

        when(Sleeping,
                matchEvent(SendSMS.class, (event, data) -> sendSMS())
                .event(MakeVoiceCall.class, (event, data) -> makeVoiceCall())
                .event(RequestDataSession.class, (event, data) -> requestDataSession()));

        whenUnhandled(
                matchEvent(Master.Step.class, (step, data) -> processStep(step.getStep()))
                .event(AddDevice.class, (state, data) -> addDevice())
                .event(RemoveDevice.class, (state, data) -> removeDevice())
                .event(DiscreteEvent.RemoveWork.class, (event, data) -> removeWork())
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
        self().tell(new SendSMS(), ActorRef.noSender());
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> addDevice() {
        devices.add(sender());
        sender().tell(new PickedBySubscriber(), self());
        Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> removeDevice() {
        devices.remove(sender());
        sender().tell(new PickedBySubscriber(), self());
        Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processWakeUp() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(50, 60), new GoToSleep());
        log.info("{} woke up.", self().path().name());
        removeWork();
        return goTo(Available);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processGoToSleep() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(20, 30), new WakeUp());
        log.info("{} went to sleep.", self().path().name());
        removeWork();
        return goTo(Sleeping);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processGoToWork() {
        removeWork();
        return goTo(Working);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processReturnFromWork() {
        removeWork();
        return goTo(Available);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> sendSMS() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        device.tell(new SendSMS(), self());

        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> makeVoiceCall() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(new MakeVoiceCall(), self());
        }
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> requestDataSession() {
        if (devices.isEmpty())
            return removeWork();

        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(new RequestDataSession(), self());
        }
        return stay();
    }
}
