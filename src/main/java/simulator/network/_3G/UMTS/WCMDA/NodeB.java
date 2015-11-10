package simulator.network._3G.UMTS.WCMDA;

import static simulator.network.Cell.Events.ConnectCellAck;
import static simulator.network.Cell.Events.ConnectDevice;
import static simulator.network.Cell.Events.ConnectToNetwork;
import static simulator.network.Cell.Events.DisconnectDevice;
import static simulator.network.Cell.State.Available;
import static simulator.network.Cell.State.Idle;

import simulator.Master;
import simulator.network.Cell;
import simulator.network.Device;
import simulator.network.Network;

public class NodeB extends Cell {
    {
        when(Idle, matchEvent(Events.class, (event, data) -> (event == ConnectToNetwork), (event, data) -> {
            sender().tell(Network.Events.ConnectCell, self());
            return stay();
        }).event((event, data) -> (event == ConnectCellAck), (event, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Available);
        }).event((event, data) -> (event == ConnectDevice), (event, data) -> {
            addDevice(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEvent(Events.class, (event, data) -> (event == ConnectDevice), (state, data) -> {
            addDevice(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }).event((event, data) -> (event == DisconnectDevice), (event, data) -> {
            removeDevice(sender());
            sender().tell(Device.Events.AckDisconnectFromCell, self());
            return stay();
        }));
    }
}
