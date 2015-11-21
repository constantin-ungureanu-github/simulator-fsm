package simulator.actors.events;

import simulator.actors.interfaces.TemplateEvents;

public enum CellEvents implements TemplateEvents {
    ConnectToNetwork, DisconnectFromNetwork,
    ConnectDevice, DisconnectDevice, ConnectCellAck
}
