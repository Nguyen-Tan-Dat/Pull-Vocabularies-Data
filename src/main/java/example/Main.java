package example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/sua_bot_vinamink";
        String username = "root";
        String password = "123456";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        HashMap<String, String> dtProvinces = new HashMap<>();
        try {
            // Kết nối đến cơ sở dữ liệu
            connection = DriverManager.getConnection(url, username, password);

            // Tạo một đối tượng Statement để thực thi truy vấn
            statement = connection.createStatement();

            // Thực hiện truy vấn SQL để đọc dữ liệu từ bảng
            String sqlQuery = "SELECT * FROM vsb_20_province";
            resultSet = statement.executeQuery(sqlQuery);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("_name");
                String code = resultSet.getString("_code");
                dtProvinces.put(name, code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        API api = new API("https://provinces.open-api.vn/api/?depth=3");
        var array = api.getJsonArray();
        int idProvince = 1;
        int idDistrict = 1;
        int idWard = 1;
        assert array != null;
        String queries = "";
        for (Object province : array) {
            String provinceName = ((JSONObject) province).get("name").toString().replace("'", "''");
            String provinceCode = "";
            for (String key : dtProvinces.keySet()) {
                if (provinceName.replaceAll(" -", "").replaceAll(" ", "").contains(key.replaceAll(" ", ""))) {
                    provinceCode = dtProvinces.get(key);
                    break;
                }
            }
            if (provinceCode.equals("")) System.out.println(provinceName);
            queries += "INSERT INTO vsb_20_province VALUE (" + idProvince + ",'" + provinceName + "','" + provinceCode + "');\n";
            JSONArray jsonDistricts = (JSONArray) ((JSONObject) province).get("districts");
            for (Object district : jsonDistricts) {
                String districtName = ((JSONObject) district).get("name").toString().replace("'", "''");
                String districtPrefix=capitalizeFirstLetter(((JSONObject) district).get("division_type").toString());
                districtName=districtName.replaceAll(districtPrefix+" ","");
                queries += "INSERT INTO vsb_20_district VALUE (" + idDistrict + ",'" + districtName + "','"+districtPrefix+"'," + idProvince + ");\n";
                JSONArray jsonWards = (JSONArray) ((JSONObject) district).get("wards");
                for (Object ward : jsonWards) {
                    String wardName = ((JSONObject) ward).get("name").toString().replace("'", "''");
                    String wardPrefix=capitalizeFirstLetter(((JSONObject) ward).get("division_type").toString());
                    wardName=wardName.replaceAll(wardPrefix+" ","");
                    queries+="INSERT INTO vsb_20_ward VALUE ("+idWard+",'"+wardName+"','"+wardPrefix+"',"+idProvince+","+idDistrict+");\n";
                    idWard++;
                }
                idDistrict++;
            }
            idProvince++;
        }
        String fileName = "insert.sql";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(queries);
            writer.close();
            System.out.println("File '" + fileName + "' has been written.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String capitalizeFirstLetter(String input) {
        String firstLetter = input.substring(0, 1).toUpperCase();
        String restOfTheString = input.substring(1);
        return firstLetter + restOfTheString;
    }
}
