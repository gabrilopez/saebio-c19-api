package org.saebio.sample;

import org.saebio.utils.Utils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SampleService {
    private static final Map<String, Sample> cache = new HashMap<>();
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
                    "(registryDate, patientName, patientSurname, birthDate, NHC, petition, service, criteria, resultPCR, resultTMA, sex, age, origin, reason, variant, lineage, episode)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
            preparedStatement.setString(15, sample.getVariant());
            preparedStatement.setString(16, sample.getLineage());
            preparedStatement.setInt(17, sample.getEpisode());

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
            sample.setVariant(resultSet.getString("variant"));
            sample.setLineage(resultSet.getString("lineage"));
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


    public static Sample handleSampleLine(String line) {
        String[] data = line.split(";");
        return createSampleFromLine(data);
    }
    private static Sample createSampleFromLine(String[] line) {
        // Length 8 mínima por ahora porque los campos resultadoTMA, sexo, edad, procedencia y motivo
        // pueden no estar seteados
        if (line.length < 9) return null;
        Sample sample = new Sample();
        try {
            sample.setRegistryDate(LocalDate.parse(line[0].split(" ")[0], Utils.dateTimeFormatter));
            sample.setPatientName(line[1]);
            sample.setPatientSurname(line[2]);
            sample.setBirthDate(LocalDate.parse(line[3], Utils.dateTimeFormatter));
            sample.setNHC(line[4]);
            sample.setPetition(Integer.parseInt(line[5]));
            sample.setService(line[6]);
            sample.setCriteria(line[7]);
            if (!line[8].trim().isEmpty()) sample.setResultPCR(line[8]);

            // El hospital doctor negrín está trabajando en implementar estos campos
            if (line.length > 9 && !line[9].trim().isEmpty()) sample.setResultTMA(line[9]);
            if (line.length > 10) sample.setSex(!line[10].trim().isEmpty() ? line[10] : null);
            if (line.length > 11) sample.setAge(Utils.isNumeric(line[11]) ? Integer.valueOf(line[11]) : null);
            if (line.length > 12) sample.setOrigin(!line[12].trim().isEmpty() ? line[12] : null);
            if (line.length > 13) sample.setReason(!line[13].trim().isEmpty() ? line[13] : null);
            if (line.length > 14) sample.setVariant(!line[14].trim().isEmpty() ? line[14] : null);
            if (line.length > 15) sample.setLineage(!line[15].trim().isEmpty() ? line[15] : null);
            sample.setEpisode(getSampleEpisodeNumber(sample));
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        updateSampleCache(sample);
        return sample;
    }

    private static int getSampleEpisodeNumber(Sample newSample) {
        // Old sample will always be the first sample from current episode
        String NHC = newSample.getNHC();
        Sample oldSample = cache.getOrDefault(NHC, null);

        if (oldSample == null) {
            SampleService sampleService = new SampleService();
            oldSample = sampleService.getFirstSampleFromCurrentEpisode(NHC);
            if (oldSample == null) return 1;
            // Save old sample found in database to cache
            cache.put(NHC, oldSample);
        }
        return newSample.belongToSameEpisode(oldSample) ? oldSample.getEpisode() : oldSample.getEpisode() + 1;
    }

    private static void updateSampleCache(Sample newSample) {
        String NHC = newSample.getNHC();
        Sample oldSample = cache.getOrDefault(NHC, null);
        if (oldSample == null || oldSample.getEpisode() < newSample.getEpisode()) {
            cache.put(NHC, newSample);
        }
    }

    public static void clearCache() {
        cache.clear();
        System.out.println("CACHE LENGTH IS:" + cache.size());
    }
}