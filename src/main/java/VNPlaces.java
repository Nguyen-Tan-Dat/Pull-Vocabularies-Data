import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class VNPlaces {
    public static void main(String[] args) {
        Set<Province> provinces=new HashSet<>();
        Set<District> districts=new HashSet<>();
        Set<Ward> wards=new HashSet<>();
        try {
            File excelFile = new File("Danh sách cấp tỉnh kèm theo quận huyện, phường xã ___21_03_2024.xlsx");
            FileInputStream fis = new FileInputStream(excelFile);

            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            // Lấy ra sheet đầu tiên từ workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Lặp qua từng dòng trong sheet và in ra giá trị của từng ô
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();
            int index=2;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                String name= cellIterator.next().toString().replace("'","\\'");
                byte id= Byte.parseByte(cellIterator.next().toString());
                provinces.add(new Province( id,name));
                name= cellIterator.next().toString().replace("'","\\'");
                int did= Integer.parseInt(cellIterator.next().toString());
                districts.add(new District(did,name,id));

                name= cellIterator.next().toString().replace("'","\\'");
                String widd=cellIterator.next().toString();
                int wid=0;
                try {
                    wid= Integer.parseInt(widd);
                }catch (Exception e){
                }
                if(wid!=0)wards.add(new Ward(wid,name,did));
                index++;
            }
            workbook.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String query="INSERT INTO provinces (id, name) VALUES";
        for(var p:provinces){
            query+="\n("+p.getId()+",'"+p.getName()+"'),";
        }
        query = query.substring(0, query.length() - 1)+";\n";
        query+="INSERT INTO districts (id, name, province) VALUES";
        for (var p:districts){
            query+="\n("+p.getId()+",'"+p.getName()+"',"+p.getProvince()+"),";
        }
        query = query.substring(0, query.length() - 1)+";\n";
        query+="INSERT INTO wards (id, name, district_id) VALUES ";
        for (var p:wards){
            query+="\n("+p.getId()+",'"+p.getName()+"',"+p.getDistrict()+"),";
        }
        query = query.substring(0, query.length() - 1)+";\n";
        writeToFile(query,"vn_places.sql");
    }
    public static void writeToFile(String content, String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath, false); // Sử dụng tham số false để ghi đè nếu tệp đã tồn tại
            writer.write(content);
            writer.close();
            System.out.println("Đã ghi chuỗi vào tệp: " + filePath);
        } catch (IOException e) {
            System.out.println("Lỗi khi ghi vào tệp: " + e.getMessage());
        }
    }


    static class Province{
        private byte id;
        private String name;

        public Province(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getId() {
            return id;
        }

        public void setId(byte id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Province province = (Province) o;
            return id == province.id && Objects.equals(name, province.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }
    static class District{
        private int id;
        private String name;
        private byte province;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            District district = (District) o;
            return id == district.id && province == district.province && Objects.equals(name, district.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, province);
        }

        public District(int id, String name, byte province) {
            this.id = id;
            this.name = name;
            this.province = province;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte getProvince() {
            return province;
        }

        public void setProvince(byte province) {
            this.province = province;
        }
    }
    static class Ward{
        private int id;
        private String name;
        private int district;

        public Ward(int id, String name, int district) {
            this.id = id;
            this.name = name;
            this.district = district;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDistrict() {
            return district;
        }

        public void setDistrict(int district) {
            this.district = district;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ward ward = (Ward) o;
            return id == ward.id && district == ward.district && Objects.equals(name, ward.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, district);
        }
    }
}
