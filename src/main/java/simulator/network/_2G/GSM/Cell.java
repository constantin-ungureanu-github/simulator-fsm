package simulator.network._2G.GSM;

import static simulator.network._2G.GSM.Cell.State.Off;
import static simulator.network._2G.GSM.Cell.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.Master;
import simulator.actors.events.CellEvents.ConnectCellAck;
import simulator.actors.events.CellEvents.ConnectDevice;
import simulator.actors.events.CellEvents.ConnectToNetwork;
import simulator.actors.events.CellEvents.DisconnectDevice;
import simulator.actors.events.DeviceEvents.AckConnectToCell;
import simulator.actors.events.DeviceEvents.AckDisconnectFromCell;
import simulator.actors.events.NetworkEvents.ConnectCell;

public class Cell extends simulator.actors.abstracts.Cell {
    private static Logger log = LoggerFactory.getLogger(Cell.class);

    public enum State implements simulator.actors.interfaces.State {
        On, Off
    }

    {
        startWith(Off, null);

        when(Off, matchEvent(ConnectToNetwork.class, (event, data) -> processConnectToNetwork(event))
                .event(ConnectCellAck.class, (event, data) -> processConnectCellAck(event))
                .event(ConnectDevice.class, (event, data) -> processConncectDevice(event))
                .event(DisconnectDevice.class, (event, data) -> processDisconnectDevice(event)).anyEvent((event, state) -> processUnhandledEvent(event)));

        when(On, matchEvent(ConnectDevice.class, (event, data) -> processConncectDevice(event))
                .event(DisconnectDevice.class, (event, data) -> processDisconnectDevice(event)).anyEvent((event, state) -> processUnhandledEvent(event)));

        whenUnhandled(matchAnyEvent((event, data) -> processUnhandledEvent(event)));

        initialize();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processConnectToNetwork(ConnectToNetwork event) {
        event.getDestination().tell(new ConnectCell(event.getSource(), event.getDestination(), null), self());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processConnectCellAck(ConnectCellAck event) {
        setNetwork(sender());
        Master.getMaster().tell(Master.Events.Ping, self());
        return goTo(On);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processConncectDevice(ConnectDevice event) {
        addDevice(event.getSource());
        event.getSource().tell(new AckConnectToCell(event.getDestination(), event.getSource(), null), self());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processDisconnectDevice(DisconnectDevice event) {
        removeDevice(event.getSource());
        sender().tell(new AckDisconnectFromCell(event.getDestination(), event.getSource(), null), self());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processUnhandledEvent(Object event) {
        log.error("Unhandled event: {}", event);
        return stay();
    }
}
