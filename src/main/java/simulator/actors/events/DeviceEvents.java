package simulator.actors.events;

import simulator.actors.interfaces.EventInterface;

public enum DeviceEvents implements EventInterface {
    PickedBySubscriber,
    AddDevice, RemoveDevice,
    PowerOn, PowerOff,
    ConnectToCell, AckConnectToCell, NAckConnectToCell, DisconnectFromCell, AckDisconnectFromCell,
    SendSMS, ReceiveSMS, AckSendSMS, NAckSendSMS,
    MakeVoiceCall, ReceiveVoiceCall, AckMakeVoiceCall, NAckMakeVoiceCall,
    RequestDataSession, AckRequestDataSession, NAckRequestDataSession
}
