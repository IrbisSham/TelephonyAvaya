package com.it_zabota.jira.telephony.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;

class LinkBtnEditor extends DefaultCellEditor {
	protected JButton button;
	
	private String label = "";
	
	private boolean isPushed;

	private FrmTel frmtel;
	
	public LinkBtnEditor(JCheckBox checkBox, FrmTel frmtel) {	 
	 super(checkBox);
	 this.frmtel = frmtel;
	 button = new JButton();
	 button.setOpaque(true);
	 button.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent e) {
	     fireEditingStopped();
	   }
	 });
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
	   boolean isSelected, int row, int column) {
	 if (isSelected) {
	   button.setForeground(table.getSelectionForeground());
	   button.setBackground(table.getSelectionBackground());
	 } else {
	   button.setForeground(table.getForeground());
	   button.setBackground(table.getBackground());
	 }
	 isPushed = true;
	 if (frmtel.getJccm().getReporterCurJira()!=null && !frmtel.getJccm().getReporterCurJira().equals("")) {
		 frmtel.linkIssue(row);
	 }
	 else {
		 JOptionPane.showMessageDialog(frmtel, "Вы не можете связыать заявки без выбора автора!", "Ошибка", JOptionPane.ERROR_MESSAGE);
	 }
	 return button;
	}
	
	public Object getCellEditorValue() {
	 if (isPushed) {
//		 JOptionPane.showMessageDialog(button, label + ": Ouch!");
	   // System.out.println(label + ": Ouch!");
	 }
	 isPushed = false;
	 return new String(label);
	}
	
	public boolean stopCellEditing() {
	 isPushed = false;
	 return super.stopCellEditing();
	}
	
	protected void fireEditingStopped() {
	 super.fireEditingStopped();
	}
}

