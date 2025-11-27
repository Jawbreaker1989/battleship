# 🚀 GUÍA PRÁCTICA: SETUP, COMPILACIÓN Y EJECUCIÓN


## PARTE 2: DESCARGAR Y PREPARAR PROYECTO

### Paso 2.1: Clonar o Descargar Proyecto

```powershell
# Opción 1: Clonar con Git (si tienes Git instalado)
cd d:\OneDrive\Escritorio\vscode-projects
git clone https://github.com/Jawbreaker1989/battleship.git
cd battleship
git checkout socketweb

# Opción 2: Descargar ZIP desde GitHub
# 1. Ir a: https://github.com/Jawbreaker1989/battleship
# 2. Code > Download ZIP
# 3. Extraer en d:\OneDrive\Escritorio\vscode-projects\
```

### Paso 2.2: Verificar Estructura del Proyecto

```powershell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682

# Ver carpetas principales
ls

# Salida esperada:
# client/
# shared/
# server/
# cliente_final/
# pom.xml
# .git/
```

---

## PARTE 3: COMPILACIÓN DEL PROYECTO

### Paso 3.1: Compilar Módulo Shared

```powershell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682

# Compilar shared (modelos compartidos)
mvn -f shared/pom.xml clean install
```

**Salida esperada:**
```
[INFO] Scanning for projects...
[INFO] 
[INFO] --------< co.edu.uptc:shared >--------
[INFO] Building shared 1.0-SNAPSHOT
[INFO] --- maven-clean-plugin:3.0.0:clean @ shared ---
[INFO] --- maven-compiler-plugin:3.11.0:compile @ shared ---
[INFO] --- maven-jar-plugin:3.3.0:jar @ shared ---
[INFO] --- maven-install-plugin:3.0.0-M1:install @ shared ---
[INFO] 
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXs
```

### Paso 3.2: Compilar Servidor

```powershell
# Compilar servidor
mvn -f server/pom.xml clean package
```

**Verificar resultado:**
```powershell
# Ver tamaño del JAR (debe ser ~10-12 MB)
ls server/target/*.jar | Select-Object Name, Length
```

**Salida esperada:**
```
Name                                             Length
----                                             ------
battleship-server-jar-with-dependencies.jar  10546287
```

**Si falla compilación:**
- Leer errores completos en rojo
- Errores comunes:
  - "Compile failure" → Verificar `javac -version`
  - "Cannot find symbol" → `mvn clean install` en shared primero
  - "No such file" → Verificar ruta

### Paso 3.3: Compilar Cliente

```powershell
# Compilar cliente
mvn -f client/pom.xml clean package
```

**Verificar resultado:**
```powershell
ls client/target/*.jar | Select-Object Name, Length
```

**Salida esperada:**
```
Name                                            Length
----                                            ------
battleship-client-jar-with-dependencies.jar  12876543
```

### Paso 3.4: Compilar Todo (Alternativa)

Si prefieres compilar todo de una vez:

```powershell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682
mvn clean install
```

---

## PARTE 4: PRUEBA LOCAL (SIN AZURE)

### Paso 4.1: Terminal 1 - Ejecutar Servidor Local

```powershell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682
java -jar server/target/battleship-server-jar-with-dependencies.jar
```

**Salida esperada:**
```
🚀 Iniciando Servidor WebSocket de Batalla Naval...

╔═══════════════════════════════════════════════════════╗
║      SERVIDOR BATALLA NAVAL WEBSOCKET                 ║
╠═══════════════════════════════════════════════════════╣
║ 🌐 Host: 0.0.0.0                                      ║
║ 🔌 Puerto: 8080                                       ║
║ 📡 Endpoint: ws://host:8080/battleship                ║
║ 🎮 Capacidad: 2 jugadores simultáneos                ║
║ 📊 Estado: Esperando conexiones...                    ║
╚═══════════════════════════════════════════════════════╝

⚠️  Para detener el servidor presiona Ctrl+C
```

**Si ves esto, ✅ el servidor está corriendo.**

### Paso 4.2: Terminal 2 - Ejecutar Cliente 1

```powershell
# Abrir nueva terminal PowerShell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682
java -jar client/target/battleship-client-jar-with-dependencies.jar localhost 8080
```

**Salida esperada:**
- Se abre ventana gráfica
- Pide nombre del jugador
- Debe decir "Conectado al servidor"

### Paso 4.3: Terminal 3 - Ejecutar Cliente 2

```powershell
# Abrir OTRA nueva terminal PowerShell
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682
java -jar client/target/battleship-client-jar-with-dependencies.jar localhost 8080
```

**Resultado esperado:**
- Ambas ventanas de cliente se conectan
- Ven mensaje "Jugador conectado"
- Aparecen opciones para colocar barcos

### Paso 4.4: Jugar Partida de Prueba

1. **Cliente 1:**
   - Ingresa nombre (ej: "Juan")
   - Hace clic en tablero izquierdo
   - Coloca 5 barcos
   - Hace clic en "✅ ¡LISTO PARA JUGAR!"

2. **Cliente 2:**
   - Ingresa nombre (ej: "María")
   - Coloca 5 barcos
   - Hace clic en "✅ ¡LISTO PARA JUGAR!"

3. **Juego comienza:**
   - Se turnan para atacar
   - El primero a hundir todos los barcos gana

**Si todo funciona localmente, ✅ la lógica es sólida.**

---

## PARTE 5: DEPLOY A AZURE

### Paso 5.1: Verificar Azure VM

**Conectarse a Azure VM:**
```powershell
ssh azureuser@68.211.112.149
```

**Verificar Java en Azure:**
```bash
java -version
```

**Si NO está Java:**
```bash
sudo apt update
sudo apt install -y openjdk-11-jre-headless
java -version  # Verificar
```

### Paso 5.2: Verificar Firewall (NSG) en Azure

**En Azure Portal:**
1. Buscar "Network Security Groups"
2. Encontrar NSG de tu VM
3. Ir a "Inbound security rules"
4. Debe existir regla:
   - Priority: (ej: 100)
   - Name: "AllowPort8080" (o similar)
   - Source: Any (0.0.0.0/0) o tu IP específica
   - Destination: Any
   - Service: Custom
   - Protocol: TCP
   - Port: 8080
   - Action: Allow

**Si NO existe:**
1. Click "+ Add inbound security rule"
2. Configurar exactamente como arriba
3. Click "Add"

### Paso 5.3: Transferir JAR a Azure

**Desde tu computadora local:**
```powershell
# Ir a carpeta del proyecto
cd d:\OneDrive\Escritorio\vscode-projects\batlleship_CesarCaro_202221682

# Transferir archivo (pedirá contraseña)
scp server/target/battleship-server-jar-with-dependencies.jar azureuser@68.211.112.149:/home/azureuser/battleship-server.jar

# Verificar transferencia (conectarse a Azure)
ssh azureuser@68.211.112.149
ls -lh /home/azureuser/battleship-server.jar
# Debe mostrar un archivo ~10-12 MB
exit
```

### Paso 5.4: Ejecutar Servidor en Azure

**En Azure VM:**
```bash
# Conectarse a Azure
ssh azureuser@68.211.112.149

# Ir al directorio
cd /home/azureuser

# Ejecutar servidor (en segundo plano, con logs)
nohup java -jar battleship-server.jar > server.log 2>&1 &

# Ver el PID del servidor
ps aux | grep battleship-server

# Ver últimas líneas del log
tail -20 server.log
```

**Salida esperada en log:**
```
🚀 Iniciando Servidor WebSocket de Batalla Naval...
╔═══════════════════════════════════════════════════════╗
║      SERVIDOR BATALLA NAVAL WEBSOCKET                 ║
...
```

### Paso 5.5: Verificar Servidor Escuchando

**En Azure:**
```bash
netstat -tlnp | grep 8080
# o
ss -tlnp | grep 8080
```

**Salida esperada:**
```
tcp  0  0  0.0.0.0:8080  0.0.0.0:*  LISTEN  12345/java
```

**Si NO aparece:**
1. Verificar logs: `tail -50 /home/azureuser/server.log`
2. Verificar Java: `java -version`
3. Revisar errores de permisos: `ls -la /home/azureuser/battleship-server.jar`

### Paso 5.6: Conectar Cliente a Azure

**Desde tu computadora local:**
```powershell
java -jar client/target/battleship-client-jar-with-dependencies.jar 68.211.112.149 8080
```

**Salida esperada:**
- Se abre ventana de cliente
- Debe conectar sin errores
- Pide nombre del jugador

### Paso 5.7: Verificar Logs en Azure

**Ver logs en tiempo real:**
```bash
ssh azureuser@68.211.112.149
tail -f /home/azureuser/server.log
# Presiona Ctrl+C para salir
```

**Buscar errores:**
```bash
grep -i "error" /home/azureuser/server.log
```

---

## PARTE 6: TROUBLESHOOTING

### Problema: "Error: Unable to find java"

**Causa:** Java no está instalado o no está en PATH  
**Solución:**
```powershell
# Instalar JDK 11
# https://www.oracle.com/java/technologies/downloads/#java11
# Reinstalar PowerShell
java -version  # Debe funcionar
```

### Problema: "Error: Cannot find symbol"

**Causa:** Módulo shared no compiló primero  
**Solución:**
```powershell
mvn -f shared/pom.xml clean install
mvn -f server/pom.xml clean package
```

### Problema: Cliente no puede conectar a Azure

**Causas posibles:**
1. Servidor no está ejecutando en Azure
2. NSG no permite puerto 8080
3. Firewall de Windows bloquea
4. IP de Azure es incorrecta

**Soluciones:**
```powershell
# 1. Verificar conexión a Azure
Test-NetConnection -ComputerName 68.211.112.149 -Port 8080
# Debe dar: TcpTestSucceeded : True

# 2. Verificar servidor en Azure
ssh azureuser@68.211.112.149 "ps aux | grep battleship-server"

# 3. Ver firewall de Windows
Get-NetFirewallProfile

# 4. Revisar NSG en Azure Portal
```

### Problema: "Connection refused" en cliente

**Causa:** Servidor no está escuchando  
**Solución:**
```bash
# En Azure
netstat -tlnp | grep 8080

# Si no sale nada, servidor no está corriendo
ssh azureuser@68.211.112.149
cd /home/azureuser
nohup java -jar battleship-server.jar > server.log 2>&1 &
tail -f server.log
```

### Problema: Servidor se cae después de conectar

**Solución:** Ver logs completos
```bash
tail -100 /home/azureuser/server.log
```

---

## RESUMEN DE COMANDOS

**Compilar:**
```powershell
mvn clean install
```

**Ejecutar servidor local:**
```powershell
java -jar server/target/battleship-server-jar-with-dependencies.jar
```

**Ejecutar cliente local:**
```powershell
java -jar client/target/battleship-client-jar-with-dependencies.jar localhost 8080
```

**Ejecutar cliente remoto (Azure):**
```powershell
java -jar client/target/battleship-client-jar-with-dependencies.jar 68.211.112.149 8080
```

**Transferir a Azure:**
```powershell
scp server/target/battleship-server-jar-with-dependencies.jar azureuser@68.211.112.149:/home/azureuser/battleship-server.jar
```

**Ejecutar en Azure:**
```bash
nohup java -jar battleship-server.jar > server.log 2>&1 &
```

**Ver logs en Azure:**
```bash
tail -f /home/azureuser/server.log
```

---

