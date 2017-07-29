package work;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;

import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.opencsv.CSVWriter;

import edu.umass.cs.benchlab.har.HarEntries;
import edu.umass.cs.benchlab.har.HarEntry;
import edu.umass.cs.benchlab.har.HarLog;
import edu.umass.cs.benchlab.har.tools.HarFileReader;

public class Datalicious {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static String harPath="E:\\log.har";
	public static boolean HitGa = false;
    public static boolean HitOp= true;
	private static String url;
	private static String urlParam;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		//Task 1 Approach: Create proxy and start the browser/
		
		BrowserMobProxy proxy = new BrowserMobProxyServer();
		proxy.start(0);
		Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
		DesiredCapabilities capabilities = new DesiredCapabilities();
	    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
	    
		System.setProperty("webdriver.gecko.driver", "E:\\selenium drivers\\geckodriver.exe");
		WebDriver driver = new FirefoxDriver();
		
        driver.get("https://www.google.co.in/?gfe_rd=cr&ei=3bB6WdHnDMiL8QfYrYzwAg");
        driver.findElement(By.xpath("//input[@id = 'lst-ib']")).sendKeys("Datalicious");
        driver.findElement(By.name("btnK")).click();
        driver.findElement(By.xpath("//*[contains(@href,'datalicious.com')]")).click();
        
        //Task 2: Use proxy to bypass all HTTP responses. Generate .har file and read the contents. 
        proxy.newHar("seleniumeasy.com");
        Thread.sleep(5000);
        Har har = proxy.getHar();
        File harFile = new File(harPath);
        FileOutputStream fos = new FileOutputStream(harFile);
        har.writeTo(fos);
        if (driver != null)
        proxy.endHar();
        
        HarFileReader read = new HarFileReader();
        HarLog log = read.readHarFile(harFile);
        HarEntries entries = log.getEntries();
        boolean HitGa = false;
        boolean HitOp= true;
        List<HarEntry> allentry = entries.getEntries();
        for (HarEntry entry : allentry)
        {
        	HitGa = entry.getRequest().getUrl().contains("google-analytics.com/r/collect");
        	if(HitGa)
        	{System.out.println("GA was hit");
        	url= entry.getRequest().getUrl();
        	break;
        	}
        	HitOp = entry.getRequest().getUrl().contains("dc.optimahub.com/");
        	if(HitOp)
        	{System.out.println("Optimahub was hit");
        	url= entry.getRequest().getUrl();
        	break;
        	}
        }
        //Task 3: Obtain the url from previous task and split the parameters to get Document title 
        //and use opencsv lib to store in log file.
        //On observation it was noted that document path is not present in the link hence no test case is written for that.
        
        for(String pair : url.split("&"))
        {
            if( pair.split("=")[0] == "dt")
            {
            urlParam = pair.split("=")[1];
            break;
            }
        }
        
        CSVWriter writecsv = new CSVWriter(new FileWriter("E:\\logger.csv"));
        ((Appendable) writecsv).append(urlParam);
        writecsv.close();
	}

}
