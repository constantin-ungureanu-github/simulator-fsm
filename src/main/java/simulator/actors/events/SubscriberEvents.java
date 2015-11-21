package simulator.actors.events;

import akka.actor.ActorRef;
import simulator.actors.abstracts.Event;
import simulator.actors.interfaces.Message;

public class SubscriberEvents {
    private SubscriberEvents() {
    }

    public static class WakeUp extends Event {
        public WakeUp() {
            super();
        }

        public WakeUp(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class GoToSleep extends Event {
        public GoToSleep() {
        }

        public GoToSleep(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class ReturnFromWork extends Event {
        public ReturnFromWork() {
        }

        public ReturnFromWork(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class GoToWork extends Event {
        public GoToWork() {
        }

        public GoToWork(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }
}
