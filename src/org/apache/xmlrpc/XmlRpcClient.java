package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation(http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "XML-RPC" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES(INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

/**
 * A multithreaded, reusable XML-RPC client object. Use this if you
 * need a full-grown HTTP client (e.g. for Proxy and Basic Auth
 * support). If you don't need that, <code>XmlRpcClientLite</code> may
 * work better for you.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @version $Id$
 */
public class XmlRpcClient
	implements XmlRpcHandler
{
	protected URL url;
	private String auth;

	// pool of worker instances
	protected Stack pool = new Stack();
	protected int workers = 0;
	protected int asyncWorkers = 0;

	// a queue of calls to be handled asynchronously
	private CallData first, last;

	/**
	 * Construct a XML-RPC client with this URL.
	 */
	public XmlRpcClient(URL url)
	{
		this.url = url;

		if (XmlRpc.debug)
		{
			System.out.println("Created client to url space " + url);
		}
	}

	/**
	 * Construct a XML-RPC client for the URL represented by this String.
	 */
	public XmlRpcClient(String url)
		throws MalformedURLException
	{
		this(new URL(url));
	}

	/**
	 * Construct a XML-RPC client for the specified hostname and port.
	 */
	public XmlRpcClient(String hostname, int port)
		throws MalformedURLException
	{
		this(new URL("http://" + hostname + ':' + port + "/RPC2"));
	}

	/**
	 * Return the URL for this XML-RPC client.
	 */
	public URL getURL()
	{
		return url;
	}

	/**
	 * Sets Authentication for this client. This will be sent as Basic
	 * Authentication header to the server as described in
	 * <a href="http://www.ietf.org/rfc/rfc2617.txt">
	 * http://www.ietf.org/rfc/rfc2617.txt</a>.
	 */
	public void setBasicAuthentication(String user, String password)
	{
		if ((user == null) || (password == null))
		{
			auth = null;
		}
		else
		{
			auth = new String(Base64.encode((user + ':' + password).getBytes())).trim();
		}
	}

	/**
	 * Generate an XML-RPC request and send it to the server. Parse the result
	 * and return the corresponding Java object.
	 *
	 * @exception XmlRpcException: If the remote host returned a fault message.
	 * @exception IOException: If the call could not be made because of lower
	 *          level problems.
	 */
	public Object execute(String method, Vector params)
		throws XmlRpcException, IOException
	{
		Worker worker = getWorker(false);

		try
		{
			Object retval = worker.execute(method, params);

			return retval;
		}
		finally
		{
			releaseWorker(worker, false);
		}
	}

	/**
	 * Generate an XML-RPC request and send it to the server in a new thread.
	 * This method returns immediately.
	 * If the callback parameter is not null, it will be called later to handle
	 * the result or error when the call is finished.
	 */
	public void executeAsync(String method, Vector params, AsyncCallback callback)
	{

		// if at least 4 threads are running, don't create any new ones,
		// just enqueue the request.
		if (asyncWorkers >= 4)
		{
			enqueue(method, params, callback);

			return;
		}

		Worker worker = null;

		try
		{
			worker = getWorker(true);

			worker.start(method, params, callback);
		}
		catch (IOException iox)
		{

			// make a queued worker that doesn't run immediately
			enqueue(method, params, callback);
		}
	}

	/**
	 *
	 * @param async
	 * @return
	 * @throws IOException
	 */
	synchronized Worker getWorker(boolean async)
		throws IOException
	{
		try
		{
			Worker w = (Worker) pool.pop();

			if (async)
			{
				asyncWorkers += 1;
			}
			else
			{
				workers += 1;
			}

			return w;
		}
		catch (EmptyStackException x)
		{
			if (workers < XmlRpc.getMaxThreads())
			{
				if (async)
				{
					asyncWorkers += 1;
				}
				else
				{
					workers += 1;
				}

				return new Worker();
			}

			throw new IOException("XML-RPC System overload");
		}
	}

	/**
	 * Release possibly big per-call object references to allow them to be
	 * garbage collected
	 */
	synchronized void releaseWorker(Worker w, boolean async)
	{
		w.result = null;
		w.call = null;

		if ((pool.size() < 20) &&!w.fault)
		{
			pool.push(w);
		}

		if (async)
		{
			asyncWorkers -= 1;
		}
		else
		{
			workers -= 1;
		}
	}

	/**
	 *
	 * @param method
	 * @param params
	 * @param callback
	 */
	synchronized void enqueue(String method, Vector params, AsyncCallback callback)
	{
		CallData call = new CallData(method, params, callback);

		if (last == null)
		{
			first = last = call;
		}
		else
		{
			last.next = call;
			last = call;
		}
	}

	/**
	 *
	 * @return
	 */
	synchronized CallData dequeue()
	{
		if (first == null)
		{
			return null;
		}

		CallData call = first;

		if (first == last)
		{
			first = last = null;
		}
		else
		{
			first = first.next;
		}

		return call;
	}

	/**
	 *
	 */
	class Worker
		extends XmlRpc
		implements Runnable
	{
		boolean fault;
		Object result = null;

		/**
		 * The output buffer used in creating a request.
		 */
		ByteArrayOutputStream buffer;
		CallData call;

		/**
		 *
		 */
		public Worker()
		{
			super();
		}

		/**
		 *
		 * @param method
		 * @param params
		 * @param callback
		 */
		public void start(String method, Vector params, AsyncCallback callback)
		{
			this.call = new CallData(method, params, callback);

			Thread t = new Thread(this);

			t.start();
		}

		/**
		 *
		 */
		public void run()
		{
			while (call != null)
			{
				executeAsync(call.method, call.params, call.callback);

				call = dequeue();
			}

			releaseWorker(this, true);
		}

		/**
		 * Execute an XML-RPC call and handle asyncronous callback.
		 */
		void executeAsync(String method, Vector params, AsyncCallback callback)
		{
			Object res = null;

			try
			{
				res = execute(method, params);

				// notify callback object
				if (callback != null)
				{
					callback.handleResult(res, url, method);
				}
			}
			catch (Exception x)
			{
				if (callback != null)
				{
					try
					{
						callback.handleError(x, url, method);
					}
					catch (Exception ignore) {}
				}
			}
		}

		/**
		 * Execute an XML-RPC call.
		 */
		Object execute(String method, Vector params)
			throws XmlRpcException, IOException
		{
			fault = false;

			long now = 0;

			if (XmlRpc.debug)
			{
				System.out.println("Client calling procedure '" + method + "' with parameters " + params);

				now = System.currentTimeMillis();
			}

			try
			{
				ByteArrayOutputStream bout = new ByteArrayOutputStream();

				if (buffer == null)
				{
					buffer = new ByteArrayOutputStream();
				}
				else
				{
					buffer.reset();
				}

				XmlWriter writer = new XmlWriter(buffer, encoding);

				writeRequest(writer, method, params);
				writer.flush();

				byte[] request = buffer.toByteArray();
				URLConnection con = url.openConnection();

				con.setDoInput(true);
				con.setDoOutput(true);
				con.setUseCaches(false);
				con.setAllowUserInteraction(false);
				con.setRequestProperty("Content-Length", Integer.toString(request.length));
				con.setRequestProperty("Content-Type", "text/xml");

				if (auth != null)
				{
					con.setRequestProperty("Authorization", "Basic " + auth);
				}

				OutputStream out = con.getOutputStream();

				out.write(request);
				out.flush();
				out.close();

				InputStream in = con.getInputStream();

				parse(in);
			}
			catch (Exception x)
			{
				if (XmlRpc.debug)
				{
					x.printStackTrace();
				}

				throw new IOException(x.getMessage());
			}

			if (fault)
			{

				// generate an XmlRpcException
				XmlRpcException exception = null;

				try
				{
					Hashtable f = (Hashtable) result;
					String faultString = (String) f.get("faultString");
					int faultCode = Integer.parseInt(f.get("faultCode").toString());

					exception = new XmlRpcException(faultCode, faultString.trim());
				}
				catch (Exception x)
				{
					throw new XmlRpcException(0, "Invalid fault response");
				}

				throw exception;
			}

			if (XmlRpc.debug)
			{
				System.out.println("Spent " + (System.currentTimeMillis() - now) + " in request");
			}

			return result;
		}

		/**
		 * Called when the return value has been parsed.
		 */
		protected void objectParsed(Object what)
		{
			result = what;
		}

		/**
		 * Generate an XML-RPC request from a method name and a parameter vector.
		 */
		void writeRequest(XmlWriter writer, String method, Vector params)
			throws IOException, XmlRpcException
		{
			writer.startElement("methodCall");
			writer.startElement("methodName");
			writer.write(method);
			writer.endElement("methodName");
			writer.startElement("params");

			int l = params.size();

			for (int i = 0; i < l; i++)
			{
				writer.startElement("param");
				writer.writeObject(params.elementAt(i));
				writer.endElement("param");
			}

			writer.endElement("params");
			writer.endElement("methodCall");
		}

		/**
		 * Overrides method in XmlRpc to handle fault repsonses.
		 */
		public void startElement(String name, AttributeList atts)
			throws SAXException
		{
			if ("fault".equals(name))
			{
				fault = true;
			}
			else
			{
				super.startElement(name, atts);
			}
		}
	}    // end of inner class Worker

	class CallData
	{
		String method;
		Vector params;
		AsyncCallback callback;
		CallData next;

		/**
		 * Make a call to be queued and then executed by the next free async
		 * thread
		 */
		public CallData(String method, Vector params, AsyncCallback callback)
		{
			this.method = method;
			this.params = params;
			this.callback = callback;
			this.next = null;
		}
	}

	/**
	 * Just for testing.
	 */
	public static void main(String args[])
		throws Exception
	{

		// XmlRpc.setDebug(true);
		// XmlRpc.setKeepAlive(true);
		try
		{
			String url = args[0];
			String method = args[1];
			Vector v = new Vector();

			for (int i = 2; i < args.length; i++)
			{
				try
				{
					v.addElement(new Integer(Integer.parseInt(args[i])));
				}
				catch (NumberFormatException nfx)
				{
					v.addElement(args[i]);
				}
			}

			XmlRpcClient client = new XmlRpcClientLite(url);

			try
			{
				System.out.println(client.execute(method, v));
			}
			catch (Exception ex)
			{
				System.err.println("Error: " + ex.getMessage());
			}
		}
		catch (Exception x)
		{
			System.err.println(x);
			System.err.println("Usage: java org.apache.xmlrpc.XmlRpcClient " + "<url> <method> <arg> ....");
			System.err.println("Arguments are sent as integers or strings.");
		}
	}
}
