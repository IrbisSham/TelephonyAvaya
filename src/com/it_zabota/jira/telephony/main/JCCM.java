/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.it_zabota.jira.telephony.main;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import com.it_zabota.jira.telephony.encryptng.SecurityFile;
import com.it_zabota.jira.telephony.gui.FrmFio;
import com.it_zabota.jira.telephony.gui.FrmIssue;
import com.it_zabota.jira.telephony.gui.FrmTel;
import com.it_zabota.jira.telephony.jira.JiraUserFields;
import com.it_zabota.jira.telephony.jira.JiraUtils;
import com.it_zabota.jira.telephony.ldap.LdapUtils;
import com.it_zabota.jira.telephony.utils.SysUtils;

/**
 * Главный класс приложения. Является контейнером для остальных классов.
 * 
 * @author Vitaly Ermilov <vitaliyerm@gmail.com>
 * @version 1.0
 * @since 06.10.2014
 */
public class JCCM {

    /*
     * Используемые константы
     */
		
    public String getProjectFldIdJira() {
		return projectFldIdJira;
	}

	public String getReporterFldIdJira() {
		return reporterFldIdJira;
	}

	public String getDateFldIdJira() {
		return dateFldIdJira;
	}

	public String getSummaryFldIdJira() {
		return summaryFldIdJira;
	}

	public String getIstypeFldIdJira() {
		return istypeFldIdJira;
	}

	private static String PROPERTIES_FILE = "telephony.properties";
	private static final String PROPERTIES_FILE_ENC = "telephony.dat";
    
    public String getFioAbonentCurAd() {
		return fioAbonentCurAd;
	}

	public void setFioAbonentCurAd(String fioAbonentCurAd) {
		this.fioAbonentCurAd = fioAbonentCurAd;
	}

	public String getOrgAbonentCurAd() {
		return orgAbonentCurAd;
	}

	public void setOrgAbonentCurAd(String orgAbonentCurAd) {
		this.orgAbonentCurAd = orgAbonentCurAd;
	}

	public String getDptAbonentCurAd() {
		return dptAbonentCurAd;
	}

	public void setDptAbonentCurAd(String dptAbonentCurAd) {
		this.dptAbonentCurAd = dptAbonentCurAd;
	}
	
	public static  String SPLITTER = "-----";
	
	//
	/*
     * Ключ к шифрованию файлов
     */    	
	public static  String FILE_ENCRIPT_KEY = "ROOT_789_WERT";
	
	public static  String APP_MODE_GUI = "GUI";
	public static  String APP_MODE_CONSOLE = "CONSOLE";
	
	public static  String APP_MODE = APP_MODE_GUI;
	// GUI - с интерфейсом, по-умолчанию
	// CONSOLE - без интерфейса, моментальная регистрация заявки
	
	public static  String ASSIGNEE_FIO;
	
	/*
     * Jira. Подключение
     */    
    public static  String JIRA_HOST = "localhost";
    public static  String JIRA_PORT = "8080";
	public static  String JIRA_USERNAME = "jiraadm";
	public static  String JIRA_PASSWORD = "sphere";
	public static  String JIRA_ENCODING = "UTF-8";
	
    /*
     * Jira. Значения базовых полей для поиска
     */    
    public static  String JIRA_PROJECT_KEY = "HDPRJ";
    public static  String JIRA_FIELD_PROJECT = "pid";
    
    public static  String[] JIRA_USER_GROUPS;    
    public static String JIRA_REST_URL_USER_GROUP_USERS = "/rest/api/2/group?groupname=$$$groupCurrent$$$&expand=users[$$$fetchFirst$$$:$$$fetchLast$$$]";
    public static  String JIRA_REST_URL_USER_GROUP_USERS_GROUP_REGEXP = "$$$groupCurrent$$$";    
    public static  String JIRA_REST_URL_USER_GROUP_USERS_GROUP_REGEXP1 = "$$$fetchFirst$$$";
    public static  String JIRA_REST_URL_USER_GROUP_USERS_GROUP_REGEXP2 = "$$$fetchLast$$$";
    public static  int JIRA_REST_URL_USER_GROUP_USERS_FETCH_SIZE = 50;
    
    public static  String JIRA_ISSUE_TYPE_DEFAULT = "IT Help";   
    public static  String[] JIRA_ISSUE_TYPE_LIST;

    
    
    public static  String[] JIRA_FILTER_ABONENT_PROJECT_KEY;
    public static  String[] JIRA_FILTER_ABONENT_ISSUE_TYPE;
    public static  String[] JIRA_FILTER_GLOBAL_PROJECT_KEY;
    public static  String[] JIRA_FILTER_GLOBAL_ISSUE_TYPE;
    
	
    public static  String JIRA_PRIORITY_DEFAULT = "";
    
    
    /*
     * Базовые части сетевых адресов REST-подключений к Jira
     */
    public static  String JIRA_REST_URL_FIELD_LIST = "rest/api/2/field";
    public static  String JIRA_REST_URL_PROJECT_LIST = "rest/api/2/project";
    public static  String JIRA_REST_URL_ISSUETYPE_LIST = "rest/api/2/issuetype";
    public static  String JIRA_REST_URL_PRIORITY_LIST = "rest/api/2/priority";
    public static  String JIRA_REST_URL_ISSUELINKTYPE_LIST = "rest/api/2/issueLinkType";    
    public static  String JIRA_REST_URL_USER_CURRENT = "rest/api/2/user/search?username=";   
    public static  String JIRA_REST_URL_ISSUELINK = "rest/api/2/issueLink";
//    public static  String JIRA_REST_URL_RESOLUTION_LIST = "rest/api/2/resolution";
    
    
    public static  String JIRA_REST_URL_ISSUE = "rest/api/2/issue";
    
    
    
    /*
     * Поля Jira
     */
    public static  String JIRA_USER_FIELD_EMAIL = "emailAddress";    
    public static  String JIRA_USER_FIELD_LOGIN = "name";
    public static  String JIRA_USER_FIELD_FULLNAME = "displayName";
    
    public static  String[] JIRA_USER_FIELD_ARRAY = {JIRA_USER_FIELD_FULLNAME, JIRA_USER_FIELD_LOGIN, JIRA_USER_FIELD_EMAIL};
    
    public static  String JIRA_ISSUE_FIELD_ID = "id";
    public static  String JIRA_ISSUE_FIELD_NAME = "name";
    public static  String JIRA_ISSUE_FIELD_KEY = "key";
    
    public static  String JIRA_ISSUE_FIELD_PROJECT = "Project";
    public static  String JIRA_ISSUE_FIELD_REPORTER = "Reporter";
    public static String JIRA_ISSUE_FIELD_ASSIGNEE = "Assignee";    
    
    public static  String JIRA_ISSUE_FIELD_TEL = "Telephone";
    
    public static  String JIRA_ISSUE_FIELD_PRIORITY = "Priority";
    
    public static  String JIRA_ISSUE_FIELD_NO = "Key";
    public static  String JIRA_ISSUE_FIELD_TYPE = "Issue Type";
    public static  String JIRA_ISSUE_FIELD_LINK_TYPE = "Linked Issues";
    public static  String JIRA_ISSUE_FIELD_LINK_TYPE_VALUE = "Relates";
    
//    public static  String JIRA_ISSUE_LINK_COMMENT = "Linked related issue!";
//    public static  String JIRA_ISSUE_LINK_VISIBILITY_TYPE = "group";
//    public static  String JIRA_ISSUE_LINK_VISIBILITY_VALUE = "jira-users";
    
    public static  String JIRA_ISSUE_FIELD_THEME = "Summary";
    public static  String JIRA_ISSUE_FIELD_DT = "";
    public static  String JIRA_ISSUE_FIELD_STS = "Status";
    public static  String JIRA_ISSUE_FIELD_DESC = "Description";
//    public static  String JIRA_ISSUE_FIELD_RESOLUTION = "Resolution";
//
//    public static  String JIRA_ISSUE_RESOLUTION_RESOLVED_NAME = "Resolved";
//    public static  String JIRA_ISSUE_RESOLUTION_UNRESOLVED_NAME = "Unresolved";
//    
//    public static  String JIRA_ISSUE_RESOLUTION_RESOLVED_ID;
//    public static  String JIRA_ISSUE_RESOLUTION_UNRESOLVED_ID;
    

    public static  String SAVE_JIRA_ISSUES_IMMEDIATELY = "ДА";
    
    public static  String FIELD_SEARCH_BASE = "AD_FIELD_TELEPHONE";
    
    public static  String FIELD_SEARCH_BASE_MODIFICATION = "НЕТ";
    
    
    // JIRA_USER_FIELD_FULLNAME
    
    
    /*
     * LDAP. Подключение к AD
     */
    
    public static  String LDAP_USE = "НЕТ";   


	public static  String LDAP_HOST = "localhost";
    public static  String LDAP_PORT = "10389";
    public static  String LDAP_SEARCHBASE = "ou=people,o=helpdesc";
    public static  String LDAP_SEARCHFILTER = "(objectClass=*)";
    
    public static  String LDAP_USERNAME = "uid=admin,ou=system";
    public static  String LDAP_PASSWORD = "opldap_Vit";    
    
    /*
     * Поля AD
     */    
    public static  String AD_FIELD_TELEPHONE = "telephoneNumber";
    public static  String AD_FIELD_EMAIL = "mail";    
//    public static  String AD_FIELD_FIO = "cn";
    public static  String AD_FIELD_SURNAME = "ou";
    public static  String AD_FIELD_NAME = "cn";
    public static  String AD_FIELD_LOGIN="sn";    
//    public static  String AD_FIELD_DTBRN = "departmentNumber";
//    public static  String AD_FIELD_ORG = "o";
//    public static  String AD_FIELD_DPT = "ou";
    
    /*
     * Поле синхронизации пользователей AD и пользователей Jira (Почта)
     */
    public static  String AD_FIELD_SYNCHRO_AD;    
    public static String[] AD_FIELD_AR;
    public static String[] JIRA_FIELD_AR;
    
    
    /*
     * Шифровать ли рабочие файлы
     */
    public static String WORK_FILES_ENCRYPTING = "НЕТ";
    
    
    /*
     * Фильтры выборки данных обращений
     */    
    public static  String FORM_ISSUE_NUM_VAR1 = "Обращения абонента";
    public static  String FORM_ISSUE_INTERVAL_VAR2 = "Глобальные обращения";
    public static  Integer FORM_ISSUE_NUM_PAR1 = 10;
    public static  String FORM_ISSUE_INTERVAL_PAR2 = "1w";    
    public static  String[] FORM_ISSUE = {FORM_ISSUE_NUM_VAR1, FORM_ISSUE_INTERVAL_VAR2};

    /*
     * Основные свойства формы
     */
    
    public static  String FORM_LBL_ABONENT = "Абонент";
    public static  String FORM_LBL_ISSUES = "Обращения";
    public static  String FORM_LBL_TEL = "Телефон:";
    public static  String FORM_LBL_FIO = "ФИО:";
    
    public static  String FORM_BTN_NEWISSUE = "Создать";    
    
    public static  String FORM_BTN_LINK_ICON_PATH = "c:\\Users\\Vitaliy.Ermilov\\Documents\\Projects\\git\\Jira_Projects\\telephony\\src\\main\\resources\\link.png";
    public static  int FORM_BTN_LINK_ICON_WIDTH = 15;
    public static  int FORM_BTN_LINK_ICON_HEIGHT = 15;
    public static  Dimension FORM_BTN_LINK_ICON_DIM = new Dimension(FORM_BTN_LINK_ICON_WIDTH, FORM_BTN_LINK_ICON_HEIGHT);
    
    
    /*
     * Основные свойства таблицы обращений
     */    
    public static String FORM_TABLE_COL_LBL_LINK = "Связать с..";
    public static String[] FORM_TABLE_COL_LBL = {"№", "Тип обращения", "Тема", "Дата", "Статус", FORM_TABLE_COL_LBL_LINK};
    public static String FORM_TABLE_COL_IND_IDISSUE = "№";
    public static short[] FORM_TABLE_COL_WIDTH = {80, 120, 200, 130, 150, 100, 120};//{"№", "Тип обращения", "Тема", "Дата", "Статус", ""};
    public static  Class[] FORM_TABLE_COL_TYP = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, javax.swing.JButton.class};
    public static Object[][] FORM_TABLE_COL_VAL = {{null, null, null, null, null, null}};
    public static  boolean[] FORM_TABLE_COL_CAN_EDIT = {false,false,false,true,false,true};
   
    /*
     * Основные свойства таблицы ФИО
     */    
    public static String[] FORM_TABLE_FIO_COL_LBL = {"ФИО", "Логин", "Электронный адрес"};
    public static short[] FORM_TABLE_FIO_COL_WIDTH = {150,  50, 75};
    public static  Class[] FORM_TABLE_FIO_COL_TYP = {java.lang.String.class, java.lang.String.class, java.lang.String.class};
    public static Object[][] FORM_TABLE_FIO_COL_VAL = {{null, null, null}};
    public static  boolean[] FORM_TABLE_FIO_COL_CAN_EDIT = {false, false, false};
    
//    public static String[] FORM_TABLE_FIO_COL_LBL = {"Логин", "Полное имя", "Электронный адрес"};
//    public static short[] FORM_TABLE_FIO_COL_WIDTH = {100, 150, 50};
//    public static  Class[] FORM_TABLE_FIO_COL_TYP = {java.lang.String.class, java.lang.String.class, java.lang.String.class};
//    public static Object[][] FORM_TABLE_FIO_COL_VAL = {{null, null, null}};
//    public static  boolean[] FORM_TABLE_FIO_COL_CAN_EDIT = {false,false,false};    
    
    /*
     * Основные свойства главного класса
     */
    
    private FrmTel frmTel = new FrmTel(this);
    public FrmTel getFrmTel() {
		return frmTel;
	}

	private FrmFio frmFio;
    private FrmIssue frmIssue;
    private static Logger logger = Logger.getLogger(JCCM.class);
    
    private String projectFldIdJira; 
    private String reporterFldIdJira;
    private String dateFldIdJira; 
    private String summaryFldIdJira;
    private String priorityFldIdJira;
    private String telephoneFldIdJira;
    
    private String telephoneVal;
    
    public String getTelephoneVal() {
		return telephoneVal;
	}

	public void setTelephoneVal(String telephoneVal) {
		this.telephoneVal = telephoneVal;
	}

	public String getTelephoneFldIdJira() {
		return telephoneFldIdJira;
	}

	public void setTelephoneFldIdJira(String telephoneFldIdJira) {
		this.telephoneFldIdJira = telephoneFldIdJira;
	}

	private String istypeFldIdJira;
    private String descriptionFldIdJira;
//    private String resolutionFldIdJira;
//    
//    public String getResolutionFldIdJira() {
//		return resolutionFldIdJira;
//	}
//
//	public void setResolutionFldIdJira(String resolutionFldIdJira) {
//		this.resolutionFldIdJira = resolutionFldIdJira;
//	}

	private HashMap<Integer,SearchResult> fltUsers = null;
    private String emailCur;
    private String reporterCurJira;    
    private String assigneeCurJira;
    public String getAssigneeCurJira() {
		return assigneeCurJira;
	}

	public void setAssigneeCurJira(String assigneeCurJira) {
		this.assigneeCurJira = assigneeCurJira;
	}

	public void setReporterCurJira(String reporterCurJira) {
		this.reporterCurJira = reporterCurJira;
	}

	public String getReporterCurJira() {
		return reporterCurJira;
	}

	private String fldSrcCur;	
	
    public String getFldSrcCur() {
		return fldSrcCur;
	}

	public void setFldSrcCur(String fldSrcCur) {
		this.fldSrcCur = fldSrcCur;
	}

    private String fioAbonentCurAd;
    private String orgAbonentCurAd;
    private String dptAbonentCurAd;    
    
    
    private String idIssueCurJira;
    public String getIdIssueCurJira() {
		return idIssueCurJira;
	}

	public String getTypIssueCurJira() {
		return typIssueCurJira;
	}

	public String getThemeIssueCurJira() {
		return themeIssueCurJira;
	}

	public String getDateIssueCurJira() {
		return dateIssueCurJira;
	}

	public String getStatusIssueCurJira() {
		return statusIssueCurJira;
	}

	public String getLinkIssueCurJira() {
		return linkIssueCurJira;
	}

	public void setLinkIssueCurJira(String linkIssueCurJira) {
		this.linkIssueCurJira = linkIssueCurJira;
	}

	private String typIssueCurJira;
	private Integer typIssueIdCurJira;
	private String priorityCurJira;
	public String getPriorityCurJira() {
		return priorityCurJira;
	}

	public void setPriorityCurJira(String priorityCurJira) {
		this.priorityCurJira = priorityCurJira;
	}

	private Long priorityIdCurJira;
    public Long getPriorityIdCurJira() {
		return priorityIdCurJira;
	}

	public void setPriorityIdCurJira(Long priorityIdCurJira) {
		this.priorityIdCurJira = priorityIdCurJira;
	}

	public Integer getTypIssueIdCurJira() {
		return typIssueIdCurJira;
	}

	public void setTypIssueIdCurJira(Integer typIssueIdCurJira) {
		this.typIssueIdCurJira = typIssueIdCurJira;
	}

	public void setTypIssueCurJira(String typIssueCurJira) {
		this.typIssueCurJira = typIssueCurJira;
	}

	private String themeIssueCurJira;
    public void setThemeIssueCurJira(String themeIssueCurJira) {
		this.themeIssueCurJira = themeIssueCurJira;
	}

	private String descriptionIssueCurJira;   
    public void setDescriptionIssueCurJira(String descriptionIssueCurJira) {
		this.descriptionIssueCurJira = descriptionIssueCurJira;
	}

	public String getDescriptionIssueCurJira() {
		return descriptionIssueCurJira;
	}

	private String dateIssueCurJira;
    private String statusIssueCurJira;   
    private String linkIssueCurJira;
    
    private List<String[]> issueCurJira;

	private JDialog dialog;

	private JLabel label;
	private Date APP_DT_END;



    

    public JCCM() throws Exception {
    	loadProperties();
    	AD_FIELD_SYNCHRO_AD = AD_FIELD_LOGIN;
    	AD_FIELD_AR = new String[] {AD_FIELD_EMAIL, AD_FIELD_SURNAME, AD_FIELD_NAME, AD_FIELD_LOGIN, AD_FIELD_TELEPHONE};
    	JIRA_FIELD_AR = new String[] {JIRA_USER_FIELD_LOGIN, JIRA_USER_FIELD_FULLNAME, JIRA_USER_FIELD_EMAIL};    	
    }

    /**
     * Метод, отвечающий за инициализацию приложения.
     *
     * @param поле для поиска (телефонный номер или полное имя или логин)
     * @throws Throwable 
     */
    public void init(String telFld, String fioFld) throws Throwable {
        try {
            logger.info("Запуск приложения...");
            dialog = new JDialog();
            label = new JLabel("Пожалуйста подождите...");
            dialog.setModal(false);
            dialog.setLocationRelativeTo(null);
            dialog.setTitle("Инициализация данных");
            dialog.add(label);
            dialog.setSize(250, 60);
//            dialog.pack();   
            dialog.setIconImage(Launcher.image);
            dialog.setVisible(true);
            loadProperties();
            if (FIELD_SEARCH_BASE_MODIFICATION.equalsIgnoreCase("ФАМ_ЗАПЯТАЯ_ИМЯ")) {
            	String[] words = fioFld.split("\\s+");
            	if (words.length == 2) {
            		fioFld = words[0].concat(", ").concat(words[1]);
            	}
            }            
            setFldSrcCur(fioFld);            
            initJiraUtils();
            String path = System.getProperty("user.dir");
            String pathFile = new String(path.concat("//tmp//tmp0001.ser"));
            // Запись списка мапов Jira    
            writeJiraFile(getJiraUtils().getJiraUsers(), pathFile);
            
            if (LDAP_USE.equalsIgnoreCase("ДА")) {
                initLdapUtils();
                pathFile = new String(path.concat("//tmp//tmp0002.ser"));            
                // Запись списка резалтсетов LDAP          
                writeLdapFile(getLdapUtils().getAdUsers(), pathFile);
                // Отфильтрованный список пользователей по полю синхронизации                
                fltUsers = SysUtils.adFilterJira(ldapUtils, ldapUtils.getAdUsers(), jiraUtils.getJiraUsers());                
            }
            // Отсортированный список пользователй Jira
            fltUsers = SysUtils.sortJira(jiraUtils.getJiraUsers());            
            incomingCall(fioFld, telFld);
        } catch (Exception ex) {
            logger.fatal("Фатальная ошибка", ex);
    	 	logger.info("Завершение приложения...");
            System.exit(-1);
        }
    }

	/**
     * Метод записи в файл списка мапов Jira
	 * @throws Throwable 
     */        
    public void writeJiraFile(ConcurrentMap<String, JiraUserFields> jiraUsers, String path) throws Throwable {
    	BufferedWriter writer = null;
        File fileIn = new File(path.concat("q"));
        File fileOut = new File(path);
    	String separator = JCCM.SPLITTER;
 	    String lineSaparator = System.getProperty("line.separator");
 	    FileOutputStream fos = null;
 	    FileInputStream fis = null;
        if (!fileOut.exists()) {
        	try {
         	   writer = new BufferedWriter(new FileWriter(fileIn));
            	   for (String res : jiraUsers.keySet()) {
            		    String writeStr = "";
	         	    	for (String fld : JIRA_FIELD_AR) {
	         	    		String atrVal = "";
	         	    		switch (fld) {
							case "name":
		         	    		atrVal = jiraUsers.get(res).getName();							
								break;
							case "displayName":
								atrVal = jiraUsers.get(res).getDisplayName();
								break;
							case "emailAddress":
								atrVal = jiraUsers.get(res).getEmailAddress();
								break;								
							default:
								break;
							}

	                   		if (atrVal == null) {
	                   			atrVal = "";     			
	                   		} 	         	    		
	                 		writeStr = writeStr.concat(atrVal);
	                 		if (fld!=JIRA_FIELD_AR[JIRA_FIELD_AR.length - 1]) {
	                 			writeStr = writeStr.concat(separator);
	                 		}	                 		
	         			}
	         	    	writeStr = writeStr.concat(lineSaparator);
	         	    	writer.write(writeStr);                      		            	   
            	   }
               writer.flush();
       		   fis = new FileInputStream(path.concat("q"));
    		   fos = new FileOutputStream(path);            	   
    		   SecurityFile.encrypt(JCCM.FILE_ENCRIPT_KEY, fis, fos, WORK_FILES_ENCRYPTING);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close the writer regardless of what happens...
         		   fis.close(); 
         		   fos.close();    
                   writer.close();         		   
         		   fileIn.delete();
                } catch (Exception e) {
                }
            }
            logger.debug("Serialized Jira Users data is saved in ".concat(fileOut.getCanonicalPath()));
        }		
	}
    
	/**
     * Метод записи в файл списка резалтсетов LDAP
	 * @throws Throwable 
     */    
    public void writeLdapFile(List<SearchResult> adUsers, String path) throws Throwable {
    	BufferedWriter writer = null;
        File fileIn = new File(path.concat("q"));
        File fileOut = new File(path);
    	String separator = JCCM.SPLITTER;
 	    String lineSaparator = System.getProperty("line.separator");
 	    FileOutputStream fos = null;
 	    FileInputStream fis = null;
 	    Integer i = 0;
        if (!fileOut.exists()) {
        	try {                   
        		  writer = new BufferedWriter(new FileWriter(fileIn));
	         	  for (SearchResult sr : adUsers) {
	        		  String writeStr = "";
	         	    	for (String fld : AD_FIELD_AR) {
	         	    		Attribute atr = sr.getAttributes().get(fld);
	         	    		String fldStr = "";
	                		if (atr != null) {
	                			fldStr = atr.get().toString();     			
	                		}
	                 		writeStr = writeStr.concat(fldStr);
	                 		if (fld!=AD_FIELD_AR[AD_FIELD_AR.length - 1]) {
	                 			writeStr = writeStr.concat(separator);
	                 		}
	         			}
	         	    	writeStr = writeStr.concat(lineSaparator);
	         	    	writer.write(writeStr);
	         	    	i++;
         	     }
               writer.flush();
      		   fis = new FileInputStream(path.concat("q"));
      		   fos = new FileOutputStream(path);            	   
      		   SecurityFile.encrypt(JCCM.FILE_ENCRIPT_KEY, fis, fos, WORK_FILES_ENCRYPTING); 
      		   logger.debug("Serialized Ldap Users data is saved in ".concat(fileOut.getCanonicalPath()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close the writer regardless of what happens...
          		   fis.close();
          		   fos.close();    
                    writer.close();         		   
          		   fileIn.delete();
                } catch (Exception e) {
                }
            }
            
        }		
	}    
    
    public HashMap<Integer, SearchResult> getFltUsers() {
		return fltUsers;
	}

	public FrmFio getFrmFio() {
		return frmFio;
	}

	public FrmIssue getFrmIssue() {
		return frmIssue;
	}

	/**
     * Метод загрузки констант из файла настроек.
     * @throws Exception
     */
    private void loadProperties() throws Exception {
        logger.debug("Загрузка настроек...");
//        properties.load(new FileInputStream(new File(getClass().getClassLoader().getResource(PROPERTIES_FILE).toURI())));
    	try {
        	Properties props = new Properties();    		
			File tempFile = File.createTempFile("prop", ".tmp");
			PROPERTIES_FILE = tempFile.getAbsolutePath();
			FileInputStream fis = new FileInputStream(PROPERTIES_FILE_ENC);
			FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE);    		
 		    SecurityFile.decrypt(FILE_ENCRIPT_KEY, fis, fos, "ДА");			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(PROPERTIES_FILE), StandardCharsets.UTF_8));    		
    	    props.load(in);
    	    in.close();
    	    fis.close();
    	    fos.close();
    	    
    	    APP_DT_END=new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(props.getProperty("APP_DT_END").trim()); 
    	    
    	    if (APP_DT_END.before(new Date())) {
    	    	logger.error("Лицензия на приложение действует до ".concat(props.getProperty("APP_DT_END").trim()).concat(" . Чтобы пользоваться программой, продлите лицензию."));
    	    	System.exit(-1);
    	    }
    	    
			//Jira. Подключение
			     
			JIRA_HOST=props.getProperty("JIRA_HOST").trim();
			JIRA_PORT=props.getProperty("JIRA_PORT").trim();
			JIRA_USERNAME=props.getProperty("JIRA_USERNAME").trim();
			JIRA_PASSWORD=props.getProperty("JIRA_PASSWORD").trim();
			JIRA_ENCODING=props.getProperty("JIRA_ENCODING").trim();
			
			
			//Jira. Значения базовых полей для поиска
			     
			JIRA_PROJECT_KEY=props.getProperty("JIRA_PROJECT_KEY").trim();
			JIRA_FIELD_PROJECT=props.getProperty("JIRA_FIELD_PROJECT").trim();
			JIRA_ISSUE_TYPE_DEFAULT=props.getProperty("JIRA_ISSUE_TYPE_DEFAULT").trim(); 
			JIRA_ISSUE_TYPE_LIST = props.getProperty("JIRA_ISSUE_TYPE_LIST").toString().split(","); 
			
			JIRA_USER_GROUPS = props.getProperty("JIRA_USER_GROUPS").toString().split(",");
			JIRA_REST_URL_USER_GROUP_USERS=props.getProperty("JIRA_REST_URL_USER_GROUP_USERS").trim();
			
			
			JIRA_PRIORITY_DEFAULT=props.getProperty("JIRA_PRIORITY_DEFAULT").trim();
			
			SAVE_JIRA_ISSUES_IMMEDIATELY = props.getProperty("SAVE_JIRA_ISSUES_IMMEDIATELY").toUpperCase().trim();
			
			
			
			//Базовые части сетевых адресов REST-подключений к Jira
			 
			JIRA_REST_URL_FIELD_LIST=props.getProperty("JIRA_REST_URL_FIELD_LIST").trim();
			JIRA_REST_URL_PROJECT_LIST=props.getProperty("JIRA_REST_URL_PROJECT_LIST").trim();
			JIRA_REST_URL_ISSUETYPE_LIST=props.getProperty("JIRA_REST_URL_ISSUETYPE_LIST").trim();    
			JIRA_REST_URL_PRIORITY_LIST=props.getProperty("JIRA_REST_URL_PRIORITY_LIST").trim();
			JIRA_REST_URL_ISSUELINKTYPE_LIST=props.getProperty("JIRA_REST_URL_ISSUELINKTYPE_LIST").trim();    
			JIRA_REST_URL_USER_CURRENT=props.getProperty("JIRA_REST_URL_USER_CURRENT").trim();
			JIRA_REST_URL_ISSUELINK=props.getProperty("JIRA_REST_URL_ISSUELINK").trim();
			
//			JIRA_REST_URL_RESOLUTION_LIST=props.getProperty("JIRA_REST_URL_RESOLUTION_LIST").trim();
			
			
			
			//Поля Jira
			 
			JIRA_USER_FIELD_EMAIL=props.getProperty("JIRA_USER_FIELD_EMAIL").trim();
			JIRA_USER_FIELD_LOGIN=props.getProperty("JIRA_USER_FIELD_LOGIN").trim();
			JIRA_USER_FIELD_FULLNAME=props.getProperty("JIRA_USER_FIELD_FULLNAME").trim();
			
			JIRA_ISSUE_FIELD_ID=props.getProperty("JIRA_ISSUE_FIELD_ID").trim();
			JIRA_ISSUE_FIELD_NAME=props.getProperty("JIRA_ISSUE_FIELD_NAME").trim();
			JIRA_ISSUE_FIELD_KEY=props.getProperty("JIRA_ISSUE_FIELD_KEY").trim();
			
			JIRA_ISSUE_FIELD_PROJECT=props.getProperty("JIRA_ISSUE_FIELD_PROJECT").trim();
			JIRA_ISSUE_FIELD_REPORTER=props.getProperty("JIRA_ISSUE_FIELD_REPORTER").trim();
			JIRA_ISSUE_FIELD_ASSIGNEE=props.getProperty("JIRA_ISSUE_FIELD_ASSIGNEE").trim();			
			JIRA_ISSUE_FIELD_PRIORITY=props.getProperty("JIRA_ISSUE_FIELD_PRIORITY");
			
			JIRA_ISSUE_FIELD_TEL=props.getProperty("JIRA_ISSUE_FIELD_TEL");
			
			JIRA_ISSUE_FIELD_NO=props.getProperty("JIRA_ISSUE_FIELD_NO").trim();
			JIRA_ISSUE_FIELD_LINK_TYPE=props.getProperty("JIRA_ISSUE_FIELD_LINK_TYPE").trim();
			JIRA_ISSUE_FIELD_LINK_TYPE_VALUE=props.getProperty("JIRA_ISSUE_FIELD_LINK_TYPE_VALUE").trim();
					
			JIRA_ISSUE_FIELD_THEME=props.getProperty("JIRA_ISSUE_FIELD_THEME").trim();
			JIRA_ISSUE_FIELD_DT=props.getProperty("JIRA_ISSUE_FIELD_DT").trim();
			JIRA_ISSUE_FIELD_STS=props.getProperty("JIRA_ISSUE_FIELD_STS").trim();
			JIRA_ISSUE_FIELD_DESC=props.getProperty("JIRA_ISSUE_FIELD_DESC");
			
			APP_MODE=props.getProperty("APP_MODE");
			
			ASSIGNEE_FIO=props.getProperty("ASSIGNEE_FIO");
			
//			JIRA_ISSUE_FIELD_RESOLUTION=props.getProperty("JIRA_ISSUE_FIELD_RESOLUTION");
//			JIRA_ISSUE_RESOLUTION_RESOLVED_NAME=props.getProperty("JIRA_ISSUE_RESOLUTION_RESOLVED_NAME");
//			JIRA_ISSUE_RESOLUTION_UNRESOLVED_NAME=props.getProperty("JIRA_ISSUE_RESOLUTION_UNRESOLVED_NAME");
						
			
			FORM_ISSUE_NUM_PAR1 = Integer.valueOf(props.getProperty("FORM_ISSUE_NUM_PAR1").trim());
		    FORM_ISSUE_INTERVAL_PAR2 = props.getProperty("FORM_ISSUE_INTERVAL_PAR2").trim();
		    
		    JIRA_FILTER_ABONENT_PROJECT_KEY = props.getProperty("JIRA_FILTER_ABONENT_PROJECT_KEY").toString().split(","); ;
		    JIRA_FILTER_ABONENT_ISSUE_TYPE = props.getProperty("JIRA_FILTER_ABONENT_ISSUE_TYPE").toString().split(","); ;
		    JIRA_FILTER_GLOBAL_PROJECT_KEY = props.getProperty("JIRA_FILTER_GLOBAL_PROJECT_KEY").toString().split(","); ;
		    JIRA_FILTER_GLOBAL_ISSUE_TYPE = props.getProperty("JIRA_FILTER_GLOBAL_ISSUE_TYPE").toString().split(","); ;
			
		    
			//LDAP. Подключение к AD
			 
			LDAP_USE=props.getProperty("LDAP_USE").trim();
			if (LDAP_USE == null) {
				LDAP_USE = "НЕТ";
			}
			
			
			
			LDAP_HOST=props.getProperty("LDAP_HOST").trim();
			LDAP_PORT=props.getProperty("LDAP_PORT").trim();
			LDAP_SEARCHBASE=props.getProperty("LDAP_SEARCHBASE").trim();
			LDAP_SEARCHFILTER=props.getProperty("LDAP_SEARCHFILTER").trim();
			
			LDAP_USERNAME=props.getProperty("LDAP_USERNAME").trim();
			LDAP_PASSWORD=props.getProperty("LDAP_PASSWORD").trim();
			
			
			//Поля AD
			     
			AD_FIELD_TELEPHONE=props.getProperty("AD_FIELD_TELEPHONE").trim();
			AD_FIELD_EMAIL=props.getProperty("AD_FIELD_EMAIL").trim();
			AD_FIELD_SURNAME=props.getProperty("AD_FIELD_SURNAME").trim();			
			AD_FIELD_NAME=props.getProperty("AD_FIELD_NAME").trim();
			AD_FIELD_LOGIN=props.getProperty("AD_FIELD_LOGIN").trim();
			
		    FIELD_SEARCH_BASE=props.getProperty("FIELD_SEARCH_BASE").trim();
		    switch (FIELD_SEARCH_BASE) {
			case "AD_FIELD_TELEPHONE":
				FIELD_SEARCH_BASE = AD_FIELD_TELEPHONE;
				break;
			case "AD_FIELD_EMAIL":
				FIELD_SEARCH_BASE = AD_FIELD_EMAIL;
				break;
			case "AD_FIELD_SURNAME":
				FIELD_SEARCH_BASE = AD_FIELD_SURNAME;
				break;
			case "AD_FIELD_NAME":
				FIELD_SEARCH_BASE = AD_FIELD_NAME;
				break;
			case "AD_FIELD_LOGIN":
				FIELD_SEARCH_BASE = AD_FIELD_LOGIN;
				break;
			case "JIRA_USER_FIELD_EMAIL":
				FIELD_SEARCH_BASE = JIRA_USER_FIELD_EMAIL;
				break;
			case "JIRA_USER_FIELD_FULLNAME":
				FIELD_SEARCH_BASE = JIRA_USER_FIELD_FULLNAME;
				break;
			case "JIRA_USER_FIELD_LOGIN":
				FIELD_SEARCH_BASE = JIRA_USER_FIELD_LOGIN;
				break;
			default:
				break;
			}

		    if (props.getProperty("FIELD_SEARCH_BASE_MODIFICATION") != null) {
		    	FIELD_SEARCH_BASE_MODIFICATION=props.getProperty("FIELD_SEARCH_BASE_MODIFICATION").trim();		    	 
		    }		    
		    
		    
		    if (props.getProperty("WORK_FILES_ENCRYPTING") != null) {
			    WORK_FILES_ENCRYPTING=props.getProperty("WORK_FILES_ENCRYPTING").trim();		    	 
		    }
		    
			
			//Основные свойства формы
			 
			
			FORM_LBL_ABONENT=props.getProperty("FORM_LBL_ABONENT").trim();
			FORM_LBL_ISSUES=props.getProperty("FORM_LBL_ISSUES").trim();
			FORM_LBL_TEL=props.getProperty("FORM_LBL_TEL").trim();
			FORM_LBL_FIO=props.getProperty("FORM_LBL_FIO").trim();
			
			FORM_BTN_NEWISSUE=props.getProperty("FORM_BTN_NEWISSUE").trim();    
			
			FORM_BTN_LINK_ICON_PATH=props.getProperty("FORM_BTN_LINK_ICON_PATH").trim();
			FORM_BTN_LINK_ICON_WIDTH=Integer.valueOf(props.getProperty("FORM_BTN_LINK_ICON_WIDTH"));
			FORM_BTN_LINK_ICON_HEIGHT=Integer.valueOf(props.getProperty("FORM_BTN_LINK_ICON_HEIGHT"));

    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}     		
        
    }


    private JiraUtils jiraUtils;
    private LdapUtils ldapUtils;
	private String islinktypeFldIdJira;
	private String projectKeyCurJira;

	private String loginAbonentCur;

	private String assigneeFldIdJira;

    public String getAssigneeFldIdJira() {
		return assigneeFldIdJira;
	}

	public void setAssigneeFldIdJira(String assigneeFldIdJira) {
		this.assigneeFldIdJira = assigneeFldIdJira;
	}

	public String getLoginAbonentCur() {
		return loginAbonentCur;
	}

	public void setLoginAbonentCur(String loginAbonentCur) {
		this.loginAbonentCur = loginAbonentCur;
	}

	/**
     * Метод инициализации взаимодействия с системой JIRA
     * @throws Throwable 
     */
    private void initJiraUtils() throws Throwable {
        logger.debug("Инициализация доступа и первичных выборок в JIRA...");
        jiraUtils = new JiraUtils();
        jiraUtils.init(this);
    }

    /**
     * Метод инициализации взаимодействия с системой LDAP
     * @throws Throwable 
     */
    private void initLdapUtils() throws Throwable {
        logger.debug("Инициализация доступа и первичных выборок в LDAP...");
        ldapUtils = new LdapUtils();
        ldapUtils.init(this);
    }

    
    public JiraUtils getJiraUtils() {
        return jiraUtils;
    }

    public LdapUtils getLdapUtils() {
        return ldapUtils;
    }
    
    /**
     * Метод обработки входящего звонка
     * @param src номер абонента
     * @throws Throwable 
     */
    public void incomingCall(String src, String other) throws Throwable {
        if (frmTel.isVisible()) {
            return;
        }
        if (src.equals(null)) {
        	logger.error("Пустое поле для поиска (телефон или полное имя) ");
            return;
        }        
        logger.info("Обработка входящего вызова от абонента " + src);
        frmTel.resetData();        
        frmFio = new FrmFio(this);
        frmIssue = new FrmIssue(this); 

        frmFio.initActions();
        frmIssue.initActions();
        frmTel.initActions();     
        SearchResult adUser = SysUtils.findAtrVal(fltUsers, FIELD_SEARCH_BASE, src);
        logger.info("adUser:");
        logger.info(adUser);
        if (adUser==null && JCCM.LDAP_USE.equals("ДА")) {
        	logger.info("Пользователь не найден. Повторная загрузка данных");
        	String path = System.getProperty("user.dir");
            String pathFile = new String(path.concat("//tmp//tmp0001.ser"));
        	File f = new File(pathFile);
        	f.delete();
        	getJiraUtils().setJiraUsers(pathFile);
        	writeJiraFile(getJiraUtils().getJiraUsers(), pathFile);
            pathFile = new String(path.concat("//tmp//tmp0002.ser"));
        	f = new File(pathFile);        	
        	f.delete();
        	getLdapUtils().init(this);
//        	getLdapUtils().setAdUsers(pathFile);
        	writeLdapFile(getLdapUtils().getAdUsers(), pathFile);        	
        	fltUsers = SysUtils.adFilterJira(ldapUtils, ldapUtils.getAdUsers(), jiraUtils.getJiraUsers());  
            adUser = SysUtils.findAtrVal(fltUsers, FIELD_SEARCH_BASE, src);
            if (adUser==null) {
            	logger.info("Пользователь не найден."); 
            }
        	
        }
        if (adUser !=null) {
        	if (JCCM.LDAP_USE.equals("ДА")) {
                emailCur = adUser.getAttributes().get(AD_FIELD_EMAIL).get().toString();
                fioAbonentCurAd = adUser.getAttributes().get(AD_FIELD_SURNAME).get().toString()
                		.concat(" ").concat(adUser.getAttributes().get(AD_FIELD_NAME).get().toString());
                loginAbonentCur = adUser.getAttributes().get(AD_FIELD_LOGIN).get().toString();                    		
        	}
        	else {
                emailCur = adUser.getAttributes().get(JIRA_USER_FIELD_EMAIL).get().toString();
                fioAbonentCurAd = adUser.getAttributes().get(JIRA_USER_FIELD_FULLNAME).get().toString();
                loginAbonentCur = adUser.getAttributes().get(JIRA_USER_FIELD_LOGIN).get().toString();                    		
        	}
            reporterCurJira = loginAbonentCur;        	
        }
        //"project" 
        projectFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_PROJECT); 
        if (projectFldIdJira == null || projectFldIdJira.equals("")) {
        	logger.error(" Не найден id поля проекта: " + JIRA_ISSUE_FIELD_PROJECT);
        	System.exit(-1);
        }
        // "reporter"
        reporterFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_REPORTER);
        if (reporterFldIdJira == null || reporterFldIdJira.equals("")) {
        	logger.error(" Не найден id поля автора заявки: " + JIRA_ISSUE_FIELD_REPORTER);
        	System.exit(-1);
        }
        // "assignee"
        assigneeFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_ASSIGNEE);
        if (assigneeFldIdJira == null || assigneeFldIdJira.equals("")) {
        	logger.error(" Не найден id поля исполнителя заявки: " + JIRA_ISSUE_FIELD_ASSIGNEE);
        	System.exit(-1);
        }                
        // "created"
        dateFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_DT);
        if (dateFldIdJira == null || dateFldIdJira.equals("")) {
        	logger.error(" Не найден id поля даты создания заявки: " + JIRA_ISSUE_FIELD_DT);
        	System.exit(-1);
        }
        // "summary"
        summaryFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_THEME);
        if (summaryFldIdJira == null || summaryFldIdJira.equals("")) {
        	logger.error(" Не найден id поля темы заявки: " + JIRA_ISSUE_FIELD_THEME);
        	System.exit(-1);
        }
        // "priority"
        priorityFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_PRIORITY);
        if (priorityFldIdJira == null || priorityFldIdJira.equals("")) {
        	logger.error(" Не найден id поля приоритета: " + JIRA_ISSUE_FIELD_PRIORITY);
        	System.exit(-1);
        } 

        telephoneFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_TEL);
        if (APP_MODE.equalsIgnoreCase(APP_MODE_CONSOLE) && (telephoneFldIdJira == null || summaryFldIdJira.equals("")) ) {
        	logger.error(" Не найден id поля телефона: " + JIRA_ISSUE_FIELD_TEL);
        	System.exit(-1);
        } 

        
        
        // "issuetype"
        istypeFldIdJira = "issuetype";//jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_TYPE);
        if (istypeFldIdJira == null || istypeFldIdJira.equals("")) {
        	logger.error(" Не найден id поля типа заявки: " + JIRA_ISSUE_FIELD_TYPE);
        	System.exit(-1);
        }
        // "issuelinks"
        islinktypeFldIdJira = jiraUtils.getJiraIssueLinkTypeFields().get(JIRA_ISSUE_FIELD_LINK_TYPE_VALUE);
        if (islinktypeFldIdJira == null || islinktypeFldIdJira.equals("")) {
        	logger.error(" Не найден id поля связанных запросов заявки: " + JIRA_ISSUE_FIELD_LINK_TYPE_VALUE);
        	System.exit(-1);
        }
        // "description"
        descriptionFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_DESC);
        if (descriptionFldIdJira == null || descriptionFldIdJira.equals("")) {
        	logger.error(" Не найден id поля описания заявки: " + JIRA_ISSUE_FIELD_DESC);
        	System.exit(-1);
        }
        // "resolution"
//        resolutionFldIdJira = jiraUtils.getJiraIssuesFields().get(JIRA_ISSUE_FIELD_RESOLUTION);
//        if (resolutionFldIdJira == null || resolutionFldIdJira.equals("")) {
//        	logger.error(" Не найден id поля резолюции заявки: " + JIRA_ISSUE_FIELD_RESOLUTION);
//        	System.exit(-1);
//        }
//        JIRA_ISSUE_RESOLUTION_RESOLVED_ID = jiraUtils.getJiraResolutions().get(JCCM.JIRA_ISSUE_RESOLUTION_RESOLVED_NAME);
//        if (JIRA_ISSUE_RESOLUTION_RESOLVED_ID == null || JIRA_ISSUE_RESOLUTION_RESOLVED_ID.equals("")) {
//        	logger.error(" Не найден id наименования резолюции: " + JIRA_ISSUE_RESOLUTION_RESOLVED_NAME);
//        	System.exit(-1);
//        }
//        JIRA_ISSUE_RESOLUTION_UNRESOLVED_ID = jiraUtils.getJiraResolutions().get(JCCM.JIRA_ISSUE_RESOLUTION_UNRESOLVED_NAME);
//        if (JIRA_ISSUE_RESOLUTION_UNRESOLVED_ID == null || JIRA_ISSUE_RESOLUTION_UNRESOLVED_ID.equals("")) {
//        	logger.error(" Не найден id наименования резолюции: " + JIRA_ISSUE_RESOLUTION_UNRESOLVED_NAME);
//        	System.exit(-1);
//        }      
        
        
        
        
        
        projectKeyCurJira =jiraUtils.getJiraProjectFields().get(JIRA_PROJECT_KEY);
        if (ASSIGNEE_FIO != null && !ASSIGNEE_FIO.isEmpty()) {
        	assigneeCurJira = ASSIGNEE_FIO;
        }
        else {
        	assigneeCurJira = JIRA_USERNAME;	
        }        
        frmTel.pnlAbonentFillData(other);
        filterIssue(JCCM.FORM_ISSUE_NUM_VAR1);
        dialog.setVisible(false);
        if (APP_MODE.equalsIgnoreCase(APP_MODE_CONSOLE)) {
        	FrmIssue frmIssue = frmTel.createIssue();
        	if (frmIssue != null) {
        		setThemeIssueCurJira("Звонок абонента " + other);
        		setTelephoneVal(other);
        		frmIssue.createIssue();
        	}
        }
        else {
            frmTel.setVisible(true);
            frmTel.repaint();        	
        }
    }

    public String getPriorityFldIdJira() {
		return priorityFldIdJira;
	}

	public String getEmailCur() {
		return emailCur;
	}

	public void setEmailCur(String emailCur) {
		this.emailCur = emailCur;
	}

	public String getProjectKeyCurJira() {
		return projectKeyCurJira;
	}

	public String getIslinktypeFldIdJira() {
		return islinktypeFldIdJira;
	}

	public void setIslinktypeFldIdJira(String islinktypeFldIdJira) {
		this.islinktypeFldIdJira = islinktypeFldIdJira;
	}

	public String getDescriptionFldIdJira() {
		return descriptionFldIdJira;
	}

	/**
     * Фильтрация запросов
     * @param тип фильтрации
     * @throws ExecutionException 
     * @throws InterruptedException 
     */    
    public void filterIssue(String fltType) throws InterruptedException, Exception {
    	if (issueCurJira != null) {
    		issueCurJira.clear();
    	}    	
        issueCurJira = jiraUtils.findIssuesTmp(projectFldIdJira, reporterFldIdJira, reporterCurJira, dateFldIdJira, fltType);        
        frmTel.tblIssuesFillData(issueCurJira.toArray(new String[0][0]));
	}
    
    public void closeFrame() {
        frmTel.resetData();
        frmTel.setVisible(false);
    }

    public synchronized boolean isAnswered() {
        return frmTel.isAnswered();
    }

    public synchronized void setAnswered(boolean b) {
        frmTel.setAnswered(b);
    }

}
