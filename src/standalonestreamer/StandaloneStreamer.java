/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package standalonestreamer;

import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.awt.*;
import java.awt.event.MouseListener;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.Group;
import javafx.scene.control.SplitPane.Divider;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;




/**
 *
 * @author Alexander Timothy
 */
public class StandaloneStreamer extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        
        return;
        
             
    }

    
    public void toggleSplitPane(MouseEvent e, SplitPane splitPane1){
        double[] sliderPositions = splitPane1.getDividerPositions();
        Object[] splitPaneItems = splitPane1.getItems().toArray();
        
        if(e.getSource().equals(splitPaneItems[0])) {//lefthbox
            if(sliderPositions[0] > .9){
            splitPane1.setDividerPositions(.5); 
            
            }
            else{
                splitPane1.setDividerPositions(1); 
            }
        }
        
        if(e.getSource().equals(splitPaneItems[1])) {//lefthbox
            if(sliderPositions[0] < .1){
            splitPane1.setDividerPositions(.5); 
            
            }
            else{
                splitPane1.setDividerPositions(0); 
            }
        }
        
        
        
        
    }
      
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
              
        System.out.println("started");
       Viewer view = new Viewer(300,300);
       
       view.mainFrame.addMouseListener(view);
       Connect();
       view.StartStream("");               
    }   
    
    
    public static void Connect()throws Exception{
        
        try{
           
            String host = "pi@raspberrypi";
            
            
            
            String psswrd =                                                                                                                                                                 "chickenis69";
            
            SSHClient ssh = new SSHClient();
            
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect("192.168.0.39", 22);
            ssh.authPassword("pi","chickenis69");
            
            Session session = ssh.startSession();
            
            
            //Channel channel = session.openChannel("shell");
            Command cmd = session.exec("raspivid -o - -t 0 -hf -w 800 -h 400 -fps 24 | cvlc -vvv stream:///dev/stdin --sout '#standard{access=http,mux=ts,dst=:8160}' :demux=h264");
            
        }catch(Exception e){
            System.out.println("Exception " + e);
        }
    


    }
}





class Viewer extends JFrame implements MouseListener{
    
    int locationX;
    int locationY;
    String url;
    Canvas videoSurfaceLeft;
    Canvas videoSurfaceRight;
    JPanel leftPlayerPanel;
    JPanel rightPlayerPanel;
    JSplitPane mainSplitPane;
    JFrame mainFrame;
    
    public Viewer(int locationX, int locationY){
        
        //setting size on mainframe breaks everything, let the children decide the size
        mainFrame = new JFrame();
        mainFrame.setLocation(locationX, locationY);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //video surface that holds the stream has to be a canvas, this canvas will be added to the left hand side JPanel
        videoSurfaceLeft = new Canvas();
        videoSurfaceLeft.setMinimumSize(new Dimension(10,10));
        videoSurfaceLeft.setPreferredSize(new Dimension(10,10));
        videoSurfaceLeft.setVisible(true);
        //added to right side JPanel
        videoSurfaceRight = new Canvas();
        videoSurfaceRight.setMinimumSize(new Dimension(10,10));
        videoSurfaceRight.setPreferredSize(new Dimension(10,10));
        videoSurfaceRight.setVisible(true);
        
        leftPlayerPanel = new JPanel(new BorderLayout()); // not sure about border layout but it works
        leftPlayerPanel.add(videoSurfaceLeft); //add the canvas that contains the stream to a jpanel so we can add it to a jsplitpane
        leftPlayerPanel.setPreferredSize(new Dimension(20,20)); //will need to adjust these to fit into main host controller
        
        rightPlayerPanel = new JPanel(new BorderLayout());
        rightPlayerPanel.add(videoSurfaceRight);
        rightPlayerPanel.setPreferredSize(new Dimension(20,20));
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPlayerPanel, rightPlayerPanel);
        mainSplitPane.setResizeWeight(new Double(.5));
        mainSplitPane.setVisible(true);
        mainSplitPane.setPreferredSize(new Dimension(200,200));
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setContinuousLayout(true);
        
        mainFrame.add(mainSplitPane);
        
        mainFrame.setSize(mainFrame.getPreferredSize()); //like I said above, setting the size explicitly does not work, but getting the childrens preferred size does
        
        mainFrame.setVisible(true);
        
        
        mainSplitPane.addMouseListener(this);
    }

    public void StartStream(String url){
    
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:VLC");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(),LibVlc.class);

        MediaPlayerFactory mpf = new MediaPlayerFactory();
        EmbeddedMediaPlayer emp = mpf.newEmbeddedMediaPlayer();
        
        emp.setVideoSurface(mpf.newVideoSurface(videoSurfaceLeft));
        
        MediaPlayerFactory mpfRight = new MediaPlayerFactory();
        EmbeddedMediaPlayer empRight = mpfRight.newEmbeddedMediaPlayer();

        empRight.setVideoSurface(mpfRight.newVideoSurface(videoSurfaceRight));

        String file = "http://192.168.0.39:8160";
        
        empRight.prepareMedia(file);
        empRight.play();
        emp.prepareMedia(file);
        emp.play();
        return;
    
    }   

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        System.out.println("clickdd");
    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
       
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
       
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        
    }
    
    

    
    
    
}


