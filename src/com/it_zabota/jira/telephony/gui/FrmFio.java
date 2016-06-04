package com.it_zabota.jira.telephony.gui;

import java.awt.SystemColor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

public class FrmFio extends JFrame {

	private JPanel contentPane;
	private JTextField txtFio;
	private JTable tblFio;
	private JCCM jccm;
	private JScrollPane scrolltblFio;
	protected String fio;
	private TableModel tblFioModel;
	private JTextField txtLogin;

	/**
	 * Create the frame.
	 */
	public FrmFio(JCCM jccm) {
		setResizable(false);
		this.jccm = jccm;
		setTitle("Поиск абонента");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 650, 510);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel pnlFio = new JPanel();
		pnlFio.setLayout(null);
		pnlFio.setBounds(20, 22, 614, 102);
		contentPane.add(pnlFio);
		
		JLabel lblFio = new JLabel("ФИО:");
		lblFio.setBounds(10, 14, 93, 14);
		pnlFio.add(lblFio);
		
		txtFio = new JTextField("");
//		txtFio.setColumns(10);
		txtFio.setBounds(136, 11, 468, 20);
		pnlFio.add(txtFio);
		
		JLabel lblLogin = new JLabel("Логин:");
		lblLogin.setBounds(10, 45, 124, 14);
		pnlFio.add(lblLogin);
		
		txtLogin = new JTextField("");
		txtLogin.setBounds(136, 42, 468, 20);
		pnlFio.add(txtLogin);
		
		JLabel lblEmail = new JLabel("Электронный адрес:");
		lblEmail.setBounds(10, 76, 124, 14);
		pnlFio.add(lblEmail);
		
		txtEmail = new JTextField("");
		txtEmail.setBounds(136, 73, 468, 20);
		pnlFio.add(txtEmail);
		
		JLabel lblRezSearch= new JLabel("Результаты поиска");
		lblRezSearch.setForeground(SystemColor.textHighlight);
		lblRezSearch.setBounds(30, 138, 204, 14);
		contentPane.add(lblRezSearch);
		
		JPanel pnlRezSearch = new JPanel();
		pnlRezSearch.setLayout(null);
		pnlRezSearch.setBounds(20, 145, 614, 326);
		contentPane.add(pnlRezSearch);
		
		tblFio = new JTable();		
		scrolltblFio = new JScrollPane(tblFio);
		scrolltblFio.setBounds(10, 23, 594, 292);
		tblFio.setFillsViewportHeight(true);		
		
		pnlRezSearch.add(scrolltblFio);
		
		/*
		 * Установка модели таблицы ФИО, ширин колонок
		 * */
		tblFioModel = tblSetModel(tblFio);
		tblSetColumnWidth(tblFio);		
		
		this.setIconImage( Launcher.image);
		this.setVisible(false);		
	}
	
	public JCCM getJccm() {
		return jccm;
	}

	/**
	 * Установка ширин колонок модели таблицы ФИО
	 */		
	public static void tblSetColumnWidth(JTable tblFio) {
		TableColumn column = null;
		for (int i = 0; i < tblFio.getColumnCount(); i++) {
		    column = tblFio.getColumnModel().getColumn(i);
	        column.setPreferredWidth(JCCM.FORM_TABLE_FIO_COL_WIDTH[i]);
		}		
	}
	
	public boolean choice = false;
	private JTextField txtEmail;
	
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
    	    	
    	
    	tblFio.addMouseListener(new java.awt.event.MouseAdapter() {


			@Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && !evt.isConsumed()) {
                    evt.consume();
                    int row = tblFio.getSelectedRow();
                    if (row >= 0) {
                    	choice  = true;
                        fio = (String) tblFioModel.getValueAt(row, 0);
                        jccm.setFioAbonentCurAd(fio);
                        jccm.setEmailCur((String) tblFioModel.getValueAt(row, 2));
                        jccm.setReporterCurJira((String) tblFioModel.getValueAt(row, 1));
                        txtFio.setText("");
                        txtLogin.setText("");
                        txtEmail.setText("");
                        try {
							jccm.filterIssue((String)jccm.getFrmTel().getCmboxFormIssue().getSelectedItem());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        closeFrame();
                    }
                }
            }
        }); 
    	      
           	
		txtFio.addKeyListener(new KeyAdapter() {
          public void keyReleased(KeyEvent keyEvent) {
              try {
				processTxtFioKeyEvent(keyEvent);
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          }		
		});
		
		txtFio.addFocusListener(new FocusListener() {
			 public void focusLost(FocusEvent e) {
				
	          }

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				 txtLogin.setText("");
				 txtEmail.setText("");
	             try {
					searchEmail();
				} catch (NamingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		txtEmail.addFocusListener(new FocusListener() {
			 public void focusLost(FocusEvent e) {

	          }

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtFio.setText("");
				txtLogin.setText("");
	             try {
	            	 searchFio();
				} catch (NamingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
			}
		});		

		txtEmail.addKeyListener(new KeyAdapter() {
	          public void keyReleased(KeyEvent keyEvent) {
	              try {
					processTxtEmailKeyEvent(keyEvent);
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	          }		
			});		
		
		txtLogin.addFocusListener(new FocusListener() {
			 public void focusLost(FocusEvent e) {

	          }

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtFio.setText("");
				txtEmail.setText("");
	             try {
	            	 searchLogin();
				} catch (NamingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
			}
		});		

		txtLogin.addKeyListener(new KeyAdapter() {
	          public void keyReleased(KeyEvent keyEvent) {
	              try {
					processTxtLoginKeyEvent(keyEvent);
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	          }		
			});			
		
    }
		
	    /**
	     * Обрабаывает событие для текстбокса поиска ФИО
	     *
	     * @param keyEvent событие
	     * @throws NamingException 
	     */
		private void processTxtFioKeyEvent(KeyEvent keyEvent) throws NamingException {
	    	if (keyEvent.getKeyCode() != KeyEvent.VK_UP && keyEvent.getKeyCode() != KeyEvent.VK_DOWN) {
	    		searchFio();
	        }//Чтобы можно было клавишами Вверх и Вниз ходить по списку без перестроения его самого - игнорируем эти клавиши
	    }		
  
	    /**
	     * Обрабаывает событие для текстбокса поиска почты
	     *
	     * @param keyEvent событие
	     * @throws NamingException 
	     */
		private void processTxtEmailKeyEvent(KeyEvent keyEvent) throws NamingException {
	    	if (keyEvent.getKeyCode() != KeyEvent.VK_UP && keyEvent.getKeyCode() != KeyEvent.VK_DOWN) {
	    		searchEmail();
	        }//Чтобы можно было клавишами Вверх и Вниз ходить по списку без перестроения его самого - игнорируем эти клавиши
	    }			
		
	    /**
	     * Обрабаывает событие для текстбокса поиска логина
	     *
	     * @param keyEvent событие
	     * @throws NamingException 
	     */
		private void processTxtLoginKeyEvent(KeyEvent keyEvent) throws NamingException {
	    	if (keyEvent.getKeyCode() != KeyEvent.VK_UP && keyEvent.getKeyCode() != KeyEvent.VK_DOWN) {
	    		searchLogin();
	        }//Чтобы можно было клавишами Вверх и Вниз ходить по списку без перестроения его самого - игнорируем эти клавиши
	    }		

    
	/**
	 * Закрытие формы
	 */    
    private void closeFrame() {
        resetData();
	  	setVisible(false);
	  	if (!choice)
	  	{
	  		jccm.setFioAbonentCurAd("");
            jccm.setEmailCur("");
            jccm.setReporterCurJira("");
            fio = "";
	  	}
	  	try {
			jccm.filterIssue((String)jccm.getFrmTel().getCmboxFormIssue().getSelectedItem());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  	choice = false;
	  	FrmTel frmTel = jccm.getFrmTel();
	  	frmTel.getTxtFio().setText(fio);
	  	frmTel.getTxtFio().repaint();
	  	frmTel.setVisible(true);	
	  	
    }
    
	/**
	 * Поиск абонентов по ФИО
	 * @throws NamingException 
	 */		
    public void searchFio() throws NamingException {
		// TODO Auto-generated method stub
		String fio = txtFio.getText();
		List<String[]> users;
		if (JCCM.LDAP_USE.equalsIgnoreCase("ДА")) {
			users = SysUtils.userSearch(jccm.getFltUsers(), fio, JCCM.AD_FIELD_SURNAME.concat("+").concat(JCCM.AD_FIELD_NAME));    			
		}
		else {
			users = SysUtils.userSearch(jccm.getFltUsers(), fio, JCCM.JIRA_USER_FIELD_FULLNAME);			
		}
		tblFioFillData(users.toArray(new String[0][0]));		
	}
    
	/**
	 * Поиск абонентов по почте
	 * @throws NamingException 
	 */		
    public void searchEmail() throws NamingException {
		// TODO Auto-generated method stub
		String email = txtEmail.getText();
		List<String[]> users;
		if (JCCM.LDAP_USE.equalsIgnoreCase("ДА")) {
			users = SysUtils.userSearch(jccm.getFltUsers(), email, JCCM.AD_FIELD_EMAIL);    			
		}
		else {
			users = SysUtils.userSearch(jccm.getFltUsers(), email, JCCM.JIRA_USER_FIELD_EMAIL);			
		}	
		tblFioFillData(users.toArray(new String[0][0]));		
	} 
    
	/**
	 * Поиск абонентов по логину
	 * @throws NamingException 
	 */		
    public void searchLogin() throws NamingException {
		// TODO Auto-generated method stub
		String login = txtLogin.getText();
		List<String[]> users;
		if (JCCM.LDAP_USE.equalsIgnoreCase("ДА")) {
			users = SysUtils.userSearch(jccm.getFltUsers(), login, JCCM.AD_FIELD_LOGIN);    			
		}
		else {
			users = SysUtils.userSearch(jccm.getFltUsers(), login, JCCM.JIRA_USER_FIELD_LOGIN);			
		}			    
		tblFioFillData(users.toArray(new String[0][0]));		
	} 
    
	/**
	 * Заполнение таблицы запросов
	 */			
	   public void tblFioFillData(Object[] isList) {
	        DefaultTableModel dtm = (DefaultTableModel) tblFio.getModel();
	        dtm.getDataVector().clear();
	        if (isList != null) {
	            for (Object anIsList : isList) {
	                Object[] rowData = (Object[]) anIsList;
	                dtm.addRow(rowData);
	            }
	        }
	        scrolltblFio.repaint();
	    }

	/**
	 * Очистка таблицы формы и поля ввода ФИО.
	 */		
    public void resetData() {
        DefaultTableModel dtm = (DefaultTableModel) tblFio.getModel();
        dtm.getDataVector().clear();  
        txtFio.setText("");
        txtEmail.setText("");
        txtLogin.setText("");
    }	   
	   
	/**
	 * Установка модели таблицы ФИО
	 */	
	public static TableModel tblSetModel(final JTable tblFio) {
		tblFio.setModel(new DefaultTableModel(
                JCCM.FORM_TABLE_FIO_COL_VAL,
                JCCM.FORM_TABLE_FIO_COL_LBL) {
			private static final long serialVersionUID = 1L;
			Class<?>[] types = JCCM.FORM_TABLE_FIO_COL_TYP;
            boolean[] canEdit = JCCM.FORM_TABLE_FIO_COL_CAN_EDIT;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return types[columnIndex];
                
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
          
        });
		return tblFio.getModel();
        		
	}    
    	
}
