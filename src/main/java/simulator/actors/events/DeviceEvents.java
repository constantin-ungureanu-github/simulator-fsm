package simulator.actors.events;

import akka.actor.ActorRef;
import simulator.actors.abstracts.Event;
import simulator.actors.interfaces.Message;

public class DeviceEvents {
    private DeviceEvents() {
    }

    public static class PickedBySubscriber extends Event {
        public PickedBySubscriber() {
            super();
        }

        public PickedBySubscriber(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class AddDevice extends Event {
        public AddDevice() {
            super();
        }

        public AddDevice(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class RemoveDevice extends Event {
        public RemoveDevice() {
            super();
        }

        public RemoveDevice(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class PowerOn extends Event {
        public PowerOn() {
            super();
        }

        public PowerOn(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class PowerOff extends Event {
        public PowerOff() {
            super();
        }

        public PowerOff(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class ConnectToCell extends Event {
        public ConnectToCell() {
            super();
        }

        public ConnectToCell(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class AckConnectToCell extends Event {
        public AckConnectToCell() {
            super();
        }

        public AckConnectToCell(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class NAckConnectToCell extends Event {
        public NAckConnectToCell() {
            super();
        }

        public NAckConnectToCell(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class DisconnectFromCell extends Event {
        public DisconnectFromCell() {
            super();
        }

        public DisconnectFromCell(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class AckDisconnectFromCell extends Event {
        public AckDisconnectFromCell() {
            super();
        }

        public AckDisconnectFromCell(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class SendSMS extends Event {
        public SendSMS() {
            super();
        }

        public SendSMS(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class ReceiveSMS extends Event {
        public ReceiveSMS() {
            super();
        }

        public ReceiveSMS(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class AckSendSMS extends Event {
        public AckSendSMS() {
            super();
        }

        public AckSendSMS(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class NAckSendSMS extends Event {
        public NAckSendSMS() {
            super();
        }

        public NAckSendSMS(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class MakeVoiceCall extends Event {
        public MakeVoiceCall() {
            super();
        }

        public MakeVoiceCall(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class ReceiveVoiceCall extends Event {
        public ReceiveVoiceCall() {
            super();
        }

        public ReceiveVoiceCall(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class AckMakeVoiceCall extends Event {
        public AckMakeVoiceCall() {
            super();
        }

        public AckMakeVoiceCall(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class NAckMakeVoiceCall extends Event {
        public NAckMakeVoiceCall() {
            super();
        }

        public NAckMakeVoiceCall(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class RequestDataSession extends Event {
        public RequestDataSession() {
            super();
        }

        public RequestDataSession(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class AckRequestDataSession extends Event {
        public AckRequestDataSession() {
            super();
        }

        public AckRequestDataSession(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }

    public static class NAckRequestDataSession extends Event {
        public NAckRequestDataSession() {
            super();
        }

        public NAckRequestDataSession(ActorRef source, ActorRef destination, Message message) {
            super(source, destination, message);
        }
    }
}
