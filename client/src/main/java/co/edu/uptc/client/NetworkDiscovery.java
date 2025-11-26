package co.edu.uptc.client;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Utilidad para descubrir servidores RMI en la red local
 * Busca servidores de Batalla Naval en rangos de IP comunes
 */
public class NetworkDiscovery {
    private static final Logger LOGGER = Logger.getLogger(NetworkDiscovery.class.getName());
    private static final int DEFAULT_PORT = 1100;
    private static final int DISCOVERY_TIMEOUT_MS = 1000; // 1 segundo por IP para ser más rápido
    private static final int MAX_CONCURRENT_SCANS = 30; // Reducir hilos para evitar sobrecarga
    private static final int RMI_CONNECTION_TIMEOUT = 3000; // Timeout específico para RMI
    
    /**
     * Descubre servidores RMI en la red local
     * @return Lista de direcciones de servidores encontrados (IP:PORT)
     */
    public static List<String> discoverServers() {
        return discoverServers(DEFAULT_PORT);
    }
    
    /**
     * Descubre servidores RMI en la red local en un puerto específico
     * @param port Puerto a escanear
     * @return Lista de direcciones de servidores encontrados (IP:PORT)
     */
    public static List<String> discoverServers(int port) {
        List<String> servers = new ArrayList<>();
        Set<String> networkRanges = getNetworkRanges();
        
        System.out.println("🔍 Escaneando red local buscando servidores...");
        System.out.println("📡 Puerto objetivo: " + port);
        System.out.println("🌐 Rangos detectados: " + networkRanges);
        
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_SCANS);
        List<Future<String>> futures = new ArrayList<>();
        
        // Escanear cada rango de red
        for (String range : networkRanges) {
            List<String> ips = generateIPsInRange(range);
            System.out.println("📊 Escaneando " + ips.size() + " IPs en rango " + range);
            
            for (String ip : ips) {
                Future<String> future = executor.submit(() -> {
                    try {
                        if (testServer(ip, port)) {
                            String serverAddr = ip + ":" + port;
                            System.out.println("✅ Servidor encontrado: " + serverAddr);
                            return serverAddr;
                        }
                    } catch (Exception e) {
                        // Silencioso - es normal que muchas IPs no respondan
                    }
                    return null;
                });
                futures.add(future);
            }
        }
        
        // Recopilar resultados
        int completed = 0;
        for (Future<String> future : futures) {
            try {
                String result = future.get(DISCOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (result != null) {
                    servers.add(result);
                }
                completed++;
                if (completed % 20 == 0) {
                    System.out.println("📈 Progreso: " + completed + "/" + futures.size() + " IPs escaneadas");
                }
            } catch (TimeoutException e) {
                future.cancel(true);
            } catch (Exception e) {
                // Ignorar errores de conexión individuales
            }
        }
        
        executor.shutdownNow();
        
        System.out.println("🎯 Escaneo completado. Servidores encontrados: " + servers.size());
        return servers;
    }
    
    /**
     * Obtiene los rangos de red local basados en las interfaces activas
     */
    private static Set<String> getNetworkRanges() {
        Set<String> ranges = new HashSet<>();
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        String range = getNetworkRange(ip);
                        if (range != null) {
                            ranges.add(range);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error detectando interfaces de red: " + e.getMessage());
        }
        
        // Fallback a rangos comunes si no se detectó ninguno
        if (ranges.isEmpty()) {
            ranges.add("192.168.1.0/24");
            ranges.add("192.168.0.0/24");
            ranges.add("10.0.0.0/24");
        }
        
        return ranges;
    }
    
    /**
     * Determina el rango de red basado en una IP local
     */
    private static String getNetworkRange(String ip) {
        if (ip.startsWith("192.168.")) {
            // Extraer los primeros 3 octetos
            String[] parts = ip.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
            }
        } else if (ip.startsWith("10.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
            }
        } else if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 3) {
                int secondOctet = Integer.parseInt(parts[1]);
                if (secondOctet >= 16 && secondOctet <= 31) {
                    return parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
                }
            }
        }
        return null;
    }
    
    /**
     * Genera lista de IPs en un rango CIDR
     */
    private static List<String> generateIPsInRange(String cidr) {
        List<String> ips = new ArrayList<>();
        
        try {
            String[] parts = cidr.split("/");
            String network = parts[0];
            int prefix = Integer.parseInt(parts[1]);
            
            if (prefix == 24) {
                // Para /24, escanear .1 a .254
                String[] octets = network.split("\\.");
                String baseNetwork = octets[0] + "." + octets[1] + "." + octets[2] + ".";
                
                for (int i = 1; i <= 254; i++) {
                    ips.add(baseNetwork + i);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error generando IPs para rango " + cidr + ": " + e.getMessage());
        }
        
        return ips;
    }
    
    /**
     * Prueba si hay un servidor RMI válido en la IP y puerto dados
     */
    private static boolean testServer(String ip, int port) {
        try {
            // Configurar timeouts para conexiones RMI más agresivos
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", String.valueOf(RMI_CONNECTION_TIMEOUT));
            System.setProperty("sun.rmi.transport.tcp.readTimeout", String.valueOf(RMI_CONNECTION_TIMEOUT));
            
            // Primero probar conectividad básica de socket
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(ip, port), 1000);
            } catch (Exception e) {
                // Si no hay conectividad básica, no vale la pena probar RMI
                return false;
            }
            
            Registry registry = LocateRegistry.getRegistry(ip, port);
            
            // Intentar listar servicios con timeout corto
            String[] services = registry.list();
            
            // Verificar que existe el servicio GameService
            for (String service : services) {
                if ("GameService".equals(service)) {
                    // Doble verificación: intentar obtener el servicio
                    try {
                        registry.lookup(service);
                        return true;
                    } catch (Exception e) {
                        // El servicio existe pero no es accesible
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            // Conexión fallida o servidor no válido
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                LOGGER.fine("Timeout conectando a " + ip + ":" + port);
            }
        }
        
        return false;
    }
    
    /**
     * Método de conveniencia para obtener el primer servidor encontrado
     */
    public static String findFirstServer() {
        List<String> servers = discoverServers();
        return servers.isEmpty() ? null : servers.get(0);
    }
    
    /**
     * Método de conveniencia para obtener solo la IP del primer servidor
     */
    public static String findFirstServerIP() {
        String server = findFirstServer();
        if (server != null && server.contains(":")) {
            return server.split(":")[0];
        }
        return server;
    }
}