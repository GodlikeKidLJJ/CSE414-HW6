package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
//        // printing greetings text
//        System.out.println();
//        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
//        System.out.println("*** Please enter one of the following commands ***");
//        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
//        System.out.println("> create_caregiver <username> <password>");
//        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
//        System.out.println("> login_caregiver <username> <password>");
//        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
//        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
//        System.out.println("> upload_availability <date>");
//        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
//        System.out.println("> add_doses <vaccine> <number>");
//        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
//        System.out.println("> logout");  // TODO: implement logout (Part 2)
//        System.out.println("> quit");
//        System.out.println();
        menuDisplay();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void menuDisplay() {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();
    }

    private static void createPatient(String[] tokens) {
        // create_patient <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        if (!strongPasswordChecker(password)) {
            return;
        }

        // check 2: check if the username has been taken already
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the Patient
        try {
            currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to patient information to our database
            currentPatient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
        menuDisplay();
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }


    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        if (!strongPasswordChecker(password)) {
            return;
        }

        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
        menuDisplay();
    }

    // private helper: check if the user inputs a valid strong password
    private static boolean strongPasswordChecker(String password) {
        // at least 8 characters
        if (password.length() < 8) {
            System.out.println("Strong passwords should be at least 8 characters");
            return false;
        }
        int lowercase = 0;
        int uppercase = 0;
        int number = 0;
        int specialCharacter = 0;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c >= 'a' && c <= 'z') {
                lowercase += 1;
            } else if (c >= 'A' && c <= 'Z') {
                uppercase += 1;
            } else if (Character.isDigit(c)) {
                number += 1;
            } else if (c == '!' || c == '@' || c == '#' || c == '?') {
                specialCharacter += 1;
            }
        }

        // check is the input valid?
        if (uppercase == 0 || lowercase == 0) {
            System.out.println("Strong password should contains both uppercase and lowercase letters");
            return false;
        }
        if (number == 0) {
            System.out.println("Strong password should contains both numbers and letters");
            return false;
        }
        if (specialCharacter == 0) {
            System.out.println("Strong password should contains at least one special character");
            return false;
        }
        return true;
    }





    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // login_patient <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed2.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
        menuDisplay();
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
        menuDisplay();
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // check if caregiver and patient have already login in
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }

        // a caregiver or patient has already logged in
        String available_date = tokens[1];

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String selectUsername = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username";
        String selectAvailableDose = "SELECT Name, Doses FROM Vaccines";
        try {
            Date d = Date.valueOf(available_date);

            PreparedStatement statementCaregiver = con.prepareStatement(selectUsername);
            PreparedStatement statementVaccine = con.prepareStatement(selectAvailableDose);

            statementCaregiver.setDate(1, d);

            ResultSet resultSetCaregiver = statementCaregiver.executeQuery();
            ResultSet resultSetVaccine = statementVaccine.executeQuery();

            System.out.println("All Available Caregivers at " + available_date + " :");
            if (!resultSetCaregiver.next()){
                System.out.println("There is no caregiver");
                return;
            }

            System.out.print(" " + resultSetCaregiver.getString(1));
            while (resultSetCaregiver.next()) {
                System.out.print(" " + resultSetCaregiver.getString(1));
            }
            System.out.println();
            System.out.println("Vaccine + number of doses: ");

            while (resultSetVaccine.next()) {
                System.out.println(resultSetVaccine.getString(1) + " " + resultSetVaccine.getInt(2));
            }
        } catch (SQLException e) { // not sure what is the error output?
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        menuDisplay();
    }

    private static void reserve(String[] tokens) {
        // requirement 6: make sure the patient log in first (probably redundancy, fix later )
        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first!");
            return;
        }
        if (currentPatient == null) {
            System.out.println("Please login as a patient!");
            return;
        }

        // check if the user input correct information
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            System.out.println("error appears when enter the information");
            return;
        }

        String vaccineName = tokens[2];
        // any constrains for date?

        //check vaccine dose(requirement 5)
        Vaccine vaccine = null;
        int number_of_dose = 0;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
            number_of_dose = vaccine.getAvailableDoses();
        } catch (SQLException e) {
            System.out.println("Please try again!");
            System.out.println("Error appears when checking the dose");
            e.printStackTrace();
        }
        if (number_of_dose == 0) {
            System.out.println("Not enough available doses!");
            return;
        }


        String patientName = currentPatient.getUsername();

        // generate a appId
        String date = tokens[1];
        Random r = new Random();
        int appId = Math.abs(r.nextInt(9999) +  date.hashCode());
//        String selectAppId = "SELECT COUNT(*) FROM Appointment";
//        try {
//            PreparedStatement statementAppId = con.prepareStatement(selectAppId);
//            ResultSet resultAppId = statementAppId.executeQuery();
//            resultAppId.next();
//            appId += resultAppId.getInt(1) + 1;
//        } catch (SQLException e) {
//            System.out.println("Error appears when generate appId");
//            e.printStackTrace();
//        }


        // assign a caregiver(similar to searchCareGiver)
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String selectUsername = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username";
        String careGiverName = "";
        try {
            PreparedStatement statementCaregiver = con.prepareStatement(selectUsername);
            statementCaregiver.setString(1, date);
            ResultSet resultSetCaregiver = statementCaregiver.executeQuery();
            //resultSetCaregiver.next();
            if (!resultSetCaregiver.next()){
                System.out.println("There is no caregiver");
                return;
            }
            careGiverName = resultSetCaregiver.getString(1);
            System.out.println("Successfully made the reservation in " + date + ". \n \nHere is your appointment information: ");
            System.out.print("Appointment ID: {" + appId + "}, Caregiver username: {" + careGiverName + "}");
        } catch (SQLException e) {
            System.out.println("Error appears when assign careGiver");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }

        //update the appointment table
        ConnectionManager cm1 = new ConnectionManager();
        Connection con1 = cm1.createConnection();
        String updateAppointment = "INSERT INTO Appointment VALUES (?, ?, ?, ?, ?)";
        String updateAvailability = "DELETE FROM Availabilities WHERE Time = ? AND Username = ?";
        try {
            PreparedStatement update_statement = con1.prepareStatement(updateAppointment);
            update_statement.setInt(1, appId);
            update_statement.setString(2, vaccineName);
            update_statement.setString(3, date);
            update_statement.setString(4, patientName);
            update_statement.setString(5, careGiverName);
            update_statement.executeUpdate();

            PreparedStatement update_availability_statement = con1.prepareStatement(updateAvailability);
            update_availability_statement.setString(1, date);
            update_availability_statement.setString(2, careGiverName);
            update_availability_statement.executeUpdate();

            // update the number of vaccine
            vaccine.decreaseAvailableDoses(1);
        } catch (SQLException e) {

            System.out.println("Error occurred when making appointments");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        System.out.println();
        menuDisplay();
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1]; // format: yyyy-mm-dd

        try {
            Date d = Date.valueOf(date);
            if (availabilityChecker(d, currentCaregiver.getUsername())) {
                System.out.println("The Availability of the careGiver has already been recorded");
                return;
            }
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
        menuDisplay();
    }

    private static boolean availabilityChecker(Date date, String careGiverName) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String availabilityStatement = "SELECT * FROM Availabilities WHERE Time = ? AND Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(availabilityStatement);
            statement.setDate(1, date);
            statement.setString(2, careGiverName);
            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking availability");
            e.printStackTrace();
        }
        return true;
    }

    private static void cancel(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        // update the dose
        // add the caregiver back to availiability
        // delete the appointment from appointment table (assume one patient can make many appointments)
        int appId = Integer.parseInt(tokens[1]);
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String appIdStatement = "SELECT caregivers_name, app_date, vaccine_name FROM Appointment WHERE app_id = ?";
        String availabilityStatement = "INSERT INTO Availabilities VALUES (?, ?)";
        String deleteStatement = "DELETE FROM Appointment WHERE app_id = ?";
        try {
            PreparedStatement selectAppointment = con.prepareStatement(appIdStatement);
            selectAppointment.setInt(1, appId);
            ResultSet result = selectAppointment.executeQuery();
            String careGiverName = null;
            Date appDate = null;
            String vaccineName = null;
            if (result.next()) {
                careGiverName = result.getString(1);
                appDate = result.getDate(2);
                vaccineName = result.getString(3);
            }
            //System.out.println("App date: " + appDate);

            Vaccine vaccine = new Vaccine.VaccineGetter(vaccineName).get();
            //update the dose of vaccine
            vaccine.increaseAvailableDoses(1);


            if (availabilityChecker(appDate, careGiverName)) {
                System.out.println("The Availability of the careGiver has already been recorded");
                return;
            } else {
                // update the Availabilities
                PreparedStatement updateAvailability = con.prepareStatement(availabilityStatement);
                updateAvailability.setDate(1, appDate);
                updateAvailability.setString(2, careGiverName);
                updateAvailability.executeUpdate();
            }

            // delete the appointment from appointment table
            PreparedStatement deleteAppointment = con.prepareStatement(deleteStatement);
            deleteAppointment.setInt(1, appId);
            deleteAppointment.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error occurred when canceling appointment");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        System.out.println("successfully cancel the Appointment " + appId);


        menuDisplay();
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
        menuDisplay();
    }

    private static void showAppointments(String[] tokens) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        if (currentCaregiver != null) {
            // print out the appointments for caregivers first
            String careGiverStatement = "SELECT app_id, vaccine_name, app_date, patient_name FROM Appointment WHERE caregivers_name = ? ORDER BY app_id";
            try {
                PreparedStatement careGiverAppointment = con.prepareStatement(careGiverStatement);
                careGiverAppointment.setString(1, currentCaregiver.getUsername());
                ResultSet result = careGiverAppointment.executeQuery();
               while (result.next()) {
                    System.out.print("Appointment ID: " + result.getInt(1) + " ");
                    System.out.print("Vaccine name : " + result.getString(2) + " ");
                    System.out.print("Date: " + result.getString(3) + " ");
                    System.out.print("Patient Name : " + result.getString(4) + " ");
                    System.out.println();
                }
            } catch (SQLException e) {
                System.out.println("Please try again!");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }

        } else if(currentPatient != null) {
            // print out the appointments for patients
            String patientStatement = "SELECT app_id, vaccine_name, app_date, caregivers_name FROM Appointment WHERE patient_name = ? ORDER BY app_id";
            try {
                PreparedStatement patientAppointment = con.prepareStatement(patientStatement);
                patientAppointment.setString(1, currentPatient.getUsername());
                ResultSet result = patientAppointment.executeQuery();
                while (result.next()) {
                    System.out.print("Appointment ID: " + result.getInt(1) + " ");
                    System.out.print("Vaccine name : " + result.getString(2) + " ");
                    System.out.print("Date: " + result.getString(3) + " ");
                    System.out.print("Caregiver Name : " + result.getString(4) + " ");
                    System.out.println();
                }
            } catch (SQLException e) {
                System.out.println("Please try again!");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        } else {
            // there is no user log in
            System.out.println("Please login first!");
        }
        menuDisplay();
    }

    private static void logout(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        currentPatient = null;
        currentCaregiver = null;
        System.out.println("Successfully logged out!");
        menuDisplay();
    }
}
