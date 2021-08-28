/**
 * Provides classes for a simple JMX implementation
 * <p>
 * Reason for this package is that current JMX implementation doesn't clean
 * itself up after client (jconsole) quits. Especially notifications go on
 * sending forever. Not in network of course but inside the application. Notifications
 * are created and garbage collected for no reason.
 */
package org.vesalainen.jmx;
