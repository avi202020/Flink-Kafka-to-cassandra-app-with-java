package com.knoldus;

import com.knoldus.services.CassandraService;
import com.knoldus.services.KafkaService;
import model.Car;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.log4j.Logger;

import java.util.Objects;

/**
 * FlinkStreamingWithKafkaAndCassandra is a class that integrates Flink Streaming
 * with kafka and cassandra.
 */
public class FlinkStreamingWithKafkaAndCassandra {

    private static final Logger LOGGER = Logger.getLogger(FlinkStreamingWithKafkaAndCassandra.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Started Flink Application.");

        LOGGER.info("Creating Flink Stream Execution Environment.");
        StreamExecutionEnvironment environment = StreamExecutionEnvironment
                .getExecutionEnvironment();

        LOGGER.info("Instantiating KafkaService Class and Consume message from kafka topic.");
        KafkaService kafkaService = new KafkaService();
        DataStream<String> jsonDataStream = kafkaService.kafkaStreamConsumer(environment);

        LOGGER.info(" Transforming Json Data from kafka into Car Pojo.");
        final DataStream<Car> carStream = jsonDataStream.map(kafkaMessage -> {
            try {
                JsonNode jsonNode = new ObjectMapper().readValue(kafkaMessage, JsonNode.class);
                return Car.builder().Name(jsonNode.get("Name").asText()).Horsepower(jsonNode.get("Horsepower").asLong())
                        .Origin(jsonNode.get("Origin").asText()).Year(jsonNode.get("Year").asText())
                        .Weight_in_lbs(jsonNode.get("Weight_in_lbs").asLong()).Miles_per_Gallon(jsonNode.get("Miles_per_Gallon").asDouble())
                        .Displacement(jsonNode.get("Displacement").asDouble()).Cylinders(jsonNode.get("Cylinders").asLong())
                        .Acceleration(jsonNode.get("Displacement").asDouble()).build();

            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).forward();

        LOGGER.info("Instantiating CassandraService Class and sinking data into CassandraDB.");
        CassandraService cassandraService = new CassandraService();
        cassandraService.sinkToCassandraDB(carStream);

        environment.execute();

    }
}
