/**
 * Copyright (C) 2014-2015 SINTEF
 *
 *     Brian Elvesæter <brian.elvesater@sintef.no>
 *     Shahzad Karamat <shazad.karamat@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.modelbased.proasense.adapter.example;

import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.SimpleEvent;
import eu.proasense.internal.VariableType;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.joda.time.DateTime;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.DataCallback;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.da.SyncAccess;


public class MyOPCAdapter {

    public static void main(String[] args) throws Exception {

        // create connection information
        final ConnectionInformation ci = new ConnectionInformation();

        ci.setHost("192.168.11.33");
        ci.setDomain("");
        ci.setUser("");
        ci.setPassword("");
        // ci.setProgId("SWToolbox.TOPServer.V5");
        ci.setClsid("680DFBF7-C92D-484D-84BE-06DC3DECCD68"); // if ProgId is not working, try it using the Clsid instead
//        final String itemId = "Channel1.Device1.Tag1";
        final String itemId = "Group1";
        // create a new server
        final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
        try {
            // connect to server
            server.connect();
            // add sync access, poll every 500 ms
            final AccessBase access = new SyncAccess(server, 500);
            access.addItem(itemId, new DataCallback() {
//                @Override
                public void changed(Item item, ItemState state) {
                    // also dump value
                    try {
                        if (state.getValue().getType() == JIVariant.VT_UI4) {
                            System.out.println("<<< " + state + " / value = " + state.getValue().getObjectAsUnsigned().getValue());
                        } else {
                            System.out.println("<<< " + state + " / value = " + state.getValue().getObject());
                        }
                    } catch (JIException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Add a new group
            final Group group = server.addGroup("test");
            // Add a new item to the group
            final Item item = group.addItem(itemId);

            // start reading
            access.bind();

            // add a thread for writing a value every 3 seconds
            ScheduledExecutorService writeThread = Executors.newSingleThreadScheduledExecutor();
            final AtomicInteger i = new AtomicInteger(0);
            writeThread.scheduleWithFixedDelay(new Runnable() {
//                @Override
                public void run() {
                    final JIVariant value = new JIVariant(i.incrementAndGet());
                    try {
                        System.out.println(">>> " + "writing value " + i.get());
                        item.write(value);
                    } catch (JIException e) {
                        e.printStackTrace();
                    }
                }
            }, 5, 3, TimeUnit.SECONDS);

            // wait a little bit
            Thread.sleep(20 * 1000);
            writeThread.shutdownNow();
            // stop reading
            access.unbind();
        } catch (final JIException e) {
            System.out.println(String.format("%08X: %s", e.getErrorCode(), server.getErrorMessage(e.getErrorCode())));
        }
    }

}
