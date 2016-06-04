/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.it_zabota.jira.telephony.jira;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpException;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.util.concurrent.Promise;
import com.it_zabota.jira.telephony.encryptng.SecurityFile;
import com.it_zabota.jira.telephony.issue.LinkerIssueJson;
import com.it_zabota.jira.telephony.main.JCCM;
import com.it_zabota.jira.telephony.utils.SysUtils;
import com.sun.jersey.api.client.ClientHandlerException;

/**
 * Класс взаимодействия с JIRA
 * 
 * @author Vitaly Ermilov <vitaliyerm@gmail.com>
 * @version 1.0
 * @since 06.10.2014
 */
public class JiraUtils {
	public HashMap<String, String> getJiraIssueTypeFields() {
		return jiraIssueTypeFields;
	}

	public HashMap<String, String> getJiraIssueLinkTypeFields() {
		return jiraIssueLinkTypeFields;
	}


	private JCCM jccm;
    private JiraRestClient restClient = null;
    // Коллекция Почта-Имя
    private ConcurrentMap<String,JiraUserFields> jiraUsers = null;
    // Коллекция Имя поля - ID поля    
    private HashMap<String,String> jiraIssuesFields = null;
    private HashMap<String,String> jiraIssueTypeFields = null;
    private HashMap<String, String> jiraPriorityFields = null;
    public HashMap<String, String> getJiraPriorityFields() {
		return jiraPriorityFields;
	}


	private HashMap<String,String> jiraIssueLinkTypeFields = null;
    private HashMap<String,String> jiraProjectFields = null;
    
//	private HashMap<String, String> jiraResolutions = null;
    
//    public HashMap<String, String> getJiraResolutions() {
//		return jiraResolutions;
//	}

	public HashMap<String, String> getJiraProjectFields() {
		return jiraProjectFields;
	}


	private static final String CONCURRENCY_LEVEL_DEFAULT = "50";
    private static final String CONCURRENCY_KEY = "concurrency";    
    private final int concurrencyLevel = Integer.parseInt(System.getProperty(CONCURRENCY_KEY, CONCURRENCY_LEVEL_DEFAULT));
    private ConcurrentMap<String,JiraUserFields> mapUserNameMail = new ConcurrentHashMap<>();
    
    public JiraRestClient getRestClient() {
		return restClient;
	}

	public ConcurrentMap<String, JiraUserFields> getJiraUsers() {
		return jiraUsers;
	}

	public HashMap<String, String> getJiraIssuesFields() {
		return jiraIssuesFields;
	}

	public String getNetAdress() {
		return netAdress;
	}

	public String getNetAuthentification() {
		return netAuthentification;
	}

	public static Logger getLogger() {
		return logger;
	}

	public TrustManager[] getTrustAllCerts() {
		return trustAllCerts;
	}

	private String netAdress = SysUtils.getUrl("http://", JCCM.JIRA_HOST,JCCM.JIRA_PORT);
    private String netAuthentification = SysUtils.getAuthentificationString(JCCM.JIRA_USERNAME, JCCM.JIRA_PASSWORD);
    private static Logger logger = Logger.getLogger(JiraUtils.class);
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Trust always
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Trust always
            }
        }
    };


    public JiraUtils() {
    	
    }

    /**
     * Инициализация класса работы с Jira
     * @throws Throwable 
     */    
    public void init(JCCM jccm) throws Throwable {
    	this.jccm = jccm;
        initREST();
        String path = System.getProperty("user.dir");
        new File(path.concat("\\tmp")).mkdir();        
        jiraIssuesFields = getJiraFields(netAuthentification, netAdress);
        String pathFile = new String(path.concat("//tmp//tmp0001.ser"));
    	jiraUsers = setJiraUsers(pathFile);
        
        jiraIssueTypeFields = getJiraIssueType(netAuthentification, netAdress);
        jiraPriorityFields = getJiraPriority(netAuthentification, netAdress);        
        jiraIssueLinkTypeFields = getJiraIssueLinkType(netAuthentification, netAdress);
        jiraProjectFields = getJiraProjects(netAuthentification, netAdress);
//        jiraResolutions = getJiraResolutions(netAuthentification, netAdress);
    }


    /**
     * Установка мапов Jira из файла или через подключение к Jira
     * @throws Throwable 
     */        
    public ConcurrentMap<String, JiraUserFields> setJiraUsers(String path) throws Throwable {
        File fileIn = new File(path);    
        File fileOut = new File(path.concat("q"));	
    	ConcurrentMap<String, JiraUserFields> res = new ConcurrentHashMap<>();
    	String separator = JCCM.SPLITTER;    	
    	if (fileIn.exists()) {
			FileInputStream fis = new FileInputStream(path);
			FileOutputStream fos = new FileOutputStream(path.concat("q"));    		
 		    SecurityFile.decrypt(JCCM.FILE_ENCRIPT_KEY, fis, fos, JCCM.WORK_FILES_ENCRYPTING );
    		Scanner scanner = null;
    			try {
    				scanner = new Scanner(fileOut);
    		        scanner.useDelimiter(separator);
    		        String nextExp;
    		        while (scanner.hasNext())
    		        {	      	
    	        		nextExp = scanner.nextLine();
//    	        		logger.debug("Строка пользователей джиры:" + nextExp);
    	        		String[] nextExpAr = nextExp.split("\\".concat(separator));
//    	        		for (int i = 0; i < nextExpAr.length; i++) {
//    	        			logger.debug("Пользователь джиры:" + nextExpAr[i]);
//						}
    	        		JiraUserFields juf = new JiraUserFields();
    	        		if (nextExpAr.length > 0) {
        	        		juf.setName(nextExpAr[0]);    	        			
    	        		}
    	        		if (nextExpAr.length > 1) {
    	        			juf.setDisplayName(nextExpAr[1]);       			
    	        		}
    	        		if (nextExpAr.length > 2) {
    	        			juf.setEmailAddress(nextExpAr[2]);     			
    	        		}
    	        		res.put(nextExpAr[0], juf);
    	        		
    		        }
    			} 
    			catch (UnsupportedOperationException e) {
    				e.printStackTrace();
    				System.exit(-1);
    			}
    			catch (FileNotFoundException e) {
    				logger.error("Не удалось прочитать файл с пользователями Ldap ".concat(fileOut.getCanonicalPath()));    				
    				e.printStackTrace();
    				System.exit(-1);    				
    			}
    			catch (ClassCastException  e) {
    				e.printStackTrace();
    				System.exit(-1);
    			}
    			catch (IllegalArgumentException  e) {
    				e.printStackTrace();
    				System.exit(-1);
    			}
    			catch (NullPointerException  e) {
    				e.printStackTrace();
    				System.exit(-1);
    			}
    			finally {				
    	 		    fis.close();
    	 		    fos.close();    				
			        scanner.close();
			        fileOut.delete();
				}    	         
    		logger.debug("Deserialized Jira Users data...");
    		fileOut.delete();
    	}
    	else {
    		logger.info("Загрузка данных пользователей Jira"); 
    		res = getJiraUsers(netAuthentification, netAdress);   	   
    	}
    	return res;
	}    
    
    /**
     * Инициализация экземпляра подключения через протокол REST
     * @throws Exception
     */
    private void initREST() throws Exception {
        logger.debug("Инициализация протокола REST...");
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri = new URI(netAdress);        
        restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, JCCM.JIRA_USERNAME, JCCM.JIRA_PASSWORD);
    }
    
    
    /**
     * Метод получения списка пользователей Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар имя пользователя - почтовый ящик
     * @throws AuthenticationException 
     * @throws ClientHandlerException 
     * @throws JSONException
     * @throws InterruptedException 
     */    
    private ConcurrentMap<String,JiraUserFields> getJiraUsers(String auth, String url) throws AuthenticationException, ClientHandlerException, JSONException, InterruptedException {
    	
    	// JCCM.JIRA_REST_URL_USER_GROUP_USERS
    	String urlGroupUsers = url.concat(JCCM.JIRA_REST_URL_USER_GROUP_USERS);
    	logger.debug("urlGroupUsers="+urlGroupUsers);
    	String urlGroup = urlGroupUsers.substring(0, urlGroupUsers.indexOf("&"));
    	logger.debug("urlGroup="+urlGroup);
    	HashMap<String, Integer> groupSize = new HashMap<>();
	    	 for (int j = 0; j < JCCM.JIRA_USER_GROUPS.length; j++)
	    	 {
	    		 try {	    		 
	    			String strEntitity = SysUtils.invokeGetMethod(auth, urlGroup.replace(JCCM.JIRA_REST_URL_USER_GROUP_USERS_GROUP_REGEXP, JCCM.JIRA_USER_GROUPS[j]));
	    			logger.debug("strEntitity="+strEntitity);
	    			logger.debug("JSONObjectstrEntitity="+new JSONObject(strEntitity).getJSONObject("users"));
	    			Integer size = (Integer) new JSONObject(strEntitity).getJSONObject("users").get("size");
	    			logger.debug("size="+size);
	    			logger.debug("JCCM.JIRA_USER_GROUPS[j]="+JCCM.JIRA_USER_GROUPS[j]);
	    			groupSize.put(JCCM.JIRA_USER_GROUPS[j], size);
	    			logger.debug("JCCM.JIRA_USER_GROUPS[j] = " + JCCM.JIRA_USER_GROUPS[j]);
	    			logger.debug("size="+size);
	 			} catch (AuthenticationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientHandlerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		 catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	    		 
	    		 catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 		    		 
		 	 }
	    	 logger.debug("groupSize.size()="+groupSize.size());
	    	 logger.debug("groupSize.get(JCCM.JIRA_USER_GROUPS[0]))="+groupSize.get(JCCM.JIRA_USER_GROUPS[0]));
	    	 
    	final ExecutorService executor = Executors.newCachedThreadPool();
		try {
			logger.debug("JCCM.JIRA_USER_GROUPS="+JCCM.JIRA_USER_GROUPS);			
			logger.debug("JCCM.JIRA_USER_GROUPS.length="+JCCM.JIRA_USER_GROUPS.length);
	    	 for (int j = 0; j < JCCM.JIRA_USER_GROUPS.length; j++)	    		 
				{
	    		 logger.debug("Step 1");
					for (int k = 0; k <= groupSize.get(JCCM.JIRA_USER_GROUPS[j]) / 50 ; k++) {
						logger.debug("Step 2");						
						executor.submit(new Runnable() 
						{
							private String auth;
							private String url;
							private int j;
							private int k;
							public void run()
							{
								logger.debug("Step 3");								
					    		 try {
						    			logger.debug("Поток ".concat(Thread.currentThread().getName()).concat(" вывода набора по 50 записей начал работу"));
										mapUserNameMail.putAll(SysUtils.getEav(auth, 
												url.replace(JCCM.JIRA_REST_URL_USER_GROUP_USERS_GROUP_REGEXP, JCCM.JIRA_USER_GROUPS[j])
												.replace(JCCM.JIRA_REST_URL_USER_GROUP_USERS_GROUP_REGEXP1, String.valueOf(k * JCCM.JIRA_REST_URL_USER_GROUP_USERS_FETCH_SIZE))
												.replace(JCCM.JIRA_REST_URL_USER_GROUP_USERS_GROUP_REGEXP2, String.valueOf((k + 1) * (JCCM.JIRA_REST_URL_USER_GROUP_USERS_FETCH_SIZE - 1)))
												, JCCM.JIRA_USER_FIELD_LOGIN, JCCM.JIRA_USER_FIELD_ARRAY));				    			 
						    			logger.debug("Поток ".concat(Thread.currentThread().getName()).concat(" вывода набора по 50 записей закончил работу"));

									} catch (AuthenticationException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (ClientHandlerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (Throwable e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} 								
							}
						
					    private Runnable init(int j, int k, String auth, String url){
					        this.auth = auth;
					        this.url = url;
					        this.k = k;
					        this.j = j;
					        return this;
					    }
					}.init(j, k, auth, urlGroupUsers));
				}	
			}
		}
		finally 
		{
			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} 
			catch (InterruptedException e) 
			{
			  logger.error("Executor service of getting Jira Users was interrupted");
			}		
		}  
		
    	return mapUserNameMail;
    }       
    
    /**
     * Метод получения списка пользователей Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар имя пользователя - почтовый ящик
     * @throws AuthenticationException 
     * @throws ClientHandlerException 
     * @throws JSONException
     * @throws InterruptedException 
     */   
    @Deprecated
    private ConcurrentMap<String,JiraUserFields> getJiraUsersOld(String auth, String url) throws AuthenticationException, ClientHandlerException, JSONException, InterruptedException {
    	final String[] ch_foreign_alph = 
    		{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","1","2","3","4","5","6","7","8","9","0","_"};
//    	final String[] ch = 
//    		{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
//    			"0","1","2","3","4","5","6","7","8","9","_" 
//    			,
//    		"й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "ъ", "ф", "ы", "в", "а", "п",
//    			 "р", "о", "л", "д", "ж", "э", "я", "ч", "с", "м", "и", "т", "ь", "б", "ю",
//    			 "Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З", "Х", "Ъ", "Ф", "Ы", "В", "А", "П",
//    			 "Р", "О", "Л", "Д", "Ж", "Э", "Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю"};   	
    	final ExecutorService executor = Executors.newCachedThreadPool();
		try {
//			for(int i = 0; i < concurrencyLevel; i++) 
//				{
		    	 for (int j = 0; j < ch_foreign_alph.length; j++) 
				{
					executor.submit(new Runnable() 
					{
						private String[] ch;
						private String auth;
						private String url;
						private int j;
						public void run()
						{
							for (int k = 0; k < ch_foreign_alph.length; k++) {
					    		 try {
					    			 	String curCharSeq = ch[j].concat(ch[k]);
					    			 	
//					    			 		logger.info(curCharSeq);
					    			 	
						    			logger.debug("Поток ".concat(Thread.currentThread().getName()).concat(" поиска по символу начал работу"));
										mapUserNameMail.putAll(SysUtils.getEav(auth, url+JCCM.JIRA_REST_URL_USER_CURRENT+URLEncoder.encode(curCharSeq, JCCM.JIRA_ENCODING), JCCM.JIRA_USER_FIELD_LOGIN, JCCM.JIRA_USER_FIELD_ARRAY));				    			 
						    			logger.debug("Поток ".concat(Thread.currentThread().getName()).concat(" поиска по символу закончил работу"));

									} catch (AuthenticationException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (ClientHandlerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (Throwable e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} 								
							}

						}
						
					    private Runnable init(String[] ch, int j, String auth, String url){
					        this.ch = ch;
					        this.auth = auth;
					        this.url = url;
					        this.j = j;
					        return this;
					    }
					}.init(ch_foreign_alph, j, auth, url));
				}			
			}
		finally 
		{
			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} 
			catch (InterruptedException e) 
			{
			  logger.error("Executor service of getting Jira Users was interrupted");
			}		
		}  
    	return mapUserNameMail;
    }   
    
    /**
     * Метод получения коллекции пар id поля - имя поля в обращениях Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар id поля - имя поля
     * @throws Throwable 
     */    
    private static HashMap<String,String> getJiraFields(String auth, String url) throws Throwable {
    	HashMap<String,String> mapFldIdVal = new HashMap<String,String>();
    	mapFldIdVal.putAll(SysUtils.getEav(auth, url+JCCM.JIRA_REST_URL_FIELD_LIST, JCCM.JIRA_ISSUE_FIELD_NAME, JCCM.JIRA_ISSUE_FIELD_ID, "FIELDS")); 
    	return mapFldIdVal;
    }      
    

    /**
     * Метод получения коллекции пар id резолюции - имя резолюции в обращениях Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар id резолюции - имя резолюции
     * @throws Throwable 
     */    
//    private static HashMap<String,String> getJiraResolutions(String auth, String url) throws Throwable {
//    	HashMap<String,String> mapFldIdVal = new HashMap<String,String>();
//    	mapFldIdVal.putAll(SysUtils.getEav(auth, url+JCCM.JIRA_REST_URL_RESOLUTION_LIST, JCCM.JIRA_ISSUE_FIELD_NAME, JCCM.JIRA_ISSUE_FIELD_ID, "RESOLUTIONS")); 
//    	return mapFldIdVal;
//    }      


    /**
     * Метод получения коллекции пар id поля - имя поля в проектах Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар id поля - имя поля
     * @throws Throwable 
     */    
    private static HashMap<String,String> getJiraProjects(String auth, String url) throws Throwable {
    	HashMap<String,String> mapFldIdVal = new HashMap<String,String>();
    	mapFldIdVal.putAll(SysUtils.getEav(auth, url+JCCM.JIRA_REST_URL_PROJECT_LIST, JCCM.JIRA_ISSUE_FIELD_KEY, JCCM.JIRA_ISSUE_FIELD_ID, "PROJECTS")); 
    	return mapFldIdVal;
    }      
    

    /**
     * Метод получения списка id поля - имя поля в типах обращений Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар id поля - имя поля
     * @throws Throwable 
     */    
    private static HashMap<String,String> getJiraIssueType(String auth, String url) throws Throwable {
    	HashMap<String,String> mapFldIdVal = new HashMap<String,String>();
    	mapFldIdVal.putAll(SysUtils.getEav(auth, url+JCCM.JIRA_REST_URL_ISSUETYPE_LIST, JCCM.JIRA_ISSUE_FIELD_NAME, JCCM.JIRA_ISSUE_FIELD_ID, "ISSUETYPE")); 
    	return mapFldIdVal;
    }      
    
    /**
     * Метод получения списка id поля - имя поля в приоритетах Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар id поля - имя поля
     * @throws Throwable 
     */    
    private static HashMap<String,String> getJiraPriority(String auth, String url) throws Throwable {
    	HashMap<String,String> mapFldIdVal = new HashMap<String,String>();
    	mapFldIdVal.putAll(SysUtils.getEav(auth, url+JCCM.JIRA_REST_URL_PRIORITY_LIST, JCCM.JIRA_ISSUE_FIELD_NAME, JCCM.JIRA_ISSUE_FIELD_ID, "PRIORITY")); 
    	return mapFldIdVal;
    }        
    
    
    /**
     * Метод получения списка id поля - имя поля в типах связи обращений Jira
     * @param строка аутентификации в Jira
     * @param сетевой адрес подключения к Jira
     * @return коллекция пар id поля - имя поля
     * @throws Throwable 
     */    
    private static HashMap<String,String> getJiraIssueLinkType(String auth, String url) throws Throwable {
    	HashMap<String,String> mapFldIdVal = new HashMap<String,String>();
    	mapFldIdVal.putAll(SysUtils.getEav(auth, url+JCCM.JIRA_REST_URL_ISSUELINKTYPE_LIST, JCCM.JIRA_ISSUE_FIELD_NAME, JCCM.JIRA_ISSUE_FIELD_ID, "ISSUELINKTYPE")); 
    	return mapFldIdVal;
    }      
    
    
    
    /**
     * Метод генерации URL для создания запроса в система JIRA
     * @return строка запроса
     * @throws Throwable 
     * @throws HttpException 
     * @throws ProtocolException 
     * @throws Exception
     */
    public String createIssue(String idLinkIssue) throws ProtocolException, HttpException, Throwable {
        logger.info("Создание запроса...");
        String issueName = "";
//        String projId = restClient.getProjectClient().getProject(JCCM.JIRA_PROJECT).get()
        if (JCCM.SAVE_JIRA_ISSUES_IMMEDIATELY.equalsIgnoreCase("ДА") || idLinkIssue != null) 
        {
        	IssueInputBuilder issueInputBuilder = new
        			IssueInputBuilder(JCCM.JIRA_PROJECT_KEY, Long.valueOf(jccm.getTypIssueIdCurJira()), jccm.getThemeIssueCurJira());
        	if (jccm.getReporterCurJira()!=null && !jccm.getReporterCurJira().equals("")) {
        		issueInputBuilder.setReporterName(jccm.getReporterCurJira());	
        	}        	
        	if (jccm.getDescriptionIssueCurJira() != null && !jccm.getDescriptionIssueCurJira().equals("") ) {
        		issueInputBuilder.setDescription(jccm.getDescriptionIssueCurJira());	
        	}    
        	issueInputBuilder.setPriorityId(jccm.getPriorityIdCurJira());
        	issueInputBuilder.setAssigneeName(jccm.getAssigneeCurJira());
        	
        	// Добавление номера телефона
        	if (JCCM.APP_MODE.equalsIgnoreCase(JCCM.APP_MODE_CONSOLE) && jccm.getTelephoneVal() != null && !jccm.getTelephoneVal().isEmpty()) {
        		issueInputBuilder.setFieldValue(jccm.getTelephoneFldIdJira(), jccm.getTelephoneVal());
        	}
//        	try {
//            	issueInputBuilder.setFieldValue("resolution", (Object)"5");				
//			} catch (CannotTransformValueException e) {
//				// TODO: handle exception
//		        logger.error("Не может присвоить значение приоритета. Ошибка трансформации.");
//		        logger.error(e);
//			}
//        	issueInputBuilder.setFieldValue(jccm.getResolutionFldIdJira(), JCCM.JIRA_ISSUE_RESOLUTION_RESOLVED_ID);
        	IssueInput issueInput = issueInputBuilder.build();
        	Promise<BasicIssue> createdIssue = restClient.getIssueClient().createIssue(issueInput);
        	BasicIssue issue = createdIssue.get();
        	String issueKey = issue.getKey();
        	
        	// Update resolution        	  
        	// String input="{\"fields\": {\"customfield_10156\": \"Testing through REST\"} }"; 
//        	
//        	{
//        	    "fields": {
//        	        "resolution": {
//        	            "name": "Fixed"
//        	        }
//        	    }
//        	}
        	
//            String input="{\"fields\": {\"".concat(jccm.getResolutionFldIdJira()).concat("\": {\"id\": \"").concat(JCCM.JIRA_ISSUE_RESOLUTION_RESOLVED_ID).concat("\"} } }");            
//            String input="{\"fields\": {\"".concat(jccm.getResolutionFldIdJira()).concat("\": {\"name\": \"").concat(JCCM.JIRA_ISSUE_RESOLUTION_RESOLVED_NAME).concat("\"} } }");        	
//            int retn = SysUtils.invokePutMethod(netAuthentification, netAdress+JCCM.JIRA_REST_URL_ISSUE+"/"+issueKey, input);
        	if (idLinkIssue != null) {
        		String linkPostRetn = linkIssue(idLinkIssue, issueKey);
        		if (linkPostRetn.trim().equals("200")) {
        			logger.info("The issue link was created successfully");
        		}
        	}
        	
        	issueName = "browse/" + issue.getKey();
        }
        else
        { 
            StringBuilder builder = new StringBuilder();
            builder.append("secure/CreateIssueDetails!init.jspa?");

            builder.append(jccm.getSummaryFldIdJira());
            builder.append("=");
//          builder.append(URLEncoder.encode("Заявка от абонета " + jccm.getTelephoneAbonentCurAd(), JCCM.JIRA_ENCODING));
            builder.append(URLEncoder.encode(jccm.getThemeIssueCurJira(), JCCM.JIRA_ENCODING));
            builder.append("&");
            builder.append(JCCM.JIRA_FIELD_PROJECT);
            builder.append("=");
            builder.append(jccm.getProjectKeyCurJira());
            builder.append("&");
            builder.append(jccm.getAssigneeFldIdJira());
            builder.append("=");
            builder.append(URLEncoder.encode(String.valueOf(jccm.getAssigneeCurJira()), JCCM.JIRA_ENCODING));
//            builder.append("&");
//            builder.append("resolution");
//            builder.append("=");
//            builder.append(URLEncoder.encode("1", JCCM.JIRA_ENCODING));            
            builder.append("&");
            builder.append(jccm.getPriorityFldIdJira());
            builder.append("=");
            builder.append(URLEncoder.encode(String.valueOf(jccm.getPriorityIdCurJira()), JCCM.JIRA_ENCODING));
            builder.append("&");
            builder.append(jccm.getIstypeFldIdJira());
            builder.append("=");
            builder.append(URLEncoder.encode(String.valueOf(jccm.getTypIssueIdCurJira()), JCCM.JIRA_ENCODING));            
            if (jccm.getReporterCurJira()!=null && !jccm.getReporterCurJira().equals("")) {
                builder.append("&");
                builder.append(jccm.getReporterFldIdJira());
                builder.append("=");
                builder.append(URLEncoder.encode(jccm.getReporterCurJira(), JCCM.JIRA_ENCODING));              	
            }         
            if (jccm.getDescriptionIssueCurJira()!=null && !jccm.getDescriptionIssueCurJira().equals("")) {
                builder.append("&");
                builder.append(jccm.getDescriptionFldIdJira());
                builder.append("=");
                builder.append(URLEncoder.encode(jccm.getDescriptionIssueCurJira(), JCCM.JIRA_ENCODING));             	
            }              
        	// Добавление номера телефона
        	if (JCCM.APP_MODE.equalsIgnoreCase(JCCM.APP_MODE_CONSOLE) && jccm.getTelephoneVal() != null && !jccm.getTelephoneVal().isEmpty()) {
                builder.append("&");
                builder.append(jccm.getTelephoneFldIdJira());
                builder.append("=");
                builder.append(URLEncoder.encode(jccm.getTelephoneVal(), JCCM.JIRA_ENCODING));            		
        	}            
            issueName = builder.toString();     
            logger.debug(builder);        	        	
        }

        return issueName;
    }
    
    private String linkIssue(String keyInwardIssue, String keyOutwardIssue) throws JSONException, AuthenticationException, ClientHandlerException {
    	LinkerIssueJson lnkIsJson = new LinkerIssueJson(keyInwardIssue, keyOutwardIssue);
    	String retn = SysUtils.invokePostMethod(netAuthentification, netAdress+JCCM.JIRA_REST_URL_ISSUELINK, lnkIsJson.getIssueLink().toString());
    	return retn;
	}

    public String mapSLA(String sla) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("sla.properties"));
        return properties.getProperty(sla);
    }

    /**
     * Метод поиска запросов Jira
     * @param request строка запроса в формате JQL
     * @return массив запросов
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public Promise<SearchResult> findIssues(String request) throws RestClientException {
        Promise<SearchResult> sr = restClient.getSearchClient().searchJql(request);        
        return sr;
    }
    
    /**
     * Метод конвертации набора данных запросов Jira в требуемый поисковой формой интерфейса массив
     * @param набор данных
     * @param массив полей формы
     * @return массив запросов 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public Object[] getFormIssues(Promise<SearchResult> sr, List<String> formFld) throws RestClientException, InterruptedException, ExecutionException {
    	List<Object> listObj = new ArrayList<Object>();
        for (Iterator<BasicIssue> it = sr.get().getIssues().iterator(); it.hasNext();) {
            Issue is = restClient.getIssueClient().getIssue(it.next().getKey()).get();
        	Vector<String> listString = new Vector<String>();
            for (String fld : formFld) {
            	Field fldCur = is.getFieldByName(fld);
            	if (fldCur != null) {
            		listString.add(fldCur.getValue().toString());
            	}
			}
            if (!listString.isEmpty()) {
            	listObj.add(listString);	
            }             
        }
        if (!listObj.isEmpty()) {
        	return listObj.toArray();
        }
        return null;   
    }
    
 
       
    /**
     * Метод заполнения вектора запросов по названию проекта, id пользователя, создавшего запрос, фильтру запроса по дате изменения.
     * @param ид поля Jira проекта
     * @param ид поля Jira пользователя, создавшего запрос
     * @param имя Jira пользователя, создавшего запрос
     * @param имя поля Jira даты последних изменений запроса
     * @param фильтр запроса по дате изменения
     * @return вектор запросов
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws ParseException 
     */
    public List<String[]> findIssuesTmp(String projectFldIdJira, String reporterFldIdJira, String reporterCurJira, String dateFldIdJira, String formIssueNumVar) throws RestClientException, InterruptedException, ExecutionException, ParseException {
    	List<String[]> result = new ArrayList<String[]>();
    	String searchMsg = "";
    	if (formIssueNumVar.equalsIgnoreCase(JCCM.FORM_ISSUE_NUM_VAR1)) {
    		// abonent
            searchMsg = reporterFldIdJira + "='" + reporterCurJira + "' "; 
            if (!JCCM.JIRA_FILTER_ABONENT_PROJECT_KEY[0].equalsIgnoreCase("ALL")) {
            	searchMsg = searchMsg.concat(" AND ").concat(jccm.getProjectFldIdJira()).concat(" IN (");
            	for (int i = 0; i < JCCM.JIRA_FILTER_ABONENT_PROJECT_KEY.length; i++) {
            		searchMsg = searchMsg.concat("'").concat(JCCM.JIRA_FILTER_ABONENT_PROJECT_KEY[i]).concat("',");
				}
            	searchMsg = searchMsg.substring(0, searchMsg.length()-1).concat(") ");
            }
            if (!JCCM.JIRA_FILTER_ABONENT_ISSUE_TYPE[0].equalsIgnoreCase("ALL")) {
            	searchMsg = searchMsg.concat(" AND ").concat(jccm.getIstypeFldIdJira()).concat(" IN (");
            	for (int i = 0; i < JCCM.JIRA_FILTER_ABONENT_ISSUE_TYPE.length; i++) {
            		searchMsg = searchMsg.concat("'").concat(JCCM.JIRA_FILTER_ABONENT_ISSUE_TYPE[i]).concat("',");
				}
            	searchMsg = searchMsg.substring(0, searchMsg.length()-1).concat(") ");
            }            
            searchMsg = searchMsg.concat(SysUtils.jqlFormIssueGet(formIssueNumVar, dateFldIdJira) + " ORDER BY " + dateFldIdJira);
         // abonent
    	}
    	else {
            if (!JCCM.JIRA_FILTER_GLOBAL_PROJECT_KEY[0].equalsIgnoreCase("ALL")) {
            	searchMsg = searchMsg.concat(jccm.getProjectFldIdJira()).concat(" IN (");
            	for (int i = 0; i < JCCM.JIRA_FILTER_GLOBAL_PROJECT_KEY.length; i++) {
            		searchMsg = searchMsg.concat("'").concat(JCCM.JIRA_FILTER_GLOBAL_PROJECT_KEY[i]).concat("',");
				}
            	searchMsg = searchMsg.substring(0, searchMsg.length()-1).concat(") ");
            }
            if (!JCCM.JIRA_FILTER_GLOBAL_ISSUE_TYPE[0].equalsIgnoreCase("ALL")) {
            	if (!searchMsg.equals("")) {
            		searchMsg = searchMsg.concat( " AND ");
            	}
            	searchMsg = searchMsg.concat(jccm.getIstypeFldIdJira()).concat(" IN (");
            	for (int i = 0; i < JCCM.JIRA_FILTER_GLOBAL_ISSUE_TYPE.length; i++) {
            		searchMsg = searchMsg.concat("'").concat(JCCM.JIRA_FILTER_GLOBAL_ISSUE_TYPE[i]).concat("',");
				}
            	searchMsg = searchMsg.substring(0, searchMsg.length()-1).concat(") ");
            }  
        	if (!searchMsg.equals("")) {
        		searchMsg = searchMsg.concat( " AND ");
        	}
//    		searchMsg = searchMsg + jccm.getResolutionFldIdJira() + " = "+ JCCM.JIRA_ISSUE_RESOLUTION_UNRESOLVED_ID + " " + SysUtils.jqlFormIssueGet(formIssueNumVar, dateFldIdJira) + " ORDER BY " + dateFldIdJira;    		
    		searchMsg = searchMsg + " " + SysUtils.jqlFormIssueGet(formIssueNumVar, dateFldIdJira) + " ORDER BY " + dateFldIdJira;    		
    	}
        logger.info("Выражение для поиска обращений: " + searchMsg);
        Promise<SearchResult> sr = null;
        try {
        	sr = restClient.getSearchClient().searchJql(searchMsg);
		} catch (RestClientException e) {
			// TODO: handle exception
			logger.error("Критическая ошибка поиска!");
			logger.error(e);
		    System.exit(-1);
		}
        
        int i = 0;
        DateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");        
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");        
        for (Iterator<BasicIssue> it = sr.get().getIssues().iterator(); it.hasNext();) {
            i++;
            BasicIssue issue = it.next();
            Issue is = null;
            try {
                is = restClient.getIssueClient().getIssue(issue.getKey()).get();				
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("Критическая ошибка поиска!");
				logger.error(e);
			    System.exit(-1);				
			}
            result.add(new String[] {is.getKey(), is.getIssueType().getName(), is.getSummary(), formatter.format(parser.parse(((dateFldIdJira.equalsIgnoreCase("CREATED")) ? is.getCreationDate() : is.getUpdateDate()).toString())), is.getStatus().getName()});
//            logger.info(is.getKey() + " "+is.getIssueType().getName()+" "+ is.getSummary()+ " " +is.getUpdateDate().toString()+ " " + is.getStatus().getName());
            if (formIssueNumVar.equalsIgnoreCase(String.valueOf(JCCM.FORM_ISSUE_NUM_VAR1)) && JCCM.FORM_ISSUE_NUM_PAR1==i) {
            	return result;
            }            
        }
        return result;       
    }   

    /**
     * Метод получения вектора значений полей по всем запросам
     * @param имя поля
     * @return вектор организаций
     * @throws Throwable 
     */        
    public Vector<String> getIssuesFldVal(String fldName) throws Throwable {
        Vector<String> result = new Vector<String>();
        Promise<SearchResult> sr = restClient.getSearchClient().searchJql(fldName + " IS NOT NULL");
        for (Iterator<BasicIssue> it = sr.get().getIssues().iterator(); it.hasNext();) {
            BasicIssue issue = it.next();
            Field fld = restClient.getIssueClient().getIssue(issue.getKey()).get().getFieldByName(fldName);
            if (fld != null) {
                String fldVal = fld.getValue().toString();
                result.add(fldVal);
            }
        }
        return result;
    }     

}
