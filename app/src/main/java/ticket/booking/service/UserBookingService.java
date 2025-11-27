package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserBookingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String USER_FILE_PATH = "C:\\Users\\Sabeer\\Documents\\ticket-booking-java-project\\app\\src\\main\\java\\ticket\\booking\\localDB\\users.json";

    private List<User> userList;
    private User currentUser;

    public UserBookingService(User user) throws IOException {
        this.currentUser = user;
        loadUsers();
    }

    public UserBookingService() throws IOException {
        loadUsers();
    }

    private void loadUsers() throws IOException {
        userList = objectMapper.readValue(new File(USER_FILE_PATH), new TypeReference<List<User>>() {});
    }

    private void saveUsers() throws IOException {
        objectMapper.writeValue(new File(USER_FILE_PATH), userList);
    }

    public boolean loginUser() {
        return userList.stream()
                .anyMatch(u -> u.getName().equals(currentUser.getName()) &&
                        UserServiceUtil.checkPassword(currentUser.getPassword(), u.getHashedPassword()));
    }

    public boolean signUp(User newUser) {
        try {
            userList.add(newUser);
            saveUsers();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void fetchBookings() {
        Optional<User> userOpt = findCurrentUser();
        userOpt.ifPresent(User::printTickets);
    }

    public boolean cancelBooking(String ticketId) {
        Optional<User> userOpt = findCurrentUser();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(ticketId));
            if (removed) {
                try {
                    saveUsers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return removed;
        }
        return false;
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    new TrainService().addOrUpdateTrain(train);
                    Ticket ticket = new Ticket(/* generate ticket ID etc */);
                    currentUser.getTicketsBooked().add(ticket);
                    saveUsers();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Optional<User> findCurrentUser() {
        return userList.stream()
                .filter(u -> u.getName().equals(currentUser.getName()) &&
                        UserServiceUtil.checkPassword(currentUser.getPassword(), u.getHashedPassword()))
                .findFirst();
    }
}
