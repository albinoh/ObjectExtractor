import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class LocalProperties {
	
	private static LocalProperties mInstance;
	private static Properties mProperties;
	
	private LocalProperties() {
		mProperties = new Properties();
		try {
			mProperties.load(new FileInputStream("./src/application.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static Properties get() {
		if(mInstance == null){
			mInstance = new LocalProperties();
		}
		return mInstance.mProperties;
	}
}
