package org.apache.velocity.app.event.implement;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.velocity.app.event.RuntimeServicesAware;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Simple event handler that renders method exceptions in the page
 * rather than throwing the exception.  Useful for debugging.
 *
 * <P>By default this event handler renders the exception name only.
 * To include both the exception name and the message, set the property
 * <code>eventhandler.methodexception.message</code> to <code>true</code>.  To render
 * the stack trace, set the property <code>eventhandler.methodexception.stacktrace</code>
 * to <code>true</code>.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id: EventCartridge.java,v 1.5 2004/03/19 17:13:33 dlr Exp $
 */
public class PrintExceptions implements MethodExceptionEventHandler,RuntimeServicesAware
{

    private static String SHOW_MESSAGE = "eventhandler.methodexception.message";
    private static String SHOW_STACK_TRACE = "eventhandler.methodexception.stacktrace";
    private RuntimeServices rs;

    /**
     * Render the method exception, and optionally the exception message and stack trace.
     */
    public Object methodException(Class claz, String method, Exception e) throws Exception
    {
        boolean showMessage = rs.getBoolean(SHOW_MESSAGE,false);
        boolean showStackTrace = rs.getBoolean(SHOW_STACK_TRACE,false);

        StringBuffer st;
        if (showMessage && showStackTrace)
        {
            st = new StringBuffer(200);
            st.append(e.getClass().getName()).append("\n");
            st.append(e.getMessage()).append("\n");
            st.append(getStackTrace(e));

        } else if (showMessage)
        {
            st = new StringBuffer(50);
            st.append(e.getClass().getName()).append("\n");
            st.append(e.getMessage()).append("\n");

        } else if (showStackTrace)
        {
            st = new StringBuffer(200);
            st.append(e.getClass().getName()).append("\n");
            st.append(getStackTrace(e));

        } else
        {
            st = new StringBuffer(15);
            st.append(e.getClass().getName()).append("\n");
        }

        return st.toString();

    }


    private static String getStackTrace(Throwable E)
    {
        try
        {
            StringWriter StackTraceWriter = new StringWriter();
            E.printStackTrace(new PrintWriter(StackTraceWriter));
            StackTraceWriter.flush();
            String StackTrace = StackTraceWriter.toString();
            StackTraceWriter.close();
            return StackTraceWriter.toString() ;
        }
        catch (IOException ioe)
        {
            return "";
        }
    }



    public void setRuntimeServices(RuntimeServices rs) throws Exception
    {
        this.rs = rs;
    }

}
