package server;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
 
import com.google.zxing.*;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.ByteMatrix;
public class ScreenShootServer {
	static ServerSocket serverSocket;
	static Socket socket;
	static int port = 1000;
	
	public static void main(String[] args) throws IOException, HeadlessException, AWTException{
		trayIcon();
		runServer();
	}
	
	public static BufferedImage screenshot() throws HeadlessException, AWTException{
		return new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
	}
	
	public static void trayIcon(){
		if(!SystemTray.isSupported()){
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		final Image batman = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("ScreenshotServerIcon.png")).getImage();
		final TrayIcon trayIcon = new TrayIcon(batman);
		final SystemTray tray = SystemTray.getSystemTray();
		
		//Create pup-up menu components
		MenuItem textIp = new MenuItem("IP-Addresse und Port");
		MenuItem qrIp = new MenuItem("IP as QR-Code");
		MenuItem exit = new MenuItem("Exit");
		
		//Add components
		popup.add(textIp);
		popup.add(qrIp);
		popup.addSeparator();
		popup.add(exit);
		
		trayIcon.setPopupMenu(popup);
		
		try{tray.add(trayIcon);
		
		}catch (AWTException e){
			System.out.println("Trayicon could not be added.");
		}
		
		exit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(socket!=null)
					try {
						socket.close();
						System.out.println("closed the Server");
					} catch (IOException e) {
						e.printStackTrace();
					}
				System.out.println("killing myself");
				System.exit(0);
			}
		});
		

		textIp.addActionListener(new ActionListener(){ //shows the IP Address
			@Override
			public void actionPerformed(ActionEvent arg0) {
			  
				try {
					
					//JOptionPane.showMessageDialog(null, address);
					trayIcon.displayMessage("IP-Addesse : Port", address()+" : "+port, TrayIcon.MessageType.INFO);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	
		qrIp.addActionListener(new ActionListener(){ //shows the IP Address as QR-code
			@Override
			public void actionPerformed(ActionEvent arg0) { //stackoverflow qr-code encode and decode using zxing
			  	// get a bit matrix for the data
				Writer writer = new	QRCodeWriter();
			  	BitMatrix matrix;
			  	int size = 600;
			  	try{
			  		matrix = writer.encode(address(), BarcodeFormat.QR_CODE, size, size);
			  	} catch (WriterException | UnknownHostException e){
			  		return;
			  	}
			  	//generate an Image from the Bit matrix
			  	BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			  	
			  	BitArray b = new BitArray(size);
			  	for(int i=0;i<size;i++){
			  		b=matrix.getRow(i, b);
			  		for(int  j=0;j<size;j++){

			  			image.setRGB(j,i, (b.get(j)? 0 : 0xFFFFFF));
			  		}
			  	}


			  	//display image as dialog
			  	JDialog dialog = new JDialog();
			  	dialog.setModal(false);
			  	dialog.addFocusListener(new FocusListener() { //closes the dialog if the focus is lost
					
					@Override
					public void focusLost(FocusEvent e) {
						dialog.setVisible(false);
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						
					}
				});
			  	
			  	dialog.setUndecorated(true);
			  	JLabel label = new JLabel( new ImageIcon(image) );
			  	dialog.add( label );
			  	dialog.pack();
			  	dialog.setLocationRelativeTo(null);
			  	dialog.setVisible(true);
			  	
			}
		});
		
		
	}
	
	private static String address() throws UnknownHostException{
		String address = InetAddress.getLocalHost().getHostAddress();
		System.out.println(address);
		return address;
	}

	
	public static void runServer()throws HeadlessException, AWTException{
		try{
			serverSocket = new ServerSocket(port);
			System.out.println("Server started. Listening on "+serverSocket.getLocalSocketAddress().toString());
			while(true){
				socket = serverSocket.accept();
				System.out.println("client connected to server");
				BufferedImage tosend = screenshot();
				ImageIO.write(tosend, "png", socket.getOutputStream());
				socket.getOutputStream().flush();
				socket.close();
				System.out.println("server closed the connection");
			}
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, e.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}
