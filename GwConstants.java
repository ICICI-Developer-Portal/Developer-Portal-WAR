package com.icici.apigw.util;

public final class GwConstants {
	
	public static final String DB_DOC_URL = ConfigUtil.get("db.doc.url");
	public static final String DB_DOC_DRIVER = ConfigUtil.get("db.doc.driver");
	public static final String DB_DOC_USERNAME = EncryptDecryptUtil.decrypt(ConfigUtil.get("db.doc.username"));
	public static final String DB_DOC_PASSWORD = EncryptDecryptUtil.decrypt(ConfigUtil.get("db.doc.password"));
	public static final Integer DB_DOC_INITIAL_SIZE = Integer.parseInt(ConfigUtil.get("db.doc.initialSize"));
	public static final Integer DB_DOC_MAX_TOTAL = Integer.parseInt(ConfigUtil.get("db.doc.maxTotal"));
	public static final Integer DB_DOC_MAX_IDLE = Integer.parseInt(ConfigUtil.get("db.doc.maxIdle"));
	public static final Integer DB_DOC_MIN_IDLE = Integer.parseInt(ConfigUtil.get("db.doc.minIdle"));
	public static final Integer DB_DOC_MAX_WAIT_MILLIS = Integer.parseInt(ConfigUtil.get("db.doc.maxWaitMillis"));
	
	public static final String DB_LRS_URL = ConfigUtil.get("db.lrs.url");
	public static final String DB_LRS_DRIVER = ConfigUtil.get("db.lrs.driver");
	public static final String DB_LRS_USERNAME = EncryptDecryptUtil.decrypt(ConfigUtil.get("db.lrs.username"));
	public static final String DB_LRS_PASSWORD = EncryptDecryptUtil.decrypt(ConfigUtil.get("db.lrs.password"));
	public static final Integer DB_LRS_INITIAL_SIZE = Integer.parseInt(ConfigUtil.get("db.lrs.initialSize"));
	public static final Integer DB_LRS_MAX_TOTAL = Integer.parseInt(ConfigUtil.get("db.lrs.maxTotal"));
	public static final Integer DB_LRS_MAX_IDLE = Integer.parseInt(ConfigUtil.get("db.lrs.maxIdle"));
	public static final Integer DB_LRS_MIN_IDLE = Integer.parseInt(ConfigUtil.get("db.lrs.minIdle"));
	public static final Integer DB_LRS_MAX_WAIT_MILLIS = Integer.parseInt(ConfigUtil.get("db.lrs.maxWaitMillis"));
	
	public static final String APP_SERVER_URL = ConfigUtil.get("server.url");
	public static final String OTP_CREATE_URL = ConfigUtil.get("service.eotp.create.url");
    public static final String OTP_VERIFY_URL = ConfigUtil.get("service.eotp.verify.url");
    
    public static final String CACHE_STORE_API_DTL = "cs.apiDtls";
    public static final String CACHE_STORE_API_PKT = "cs.apiPkt";
    public static final String CACHE_STORE_MENU_DTLS = "cs.menuDtls";
    
    public static final String JIRA_STS_COMPLETE = "Complete";
    public static final String CERTIFICATE_UPLOADPATH = ConfigUtil.get("certificate.uploadPath");
    public static final String PDFFILE_UPLOADPATH = ConfigUtil.get("pdfFile.uploadPath");
    public static final String CERTIFICATE_UPLOADPATH_APPATHON = ConfigUtil.get("certificate.uploadPath_appathon");
    public static final String MIS_LOCAL = ConfigUtil.get("MISLocal");
    
    
    public static final String JIRA_URL_V2=ConfigUtil.get("jira.url.v2");
    public static final String JIRA_V2_USERNAME=ConfigUtil.get("jira.v2.username");
    public static final String JIRA_V2_PASSWORD=ConfigUtil.get("jira.v2.password");

    
    private GwConstants() {
	}
	
}
