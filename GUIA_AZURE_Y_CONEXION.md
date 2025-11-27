# GUÍA DE DEPLOYMENT A AZURE Y CONEXIÓN REMOTA

## 📋 Requisitos Previos

- Azure VM configurada con IP: **68.211.112.149**
- Usuario: **azureuser**
- Contraseña: (la que configuraste en Azure)
- Puerto 8080 abierto en Azure Network Security Group  
- Java 11+ instalado en Azure VM
- SSH accesible desde tu computador

---

## 🚀 PASO 1: Desplegar Servidor a Azure

### Opción A: Por Scripts (Recomendado si tienes contrasña)

**En Windows (PowerShell):**
```powershell
# Compilar el proyecto recién optimizado
mvn clean package

# Desplegar a Azure VM (se pedirá contraseña SSH)
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682

# Copiar JAR al servidor
scp server\target\server-1.0-SNAPSHOT-jar-with-dependencies.jar azureuser@68.211.112.149:/home/azureuser/battleship-server.jar

# Conectar y ejecutar
ssh azureuser@68.211.112.149
cd /home/azureuser
nohup java -jar battleship-server.jar > server.log 2>&1 &
exit
```

### Opción B: Deployment Manual Paso a Paso

```powershell
# 1. Compilar localmente
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682
mvn clean package

# 2. Conectar a Azure VM
ssh azureuser@68.211.112.149

# 3. Una vez conectado a Azure
cd /home/azureuser

# 4. Si hay servidor ejecutándose, detenerlo
ps aux | grep battleship-server
pkill -f battleship-server

# 5. En otra terminal local, copiar JAR
# (sal de SSH primero con exit o abre nueva terminal)
scp server\target\server-1.0-SNAPSHOT-jar-with-dependencies.jar azureuser@68.211.112.149:/home/azureuser/battleship-server.jar

# 6. Volver a SSH y ejecutar
ssh azureuser@68.211.112.149
cd /home/azureuser
nohup java -jar battleship-server.jar > server.log 2>&1 &

# 7. Verificar que está corriendo
tail -f server.log
# Deberías ver: "WebSocket server started on port 8080"
# Presiona Ctrl+C para salir de los logs
```

---

## 💻 PASO 2: Conectar Clientes desde Diferentes Computadores

### Configurar Cliente para Azure

Hay **3 formas** de configurar el cliente para conectarse al servidor Azure:

#### Método 1: Por Línea de Comandos (Más Fácil)

```powershell
# En el computador cliente
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682\client\target

# Ejecutar especificando host de Azure
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar 68.211.112.149 8080
```

#### Método 2: Por Variables de Entorno (Windows)

```powershell
# Configurar variables de entorno
$env:BATTLESHIP_HOST="68.211.112.149"
$env:BATTLESHIP_PORT="8080"

# Ejecutar cliente
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682\client\target
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### Método 3: Por System Properties

```powershell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682\client\target

java -Dbattleship.host=68.211.112.149 -Dbattleship.port=8080 -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 🌐 Conectar desde OTROS Computadores

**Pasos para cada computador adicional:**

1. **Copiar el JAR del cliente** a ese computador
   ```powershell
   # En el computador original
   # Copiar archivo a USB o enviar por email/drive:
   # client\target\client-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

2. **En cada computador, ejecutar:**
   ```powershell
   # Navegar al directorio donde copiaste el JAR
   java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar 68.211.112.149 8080
   ```

3. **Requisito:** Cada computador necesita tener Java 11 o superior instalado.

---

## 🔍 VERIFICACIÓN Y MONITOREO

### Verificar que el Servidor está Corriendo en Azure

```powershell
# Ver si el proceso está activo
ssh azureuser@68.211.112.149 "ps aux | grep battleship-server"

# Ver logs en tiempo real
ssh azureuser@68.211.112.149 "tail -f /home/azureuser/server.log"

# Ver últimas 20 líneas de log
ssh azureuser@68.211.112.149 "tail -20 /home/azureuser/server.log"

# Ver puerto 8080
ssh azureuser@68.211.112.149 "netstat -an | grep 8080"
```

### Logs Importantes a Buscar

**Servidor iniciado correctamente:**
```
INFO: WebSocket server started on port 8080
INFO: Game Service WebSocket initialized
```

**Cliente conectándose:**
```
INFO: New client connected: [session-id]
INFO: Join request from: [nombre-jugador]
INFO: Player [nombre] joined
```

---

## 🛠️ COMANDOS ÚTILES

### Detener el Servidor

```powershell
ssh azureuser@68.211.112.149 "pkill -f battleship-server"
```

### Reiniciar el Servidor

```powershell
ssh azureuser@68.211.112.149 "pkill -f battleship-server; cd /home/azureuser && nohup java -jar battleship-server.jar > server.log 2>&1 &"
```

### Limpiar Logs Antiguos

```powershell
ssh azureuser@68.211.112.149 "rm /home/azureuser/server.log"
```

---

## 🎮 JUGAR PARTIDA COMPLETA

### Escenario: 2 Jugadores en Diferentes Computadores

**Jugador 1 (Computador A):**
```powershell
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar 68.211.112.149 8080
# Ingresar nombre: "Juan"
```

**Jugador 2 (Computador B):**
```powershell
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar 68.211.112.149 8080
# Ingresar nombre: "María"
```

**Pasos:**
1. Ambos jugadores colocan sus 5 barcos
2. Ambos hacen clic en "✅ ¡LISTO PARA JUGAR!"
3. El juego comienza automáticamente
4. Se turnan para atacar

---

## ⚠️ TROUBLESHOOTING

### Problema: Cliente no puede conectar

**Verificar:**
1. ¿El servidor está corriendo en Azure?
   ```powershell
   ssh azureuser@68.211.112.149 "ps aux | grep battleship-server"
   ```

2. ¿El puerto 8080 está abierto en Azure NSG?
   - Ir a Azure Portal → VM → Networking → Add inbound port rule
   - Puerto: 8080, Protocolo: TCP

3. ¿La IP es correcta? (68.211.112.149)

### Problema: Conexión lenta o timeouts

**Solución:** Las optimizaciones ya implementadas deberían ayudar:
- Timeout aumentado a 120 segundos
- Heartbeat cada 5 segundos
- Envíos asíncronos

### Problema: Servidor se cae durante el juego

**Ver logs:**
```powershell
ssh azureuser@68.211.112.149 "tail -50 /home/azureuser/server.log"
```

**Solución común:** Reiniciar servidor
```powershell
ssh azureuser@68.211.112.149 "pkill -f battleship-server; cd /home/azureuser && nohup java -jar battleship-server.jar > server.log 2>&1 &"
```

---

## 📝 NOTAS IMPORTANTES

> [!IMPORTANT]
> **Firewall de Windows:** Si tienes problemas de conexión, asegúrate de que Java tenga permisos en el firewall de Windows para conexiones salientes.

> [!TIP]
> **Distribución del JAR:** Puedes compartir SOLO el archivo `client-1.0-SNAPSHOT-jar-with-dependencies.jar` con otros jugadores. No necesitan el código fuente ni Maven.

> [!NOTE]
> **Puerto del Servidor:** El servidor siempre escucha en el puerto 8080. Si necesitas cambiarlo, debes modificar el código fuente en `ServerMainWebSocket.java`.

---

## 🎯 RESUMEN RÁPIDO

**Para desplegar:**
```powershell
mvn clean package
scp server\target\server-1.0-SNAPSHOT-jar-with-dependencies.jar azureuser@68.211.112.149:/home/azureuser/battleship-server.jar
ssh azureuser@68.211.112.149 "nohup java -jar battleship-server.jar > server.log 2>&1 &"
```

**Para conectar cliente:**
```powershell
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar 68.211.112.149 8080
```

**Para verificar:**
```powershell
ssh azureuser@68.211.112.149 "tail -f server.log"
```

¡Listo! 🚢 Tu servidor de Batalla Naval optimizado está corriendo en Azure y puedes conectar desde múltiples computadores.
