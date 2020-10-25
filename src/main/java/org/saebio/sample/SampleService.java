package org.saebio.sample;

import org.saebio.api.HttpStatus;

import java.sql.*;

public class SampleService {
    Connection conn = null;
    String url = "jdbc:mysql://localhost:8889/metabase?serverTimezone=UTC&autoReconnect=true";
    String user = "root";
    String password = "root";

    public boolean tryConnection() {
        try{
            Connection conn = this.getConnection();
            if (conn != null) return true;
        } catch (SQLException throwables) {
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
            }
        }
        return conn;
    }

    private void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
            }
        }
    }

    public int addSample(Sample sample) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Samples " +
                    "(petition, registryDate, hospital, hospitalService, destination, prescriptor, NHC, patient, sex, age, birthDate, month, year, type, result)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, sample.getPetition());
            preparedStatement.setObject(2, sample.getRegistryDate());
            preparedStatement.setString(3, sample.getHospital());
            preparedStatement.setString(4, sample.getHospitalService());
            preparedStatement.setString(5, sample.getDestination());
            preparedStatement.setString(6, sample.getPrescriptor());
            preparedStatement.setString(7, sample.getNHC());
            preparedStatement.setString(8, sample.getPatient());
            preparedStatement.setString(9, sample.getSex());
            preparedStatement.setString(10, sample.getAge());
            preparedStatement.setObject(11, sample.getBirthDate());
            preparedStatement.setInt(12, sample.getMonth());
            preparedStatement.setInt(13, sample.getYear());
            preparedStatement.setString(14, sample.getType());
            preparedStatement.setString(15, sample.getResult());

            preparedStatement.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
        } catch (SQLException e) {
            System.out.println("Error with petition [" + sample.getPetition() + "]. Check this entry's data");
            return HttpStatus.BadRequest();
        } finally {
            closeConnection();
        }
        return HttpStatus.OK();
    }

    public void getSample(Sample sample) {

    }
}