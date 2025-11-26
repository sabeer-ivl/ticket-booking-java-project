package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.service.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public String getGreeting() {
        return "Hello, I would have a greeting";
    }

    // Safe method to read integers from Scanner
    private static int readInt(Scanner scanner, String prompt) {
        System.out.println(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number!");
            scanner.next(); // consume invalid input
        }
        return scanner.nextInt();
    }

    // Safe method to read Strings from Scanner
    private static String readString(Scanner scanner, String prompt) {
        System.out.println(prompt);
        while (!scanner.hasNext()) {
            System.out.println("Please enter a valid text!");
            scanner.next(); // consume invalid input
        }
        return scanner.next();
    }

    public static void main(String[] args) {
        System.out.println("Running Train Booking System");

        Scanner scanner = new Scanner(System.in);
        int option = 1;
        UserBookingService userBookingService;
        Train trainSelectedForBooking = null;

        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            System.out.println("Error initializing service: " + ex);
            return;
        }

        while (option != 7) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");

            option = readInt(scanner, "Enter your choice:");

            switch (option) {
                case 1:
                    String nameToSignUp = readString(scanner, "Enter the username to signup:");
                    String passwordToSignUp = readString(scanner, "Enter the password to signup:");
                    User userToSignup = new User(
                            nameToSignUp,
                            passwordToSignUp,
                            UserServiceUtil.hashPassword(passwordToSignUp),
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );
                    userBookingService.signUp(userToSignup);
                    break;

                case 2:
                    String nameToLogin = readString(scanner, "Enter the username to login:");
                    String passwordToLogin = readString(scanner, "Enter the password to login:");
                    User userToLogin = new User(
                            nameToLogin,
                            passwordToLogin,
                            UserServiceUtil.hashPassword(passwordToLogin),
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );
                    try {
                        userBookingService = new UserBookingService(userToLogin);
                        System.out.println("Login successful!");
                    } catch (IOException ex) {
                        System.out.println("Login failed: " + ex);
                        return;
                    }
                    break;

                case 3:
                    System.out.println("Fetching your bookings...");
                    userBookingService.fetchBookings();
                    break;

                case 4:
                    String source = readString(scanner, "Type your source station:");
                    String dest = readString(scanner, "Type your destination station:");
                    List<Train> trains = userBookingService.getTrains(source, dest);
                    if (trains.isEmpty()) {
                        System.out.println("No trains found for this route.");
                        break;
                    }
                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + ". Train id: " + t.getTrainId());
                        for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                            System.out.println("   Station " + entry.getKey() + " Time: " + entry.getValue());
                        }
                        index++;
                    }
                    int trainChoice = readInt(scanner, "Select a train by typing 1, 2, 3...");
                    if (trainChoice < 1 || trainChoice > trains.size()) {
                        System.out.println("Invalid train selection!");
                        break;
                    }
                    trainSelectedForBooking = trains.get(trainChoice - 1);
                    break;

                case 5:
                    if (trainSelectedForBooking == null) {
                        System.out.println("Please select a train first (option 4).");
                        break;
                    }
                    System.out.println("Available seats:");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    for (List<Integer> rowSeats : seats) {
                        for (Integer val : rowSeats) {
                            System.out.print(val + " ");
                        }
                        System.out.println();
                    }
                    int row = readInt(scanner, "Enter the row:");
                    int col = readInt(scanner, "Enter the column:");
                    System.out.println("Booking your seat...");
                    boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);
                    if (booked) {
                        System.out.println("Booked! Enjoy your journey.");
                    } else {
                        System.out.println("Can't book this seat.");
                    }
                    break;

                case 6:
                    System.out.println("Cancel booking feature not implemented yet.");
                    break;

                case 7:
                    System.out.println("Exiting application. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }

        scanner.close();
    }
}
