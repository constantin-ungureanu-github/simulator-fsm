package simulator.network._4G.LTE;

import static simulator.network._4G.LTE.PCRF.State.Available;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.network.Data;
import simulator.network._4G.LTE.PCRF.State;
import simulator.network._4G.LTE.Interfaces.Gx;
import akka.actor.AbstractFSM;

public class PCRF extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(PGW.class);

    public enum State {
        Idle,
        Available,
        Down
    }

    {
        startWith(Available, null);

        when(Available, matchEvent(Gx.class, (event, data) -> (event == Gx.Event1), (state, data) -> {
            // TODO
            return stay();
        }).event(Gx.class, (event, data) -> (event == Gx.Event2), (event, data) -> {
            // TODO
            return stay();
        }));

        whenUnhandled(matchAnyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }
}
