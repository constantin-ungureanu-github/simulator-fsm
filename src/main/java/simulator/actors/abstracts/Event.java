package simulator.actors.abstracts;

import akka.actor.ActorRef;
import simulator.actors.interfaces.Events;
import simulator.actors.interfaces.Message;

public abstract class Event implements Events {
    private ActorRef source;
    private ActorRef destination;
    private Message message;

    public Event() {
    }

    public Event(ActorRef source, ActorRef destination, Message message) {
        this.source = source;
        this.destination = destination;
        this.message = message;
    }

    public ActorRef getSource() {
        return source;
    }

    public ActorRef getDestination() {
        return destination;
    }

    public Message getMessage() {
        return message;
    }
}
