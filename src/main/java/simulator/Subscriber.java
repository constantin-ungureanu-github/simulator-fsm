package simulator;

import static simulator.Subscriber.DeviceEvents.AddDevice;
import static simulator.Subscriber.DiscreteEvent.RemoveWork;
import static simulator.Subscriber.Events.GoToSleep;
import static simulator.Subscriber.Events.GoToWork;
import static simulator.Subscriber.Events.ReturnFromWork;
import static simulator.Subscriber.Events.WakeUp;
import static simulator.Subscriber.NetworkEvents.SendSMS;
import static simulator.Subscriber.State.Available;
import static simulator.Subscriber.State.Sleeping;
import static simulator.Subscriber.State.Working;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.Subscriber.State;
import simulator.network.Data;
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

        when(Available, matchEventEquals(SendSMS, (state, data) -> {
            ActorRef ue = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
            if (ue != null) {
                ue.tell(UE.Events.SendSMS, self());
                stay();
            }
            return stay();
        }));

        // TODO Add rest of events

        whenUnhandled(matchEvent(Master.Step.class, (step, data) -> {
            processStep(step.getStep());
            return stay();
        }).eventEquals(AddDevice, (state, data) -> {
            devices.add(sender());
            sender().tell(UE.Events.PickedBySubscriber, self());
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }).eventEquals(RemoveWork, (state, data) -> {
            workLoad.removeWork();
            if (workLoad.isWorkDone()) {
                Master.getMaster().tell(Master.Events.Ping, self());
            }
            return stay();
        }).anyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }

    private void processStep(long step) {
//        if (ThreadLocalRandom.current().nextInt(100) < 10) {
            workLoad.addWork();
            self().tell(SendSMS, self());
//        } else {
//            Master.getMaster().tell(Master.Events.Ping, self());
//        }
    }
}
