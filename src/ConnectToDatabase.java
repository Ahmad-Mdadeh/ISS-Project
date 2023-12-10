import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class ConnectToDatabase {
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
        return "connection error";
    }

    public String Login(String name, String pass) {
        Statement stmtement = null;
        Connection connection = this.connect();
        if (connection != null) {
            try {
                stmtement = (Statement) connection.createStatement();

                ResultSet user = stmtement.executeQuery("SELECT * FROM `user` WHERE name=\"" + name + "\" ");
                if (user.next()) {
                    // System.out.println(pass + " " + user.getString("password"));
                    if (VerifyingPasswords.validatePassword(pass, user.getString("pass")))
                        return "Login Successful !!! " + user.getInt("id") + " : " + user.getString("name");
                    else
                        return "Wrong Password ";
                } else
                    return "user not found ";
            } catch (SQLException throwables) {
                throwables.printStackTrace();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }

        return "connection error";
    }
} // class ends
