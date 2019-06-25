/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stomp.externimpl.consumer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stomp.StompConstants;
import org.ballerinalang.stomp.StompUtils;
import org.ballerinalang.stomp.message.DefaultStompClient;
import org.ballerinalang.stomp.message.StompDispatcher;
import org.ballerinalang.stomp.message.StompException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Start server stomp listener.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = StompConstants.BALLERINA,
        packageName = StompConstants.STOMP,
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "Listener",
                structPackage = StompConstants.STOMP_PACKAGE),
        isPublic = true
)
public class Start implements NativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Start.class);
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

    }

    public static void Start(Strand strand, ObjectValue connectionObjectValue, MapValue<String,
            Object> startConfig) {
        try {
        startConfig.addNativeData(StompConstants.COUNTDOWN_LATCH, countDownLatch);

        String ackMode = (String) startConfig.getNativeData(StompConstants.CONFIG_FIELD_ACKMODE);
        String durableId = (String) startConfig.getNativeData(StompConstants.CONFIG_FIELD_DURABLEID);
        boolean durableFlag = (boolean) startConfig.getNativeData(StompConstants.CONFIG_FIELD_DURABLE);

        // Get stompClient object created in intListener.
        DefaultStompClient client = (DefaultStompClient)
                startConfig.getNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ);

        client.setCallableUnit(strand);

        CountDownLatch signal = new CountDownLatch(1);

        client.setContext(startConfig);

        // Connect to STOMP server, send CONNECT command and wait CONNECTED answer.
        client.setCountDownLatch(signal);

        if (durableFlag) {
            client.durableConnect(durableId);
        } else {
            client.connect();
        }

        try {
            // TODO: Implement Retry attempt method after timeout
            if (!signal.await(60, TimeUnit.SECONDS)) {
                log.debug("Connection time exceeded");
                throw new BallerinaException(new TimeoutException());
            }
            log.debug("Waiting for connect");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Change variable name to destination Map or something.
        Map<String, Service> destinationMap = StompDispatcher.getServiceRegistryMap();
        for (Map.Entry<String, Service> entry : destinationMap.entrySet()) {
            String subscribeDestination = entry.getKey();
            if (durableFlag) {
                client.durableSubscribe(subscribeDestination, ackMode, durableId);
            } else {
                client.subscribe(subscribeDestination, ackMode);
            }
        }

        // It is essential to keep a non-daemon thread running in order to avoid the java program or the
        // Ballerina service from exiting.
        new Thread(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
        context.setReturnValues();
    } catch (StompException e) {
        context.setReturnValues(StompUtils.getError(context, e));
        callableUnitCallback.notifySuccess();
    }
}

    @Override
    public boolean isBlocking() {
        return false;
    }
}
