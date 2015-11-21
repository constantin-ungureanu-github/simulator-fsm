package simulator.network._4G.LTE;

import static simulator.actors.abstracts.NE.State.Off;
import static simulator.actors.abstracts.NE.State.On;
import static simulator.actors.events.CellEvents.ConnectCellAck;
import static simulator.actors.events.CellEvents.ConnectDevice;
import static simulator.actors.events.CellEvents.ConnectToNetwork;
import static simulator.actors.events.CellEvents.DisconnectDevice;
import static simulator.actors.events.DeviceEvents.AckConnectToCell;
import static simulator.actors.events.DeviceEvents.AckDisconnectFromCell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.Master;
import simulator.actors.abstracts.Cell;
import simulator.actors.events.CellEvents;
import simulator.actors.events.NetworkEvents;

public class ENodeB extends Cell {
    private static Logger log = LoggerFactory.getLogger(ENodeB.class);

    {
        startWith(Off, null);

        when(Off, matchEvent(CellEvents.class, (event, data) -> (event == ConnectToNetwork), (event, data) -> {
            sender().tell(NetworkEvents.ConnectCell, self());
            return stay();
        }).event((event, data) -> (event == ConnectCellAck), (event, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event((event, data) -> (event == ConnectDevice), (event, data) -> {
            addDevice(sender());
            sender().tell(AckConnectToCell, self());
            return stay();
        }).anyEvent((event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }).event(CellEvents.class, (event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        when(On, matchEvent(CellEvents.class, (event, data) -> (event == ConnectDevice), (state, data) -> {
            addDevice(sender());
            sender().tell(AckConnectToCell, self());
            return stay();
        }).event((event, data) -> (event == DisconnectDevice), (event, data) -> {
            removeDevice(sender());
            sender().tell(AckDisconnectFromCell, self());
            return stay();
        }).anyEvent((event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }).event(CellEvents.class, (event, state) -> {
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
