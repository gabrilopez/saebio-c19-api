package org.saebio.sample;

import org.saebio.api.HttpStatus;

import javax.xml.transform.Result;
import java.sql.*;
import java.time.LocalDate;

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
                    "(petition, registryDate, hospital, hospitalService, destination, prescriptor, NHC, patient, sex, age, birthDate, month, year, type, result, episode)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, sample.getPetition());
            preparedStatement.setObject(2, sample.getRegistryDate());
            preparedStatement.setString(3, sample.getHospital());
            preparedStatement.setString(4, sample.getHospitalService());
            preparedStatement.setString(5, sample.getDestination());
            preparedStatement.setString(6, sample.getPrescriptor());
            preparedStatement.setString(7, sample.getNHC());
            preparedStatement.setString(8, sample.getPatient());
            preparedStatement.setString(9, sample.getSex());
            preparedStatement.setObject(10, sample.getAge(), Types.INTEGER);
            preparedStatement.setObject(11, sample.getBirthDate());
            preparedStatement.setInt(12, sample.getMonth());
            preparedStatement.setInt(13, sample.getYear());
            preparedStatement.setString(14, sample.getType());
            preparedStatement.setString(15, sample.getResult());
            preparedStatement.setInt(16, sample.getEpisode());
            // TODO: Add episode number
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

    public ResultSet getSamplesByNHC(String NHC) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Samples WHERE NHC = ?");
            preparedStatement.setString(1, NHC);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getString("NHC"));
                System.out.println(resultSet.getString("result"));
                System.out.println(resultSet.getTimestamp("registryDate"));
                System.out.println("\n\n");
            }
            return resultSet;
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
            sample.setPetition(resultSet.getInt("petition"));
            sample.setRegistryDate(LocalDate.parse((CharSequence) resultSet.getDate("registryDate")));
            sample.setHospital(resultSet.getString("hospital"));
            sample.setHospitalService(resultSet.getString("hospitalService"));
            sample.setDestination(resultSet.getString("destination"));
            sample.setPrescriptor(resultSet.getString("prescriptor"));
            sample.setNHC(resultSet.getString("NHC"));
            sample.setPatient(resultSet.getString("patient"));
            sample.setSex(resultSet.getString("sex"));
            sample.setAge(getInteger(resultSet, "age")); // Handle possible NULL value
            sample.setBirthDate(LocalDate.parse((CharSequence) resultSet.getDate("birthDate")));
            sample.setMonth(resultSet.getInt("month"));
            sample.setYear(resultSet.getInt("year"));
            sample.setType(resultSet.getString("type"));
            sample.setResult(resultSet.getString("result"));
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


}