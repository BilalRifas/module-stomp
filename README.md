
# **Ballerina Stomp Client**

Ballerina Stomp Client Endpoint is used to connect Ballerina with STOMP specific Brokers. With this Stomp Client, Ballerina can act as Stomp Consumers and Stomp Producers.
This connector is compatible with Stomp 1.2 version.


## Compatibility

| Ballerina Language Version  | Stomp Protocol Version   | 
|:---------------------------:|:------------------------:|
| 0.991.0                     | 1.2                      |

## Getting started

> Refer the [Getting Started](https://ballerina.io/learn/getting-started/) guide to download and install Ballerina.

## Ballerina as a Stomp Consumer

Following is a simple service (stompService) which is subscribed to queue 'test-stomp' on remote Stomp specific broker. In this example, acknowledgement is done automatically inside the resource by setting property `ackMode: stomp:AUTO` at service config.

```ballerina
import wso2/stomp;
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
````

## Ballerina as a Stomp Producer

Following example demonstrates a way to publish a message to a specified queue. A Stomp message is created from string, and then it is published to queue 'test-stomp' in remote Stomp specific broker.

```ballerina
import wso2/stomp;

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
````

For more Stomp Connector Ballerina configurations please refer to the samples directory.
