/* Generated By:JJTree: Do not edit this line. ASTIdentifier.java */

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

/**
 *  ASTIdentifier.java
 *
 *  Method support for identifiers :  $foo
 *
 *  mainly used by ASTRefrence
 *
 *  Introspection is now moved to 'just in time' or at render / execution time.
 *  There are many reasons why this has to be done, but the primary two are 
 *  thread safety, to remove any context-derived information from class member
 *  variables.
 *
 *  Please look at the Parser.jjt file which is
 *  what controls the generation of this class.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTIdentifier.java,v 1.5 2001/01/03 05:25:50 geirm Exp $ 
 */
package org.apache.velocity.runtime.parser.node;

import java.util.Map;
import java.lang.reflect.Method;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.*;
import org.apache.velocity.util.introspection.IntrospectionCacheData;

public class ASTIdentifier extends SimpleNode
{
    private String method = "";
    private String identifier = "";

    public ASTIdentifier(int id)
    {
        super(id);
    }

    public ASTIdentifier(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  simple init - don't do anything that is context specific.
     *  just get what we need from the AST, which is static.
     */
    public  Object init( InternalContextAdapter context, Object data)
        throws Exception
    {
        super.init( context, data );

        identifier = getFirstToken().image;
        method = "get" + identifier;

        return data;
    }

    /**
     *  introspects the class to find the method name of the node,
     *  or if that fails, treats the reference object as a map
     *  and treats the identifier as a key in that map.
     *  This needs work.
     *
     *  @param data Class to be introspected
     */
    private  AbstractExecutor doIntrospection( Class data )
        throws Exception
    {
        /*
         *  Now there might just be an error here.  If there is a typo in the property
         *  then a MapExecutor is created and this needs to be prevented.
         */

        try
        {
            AbstractExecutor executor = new PropertyExecutor();
            Method m = data.getMethod(method,null);
            executor.setData(m);
            return executor;
        }
        catch (NoSuchMethodException nsme)
        {
            AbstractExecutor executor = new MapExecutor();
            executor.setData(identifier);
            return executor;
        }
    }

    /**
     *  invokes the method on the object passed in
     */
    public Object execute(Object o, InternalContextAdapter context)
    {
        AbstractExecutor executor = null;

        try
        {
            Class c = o.getClass();

            /*
             *  first, see if we have this information cached.
             */

            IntrospectionCacheData icd = context.icacheGet( this );

            /*
             * if we have the cache data and the class of the object we are 
             * invoked with is the same as that in the cache, then we must
             * be allright.  The last 'variable' is the method name, and 
             * that is fixed in the template :)
             */

            if ( icd != null && icd.contextData == c )
            {
                 executor = ( AbstractExecutor ) icd.thingy;
            }
            else
            {
                /*
                 *  otherwise, do the introspection, and cache it
                 */

                executor = doIntrospection(  c );
                
                if (executor != null)
                {    
                    icd = new IntrospectionCacheData();
                    icd.contextData = c;
                    icd.thingy = executor;
                    context.icachePut( this, icd );
                }
            }

        }
        catch( Exception e)
        {
            System.out.println("ASTIdentifier.execute() : identifier = " + identifier + " : " + e );
        }

        if (executor != null)
            return executor.execute(o, context);
        else
            return null;
    }
}
