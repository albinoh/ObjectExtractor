import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AttributeInserter {
	
	private static AttributeInserter mInstance;
	
	
	private Connection mConnection;
	private PreparedStatement mPreparedStmtAttDef;
	private PreparedStatement mPreparedStmtAttRel;
	
	private AttributeInserter(Connection iConn) throws SQLException{
		mConnection = iConn;
		mPreparedStmtAttDef = iConn.prepareStatement("insert into [WFM_DEFS].[dbo].[ATT_DEF] (ATT_GUID, ATT_NAME, ATT_OWNER_GUID, ATT_OWNER_NAME, "
				+ "ATT_CREATE_DATE, ATT_MODIF_DATE, PROJ_GUID) "
				+ "values (?, ?, ?, ?, ?, ?, ?)");
		mPreparedStmtAttRel = iConn.prepareStatement("insert into [WFM_DEFS].[dbo].[REL_REP_ATT] (ATT_GUID, REP_GUID, PROJ_GUID) "
				+ "values (?, ?, ?)");
		
	}
	
	public static AttributeInserter getAttributeInserter(Connection iConn) throws SQLException{
		if(mInstance == null){
			mInstance = new AttributeInserter(iConn);
		}
		return mInstance;
	}
	
	public void insertAttributeInfo(String iAttGUID, String iAttName, String iAttOwnerGUID, String iAttOwnerName, 
			String iAttCreation, String iAttModification, String iReportGUID, String iProjectGUID ) throws SQLException{
		
		mPreparedStmtAttDef.setString (1, iAttGUID);
		mPreparedStmtAttDef.setString (2, iAttName);
		mPreparedStmtAttDef.setString (3, iAttOwnerGUID);
		mPreparedStmtAttDef.setString (4, iAttOwnerName);
		mPreparedStmtAttDef.setString (5, iAttCreation);
		mPreparedStmtAttDef.setString (6, iAttModification);
		mPreparedStmtAttDef.setString (7, iProjectGUID);
		  // execute the preparedstatement
		mPreparedStmtAttDef.execute();

		mPreparedStmtAttRel.setString (1, iAttGUID);
		mPreparedStmtAttRel.setString (2, iReportGUID);
		mPreparedStmtAttRel.setString (3, iProjectGUID);
		mPreparedStmtAttRel.execute();
	}
	
}
