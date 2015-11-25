package simulator.actors;

import static simulator.actors.Subscriber.State.Available;
import static simulator.actors.Subscriber.State.Sleeping;
import static simulator.actors.Subscriber.State.Walking;
import static simulator.actors.Subscriber.State.Working;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.abstracts.Actor;
import simulator.actors.events.DeviceEvents.AddDevice;
import simulator.actors.events.DeviceEvents.MakeVoiceCall;
import simulator.actors.events.DeviceEvents.PickedBySubscriber;
import simulator.actors.events.DeviceEvents.RemoveDevice;
import simulator.actors.events.DeviceEvents.RequestDataSession;
import simulator.actors.events.DeviceEvents.SendSMS;
import simulator.actors.events.DiscreteEvent;
import simulator.actors.events.SubscriberEvents.ArriveHome;
import simulator.actors.events.SubscriberEvents.ArriveToWork;
import simulator.actors.events.SubscriberEvents.GoToSleep;
import simulator.actors.events.SubscriberEvents.GoToWork;
import simulator.actors.events.SubscriberEvents.Move;
import simulator.actors.events.SubscriberEvents.ReturnFromWork;
import simulator.actors.events.SubscriberEvents.WakeUp;
import akka.actor.ActorRef;

public class Subscriber extends Actor {
    private static Logger log = LoggerFactory.getLogger(Subscriber.class);

    public enum State implements simulator.actors.interfaces.State {
        Available, Working, Sleeping, Walking
    }

    private List<ActorRef> devices = new ArrayList<ActorRef>();
    private double latitude, longitude;

    {
        startWith(Available, null);
        scheduleEvent((long) ThreadLocalRandom.current().nextInt(20, 30), new GoToSleep());

        when(Available, matchEvent(GoToSleep.class, (state, data) -> processGoToSleep()));
        when(Sleeping, matchEvent(WakeUp.class, (state, data) -> processWakeUp()));
        when(Available, matchEvent(GoToWork.class, (state, data) -> processGoToWork()));
        when(Walking, matchEvent(Move.class, (state, data) -> processMoving()));
        when(Walking, matchEvent(ArriveToWork.class, (state, data) -> processArriveToWork()));
        when(Working, matchEvent(ReturnFromWork.class, (state, data) -> processReturnFromWork()));
        when(Walking, matchEvent(Move.class, (state, data) -> processMoving()));
        when(Walking, matchEvent(ArriveHome.class, (state, data) -> processArriveHome()));

        when(Available,
                matchEvent(SendSMS.class, (event, data) -> sendSMS())
                .event(MakeVoiceCall.class, (event, data) -> makeVoiceCall())
                .event(RequestDataSession.class, (event, data) -> requestDataSession()));

        when(Working,
                matchEvent(SendSMS.class, (event, data) -> sendSMS())
                .event(MakeVoiceCall.class, (event, data) -> makeVoiceCall())
                .event(RequestDataSession.class, (event, data) -> requestDataSession()));

        when(Sleeping,
                matchEvent(SendSMS.class, (event, data) -> rejectWork())
                .event(MakeVoiceCall.class, (event, data) -> rejectWork())
                .event(RequestDataSession.class, (event, data) -> rejectWork()));

        when(Walking,
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
        log.info("Step {} - {} woke up.", getStep(), self().path().name());

        removeWork();
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(5, 10), new GoToWork());
        return goTo(Available);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processGoToSleep() {
        log.info("Step {} - {} went to sleep.", getStep(), self().path().name());

        removeWork();

        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(20, 30), new WakeUp());

        return goTo(Sleeping);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processReturnFromWork() {
        log.info("Step {} - {} is preparing to go to home.", getStep(), self().path().name());

        removeWork();

        for (int i = 0; i < 5; i++) {
            scheduleEvent(getStep() + i, new Move());
        }
        scheduleEvent(getStep() + 5, new ArriveHome());

        return goTo(Walking);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processGoToWork() {
        log.info("Step {} - {} is preparing to go to work.", getStep(), self().path().name());

        removeWork();

        for (int i = 0; i < 5; i++) {
            scheduleEvent(getStep() + i, new Move());
        }
        scheduleEvent(getStep() + 5, new ArriveToWork());

        return goTo(Walking);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processArriveToWork() {
        log.info("Step {} - {} arrived at work.", getStep(), self().path().name());

        removeWork();

        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(20, 30), new ReturnFromWork());

        return goTo(Working);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processArriveHome() {
        log.info("Step {} - {} arrived home.", getStep(), self().path().name());

        removeWork();

        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(10, 20), new GoToSleep());

        return goTo(Available);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processMoving() {
        log.info("Step {} - {} is walking.", getStep(), self().path().name());

        removeWork();

        move();

        return stay();
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

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> rejectWork() {
        return removeWork();
    }

    private void move() {
        setLatitude(getLatitude() + ThreadLocalRandom.current().nextDouble(0.01));
        setLongitude(getLongitude() + ThreadLocalRandom.current().nextDouble(0.01));
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
