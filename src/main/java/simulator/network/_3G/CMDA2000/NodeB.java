package simulator.network._3G.CMDA2000;

import static simulator.network._3G.CMDA2000.NodeB.State.Off;
import static simulator.network._3G.CMDA2000.NodeB.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.actors.Master;
import simulator.actors.abstracts.Cell;
import simulator.actors.events.CellEvents;
import simulator.actors.events.CellEvents.ConnectCellAck;
import simulator.actors.events.CellEvents.ConnectDevice;
import simulator.actors.events.CellEvents.ConnectToNetwork;
import simulator.actors.events.CellEvents.DisconnectDevice;
import simulator.actors.events.DeviceEvents.AckConnectToCell;
import simulator.actors.events.DeviceEvents.AckDisconnectFromCell;
import simulator.actors.events.NetworkEvents.ConnectCell;

public class NodeB extends Cell {
    private static Logger log = LoggerFactory.getLogger(NodeB.class);

    public enum State implements simulator.actors.interfaces.State {
        On, Off
    }

    {
        startWith(Off, null);

        when(Off, matchEvent(ConnectToNetwork.class, (event, data) -> {
            sender().tell(new ConnectCell(), self());
            return stay();
        }).event(ConnectCellAck.class, (event, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event(ConnectDevice.class, (event, data) -> {
            addDevice(sender());
            sender().tell(new AckConnectToCell(), self());
            return stay();
        }).event(CellEvents.class, (event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        when(On, matchEvent(ConnectDevice.class, (state, data) -> {
            addDevice(sender());
            sender().tell(new AckConnectToCell(), self());
            return stay();
        }).event(DisconnectDevice.class, (event, data) -> {
            removeDevice(sender());
            sender().tell(new AckDisconnectFromCell(), self());
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
