package com.it_zabota.jira.telephony.gui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.util.json.JSONException;
import com.it_zabota.jira.telephony.main.JCCM;
import com.it_zabota.jira.telephony.main.Launcher;
import com.it_zabota.jira.telephony.utils.SysUtils;
import com.sun.jersey.api.client.ClientHandlerException;

public class FrmIssue extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3061389239754830199L;
	private JPanel contentPane;
	private JTextField txtIssueLink;
	private JTextField txtIssueTheme;
	private JComboBox cmboxIssueType;
	private JComboBox cmboxPriority;
	private JCCM jccm;
	private String idLinkIssue;
	private JTextArea txtIssueDescription;
	private JButton btnSave;
		
	private static Logger logger = Logger.getLogger(FrmIssue.class);

	/**
	 * Create the frame.
	 */
	public FrmIssue(JCCM jccm) {
		setResizable(false);
		this.jccm = jccm;
		setTitle("Новое обращение");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 580, 268);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblIssueType = new JLabel("Тип обращения:");
		lblIssueType.setBounds(10, 14, 104, 14);
		contentPane.add(lblIssueType);
		
		txtIssueLink = new JTextField("");
		txtIssueLink.setEditable(false);
//		txtIssueLink.setColumns(10);
		txtIssueLink.setBounds(113, 36, 452, 20);
		contentPane.add(txtIssueLink);
		
		JLabel lblLink = new JLabel("Связано с:");
		lblLink.setBounds(10, 39, 104, 14);
		contentPane.add(lblLink);
		
		JLabel lblTheme = new JLabel("Тема:");
		lblTheme.setBounds(10, 64, 104, 14);
		contentPane.add(lblTheme);
		
		txtIssueTheme = new JTextField("");
//		txtIssueTheme.setColumns(10);
		txtIssueTheme.setBounds(113, 61, 452, 20);
		contentPane.add(txtIssueTheme);
				
		
		JLabel lblDescription = new JLabel("Описание:");
		lblDescription.setBounds(10, 121, 104, 14);
		contentPane.add(lblDescription);
		
		btnSave = new JButton("Сохранить");
		btnSave.setBounds(461, 205, 104, 23);
		contentPane.add(btnSave);
		
		cmboxIssueType = new JComboBox(JCCM.JIRA_ISSUE_TYPE_LIST);
		cmboxIssueType.setEditable(true);
		cmboxIssueType.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		cmboxIssueType.setBounds(113, 11, 452, 20);
		contentPane.add(cmboxIssueType);
		
		txtIssueDescription = new JTextArea("");		
		
		JScrollPane scrollIssueDescription = new JScrollPane(txtIssueDescription);
		scrollIssueDescription.setBounds(113, 120, 452, 74);
		contentPane.add(scrollIssueDescription);
		
		JLabel lblPriority = new JLabel("Приоритет:");
		lblPriority.setBounds(10, 92, 104, 14);
		contentPane.add(lblPriority);
		
		cmboxPriority = new JComboBox(jccm.getJiraUtils().getJiraPriorityFields().keySet().toArray());
		cmboxPriority.setEditable(true);
		cmboxPriority.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		cmboxPriority.setBounds(113, 92, 452, 20);
		contentPane.add(cmboxPriority);
		
		this.setIconImage( Launcher.image);
		this.setVisible(false);		
	}

	public JCCM getJccm() {
		return jccm;
	}
	
	/**
	 * Заполнение полей обращения
	 */			   
   public void frmIssueFillData(Integer idLinkIssue) {
	   this.idLinkIssue = jccm.getLinkIssueCurJira();
//	   txtIssueTheme.setText(jccm.getThemeIssueCurJira());
	   txtIssueLink.setText(jccm.getLinkIssueCurJira());
       cmboxIssueType.setSelectedItem(jccm.getTypIssueCurJira());
//	   txtIssueTheme.repaint();
	   txtIssueLink.repaint(); 
	   cmboxIssueType.repaint();
	   cmboxPriority.setSelectedItem(jccm.getPriorityCurJira());
	   cmboxPriority.repaint();
    }	
   
	/**
	 * Очистка полей и таблиц формы. Состояние отсутствия ответа.
	 */		
   public void resetData() {      
	   for (Component comp : contentPane.getComponents()) {
			if (comp instanceof JTextField) {
				((JTextField) comp).setText("");
			}
		}
	  txtIssueDescription.setText("");
	  jccm.setLinkIssueCurJira(null);	   	  
	  jccm.setDescriptionIssueCurJira("");
	  jccm.setThemeIssueCurJira("");
	  
	  txtIssueLink.repaint(); 	  
	  txtIssueTheme.repaint();
	  txtIssueDescription.repaint();
   }	   
   
	 public void createIssue() throws MalformedURLException, UnsupportedEncodingException,  RestClientException, UnsupportedEncodingException, InterruptedException, ExecutionException, AuthenticationException, ClientHandlerException, JSONException

 {
	 String url = null;
	 String msgError = "";
	 try {
		 jccm.setDescriptionIssueCurJira(txtIssueDescription.getText());
		 String theme;
		 if (!JCCM.APP_MODE.equalsIgnoreCase(JCCM.APP_MODE_CONSOLE)) {
			 jccm.setThemeIssueCurJira(txtIssueTheme.getText().trim());
		 }
		 if (jccm.getThemeIssueCurJira() == null || jccm.getThemeIssueCurJira().isEmpty()) {
			 msgError = msgError.concat(System.lineSeparator()).concat("Поле 'Тема' обязательно для заполнения!");
		 }
		 String typIssue = cmboxIssueType.getSelectedItem().toString().trim();
		 jccm.setTypIssueCurJira(typIssue);		 
		 if (typIssue == null || typIssue.equals("")) {
			 msgError = msgError.concat(System.lineSeparator()).concat("Поле 'Тип заявки' обязательно для заполнения!");
		 }
		 else {
			 jccm.setTypIssueIdCurJira(Integer.valueOf((String)jccm.getJiraUtils().getJiraIssueTypeFields().get(jccm.getTypIssueCurJira())));			 
		 }
		 String priority = cmboxPriority.getSelectedItem().toString().trim();
		 jccm.setPriorityCurJira(priority);		 
		 if (priority == null || priority.equals("")) {
			 msgError = msgError.concat(System.lineSeparator()).concat("Поле 'Приоритет' обязательно для заполнения!");
		 }
		 else {
			 jccm.setPriorityIdCurJira(Long.valueOf((String)jccm.getJiraUtils().getJiraPriorityFields().get(jccm.getPriorityCurJira())));			 
		 }
		 
		 if (!msgError.equals("")) {
		     JOptionPane.showMessageDialog(rootPane, msgError, "Ошибка", JOptionPane.ERROR_MESSAGE);
		     return;
		 }
         url = jccm.getJiraUtils().createIssue(idLinkIssue);
	 } catch (Throwable ex) {
		 if (!ex.getMessage().contains("204")) {
			 logger.error("Невозможно создать запрос!");
			 logger.error(ex);			 
		 }
	 }
	 if (url != null) { 
		logger.info("Запрос создан");
		String webLink = SysUtils.getUrl("http://", JCCM.JIRA_HOST, JCCM.JIRA_PORT) + url;
//        System.out.println(webLink);
		logger.debug("Ссылка в браузере: "+webLink);		
	 	SysUtils.openWebpage(new URL(webLink));
	 	logger.info("Завершение приложения...");
	 	System.exit(0);
	 } else {
	     JOptionPane.showMessageDialog(rootPane, "Запрос не создан!", "Ошибка", JOptionPane.ERROR_MESSAGE);
	 }
	}
	 
	/**
	 * Инициализация слушателей событий элементов формы
	 */		
    public void initActions() {
    	
    	addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                closeFrame();
            }
        });
    	
    	btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	try {
					try {
						createIssue();
					} catch (AuthenticationException | RestClientException
							| ClientHandlerException | InterruptedException
							| ExecutionException | JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });        
        
    } 
	 
	/**
	 * Закрытие формы
	 */    
    private void closeFrame() {
	  	setVisible(false);
	  	resetData();
	  	FrmTel frmTel = jccm.getFrmTel();
	  	frmTel.setVisible(true);		
    }
}
