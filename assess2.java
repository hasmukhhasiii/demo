package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;


public class Studentdata {
    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\HASMUKH KHAN S\\Desktop\\javacode1/Book.xlsx";
        String jsonFilePath = "output.json";
        String url = "jdbc:mysql://localhost:3306/marklist";
        String user = "root";
        String password = "123456";
        Scanner scanner = new Scanner(System.in);
        System.out.println("ENTER YOUR ADMISSION NUMBER OR NAME :");
        String searchValue = scanner.nextLine().trim();

        // SQL query for searching based on Admission Number or Name
        String sql = "SELECT * FROM STUDENT WHERE `ADMISSION_NUMBER ` = ? OR `NAME` = ?";
        excelToJason( excelFilePath, jsonFilePath);
        jsonToMySQL(jsonFilePath,url,user,password);

        try {
            // Create a connection to the MySQL database
            Connection conn = DriverManager.getConnection(url, user, password);

            // Prepare the SQL query
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchValue);  // Set the search value for Admission Number
            stmt.setString(2, searchValue);  // Set the search value for Name

            // Execute the query and get the result set
            ResultSet rs = stmt.executeQuery();

            // Check if the student is found
            if (rs.next()) {
                // Map the result set to JSON format using Jackson
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode studentJson = objectMapper.createObjectNode();

                // Add student details to JSON node
                studentJson = objectMapper.createObjectNode()
                        .put("NAME", rs.getString("NAME"))
                        .put("ADMISSION NO ", rs.getString("ADMISSION NO "))
                        .put("MARKS - PHYSICS", rs.getDouble("MARKS - PHYSICS"))
                        .put("MARKS - CHEMISTRY", rs.getDouble("MARKS - CHEMISTRY"))
                        .put("MARKS - MATHS", rs.getDouble("MARKS - MATHS"));

                // Print the student details in JSON format
                System.out.println("Student Details in JSON format: ");
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(studentJson));
            } else {
                // If no student is found, inform the user
                System.out.println("No student found with the provided Admission Number or Name.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//        String valueToSearch = scanner.nextLine().trim();
//        String sql = "SELECT * FROM STUDENT WHERE NAME=? OR ADMISSION_NUMBER=?";
//        excelToJason( excelFilePath, jsonFilePath);
//        jsonToMySQL(jsonFilePath,url,user,password);
//        try {
//
//            // Create a connection to the MySQL database
//            Connection conn = DriverManager.getConnection(url, user, password);
//
//            // Prepare the SQL query
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            stmt.setString(2, valueToSearch);  // Set the search value for Admission Number
//            stmt.setString(1, valueToSearch);  // Set the search value for Name
//
//            // Execute the query and get the result set
//            ResultSet rs = stmt.executeQuery();
//
//            // Check if the student is found
//            if (rs.next()) {
//                // Map the result set to JSON format using Jackson
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode studentJson = objectMapper.createObjectNode();
//
//                // Add student details to JSON node
//                studentJson = objectMapper.createObjectNode()
//                        .put("NAME", rs.getString("NAME"))
//                        .put("ADMISSION_NUMBER ", rs.getString("ADMISSION_NUMBER "))
//                        .put("MARKS_PHYSICS", rs.getDouble("MARKS_PHYSICS"))
//                        .put("MARKS_CHEMISTRY", rs.getDouble("MARKS_CHEMISTRY"))
//                        .put("MARKS_MATHS", rs.getDouble("MARKS_MATHS"));
//
//                // Print the student details in JSON format
//                System.out.println("Student Details in JSON format: ");
//                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(studentJson));
//            } else {
//                // If no student is found, inform the user
//                System.out.println("No student found with the provided Admission Number or Name.");
//            }
//        } catch (SQLException e) {
//            System.out.println("Error connecting to database: " + e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
    public static void excelToJason(String excelFilePath,String jsonFilePath){

        try {
            // Load the Excel file
            FileInputStream fileInputStream = new FileInputStream(excelFilePath);
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

            // Create a list to hold the rows
            List<Map<String, String>> rows = new ArrayList<>();

            // Read the headers (first row) to use as keys for the JSON
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) { // Start from row 1, skipping the header
                Row row = sheet.getRow(i);
                Map<String, String> rowData = new LinkedHashMap<>();//making it in order

                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = cell != null ? cell.toString() : "";
                    rowData.put(headers.get(j), cellValue);
                }

                rows.add(rowData);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String jsonString = objectMapper.writeValueAsString(rows);

            FileWriter fileWriter = new FileWriter(jsonFilePath);
            fileWriter.write(jsonString);
            fileWriter.close();

            System.out.println("Excel data has been successfully converted to JSON and saved at: " + jsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void jsonToMySQL(String jsonFilePath,String url,String user,String password)
    {
        System.out.println(url);
        ObjectMapper objectMapper = new ObjectMapper();

        try
        {   Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            // Prepare the SQL query for inserting data
            String sql = "INSERT INTO STUDENT (name, admission_number, marks_physics, marks_chemistry, marks_maths) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            // Read the JSON file and convert it into a JsonNode (array of students)
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            // Loop through each student record in the JSON array
            for (JsonNode studentNode : rootNode) {
                // Extract individual fields from the JSON object
                String name = studentNode.get("NAME").asText();
                String admissionNo = studentNode.get("ADMISSION NO ").asText();
                double marksPhysics = studentNode.get("MARKS - PHYSICS").asDouble();
                double marksChemistry = studentNode.get("MARKS - CHEMISTRY").asDouble();
                double marksMaths = studentNode.get("MARKS - MATHS").asDouble();

                // Set values in the PreparedStatement
                stmt.setString(1, name);
                stmt.setString(2, admissionNo);
                stmt.setDouble(3, marksPhysics);
                stmt.setDouble(4, marksChemistry);
                stmt.setDouble(5, marksMaths);

                // Execute the SQL query to insert the data into the table
                stmt.executeUpdate();
            }

            System.out.println("Data has been successfully inserted into the database.");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
