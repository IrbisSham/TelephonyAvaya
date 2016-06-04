package com.it_zabota.jira.telephony.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.it_zabota.jira.telephony.encryptng.SecurityFile;
import com.it_zabota.jira.telephony.jira.JiraUserFields;
import com.it_zabota.jira.telephony.jira.JiraUtils;
import com.it_zabota.jira.telephony.main.JCCM;

/**
 * Работает с Активной директорией пользователей с соедининем LDAP
 * 
 * @author Vitaly Ermilov <vitaliyerm@gmail.com>
 * @version 1.0
 * @since 03.10.2014
 */
public class LdapUtils {
	
	public static Logger getLogger() {
		return logger;
	}
	
	private static Logger logger = Logger.getLogger(JiraUtils.class);
	
	public DirContext getContext() {
		return context;
	}

	public List<SearchResult> getAdUsers() {
		return adUsers;
	}

	private JCCM jccm;
	private DirContext context;
	
	
	public void setContext(DirContext context) {
		this.context = context;
	}

	private List<SearchResult> adUsers;
	
	
    public void setAdUsers(List<SearchResult> adUsers) {
		this.adUsers = adUsers;
	}

	/**
     * Инициализация класса работы с LDAP
	 * @throws Throwable 
     */    
    public void init(JCCM jccm) throws Throwable {
    	this.jccm = jccm;
    	if (JCCM.LDAP_USE != null && JCCM.LDAP_USE.equalsIgnoreCase("ДА")) {    		
    				
    		logger.debug(JCCM.LDAP_USERNAME);
  
//    		GssLogging.main(null, this);
    		
    		context = connect(JCCM.LDAP_HOST, JCCM.LDAP_PORT, JCCM.LDAP_USERNAME, JCCM.LDAP_PASSWORD);
//    		context = connect(null);
    	}   	
    	if (context==null) {
    		JCCM.LDAP_USE = "НЕТ";
    		logger.info("Cервис LDAP отключен");
    		adUsers = searchJira(jccm.getJiraUtils().getJiraUsers());
    	}
    	else {
    	       String path = System.getProperty("user.dir");
    	       String pathFile = new String(path.concat("//tmp//tmp0002.ser"));    	       
    	       adUsers = setAdUsers(pathFile);
//    	       context.close();
    	}    	 	                
    }
	
    /**
     * Установка списка резалтсетов LDAP из файла или через подключение к LDAP
     * @return 
     * @throws Throwable 
     */        
    public  List<SearchResult> setAdUsers(String path) throws Throwable {
        File fileIn = new File(path);    
        File fileOut = new File(path.concat("q"));	    	
    	List<SearchResult> res = new ArrayList<>();
    	String separator = JCCM.SPLITTER;    	
    	if (fileIn.exists()) {
			FileInputStream fis = new FileInputStream(path);
			FileOutputStream fos = new FileOutputStream(path.concat("q"));    		
 		    SecurityFile.decrypt(JCCM.FILE_ENCRIPT_KEY, fis, fos, JCCM.WORK_FILES_ENCRYPTING);    		
    		Scanner scanner = null;
    			try {
    				scanner = new Scanner(fileOut);
    		        scanner.useDelimiter(separator);
    		        String nextExp;
    		        while (scanner.hasNext())
    		        {	      	
    	        		nextExp = scanner.nextLine();
    	        		String[] nextExpAr = nextExp.split("\\".concat(separator));
    	        		
    	        		BasicAttributes attrs = new BasicAttributes();
    	        		
    	        		if (nextExpAr.length > 0) {
    	        			attrs.put(JCCM.AD_FIELD_EMAIL, nextExpAr[0]);	        			
    	        		}
    	        		else {
    	        			attrs.put(JCCM.AD_FIELD_EMAIL, "");
    	        		}
    	        		if (nextExpAr.length > 1) {
    	        			attrs.put(JCCM.AD_FIELD_SURNAME, nextExpAr[1]);     			
    	        		}
    	        		else {
    	        			attrs.put(JCCM.AD_FIELD_SURNAME, "");
    	        		}
    	        		
    	        		if (nextExpAr.length > 2) {
    	        			attrs.put(JCCM.AD_FIELD_NAME, nextExpAr[2]);     			
    	        		}
    	        		else {
    	        			attrs.put(JCCM.AD_FIELD_NAME, "");
    	        		}
    	        		
    	        		if (nextExpAr.length > 3) {
    	        			attrs.put(JCCM.AD_FIELD_LOGIN, nextExpAr[3]);     			
    	        		}
    	        		else {
    	        			attrs.put(JCCM.AD_FIELD_LOGIN, "");
    	        		}
    	        		
    	        		
//    	        		if (nextExpAr.length > 2) {
//    	        			attrs.put(JCCM.AD_FIELD_ORG, nextExpAr[2]);   			
//    	        		}
//    	        		if (nextExpAr.length > 3) {
//    	        			attrs.put(JCCM.AD_FIELD_DPT, nextExpAr[3]);		
//    	        		}
    	        		if (nextExpAr.length > 4) {
    	        			attrs.put(JCCM.AD_FIELD_TELEPHONE, nextExpAr[4]);		
    	        		}
    	        		else {
    	        			attrs.put(JCCM.AD_FIELD_TELEPHONE, "");
    	        		}    	        		

    	    	       	SearchResult searchResult = new SearchResult ("adUsers", null, attrs);
    	    	       	res.add(searchResult);
    	    	       	
    		        }    		        
    			} catch (FileNotFoundException i) {
    				logger.error("Не удалось прочитать файл с пользователями Ldap ".concat(fileOut.getCanonicalPath()));
	    	         i.printStackTrace();
	    	         System.exit(-1);
    			} 
    			catch (UnsupportedOperationException e) {
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
    	}
    	else {
    		logger.info("Загрузка данных пользователей AD"); 
    		res = search(context); 	     	  
    	}   
    	return res;
	}
    

	
	 /**
	  * Поиск в активной директории 
	  * @param  Контекст соединения
	  * @param  База поиска 
	  * @param  Фильтр поиска
	  * @return Список возвращаемых данных
	  */	
	public static List<SearchResult> search(DirContext ctx) throws NamingException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        NamingEnumeration<SearchResult> results1 = ctx.search(JCCM.LDAP_SEARCHBASE, JCCM.LDAP_SEARCHFILTER, searchControls);
        while(results1.hasMoreElements()) {
        	SearchResult searchResult = (SearchResult) results1.nextElement();
            results.add(searchResult); 
       }	
		return results;
	}
	
	 /**
	  * Заполнение списка резалтсетов значениями пользователей Jira при отсутствии подключения к LDAP
	  * @param  Список пользователей Jira
	  * @return Список возвращаемых данных
	  */	
	public static List<SearchResult> searchJira(ConcurrentMap<String,JiraUserFields> jiraUsers) throws NamingException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		for (String res : jiraUsers.keySet()) {
			BasicAttributes attrs = new BasicAttributes();
			attrs.put(JCCM.AD_FIELD_EMAIL, jiraUsers.get(res).getEmailAddress());
			attrs.put(JCCM.AD_FIELD_SURNAME, jiraUsers.get(res).getDisplayName());
			attrs.put(JCCM.AD_FIELD_NAME, "");
			attrs.put(JCCM.AD_FIELD_LOGIN, jiraUsers.get(res).getName());
//			attrs.put(JCCM.AD_FIELD_FIO, jiraUsers.get(res).getDisplayName());
//			attrs.put(JCCM.AD_FIELD_ORG, "");
//			attrs.put(JCCM.AD_FIELD_DPT, "");
			attrs.put(JCCM.AD_FIELD_TELEPHONE, "");
	       	SearchResult searchResult = new SearchResult ("jiraUsers", null, attrs);
	        results.add(searchResult); 			
		}
		return results;
	}	
	
	 /**
	  * Получение списка значений определенного атрибута 
	  * @param  Список входных данных
	  * @param  Имя атрибута 
	  * @return Список значений определенного атрибута
	 * @throws NamingException 
	  */		
	public static List<Object> getAtrVal(List<SearchResult> results, String atrName) throws NamingException {
		List<Object> listAtrVal = new ArrayList<Object>();
		for (SearchResult sr : results) {
            listAtrVal.add(sr.getAttributes().get(atrName).get());			
		}
		return listAtrVal;
	}
	
	 /**
	  * Поиск объекта по определенному атрибуту в AD 
	  * @param  Набор входных данных AD
	  * @param  Имя атрибута AD (телефон)
	  * @param  Значение атрибута AD (номер телефона)
	  * @return Найденный объект AD
	 * @throws NamingException 
	  */		
	public SearchResult findAtrVal(List<SearchResult> results, String atrName, String atrValue) throws NamingException {
		for (SearchResult sr : results) {
            if (sr.getAttributes().get(atrName).get().toString().equalsIgnoreCase(atrValue)) {
            	return sr;
            }			
		} 
		return null;
	}	
	
	
	 /**
	  * Соединение с LDAP 
	  * @param  Хост соединения
	  * @param  Порт соединения
	  * @param  Имя пользователя соединения
	  * @param  Пароль соединения
	  * @return Контекст соединения
	  */
   
	public static LdapContext connect(String ldapHost, String ldapPort, String ldapUsername, String ldapPassword) throws NamingException {
		
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		String url = "";
		LdapContext ctx = null;
		try {
									
//				@Simple-authentification			
		       env.put(Context.SECURITY_AUTHENTICATION, "simple");
		       
		   	// Request the use of the "GSSAPI" SASL mechanism
		   	// Authenticate by using already established Kerberos credentials
//		       env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");		       
		       
		        if(ldapUsername != null) {
		            env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
		        }
		        if(ldapPassword != null) {
		            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
		        }
		        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		        if (ldapPort.equals("") || ldapPort == null) {
		        	url = "ldap://" + ldapHost;
		        }
		        else {
		        	url = "ldap://" + ldapHost + ":" + ldapPort+"/";
		        }
		        env.put(Context.PROVIDER_URL, url);

		        //ensures that objectSID attribute values
		        //will be returned as a byte[] instead of a String
		        env.put("java.naming.ldap.attributes.binary", "objectSID");
		        
		        // the following is helpful in debugging errors
		        //env.put("com.sun.jndi.ldap.trace.ber", System.err);
		        
		        ctx = new InitialLdapContext(env, null);			
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Не удалось подключиться к сервису LDAP");
			logger.error(e);
			JOptionPane.showMessageDialog(null, "Не удалось подключиться к сервису LDAP. Продолжение работы с базой данной пользователей Jira.");
		}

		return ctx;
	}
	
}
