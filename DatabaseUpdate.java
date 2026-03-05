import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseUpdate {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:backend/data/mockserver.db");
            Statement stmt = conn.createStatement();

            // 检查active字段是否已存在
            try {
                stmt.executeQuery("SELECT active FROM t_mock_response LIMIT 1");
                System.out.println("active字段已存在");
            } catch (Exception e) {
                System.out.println("active字段不存在，正在添加...");
                stmt.execute("ALTER TABLE t_mock_response ADD COLUMN active BOOLEAN DEFAULT 0");
                System.out.println("active字段添加成功");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
