/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.management;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import static javax.management.AttributeChangeNotification.ATTRIBUTE_CHANGE;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.DescriptorSupport;
import org.vesalainen.bean.BeanHelper;

/**
 * Base class for DynamicMBean
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractDynamicMBean implements DynamicMBean, NotificationBroadcaster, MBeanRegistration
{
    private final Map<String,AttributeImpl> attributes = new TreeMap<>();
    private final Map<String,OperationImpl> operations = new TreeMap<>();
    private final String description;
    private final NotificationBroadcasterSupport notificationBroadcasterSupport;
    private Descriptor descriptor = new DescriptorSupport();
    private long sequence;
    private MBeanServer server;
    protected ObjectName objectName;
    private String classname;
    /**
     * Create AbstractDynamicMBean with description and target. All targets
     * valid attribute methods are added as attributes. Attribute is valid
     * if it is standard java attribute (get/set/is) and it's object type
     * is open-type.
     * @param description
     * @param target 
     * @see javax.management.openmbean.OpenType
     */
    public AbstractDynamicMBean(String description, Object target)
    {
        this(description);
        addAttributes(target);
        this.classname = target.getClass().getName();
    }
    /**
     * Create AbstractDynamicMBean with description and target. All targets
     * @param description 
     */
    public AbstractDynamicMBean(String description)
    {
        this.classname = this.getClass().getName();
        this.description = description;
        this.notificationBroadcasterSupport = new NotificationBroadcasterSupport(new MBeanNotificationInfo(new String[]{ATTRIBUTE_CHANGE}, AttributeChangeNotification.class.getName(), "changed"));
        descriptor.setField(JMX.IMMUTABLE_INFO_FIELD, "false");
    }
    /**
     * Adds operation.Convenience method to extract methodName from target.
     * @param target
     * @param methodName 
     * @param params 
     * @see javax.management.openmbean.OpenType
     */
    public final void addOperation(Object target, String methodName, Class<?>... params)
    {
        try
        {
            Method method = target.getClass().getMethod(methodName, params);
            addOperation(target, method);
        }
        catch (NoSuchMethodException | SecurityException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    /**
     * Add operation.
     * @param target
     * @param method 
     * @see javax.management.openmbean.OpenType
     */
    public final void addOperation(Object target, Method method)
    {
        operations.put(method.getName(), new OperationImpl(target, method));
    }
    /**
     * Add functional attribute. Type must be open-type.
     * @param <T>
     * @param name
     * @param type
     * @param getter
     * @param setter 
     * @see javax.management.openmbean.OpenType
     */
    public final <T> void addAttribute(String name, Class<T> type, Supplier<T> getter, Consumer<T> setter)
    {
        if (!OpenTypeUtil.isOpenType(type))
        {
            throw new IllegalArgumentException(type+" not open type");
        }
        attributes.put(name, new AttributeImpl(name, type, getter, setter));
    }
    /**
     * Adds all valid attributes from target.
     * @param target 
     */
    public final void addAttributes(Object target)
    {
        Set<String> attrs = new TreeSet<>();
        Map<String,Method> getters = new HashMap<>();
        Map<String,Method> setters = new HashMap<>();
        try
        {
            Class<? extends Object> cls = target.getClass();
            for (Method method : cls.getMethods())
            {
                if (isGetter(method))
                {
                    String property = BeanHelper.getProperty(method);
                    attrs.add(property);
                    getters.put(property, method);
                }
                else
                {
                    if (isSetter(method))
                    {
                        String property = BeanHelper.getProperty(method);
                        attrs.add(property);
                        setters.put(property, method);
                    }
                }
            }
            for (String property : attrs)
            {
                Method getter = getters.get(property);
                Method setter = setters.get(property);
                attributes.put(property, new AttributeImpl(property, property, target, getter, setter));
            }
        }
        catch (IntrospectionException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        AttributeImpl a = attributes.get(attribute);
        if (a == null)
        {
            throw new AttributeNotFoundException(attribute);
        }
        return a.get();
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        AttributeImpl a = attributes.get(attribute.getName());
        if (a == null)
        {
            throw new AttributeNotFoundException(attribute.getName());
        }
        Object oldValue = a.get();
        Object newVAlue = attribute.getValue();
        a.set(newVAlue);
        notificationBroadcasterSupport.sendNotification(
                new AttributeChangeNotification(
                        this, 
                        sequence++, 
                        System.currentTimeMillis(),
                        "changed",
                        attribute.getName(),
                        a.getType(),
                        oldValue,
                        newVAlue
                ));
    }

    @Override
    public AttributeList getAttributes(String[] attributes)
    {
        AttributeList list = new AttributeList();
        for (String attribute : attributes)
        {
            try
            {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            }
            catch (AttributeNotFoundException | MBeanException | ReflectionException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes)
    {
        AttributeList list = new AttributeList();
        int size = attributes.size();
        for (int ii=0;ii<size;ii++)
        {
            Attribute a = (Attribute) attributes.get(ii);
            Attribute na = new Attribute(a.getName(), a.getValue());
            try
            {
                setAttribute(na);
            }
            catch (AttributeNotFoundException | InvalidAttributeValueException | MBeanException | ReflectionException ex)
            {
                throw new RuntimeException(ex);
            }
            list.add(na);
        }
        return list;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException
    {
        OperationImpl operation = operations.get(actionName);
        if (operation == null)
        {
            throw new IllegalArgumentException(operation+" not found");
        }
        return operation.invoke(params);
    }

    @Override
    public MBeanInfo getMBeanInfo()
    {
        return new MBeanInfo(
                classname, 
                description, 
                getAttributeInfo(), 
                null, 
                getOperationInfo(), 
                notificationBroadcasterSupport.getNotificationInfo(), 
                descriptor);
    }
    /**
     * Register with platform mbean server with default name.
     * @see org.vesalainen.management.AbstractDynamicMBean#createObjectName() 
     */
    public void register()
    {
        register(null);
    }
    /**
     * Register with platform mbean server using given name.
     * @param name 
     */
    public void register(ObjectName name)
    {
        try
        {
            MBeanServer pbs = ManagementFactory.getPlatformMBeanServer();
            if (objectName != null)
            {
                unregister();
            }
            pbs.registerMBean(this, name);
        }
        catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    /**
     * Unregister with platform mbean server
     */
    public void unregister()
    {
        try
        {
            MBeanServer pbs = ManagementFactory.getPlatformMBeanServer();
            pbs.unregisterMBean(objectName);
        }
        catch (MBeanRegistrationException | InstanceNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
    {
        this.server = server;
        this.objectName = name;
        if (objectName == null)
        {
            objectName = objectName();
        }
        return objectName;
    }

    @Override
    public void postRegister(Boolean registrationDone)
    {
        if (!registrationDone)
        {
            this.server = null;
        }
    }

    @Override
    public void preDeregister() throws Exception
    {
    }

    @Override
    public void postDeregister()
    {
        this.server = null;
        this.objectName = null;
    }

    private MBeanAttributeInfo[] getAttributeInfo()
    {
        MBeanAttributeInfo[] info = new MBeanAttributeInfo[attributes.size()];
        int index = 0;
        for (Entry<String, AttributeImpl> entry : attributes.entrySet())
        {
            info[index++] = entry.getValue();
        }
        return info;
    }
    private MBeanOperationInfo[] getOperationInfo()
    {
        MBeanOperationInfo[] info = new MBeanOperationInfo[operations.size()];
        int index = 0;
        for (Entry<String, OperationImpl> entry : operations.entrySet())
        {
            info[index++] = entry.getValue();
        }
        return info;
    }
    @Override
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
    {
        notificationBroadcasterSupport.addNotificationListener(listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
    {
        notificationBroadcasterSupport.removeNotificationListener(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo()
    {
        return notificationBroadcasterSupport.getNotificationInfo();
    }

    private boolean isGetter(Method method)
    {
        return 
                BeanHelper.isGetter(method) &&
                method.getParameterTypes().length == 0 &&
                OpenTypeUtil.isOpenType(method.getReturnType())
                ;
    }

    private boolean isSetter(Method method)
    {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return 
                BeanHelper.isSetter(method) &&
                void.class.equals(method.getReturnType()) &&
                parameterTypes.length == 1 &&
                OpenTypeUtil.isOpenType(parameterTypes[0])
                ;
    }

    private ObjectName objectName()
    {
            try
            {
                return createObjectName();
            }
            catch (MalformedObjectNameException ex)
            {
                throw new RuntimeException(ex);
            }
    }
    protected abstract ObjectName createObjectName() throws MalformedObjectNameException;

    private static class OperationImpl extends MBeanOperationInfo
    {
        private final Method method;
        private final Object target;
        
        public OperationImpl(Object target, Method method)
        {
            super(method.getName(), method);
            this.target = target;
            this.method = method;
        }
        
        public Object invoke(Object[] params) throws MBeanException, ReflectionException
        {
            try
            {
                return method.invoke(target, params);
            }
            catch (IllegalAccessException | IllegalArgumentException ex)
            {
                throw new ReflectionException(ex);
            }
            catch (InvocationTargetException ex)
            {
                throw new MBeanException(ex);
            }
        }

    }
    private static class AttributeImpl<T> extends MBeanAttributeInfo
    {
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        public AttributeImpl(String name, Class<T> type, Supplier<T> getter, Consumer<T> setter)
        {
            super(name, type.getName(), name, getter != null, setter != null, getter != null && (type==boolean.class || type==Boolean.class));
            this.getter = getter;
            this.setter = setter;
        }

        public AttributeImpl(String name, String description, Object target, Method getter, Method setter) throws IntrospectionException
        {
            super(name, description, getter, setter);
            if (getter != null)
            {
                this.getter = ()->
                {
                    try
                    {
                        return (T) getter.invoke(target);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                };
            }
            else
            {
                this.getter = null;
            }
            if (setter != null)
            {
                this.setter = (v)->
                {
                    try
                    {
                        setter.invoke(target, v);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                };
            }
            else
            {
                this.setter = null;
            }
        }

        public Object get() throws ReflectionException
        {
            return getter.get();
        }

        public void set(T value) throws ReflectionException
        {
            setter.accept(value);
        }

    }
    
}
