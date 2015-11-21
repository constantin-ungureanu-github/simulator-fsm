package simulator.actors.events;

import simulator.actors.interfaces.EventInterface;

public enum CellEvents implements EventInterface {
    ConnectToNetwork, DisconnectFromNetwork,
    ConnectDevice, DisconnectDevice, ConnectCellAck
}
