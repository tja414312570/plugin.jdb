import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.plugin.handler.PlugsHandler;

public class DataSourceTest {
	public static void main(String[] args) {
		PlugsFactory.getInstance().init("classpath:plugin.yc");
		PlugsFactory.getInstance().addScanPath(DataSourceTest.class);
		DataSource defaultDataSource = PlugsFactory.getPluginsInstance(DataSource.class);
		System.out.println("===========");
		System.out.println(defaultDataSource.getClass());
		PlugsHandler plugsHandler = PlugsFactory.getPluginsHandler(defaultDataSource);
		System.out.println(defaultDataSource);
		Connection connection;
		try {
			System.out.println(defaultDataSource.getConnection());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
