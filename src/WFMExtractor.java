import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.microstrategy.web.beans.WebBeanException;
import com.microstrategy.web.beans.WebException;
import com.microstrategy.web.objects.EnumWebPromptType;
import com.microstrategy.web.objects.SimpleList;
import com.microstrategy.web.objects.WebAttribute;
import com.microstrategy.web.objects.WebConstantPrompt;
import com.microstrategy.web.objects.WebDefaultDisplaySettings;
import com.microstrategy.web.objects.WebDisplayHelper;
import com.microstrategy.web.objects.WebElementSource;
import com.microstrategy.web.objects.WebElements;
import com.microstrategy.web.objects.WebElementsPrompt;
import com.microstrategy.web.objects.WebExpression;
import com.microstrategy.web.objects.WebFilter;
import com.microstrategy.web.objects.WebFolder;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebMetric;
import com.microstrategy.web.objects.WebNode;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;
import com.microstrategy.web.objects.WebObjectsPrompt;
import com.microstrategy.web.objects.WebPrompt;
import com.microstrategy.web.objects.WebPrompts;
import com.microstrategy.web.objects.WebPromptableNode;
import com.microstrategy.web.objects.WebReportInstance;
import com.microstrategy.web.objects.WebReportManipulation;
import com.microstrategy.web.objects.WebReportSource;
import com.microstrategy.web.objects.WebReportValidationException;
import com.microstrategy.web.objects.WebShortcutNode;
import com.microstrategy.web.objects.WebTemplate;
import com.microstrategy.web.objects.WebTemplateMetric;
import com.microstrategy.web.objects.WebTemplateMetrics;
import com.microstrategy.web.objects.WebTemplateUnit;
import com.microstrategy.web.objects.WebWorkingSet;
import com.microstrategy.webapi.EnumDSSXMLExecutionFlags;
import com.microstrategy.webapi.EnumDSSXMLExpressionType;
import com.microstrategy.webapi.EnumDSSXMLFolderNames;
import com.microstrategy.webapi.EnumDSSXMLFunction;
import com.microstrategy.webapi.EnumDSSXMLObjectFlags;
import com.microstrategy.webapi.EnumDSSXMLObjectSubTypes;
import com.microstrategy.webapi.EnumDSSXMLObjectTypes;
import com.microstrategy.webapi.EnumDSSXMLReportSaveAsFlags;
import com.microstrategy.webapi.EnumDSSXMLResultFlags;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class WFMExtractor {
	
	private static WebObjectsFactory factory = null;
	private static WebIServerSession serverSession = null;
	
	private static String mUser = "administrator";
	private static String mPassword = "";
	private static String mProjectName = "CDS Project Test";

	
	public static WebIServerSession getSession(){
		factory = WebObjectsFactory.getInstance();
		serverSession = factory.getIServerSession();
		
		// Set up session properties
		serverSession.setServerName("localhost"); // Should be replaced with the name of an Intelligence Server
		serverSession.setServerPort(0);
		serverSession.setProjectName(mProjectName);
		serverSession.setLogin(mUser); // User ID
		serverSession.setPassword(mPassword); // Password
		
		try {
			System.out.println("nSession created with ID: "+ serverSession.getSessionID());
		} catch (WebObjectsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Session State: "+ serverSession.saveState(0));
		// Return session
		return serverSession;
	}
	
	
	
	public static void populateDerivedMetricData() {
		
		WebReportSource rSource = factory.getReportSource();
		
		//Just execute prompts no SQL 
		rSource.setExecutionFlags(EnumDSSXMLExecutionFlags.DssXmlExecutionNoAction);
		rSource.setExecutionFlags(EnumDSSXMLExecutionFlags.DssXmlExecutionResolve);
		//These force SQL generation
		//rSource.setResultFlags(EnumDSSXMLResultFlags.DssXmlResultXmlSQL);
		//rSource.setExecutionFlags(EnumDSSXMLExecutionFlags.DssXmlExecutionGenerateSQL);
		rSource.setResultFlags(EnumDSSXMLResultFlags.DssXmlResultWorkingSet | EnumDSSXMLResultFlags.DssXmlResultViewReport | EnumDSSXMLResultFlags.DssXmlResultStatusOnlyIfNotReady);
		
				
		String connString =LocalProperties.get().getProperty("jdbc.connection");
		String lUser = LocalProperties.get().getProperty("jdbc.user");
		String lPwd = LocalProperties.get().getProperty("jdbc.pwd");
		String lReportQuery = LocalProperties.get().getProperty("query.report");

		try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				Connection lConn = DriverManager.getConnection(connString, lUser, lPwd);
				MetricInserter lMetInserter = MetricInserter.getMetricInserter(lConn);
				ReportInserter lReportInserter = ReportInserter.getReportInserter(lConn);
				AttributeInserter lAttributeInserter = AttributeInserter.getAttributeInserter(lConn);
				
				Statement st = lConn.createStatement();
				ResultSet rec = st.executeQuery(lReportQuery);
				while (rec.next()) {
					try{
						System.out.println(rec.getString(1));
						WebReportInstance wrpInstance = rSource.getNewInstance();
						WebObjectSource objSource = factory.getObjectSource();
					
						WebObjectInfo oInfoReport = objSource.getObject(rec.getString(1), EnumDSSXMLObjectTypes.DssXmlTypeReportDefinition, true);
						System.out.println("SubType: "+oInfoReport.getSubType());
						System.out.println("ExtendedType: "+oInfoReport.getExtendedType());
						System.out.println("Flags: "+oInfoReport.getFlags());
						System.out.println("State: "+oInfoReport.getState());
						if(oInfoReport.getSubType() != 780 && oInfoReport.getSubType() != 777 && oInfoReport.getSubType() != 775){
							
							
						
							String lReportId = "";
							
							lReportInserter.insertReportInfo(oInfoReport.getID(), buildPath(oInfoReport.getAncestors()), 
									oInfoReport.getName(), oInfoReport.getDescription(), oInfoReport.getOwner().getID(),
									oInfoReport.getOwner().getName(), oInfoReport.getCreationTime(), oInfoReport.getModificationTime(),
									serverSession.getProjectID());
							WebReportInstance rInstance = rSource.getNewInstance(rec.getString(1));
							
							WebFolder lResults = rInstance.getWorkingSet().getWorkingSetObjects();
							for(int k=0; k<lResults.size(); k++){
								switch(lResults.get(k).getType()){
									case 4:
										WebMetric lMetric = (WebMetric) lResults.get(k);
										if(lMetric.getState() == 36991){
											lMetInserter.insertMetricInfo(lMetric.getID(), lMetric.getFormula(), 
													"", "", "", "Derived", oInfoReport.getID(), serverSession.getProjectID());

										}
									break;
									case 12:
										WebAttribute lAttribute = (WebAttribute) objSource.getObject(lResults.get(k).getID(), EnumDSSXMLObjectTypes.DssXmlTypeAttribute, true);
										lAttributeInserter.insertAttributeInfo(lAttribute.getID(), lAttribute.getName(), lAttribute.getOwner().getID(), 
												lAttribute.getOwner().getName(), lAttribute.getCreationTime(), lAttribute.getModificationTime(),
												oInfoReport.getID(), serverSession.getProjectID());

									break;
								}
							}
							
							
						
							rInstance.setExecutionMode(1);
							rInstance.setMaxWait(-1);				
							rInstance.setAsync(false);
					
							WebPrompts prompts = rInstance.getPrompts();
							int maxObjects = 25;
							if(prompts.size()>0){
								for(int i = 0; i<prompts.size(); i++){
									WebPrompt lCurrentPrompt = prompts.get(i);
									switch (lCurrentPrompt.getPromptType()) {
										case EnumWebPromptType.WebPromptTypeObjects:
											WebObjectsPrompt objPrompt = null;
											objPrompt = (WebObjectsPrompt) lCurrentPrompt;
											//if (!objPrompt.hasOriginalAnswer()) {
												if (objPrompt.hasDefaultAnswer()) {
													objPrompt.setAnswer(objPrompt.getDefaultAnswer());
												}
												else if (objPrompt.hasPreviousAnswer()) {
													objPrompt.setAnswer(objPrompt.getPreviousAnswer());
												} else{
													answerObjectPrompt(objPrompt);
												}
											//}
											//if (objPrompt.getSearchRestriction() != null) {
											//	objPrompt.getSearchRestriction().setBlockBegin(1);
											//	objPrompt.getSearchRestriction().setBlockCount(maxObjects);	
											//}		
											break;
										case EnumWebPromptType.WebPromptTypeElements:
											WebElementsPrompt elePrompt = null;
											elePrompt = (WebElementsPrompt) lCurrentPrompt;
											if (elePrompt.hasDefaultAnswer()) {
												elePrompt.setAnswer(elePrompt.getDefaultAnswer());
											}
											else if (elePrompt.hasPreviousAnswer()) {
												elePrompt.setAnswer(elePrompt.getPreviousAnswer());
											} else{
												answerElementPrompt(elePrompt);
											}
											break;
										case EnumWebPromptType.WebPromptTypeConstant:
											WebConstantPrompt wcp = (WebConstantPrompt) lCurrentPrompt;
											if (wcp.hasDefaultAnswer()) {
												wcp.setAnswer(wcp.getDefaultAnswer());
											}
											else if (wcp.hasPreviousAnswer()) {
												wcp.setAnswer(wcp.getPreviousAnswer());
											} else{
												answerConstantPrompt(wcp);
											}
											break;
									}
								}
								prompts.answerPrompts();
							}
							//objPrompt.getSuggestedAnswers().				 
							//int status = rInstance.pollStatus();
							
							System.out.println("Template: "+rInstance.getTemplate());
							rInstance.getResults();
							System.out.println("XDA Type: "+rInstance.getXdaType());
							//WebReportManipulation manip= rInstance.getReportManipulator();
							if(rInstance.getTemplate() != null){
								
								WebTemplate template = rInstance.getTemplate();
								WebTemplateMetrics wtms = template.getTemplateMetrics();
								for(int j=0; j< wtms.size(); j++){
									WebTemplateMetric wtm = wtms.get(j);
									if(wtm.isDerived()){
										System.out.println("Metric: "+wtm.getFormula());
										System.out.println("Metric ID: "+wtm.getMetric().getID());
										

									}
								}
								//System.out.println("SQL: "+rInstance.getResults().getSQL());
								//WebTemplateMetric wtm = wtms.get(0);
							}
						
						}
						
					    	
					 } catch (WebObjectsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					 }
				}
				st.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void answerConstantPrompt(WebConstantPrompt prompt) throws WebObjectsException, IllegalArgumentException {
		//System.out.println("\nMethod Call: answerConstantPrompt");
			
			prompt.setAnswer("0");
	}
	
	private static void answerObjectPrompt(WebObjectsPrompt prompt) throws WebObjectsException, IllegalArgumentException {
		//System.out.println("\nMethod Call: answerObjectPrompt");
		
		WebFolder defaultWebFolder = prompt.getAnswer();
		defaultWebFolder.clear();
		WebFolder lAnswers = prompt.getSuggestedAnswers();
		if(lAnswers != null){
			WebObjectInfo lOneAnswer = lAnswers.get(1);		
			prompt.getAnswer().add(lOneAnswer);
			
		}
		prompt.validate();
		return;
	}
	
	private static void answerElementPrompt(WebElementsPrompt prompt) throws WebObjectsException {
		//System.out.println("\nMethod Call: answerElementPrompt");
		
		WebElements defaultAnswer = prompt.getAnswer();
		defaultAnswer.clear();
		
		WebElementSource eltSrc=prompt.getOrigin().getElementSource();
		WebElements elements = eltSrc.getElements();
		WebElements we = prompt.getAnswer();
		
		//for (int e = 0; e < elements.size(); e++) {
			String originName = prompt.getOrigin().getName();
			String elemName = elements.get(0).getDisplayName();
			//System.out.println("originName: " + originName + "; elemName: " + elemName);
			
			we.add(elements.get(0).getElementID());

		//}
		prompt.setAnswer(we);
		prompt.validate();
		return;
	}
	
	private static String buildPath(SimpleList lList){
		String lPath = "";
		for (int i =0; i< lList.size(); i++){
			WebFolder lFolder = (WebFolder)lList.item(i);
			lPath = lPath + "\\" + lFolder.getName();
		}
		return lPath;
	}
	
	public static void main(String[] args){
		//mUser = args[0];
		//mPassword = args[1];
		//mProjectName = args[2];
		
		System.out.println(LocalProperties.get().getProperty("server.port"));
		
		getSession();
		WebObjectSource oSource = factory.getObjectSource();	
		populateDerivedMetricData();
		System.exit(0);
	}
	
}
