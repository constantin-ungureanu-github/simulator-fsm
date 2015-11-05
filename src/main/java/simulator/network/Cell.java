package simulator.network;

import simulator.network.Cell.State;
import akka.actor.AbstractFSM;

public abstract class Cell extends AbstractFSM<State, Data> {
    public enum State {
        Idle,
        Available,
        Down
    }

    public enum Events {
        ConnectToNetwork,
        DisconnectFromNetwork,
        ConnectCellAck,
        ConnectDevice,
        DisconnectDevice
    }
}
