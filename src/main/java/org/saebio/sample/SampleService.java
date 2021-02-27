package org.saebio.sample;

import org.saebio.api.HttpStatus;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class SampleService {
    private static Connection conn = null;
    // String url = "jdbc:mysql://localhost:8889/metabase?serverTimezone=UTC&autoReconnect=true";
    // TODO: Cambiar ruta del archivo. Usar File separator para especificar la ruta
        // File separator sirve para añadir las / o \ dependiendo del OS
    static String databaseFileName = "metabase.db";
    static String databaseRoute = "/Users/gabriellopez/Desktop/sqlite/";
    static String backupsRoute = "backups/";
    String url = "jdbc:sqlite:" + databaseRoute + databaseFileName;
    String user = "root";
    String password = "root";

    public boolean tryConnection() {
        try{
            Connection conn = this.getConnection();
            if (conn != null) return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return false;
    }

    private Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                conn = DriverManager.getConnection(url, user, password);
                return conn;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return conn;
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
            }
        }
    }

    public boolean addSample(Sample sample) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Samples " +
                    "(registryDate, patientName, patientSurname, birthDate, NHC, petition, service, criteria, resultPCR, resultTMA, sex, age, origin, reason, episode)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setObject(1, sample.getRegistryDate());
            preparedStatement.setString(2, sample.getPatientName());
            preparedStatement.setString(3, sample.getPatientSurname());
            preparedStatement.setObject(4, sample.getBirthDate());
            preparedStatement.setString(5, sample.getNHC());
            preparedStatement.setInt(6, sample.getPetition());
            preparedStatement.setString(7, sample.getService());
            preparedStatement.setString(8, sample.getCriteria());
            preparedStatement.setString(9, sample.getResultPCR());
            preparedStatement.setString(10, sample.getResultTMA());
            preparedStatement.setString(11, sample.getSex());
            preparedStatement.setObject(12, sample.getAge(), Types.INTEGER);
            preparedStatement.setString(13, sample.getOrigin());
            preparedStatement.setString(14, sample.getReason());
            preparedStatement.setInt(15, sample.getEpisode());

            preparedStatement.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
        } catch (SQLException e) {
            // e.printStackTrace();
            System.out.println("Error with petition [" + sample.getPetition() + "]. Check this entry's data");
            return false;
        } finally {
            closeConnection();
        }
        return true;
    }

    public ResultSet getSamplesByNHC(String NHC) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Samples WHERE NHC = ?");
            preparedStatement.setString(1, NHC);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            return null;
        } finally {
            closeConnection();
        }
    }

    public Sample getFirstSampleFromCurrentEpisode(String NHC) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                "SELECT * FROM Samples WHERE NHC = ? AND episode = (SELECT MAX(episode) from Samples WHERE NHC = ?) " +
                        "ORDER BY registryDate ASC LIMIT 1"
            );
            preparedStatement.setString(1, NHC);
            preparedStatement.setString(2, NHC);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return transformRowToSample(resultSet);
            }
            return null;
        } catch(SQLException e) {
            return null;
        } finally {
            closeConnection();
        }
    }

    private Sample transformRowToSample(ResultSet resultSet) {
        Sample sample = new Sample();
        try {
            if (!resultSet.next()) return null;
            sample.setRegistryDate(LocalDate.parse((CharSequence) resultSet.getDate("registryDate")));
            sample.setPatientName(resultSet.getString("patientName"));
            sample.setPatientSurname(resultSet.getString("patientSurname"));
            sample.setBirthDate(LocalDate.parse((CharSequence) resultSet.getDate("birthDate")));
            sample.setNHC(resultSet.getString("NHC"));
            sample.setPetition(resultSet.getInt("petition"));
            sample.setService(resultSet.getString("service"));
            sample.setCriteria(resultSet.getString("criteria"));
            sample.setCriteria(resultSet.getString("resultPCR"));
            sample.setCriteria(resultSet.getString("resultTMA"));
            sample.setSex(resultSet.getString("sex"));
            sample.setAge(getInteger(resultSet, "age"));
            sample.setOrigin(resultSet.getString("origin"));
            sample.setReason(resultSet.getString("reason"));
            sample.setEpisode(resultSet.getInt("episode"));
        } catch (SQLException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sample;
    }

    private Integer getInteger(ResultSet resultSet, String columnLabel) throws SQLException {
        int value = resultSet.getInt(columnLabel);
        return resultSet.wasNull() ? null : value;
    }

    public int getRowCount() {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "SELECT count(*) as row_count FROM Samples"
            );
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt("row_count");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnection();
        }
        return 0;
    }

    public void vacuum() {
        try {
            Statement statement = getConnection().createStatement();
            // Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement or an SQL statement
            // that returns nothing, such as an SQL DDL statement.
            statement.executeUpdate("VACUUM");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public boolean vacuumInto() {
        try {
            String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss").format(new Date());
            String route = databaseRoute + backupsRoute + getRowCount() + " " + timeStamp + ".db";
            Statement statement = getConnection().createStatement();
            // Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement or an SQL statement
            // that returns nothing, such as an SQL DDL statement.
            statement.executeUpdate("VACUUM INTO '" + route + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }
        return true;
    }

    public static String getDatabaseRoute() {
        return databaseRoute;
    }

    public static String getDatabaseFileName() {
        return databaseFileName;
    }

    public static String getBackupsRoute() { return backupsRoute; }
}