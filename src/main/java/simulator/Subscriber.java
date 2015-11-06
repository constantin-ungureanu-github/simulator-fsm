package simulator;

import static simulator.Subscriber.Events.AddDevice;
import static simulator.Subscriber.Events.EndVoiceCall;
import static simulator.Subscriber.Events.GoToSleep;
import static simulator.Subscriber.Events.GoToWork;
import static simulator.Subscriber.Events.MakeVoiceCall;
import static simulator.Subscriber.Events.ReturnFromWork;
import static simulator.Subscriber.Events.WakeUp;
import static simulator.Subscriber.State.Available;
import static simulator.Subscriber.State.Flying;
import static simulator.Subscriber.State.InCall;
import static simulator.Subscriber.State.Sleeping;
import static simulator.Subscriber.State.Unavailable;
import static simulator.Subscriber.State.Walking;
import static simulator.Subscriber.State.Working;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.Subscriber.Data;
import simulator.Subscriber.State;
import simulator.network.Device;
import simulator.utils.WorkLoad;

public class Subscriber extends AbstractFSM<State, Data> {
    public enum State {
        Sleeping,
        Working,
        Available,
        Unavailable,

        Walking,
        Flying,

        InCall,
        InDataSession
    }

    public enum Events {
        WakeUp,
        GoToSleep,
        ReturnFromWork,
        GoToWork,
        AddDevice,
        RemoveDevice,

        MakeVoiceCall,
        EndVoiceCall,
        SendSMS,

        RemoveWork
    }

    public static class Data {
    }

    private List<ActorRef> devices = new ArrayList<ActorRef>();
//    private Map<Long, Set<Events>> workMap = new HashMap<>();
    private WorkLoad workLoad = new WorkLoad();
//    private double latitude, longitude;

    {
        startWith(Sleeping, null);

        when(Sleeping, matchEventEquals(WakeUp, (state, data) -> {
            return goTo(Available);
        }));

        when(Available, matchEventEquals(GoToSleep, (state, data) -> {
            return goTo(Sleeping);
        }));

        when(Available, matchEventEquals(GoToWork, (state, data) -> {
            return goTo(Working);
        }));

        when(Working, matchEventEquals(ReturnFromWork, (state, data) -> {
            return goTo(Available);
        }));

        // Refresh step
        when(Sleeping, matchEvent(Master.Step.class, (step, data) -> {
            processStep(step.getStep());
            return stay();
        }));

        when(Working, matchEvent(Master.Step.class, (step, data) -> {
            processStep(step.getStep());
            return stay();
        }));

        when(Available, matchEvent(Master.Step.class, (step, data) -> {
            processStep(step.getStep());
            return stay();
        }));

        when(Unavailable, matchEvent(Master.Step.class, (step, data) -> {
            processStep(step.getStep());
            return stay();
        }));

        when(InCall, matchEvent(Master.Step.class, (step, data) -> {
            processStep(step.getStep());
            return stay();
        }));

        when(Walking, matchEvent(Master.Step.class, (step, data) -> {
            move();
            processStep(step.getStep());
            return stay();
        }));

        when(Flying, matchEvent(Master.Step.class, (step, data) -> {
            fly();
            processStep(step.getStep());
            return stay();
        }));

        // Receive Device
        when(Sleeping, matchEventEquals(AddDevice, (state, data) -> {
            devices.add(sender());
            sender().tell(Device.Events.PickedBySubscriber, self());
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Working, matchEventEquals(AddDevice, (state, data) -> {
            devices.add(sender());
            sender().tell(Device.Events.PickedBySubscriber, self());
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(AddDevice, (state, data) -> {
            devices.add(sender());
            sender().tell(Device.Events.PickedBySubscriber, self());
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        // Calls
        when(Available, matchEventEquals(MakeVoiceCall, (state, data) -> {
            return goTo(InCall);
        }));

        when(Available, matchEventEquals(Events.SendSMS, (state, data) -> {
            return stay();
        }));

        when(InCall, matchEventEquals(EndVoiceCall, (state, data) -> {
            return goTo(Available);
        }));

        // Remove work
        when(Sleeping, matchEventEquals(Events.RemoveWork, (state, data) -> {
            workLoad.removeWork();
            if (workLoad.isWorkDone()) {
                Master.getMaster().tell(Master.Events.Ping, self());
            }
            return stay();
        }));

        when(Working, matchEventEquals(AddDevice, (state, data) -> {
            devices.add(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(AddDevice, (state, data) -> {
            devices.add(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(MakeVoiceCall, (state, data) -> {
            ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
            if (device != null) {
                device.tell(Device.Events.MakeVoiceCall, self());
//                return goTo(InCall);
            }
            return stay();
        }));

        when(Available, matchEventEquals(Events.SendSMS, (state, data) -> {
            return stay();
        }));

        when(InCall, matchEventEquals(EndVoiceCall, (state, data) -> {
            return goTo(Available);
        }));

        when(Sleeping, matchEventEquals(Device.Events.ReceiveVoiceCall, (state, data) -> {
//            sender().tell(Device.Events.AckMakeVoiceCall, self());
//            return goTo(InCall);
            return stay();
        }));

        when(Sleeping, matchEventEquals(MakeVoiceCall, (state, data) -> {
            ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
            if (device != null) {
                device.tell(Device.Events.MakeVoiceCall, self());
//                return goTo(InCall);
            } else {
                // cry
            }
            return stay();
        }));

        initialize();
    }

    private void processStep(long step) {
//        Set<Events> events = workMap.get(step);
//        if (events != null) {
//            workLoad.addWork(events.size());
//            for (Events event : events) {
//                self().tell(event, self());
//            }
//            events.remove(step);
//        }

        if (ThreadLocalRandom.current().nextInt(100) < 10) {
            workLoad.addWork();
            self().tell(MakeVoiceCall, self());
        } else {
            Master.getMaster().tell(Master.Events.Ping, self());
        }
    }

    private void move() {
        // TODO
    }

    private void fly() {
        // TODO
    }
}
