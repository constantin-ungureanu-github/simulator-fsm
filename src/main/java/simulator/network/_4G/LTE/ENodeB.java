package simulator.network._4G.LTE;

import static simulator.abstracts.Cell.Events.ConnectCellAck;
import static simulator.abstracts.Cell.Events.ConnectDevice;
import static simulator.abstracts.Cell.Events.ConnectToNetwork;
import static simulator.abstracts.Cell.Events.DisconnectDevice;
import static simulator.abstracts.Cell.State.Off;
import static simulator.abstracts.Cell.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.abstracts.Cell;
import simulator.actors.Master;
import simulator.network.Network;
import simulator.network.UE;

public class ENodeB extends Cell {
    private static Logger log = LoggerFactory.getLogger(ENodeB.class);

    {
        startWith(Off, null);

        when(Off, matchEvent(Events.class, (event, data) -> (event == ConnectToNetwork), (event, data) -> {
            sender().tell(Network.NetworkEvents.ConnectCell, self());
            return stay();
        }).event((event, data) -> (event == ConnectCellAck), (event, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event((event, data) -> (event == ConnectDevice), (event, data) -> {
            addDevice(sender());
            sender().tell(UE.Events.AckConnectToCell, self());
            return stay();
        }).anyEvent((event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }).event(Events.class, (event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        when(On, matchEvent(Events.class, (event, data) -> (event == ConnectDevice), (state, data) -> {
            addDevice(sender());
            sender().tell(UE.Events.AckConnectToCell, self());
            return stay();
        }).event((event, data) -> (event == DisconnectDevice), (event, data) -> {
            removeDevice(sender());
            sender().tell(UE.Events.AckDisconnectFromCell, self());
            return stay();
        }).anyEvent((event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }).event(Events.class, (event, state) -> {
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
