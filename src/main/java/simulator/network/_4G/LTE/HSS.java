package simulator.network._4G.LTE;

import static simulator.network.NE.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.network.NE;
import simulator.network._4G.LTE.Interfaces.Gx;

public class HSS extends NE {
    private static Logger log = LoggerFactory.getLogger(HSS.class);

    {
        startWith(On, null);

        when(On, matchEvent(Gx.class, (event, data) -> (event == Gx.Event1), (state, data) -> {
            // TODO
            return stay();
        }).event(Gx.class, (event, data) -> (event == Gx.Event2), (event, data) -> {
            // TODO
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
