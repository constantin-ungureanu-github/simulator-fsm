package simulator.network._4G.LTE;

import static simulator.network._4G.LTE.MME.State.Available;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.network.Data;
import simulator.network._4G.LTE.MME.State;
import simulator.network._4G.LTE.Interfaces.S11;
import akka.actor.AbstractFSM;

public class MME extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(MME.class);

    public enum State {
        Idle,
        Available,
        Down
    }

    {
        startWith(Available, null);

        when(Available, matchEvent(S11.class, (event, data) -> (event == S11.Event1), (state, data) -> {
            // TODO
            return stay();
        }).event(S11.class, (event, data) -> (event == S11.Event2), (event, data) -> {
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
