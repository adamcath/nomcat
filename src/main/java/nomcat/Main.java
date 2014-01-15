package nomcat;


import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import static nomcat.TerminalColors.BLUE;
import static nomcat.TerminalColors.RESET;

/**
 * Copyright (c) AppDynamics Technologies
 *
 * @author acath
 */
public class Main
{
    public static final int PORT = 10080;

    public static void main(String[] args) throws Exception
    {
        Server server = new Server(PORT);

        final List<ContextHandler> warHandlers = new ArrayList<ContextHandler>();
        warHandlers.addAll(createWarContexts(findWarFiles(new File("."))));
        warHandlers.addAll(createWarContexts(findWarFiles(new File("wars"))));

        final List<Handler> allHandlers = new ArrayList<Handler>();
        allHandlers.addAll(warHandlers);
        allHandlers.add(createIndexContext(warHandlers));
        server.setHandler(new HandlerList() {{
            setHandlers(allHandlers.toArray(new Handler[]{}));
        }});

        server.start();

        info("\n##############################\n# Server up on port " + PORT + "!\n##############################");

        server.join();
    }

    private static void info(String msg)
    {
        System.out.println(BLUE + msg + RESET);
    }

    private static File[] findWarFiles(File rootDir)
    {
        return rootDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File file, String s)
            {
                return s.endsWith(".war");
            }
        });
    }

    private static List<ContextHandler> createWarContexts(File[] warFiles)
    {
        List<ContextHandler> result = new ArrayList<ContextHandler>();

        for (final File warFile : warFiles)
        {
            final String contextPath = "/" + warFile.getName().replaceAll("\\.war$", "");

            info("Nomcat: Serving " + warFile.getName() + " at " + contextPath);

            result.add(new WebAppContext() {{
                setContextPath(contextPath);
                setWar(warFile.toURI().toString());
                setThrowUnavailableOnStartupException(true);
            }});
        }

        return result;
    }

    private static Handler createIndexContext(final List<ContextHandler> handlers)
    {
        return new ServletHandler() {{
            addServletWithMapping(
                    new ServletHolder(new IndexServlet(new ArrayList<ContextHandler>(handlers))),
                    "/");
        }};
    }
}
