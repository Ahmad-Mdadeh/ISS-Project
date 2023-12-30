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

    public String login(String name, String pass, String nationalNumber) {

        

        try (Connection connection = this.connect();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE name=?")) {
                
            statement.setString(1, name);

            try (ResultSet user = statement.executeQuery()) {
                if (user.next()) {
                    if (VerifyingPasswords.validatePassword(pass, user.getString("pass"))&&
                    nationalNumber.equals(user.getString("nationalNumber"))) {
                        id = user.getInt("id");
                        nationalNumber = user.getString("nationalNumber");
                        permissions = user.getString("permissions");
                        return "Login Successful !!! " + permissions + "! " + nationalNumber + "! " + user.getInt("id")
                                + " : "
                                + user.getString("name");
                    } else {
                        return "Wrong Password or Wrong nationalNumber";
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
        try (Connection connection = this.connect()) {
            if (connection != null) {
                String availability = username(name, connection);
                if (!availability.equals("Username available")) {
                    return availability;
                }

                pass = VerifyingPasswords.hashedPassword(pass);
                // Use PreparedStatement to prevent SQL injection
                String insertQuery = "INSERT INTO users (name, pass,nationalNumber) VALUES (?, ?,?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery,
                        Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, name);
                    preparedStatement.setString(2, pass);
                    preparedStatement.setString(3, nationalNumber);

                    preparedStatement.executeUpdate();

                    try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
                return "SignUp Successful !!! " + permissions + "! " + id;
            }
            return "Connection error !!! ";
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return "SignUp failed due to an unexpected error.";
        }

    }

    public String updateInformation(String newPhone, String newAddress, String newAge) {
        try (Connection connection = this.connect();
                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE users SET phone=?, address=?, age=? WHERE id=?");
                PreparedStatement selectStatement = connection.prepareStatement(
                        "SELECT * FROM `users` WHERE id=?")) {

            // Update user information
            updateStatement.setString(1, newPhone);
            updateStatement.setString(2, newAddress);
            updateStatement.setString(3, newAge);
            updateStatement.setInt(4, id);

            int affectedRows = updateStatement.executeUpdate();

            if (affectedRows > 0) {
                // If update was successful, retrieve updated user information
                selectStatement.setInt(1, id);
                try (ResultSet updatedUser = selectStatement.executeQuery()) {
                    if (updatedUser.next()) {
                        return "Update Information Successful !!! " + permissions + "! " + "id : " + id +
                                ", Name : " + updatedUser.getString("name") +
                                ", Phone : " + updatedUser.getString("phone") +
                                ", Address : " + updatedUser.getString("address") +
                                ", Age : " + updatedUser.getString("age");
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

    public String setPracticalProjects(String project) {
        try (Connection connection = this.connect();
                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE users SET practicalprojects=? WHERE id=?");
                PreparedStatement selectStatement = connection.prepareStatement(
                        "SELECT * FROM `users` WHERE id=?")) {

            // Update user information
            updateStatement.setString(1, project);
            updateStatement.setInt(2, id);

            int affectedRows = updateStatement.executeUpdate();

            if (affectedRows > 0) {
                // If update was successful, retrieve updated user information
                selectStatement.setInt(1, id);
                try (ResultSet updatedUser = selectStatement.executeQuery()) {
                    if (updatedUser.next()) {
                        return "Update Information Successful !!! " + "id : " + id +
                                ", Project And Description: " + updatedUser.getString("practicalprojects");
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

    public String setMark(String name, String mark) {
        try (Connection connection = this.connect();
                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE users SET mark=? WHERE name=?");
                PreparedStatement selectStatement = connection.prepareStatement(
                        "SELECT * FROM `users` WHERE name=?")) {

            // Update user information
            updateStatement.setString(1, mark); // Set the mark in the first placeholder
            updateStatement.setString(2, name); // Set the name in the second placeholder

            int affectedRows = updateStatement.executeUpdate();

            if (affectedRows > 0) {
                // If update was successful, retrieve updated user information
                selectStatement.setString(1, name);
                try (ResultSet updatedUser = selectStatement.executeQuery()) {
                    if (updatedUser.next()) {
                        return "Set Mark Successful !!! " +
                                "Name : " + updatedUser.getString("name") +
                                ", Project Mark's: " + updatedUser.getString("mark");
                    } else {
                        return "No user found with Name : " + name;
                    }
                }
            } else {
                return "No user found with Name : " + name;
            }

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return "Update Information failed for user : " + name;
        }
    }

}