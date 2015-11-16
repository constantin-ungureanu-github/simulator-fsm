package simulator.actors;

import static simulator.actors.Subscriber.DeviceEvents.AddDevice;
import static simulator.actors.Subscriber.DiscreteEvent.RemoveWork;
import static simulator.actors.Subscriber.Events.GoToSleep;
import static simulator.actors.Subscriber.Events.GoToWork;
import static simulator.actors.Subscriber.Events.ReturnFromWork;
import static simulator.actors.Subscriber.Events.WakeUp;
import static simulator.actors.Subscriber.NetworkEvents.MakeVoiceCall;
import static simulator.actors.Subscriber.NetworkEvents.SendSMS;
import static simulator.actors.Subscriber.State.Available;
import static simulator.actors.Subscriber.State.Sleeping;
import static simulator.actors.Subscriber.State.Working;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.Subscriber.State;
import simulator.network.NE.Data;
import simulator.network.UE;
import simulator.utils.WorkLoad;
import akka.actor.AbstractFSM;
import akka.actor.ActorRef;

public class Subscriber extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(Subscriber.class);

    public enum State {
        Sleeping, Working, Available, Unavailable,
        Walking, Flying,
        InCall, InDataSession
    }

    public enum Events {
        WakeUp, GoToSleep, ReturnFromWork, GoToWork
    }

    public enum DeviceEvents {
        AddDevice, RemoveDevice
    }

    public enum DiscreteEvent {
        RemoveWork
    }

    public enum NetworkEvents {
        MakeVoiceCall, EndVoiceCall, SendSMS
    }

    private List<ActorRef> devices = new ArrayList<ActorRef>();
    private WorkLoad workLoad = new WorkLoad();

    {
        startWith(Available, null);

        when(Sleeping, matchEventEquals(WakeUp, (state, data) -> goTo(Available)));
        when(Available, matchEventEquals(GoToSleep, (state, data) -> goTo(Sleeping)));
        when(Available, matchEventEquals(GoToWork, (state, data) -> goTo(Working)));
        when(Working, matchEventEquals(ReturnFromWork, (state, data) -> goTo(Available)));

        when(Available, matchEvent((event, data) -> (event == SendSMS), (state, data) -> {
            if (devices.isEmpty())
                return removeWork();
            return sendSMS();
        }).event((event, data) -> (event == MakeVoiceCall), (state, data) -> {
            if (devices.isEmpty())
                return removeWork();
            return makeVoiceCall();
        }));

        // TODO Add rest of events
        whenUnhandled(matchEvent(Master.Step.class, (step, data) -> {
            return processStep();
        }).eventEquals(AddDevice, (state, data) -> {
            return addDevice();
        }).eventEquals(RemoveWork, (state, data) -> {
            return removeWork();
        }).anyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }

    private akka.actor.FSM.State<State, Data> makeVoiceCall() {
        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(UE.Events.MakeVoiceCall, self());
        }
        return stay();
    }

    private akka.actor.FSM.State<State, Data> sendSMS() {
        ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
        if (device != null) {
            device.tell(UE.Events.SendSMS, self());
        }
        return stay();
    }

    private akka.actor.FSM.State<State, Data> processStep() {
        workLoad.addWork();
        self().tell(SendSMS, self());
        return stay();
    }

    private akka.actor.FSM.State<State, Data> addDevice() {
        devices.add(sender());
        sender().tell(UE.Events.PickedBySubscriber, self());
        Master.getMaster().tell(Master.Events.Ping, self());
        return stay();
    }

    private akka.actor.FSM.State<State, Data> removeWork() {
        workLoad.removeWork();
        if (workLoad.isWorkDone()) {
            Master.getMaster().tell(Master.Events.Ping, self());
        }
        return stay();
    }
}