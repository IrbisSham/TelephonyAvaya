package com.it_zabota.jira.telephony.gui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.SystemColor;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.it_zabota.jira.telephony.main.JCCM;
import com.it_zabota.jira.telephony.main.Launcher;
import com.it_zabota.jira.telephony.utils.SysUtils;

import java.awt.Window.Type;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;


public class FrmTel extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JCCM jccm;
	private JPanel contentPane;
	private JPanel pnlAbonent;
	private JPanel pnlIssues; 
	private JTextField txtTel;
	private JTextField txtFio;
	public JTextField getTxtFio() {
		return txtFio;
	}

//	private JTextField txtOrg;
//	private JTextField txtOtd;
	private JTable tblIssues;
	private JComboBox cmboxFormIssue;
	public JComboBox getCmboxFormIssue() {
		return cmboxFormIssue;
	}

	private JButton btnListFio;
	private JButton btnNewIssue;
	private JScrollPane scrolltblIssues;
	
	
    private boolean answered = false;
	private FrmTel frmTel;
    
    public FrmTel getFrmTel() {
		return frmTel;
	}

	public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean b) {
        answered = b;
    }
    
    public void initProperties() {
        //Проставляем проперти формы
    }	
	
	/**
	 * Создание базового фрейма
	 */
	public FrmTel(JCCM jccm) {
		setResizable(false);
		setMinimumSize(new Dimension(500, 300));
		this.jccm = jccm;
		frmTel = this;
		setTitle("Модуль интеграции Avaya и Atlassian Jira");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 833, 508);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblAbonent = new JLabel(JCCM.FORM_LBL_ABONENT);
		lblAbonent.setForeground(SystemColor.textHighlight);
		lblAbonent.setBounds(20, 11, 66, 14);
		contentPane.add(lblAbonent);
		
		JLabel lblIssues = new JLabel(JCCM.FORM_LBL_ISSUES);
		lblIssues.setForeground(SystemColor.textHighlight);
		lblIssues.setBounds(20, 103, 83, 14);
		contentPane.add(lblIssues);
		
		pnlAbonent = new JPanel();
		pnlAbonent.setBounds(10, 22, 587, 70);
		contentPane.add(pnlAbonent);
		pnlAbonent.setLayout(null);
		
		JLabel lblTel = new JLabel(JCCM.FORM_LBL_TEL);
		lblTel.setBounds(10, 14, 104, 14);
		
		JLabel lblFio = new JLabel(JCCM.FORM_LBL_FIO);
		lblFio.setBounds(10, 39, 104, 14);
		pnlAbonent.add(lblFio);
				
		txtTel = new JTextField("");
		txtTel.setEditable(false);
		txtTel.setBounds(113, 11, 452, 20);
		
		pnlAbonent.add(txtTel);
		pnlAbonent.add(lblTel);			

	
		txtFio = new JTextField("");
		txtFio.setEditable(false);
		txtFio.setBounds(113, 36, 430, 20);
		pnlAbonent.add(txtFio);
		
		
		
		btnListFio = new JButton("...");
		btnListFio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnListFio.setBounds(542, 35, 23, 23);
		pnlAbonent.add(btnListFio);
		
		tblIssues = new JTable();		
		tblIssues.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrolltblIssues = new JScrollPane(tblIssues);
		scrolltblIssues.setBounds(10, 40, 783, 294);
		tblIssues.setFillsViewportHeight(true);		
		
		pnlIssues = new JPanel();
		pnlIssues.setBounds(10, 114, 803, 345);
		contentPane.add(pnlIssues);
		pnlIssues.setLayout(null);
		pnlIssues.add(scrolltblIssues);
				
		
//		panel.setLayout(new BorderLayout());
//		panel.add(tblIssues.getTableHeader(), BorderLayout.PAGE_START);
//		panel.add(tblIssues, BorderLayout.CENTER);		
		
		
		cmboxFormIssue = new JComboBox(JCCM.FORM_ISSUE);
		cmboxFormIssue.setEditable(true);
		cmboxFormIssue.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		cmboxFormIssue.setBounds(10, 11, 207, 20);	
		pnlIssues.add(cmboxFormIssue);
		
		btnNewIssue = new JButton(JCCM.FORM_BTN_NEWISSUE);
		btnNewIssue.setName("");
		btnNewIssue.setBounds(476, 10, 90, 23);
		pnlIssues.add(btnNewIssue);
		/*
		 * Установка модели таблицы обращений, ширин колонок
		 * */
		tblSetModel(tblIssues);
		tblSetColumnWidth(tblIssues);
		this.setIconImage( Launcher.image);
		this.setVisible(false);
	}
	
	public JCCM getJccm() {
		return jccm;
	}

	/**
	 * Установка ширин колонок модели таблицы обращений
	 */		
	public static void tblSetColumnWidth(JTable tblIssues) {
		TableColumn column = null;
		for (int i = 0; i < tblIssues.getColumnCount(); i++) {
		    column = tblIssues.getColumnModel().getColumn(i);
	        column.setPreferredWidth(JCCM.FORM_TABLE_COL_WIDTH[i]);
		}		
	}
	
	/**
	 * Инициализация слушателей событий элементов формы
	 */		
    public void initActions() {
   	
        	addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent winEvt) {
                    closeFrame();
                }
            });
    	
    	cmboxFormIssue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            			actionCmbox((JComboBox)evt.getSource());
            }
        });  
    	
        tblIssues.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && !evt.isConsumed()) {
                    evt.consume();
                    int row = tblIssues.getSelectedRow();
                    if (row >= 0) {
                        DefaultTableModel dtm = (DefaultTableModel) tblIssues.getModel();
                        String issueKey = (String) dtm.getValueAt(row, 0);
                        try {
							SysUtils.openWebpage(new URL(SysUtils.getUrl("http://", JCCM.JIRA_HOST, JCCM.JIRA_PORT) + "browse/" + issueKey));
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        setState(Frame.ICONIFIED);
                    }
                }
            }
        }); 
        
        btnNewIssue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	createIssueGUI();
            }
        });
        
        btnListFio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					searchFio();
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });         
        
    }
    
    
    private void closeFrame() {
		// TODO Auto-generated method stub
	     JOptionPane.showMessageDialog(rootPane, "Вы не можете закрыть окно без сохранения заявки!", "Уведомление", JOptionPane.INFORMATION_MESSAGE);
	}

	private void searchFio() throws NamingException {
		// TODO Auto-generated method stub
	  	  setVisible(false);
	  	  FrmFio frmFio = jccm.getFrmFio();
	  	  frmFio.searchFio();
	  	  frmFio.setVisible(true);		
	}

	/**
	 * Создание запроса без линковки
	 */      
    private void createIssueGUI() {
  	  setVisible(false);
  	  FrmIssue frmIssue = createIssue();
  	  frmIssue.setVisible(true);
    }    
    
    public FrmIssue createIssue() {
    	  FrmIssue frmIssue = jccm.getFrmIssue();
    	  if (getJccm().getTypIssueCurJira() == null || (getJccm().getTypIssueCurJira().equals(""))) {
    		  getJccm().setTypIssueCurJira(JCCM.JIRA_ISSUE_TYPE_DEFAULT);  		  
    	  }
    	  if (getJccm().getPriorityCurJira() == null || (getJccm().getPriorityCurJira().equals(""))) { 
    		  getJccm().setPriorityCurJira(JCCM.JIRA_PRIORITY_DEFAULT);  		  
    	  }
    	  frmIssue.frmIssueFillData((Integer)null);
    	  return frmIssue;
      } 
    
	/**
	 * Создание запроса с линковкой
	 * @param объект для поиска - id обращения
	 */          
    public void linkIssue(int idIssue) {
	  setVisible(false);
	  FrmIssue frmIssue = jccm.getFrmIssue();
	  TableModel model = tblIssues.getModel();
	  jccm.setLinkIssueCurJira(model.getValueAt(idIssue, 0).toString());
  	  if (getJccm().getTypIssueCurJira() == null || (getJccm().getTypIssueCurJira().equals(""))) {
  		  getJccm().setTypIssueCurJira(JCCM.JIRA_ISSUE_TYPE_DEFAULT);  		  
  	  }	  
//	  getJccm().setTypIssueCurJira(model.getValueAt(Integer.valueOf(idIssue), 1).toString());
	  
  	  if (getJccm().getPriorityCurJira() == null || (getJccm().getPriorityCurJira().equals(""))) { 
  		  getJccm().setPriorityCurJira(JCCM.JIRA_PRIORITY_DEFAULT);  		  
  	  }

	  
	  frmIssue.frmIssueFillData(idIssue);
	  frmIssue.setVisible(true);
}       
  
	
	/**
	 * Установка модели таблицы обращений
	 */	
	public void tblSetModel(final JTable tblIssues) {
		tblIssues.setModel(new DefaultTableModel(
                JCCM.FORM_TABLE_COL_VAL,
                JCCM.FORM_TABLE_COL_LBL) {
			private static final long serialVersionUID = 1L;
			Class<?>[] types = JCCM.FORM_TABLE_COL_TYP;
            boolean[] canEdit = JCCM.FORM_TABLE_COL_CAN_EDIT;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return types[columnIndex];
                
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
          
        });
        
		tblIssues.getColumn(JCCM.FORM_TABLE_COL_LBL_LINK).setCellRenderer(new LinkBtnRenderer());
		tblIssues.getColumn(JCCM.FORM_TABLE_COL_LBL_LINK).setCellEditor(
		     new LinkBtnEditor(new JCheckBox(), getFrmTel()));
		
	}
	
	/**
	 * Событие комбобокса выбора типа фильтрации
	 */		
	public void actionCmbox(JComboBox cb) {
        String fltType = (String)cb.getSelectedItem();
        try {
			jccm.filterIssue(fltType);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	/**
	 * Заполнение таблицы запросов
	 */			
	   public void tblIssuesFillData(Object[] isList) {
	        DefaultTableModel dtm = (DefaultTableModel) tblIssues.getModel();
	        dtm.getDataVector().clear();
	        if (isList != null) {
	            for (Object anIsList : isList) {
	                Object[] rowData = (Object[]) anIsList;
	                dtm.addRow(rowData);
	            }
	        }
	        scrolltblIssues.repaint();
	        tblIssues.invalidate();
	    }
	   
		public JTable getTblIssues() {
		return tblIssues;
	}

		/**
		 * Заполнение полей панели абонентов
		 */			   
	   public void pnlAbonentFillData(String other) {		   
		   txtTel.setText(other);
		   txtFio.setText(jccm.getFioAbonentCurAd());
		   txtTel.repaint();
		   txtFio.repaint();     
	    }	   
	
	/**
	 * Очистка полей и таблиц формы. Состояние отсутствия ответа.
	 */		
    public void resetData() {
        DefaultTableModel dtm = (DefaultTableModel) tblIssues.getModel();
        dtm.getDataVector().clear();        
        for (Component comp : pnlAbonent.getComponents()) {
			if (comp instanceof JTextField) {
				((JTextField) comp).setText(null);
			}
		}
        answered = false;
    }	
}
