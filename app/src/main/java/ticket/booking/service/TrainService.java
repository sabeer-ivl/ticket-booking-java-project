package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainService {

    private List<Train> trainList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_DB_PATH = "C:\\Users\\Sabeer\\Documents\\ticket-booking-java-project\\app\\src\\main\\java\\ticket\\booking\\localDB\\trains.json";

    public TrainService() throws IOException {
        loadTrains();
    }

    private void loadTrains() throws IOException {
        trainList = objectMapper.readValue(new File(TRAIN_DB_PATH), new TypeReference<List<Train>>() {});
    }

    private void saveTrains() throws IOException {
        objectMapper.writeValue(new File(TRAIN_DB_PATH), trainList);
    }

    public List<Train> searchTrains(String source, String destination) {
        return trainList.stream()
                .filter(train -> isValidTrain(train, source, destination))
                .collect(Collectors.toList());
    }

    public void addOrUpdateTrain(Train newTrain) throws IOException {
        Optional<Train> existingTrain = trainList.stream()
                .filter(t -> t.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();

        if (existingTrain.isPresent()) {
            trainList.set(trainList.indexOf(existingTrain.get()), newTrain);
        } else {
            trainList.add(newTrain);
        }

        saveTrains();
    }

    private boolean isValidTrain(Train train, String source, String destination) {
        List<String> stations = train.getStations();
        int sourceIndex = stations.indexOf(source.toLowerCase());
        int destinationIndex = stations.indexOf(destination.toLowerCase());
        return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
    }
}
