package simulator.actors.events;

import simulator.actors.interfaces.Events;

public enum CellEvents implements Events {
    ConnectToNetwork, DisconnectFromNetwork,
    ConnectDevice, DisconnectDevice, ConnectCellAck
}
