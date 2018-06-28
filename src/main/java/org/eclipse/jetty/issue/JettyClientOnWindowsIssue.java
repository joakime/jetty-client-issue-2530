package org.eclipse.jetty.issue;

import java.io.ByteArrayInputStream;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public class JettyClientOnWindowsIssue
{
    public static void main(final String[] args) throws Exception
    {
        final Server server = createServer();

        try
        {
            server.start();

            demonstrateWindowsBug();
        }
        finally
        {
            server.stop();
        }
    }

    private static String getPayload(final int payloadSize)
    {
        final StringBuilder builder = new StringBuilder();

        for (int loop = payloadSize; loop >= 0; loop--)
        {
            builder.append("a");
        }

        return builder.toString();
    }

    private static void demonstrateWindowsBug() throws Exception
    {
        final HttpClient client = new HttpClient();
        client.start();

        final int increment = 32 * 1024;
        try
        {
            for (int size = increment; size < 100 * increment; size += increment)
            {
                System.out.println("****************** REQUEST size=" + size);
                final Request request = client.POST("http://localhost:8888");
                request.content(new InputStreamContentProvider(new ByteArrayInputStream(getPayload(size).getBytes("UTF8"))));
                final ContentResponse response = request.send();
                System.out.println("****************** REQUEST size=" + size + "; RESPONSE=" + response.getStatus());

                Thread.sleep(1_000);
            }
        }
        finally
        {
            client.stop();
        }
    }

    private static Server createServer()
    {
        Server server = new Server();
        ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory());
        httpConnector.setPort(8888);
        server.addConnector(httpConnector);
        return server;
    }
}
