package simulator.network._4G.LTE;

import static simulator.network.Cell.Events.ConnectCellAck;
import static simulator.network.Cell.Events.ConnectDevice;
import static simulator.network.Cell.Events.ConnectToNetwork;
import static simulator.network.Cell.Events.DisconnectDevice;
import static simulator.network.Cell.State.Off;
import static simulator.network.Cell.State.On;
import simulator.Master;
import simulator.network.Cell;
import simulator.network.Device;
import simulator.network.Network;

public class eNodeB extends Cell {
    {
        startWith(Off, null);

        when(Off, matchEvent(Events.class, (event, data) -> (event == ConnectToNetwork), (event, data) -> {
            sender().tell(Network.Events.ConnectCell, self());
            return stay();
        }).event((event, data) -> (event == ConnectCellAck), (event, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event((event, data) -> (event == ConnectDevice), (event, data) -> {
            addDevice(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
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
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }).event((event, data) -> (event == DisconnectDevice), (event, data) -> {
            removeDevice(sender());
            sender().tell(Device.Events.AckDisconnectFromCell, self());
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
