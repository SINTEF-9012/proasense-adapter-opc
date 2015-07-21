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
package net.modelbased.proasense.adapter.opc;

import net.modelbased.proasense.adapter.base.KafkaProducerOutput;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public abstract class AbstractOPCAdapter {
    // protected <Type> inputPort;
    protected KafkaProducerOutput outputPort;


    public AbstractOPCAdapter() {
        // Get net.modelbased.proasense.adapter.base properties
        Properties adapterProperties = loadAdapterProperties();

        // <Type> input port properties

        // Define the <Type> input port

        // Kafka output port properties
        String bootstrapServers = adapterProperties.getProperty("kafka.bootstrap.servers");
        String topic = adapterProperties.getProperty("proasense.adapter.base.topic");
        String sensorId = adapterProperties.getProperty("proasense.adapter.base.sensorid");

        // Define the Kafka output port
        this.outputPort = new KafkaProducerOutput(bootstrapServers, topic, sensorId);
    }


    private Properties loadAdapterProperties() {
        Properties adapterProperties = new Properties();
        String propFilename = "adapter.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFilename);

        try {
            if (inputStream != null) {
                adapterProperties.load(inputStream);
            } else
                throw new FileNotFoundException("Property file: '" + propFilename + "' not found in classpath.");
        }
        catch (IOException e) {
            System.out.println("Exception:" + e.getMessage());
        }

        return adapterProperties;
    }

}
