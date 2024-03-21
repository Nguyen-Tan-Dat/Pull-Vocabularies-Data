package example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ExcelReader {
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

        try {
            FileInputStream file = new FileInputStream("Quy_định_cách_ghi_nhận_thông_tin_KH_SĐT_địa_chỉ_DOB_đã_điều_chỉnh.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            HashMap<String, HashMap<String, Set<String>>> provinces = new HashMap<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                String province = row.getCell(0).getStringCellValue();
                String district = row.getCell(1).getStringCellValue();
                String ward = row.getCell(2).getStringCellValue();
                if (!provinces.containsKey(province)) {
                    provinces.put(province, new HashMap<>());
                }
                if (!provinces.get(province).containsKey(district)) {

                    provinces.get(province).put(district, new HashSet<>());
                }
                if (!provinces.get(province).get(district).contains(ward))
                provinces.get(province).get(district).add(ward);
                else{
                    System.out.println( ward+", "+district+", "+province);
                }
            }
            int idProvince = 1;
            int idDistrict = 1;
            int idWard = 1;
            String queries = "";
            for (String province : provinces.keySet()) {
                String provinceCode = "";
                for (String key : dtProvinces.keySet()) {
                    if (province.replaceAll(" -", "").replaceAll(" ", "").contains(key.replaceAll(" ", ""))) {
                        provinceCode = dtProvinces.get(key);
                        break;
                    }
                }
                if (provinceCode.equals("")) System.out.println(province);
                String provinceName = province.replace("'", "''");
                queries += "INSERT INTO vsb_20_province VALUE (" + idProvince + ",'" + provinceName + "','" + provinceCode + "');\n";
                for (String district : provinces.get(province).keySet()) {
//                    String[] dataDistrict = district.split("\\.");
//                    if (dataDistrict.length != 2){
////                        System.out.println("District Length = "+dataDistrict.length+" "+district);
//                        System.out.println(dataDistrict);
//                    }
//                    else {
//                        String districtName = dataDistrict[1];
//                        String districtPrefix="";
//                        if(dataDistrict[0].equals("H")) districtPrefix="Huyện";
//                        if(dataDistrict[0].equals("TP")) districtPrefix="Thành phố";
//                        if(dataDistrict[0].equals("Q")) districtPrefix="Quận";
//                        if(dataDistrict[0].equals("TX")) districtPrefix="Thị xã";
//                        if(districtPrefix.equals("")){
//                            System.out.println(dataDistrict);
//                        }
//                        else {
//                            queries += "INSERT INTO vsb_20_district VALUE (" + idDistrict + ",'" + districtName + "','" + districtPrefix + "'," + idProvince + ");\n";
//                        }
                    queries += "INSERT INTO vsb_20_district VALUE (" + idDistrict + ",'" + district.replace("'", "''") + "',''," + idProvince + ");\n";

                        for (String ward : provinces.get(province).get(district)) {
//                            String[] wardData=ward.split("\\.");
//                            if(wardData.length!=2){
////                                System.out.println("Ward Length != "+wardData.length+" "+ward);
//                                System.out.println(ward);
//                            }
//                            else{
//                                String wardName=wardData[1];
//                                String wardPrefix="";
//                                if(wardData[0].equals("X"))wardPrefix="Xã";
//                                if(wardData[0].equals("P"))wardPrefix="Phường";
//                                if(wardData[0].equals("TT"))wardPrefix="Thị trấn";
//                                if(wardPrefix.equals("")){
//                                    System.out.println(ward);
//                                }
//                                else {
//                                    queries+="INSERT INTO vsb_20_ward VALUE ("+idWard+",'"+wardName+"','"+wardPrefix+"',"+idProvince+","+idDistrict+");\n";
//                                }
//                            }
                            queries+="INSERT INTO vsb_20_ward VALUE ("+idWard+",'"+ward.replace("'", "''")+"','',"+idProvince+","+idDistrict+");\n";
                            idWard++;
                        }
                        idDistrict++;
//                    }
                }
                idProvince++;
            }
            workbook.close();
            String fileName = "vsb_20_provinces.sql";

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                writer.write(queries);
                writer.close();
                System.out.println("File '" + fileName + "' has been written.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
