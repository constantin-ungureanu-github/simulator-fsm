package simulator.actors.events;

import simulator.actors.interfaces.Events;

public enum DeviceEvents implements Events {
    PickedBySubscriber,
    AddDevice, RemoveDevice,
    PowerOn, PowerOff,
    ConnectToCell, AckConnectToCell, NAckConnectToCell, DisconnectFromCell, AckDisconnectFromCell,
    SendSMS, ReceiveSMS, AckSendSMS, NAckSendSMS,
    MakeVoiceCall, ReceiveVoiceCall, AckMakeVoiceCall, NAckMakeVoiceCall,
    RequestDataSession, AckRequestDataSession, NAckRequestDataSession
}
