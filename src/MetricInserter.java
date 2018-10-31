import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MetricInserter {
	
	private static MetricInserter mInstance;
	
	
	private Connection mConnection;
	private PreparedStatement mPreparedStmtMetDef;
	private PreparedStatement mPreparedStmtMetRel;
	
	private MetricInserter(Connection iConn) throws SQLException{
		String lMetricTable = LocalProperties.get().getProperty("table.metric");
		String lMetricRelateTable = LocalProperties.get().getProperty("table.metricrelate");

		mConnection = iConn;
		mPreparedStmtMetDef = iConn.prepareStatement("insert into "+lMetricTable+" (MET_GUID, MET_DEF, MET_FILT, MET_LEVEL, "
				+ "MET_TRANS, MET_TYPE, PROJ_GUID) values (?, ?, ?, ?, ?, ?, ?)");
		mPreparedStmtMetRel = iConn.prepareStatement("insert into "+lMetricRelateTable+" (REP_GUID, MET_GUID, PROJ_GUID) values (?, ?, ?)");

	}
	
	public static MetricInserter getMetricInserter(Connection iConn) throws SQLException{
		if(mInstance == null){
			mInstance = new MetricInserter(iConn);
		}
		return mInstance;
	}
	
	public void insertMetricInfo(String iMetricGUID, String iMetricFormula, String iMetricFilter, 
			String iMetricLevel, String iMetricTrans, String iMetricType, String iReportGUID, String iProjectGUID ) throws SQLException{
		
		mPreparedStmtMetDef.setString (1, iMetricGUID);
		mPreparedStmtMetDef.setString (2, iMetricFormula);
		mPreparedStmtMetDef.setString (3, iMetricFilter);
		mPreparedStmtMetDef.setString (4, iMetricLevel);
		mPreparedStmtMetDef.setString (5, iMetricTrans);
		mPreparedStmtMetDef.setString (6, iMetricType);
		mPreparedStmtMetDef.setString (7, iProjectGUID);
		  // execute the preparedstatement
		mPreparedStmtMetDef.execute();
		  
		mPreparedStmtMetRel.setString (1, iReportGUID);
		mPreparedStmtMetRel.setString (2, iMetricGUID);
		mPreparedStmtMetRel.setString (3, iProjectGUID);
		mPreparedStmtMetRel.execute();
		
	}
	
}
