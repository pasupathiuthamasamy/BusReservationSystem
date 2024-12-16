package reservationsystem;
import java.util.*;
import java.util.Date;
import java.sql.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import databaseconnection.DBConnection;
public class BusReservationSystem {

	public static void main(String[] args) throws ParseException {
		try(Connection conn= databaseconnection.DBConnection.getConnection();
				Scanner sc=new Scanner(System.in)){
			while(true) {
				System.out.println("VK BUS RENTAL BOOKING SYSTEM\n1. VIEW BUS INFORMATIONS\n2. TO BOOK A BUS\n3. TO RETURN A BUS\n4.EXIT");
				int userInput=sc.nextInt();
				sc.nextLine();
				switch(userInput) {
				case 1:
					viewBusInfo(conn);
					break;
				case 2:
					bookBus(conn,sc);
					break;
				case 3:
					returnBus(conn,sc);
					break;
				case 4:
					System.out.println("Thank you for using VK Bus Reservation System!");
                    return;
                default:
                    System.out.println("Invalid option. Please choose a valid one.");
				}
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		  	   
	}
	private static void viewBusInfo(Connection conn) throws SQLException{
		String query="SELECT * FROM buses";
		Statement st=conn.createStatement();
		ResultSet rs=st.executeQuery(query);
		while(rs.next()) {
			System.out.println("============================================");
			System.out.println("Bus_No: "+rs.getInt("Bus_No")+
					"\nAc: "+rs.getBoolean("Ac")+
					"\nTotal Capacity: "+rs.getInt("Total_Capacity")+
					"\nRent Per Day: "+rs.getDouble("Base_Price")+
					"\nAvailable: "+rs.getBoolean("Bus_Available"));
			System.out.println("============================================");
		}
	}
	 private static void bookBus(Connection conn, Scanner sc) throws ParseException, SQLException ,InputMismatchException{
	        System.out.print("Enter Passenger Name: ");
	        String name = sc.nextLine();
	        System.out.print("Enter Bus No: ");
	        int busNo = sc.nextInt();
	        System.out.print("Enter Start Date (dd-MM-yyyy): ");
	        String startDateInput = sc.next();
	        System.out.print("Enter End Date (dd-MM-yyyy): ");
	        String endDateInput = sc.next();

	        Date startDate = new SimpleDateFormat("dd-MM-yyyy").parse(startDateInput);
	        Date endDate = new SimpleDateFormat("dd-MM-yyyy").parse(endDateInput);

	        long days = calculateDays(startDate, endDate);
	        if (days <= 0) {
	            System.out.println("Invalid date range. Please try again.");
	            return;
	        }

	        if (isBusAvailable(conn, busNo, startDate, endDate)) {
	            double totalRent = calculateRent(conn, busNo, days);

	            String insertBooking = "INSERT INTO bookings (Passenger_Name, Bus_No, Start_Date, End_Date, Total_Rent) VALUES (?, ?, ?, ?, ?)";
	            try (PreparedStatement pstmt = conn.prepareStatement(insertBooking)) {
	                pstmt.setString(1, name);
	                pstmt.setInt(2, busNo);
	                pstmt.setDate(3, new java.sql.Date(startDate.getTime()));
	                pstmt.setDate(4, new java.sql.Date(endDate.getTime()));
	                pstmt.setDouble(5, totalRent);
	                pstmt.executeUpdate();

	                markBusUnavailable(conn, busNo);
	                System.out.println("Successfully booked! Total Rent: Rs. " + totalRent);
	            }
	            catch(Exception e) {
	            	e.printStackTrace();
	            }
	        } else {
	            System.out.println("Bus is not available for the selected dates.");
	        }
	 }
	        private static long calculateDays(Date startDate, Date endDate) {
	            long difference = endDate.getTime() - startDate.getTime();
	            return difference / (1000 * 60 * 60 * 24) + 1;
	        }

	        private static boolean isBusAvailable(Connection conn, int busNo, Date startDate, Date endDate) throws SQLException {
	            String query = "SELECT * FROM bookings WHERE Bus_No = ? AND (Start_Date <= ? AND End_Date >= ?)";
	            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                pstmt.setInt(1, busNo);
	                pstmt.setDate(2, new java.sql.Date(endDate.getTime()));
	                pstmt.setDate(3, new java.sql.Date(startDate.getTime()));

	                try (ResultSet rs = pstmt.executeQuery()) {
	                    if (rs.next()) {
	                        return false;
	                    }
	                }
	            }

	            String busQuery = "SELECT Bus_Available FROM buses WHERE Bus_No = ?";
	            try (PreparedStatement pstmt = conn.prepareStatement(busQuery)) {
	                pstmt.setInt(1, busNo);
	                try (ResultSet rs = pstmt.executeQuery()) {
	                    if (rs.next() && rs.getBoolean("Bus_Available")) {
	                        return true;
	                    }
	                }
	            }
	            return false;
	        }

	        private static double calculateRent(Connection conn, int busNo, long days) throws SQLException {
	            String query = "SELECT Base_Price FROM buses WHERE Bus_No = ?";
	            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                pstmt.setInt(1, busNo);
	                try (ResultSet rs = pstmt.executeQuery()) {
	                    if (rs.next()) {
	                        return rs.getDouble("Base_Price") * days;
	                    }
	                }
	            }
	            return 0;
	        }

	        private static void markBusUnavailable(Connection conn, int busNo) throws SQLException {
	            String updateBus = "UPDATE buses SET Bus_Available = FALSE WHERE Bus_No = ?";
	            try (PreparedStatement pstmt = conn.prepareStatement(updateBus)) {
	                pstmt.setInt(1, busNo);
	                pstmt.executeUpdate();
	            }
	        }
	        private static boolean busRented(Connection conn, int busNo) throws SQLException {
	            String query = "SELECT Bus_Available FROM buses WHERE Bus_No = ?";
	            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                pstmt.setInt(1, busNo);
	                try (ResultSet rs = pstmt.executeQuery()) {
	                    if (rs.next()) {
	                        return !rs.getBoolean("Bus_Available");
	                    }
	                }
	            }
	            return false;
	        }
	        private static void returnBus(Connection conn, Scanner sc) throws SQLException {
	            System.out.println("Enter the Bus No: ");
	            int busNo = sc.nextInt();

	            if (busRented(conn, busNo)) {
	                String updateBus = "UPDATE buses SET Bus_Available = TRUE WHERE Bus_No = ?";
	                try (PreparedStatement pstmt = conn.prepareStatement(updateBus)) {
	                    pstmt.setInt(1, busNo);
	                    pstmt.executeUpdate();
	                    System.out.println("Bus returned successfully.");
	                }
	            } else {
	                System.out.println("Invalid Bus No or Bus is already available.");
	            }
	        }
	
}