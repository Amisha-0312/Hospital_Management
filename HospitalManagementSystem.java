package HospitalManagementSystem;

import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {
    // Corrected JDBC URL with database name
    private static final String url = "jdbc:mysql://localhost:3306/hospital?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String username = "root";
    private static final String password = "Meowmeow@1203";

    public static void main(String[] args) {
        try {
            // Correct driver name
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Registered Successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the database successfully!\n");

            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            while (true) {
                System.out.println("===== HOSPITAL MANAGEMENT SYSTEM =====");
                System.out.println("1. Add Patient");
                System.out.println("2. View Patients");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        patient.addPatient();
                        break;
                    case 2:
                        patient.viewPatients();
                        break;
                    case 3:
                        doctor.viewDoctors();
                        break;
                    case 4:
                        bookAppointment(patient, doctor, connection, scanner);
                        break;
                    case 5:
                        System.out.println("Thank you for using the Hospital Management System. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please enter a number between 1 and 5.");
                }
                System.out.println();
            }

        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Please check your connection settings.");
            e.printStackTrace();
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        int patientId = scanner.nextInt();
        System.out.print("Enter Doctor ID: ");
        int doctorId = scanner.nextInt();
        System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
        String appointmentDate = scanner.next();

        if (patient.getPatientById(patientId) && doctor.getDoctorById(doctorId)) {
            if (checkDoctorAvailability(doctorId, appointmentDate, connection)) {
                String appointmentQuery = "INSERT INTO appointments (patient_id, doctor_id, appointment_date) VALUES (?, ?, ?)";

                try (PreparedStatement ps = connection.prepareStatement(appointmentQuery)) {
                    ps.setInt(1, patientId);
                    ps.setInt(2, doctorId);
                    ps.setString(3, appointmentDate);
                    int rowsAffected = ps.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Appointment booked successfully!");
                    } else {
                        System.out.println("Failed to book appointment.");
                    }

                } catch (SQLException e) {
                    System.err.println("Error booking appointment.");
                    e.printStackTrace();
                }

            } else {
                System.out.println("Doctor is not available on this date.");
            }

        } else {
            System.out.println("Invalid Patient ID or Doctor ID.");
        }
    }

    public static boolean checkDoctorAvailability(int doctorId, String appointmentDate, Connection connection) {
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setString(2, appointmentDate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking doctor availability.");
            e.printStackTrace();

        }
        return false;
    }
}
