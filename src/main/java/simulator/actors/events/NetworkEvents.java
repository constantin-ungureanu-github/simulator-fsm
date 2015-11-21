package simulator.actors.events;

import akka.actor.ActorRef;
import simulator.actors.abstracts.Event;
import simulator.actors.interfaces.Message;

public class NetworkEvents {
    private NetworkEvents() {
    }

    public static class ConnectCell extends Event {
        public ConnectCell() {
            super();
        }

        public ConnectCell(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class DisconnectCell extends Event {
        public DisconnectCell() {
            super();
        }

        public DisconnectCell(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class RegisterDevice extends Event {
        public RegisterDevice() {
            super();
        }

        public RegisterDevice(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class UnregisterDevice extends Event {
        public UnregisterDevice() {
            super();
        }

        public UnregisterDevice(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class Routing extends Event {
        public Routing() {
            super();
        }

        public Routing(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }
}
