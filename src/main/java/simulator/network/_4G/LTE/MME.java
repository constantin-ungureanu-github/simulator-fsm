package simulator.network._4G.LTE;

import static simulator.actors.abstracts.NE.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.abstracts.NE;
import simulator.network._4G.LTE.Interfaces.S11;

public class MME extends NE {
    private static Logger log = LoggerFactory.getLogger(MME.class);

    {
        startWith(On, null);

        when(On, matchEvent(S11.class, (event, data) -> (event == S11.Event1), (state, data) -> {
            return stay();
        }).event(S11.class, (event, data) -> (event == S11.Event2), (event, data) -> {
            return stay();
        }).event(S11.class, (event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        whenUnhandled(matchAnyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }
}
