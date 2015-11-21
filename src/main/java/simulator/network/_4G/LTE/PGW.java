package simulator.network._4G.LTE;

import static simulator.actors.abstracts.NE.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.abstracts.NE;
import simulator.network._4G.LTE.Interfaces.Gx;

public class PGW extends NE {
    private static Logger log = LoggerFactory.getLogger(PGW.class);

    {
        startWith(On, null);

        when(On, matchEvent(Gx.class, (event, data) -> (event == Gx.Event1), (state, data) -> {
            return stay();
        }).event(Gx.class, (event, data) -> (event == Gx.Event2), (event, data) -> {
            return stay();
        }).event(Gx.class, (event, state) -> {
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
