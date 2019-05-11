package com.emistoolbox.jetty;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application; 
import javafx.scene.Group; 
import javafx.scene.Scene; 
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;  
import javafx.stage.Stage;  
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.animation.AnimationTimer; 

import java.util.concurrent.*; 
import java.util.*; 

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;

public class EmisToolboxJetty extends Application
{
    private BlockingQueue<String> status = new LinkedBlockingQueue<String>();

    private TextArea txa = new TextArea();
    private Server server; 
   
    public static void main(String[] args) throws Exception
    {
        EmisToolboxJetty app = new EmisToolboxJetty(); 
        app.start(8080, args); 
        
        Log.setLog(new StdErrLog()); 
    }
    
    private void start(final int port, final String[] args)
        throws Exception
    {
        message("Welcome to the EMIS Toolbox");
        

        if (!isEmisToolboxRunning(port))
        {
	        // Starting UI
	        new Thread(new Runnable() {
	            public void run()
	            { 
	                try { launchJetty(port); }
	                catch (Throwable err)
	                { message(err); }
	            }
	        }).start(); 
	
	        AnimationTimer timer = new AnimationTimer() {
	            @Override
	            public void handle(long now) 
	            {
	                List<String> newStrings = new ArrayList<String>();
	                status.drainTo(newStrings);
	                for (String s : newStrings) 
	                    txa.appendText(s);
	            }
	        };
	        timer.start();
        }
        
        launch(args); 
        if (server != null)
            server.stop(); 
    }
    
    private boolean isEmisToolboxRunning(int port)
    {
    	String content = null; 
        Scanner scanner = null; 
        try { 
        	scanner =new Scanner(new URL("http://localhost:" + port + "/test?action=ping").openStream(), StandardCharsets.UTF_8.toString()); 
            scanner.useDelimiter("\\A");
            content = scanner.hasNext() ? scanner.next() : "";
        }
    	catch (Exception ex)
    	{}
        finally {
        	if (scanner != null)
        		scanner.close(); 
        }
	        
        return content != null && content.indexOf("pong") != -1; 
    }
    
    private void launchJetty(int port)
        throws Exception
    {
        message("Starting EMIS Toolbox engine - please wait...");
        
        server = new Server(port);

        // Setup JMX
        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);

        File emistoolboxPath = getPath().getParentFile(); 
        System.setProperty("emistoolbox.path", emistoolboxPath.getAbsolutePath()); 
        System.setProperty("emistoolbox.path.writable", emistoolboxPath.getAbsolutePath()); 
        System.out.println(emistoolboxPath.getAbsolutePath()); 

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/emistoolbox");
        File warFile = new File(getPath(), "emistoolbox.war"); 
        webapp.setWar(warFile.getAbsolutePath());

        // A WebAppContext is a ContextHandler as well so it needs to be set to
        // the server so it is aware of where to send the appropriate requests.
        server.setHandler(webapp);

        // Start things up!
        server.start(); // server.stop() to finish
//        server.dumpStdErr();
        
        Thread.sleep(3000);
        launch(port); 
        message("Launching EMIS Toolbox user interface."); 
    }
    
    private void message(String message)
    {
        try { status.put("\n" + message); }
        catch (Exception ex)
        {}
    }
    
    private void message(Throwable err)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        err.printStackTrace(pw);
        message(sw.toString()); // stack trace as a string
    }

    private static File getExePath()
    {
        String path = EmisToolboxJetty.class.getProtectionDomain().getCodeSource().getLocation().getPath(); 
        System.out.println("A: " + path); 
        path = path.replaceAll("\\%20", " "); 
        System.out.println("B: " + path); 
        System.out.flush(); 

        return new File(path); 
    }

    private static File getPath()
    { return getExePath().getParentFile(); }
    
    @Override
    public void start(Stage stage) throws FileNotFoundException 
    {
        //Creating an image 
        Image image = loadClasspathImage("emistoolbox.jpg"); 
      
        //Setting the image view 
        ImageView imageView = new ImageView(image); 

        //Setting the position of the image 
        imageView.setX(50); 
        imageView.setY(25); 

        //setting the fit height and width of the image view 
        imageView.setFitHeight(160); 
        imageView.setFitWidth(360); 

        //Setting the preserve ratio of the image view 
        imageView.setPreserveRatio(true);  

        //Creating a Group object   
        BorderPane border = new BorderPane();     
        border.setTop(imageView); 
        border.setCenter(txa); 

        Group root = new Group(border);  

        //Creating a scene object 
        Scene scene = new Scene(root, 600, 500);  

        //Setting title to the Stage 
        stage.setTitle("Emis Toolbox");  

        //Adding scene to the stage 
        stage.setScene(scene);
        stage.getIcons().add(loadClasspathImage("emis.png")); 

        //Displaying the contents of the stage 
        stage.show(); 
    }  

    private static Image loadClasspathImage(String path)
    { return new Image(EmisToolboxJetty.class.getResourceAsStream(path)); }   

    private static void launch(int port)
    {
        System.out.println("Launching browser");
        try { exec("cmd.exe /c start http://localhost:" + port + "/emistoolbox/"); }
        catch (Exception e)
        { e.printStackTrace(); }
    }

    private static void exec(String command)
        throws Exception
    {
        System.out.println(command);
        Runtime runtime = Runtime.getRuntime();
        runtime.exec(command);
    }
   
    private void write(File outputPath, InputStream in)
    {
    	try { Files.copy(in, Paths.get(outputPath.getAbsolutePath())); }
    	catch (IOException ex) 
    	{}
    }
}
