package nomcat;

import org.eclipse.jetty.server.handler.ContextHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) AppDynamics Technologies
 *
 * @author acath
 */
public class IndexServlet extends HttpServlet
{
    private final String indexHtml;

    public IndexServlet(List<ContextHandler> warHandlers)
    {
        String html = "<html><body>Nomcat is serving the following WARs:<ul>";
        for (String href : getHrefs(warHandlers))
            html += "<li><a href=\"" + href + "\">" + href + "</a></li>";
        html += "</ul></body></html>";

        this.indexHtml = html;
    }

    private List<String> getHrefs(List<ContextHandler> warHandlers)
    {
        List<String> result = new ArrayList<String>();
        for (ContextHandler handler : warHandlers)
            result.add(handler.getContextPath());
        return result;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.getWriter().write(indexHtml);
    }
}
