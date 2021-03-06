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
import java.util.Arrays;
import java.util.List;

import static nomcat.TerminalColors.BLUE;
import static nomcat.TerminalColors.RESET;

/**
 * @author acath
 */
public class Main
{
    public static int port = 10080;

    public static void main(String[] args) throws Exception
    {
        List<String> argv = new ArrayList<String>(Arrays.asList(args));

        if (argv.contains("-h") || argv.contains("--help"))
        {
            usage();
        }

        String portStr = extractFlagValue("-p", argv);
        if (portStr != null)
        {
            try
            {
                port = Integer.parseInt(portStr);
            }
            catch (NumberFormatException e)
            {
                usage();
            }
        }

        String workingDir =
                argv.size() > 0 ? argv.get(0) : ".";
        List<String> specificWARs =
                argv.size() > 1 ? makeRelativeTo(workingDir, argv.subList(1, argv.size())) : null;

        startServer(workingDir, specificWARs);
    }

    private static String extractFlagValue(String flag, List<String> argv)
    {
        int flagIdx = argv.indexOf(flag);
        if (flagIdx < 0)
            return null;

        int valIdx = flagIdx + 1;
        if (valIdx >= argv.size())
            usage();
        String result = argv.get(valIdx);
        argv.remove(valIdx);
        argv.remove(flagIdx);
        return result;
    }

    private static void usage()
    {
        info("Nomcat: a simple WAR server that's not Tomcat\n");
        info("Usage: nomcat.sh [-p PORT] [WAR_1] [WAR_2]\n");
        info("If WAR_X is omitted, nomcat will serve any WARs in $CWD, $CWD/wars, or $CWD/dist");
        System.exit(0);
    }

    private static void startServer(String workingDir, List<String> specificWARs) throws Exception
    {
        Server server = new Server(port);

        List<ContextHandler> warHandlers = createWarHandlers(workingDir, specificWARs);
        Handler indexHandler = createIndexContext(warHandlers);

        List<Handler> allHandlers = new ArrayList<Handler>();
        allHandlers.addAll(warHandlers);
        allHandlers.add(indexHandler);
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(allHandlers.toArray(new Handler[]{}));
        server.setHandler(handlerList);

        server.start();

        info("\n##############################\n# Server up on port " + port + "!\n##############################");

        server.join();
    }

    private static List<ContextHandler> createWarHandlers(String workingDir, List<String> specificWARsRequested)
    {
        final List<ContextHandler> warHandlers = new ArrayList<ContextHandler>();

        if (specificWARsRequested != null)
        {
            warHandlers.addAll(createWarContexts(gatherSpecificWars(specificWARsRequested).toArray(new File[]{})));
        }
        else
        {
            warHandlers.addAll(createWarContexts(findWarFiles(new File(workingDir))));
            String warsDir = workingDir + "/wars";
            warHandlers.addAll(createWarContexts(findWarFiles(new File(warsDir))));
            String distDir = workingDir + "/dist";
            warHandlers.addAll(createWarContexts(findWarFiles(new File(distDir))));

            if (warHandlers.isEmpty())
            {
                info("No WAR files found in " + workingDir + ", " + warsDir + ", or " + distDir + ". \nExiting.");
                System.exit(0);
            }
        }
        return warHandlers;
    }

    private static List<File> gatherSpecificWars(List<String> warPaths)
    {
        List<File> warFiles = new ArrayList<File>();
        for (String warPath : warPaths)
        {
            File warFile = new File(warPath);
            if (! (warFile.exists() && warFile.getName().endsWith(".war")))
            {
                info(warPath + " is not a WAR file. Exiting.");
                System.exit(1);
            }
            warFiles.add(warFile);
        }
        return warFiles;
    }

    private static List<String> makeRelativeTo(String parent, List<String> files)
    {
        List<String> result = new ArrayList<String>();
        for (String file : files)
            result.add(parent + "/" + file);
        return result;
    }

    private static void info(String msg)
    {
        System.out.println(BLUE + msg + RESET);
    }

    private static File[] findWarFiles(File rootDir)
    {
        if (!rootDir.isDirectory())
            return new File[]{};
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
