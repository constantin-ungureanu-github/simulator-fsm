package simulator.actors.events;

import simulator.actors.interfaces.Events;

public enum SubscriberEvents implements Events {
    WakeUp, GoToSleep, ReturnFromWork, GoToWork
}
