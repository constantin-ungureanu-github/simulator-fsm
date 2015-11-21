package simulator.actors.events;

import akka.actor.ActorRef;
import simulator.actors.abstracts.Event;
import simulator.actors.interfaces.Message;

public class CellEvents {
    private CellEvents() {
    }

    public static class ConnectToNetwork extends Event {
        public ConnectToNetwork() {
            super();
        }

        public ConnectToNetwork(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class DisconnectFromNetwork extends Event {
        public DisconnectFromNetwork() {
            super();
        }

        public DisconnectFromNetwork(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class ConnectDevice extends Event {
        public ConnectDevice() {
            super();
        }

        public ConnectDevice(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class DisconnectDevice extends Event {
        public DisconnectDevice() {
            super();
        }

        public DisconnectDevice(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class ConnectCellAck extends Event {
        public ConnectCellAck() {
            super();
        }

        public ConnectCellAck(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }
}
