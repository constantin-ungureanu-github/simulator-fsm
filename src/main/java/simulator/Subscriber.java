package simulator;

import static simulator.Subscriber.Events.AddDevice;
import static simulator.Subscriber.Events.EndVoiceCall;
import static simulator.Subscriber.Events.GoToSleep;
import static simulator.Subscriber.Events.GoToWork;
import static simulator.Subscriber.Events.MakeVoiceCall;
import static simulator.Subscriber.Events.RemoveWork;
import static simulator.Subscriber.Events.ReturnFromWork;
import static simulator.Subscriber.Events.SendSMS;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.Subscriber.Data;
import simulator.Subscriber.State;
import simulator.network.Device;
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
        WakeUp, GoToSleep, ReturnFromWork, GoToWork, AddDevice, RemoveDevice,

        MakeVoiceCall, EndVoiceCall, SendSMS,

        RemoveWork
    }

    public enum NetworkEvents {
        MakeVoiceCall, EndVoiceCall, SendSMS
    }


    interface Data {
    }

    final class Todo implements Data {
        private final ActorRef target;
        private final List<Object> queue;

        public Todo(ActorRef target, List<Object> queue) {
            this.target = target;
            this.queue = queue;
        }

        public ActorRef getTarget() {
            return target;
        }

        public List<Object> getQueue() {
            return queue;
        }
    }

    private List<ActorRef> devices = new ArrayList<ActorRef>();
    private WorkLoad workLoad = new WorkLoad();

    {
        startWith(Sleeping, null);

        when(Sleeping, matchEventEquals(WakeUp, (state, data) -> goTo(Available)));
        when(Available, matchEventEquals(GoToSleep, (state, data) -> goTo(Sleeping)));
        when(Available, matchEventEquals(GoToWork, (state, data) -> goTo(Working)));
        when(Working, matchEventEquals(ReturnFromWork, (state, data) -> goTo(Available)));

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
            processStep(step.getStep());
            return stay();
        }));

        when(Flying, matchEvent(Master.Step.class, (step, data) -> {
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
        when(Sleeping, matchEventEquals(RemoveWork, (state, data) -> {
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
            }
            return stay();
        }));

        when(Available, matchEventEquals(SendSMS, (state, data) -> {
            return stay();
        }));

        when(InCall, matchEventEquals(EndVoiceCall, (state, data) -> {
            return goTo(Available);
        }));

        when(Sleeping, matchEventEquals(Device.Events.ReceiveVoiceCall, (state, data) -> {
            return stay();
        }));

        when(Sleeping, matchEventEquals(MakeVoiceCall, (state, data) -> {
            ActorRef device = devices.get(ThreadLocalRandom.current().nextInt(devices.size()));
            if (device != null) {
                device.tell(Device.Events.MakeVoiceCall, self());
            } else {
            }
            return stay();
        }));

        whenUnhandled(matchAnyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        onTransition(matchState(Sleeping, Available, () -> {
        }).state(Sleeping, Available, () -> {
        }));

        onTermination(matchStop(Normal(), (state, data) -> {
        }).stop(Shutdown(), (state, data) -> {
        }).stop(Failure.class, (reason, state, data) -> {
        }));

        initialize();
    }

    private void processStep(long step) {
        if (ThreadLocalRandom.current().nextInt(100) < 90) {
            workLoad.addWork();
            self().tell(MakeVoiceCall, self());
        } else {
            Master.getMaster().tell(Master.Events.Ping, self());
        }
    }
}
