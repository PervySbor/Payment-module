package payment.module;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class TomcatEmbedRunner {
    public static void main(String[] args) throws Exception{
        Tomcat tomcat = new Tomcat();

        tomcat.setPort(8079);
        tomcat.getConnector();
        tomcat.setBaseDir("tomcat-base-dir");

        Host host = tomcat.getHost();
        String webAppLocation = new File("src/main/webapp/").getAbsolutePath();

        Context ctx = tomcat.addWebapp(host, "/payment", webAppLocation);

        File classesDir = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(
                resources,
                "/WEB-INF/classes",
                classesDir.getAbsolutePath(),
                "/"
        ));
        ctx.setResources(resources);

        WebappLoader webappLoader = new WebappLoader(/*Thread.currentThread().getContextClassLoader()*/);
        webappLoader.setDelegate(true);  // аналог <Loader delegate="true"/>
        ctx.setLoader(webappLoader);


        tomcat.start();
    }
}
