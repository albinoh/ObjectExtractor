import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReportInserter {
	
	private static ReportInserter mInstance;
	
	
	private Connection mConnection;
	private PreparedStatement mPreparedStmtRepDef;
	
	private ReportInserter(Connection iConn) throws SQLException{
		mConnection = iConn;
		mPreparedStmtRepDef = iConn.prepareStatement("insert into [WFM_DEFS].[dbo].[REP_DEF] (REP_GUID, REP_LOC, REP_NAME, REP_DESC, "
				+ "REP_OWNER_GUID, REP_OWNER_NAME, REP_CREATE_DATE, REP_MODIF_DATE, PROJ_GUID) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?)");

	}
	
	public static ReportInserter getReportInserter(Connection iConn) throws SQLException{
		if(mInstance == null){
			mInstance = new ReportInserter(iConn);
		}
		return mInstance;
	}
	
	public void insertReportInfo(String iRepGUID, String iRepLoc, String iRepName, String iRepDesc, String iRepOwnerGUID, String iRepOwnerName, 
			String iRepCreation, String iRepModification, String iProjectGUID ) throws SQLException{
		
		mPreparedStmtRepDef.setString (1, iRepGUID);
		mPreparedStmtRepDef.setString (2, iRepLoc);
		mPreparedStmtRepDef.setString (3, iRepName);
		mPreparedStmtRepDef.setString (4, iRepDesc);
		mPreparedStmtRepDef.setString (5, iRepOwnerGUID);
		mPreparedStmtRepDef.setString (6, iRepOwnerName);
		mPreparedStmtRepDef.setString (7, iRepCreation);
		mPreparedStmtRepDef.setString (8, iRepModification);
		mPreparedStmtRepDef.setString (9, iProjectGUID);
		  // execute the preparedstatement
		mPreparedStmtRepDef.execute();

		
	}
	
}
