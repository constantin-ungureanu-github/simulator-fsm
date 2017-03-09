package simulator.network._4G.LTE;

import static simulator.network._4G.LTE.SGW.State.Off;
import static simulator.network._4G.LTE.SGW.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.abstracts.NE;

public class SGW extends NE {
    private static Logger log = LoggerFactory.getLogger(SGW.class);

    public enum State implements simulator.actors.interfaces.State {
        On, Off
    }

    {
        startWith(Off, null);

        when(Off, matchAnyEvent((event, state) -> processUnhandledEvent(event)));

        when(On, matchAnyEvent((event, state) -> processUnhandledEvent(event)));

        whenUnhandled(matchAnyEvent((event, state) -> processUnhandledEvent(event)));

        initialize();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processUnhandledEvent(Object event) {
        log.error("Unhandled event: {}", event);
        return stay();
    }
}
