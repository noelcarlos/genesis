package org.esmartpoint.genesis.plugins.units;

import org.dom4j.Element;
import org.dom4j.Node;
import org.esmartpoint.dbutil.Cronometro;
import org.esmartpoint.genesis.helpers.HttpHelper;
import org.esmartpoint.genesis.plugins.IXMLCommandUnit;
import org.esmartpoint.genesis.util.ApplicationContextHolder;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.XMLProccesorContext;
import org.springframework.context.ApplicationContext;

public class HttpCommandUnit implements IXMLCommandUnit {
	HttpHelper httpHelper;
	
	@Override
	public void setup(XMLProccesorContext context)  throws Exception {
		ApplicationContext springContext = ApplicationContextHolder.getApplicationContext();
		
		httpHelper = springContext.getBean(HttpHelper.class);
		httpHelper.openSession();
		
  /*      HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent("Test/1.1"))
                .add(new RequestExpectContinue(true)).build();

            HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

            HttpCoreContext coreContext = HttpCoreContext.create();
            HttpHost host = new HttpHost("localhost", 8080);
            coreContext.setTargetHost(host);

            DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(8 * 1024);
            ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

            try {

                HttpEntity[] requestBodies = {
                        new StringEntity(
                                "This is the first test request",
                                ContentType.create("text/plain", Consts.UTF_8)),
                        new ByteArrayEntity(
                                "This is the second test request".getBytes(Consts.UTF_8),
                                ContentType.APPLICATION_OCTET_STREAM),
                        new InputStreamEntity(
                                new ByteArrayInputStream(
                                        "This is the third test request (will be chunked)"
                                        .getBytes(Consts.UTF_8)),
                                ContentType.APPLICATION_OCTET_STREAM)
                };

                for (int i = 0; i < requestBodies.length; i++) {
                    if (!conn.isOpen()) {
                        Socket socket = new Socket(host.getHostName(), host.getPort());
                        conn.bind(socket);
                    }
                    BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST",
                            "/servlets-examples/servlet/RequestInfoExample");
                    request.setEntity(requestBodies[i]);
                    System.out.println(">> Request URI: " + request.getRequestLine().getUri());

                    httpexecutor.preProcess(request, httpproc, coreContext);
                    HttpResponse response = httpexecutor.execute(request, conn, coreContext);
                    httpexecutor.postProcess(response, httpproc, coreContext);

                    System.out.println("<< Response: " + response.getStatusLine());
                    System.out.println(EntityUtils.toString(response.getEntity()));
                    System.out.println("==============");
                    if (!connStrategy.keepAlive(response, coreContext)) {
                        conn.close();
                    } else {
                        System.out.println("Connection kept alive...");
                    }
                }
            } finally {
                conn.close();
            }
*/            
	}

	@Override
	public boolean dispatch(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		if (node.getName().equals("post")) {
			commandPost(context, node);
			return true;
		}
		if (node.getName().equals("delete")) {
			commandDelete(context, node);
			return true;
		}
		if (node.getName().equals("put")) {
			commandPut(context, node);
			return true;
		}
		if (node.getName().equals("get")) {
			commandGet(context, node);
			return true;
		}
		return false;
	}

	@Override
	public void cleanup(XMLProccesorContext context) {
		// TODO Auto-generated method stub
		httpHelper.closeSession();
	}
	
	private void commandPost(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String url = context.evaluateExpression(node, node.valueOf("@url")).toString();
		String body = context.evaluateTemplate(node, node.getText());
		String resultType = node.valueOf("@resultType");
		String result = node.valueOf("@result");

		try {
			Object res = httpHelper.post(url, body, resultType);
			if (result != null) {
				context.getVariables().put(result, res);
			}
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw context.createExceptionForNode(evalExc, node, evalExc.getMessage() + " [" + body + "]"); 
		} finally {
			Cronometro.stop("ELASTICSEARCH");
		}
	}
	
	private void commandDelete(XMLProccesorContext context, Node node) throws GenesisRuntimeException {
		//context.setVariables(variables);
		String url = context.evaluateExpression(node, node.valueOf("@url")).toString();
		String resultType = node.valueOf("@resultType");
		String result = node.valueOf("@result");
		
		try {
			Object res = httpHelper.delete(url, resultType);
			if (result != null) {
				context.getVariables().put(result, res);
			}
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw context.createExceptionForNode(evalExc, node, evalExc.getMessage() + " [" + url + "]"); 
		}
	}
	
	private void commandPut(XMLProccesorContext context, Element node) throws GenesisRuntimeException {

	}
	
	private void commandGet(XMLProccesorContext context, Element node) throws GenesisRuntimeException {

	}
}
