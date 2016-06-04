package com.it_zabota.jira.telephony.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;

import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.it_zabota.jira.telephony.encryptng.SecurityFile;
import com.it_zabota.jira.telephony.jira.JiraUserFields;
import com.it_zabota.jira.telephony.ldap.LdapUtils;
import com.it_zabota.jira.telephony.main.JCCM;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Класс утилит общего пользования и по конкретной обработке согласно задачам модуля интеграции Avaya и Jira
 * 
 * @author Vitaly Ermilov <vitaliyerm@gmail.com>
 * @version 1.0
 * @since 06.10.2014
 */
public class SysUtils {
	
    private static Logger logger = Logger.getLogger(JCCM.class);
	
    /**
     * Метод получения EAV-массива данных из с удаленного приложения
     * @param строка авторизации
     * @param сетевой адрес подключения
     * @param наименование поля ключа атрибута
     * @param наименование поля значения атрибута
     * @return EAV-массив
     * @throws Throwable 
     */	    
    public static HashMap<String,String> getEav (String auth, String url, String atrKey, String atrValue, String type) throws Throwable 
    {
    	HashMap<String,String> mapAtrVal = new HashMap<String,String>();
    	String eavStr = "";
    	if (!type.equalsIgnoreCase("USERS")) {
    	   	FileWriter writer = null;
            String path = System.getProperty("user.dir");
            String pathFile = null;
            switch (type) {
			case "FIELDS":
				pathFile = path.concat("//tmp//".concat("tmp0003").concat(".ser"));
				break;
			case "ISSUELINKTYPE":
				pathFile = path.concat("//tmp//".concat("tmp0004").concat(".ser"));
				break;
			case "ISSUETYPE":
				pathFile = path.concat("//tmp//".concat("tmp0005").concat(".ser"));
				break;
			case "PROJECTS":
				pathFile = path.concat("//tmp//".concat("tmp0006").concat(".ser"));
				break;
			case "PRIORITY":
				pathFile = path.concat("//tmp//".concat("tmp0007").concat(".ser"));
				break;
//			case "RESOLUTIONS":
//				pathFile = path.concat("//tmp//".concat("tmp0008").concat(".ser"));
//				break;								
			default:
				break;
			}            
            FileInputStream fis = null;
            FileOutputStream fos = null;
            File f1 = new File(pathFile);
            File f2 = new File(pathFile.concat("q"));            
            if (f1.exists()) {
    			fis = new FileInputStream(pathFile);
    			fos = new FileOutputStream(pathFile.concat("q"));    		
     		    SecurityFile.decrypt(JCCM.FILE_ENCRIPT_KEY, fis, fos, JCCM.WORK_FILES_ENCRYPTING);            	
            	eavStr =  FileUtils .readFileToString(f2);
            	fis.close();
            	fos.close();
            	f2.delete();
            }
            else {
            	writer = new FileWriter(f2);
            	eavStr = SysUtils.invokeGetMethod(auth, url);
        		logger.debug("Rest-подключение для получение EAV полей: " + eavStr);
        		writer.write(eavStr);
        		writer.flush();
        		writer.close();                
       		    fis = new FileInputStream(pathFile.concat("q"));
       		    fos = new FileOutputStream(pathFile);            	   
       		    SecurityFile.encrypt(JCCM.FILE_ENCRIPT_KEY, fis, fos, JCCM.WORK_FILES_ENCRYPTING);    
	       		fis.close();
	         	fos.close();
	         	f2.delete();       		    
            }
    	}
    	else {
    		eavStr = SysUtils.invokeGetMethod(auth, url);
    		logger.debug("Rest-подключение для получение EAV полей: " + eavStr);
    	} 	
    	JSONArray userArray;
    	if (type.equalsIgnoreCase("ISSUELINKTYPE")) {
    		JSONObject obj = new JSONObject(eavStr);
    		userArray = obj.getJSONArray("issueLinkTypes");
    	}
    	else {
    		userArray = new JSONArray(eavStr);
    	}
        for (int k = 0; k < userArray.length(); k++)
        {
	        JSONObject obj = userArray.getJSONObject(k);
	        logger.debug("JSON-объект: " + obj);

	        String key = obj.getString(atrKey);
	        String value = obj.getString(atrValue);
	        if ((type.equalsIgnoreCase("USERS") && !mapAtrVal.containsValue(key)) || !mapAtrVal.containsKey(value)) {
	        	logger.debug("Ключ: " + key);
	        	logger.debug("Значение: " + value);
	        	mapAtrVal.put(key, value);
	        }
        }
	return mapAtrVal;    	
    }	
	
    /**
     * Метод получения EAV-массива данных из с удаленного приложения
     * @param строка авторизации
     * @param сетевой адрес подключения
     * @param наименование поля ключа атрибута
     * @param наименование поля значения атрибута
     * @return EAV-массив
     * @throws Throwable 
     */	    
    public static HashMap<String,JiraUserFields> getEav (String auth, String url, String atrKey, String[] atrValue) throws Throwable 
    {

    	HashMap<String,JiraUserFields> mapAtrVal = new HashMap<>();
    	String eavStr = "";
		eavStr = SysUtils.invokeGetMethod(auth, url);
		logger.debug("Rest-подключение для получение EAV полей: " + eavStr);
    	JSONArray userArray = new JSONObject(eavStr).getJSONObject("users").getJSONArray("items");        
        for (int k = 0; k < userArray.length(); k++)
        {
	        JSONObject obj = userArray.getJSONObject(k);
	        JiraUserFields juf = new JiraUserFields();
	        logger.debug("JSON-объект: " + obj);

	        String key = obj.getString(atrKey);
    		juf.setDisplayName(obj.getString(JCCM.JIRA_USER_FIELD_FULLNAME));
    		juf.setEmailAddress(obj.getString(JCCM.JIRA_USER_FIELD_EMAIL));
    		juf.setName(key);       	
	        if (!mapAtrVal.containsValue(key)) {
	        	logger.debug("Ключ: " + key);
	        	logger.debug("Значение: " + juf.toString());

	        	mapAtrVal.put(key, juf);
//	        	if (key.contains("ko")) {
//	        		logger.info(key );	
//	        		logger.info(mapAtrVal.get(key));
//	        	}	        	
	        }
        }
	return mapAtrVal;    	
    }	        
    
    
    /**
     * Метод получения EAV-массива данных из с удаленного приложения
     * @param строка авторизации
     * @param сетевой адрес подключения
     * @param наименование поля ключа атрибута
     * @param наименование поля значения атрибута
     * @return EAV-массив
     * @throws Throwable 
     */	    
    @Deprecated
    public static HashMap<String,JiraUserFields> getEavOld (String auth, String url, String atrKey, String[] atrValue) throws Throwable 
    {

    	HashMap<String,JiraUserFields> mapAtrVal = new HashMap<>();
    	String eavStr = "";
		eavStr = SysUtils.invokeGetMethod(auth, url);
		logger.debug("Rest-подключение для получение EAV полей: " + eavStr);	
    	JSONArray userArray;
    	userArray = new JSONArray(eavStr);        
        for (int k = 0; k < userArray.length(); k++)
        {
	        JSONObject obj = userArray.getJSONObject(k);
	        JiraUserFields juf = new JiraUserFields();
	        logger.debug("JSON-объект: " + obj);

	        String key = obj.getString(atrKey);
    		juf.setDisplayName(obj.getString(JCCM.JIRA_USER_FIELD_FULLNAME));
    		juf.setEmailAddress(obj.getString(JCCM.JIRA_USER_FIELD_EMAIL));
    		juf.setName(key);       	
	        if (!mapAtrVal.containsValue(key)) {
	        	logger.debug("Ключ: " + key);
	        	logger.debug("Значение: " + juf.toString());

	        	mapAtrVal.put(key, juf);
//	        	if (key.contains("ko")) {
//	        		logger.info(key );	
//	        		logger.info(mapAtrVal.get(key));
//	        	}	        	
	        }
        }
	return mapAtrVal;    	
    }	    
    
    /**
     * Метод получения выражения для поика по неделям
     * @param HashMap
     * @param объект-значение
     */	      
    public static String jqlFormIssueGet(String formIssue, String fldId) {
    	String retn = " ";
    	if (formIssue.equalsIgnoreCase(JCCM.FORM_ISSUE_INTERVAL_VAR2) && JCCM.FORM_ISSUE_INTERVAL_PAR2 != null && !JCCM.FORM_ISSUE_INTERVAL_PAR2.equals("")) {
    		retn = retn + fldId + ">=-"+ JCCM.FORM_ISSUE_INTERVAL_PAR2;
		}
    	return retn;
	}
    
    /**
     * Метод получения ключа по значению в HashMap
     * @param HashMap
     * @param объект-значение
     */	    
    public static Object getKeyFromValue(ConcurrentMap<String,JiraUserFields> hm, String value) {
        for (String o : hm.keySet()) {
          if (hm.get(o).getName().equals(value)) {
            return o;
          }
        }
        return null;
      }    
    
    /**
     * Метод-ядро открытия страницы в браузере
     * @param URI-идентификатор ресурса 
     */	
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

    /**
     * Метод-обертка открытия страницы в браузере
     * @param сетевой адрес URL страницы 
     */		
	public static void openWebpage(URL url) {
	    try {	    	
	        openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	}	
	
    /**
     * Получение строки аутентификации для Rest-сервиса 
     * @param имя пользователя
     * @param пароль пользователя
     * @return строка аутентификации
     */  	
	   public static String getAuthentificationString(String username, String password) {
	    	String authString = username + ":" + password;
	    	byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			return new String(authEncBytes);			        	    	
		}
	   
    /**
     * Получение строки сетевого адреса 
     * @param хост
     * @param порт
     * @return строка-сетевой адрес подключения
     */  	
	   public static String getUrl(String host_prefix, String host, String port) {
		   if (port == null || port.equals("")) {
			   // "http://"
			   return new String(host_prefix.concat(host).concat("/"));	
		   }
			return new String(host_prefix.concat(host).concat(":").concat(port).concat("/"));			        	    	
		}
	
    /**
     * REST-запрос Get: запрос - ответ 
     * @param строка ауторизации
     * @param строка сетевого адреса
     * @return строка-сетевой адрес подключения
     * @throws AuthenticationException 
     * @throws ClientHandlerException 
     */  
	public static String invokeGetMethod(String auth, String url) throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").get(ClientResponse.class);
        int statusCode = response.getStatus();
        if (statusCode == 401) {
            throw new AuthenticationException("Имя пользователя или пароль ошибочны");
        }
        if (statusCode == 403) {
            throw new AuthenticationException("Недостаточно прав для выполнения операции");
        }    
        if (statusCode == 404) {
            throw new AuthenticationException("Данный ресурс не существует");
        }             
        return response.getEntity(String.class);
    } 	
	
	   /**
     * REST-запрос Post: запрос - ответ 
     * @param строка ауторизации
     * @param строка сетевого адреса
     * @param строка запроса
     * @return строка-сетевой адрес подключения
     * @throws AuthenticationException 
     * @throws ClientHandlerException 
     */  
	public static String invokePostMethod(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").post(ClientResponse.class, data);
        int statusCode =  response.getStatus();
        if (statusCode == 401) {
            throw new AuthenticationException("Имя пользователя или пароль ошибочны");
        }
        return response.getEntity(String.class);
    } 	
	
	   /**
  * REST-запрос Put: запрос - ответ 
  * @param строка ауторизации
  * @param строка сетевого адреса
  * @param строка запроса
  * @return строка-сетевой адрес подключения
  * @throws AuthenticationException 
  * @throws ClientHandlerException 
  */  

	public static int invokePutMethod(String auth, String url, String data) throws AuthenticationException, HttpStatusException, ClientHandlerException {
     Client client = Client.create();
     WebResource webResource = client.resource(url);
     ClientResponse response = null;
     response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                 .accept("application/json").put(ClientResponse.class, data);
     int statusCode =  response.getStatus();     
     if (statusCode == 401) {
         throw new AuthenticationException("Имя пользователя или пароль ошибочны");
     }    	
     return response.getStatus();
 } 	
	
    /**
     * Метод получения набора данных из AD путем фильтрации данных AD по списку пользователей Jira по полю (логина)
     * @param экземпляр класса по работе с LDAP
     * @param Исходный набор данных AD 
     * @param Список пользователей(логин, полное имя, почтовый адрес) Jira  
     * @return Отфильтрованный и синронизованный c Jira набор данных AD
     * @throws NamingException 
     */
    public static HashMap<Integer,SearchResult> adFilterJira(LdapUtils ldapUtils, List<SearchResult> adResult, ConcurrentMap<String,JiraUserFields> jiraUsersResult) throws NamingException {
    	Map<String,SearchResult> finRes = new HashMap<>();
    	Integer i = -1;
    	SearchResult obj;
    	for (String res : jiraUsersResult.keySet()) {   		
    		obj = null;
			obj = ldapUtils.findAtrVal(adResult, JCCM.AD_FIELD_SYNCHRO_AD, jiraUsersResult.get(res).getName());
			if (obj!=null && !finRes.containsValue(obj))
    		{
    			i++;
    			finRes.put(res, obj);
    		}
		}
    	Map<String, SearchResult> treeMap = new TreeMap<>(finRes);
    	HashMap<Integer,SearchResult> finRes2 = new HashMap<>();
    	i = -1;
    	for (String res : treeMap.keySet()) {
    		i++;
    		finRes2.put(i, treeMap.get(res));
		}
    	return finRes2;
    }	
    
    /**
     * Метод получения набора данных из AD путем фильтрации данных AD по списку пользователей Jira по полю (логина)
     * @param Список пользователей(логин, полное имя, почтовый адрес) Jira  
     * @return Отфильтрованный и синронизованный c Jira набор данных AD
     * @throws NamingException 
     */
    public static HashMap<Integer,SearchResult> sortJira(ConcurrentMap<String,JiraUserFields> jiraUsersResult) throws NamingException {
    	Map<String,SearchResult> treeMap = new TreeMap<>();
    	Integer i = -1;
    	SearchResult obj;
    	for (String res : jiraUsersResult.keySet()) {   		
    		obj = null;
			i++;
			BasicAttributes attrs = new BasicAttributes();
			attrs.put(JCCM.JIRA_USER_FIELD_EMAIL, jiraUsersResult.get(res).getEmailAddress());
			attrs.put(JCCM.JIRA_USER_FIELD_FULLNAME, jiraUsersResult.get(res).getDisplayName());
			attrs.put(JCCM.JIRA_USER_FIELD_LOGIN, jiraUsersResult.get(res).getName());

			obj = new SearchResult(res, (Object) res, attrs);
			treeMap.put(res, obj);    		    
		}
    	HashMap<Integer,SearchResult> finRes = new HashMap<>();
    	i = -1;
    	for (String res : treeMap.keySet()) {
    		i++;
    		finRes.put(i, treeMap.get(res));
		}
    	return finRes;
    }	    
  
    
    /**
     * Метод поиска пользователей в интегрированном списке пользователей Jira по полю ФИО
     * @param Интегрированный набор данных Jira
     * @param ФИО для поиска
     * @return Удовлетворяющий условиям поиска набор данных для заполнения результтрующей таблицы
     * @throws NamingException 
     */
    public static List<String[]> userSearch(HashMap<Integer,SearchResult> adFltUsers, String fio, String fld) throws NamingException, NullPointerException {
    	List<String[]> finRes = new ArrayList<String[]>();
    	String fldSearch = "";
    	if (fld==null || fld.equals("")) {
    		if (JCCM.LDAP_USE.equalsIgnoreCase("ДА")) {
        		fldSearch = JCCM.AD_FIELD_SURNAME.concat("+").concat(JCCM.AD_FIELD_NAME);    			
    		}
    		else {
        		fldSearch = JCCM.JIRA_USER_FIELD_FULLNAME;
    		}
    	}
    	else {
    		fldSearch = fld;
    	}
    	String searchStr = fio.toUpperCase();
    	int i = 0;
    	String value = "";
    	String value2 = "";
    	Attribute atr;
    	Attribute atr2;
    	for (Integer res : adFltUsers.keySet()) {
    		if (fldSearch.contains("+")) {
    			atr = adFltUsers.get(res).getAttributes().get(JCCM.AD_FIELD_SURNAME);
    			value = "";
    			if (atr != null) {
    				value = atr.get().toString().toUpperCase();
    			}
    			value2 = "";
    			atr2 = adFltUsers.get(res).getAttributes().get(JCCM.AD_FIELD_NAME);
    			if (atr2 != null) {
    				value2 = atr2.get().toString().toUpperCase();
    			}    			
        		value = value.concat(" ").concat(value2);
    		}
    		else {
    			atr = adFltUsers.get(res).getAttributes().get(fldSearch);
    			value = "";
    			if (atr != null && atr.get() != null) {
    				value = atr.get().toString().toUpperCase();
    			}    			
    		}

    		i++;
    		
    		
    		if ( (searchStr == null || searchStr.equals("")) || value.indexOf(searchStr, 0) > -1)
    		{
    			String fioVal = "";
    			
        		if (JCCM.LDAP_USE.equalsIgnoreCase("ДА")) {
        			try {    				
            			atr = adFltUsers.get(res).getAttributes().get(JCCM.AD_FIELD_SURNAME);
            			value = "";
            			if (atr != null) {
            				value = atr.get().toString();
            			}
            			value2 = "";
            			atr2 = adFltUsers.get(res).getAttributes().get(JCCM.AD_FIELD_NAME);
            			if (atr2 != null) {
            				value2 = atr2.get().toString();
            			}    			
            			fioVal = value.concat(" ").concat(value2);
    				} catch (Exception e) {
    					logger.error(e.getMessage());
    					logger.error(e.getStackTrace());
    					// TODO: handle exception
    				}
        			String mailVal = "";
        			try {
            			atr = adFltUsers.get(res).getAttributes().get(JCCM.AD_FIELD_EMAIL);
            			if (atr != null) {
            				mailVal = atr.get().toString();
            			}    			
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
        			
        			String loginVal = "";
        			try {
            			atr = adFltUsers.get(res).getAttributes().get(JCCM.AD_FIELD_LOGIN);
            			if (atr != null) {
            				loginVal = atr.get().toString();
            			}    			
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
        			
        			finRes.add(new String [] {fioVal, loginVal,
        					mailVal});
        		}    			        		
        		else {
           			try {    				
            			atr = adFltUsers.get(res).getAttributes().get(JCCM.JIRA_USER_FIELD_FULLNAME);
            			if (atr != null) {
            				value = atr.get().toString();
            			}
            			fioVal = value.concat(" ").concat(value2);
    				} catch (Exception e) {
    					logger.error(e.getMessage());
    					logger.error(e.getStackTrace());
    					// TODO: handle exception
    				}
        			String mailVal = "";
        			try {
            			atr = adFltUsers.get(res).getAttributes().get(JCCM.JIRA_USER_FIELD_EMAIL);
            			if (atr != null) {
            				mailVal = atr.get().toString();
            			}    			
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
        			
        			String loginVal = "";
        			try {
            			atr = adFltUsers.get(res).getAttributes().get(JCCM.JIRA_USER_FIELD_LOGIN);
            			if (atr != null) {
            				loginVal = atr.get().toString();
            			}    			
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
        			
        			finRes.add(new String [] {fioVal, loginVal,
        					mailVal});
        		}    			
    			
    		}
    	}
    	return finRes;    	
    }
    
	 /**
	  * Поиск объекта по определенному атрибуту 
	  * @param  Набор входных данных
	  * @param  Имя атрибута 
	  * @param  Значение атрибута 
	  * @return Найденный объект
	 * @throws NamingException 
	  */		
	public static SearchResult findAtrVal(HashMap<Integer,SearchResult> results, String atrName, String atrValue) throws NamingException {
		SearchResult searchResult = null;
		for (Integer key : results.keySet()) {
			searchResult = results.get(key);
			Attribute atr = searchResult.getAttributes().get(atrName);
			if (atr != null && atr.get().toString().equalsIgnoreCase(atrValue)) {				
				return searchResult;
			}
		}
		return null;
	}		
    

}
