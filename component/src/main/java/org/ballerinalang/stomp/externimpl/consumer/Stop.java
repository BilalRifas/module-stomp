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

package org.ballerinalang.stdlib.stomp.externimpl.consumer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.StompConstants;

import java.util.concurrent.CountDownLatch;
/**
 * Stop server stomp listener.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = StompConstants.BALLERINA,
        packageName = StompConstants.STOMP,
        functionName = "stop",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "Listener",
                structPackage = StompConstants.STOMP_PACKAGE), isPublic = true)
public class Stop extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BMap<String, BValue> listenerObj = (BMap<String, BValue>) context.getRefArgument(0);
        CountDownLatch countDownLatch =
                (CountDownLatch) listenerObj.getNativeData(StompConstants.COUNTDOWN_LATCH);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
        context.setReturnValues();
    }
}
