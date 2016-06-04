/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.it_zabota.jira.telephony.main;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;


/**
 * Класс запуска приложения.
 * @author Vers_us
 */
public class Launcher {
    private static Logger logger = Logger.getLogger(Launcher.class);
    
    private static String APP_VERSION = "2.1.1.1";
    
    public static  String EXIT_MENU_ITEM_TEXT = "Выход";

    public static String TRAY_IMAGE_PAGE = "/logo.png";

    public static String TRAY_IMAGE_TIP = "IT-Забота";
	
    public static Image image;
    
    public static void main(final String[] args) throws Throwable {
    	
//        if (args.length < 1) {
//        	logger.error("Передайте параметр телефона на вход приложения!");
//        	System.exit(-1);
//        }     	
        if (args.length < 1) {
        	logger.error("Передайте телефон на вход приложения!");
        	System.exit(-1);
        }    	
        String firstArg = args[0];
        String secondArg = "";        
        
        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem(EXIT_MENU_ITEM_TEXT);

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        popup.add(exitItem);

        SystemTray systemTray = SystemTray.getSystemTray();
        
		try {
			InputStream is = Launcher.class.getResourceAsStream("/resources".concat(TRAY_IMAGE_PAGE));
			image = ImageIO.read(is);			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						        
        TrayIcon trayIcon = new TrayIcon(image, TRAY_IMAGE_TIP , popup);

        trayIcon.setImageAutoSize(true);

        systemTray.add(trayIcon);        
        if ("stop".equalsIgnoreCase(firstArg)) {
            Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 9999);
            PrintWriter pw = new PrintWriter(socket1.getOutputStream(), true);
            pw.write("stop\n");
            pw.close();
            socket1.close();
        } else if ("answer".equalsIgnoreCase(firstArg)) {
            if (args.length < 2) {
        	 	logger.info("Завершение приложения...");
                System.exit(-1);
            }
            String phone = args[1];
            Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 9999);
            PrintWriter pw = new PrintWriter(socket1.getOutputStream(), true);
            pw.write("answer:" + phone + "\n");
            pw.close();
            socket1.close();
        } else {
            try {
                ServerSocket socket = new ServerSocket(9999, 10, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
                if (firstArg.equalsIgnoreCase("stop")) {
                    socket.close();
            	 	logger.info("Завершение приложения...");
                    System.exit(0);
                }
                final JCCM jccm = new JCCM();
                jccm.init(firstArg, secondArg.trim());
                while (true) {
                    final Socket s = socket.accept();
                    Thread t = new Thread(new Runnable() {

                        public void run() {
                            try {
                                InputStream is = s.getInputStream();
                                Scanner in = new Scanner(is);
                                boolean done = false;
                                while (!done && in.hasNextLine()) {
                                    String line = in.nextLine().trim();
                                    if (line.equals("stop")) {
                                        if (!jccm.isAnswered()) {
                                            done = true;
                                            jccm.closeFrame();
                                    	 	logger.info("Завершение приложения...");
                                            System.exit(0);
                                        }
                                    } else if (line.startsWith("answer:")) {
                                        done = true;
                                        if (line.substring(line.indexOf(":") + 1).equalsIgnoreCase(jccm.getFldSrcCur())) {
                                            jccm.setAnswered(true);
                                        }
                                        System.out.println("Answer " + line.substring(line.indexOf(":") + 1));
                                    }
                                }
                                in.close();
                            } catch (Exception e) {
                            }
                        }
                    });
                    t.start();
                }

            } catch (java.net.BindException b) {
            	logger.info("Приложение уже запущено");
            	System.exit(-1);
            } catch (Exception e) {
            	logger.error(e.toString());
                e.printStackTrace();
                System.exit(-1);
            }
        }

    }
    
    
}
