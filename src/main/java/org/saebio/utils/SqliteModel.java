package org.saebio.utils;

import org.saebio.sample.Sample;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;

public class SqliteModel implements DatabaseModel {
    private static Connection connection;
    private static String databaseFileName;
    private static String databaseRoute;
    private final String connectionUrl;

    public SqliteModel(String databaseFileRoute) {
        int lastSlashPosition = databaseFileRoute.lastIndexOf(File.separatorChar);
        databaseFileName = databaseFileRoute.substring(lastSlashPosition + 1);
        this.connectionUrl = "jdbc:sqlite:" + databaseFileRoute;
        databaseRoute = databaseFileRoute.substring(0, lastSlashPosition + 1);
        System.out.println("CONNECTION URL: " + connectionUrl);
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private boolean validateDatabaseFile(String route) {
        File file = new File(route);

        int extensionIndex = file.getName().lastIndexOf(".");
        return extensionIndex != -1 && ((file.getName()).substring(extensionIndex).equals(".db"));
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(connectionUrl);
                return connection;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return connection;
    }
    @Override
    public boolean testConnection() {
        try{
            if (!validateDatabaseFile(databaseRoute + databaseFileName)) {
                System.out.println("Invalid database file");
                return false;
            }
            this.getConnection().prepareStatement("SELECT 1 FROM Samples").executeQuery();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        } finally {
            closeConnection();
        }
        return true;
    }

    @Override
    public InsertStatus addSample(Sample sample) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Samples " +
                    "(registryDate, patientName, patientSurname, birthDate, NHC, petition, service, criteria, resultPCR, resultTMA, sex, age, origin, reason, variant, lineage, episode, normalizedResult)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setObject(1, sample.getRegistryDate());
            preparedStatement.setString(2, sample.getPatientName());
            preparedStatement.setString(3, sample.getPatientSurname());
            preparedStatement.setObject(4, sample.getBirthDate());
            preparedStatement.setString(5, sample.getNHC());
            preparedStatement.setString(6, sample.getPetition());
            preparedStatement.setString(7, sample.getService());
            preparedStatement.setString(8, sample.getCriteria());
            preparedStatement.setString(9, sample.getResultPCR());
            preparedStatement.setString(10, sample.getResultTMA());
            preparedStatement.setString(11, sample.getSex());
            preparedStatement.setObject(12, sample.getAge(), Types.INTEGER);
            preparedStatement.setString(13, sample.getOrigin());
            preparedStatement.setString(14, sample.getReason());
            preparedStatement.setString(15, sample.getVariant());
            preparedStatement.setString(16, sample.getLineage());
            preparedStatement.setInt(17, sample.getEpisode());
            preparedStatement.setString(18, sample.getNormalizedResult());

            preparedStatement.execute();
        } catch (SQLException e) {
            String message = e.getMessage();
            return message.contains("UNIQUE constraint failed") ? InsertStatus.SAMPLE_ALREADY_EXISTS : InsertStatus.SAMPLE_INSERT_ERROR;
        } finally {
            closeConnection();
        }
        return InsertStatus.SAMPLE_INSERTED_SUCCESSFULLY;
    }

    @Override
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

    @Override
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

    @Override
    public boolean updateSampleReasonLineageAndVariant(Sample sample) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("" +
                    "UPDATE Samples SET variant = ?, lineage = ?, reason = ? WHERE NHC = ? AND petition = ?");

            preparedStatement.setString(1, sample.getVariant());
            preparedStatement.setString(2, sample.getLineage());
            preparedStatement.setString(3, sample.getReason());
            preparedStatement.setString(4, sample.getNHC());
            preparedStatement.setString(5, sample.getPetition());
            preparedStatement.execute();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }
    }

    public boolean vacuumInto(String backupRoute) {
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate("VACUUM INTO '" + backupRoute + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }
        return true;
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
            sample.setPetition(resultSet.getString("petition"));
            sample.setService(resultSet.getString("service"));
            sample.setCriteria(resultSet.getString("criteria"));
            sample.setCriteria(resultSet.getString("resultPCR"));
            sample.setCriteria(resultSet.getString("resultTMA"));
            sample.setSex(resultSet.getString("sex"));
            sample.setAge(getInteger(resultSet, "age"));
            sample.setOrigin(resultSet.getString("origin"));
            sample.setReason(resultSet.getString("reason"));
            sample.setVariant(resultSet.getString("variant"));
            sample.setLineage(resultSet.getString("lineage"));
            sample.setEpisode(resultSet.getInt("episode"));
            sample.setNormalizedResult(resultSet.getString("normalizedResult"));
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

    public static String getDatabaseFileName() {
        return databaseFileName;
    }

    public static String getDatabaseRoute() {
        return databaseRoute;
    }
}
