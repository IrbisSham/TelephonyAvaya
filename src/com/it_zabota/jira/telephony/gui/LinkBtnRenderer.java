package com.it_zabota.jira.telephony.gui;

import java.awt.Component;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.it_zabota.jira.telephony.main.JCCM;
import com.it_zabota.jira.telephony.main.Launcher;

class LinkBtnRenderer extends JButton implements TableCellRenderer {

/**
	 * 
	 */
	private static final long serialVersionUID = -4310683245425932127L;

public LinkBtnRenderer() {
 setOpaque(true);
}

public Component getTableCellRendererComponent(JTable table, Object value,
   boolean isSelected, boolean hasFocus, int row, int column) {
   InputStream is = Launcher.class.getResourceAsStream("/resources/".concat(JCCM.FORM_BTN_LINK_ICON_PATH));
   Image img = null;
try {
	img = ImageIO.read(is);
    Image newimg = img.getScaledInstance(JCCM.FORM_BTN_LINK_ICON_DIM.width, JCCM.FORM_BTN_LINK_ICON_DIM.height,  java.awt.Image.SCALE_SMOOTH ) ;  
    Icon icon = new ImageIcon( newimg );	
    setIcon(icon);	
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}			

   this.setToolTipText("Привязка нового обращения к существующему");
// if (isSelected) {
//   setForeground(table.getSelectionForeground());
//   setBackground(table.getSelectionBackground());
// } else {
//   setForeground(table.getForeground());
//   setBackground(UIManager.getColor("Button.background"));
// }
 return this;
}
}
