package simulator.actors.events;

import simulator.actors.interfaces.EventInterface;

public enum SubscriberEvents implements EventInterface {
    WakeUp, GoToSleep, ReturnFromWork, GoToWork
}
