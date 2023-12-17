import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class ConnectToDatabase {
    public static int id = 0;
    public static String permissions = "";
    public static String nationalNumber = "";

    // function connect to DataBase
    public Connection connect() {
        String MySQLURL = "jdbc:mysql://127.0.0.1:3306/iss";
        String databseUserName = "root";
        String databasePassword = "";
        Connection connection = null;

        try {

            connection = DriverManager.getConnection(MySQLURL, databseUserName, databasePassword);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (connection != null) {
            System.out.println("Database connection is successful !!!!");
            return connection;

        }
        return null;
    }

    public String username(String name, Connection connection) {
        String result = "";
        try (
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM `users` WHERE name=?")) {

            preparedStatement.setString(1, name);
            ResultSet user = preparedStatement.executeQuery();

            if (user.next()) {
                result = "Username already exite";
            } else {
                result = "Username available";
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public String checkPermissions(int id, Connection connection) {
        String permissions = "";
        try (
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM `users` WHERE id=?")) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                permissions = resultSet.getString("permissions");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println(permissions);
        return permissions;
    }

    public String login(String name, String pass, String nationalNumber) {

        try (Connection connection = this.connect();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE name=?")) {

            statement.setString(1, name);

            try (ResultSet user = statement.executeQuery()) {
                if (user.next()) {
                    if (VerifyingPasswords.validatePassword(pass, user.getString("pass"))) {
                        id = user.getInt("id");
                        nationalNumber = user.getString("nationalNumber");
                        permissions = user.getString("permissions");
                        return "Login Successful !!! " + permissions + "! " +user.getInt("id") + " : " + user.getString("name");
                    } else {
                        return "Wrong Password";
                    }
                } else {
                    return "User not found";
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace(); // Handle the exception appropriately
                return "Login failed due to an unexpected error.";
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace(); // Handle the exception appropriately
            return "Connection error";
        }
    }

    public String signup(String name, String pass, String nationalNumber) {
        if (checkPermissions(id, this.connect()).equals("0")) {
            try (Connection connection = this.connect()) {
                if (connection != null) {
                    String availability = username(name, connection);
                    if (!availability.equals("Username available")) {
                        return availability;
                    }

                    pass = VerifyingPasswords.hashedPassword(pass);
                    // Use PreparedStatement to prevent SQL injection
                    String insertQuery = "INSERT INTO users (name, pass) VALUES (?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery,
                            Statement.RETURN_GENERATED_KEYS)) {
                        preparedStatement.setString(1, name);
                        preparedStatement.setString(2, pass);

                        preparedStatement.executeUpdate();

                        try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                            if (rs.next()) {
                                id = rs.getInt(1);
                            }
                        }
                    }

                    return "SignUp Successful !!! " + id;
                }
                return "Connection error !!! ";
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return "SignUp failed due to an unexpected error.";
            }
        }
        return "Not Permissions";
    }

    public String updateInformation(String newPhone, String newAddress, String newAge, String nationalNumber) {
        try (Connection connection = this.connect();
                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE users SET phone=?, address=?, age=?, nationalNumber=? WHERE id=?");
                PreparedStatement selectStatement = connection.prepareStatement(
                        "SELECT * FROM `users` WHERE id=?")) {

            // Update user information
            updateStatement.setString(1, newPhone);
            updateStatement.setString(2, newAddress);
            updateStatement.setString(3, newAge);
            updateStatement.setString(4, nationalNumber);
            updateStatement.setInt(5, id);

            int affectedRows = updateStatement.executeUpdate();

            if (affectedRows > 0) {
                // If update was successful, retrieve updated user information
                selectStatement.setInt(1, id);
                try (ResultSet updatedUser = selectStatement.executeQuery()) {
                    if (updatedUser.next()) {
                        return "Update Information Successful !!! " + "id : " + id +
                                ", Name : " + updatedUser.getString("name") +
                                ", Phone : " + updatedUser.getString("phone") +
                                ", Address : " + updatedUser.getString("address") +
                                ", Age : " + updatedUser.getString("age") +
                                ", nationalNumber : " + updatedUser.getString("nationalNumber");
                    } else {
                        return "No user found with ID: " + id;
                    }
                }
            } else {
                return "No user found with ID: " + id;
            }

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return "Update Information failed !!! " + "id : " + id;
        }
    }

}