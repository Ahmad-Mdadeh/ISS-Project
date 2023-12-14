import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class ConnectToDatabase {
    static int id = 0;

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
        String result = "error";
        try (
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM `user` WHERE name=?")) {

            preparedStatement.setString(1, name);
            ResultSet user = preparedStatement.executeQuery();

            if (user.next()) {
                result = "Username already taken";
            } else {
                result = "Username available";
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public String login(String name, String pass) {

        try (Connection connection = this.connect();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `user` WHERE name=?")) {

            statement.setString(1, name);

            try (ResultSet user = statement.executeQuery()) {
                if (user.next()) {
                    id = user.getInt("id");
                    if (VerifyingPasswords.validatePassword(pass, user.getString("pass"))) {
                        return "Login Successful !!! " + user.getInt("id") + " : " + user.getString("name");
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

    public String Signup(String name, String pass) {
        Statement stmtement = null;
        Connection connection = this.connect();
        String id = "-1";
        if (connection != null) {
            try {
                String availability = username(name, connection);
                if (!availability.equals("Username available")) {
                    return availability;
                }

                pass = VerifyingPasswords.hashedPassword(pass);
                stmtement = (Statement) connection.createStatement();
                stmtement.executeUpdate(
                        "INSERT INTO user (name,pass) VALUES ( '" + name + "','" + pass + "')",
                        Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmtement.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getInt(1) + " : " + name;
                }

            } catch (SQLException throwable) {
                throwable.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
            return "SignUp Successful !!! " + id;
        }
        return "connection error !!! ";
    }

    public String updateInformation(String newPhone, String newAddress, String newAge) {
        try (Connection connection = this.connect();
                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE user SET phone=?, address=?, age=? WHERE id=?");
                PreparedStatement selectStatement = connection.prepareStatement(
                        "SELECT * FROM `user` WHERE id=?")) {

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
                        return "Update Information Successful !!! " + "id : " + id +
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

} // class ends
