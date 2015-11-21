package simulator.network._2G.GSM.GPRS.EDGE;

import static simulator.actors.events.CellEvents.ConnectCellAck;
import static simulator.actors.events.CellEvents.ConnectDevice;
import static simulator.actors.events.CellEvents.ConnectToNetwork;
import static simulator.actors.events.CellEvents.DisconnectDevice;
import static simulator.actors.events.DeviceEvents.AckConnectToCell;
import static simulator.actors.events.DeviceEvents.AckDisconnectFromCell;
import static simulator.actors.events.NetworkEvents.ConnectCell;
import static simulator.network._2G.GSM.GPRS.EDGE.Cell.State.Off;
import static simulator.network._2G.GSM.GPRS.EDGE.Cell.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.Master;
import simulator.actors.events.CellEvents;

public class Cell extends simulator.actors.abstracts.Cell {
    private static Logger log = LoggerFactory.getLogger(Cell.class);

    public enum State implements simulator.actors.interfaces.State {
        On, Off
    }

    {
        startWith(Off, null);

        when(Off, matchEvent(CellEvents.class, (event, data) -> (event == ConnectToNetwork), (event, data) -> {
            sender().tell(ConnectCell, self());
            return stay();
        }).event(CellEvents.class, (event, data) -> (event == ConnectCellAck), (event, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event(CellEvents.class, (event, data) -> (event == ConnectDevice), (event, data) -> {
            addDevice(sender());
            sender().tell(AckConnectToCell, self());
            return stay();
        }).event(CellEvents.class, (event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        when(On, matchEvent(CellEvents.class, (event, data) -> (event == ConnectDevice), (state, data) -> {
            addDevice(sender());
            sender().tell(AckConnectToCell, self());
            return stay();
        }).event(CellEvents.class, (event, data) -> (event == DisconnectDevice), (event, data) -> {
            removeDevice(sender());
            sender().tell(AckDisconnectFromCell, self());
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
