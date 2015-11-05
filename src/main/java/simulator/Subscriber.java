package simulator;

import static simulator.Subscriber.Events.GoToSleep;
import static simulator.Subscriber.Events.GoToWork;
import static simulator.Subscriber.Events.ReturnFromWork;
import static simulator.Subscriber.Events.WakeUp;
import static simulator.Subscriber.State.Available;
import static simulator.Subscriber.State.Sleeping;
import static simulator.Subscriber.State.Working;
import simulator.Subscriber.Data;
import simulator.Subscriber.State;
import akka.actor.AbstractFSM;

public class Subscriber extends AbstractFSM<State, Data> {
    public enum State {
        Sleeping,
        Working,
        Available
    }

    public enum Events {
        WakeUp,
        GoToSleep,
        ReturnFromWork,
        GoToWork
    }

    public static class Data {
    }

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

        initialize();
    }
}
