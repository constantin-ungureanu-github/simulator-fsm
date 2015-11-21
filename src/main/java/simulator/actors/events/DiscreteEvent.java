package simulator.actors.events;

import akka.actor.ActorRef;
import simulator.actors.abstracts.Event;
import simulator.actors.interfaces.Message;

public final class DiscreteEvent {
    private DiscreteEvent() {
    }

    public static class RemoveWork extends Event {
        public RemoveWork() {
            super();
        }

        public RemoveWork(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }
}
