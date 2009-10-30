package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 *        Apache Software Foundation (http://www.apache.org/)."
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
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A multithreaded, reusable XML-RPC client object. This version uses a homegrown
 * HTTP client which can be quite a bit faster than java.net.URLConnection, especially
 * when used with XmlRpc.setKeepAlive(true).
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @version $Id$
 */
public class XmlRpcClientLite
	extends XmlRpcClient
{
	static String auth;

	/**
	 * Construct a XML-RPC client with this URL.
	 */
	public XmlRpcClientLite(URL url)
	{
		super(url);
	}

	/**
	 * Construct a XML-RPC client for the URL represented by this String.
	 */
	public XmlRpcClientLite(String url)
		throws MalformedURLException
	{
		super(url);
	}

	/**
	 * Construct a XML-RPC client for the specified hostname and port.
	 */
	public XmlRpcClientLite(String hostname, int port)
		throws MalformedURLException
	{
		super(hostname, port);
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

				return new LiteWorker();
			}

			throw new IOException("XML-RPC System overload");
		}
	}

	/**
	 *
	 */
	class LiteWorker
		extends Worker
		implements Runnable
	{
		HttpClient client = null;

		/**
		 *
		 */
		public LiteWorker()
		{
			super();
		}

		/**
		 *
		 * @param method
		 * @param params
		 * @return
		 * @throws XmlRpcException
		 * @throws IOException
		 */
		Object execute(String method, Vector<Comparable> params)
			throws XmlRpcException, IOException
		{
			long now = System.currentTimeMillis();

			fault = false;

			try
			{
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

				// and send it to the server
				if (client == null)
				{
					client = new HttpClient(url);
				}

				InputStream in = null;

				// send request to the server and get an input stream
				// from which to read the response
				try
				{
					in = client.sendRequest(request);
				}
				catch (IOException iox)
				{

					// if we get an exception while sending the request,
					// and the connection is a keepalive connection, it may
					// have been timed out by the server. Try again.
					if (client.keepalive)
					{
						client.closeConnection();
						client.initConnection();

						in = client.sendRequest(request);
					}
					else
					{
						throw iox;
					}
				}

				// parse the response
				parse(in);

				// client keepalive is always false if XmlRpc.keepalive is false
				if (!client.keepalive)
				{
					client.closeConnection();

					client = null;
				}

				if (debug)
				{
					System.out.println("result = " + result);
				}

				// check for errors from the XML parser
				if (errorLevel == FATAL)
				{
					throw new Exception(errorMsg);
				}
			}
			catch (IOException iox)
			{

				// this is a lower level problem,  client could not talk to
				// server for some reason.
				throw iox;
			}
			catch (Exception x)
			{

				// same as above, but exception has to be converted to
				// IOException.
				if (XmlRpc.debug)
				{
					x.printStackTrace();
				}

				String msg = x.getMessage();

				if ((msg == null) || (msg.length() == 0))
				{
					msg = x.toString();
				}

				throw new IOException(msg);
			}

			if (fault)
			{

				// this is an XML-RPC-level problem, i.e. the server reported an error.
				// throw an XmlRpcException.
				XmlRpcException exception = null;

				try
				{
					Hashtable<?, ?> f = (Hashtable<?, ?>) result;
					String faultString = (String) f.get("faultString");
					int faultCode = Integer.parseInt(f.get("faultCode").toString());

					exception = new XmlRpcException(faultCode, faultString.trim());
				}
				catch (Exception x)
				{
					throw new XmlRpcException(0, "Server returned an invalid fault response.");
				}

				throw exception;
			}

			if (debug)
			{
				System.out.println("Spent " + (System.currentTimeMillis() - now) + " millis in request");
			}

			return result;
		}
	}    // end of class Worker

	/**
	 * A replacement for java.net.URLConnection, which seems very slow on
	 * MS Java.
	 */
	class HttpClient
	{
		String hostname;
		String host;
		int port;
		String uri;
		Socket socket = null;
		BufferedOutputStream output;
		BufferedInputStream input;
		boolean keepalive;
		byte[] buffer;

		/**
		 *
		 * @param url
		 * @throws IOException
		 */
		public HttpClient(URL url)
			throws IOException
		{
			hostname = url.getHost();
			port = url.getPort();

			if (port < 1)
			{
				port = 80;
			}

			uri = url.getFile();

			if ((uri == null) || "".equals(uri))
			{
				uri = "/";
			}

			host = (port == 80)
				   ? hostname
				   : hostname + ":" + port;

			initConnection();
		}

		/**
		 *
		 * @throws IOException
		 */
		protected void initConnection()
			throws IOException
		{
			socket = new Socket(hostname, port);
			output = new BufferedOutputStream(socket.getOutputStream());
			input = new BufferedInputStream(socket.getInputStream());
		}

		/**
		 *
		 */
		protected void closeConnection()
		{
			try
			{
				socket.close();
			}
			catch (Exception ignore) {}
		}

		/**
		 *
		 * @param request
		 * @return
		 * @throws IOException
		 */
		public InputStream sendRequest(byte[] request)
			throws IOException
		{
			output.write(("POST " + uri + " HTTP/1.0\r\n").getBytes());
			output.write(("User-Agent: " + XmlRpc.version + "\r\n").getBytes());
			output.write(("Host: " + host + "\r\n").getBytes());

			if (XmlRpc.getKeepAlive())
			{
				output.write("Connection: Keep-Alive\r\n".getBytes());
			}

			output.write("Content-Type: text/xml\r\n".getBytes());

			if (auth != null)
			{
				output.write(("Authorization: Basic " + auth + "\r\n").getBytes());
			}

			output.write(("Content-Length: " + request.length).getBytes());
			output.write("\r\n\r\n".getBytes());
			output.write(request);
			output.flush();

			// start reading  server response headers
			String line = readLine();

			if (XmlRpc.debug)
			{
				System.out.println(line);
			}

			int contentLength = -1;

			try
			{
				StringTokenizer tokens = new StringTokenizer(line);
				String httpversion = tokens.nextToken();
				String statusCode = tokens.nextToken();
				String statusMsg = tokens.nextToken("\n\r");

				keepalive = XmlRpc.getKeepAlive() && "HTTP/1.1".equals(httpversion);

				if (!"200".equals(statusCode))
				{
					throw new IOException("Unexpected Response from Server: " + statusMsg);
				}
			}
			catch (IOException iox)
			{
				throw iox;
			}
			catch (Exception x)
			{

				// x.printStackTrace ();
				throw new IOException("Server returned invalid Response.");
			}

			do
			{
				line = readLine();

				if (line != null)
				{
					if (XmlRpc.debug)
					{
						System.out.println(line);
					}

					line = line.toLowerCase();

					if (line.startsWith("content-length:"))
					{
						contentLength = Integer.parseInt(line.substring(15).trim());
					}

					if (line.startsWith("connection:"))
					{
						keepalive = XmlRpc.getKeepAlive() && (line.indexOf("keep-alive") > -1);
					}
				}
			}
			while ((line != null) &&!line.equals(""))
			;

			return new ServerInputStream(input, contentLength);
		}

		/**
		 *
		 * @return
		 * @throws IOException
		 */
		private String readLine()
			throws IOException
		{
			if (buffer == null)
			{
				buffer = new byte[2048];
			}

			int next;
			int count = 0;

			while (true)
			{
				next = input.read();

				if ((next < 0) || (next == '\n'))
				{
					break;
				}

				if (next != '\r')
				{
					buffer[count++] = (byte) next;
				}

				if (count >= buffer.length)
				{
					throw new IOException("HTTP Header too long");
				}
			}

			return new String(buffer, 0, count);
		}

		/**
		 *
		 * @throws Throwable
		 */
		protected void finalize()
			throws Throwable
		{
			closeConnection();
		}
	}

	/**
	 * Just for testing.
	 */
	public static void main(String args[])
		throws Exception
	{

		// XmlRpc.setDebug (true);
		try
		{
			String url = args[0];
			String method = args[1];
			XmlRpcClientLite client = new XmlRpcClientLite(url);
			Vector<Comparable> v = new Vector<Comparable>();

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

			// XmlRpc.setEncoding ("UTF-8");
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
