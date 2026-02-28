package dev.jvmai.diagnostic;

import com.sun.tools.attach.VirtualMachine;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;

public class JvmConnection implements AutoCloseable {

    private final VirtualMachine vm;
    private final JMXConnector connector;
    private final MBeanServerConnection serverConnection;

    public JvmConnection(long pid) {
        try {
            this.vm = VirtualMachine.attach(String.valueOf(pid));
            String connectorAddress = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            if (connectorAddress == null) {
                String javaHome = vm.getSystemProperties().getProperty("java.home");
                String agent = javaHome + File.separator + "lib" + File.separator + "management-agent.jar";
                vm.loadAgent(agent);
                connectorAddress = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            }
            if (connectorAddress == null) {
                throw new JvmAttachException("Failed to get local JMX connector address");
            }

            JMXServiceURL url = new JMXServiceURL(connectorAddress);
            this.connector = JMXConnectorFactory.connect(url);
            this.serverConnection = connector.getMBeanServerConnection();

        } catch (Exception e) {
            throw new JvmAttachException("Failed to attach to JVM pid " + pid, e);
        }
    }

    public MBeanServerConnection getConnection() {
        return serverConnection;
    }

    @Override
    public void close() throws IOException {
        if (connector != null) {
            try {
                connector.close();
            } catch (IOException e) {
                // ignore
            }
        }
        if (vm != null) {
            try {
                vm.detach();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
