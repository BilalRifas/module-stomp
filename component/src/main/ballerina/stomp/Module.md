## Package overview

Ballerina Stomp Client Endpoint is used to connect Ballerina with STOMP specific Brokers. With this Stomp Client, Ballerina can act as Stomp Consumers and Stomp Producers. 
This connector is compatible with Stomp 1.2 version.

## Samples
### Simple Stomp consumer

Following is a simple service (stompService) which is subscribed to queue 'test-stomp' on remote Stomp specific broker. In this example, acknowledgement is done automatically inside the resource by setting property ackMode: stomp:AUTO at service config.

```ballerina
import ballerina/stomp;
import ballerina/log;

listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

@stomp:ServiceConfig{
        destination:"/queue/test-stomp",
        ackMode: stomp:AUTO
}

service stompListenerSports on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object gives us the string message and id.
    resource function onMessage(stomp:Message message) {
        var messageId = message.getMessageId();
        var content = message.getContent();
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}
```

### Stomp Producer

Following example demonstrates a way to publish a message to a specified queue. A Stomp message is created from string, and then it is published to queue 'test-stomp' in remote Stomp specific broker.

```ballerina
import ballerina/stomp;

stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

public function main() {
        string message = "Hello World From Ballerina";
        string destination = "/queue/test-stomp";
        map<string> customHeaderMap = {};
        var publish = stompSender->send(message,destination,customHeaderMap);
        var disconnect = stompSender->disconnect();
}
```